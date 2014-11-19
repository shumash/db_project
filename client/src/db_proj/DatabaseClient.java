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
	
	
	private class PointerData{
		
		public Integer patchNum;
		public Integer x;
		public Integer y;
		
		public PointerData(Integer patchNum, Integer x, Integer y){
			this.patchNum = patchNum;
			this.x = x;
			this.y = y;
		}
		
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

	public Vector patchify(BufferedImage image, String patchSize) throws SQLException, IOException {
		int pSize = 0;
		Vector retVec = new Vector();
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
				numPatches++;
				retVec.add(new PointerData(new Integer(numPatches), new Integer(i), new Integer(j)));
				ps.setInt(1, numPatches);
				ps.setBinaryStream(2, fis, (int)os.toByteArray().length);
				ps.executeUpdate();
				ps.close();
				fis.close();
			}
		}

		return retVec;

	}

	public void storePointers(Vector patchNumbers, String imgName) throws SQLException {
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
}
