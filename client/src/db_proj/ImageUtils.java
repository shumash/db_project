package db_proj;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

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
     * Writes image to file.
     */
    public static void saveImage(BufferedImage img, String file) throws IOException {
        ImageIO.write(img, "jpg", new File(file));
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

	public static Vector<Double> computeNormChannelDistanceSquared(PatchWrapper pw1, PatchWrapper pw2) {
		return computeNormChannelDistanceSquared(pw1.getImgVector(), pw2.getImgVector());
	}

    public static double[] rescale(double[] v1){
        double [] normalized = new double[v1.length];
        int elems = v1.length / 3;
        for (int i = 0; i < elems; ++i) {
            // divide L by 100
            // add 100 to U/V and divide by 200
            double L = v1[i*3];
            double U = v1[3*i + 1];
            double V = v1[3*i + 2];
            normalized[3*i] = L/100;
            normalized[3*i+1] = (U+100)/200;
            normalized[3*i+2] = (V+100)/200;
        }
        return normalized;
    }

    public static double[] normalize(double[] v){
        double lengthSquared = 0;
        for (int i = 0; i < v.length; i++) {
          lengthSquared += v[i]*v[i]  ;
        }
        double[] res = new double[v.length];
        double length = Math.sqrt(lengthSquared);
        for (int i = 0; i < v.length; i++) {
            res[i] = v[i]/length;
        }
        return res;

    }
    // to calculate the newly nomalized distances
    public static double computeNewDistance(double[] va, double[] vb) {
        double dis = 0;
        assert va.length == vb.length;
        double[] v1 = rescale(va);
        double[] v2 = rescale(vb);
        v1 = normalize(v1);
        v2 = normalize(v2);

        int elems = va.length;
        for (int i = 0; i < elems; ++i) {
                dis += Math.pow(v1[i] - v2[i], 2.0);
        }
        /*
        List<Double> list1 = new ArrayList<Double>();
        List<Double> list2 = new ArrayList<Double>();
        for (int i = 0; i < elems; i++) {
            list1.add(v1[i]);
            list2.add(v2[i]);
        }
        Collections.sort(list1);
        Collections.sort(list2);
        System.out.println("min in v1, v2 " + list1.get(0) + " " + list2.get(0));
        System.out.println("max in v1, v2 " + list1.get(elems-1) + " " + list2.get(elems-1));
        */


        return Math.sqrt(dis);
    }


    public static Vector<Double> computeNormChannelDistanceSquared(double[] v1, double[] v2) {
        assert v1.length == v2.length;
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

    public static BufferedImage scaleCrop(BufferedImage image) {
    	// Step 1: crop
    	int min_dim = Math.min(image.getHeight(), image.getWidth());
    	BufferedImage cropped = image.getSubimage(0, 0, min_dim, min_dim);

        BufferedImage newImage = new BufferedImage(Constants.getSmallSize(), Constants.getSmallSize(), image.getType());
		Graphics g = newImage.createGraphics();
		g.drawImage(cropped, 0, 0, Constants.getSmallSize(), Constants.getSmallSize(), null);
		g.dispose();
        return newImage;
    }

    public static void checkImageSizeValid(BufferedImage image, int pSize) {
        int iw = image.getWidth();
		if (iw % pSize != 0) {
			throw new RuntimeException("patch size not a factor of image width");
		}
		int ih = image.getHeight();
		if (ih % pSize != 0) {
			throw new RuntimeException("patch size not a factor of image height");
		}
    }

    public static List<BufferedImage> getSamplePatches(BufferedImage image, int pSize, Random rand) {
        ImageUtils.checkImageSizeValid(image, pSize);
		int horPatches = image.getWidth() / pSize;
		int vertPatches = image.getHeight() / pSize;

		// Get about every 5th patch
        List<BufferedImage> res = new ArrayList<BufferedImage>(horPatches * vertPatches);
		for (int i = 0; i < horPatches; i++) {
			for (int j = 0; j < vertPatches; j++) {
				int randomNum = rand.nextInt(1000);
				if (randomNum % 5 == 0) {
					BufferedImage patch = image.getSubimage(i * pSize,
							j * pSize,
							pSize,
							pSize);
					res.add(patch);
				}
			}
		}
        return res;
    }

    public static double[] getImgVector(BufferedImage img) {
        return  ImageUtils.toLuv(ImageUtils.toRgbVector(img));
    }

	public static int[] getPixelData(BufferedImage img, int x, int y) {
		int argb = img.getRGB(x, y);

		int rgb[] = new int[] {
		    (argb >> 16) & 0xff, //red
		    (argb >>  8) & 0xff, //green
		    (argb      ) & 0xff  //blue
		};
		return rgb;
	}

	public static BufferedImage thumbnail(BufferedImage image) {
		int min_dim = Math.min(image.getHeight(), image.getWidth());
    	BufferedImage cropped = image.getSubimage(0, 0, min_dim, min_dim);

        BufferedImage newImage = new BufferedImage(Constants.getPatchSize(), Constants.getPatchSize(), image.getType());
		Graphics g = newImage.createGraphics();
		g.drawImage(cropped, 0, 0, Constants.getPatchSize(), Constants.getPatchSize(), null);
		g.dispose();
        return newImage;
	}
}
