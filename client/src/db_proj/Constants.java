package db_proj;

import java.util.Vector;

public class Constants {

	public static int PATCH_CACHE_SIZE = 10000;  // patches
	public static int HASH_BIN_SIZE = 10;
	public static boolean ENABLE_BRUTE_NEAREST_NEIGHBOR = false;
    public static boolean BATCH_INSERT = false;
    public static boolean BATCH_RECONSTRUCT = false;

	private Vector<Double> maxDistance;
	private int patchSize = 10;
	private int smallSize = 500;
	private String hashVectorsFile = new String("../data/hash_vectors_rand10.txt");

	private LshHelper lshHelper_ = null;

	private static Constants singleton = null;

    public static int getPatchesPerImage() {
        return (int) Math.pow(getSmallSize() / getPatchSize(), 2);
    }

	public static LshHelper lshHelper() {
		return getSingleton().getInitLshHelper();
	}

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
		this.maxDistance.add((double) 200.0);
		this.maxDistance.add((double) 200.0);
		this.maxDistance.add((double) 200.0);
	}

	private static Constants getSingleton() {
		if (singleton == null){
			singleton = new Constants();
		}
		return singleton;
	}

	private LshHelper getInitLshHelper() {
		if (lshHelper_ == null) {
			lshHelper_ = new LshHelper();
		}
		return lshHelper_;
	}
}
