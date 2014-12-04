package db_proj;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

/**
 * Encapsulates low-level image utilities, mostly as static methods.
 */
public class ImageUtils {
	
	/**
	 * Loads image from filename.
	 * @param filename
	 * @return image if succeeded
	 * @throws IOException
	 */
	public static BufferedImage loadImage(String filename) throws IOException {
		BufferedImage img = null;
		img = ImageIO.read(new File(filename));
		return img;
	}
	
	/**
	 * Loads image from a url.
	 * @param url fully specified url
	 * @return image if successful
	 * @throws IOException
	 */
	public static BufferedImage loadWebImage(String url) throws IOException {
		BufferedImage img = null;
		img = ImageIO.read(new URL(url));
		return img;
	}
	
	/**
	 * Creates a popup with an image.
	 * Note: popup may be hidden by current window.
	 * @param image must be non-null image
	 */
	public static void showImage(Image image) {
		JFrame f = new JFrame();
	    //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    ImageIcon icon = new ImageIcon(image);
	    JLabel lbl = new JLabel(icon);
	    f.getContentPane().add(lbl);
	    f.setSize(icon.getIconWidth(), icon.getIconHeight()); 
	    
	    // Position in the center
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (screenSize.width - f.getSize().width)/2;
	    int y = (screenSize.height - f.getSize().height)/2;
	    f.setLocation(x, y);
	    
	    f.setVisible(true);
	    
	    // TODO: make it pop up to the top
	    //Timer t = new Timer(3000,new ActionListener(){
          //  @Override
            //public void actionPerformed(ActionEvent arg0) {
                //java.awt.EventQueue.invokeLater(new Runnable() {
                  //  @Override
                    //public void run() {
                      //  f.toFront();
                        //f.repaint();
                    //}
                //});             
            //}   
        //});
        //t.setRepeats(false);
        //t.start();
	}
	
	public static void saveImage(BufferedImage image, String format, String file) throws IOException {
		ImageIO.write(image, format, new File(file));
	}
	
	public static double[] toRgbVector(BufferedImage img) {
		double [] res = new double[img.getWidth() * img.getHeight() * 3];
		for (int x = 0; x < img.getWidth(); ++x) {
			for (int y = 0; y < img.getHeight(); ++y) {
				Color rgb = new Color(img.getRGB(x, y));
				int idx = 3 * (y * img.getWidth() + x);
				res[idx] = rgb.getRed();
				res[idx + 1] = rgb.getGreen();
				res[idx + 2] = rgb.getBlue();
			}
		}
		return res;
	}
	
	public static double[] toLuv(double[] rgb) {
		double [] res = new double[rgb.length];
		
		int elems = rgb.length / 3;
		for (int i = 0; i < elems; ++i) {
			int idx = i * 3;
			double[] luv = LuvColor.toLuv(
					LuvColor.toXYZ(rgb[idx], rgb[idx + 1], rgb[idx + 2]));
			res[idx] = luv[0];
			res[idx + 1] = luv[1];
			res[idx + 2] = luv[2];
		}
		return res;
	}
	
	public static Vector<Double> computeDistance(BufferedImage imageA, BufferedImage imageB) {
		double[] v1 = toLuv(toRgbVector(imageA));
		double[] v2 = toLuv(toRgbVector(imageB));
		assert v1.length == v2.length;
		
		return computeDistance(v1, v2);
	}
	
	public static Vector<Double> computeDistance(double[] v1, double[] v2) {
		double [] dist = new double[3];
		dist[0] = dist[1] = dist[2] = 0.0;
		int elems = v1.length / 3;
		for (int i = 0; i < elems; ++i) {
			for (int c = 0; c < 3; ++c) {
				dist[c] += Math.pow(v1[i * 3 + c] - v2[i * 3 + c], 2.0) / elems;
			}
		}
		
		//System.out.println("Distance = " + dist[0] + ", " + dist[1] + ", " + dist[2]);
		Vector<Double> retVector = new Vector<Double>();
		retVector.add(dist[0]);
		retVector.add(dist[1]);
		retVector.add(dist[2]);
		return retVector;
	}
}
