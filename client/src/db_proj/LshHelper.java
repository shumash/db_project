package db_proj;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

abstract public class LshHelper {
	protected double[][] projVectors = null;

	// File of format
	// a0 a1 ... an (opt: mu stdev)
	LshHelper() {}

	protected void initProjVectors(String projVectorsFile, int expectedSize) throws FileNotFoundException {
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
		input.close();

		projVectors = new double[lines.size()][expectedSize];
		for (int i = 0; i < lines.size(); ++i) {
			int col_num = 0;
		    Scanner colReader = new Scanner(lines.get(i));
		    while (colReader.hasNextDouble()) {
		        projVectors[i][col_num] = colReader.nextDouble();
				++col_num;
		    }
		    colReader.close();
		    assert col_num == expectedSize;
		}
	}

    public void printInfo() {
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
		if (numHashes > projVectors.length) {
			throw new IllegalArgumentException("More hashes requested than projection vectors");
		}
		int [] res = new int[numHashes];
		for (int i = 0; i < numHashes; ++i) {
			res[i] = computeHash(i, imgVector);
		}
		return res;
	}

    abstract public int computeHash(int hashId, double[] imgVec);
}
