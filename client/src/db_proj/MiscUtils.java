package db_proj;

import java.io.File;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

public class MiscUtils {
	static public void listFilesForFolder(final File folder, ArrayList<String> files) {
        listFilesForFolder(folder, files, "");
	}

    static private void listFilesForFolder(final File folder, ArrayList<String> files, String prefix) {
	    for (final File fileEntry : folder.listFiles()) {
	    	if (fileEntry == null) {
	    		continue;
	    	}
            String path = (prefix.length() >= 1 ? (prefix + "/") : "") + fileEntry.getName();
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, files, path);
	        } else {
	        	files.add(path);
	        }
	    }
	}

    static public boolean lessThan(Vector<Double> v1, Vector<Double> v2) {
		boolean retVal = true;
		for (int i = 0; i < v1.size(); i++){
			retVal = retVal && v1.get(i) <= v2.get(i);
		}
		return retVal;
	}

    static public void dumpToFile(double[] v, PrintStream output) {
        for (int i = 0; i < v.length; ++i) {
            output.print(v[i] + " ");
        }
        output.print("\n");
    }

    static public void writeImageIdsToFile(String fileName, int[] imgIds){
        PrintWriter fw= null;
        try {
            fw = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int id : imgIds) {
            fw.println("reconstruct "+ id );
            fw.println("img-show");
        }
        fw.flush();
        fw.close();
    }

    double[] readImgVectorFromFile(String inputFile) throws FileNotFoundException {
    	Scanner input = new Scanner(new File(inputFile));

    	String line = null;
    	while (input.hasNextLine()) {
    		line = input.nextLine();
    		line.trim();
    		if (line.length() > 0) {
    			break;
    		}
    	}
    	input.close();

    	double[] res = new double[Constants.getPatchSize() * Constants.getPatchSize() * 3];
		int col_num = 0;
	    Scanner colReader = new Scanner(line);
	    while (colReader.hasNextDouble()) {
	        res[col_num] = colReader.nextDouble();
			++col_num;
	    }
	    colReader.close();
	    assert col_num == Constants.getPatchSize() * Constants.getPatchSize() * 3;
	    return res;
	}
}
