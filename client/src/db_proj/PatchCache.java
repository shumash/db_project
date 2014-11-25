package db_proj;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Cache of patches. Currently follows LRU policy.
 * 
 * @author shumash
 *
 */
public class PatchCache {
	private HashMap<Integer, BufferedImage> cache = new HashMap<Integer, BufferedImage>();
	private LinkedList<Integer> queue = new LinkedList<Integer>();
	private int capacity = 0;
	
	public PatchCache(int capacity) {
		this.capacity = capacity;
	}
	
	public BufferedImage getPatch(int id) {
		BufferedImage res = cache.get(id);
		if (res != null) {
			markAsUsed(id);  // record that was used
		}
		return res;
	}
	
	public void addPatch(int id, BufferedImage img) {
		cache.put(id, img);
		markAsUsed(id);
		if (cache.size() > capacity) {
			Integer toRemove = queue.removeFirst();
			cache.remove(toRemove);
		}
	}
	
	public void markAsUsed(int id) {
		Iterator<Integer> it = queue.iterator();
		  
		while (it.hasNext()) {
			Integer item = it.next();
			if (item.equals(id)) {
				it.remove();
				break;  // assume just one
			}
		}
		queue.add(id);
	}
	
	public int size() {
		return cache.size();
	}

}
