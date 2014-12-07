package db_proj;

import java.io.File;
import java.util.ArrayList;
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
}
