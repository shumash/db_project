package db_proj;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class LshHelper {

	double[][] projVectors = null;
	int binSize = Constants.HASH_BIN_SIZE;


	// File of format
	// a0 a1 ... an b w
	LshHelper() {
		try {
			init(Constants.getHashVectorsFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	private void init(String projVectorsFile) throws FileNotFoundException {
		ArrayList<String> lines = new ArrayList<String>();

		Scanner input = new Scanner(new File(projVectorsFile));
		while (input.hasNextLine()) {
			String line = input.nextLine();
			line.trim();
			if (line.length() > 0) {
				lines.add(line);
			}
		}
		System.out.println("Reading " + lines.size() + " hash vectors");

		projVectors = new double[lines.size()][Constants.getPatchSize() * Constants.getPatchSize() * 3];
		for (int i = 0; i < lines.size(); ++i) {
			int col_num = 0;
		    Scanner colReader = new Scanner(lines.get(i));
		    while (colReader.hasNextDouble()) {
		        projVectors[i][col_num] = colReader.nextDouble();
				++col_num;
		    }
		    assert col_num == Constants.getPatchSize() * Constants.getPatchSize() * 3;
		}
	}

	public int computeSingleIntegerHash(int[] hashes) {
        int result = 0;
        for(int i=0;i<hashes.length;i++) {
            result <<= 3;
            result += hashes[i];
        }
        return result;
    }

	public int[] getHashes(double[] imgVector, int numHashes) {
		return getHashes(imgVector, numHashes, false);
	}

	public int[] getHashes(double[] imgVector, int numHashes, boolean nextBin) {
		if (numHashes > projVectors.length) {
			throw new IllegalArgumentException("More hashes requested than projection vectors");
		}
		int [] res = new int[numHashes];
		for (int i = 0; i < numHashes; ++i) {
			res[i] = computeHash(projVectors[i], imgVector, binSize, nextBin);
		}
		return res;
	}

	public static int computeHash(double[] hashVec, double[] imgVec, int binSize, boolean nextBin) {
		double dot = 0;
		for (int i = 0; i < imgVec.length; ++i) {
			dot += hashVec[i] * imgVec[i];
		}
		dot = Math.abs(dot);
		int bin = (int) Math.floor(dot / binSize);
		if (!nextBin) {
			return bin;
		}

		if (dot - bin * binSize > binSize / 2.0) {
			return bin + 1;
		} else {
			return bin - 1;
		}

	}
}
