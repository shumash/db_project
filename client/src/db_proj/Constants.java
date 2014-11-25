package db_proj;

import java.util.Vector;

public class Constants {
	
	public static int PATCH_CACHE_SIZE = 1000;  // patches
	public static int HASH_BIN_SIZE = 30;
	public static boolean ENABLE_BRUTE_NEAREST_NEIGHBOR = false;
	
	private Vector<Double> maxDistance;
	private int patchSize = 10;
	private int smallSize = 500;
	private String hashVectorsFile = new String("../data/hash_vectors_rand10.txt");
	
	private static Constants singleton = null;
	
	public static Vector<Double> getMaxDistance(){
		return getSingleton().maxDistance;
	}
	
	public static int getSmallSize(){
		return getSingleton().smallSize;
	}
	
	public static int getPatchSize(){
		return getSingleton().patchSize;
	}
	
	public static String getHashVectorsFile() {
		return getSingleton().hashVectorsFile;
	}
	
	private Constants(){
		this.maxDistance = new Vector<Double>();
		this.maxDistance.add((double) 200.0); //TODO: dummy for now, so that no new patches are added
		this.maxDistance.add((double) 200.0);
		this.maxDistance.add((double) 200.0);
	}

	public static Constants getSingleton() {
		if (singleton == null){
			singleton = new Constants();
		}
		return singleton;
		
	}
}
