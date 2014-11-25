package db_proj;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.junit.Test;

public class StoreReconstructTest {
	
	public DatabaseClient establishConnection() {
		DbConnectionInfo info = new DbConnectionInfo();
		info.setLocalUrl("imgtest");
		info.setUserInfo("shumash", null);
		DatabaseClient dbc = new DatabaseClient();
		dbc.connect(info);
		return dbc;
	}
	
	@Test
	public void test() throws IOException, SQLException {
		DatabaseClient dbc = establishConnection();
		//dbc.clean();
		
		
		BufferedImage img = ImageUtils.loadImage("../tiny_data/pic4.png");		
		int id = dbc.storeImage(img, "test");
		//assertEquals(id, dbc.getImageId("test"));
		BufferedImage out = dbc.getImageReconstructed(id);
		
		ImageUtils.saveImage(out, "png", "/tmp/out_pic4_approx2_18.png");
	}
	
	@Test
	public void testOneDir() throws IOException, SQLException {
		DatabaseClient dbc = establishConnection();
		
		ArrayList<String> files = new ArrayList<String>();
		MiscUtils.listFilesForFolder(new File("../../../imgdata"), files);
		System.out.println("Num files: " + files.size());
		
		for (int i = 0; i < files.size(); ++i) {
			System.out.println("File: " + files.get(i));
			BufferedImage img = ImageUtils.loadImage("../../../imgdata/" + files.get(i));		
			int id = dbc.storeImage(img.getSubimage(0, 0, 100, 100), files.get(i));
		}
	}

}
