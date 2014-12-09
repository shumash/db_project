package db_proj;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Experimental {

	public static DatabaseClient establishConnection() {
		DbConnectionInfo info = new DbConnectionInfo();
		info.setLocalUrl("imgtest");
		info.setUserInfo("shumash", null);
		DatabaseClient dbc = new DatabaseClient();
		dbc.connect(info);
		return dbc;
	}



	/**
	 * @param args
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws IOException, SQLException {
        //MockInsertImage();
        //DebugLoadImagesFromUrls();
		//DumpPatchVectorsToFile();
        ComputeDotProductDistributions();
    }

	public static void MockInsertImage() throws IOException, SQLException {
		DatabaseClient dbc = establishConnection();

		//BufferedImage img = ImageUtils.loadImage("../tiny_data/test2.jpg");
		//dbc.mockStoreImage(img, "mock");

		String folder = "../../../imgdata/IMG/tst";
		ArrayList<String> files = new ArrayList<String>();
		MiscUtils.listFilesForFolder(new File(folder), files);
		for (String filename : files) {
			String full_file = folder + "/" + filename;
			System.out.println("Reading file: " + full_file);
			BufferedImage img = ImageUtils.loadImage(full_file);
			dbc.mockStoreImage(img, "mock");
		}
	}

    public static void TestImgScale() throws IOException {
    	BufferedImage img = ImageUtils.loadImage("../tiny_data/test2.jpg");
    	BufferedImage scaled = ImageUtils.scaleCrop(img);
    	ImageUtils.saveImage(scaled, "/tmp/scaled_non_square.jpg");

        img = ImageUtils.loadImage("../tiny_data/star.jpg");
        scaled = ImageUtils.scaleCrop(img);
        ImageUtils.saveImage(scaled, "/tmp/scaled_tiny.jpg");
    }

    public static void ComputeDotProductDistributions() throws IOException {
        // Inputs
        String folder = "../../../imgdata/IMG/pca_dev";
        String pcaVecFile = "../data/pca25_vec_only.txt";

        // Outputs
        String outFile = "/tmp/dot_distr";
        String newVec = "/tmp/pca_vec_and_distr";

        PcaLshHelper helper = new PcaLshHelper(
            pcaVecFile, false /* no adaptive bins */);
        Random rand = new Random();

        PrintStream output = new PrintStream(new File(outFile));

        int count = 0;
        int fcount = 0;
        double[] sum_of_dots =
                {0.0, 0.0, 0.0, 0.0, 0.0,
                 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] sum_of_dots_squared =
                {0.0, 0.0, 0.0, 0.0, 0.0,
                 0.0, 0.0, 0.0, 0.0, 0.0};
        ArrayList<String> files = new ArrayList<String>();
        MiscUtils.listFilesForFolder(new File(folder), files);
        for (String filename : files) {
            ++fcount;
            String full_file = folder + "/" + filename;
            System.out.println(fcount + " - Reading file: " + full_file);
            BufferedImage img = ImageUtils.scaleCrop(ImageUtils.loadImage(full_file));
            List<BufferedImage> patches = ImageUtils.getSamplePatches(
                img, Constants.getPatchSize(), rand);
            for (BufferedImage patch_img : patches) {
                ++count;  // we count wrappers
                PatchWrapper pwrapper = new PatchWrapper(patch_img);

                // Next, we dot this with the PcaLshHelper
                double[] dot_v =
                        {0.0, 0.0, 0.0, 0.0, 0.0,
                         0.0, 0.0, 0.0, 0.0, 0.0};
                for (int c = 0; c < 10; ++c) {
                    double dot = helper.computeDot(c, pwrapper.getImgVector());
                    sum_of_dots[c] += dot;
                    sum_of_dots_squared[c] += (dot * dot);
                    dot_v[c] = dot;
                    output.print(dot + " ");
                }
                output.print("\n");
            }
        }

        // Next we dump projection vectors themselves
        PrintStream vecOutput = new PrintStream(new File(newVec));
        Scanner input = new Scanner(new File(pcaVecFile));
        String line = new String();
        for (int c = -1; c < 10; ++c) {
            while (line.length() == 0) {
                if (!input.hasNextLine()) {
                    SimpleTimer.timedLog("No line for c: " + c + "\n");
                    break;
                }
                line = input.nextLine();
                line.trim();
            }
            double mean = 0;
            double stdev = 0;
            if (c >= 0) {
                mean = sum_of_dots[c] / count;
                // sqrt( E[X^2] - E[X]^2 )
                stdev = Math.sqrt( sum_of_dots_squared[c] / count - mean * mean );
            }
            vecOutput.print(line + " " + mean + " " + stdev + "\n");
            SimpleTimer.timedLog("vector " + c + ": mu = " + mean + ", sigma = " + stdev + "\n");
            line = new String();
        }
    }

    public static void DumpPatchVectorsToFile() throws IOException {
        String folder = "../../../imgdata/IMG/pca_train";
        String outFile = "/tmp/train_patch_vectors";

        PrintStream output = new PrintStream(new File(outFile));
        Random rand = new Random();

        ArrayList<String> files = new ArrayList<String>();
        MiscUtils.listFilesForFolder(new File(folder), files);
        int count = 0;
        for (String filename : files) {
            String full_file = folder + "/" + filename;
            System.out.println(count + " - Reading file: " + full_file);
            BufferedImage img = ImageUtils.scaleCrop(ImageUtils.loadImage(full_file));
            List<BufferedImage> patches = ImageUtils.getSamplePatches(img, Constants.getPatchSize(), rand);
            for (BufferedImage patch_img : patches) {

                PatchWrapper pwrapper = new PatchWrapper(patch_img);
                MiscUtils.dumpToFile(pwrapper.getImgVector(), output);
                output.flush();
            }
            ++count;
        }
    }

    public static void DebugLoadImagesFromUrls() {
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
