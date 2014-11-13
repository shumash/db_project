package db_proj;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

public class ImageUtils {
	public static BufferedImage loadImage(String args) {
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File(args));
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return img;
	}
	
	public static void showImage(Image image) {
		JFrame f = new JFrame();
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //this is your screen size

	    //f.setUndecorated(true); //removes the surrounding border
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    ImageIcon icon = new ImageIcon(image);
	    JLabel lbl = new JLabel(icon); //puts the image into a jlabel
	    f.getContentPane().add(lbl); //puts label inside the jframe

	    f.setSize(icon.getIconWidth(), icon.getIconHeight()); //gets h and w of image and sets jframe to the size

	    int x = (screenSize.width - f.getSize().width)/2; //These two lines are the dimensions
	    int y = (screenSize.height - f.getSize().height)/2;//of the center of the screen

	    f.setLocation(x, y); //sets the location of the jframe
	    f.setVisible(true); //makes the jframe visible
	    
	    
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
