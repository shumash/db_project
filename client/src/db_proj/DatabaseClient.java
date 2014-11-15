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
}
