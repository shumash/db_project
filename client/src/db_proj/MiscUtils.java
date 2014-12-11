package db_proj;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.Vector;

public class MiscUtils {
	static public void listFilesForFolder(final File folder, ArrayList<String> files) {
        listFilesForFolder(folder, files, "");
	}

	public static double[] mean(Vector<int[]> intensities){
		double[] mean = new double[3];

		for (int[] intensity : intensities){
			mean[0] += intensity[0];
			mean[1] += intensity[1];
			mean[2] += intensity[2];
		}

		mean[0] /= intensities.size();
		mean[1] /= intensities.size();
		mean[2] /= intensities.size();

		return mean;
	}

	public static double[] stdDev(Vector<int[]> intensities){
		double[] mu = MiscUtils.mean(intensities);

		double[] sigma = new double[3];
		for (int[] intensity : intensities){
			sigma[0] += (intensity[0] - mu[0])* (intensity[0] - mu[0]);
			sigma[1] += (intensity[1] - mu[1])* (intensity[1] - mu[1]);
			sigma[2] += (intensity[2] - mu[2])* (intensity[2] - mu[2]);
		}

		sigma[0] /= intensities.size();
		sigma[1] /= intensities.size();
		sigma[2] /= intensities.size();

		sigma[0] = Math.sqrt(sigma[0]);
		sigma[1] = Math.sqrt(sigma[1]);
		sigma[2] = Math.sqrt(sigma[2]);
		return sigma;
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

    public static String formatDouble(double d) {
    	DecimalFormat formatter = new java.text.DecimalFormat("00.##");
    	return formatter.format(d);
        //if(d == (long) d)
          //  return String.format("%d",(long)d);
        //else
          //  return String.format("%2.0f", d);
    }
}
