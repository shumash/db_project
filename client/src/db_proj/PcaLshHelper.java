package db_proj;

import java.io.FileNotFoundException;

public class PcaLshHelper extends LshHelper{
	// File of format
    // mu0 mu1 ... mun 0 0
	// a0 a1 ... an mu stdev
	PcaLshHelper() {
		try {
			initProjVectors(Constants.getPcaHashVectorsFile(),
                            Constants.getPatchSize() * Constants.getPatchSize() * 3 + 2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

    public int computeHash(int hashId, double[] imgVec) {
        double[] hashVec = projVectors[hashId + 1];
        double mean_dot = hashVec[hashVec.length - 2];
        double stdev = hashVec[hashVec.length - 1];

        double[] muVec = projVectors[0];
		double dot = 0;
		for (int i = 0; i < imgVec.length; ++i) {
			dot += hashVec[i] * (imgVec[i] - muVec[i]);
		}

        int bin = -1;
        if (dot <= mean_dot - 2 * stdev) {
            bin = 0;
        } else if (dot <= mean_dot - stdev) {
            bin = 1;
        } else if (dot <= mean_dot - 0.5 * stdev) {
            bin = 2;
        } else if (dot <= mean_dot - 0.16 * stdev) {
            bin = 3;
        } else if (dot <= mean_dot + 0.16 * stdev) {
            bin = 4;
        } else if (dot <= mean_dot + 0.5 * stdev) {
            bin = 5;
        } else if (dot <= mean_dot + stdev) {
            bin = 6;
        } else if (dot <= mean_dot + 2 * stdev) {
            bin = 7;
        } else {
            bin = 8;
        }

        return bin;
	}
}
