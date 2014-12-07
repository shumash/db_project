package db_proj;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Experimental {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String csvFile = "../data/manifest-sun2012.txt";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = "\t";
		String prefix = "http://people.csail.mit.edu/aespielberg/SUN2012/Images";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] url = line.split(cvsSplitBy);
				if (url[1].endsWith(".jpg")){
					String whole_url = prefix + url[1].substring(1);
					System.out.println(whole_url);
					BufferedImage image = ImageUtils.loadWebImage(whole_url);
					//ImageUtils.showImage(image);
					//return;
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		System.out.println("Done");
	}

}


