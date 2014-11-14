package db_proj;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

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
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
}
