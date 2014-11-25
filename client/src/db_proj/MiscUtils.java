package db_proj;

import java.io.File;
import java.util.ArrayList;

public class MiscUtils {
	static public void listFilesForFolder(final File folder, ArrayList<String> files) {
	    for (final File fileEntry : folder.listFiles()) {
	    	if (fileEntry == null) {
	    		continue;
	    	}
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, files);
	        } else {
	        	files.add(folder.getName() + "/" + fileEntry.getName());
	            System.out.println(folder.getName() + "/" + fileEntry.getName());
	        }
	    }
	}
}
