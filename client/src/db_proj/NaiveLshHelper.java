package db_proj;

import java.io.FileNotFoundException;

public class NaiveLshHelper extends LshHelper{
	private int binSize = 10; //500 * Constants.pixelsInPatch() / 8;
    private int odd_hashes = 0;
    private int mean_dot = 0;
    private int count = 0;

	// File of format
	// a0 a1 ... an b w
	NaiveLshHelper(String vectorFile) {
		try {
			initProjVectors(vectorFile,
                            Constants.getPatchSize() * Constants.getPatchSize() * 3);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void printInfo() {
        SimpleTimer.timedLog("Odd naive hashes: " + odd_hashes + "\n");
	}

    // Note: we expect the largest possible result to be:
    // maxL + maxU + maxV ~ 100 + 100 + 100 = 300
    // smallest:
    // ~ -100 -100 + 0
	public int computeHash(int hashId, double[] imgVec) {
        double[] hashVec = projVectors[hashId];
		double dot = 0;
		for (int i = 0; i < imgVec.length; ++i) {
			dot += hashVec[i] * imgVec[i];
		}
        dot += 50;
        if (dot < 0) {  // unlikely
            dot = 0;
            odd_hashes++;
        }
        int bin = (int) Math.floor(dot / binSize);
        if (bin > 8) {
            bin = 8;
            odd_hashes++;
        }
        return bin;
	}
}
