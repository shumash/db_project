package db_proj;

import java.util.Vector;

public class Constants {
	public static int PATCH_CACHE_SIZE = 10000;  // patches
	public static boolean ENABLE_BRUTE_NEAREST_NEIGHBOR = false;
    public static boolean BATCH_INSERT = false;
    public static boolean BATCH_RECONSTRUCT = false;
    public static int RANDOM_SAMPLE = 10;
    public static boolean USE_PCA_HASH = false; //true;
    public static boolean EXPLOIT_SELF_SIMILARITY = false;

	private Vector<Double> maxDistance;
	private int patchSize = 25;
	private int smallSize = 500;
	private String naiveHashVectorsFile = new String("../data/hash_vectors_rand25.txt");
    private String pcaHashVectorsFile = new String("../data/hash_vectors_pca25.txt");

    private static double likelyUniformIntensityThresh = 25.0;

	private static double[] likelyUniformColorThresh = new double[]{12.0, 12.0, 12.0};

    public static double getUniformIntensityThresh(){
    	return likelyUniformIntensityThresh;
    }

    public static double[] getUniformColorThresh() {
    	return likelyUniformColorThresh ;
    }

	private LshHelper lshHelper_ = null;

	private static Constants singleton = null;

    public static int pixelsInPatch() {
        return getPatchSize() * getPatchSize();
    }

    public static int getPatchesPerImage() {
        return (int) Math.pow(getSmallSize() / getPatchSize(), 2);
    }

    public static int getPatchesPerSide() {
        return getSmallSize() / getPatchSize();
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

	public static String getNaiveHashVectorsFile() {
		return getSingleton().naiveHashVectorsFile;
	}

    public static String getPcaHashVectorsFile() {
		return getSingleton().pcaHashVectorsFile;
	}

	private Constants(){
		this.maxDistance = new Vector<Double>();

        // Note: for RGB the best threshold was 0.2:
        // "average distance squared of R" <= 0.2
        // ==>
        // "average distance of R" <= 0.447
        // where R varies from 0 to 1.0
        // but L varies 0 to 100
        // so we apply scaling by 100 ==> 44.7
        // then we square it  -- No Way!!!
        // The reconstructions already looks awful with a threshold of 200.
        //
        // ... Executive decision: applying completely arbitrary
        // and mathematically unsound threshold of 0.2 * 100
        //
        //
		this.maxDistance.add((double) 20.0);
		this.maxDistance.add((double) 40.0);
		this.maxDistance.add((double) 40.0);
	}

	private static Constants getSingleton() {
		if (singleton == null){
			singleton = new Constants();
		}
		return singleton;
	}

	private LshHelper getInitLshHelper() {
		if (lshHelper_ == null) {
            if (!USE_PCA_HASH) {
            	SimpleTimer.timedLog("Using naive Hash\n");
                lshHelper_ = new NaiveLshHelper(Constants.getNaiveHashVectorsFile());
            } else {
                lshHelper_ = new PcaLshHelper(Constants.getPcaHashVectorsFile(), true);
            }
		}
		return lshHelper_;
	}
}
