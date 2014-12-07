package db_proj;

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
    // TODO: add cache of hash : image_id map
    private HashMap<Integer, Integer> idToHash = new HashMap<Integer, Integer>();
	private HashMap<Integer, PatchWrapper> cache = new HashMap<Integer, PatchWrapper>();
	private LinkedList<Integer> queue = new LinkedList<Integer>();
	private int capacity = 0;

	public PatchCache(int capacity) {
		this.capacity = capacity;
	}

	public PatchWrapper getPatch(int id) {
		PatchWrapper res = cache.get(id);
		if (res != null) {
			markAsUsed(id);  // record that was used
		}
		return res;
	}

    public Integer getHashById(int id) {
        return idToHash.get(id);
    }

    public void addIdHash(int id, int hash) {
        idToHash.put(id, hash);
    }

	public void addPatch(PatchWrapper wrapper) {
        if (wrapper.getId() == null) {
            throw new IllegalArgumentException("Cannot add patch without ID!");
        }
        if (wrapper.hasComputedHash()) {
            idToHash.put(wrapper.getId(), wrapper.getSingleHash());
        }
		cache.put(wrapper.getId(), wrapper);
		markAsUsed(wrapper.getId());
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
