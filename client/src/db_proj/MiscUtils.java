package db_proj;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

    static public void writeImageIdsToFile(String fileName, List<Integer> imgIds){
        PrintWriter fw= null;
        try {
            fw = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int id : imgIds) {
            fw.println("img-show");
            fw.println("reconstruct "+ id );
            fw.println("img-show");
        }
        fw.flush();
        fw.close();
    }

    static public void writeQualityMetric(String fileName, double[] points){
        PrintWriter fw= null;
        try {
            fw = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int size = points.length;
        for (int i = 0; i < size/2; i++) {
            fw.println(points[2*i] + "," + points[2*i+1]);

        }
        fw.flush();
        fw.close();
    }

    static public double getStd(List<Double> p){
        double res = 0;
        double mean = getMean(p);
        for(double d: p){
            res += Math.pow((d-mean),2);
        }
        return Math.sqrt(res/p.size());
    }

    static public double getMean(List<Double> p){
        double sum = 0;
        for(double d: p){
            sum += d;
        }
        return sum/p.size();
    }

}
