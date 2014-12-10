package db_proj;

import java.awt.image.BufferedImage;
import java.util.Vector;

public class ImageColorStats {
	public double[] mean = { 0.0, 0.0, 0.0 };
    public double[] pNorm = { 0.0, 0.0, 0.0 };
    private double p = 4.0;

    private double[] pMax = { 0.1, 0.1, 0.1};

    // To get p-norm which penalizes outliers:
    //
	public ImageColorStats(double[] colorVec) {
        int elems = colorVec.length / 3;
		for (int i = 0; i < elems; ++i) {
            for (int c = 0; c < 3; ++c) {
                mean[c] += colorVec[i * 3 + c];
            }
        }

        for (int c = 0; c < 3; ++c) {
            mean[c] /= elems;
        }

        // Next, we compute a p-norm of the
        // (value - mean)^p
        // However! we want value-mean that is "ok" to be
        // < 1, and the rest to be > 1.
        // This can be accomplished using our regular quality thresholds.
        for (int i = 0; i < elems; ++i) {
            for (int c = 0; c < 3; ++c) {
                pNorm[c] += Math.pow(
                    (colorVec[i * 3 + c] - mean[c]) / Constants.getMaxDistance().get(c),
                    p);
            }
        }

        //mean_of_squares[c] += Math.pow(colorVec[i * 3 + c], 2.0);

        for (int c = 0; c < 3; ++c) {
            pNorm[c] = Math.pow(pNorm[c] / elems, 1/p);
        }
	}

    public LuvColor getMean() {
        return new LuvColor(mean[0], mean[1], mean[2]);
    }

    public boolean isUniform() {
        return pNorm[0] <= pMax[0] &&
                pNorm[1] <= pMax[1] &&
                pNorm[2] <= pMax[2];
    }

    public static boolean isLikelyUniformColor(BufferedImage img) {
    	Vector<int[]> intensities = new Vector<int[]>();
    	for(int i = 0; i < img.getHeight(); i++){
		    for(int j = 0; j < img.getWidth(); j++){
                int[] data = ImageUtils.getPixelData(img, j, i);
                intensities.add(data);
		    }
		}

		double[] sigma = MiscUtils.stdDev(intensities);

		double[] thresh = Constants.getUniformColorThresh();
		return sigma[0] < thresh[0] && sigma[1] < thresh[1] && sigma[2] < thresh[2];
    }
}
