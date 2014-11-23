package db_proj;

import java.util.Vector;

public class Constants {
	
	private Vector<Double> minSimilarity;
	private int patchSize;
	
	private static Constants singleton = null;
	
	public Vector<Double> getMinSimilarity(){
		return singleton.minSimilarity;
	}
	
	public int getPatchSize(){
		return singleton.patchSize;
	}
	
	private Constants(){
		this.minSimilarity = new Vector<Double>();
		this.minSimilarity.add(Double.MAX_VALUE); //TODO: dummy for now, so that no new patches are added
		this.minSimilarity.add(Double.MAX_VALUE);
		this.minSimilarity.add(Double.MAX_VALUE);
		
		this.patchSize = 8; //TODO: dummy for now
	}

	public static Constants getSingleton() {
		if (singleton == null){
			singleton = new Constants();
		}
		return singleton;
		
	}
	
	public enum SimilarityType{
		EUCLIDEAN
	}
}
