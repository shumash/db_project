package db_proj;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;


/** Holds a connection to a remote postgres server;
 * allows adding and removing images.
 * 
 * IMPORTANT: for now assumes existence of only one table, created
 * such as: CREATE TABLE images (imgname text, img bytea);
 * 
 * REQUIRES: postgress jar, obtained here (and also included on git)
 * http://jdbc.postgresql.org/documentation/91/classpath.html
 *
 */
public class DatabaseClient {
	// Reference: http://jdbc.postgresql.org/documentation/91/connect.html
	//            http://jdbc.postgresql.org/documentation/91/binary-data.html

	Connection conn = null;
	PatchCache patchCache = new PatchCache(Constants.PATCH_CACHE_SIZE);
	LshHelper lshHelper = new LshHelper();

	// TODO: add initialization info that configures table to which
	// images are added, etc.
	DatabaseClient() {}

	/**
	 * @return true if the server has a valid connection.
	 */
	public boolean connected() {
		return conn != null;
	}

	/**
	 * If connected, disconnects.
	 */
	public void disconnect() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			conn = null;
		}
	}

	/**
	 * Connects or reconnects to the database
	 * @param cInfo full connection info
	 * @return true on success
	 */
	public boolean connect(DbConnectionInfo cInfo) {
		try {
			// TODO: move to a static init method called only once
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(cInfo.getUrl(), cInfo.getProps());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return conn != null;
	}
	
	
	/** High-level image insert function which does the following:
	 * 1. stores the image itself in images DB
	 * 2. stores any patches that don't have close neighbors in the DB
	 * 3. get pointers of the patches that are already stored
	 * 4. stores patch pointer data for this image
	 * @throws SQLException 
	 * @throws IOException
	 * @return id of the stored image
	 */
	public int storeImage(BufferedImage image, String name) throws IOException, SQLException {
		
		
		BufferedImage newImage = new BufferedImage(Constants.getSmallSize(), Constants.getSmallSize(), image.getType());

		Graphics g = newImage.createGraphics();
		g.drawImage(image, 0, 0, Constants.getSmallSize(), Constants.getSmallSize(), null);
		g.dispose();
		image = newImage;
		
		
		
		int imgId = insertImage(image, name == null ? "" : name);
		Vector<PointerData> patchInfo = patchify(image, Constants.getPatchSize());
		storePointers(patchInfo, imgId);
		return imgId;
	}
	
	/**
	 * Gets original image from the images DB.
	 * @param id
	 * @return
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public BufferedImage getImageOriginal(int id) throws SQLException, IOException {
		return getImage(id);
	}
	
	/**
	 * Gets reconstructed image from images DB.
	 * @param id
	 * @return
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public BufferedImage getImageReconstructed(int id) throws SQLException, IOException {
		Vector<PointerData> patches = getPatches(id);
		BufferedImage res = reconstructImage(patches);
		return res;
	}
	
	/**
	 * Returns an id of image with this name; if multiple images - just returns one.
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public int getImageId(String name) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT id FROM images where imgname = ?");
		ps.setString(1, name);
		
		ResultSet rs = ps.executeQuery();
		int res = -1;
		if (rs.next()) {
			res = rs.getInt(1);
		} else {
			throw new SQLException("No image with name: " + name);
		}
		rs.close();
		ps.close();
		return res;
	}

	/** 
	 * LOW-LEVEL function: Stores just the image in the database.
	 * 
	 * @param image
	 * @param name
	 * @return image id in the database
	 * @throws IOException
	 * @throws SQLException
	 */
    public int insertImage(BufferedImage image, String name) throws IOException, SQLException {
		int id = getNextImageId();
		
		// TODO: add warning if img with same name already exists
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image,"png", os); 
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
		PreparedStatement ps = conn.prepareStatement("INSERT INTO images VALUES (?, ?, ?)");
		ps.setInt(1, id);
		ps.setString(2, name);
		ps.setBinaryStream(3, fis, (int)os.toByteArray().length);
		ps.executeUpdate();
		ps.close();
		fis.close();
		return id;
	}


	/** 
	 * LOW-LEVEL function: Stores just the patch in the database.
	 * 
	 * @param image
	 * @param hashes
	 * @return image id in the database
	 * @throws IOException
	 * @throws SQLException
	 */
	private int insertPatch(BufferedImage image, int[] hashes) throws IOException, SQLException {
		int id = getNextPatchId();
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image,"png", os); 
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO patches VALUES (?, ?)");
		ps.setInt(1, id);
		ps.setBinaryStream(2, fis, (int)os.toByteArray().length);
		ps.executeUpdate();
		ps.close();
		fis.close();
		
		insertHashes(id, hashes);
		return id;
	}
	
	private void insertHashes(int patchId, int[] hashes) throws SQLException {
		StringBuffer sb = new StringBuffer("INSERT INTO patch_hashes VALUES (?, ?)");
		
		PreparedStatement ps  = conn.prepareStatement(sb.toString());
        ps.setInt(1, patchId);
		ps.setInt(2, computeSingleIntegerHash(hashes));
		ps.executeUpdate();
		ps.close();
	}
	
	/**
	 * Gets image from images table.
	 * 
	 * @param id
	 * @return image or NULL
	 * @throws SQLException
	 * @throws IOException
	 */
	public BufferedImage getImage(int id) throws SQLException, IOException {
		return getImageDataById(id, "images");
	}
	
	/**
	 * Gets patch from patches table.
	 * 
	 * @param id
	 * @return image or NULL
	 * @throws SQLException
	 * @throws IOException
	 */
	public BufferedImage getPatch(int id) throws NumberFormatException, SQLException, IOException {
		BufferedImage res = patchCache.getPatch(id);
		if (res == null) {
			res = getImageDataById(id, "patches");
		}
		if (res != null) {
			patchCache.addPatch(id, res);
		}
		return res;
	}
	
	/**
	 * Encapsulates code to get image data from some table by ID.
	 * 
	 * @param id
	 * @return image or NULL
	 * @throws SQLException
	 * @throws IOException
	 */
	private BufferedImage getImageDataById(int id, String tableName) throws SQLException, IOException {
		PreparedStatement ps = conn.prepareStatement(
				"SELECT img FROM " + tableName + " WHERE id = ?");
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();
		BufferedImage res = null;
		if (rs.next()) {
			res = getImgFromRes(rs, 1);
		}
		rs.close();
		ps.close();
		return res;
	}
	
	private BufferedImage getImgFromRes(ResultSet rs, int resNumber) throws SQLException, IOException {
		byte[] imgBytes = rs.getBytes(resNumber);
		InputStream in = new ByteArrayInputStream(imgBytes);
		return ImageIO.read(in);
	}


	public int getPatchTableSize() throws SQLException{
		return getCountOf("patches");
	}
	
	public int getImageTableSize() throws SQLException {
		return getCountOf("images");
	}
	
	public int getNextImageId() throws SQLException {
		return getImageTableSize() + 1;
	}
	
	public int getNextPatchId() throws SQLException {
		return getPatchTableSize() + 1;
	}
	
	public int getCountOf(String tableName) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT MAX(id) FROM " + tableName);
		ResultSet rs = ps.executeQuery();
		int size = 0;
		if (rs.next()) {
			size = rs.getInt(1);
		}
		rs.close();
		ps.close();
		return size;
	}

	/**
	 * Stores the patch data for not-similar patches, and gets info for patches that already
	 * have good matches. DOES NOT STORE patch pointer data.
	 * 
	 * @param image
	 * @param pSize
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private Vector<PointerData> patchify(BufferedImage image, int pSize) throws SQLException, IOException {
		Vector<PointerData> retVec = new Vector<PointerData>();
		int iw = image.getWidth();
		if (iw % pSize != 0) {
			throw new RuntimeException("patch size not a factor of image width");
		}
		int ih = image.getHeight();
		if (ih % pSize != 0) {
			throw new RuntimeException("patch size not a factor of image height");
		}

		int horPatches = image.getWidth() / pSize;
		int vertPatches = image.getHeight() / pSize;
        List<BufferedImage> patches = new ArrayList<BufferedImage>();
		for (int i = 0; i < horPatches; i++){
			for (int j = 0; j < vertPatches; j++) {
				BufferedImage patch = image.getSubimage(i * pSize, 
						j * pSize, 
						pSize, 
						pSize);
                patches.add(patch);
			}
		}
        PatchDedup dedup = new PatchDedup(patches.toArray(new BufferedImage[patches.size()]));
        int initPatchId = getNextPatchId();
        Set<Integer> hashes = dedup.getUniqueHashesForThisImage();
        Map<Integer, Map<Integer, BufferedImage>> hashQueryResult = getExistingHashesWithPatchIdsAndPatches(hashes);
        dedup.setRemoteExistingHashes(patchCache, hashQueryResult, initPatchId);
        Map<Integer, BufferedImage> patchesToStore = dedup.getPatchesToStore();
        batchInsertPatches(patchesToStore);
        int[] pointersToStore = dedup.getPointersToStore();
        int index = 0;
        for (int i = 0; i < horPatches; i++) {
            for (int j = 0; j < vertPatches; j++) {
                retVec.add(new PointerData(pointersToStore[index], i * pSize, j * pSize));
                index++;
            }
        }
        return retVec;
	}

    private Vector<PointerData> patchifySlow(BufferedImage image, int pSize) throws SQLException, IOException {
        Vector<PointerData> retVec = new Vector<PointerData>();
        int iw = image.getWidth();
        if (iw % pSize != 0) {
            throw new RuntimeException("patch size not a factor of image width");
        }
        int ih = image.getHeight();
        if (ih % pSize != 0) {
            throw new RuntimeException("patch size not a factor of image height");
        }

        int horPatches = image.getWidth() / pSize;
        int vertPatches = image.getHeight() / pSize;
        for (int i = 0; i < horPatches; i++){
            for (int j = 0; j < vertPatches; j++) {
                BufferedImage patch = image.getSubimage(i * pSize,
                        j * pSize,
                        pSize,
                        pSize);

                int pointerNum = maybeInsertPatch(patch);
                retVec.add(new PointerData(pointerNum, i * pSize, j * pSize));
            }
        }

        return retVec;
    }


    private void batchInsertPatches(Map<Integer, BufferedImage> patchesToStore) throws SQLException, IOException {
        System.out.print("Writing patch data and hashes to database... ");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO patches VALUES (?, ?)");
        PreparedStatement psHash = conn.prepareStatement("INSERT INTO patch_hashes VALUES (?, ?)");

        for (Map.Entry<Integer, BufferedImage> entry : patchesToStore.entrySet()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(entry.getValue(),"png", os);
            InputStream fis = new ByteArrayInputStream(os.toByteArray());
            int hash = getHash(entry.getValue());

            ps.setInt(1, entry.getKey());
            ps.setBinaryStream(2, fis, (int)os.toByteArray().length);
            ps.addBatch();

            psHash.setInt(1, entry.getKey());
            psHash.setInt(2, hash);
            psHash.addBatch();
            fis.close();
        }
        ps.executeBatch();
        ps.close();
        psHash.executeBatch();
        psHash.close();
        System.out.println("done.");
    }


    private int getHash(BufferedImage patch) {
        double[] imgVector = ImageUtils.toLuv(ImageUtils.toRgbVector(patch));
        int[] hashes = lshHelper.getHashes(imgVector, 10);
        return computeSingleIntegerHash(hashes);
    }



    private Map<Integer, Map<Integer, BufferedImage>> getExistingHashesWithPatchIdsAndPatches(Set<Integer> hashes) throws SQLException, IOException {
        System.out.print("Querying database for similar patches... ");
        StringBuilder sb = new StringBuilder("SELECT patch_id, hash, img FROM patch_hashes inner join patches ON patch_id = patches.id WHERE hash in (");
        for (Integer hash : hashes) {
            sb.append(hash);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        PreparedStatement ps = conn.prepareStatement(sb.toString());
        ResultSet rs = ps.executeQuery();
        Map<Integer, Map<Integer, BufferedImage>> result = new HashMap<Integer, Map<Integer, BufferedImage>>();
        while(rs.next()) {
            int hash = rs.getInt("hash");
            int patchId = rs.getInt("patch_id");
            BufferedImage image = getImgFromRes(rs, 3);
            if (!result.containsKey(hash)) {
                result.put(hash, new HashMap<Integer, BufferedImage>());
            }
            result.get(hash).put(patchId, image);
        }
        System.out.println("done.");
        return result;
    }


    private void storePointers(Vector<PointerData> patchInfo, int imgId) throws SQLException {
        System.out.print("Writing patch pointers to database... ");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO patch_pointers VALUES (?, ?, ?, ?)");
		for (int i = 0; i < patchInfo.size(); i++){
			PointerData pd = patchInfo.get(i);
			ps.setInt(1, imgId);
			ps.setInt(2, pd.patchNum.intValue());
			ps.setInt(3, pd.x.intValue());
			ps.setInt(4, pd.y.intValue());
			ps.addBatch();
		}
        ps.executeBatch();
        ps.close();
        System.out.println("done.");
    }

	private Vector<PointerData> getPatches(int imgId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT patch_id, x, y FROM patch_pointers WHERE from_image = ?");
		ps.setInt(1, imgId);
		ResultSet rs = ps.executeQuery();

		Vector<PointerData> res = new Vector<PointerData>();
		while (rs.next()) {              
			int patch_id = rs.getInt("patch_id");
			int x = rs.getInt("x");
			int y = rs.getInt("y");

			res.add(new PointerData(patch_id, x, y));
		}

		rs.close();
		ps.close();
		return res;
	}

	private int getImageWidth(Vector<PointerData> patches) {
		int maxX = -1;
		for (PointerData patch : patches) {
			if (patch.x > maxX){
				maxX = patch.x;
			}
		}
		return maxX + Constants.getPatchSize();
	}
	
	private int getImageHeight(Vector<PointerData> patches) {
		int maxY = -1;
		for (PointerData patch : patches) {
			if (patch.y > maxY){
				maxY = patch.y;
			}
		}
		return maxY + Constants.getPatchSize();
	}
	
	private BufferedImage reconstructImage(Vector<PointerData> patches) throws SQLException, IOException {
		if (patches.size() == 0) {
			System.out.println("No patches provided!");
			return null;
		}
		
		// Note: these may not be in the right order, so we re-associate the data
		// with the patch by patchID
		StringBuffer sb = new StringBuffer("SELECT id, img FROM patches WHERE id in (");
		for (int i = 0; i < patches.size(); ++i) {
			if (i > 0) {
				sb.append(", ");
			}
			PointerData patch = patches.get(i);
			sb.append(patch.patchNum);
		}
		sb.append(")");
		PreparedStatement ps  = conn.prepareStatement(sb.toString());
		ResultSet rs = ps.executeQuery();

		int type = 0;
		HashMap<Integer, BufferedImage> patchImages = new HashMap<Integer, BufferedImage>();
		while (rs.next()) {
			int patchId = rs.getInt(1);
			BufferedImage patch = getImgFromRes(rs, 2);
			patchImages.put(patchId, patch);
			assert patch.getWidth() == Constants.getPatchSize();  // TODO: remove for speed
			type = patch.getType();
		}
		rs.close();
		ps.close();
		
		// Create empty image
		BufferedImage stitchedImage = new BufferedImage(getImageWidth(patches), getImageHeight(patches), type);

		//now let' stitch them together
		for (int i = 0; i < patches.size(); i++){
			PointerData patchData = patches.get(i);
			BufferedImage patch = patchImages.get(patchData.patchNum);
			
			Raster raster = Raster.createRaster(
					patch.getSampleModel(), patch.getData().getDataBuffer(), new Point(patchData.x, patchData.y));
			stitchedImage.setData(raster);
		}

		return stitchedImage;
	}
	
	public class SimilarPatchInfo {
		public BufferedImage patch;
		int patchId;
		Vector<Double> distance;
		
	}
	
	private Integer maybeInsertPatch(BufferedImage patch) throws IOException, SQLException {		
		double[] imgVector = ImageUtils.toLuv(ImageUtils.toRgbVector(patch));
		int[] hashes = lshHelper.getHashes(imgVector, 7);
		int[] hashes_alt = lshHelper.getHashes(imgVector, 7);
		
		SimilarPatchInfo sim = findMostSimilarPatch(patch, hashes, hashes_alt);
		
		if (sim != null && lessThan(sim.distance, Constants.getMaxDistance())) {
			return sim.patchId;  // just return the similar patch
		}
		
		return insertPatch(patch, hashes);
	}

	//assumes both vectors are of the same length
	private boolean lessThan(Vector<Double> v1, Vector<Double> v2) {
		boolean retVal = true;
		for (int i = 0; i < v1.size(); i++){
			retVal = retVal && v1.get(i) <= v2.get(i);
		}
		return retVal;
	}
	
	ArrayList<Integer> getLikelySimilarPatchIds(int[] hashes, int[] hashes_alt) throws SQLException {
        int hash = computeSingleIntegerHash(hashes);
		StringBuffer sb = new StringBuffer("SELECT patch_id FROM patch_hashes WHERE hash = " + hash);
		
		ArrayList<Integer> res = new ArrayList();
		PreparedStatement ps  = conn.prepareStatement(sb.toString());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int patchId = rs.getInt(1);
			res.add(patchId);
		}
		rs.close();
		ps.close();
		return res;
	}

    private int computeSingleIntegerHash(int[] hashes) {
        int result = 0;
        for(int i=0;i<hashes.length;i++) {
            result <<= 3;
            result += hashes[i];
        }
        return result;
    }

    public SimilarPatchInfo findMostSimilarPatch(
			BufferedImage patch, int[] hashes, int[] hashes_alt) throws SQLException, IOException {
		ArrayList<Integer> patchIds = getLikelySimilarPatchIds(hashes, hashes_alt);
		System.out.println("Selected " + patchIds.size() + " likely matches from " + getPatchTableSize() + " patches");
		
		SimilarPatchInfo approxBest = null;
		for (Integer patchId : patchIds) {
			SimilarPatchInfo sim = new SimilarPatchInfo();
			sim.patchId = patchId;
			sim.patch = getPatch(sim.patchId);
			sim.distance = ImageUtils.computeDistance(patch, sim.patch);
			if (approxBest == null || lessThan(sim.distance, approxBest.distance)) {
				approxBest = sim;
			}
		}

		if (Constants.ENABLE_BRUTE_NEAREST_NEIGHBOR) {
			SimilarPatchInfo trueBest = findMostSimilarPatchNaive(patch);

			if (trueBest != null && lessThan(trueBest.distance, Constants.getMaxDistance())) {
				if (approxBest == null) {
					System.out.println("TRUE BEST EXISTS --- but approximation FOUND NOTHING");
				} else if (approxBest.patchId != trueBest.patchId) {
					System.out.println("TRUE BEST EXISTS --- but DIFFERENT approximation found");

					//				int[] hashes2 = lshHelper.getHashes(ImageUtils.toLuv(ImageUtils.toRgbVector(getPatch(sim.patchId))), 3);
					//
					//				boolean all_match = true;
					//				for (int i = 0; i < 3; ++i) {
					//					boolean match = (hashes1[i] == hashes2[i]) || (hashes1_alt[i] == hashes2[i]);
					//					all_match = all_match && match;
					//					System.out.println("HASH#" + i + " - a " + (match ? "MATCH" : "MISS"));
					//					System.out.println("" + hashes2[i] + " vs. " + hashes1[i] + "," + hashes1_alt[i]);
					//				}
				}
			}
			return trueBest;
		}
		return approxBest;
	}
	
	private SimilarPatchInfo findMostSimilarPatchNaive(BufferedImage patch) throws SQLException, IOException {
		PreparedStatement ps = conn.prepareStatement("SELECT id FROM patches");
		ResultSet rs = ps.executeQuery();

		SimilarPatchInfo best = null;
		while (rs.next()) {
			SimilarPatchInfo sim = new SimilarPatchInfo();
			sim.patchId = rs.getInt("id");
			sim.patch = getPatch(sim.patchId);
			sim.distance = ImageUtils.computeDistance(patch, sim.patch);
			if (best == null || lessThan(sim.distance, best.distance)) {
				best = sim;
			}
		}
		rs.close();
		ps.close();
		
		return best;
	}

	public void clean(String db) throws SQLException, IOException {
		Process proc;
		
		if (db.equals("db")){
			proc = Runtime.getRuntime().exec("psql -h vise3.csail.mit.edu -U zoya -d zoya -f ../schemas/reset.sql");
		}else{
			proc = Runtime.getRuntime().exec( "psql -d " + db + " -f ../schemas/reset.sql");
		}
		
		InputStream stdin = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		BufferedReader br = new BufferedReader(isr);

		String line = null;


		while ( (line = br.readLine()) != null)
		     System.out.println(line);


		int exitVal = 0;
		try {
			exitVal = proc.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (db.equals("db")){
			proc = Runtime.getRuntime().exec( "psql -h vise3.csail.mit.edu -U zoya -d zoya -f ../schemas/schema.sql");
		}else{
			proc = Runtime.getRuntime().exec( "psql -d " + db + " -f ../schemas/schema.sql");
		}
		
		stdin = proc.getInputStream();
		isr = new InputStreamReader(stdin);
		br = new BufferedReader(isr);

		line = null;

		while ( (line = br.readLine()) != null)
		     System.out.println(line);


		exitVal = 0;
		try {
			exitVal = proc.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<String> randomSample(int number) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("select * from images order by random() limit ?");
		ps.setDouble(1, number);
		ResultSet rs = ps.executeQuery();
        List<String> result= new ArrayList<String>();
		while (rs.next()){
            result.add(rs.getString("imgname"));
		}
        return result;
		
	}

    public String getTableSize(String tableName) throws SQLException {
        StringBuffer sb = new StringBuffer("SELECT pg_size_pretty(pg_relation_size('");
        sb.append(tableName);
        sb.append("'))");
        PreparedStatement ps  = conn.prepareStatement(sb.toString());
        ResultSet rs = ps.executeQuery();
        rs.next();
        String value = rs.getString(1);
        rs.close();
        return value;
    }

    public String getDatabaseSize(String dbName) throws SQLException {
        StringBuffer sb = new StringBuffer("SELECT pg_size_pretty(pg_database_size('");
        sb.append(dbName);
        sb.append("'))");
        PreparedStatement ps  = conn.prepareStatement(sb.toString());
        ResultSet rs = ps.executeQuery();
        rs.next();
        String value = rs.getString(1);
        rs.close();
        return value;


    }

    public String[] getAllSizes(){
        String[] result = new String[5];
        List<String> names = new ArrayList<String>();
        names.add("patches");
        names.add("patch_pointers");
        names.add("patch_hashes");
        names.add("images");

        try {
            result[0]=getDatabaseSize("zoya");
            for(int i=1; i<5;i++){
                result[i]=getTableSize(names.get(i-1));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //return 0 if things went wrong;
        return result;
    }

}
