package db_proj;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.imageio.ImageIO;

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







	/** 
	 * SUPER SIMPLE DEBUG VERSION! 
	 * TODO: fix!
	 * 
	 * @param name
	 * @param image
	 * @throws IOException
	 * @throws SQLException
	 */
	public void storeImage(String name, BufferedImage image) throws IOException, SQLException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image,"png", os); 
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
		PreparedStatement ps = conn.prepareStatement("INSERT INTO images VALUES (?, ?)");
		ps.setString(1, name);
		ps.setBinaryStream(2, fis, (int)os.toByteArray().length);
		ps.executeUpdate();
		ps.close();
		fis.close();
	}

	/**
	 * SUPER SIMPLE DEBUG VERSION! 
	 * TODO: fix!
	 * @param name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public BufferedImage getImage(String name) throws SQLException, IOException {
		PreparedStatement ps = conn.prepareStatement("SELECT img FROM images WHERE imgname = ?");
		ps.setString(1, name);
		ResultSet rs = ps.executeQuery();
		BufferedImage res = null;
		if (rs.next()) {
			byte[] imgBytes = rs.getBytes(1);
			InputStream in = new ByteArrayInputStream(imgBytes);
			res = ImageIO.read(in);
		}
		rs.close();
		ps.close();
		return res;
	}

	public void storePatch(String number, BufferedImage image) throws IOException, SQLException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image,"png", os); 
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
		PreparedStatement ps = conn.prepareStatement("INSERT INTO patches VALUES (?, ?)");
		ps.setInt(1, Integer.parseInt(number));
		ps.setBinaryStream(2, fis, (int)os.toByteArray().length);
		ps.executeUpdate();
		ps.close();
		fis.close();

	}

	public BufferedImage getPatch(String num) throws NumberFormatException, SQLException, IOException {
		PreparedStatement ps = conn.prepareStatement("SELECT patch FROM patches WHERE id = ?");
		ps.setInt(1, Integer.parseInt(num));
		ResultSet rs = ps.executeQuery();
		BufferedImage res = null;
		if (rs.next()) {
			byte[] imgBytes = rs.getBytes(1);
			InputStream in = new ByteArrayInputStream(imgBytes);
			res = ImageIO.read(in);
		}
		rs.close();
		ps.close();
		return res;
	}

	public int getPatchTableSize() throws SQLException{
		PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM patches");
		ResultSet rs = ps.executeQuery();
		BufferedImage res = null;
		int size = 0;
		if (rs.next()) {
			size = rs.getInt(1);
		}
		rs.close();
		ps.close();
		return size;
	}

	public Integer maybeStorePatch(BufferedImage image, Constants.SimilarityType type){

		BufferedImage sim = PatchSearch.findMostSimilarPatch(this, image);
		Vector<Double> similarity = null;


		if (sim != null){
			similarity = ImageUtils.computeSimilarity(image, sim, type);
		}else{
			similarity = new Vector<Double>(); //make it the  min so it's always below threshold
			similarity.add(-Double.MAX_VALUE);
			similarity.add(-Double.MAX_VALUE);
			similarity.add(-Double.MAX_VALUE);
		}
		if ( aboveThreshold(similarity, Constants.getSingleton().getMinSimilarity())){ //TODO: this always returns false for now
			//TODO(AESPIELB): Would need to have the pointer number here too and return it
		}
		//return patch or create a new patch and return it

		//for now, just split it every time.

		return null;
	}

	//assumes both vectors are of the same length
	private boolean aboveThreshold(Vector<Double> similarity,
			Vector<Double> minSimilarity) {
		boolean retVal = true;
		for (int i = 0; i < similarity.size(); i++){
			retVal = retVal && similarity.get(i) >= minSimilarity.get(i);
		}
		return retVal;
	}

	public Vector<BufferedImage> splitIntoPatches(BufferedImage image){

		return null;

	}

	//TODO: Vector<BufferedImage>
	public Vector<PointerData> patchify(BufferedImage image, String patchSize) throws SQLException, IOException {
		int pSize = 0;
		Vector<PointerData> retVec = new Vector();
		try { 
			pSize = Integer.parseInt(patchSize); 
		} catch(NumberFormatException e) { 
			throw new RuntimeException("Argument must be a patch index");
		}
		int iw = image.getWidth();
		if (iw % pSize != 0){
			throw new RuntimeException("patch size not a factor of image width");
		}
		int ih = image.getHeight();
		if (ih % pSize != 0){
			throw new RuntimeException("patch size not a factor of image height");
		}
		int numPatches = this.getPatchTableSize();


		int horPatches = image.getWidth() / pSize;
		int vertPatches = image.getHeight() / pSize;
		for (int i = 0; i < horPatches; i++){
			for (int j = 0; j < vertPatches; j++){
				//first create the subpatch

				BufferedImage patch = image.getSubimage(i * horPatches, 
						j * vertPatches, 
						pSize, 
						pSize);

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(patch,"png", os); 
				InputStream fis = new ByteArrayInputStream(os.toByteArray());
				PreparedStatement ps = conn.prepareStatement("INSERT INTO patches VALUES (?, ?)");
				Integer pointerNum = maybeStorePatch(patch, Constants.SimilarityType.EUCLIDEAN); //TODO(AESPIELB): Refactor to take in type somewhere
				if (pointerNum == null){
					numPatches++;
					pointerNum = numPatches;
				}
				retVec.add(new PointerData(pointerNum, i, j));
				ps.setInt(1, numPatches);
				ps.setBinaryStream(2, fis, (int)os.toByteArray().length);
				ps.executeUpdate();
				ps.close();
				fis.close();
			}
		}

		return retVec;

	}

	public void storePointers(Vector<Integer> patchNumbers, String imgName) throws SQLException {
		for (int i = 0; i < patchNumbers.size(); i++){
			Object o = patchNumbers.get(i);
			PointerData pd  = (PointerData)o;
			PreparedStatement ps = conn.prepareStatement("INSERT INTO patch_pointers VALUES (?, ?, ?, ?)");
			ps.setString(1, imgName);
			ps.setInt(2, pd.patchNum.intValue());
			ps.setInt(3, pd.x.intValue());
			ps.setInt(4, pd.y.intValue());
			ps.executeUpdate();
			ps.close();
		}

	}

	public Vector<PointerData> getPatches(String imgName) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT patch_id, x, y FROM patch_pointers WHERE from_image = ?");
		ps.setString(1, imgName);
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

	public BufferedImage reconstructImage(Vector<PointerData> patches) throws SQLException, IOException {
		//first get the images:
		System.out.println("reconstructImage!");
		Vector<BufferedImage> images = new Vector<BufferedImage>();

		StringBuffer sb = new StringBuffer("SELECT patch FROM patches WHERE id in (");
		for (PointerData patch : patches){
			sb.append(patch.patchNum);
			sb.append(", ");
		}
		String str = sb.substring(0, sb.length() - 2);
		sb = new StringBuffer(str);
		sb.append(")");
		System.out.println(sb);
		PreparedStatement ps  = conn.prepareStatement(sb.toString());
		ResultSet rs = ps.executeQuery();


		while (rs.next()) {   
			byte[] imgBytes = rs.getBytes(1);
			InputStream in = new ByteArrayInputStream(imgBytes);
			BufferedImage res = ImageIO.read(in);
			images.add(res);

		}

		int maxX = -1;
		int maxY = -1;
		int width = images.get(0).getWidth();
		int height = images.get(0).getHeight();
		int type = images.get(0).getType();

		for (PointerData patch : patches){
			if (patch.x > maxX){
				maxX = patch.x;
			}
			if (patch.y > maxY){
				maxY = patch.y;
			}
		}

		//create empty image of size width*x + height*y
		BufferedImage stitchedImage = new BufferedImage((maxX + 1) * width, (maxY + 1) * height, type);

		//now let' stitch them together
		for (int i = 0; i < patches.size(); i++){
			PointerData patch = patches.get(i);
			BufferedImage image = images.get(i);

			//set rgb values:
			for (int x = 0; x < width; x++ ){
				for (int y = 0; y < height; y++ ){
					/*
					System.out.println("Height is " + stitchedImage.getHeight());
					System.out.println("Width is " + stitchedImage.getHeight());
					System.out.println(patch.x);
					System.out.println(width);
					System.out.println(x);
					System.out.println(patch.y);
					System.out.println(height);
					System.out.println(y);
					System.out.println("Accessing x " + patch.x*width + x);
					System.out.println("Accessing y " + patch.y*height + y);
					*/
					try{
						System.out.println(image.getRGB(x, y));
					}
					catch(Exception e){
						System.out.println("Uh oh");
					}
					stitchedImage.setRGB(patch.x*width + x, patch.y* height + y, image.getRGB(x, y));
				}
			}


		}

		return stitchedImage;
	}

	public void clean() throws SQLException, IOException {
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("DROP TABLE patch_pointers");
			ps.executeQuery();
		}catch(SQLException e){

		}

		try{
			ps = conn.prepareStatement("DROP TABLE images");
			ps.executeQuery();
		}catch(SQLException e){

		}

		try{
			ps = conn.prepareStatement("DROP TABLE patches");
			ps.executeQuery();
		}catch(SQLException e){

		}


		Runtime.getRuntime().exec( "psql -d imgtest -f ../schemas/schema.sql" );


		if (ps != null){
			ps.close();
		}
	}
}
