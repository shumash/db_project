package db_proj;

import java.util.*;

/**
 * Author: Wei, Masha
 * Date: 12/3/2014
 * Time: 11:46 PM
 *
 */
public class PatchDedup {
    public static class PatchInfo {
        public int x = -1;
        public int y = -1;
        public PatchWrapper pwrapper = null;
        Vector<Double> distance = null;

        public PatchInfo(PatchWrapper pw) {
            pwrapper = pw;
        }
        public PatchInfo(PatchWrapper pw, int inX, int inY) {
            pwrapper = pw;
            x = inX;
            y = inY;
        }
    }

    // Hashes
    List<PatchInfo> patches = new ArrayList<PatchInfo>(Constants.getPatchesPerImage());
    List<PatchWrapper> approximatingPatches = null;

    Map<Integer, Set<Integer> > localHashes = new HashMap<Integer, Set<Integer>>();
    List<PatchWrapper> patchesToStore = new ArrayList<PatchWrapper>();

    public PatchDedup() {}

    /**
     * Adds a patch to the local patches.
     */
    public int processLocalPatch(PatchWrapper wrapper, int x, int y) {
        int localId = patches.size();
        patches.add(new PatchInfo(wrapper, x, y));

        int hash = wrapper.getSingleHash();
        if (!localHashes.containsKey(hash)) {
            localHashes.put(hash, new HashSet<Integer>());
        }
        localHashes.get(hash).add(localId);
        return localId;
    }

    public Set<Integer> getUniqueHashes() {
        return localHashes.keySet();
    }

    public List<PatchWrapper> patchesToStore() {
        return patchesToStore;
    }

    /**
     * Call after:
     * - calling processLocalPatch on all new image patches
     * - calling processPossibleDbMatches to compute patches to store
     * - storing patchesToStore in DB
     * to get information on patch pointers associated with all x,y positions
     * in the image.
     */
    public List<PointerData> getPointerData() {
        if (patches.size() != approximatingPatches.size()) {
            throw new RuntimeException("Improper usage of PatchDedup");
        }
        List<PointerData> res = new ArrayList<PointerData>();
        for (int i = 0; i < patches.size(); ++i) {
            Integer patchId = approximatingPatches.get(i).getId();
            assert patchId != null;
            PatchInfo original = patches.get(i);
            res.add(new PointerData(patchId, original.x, original.y));
        }
        return res;
    }

    /**
     * Computes pathes to store, and patch pointers.
     * @param dbPatches - for all getUniqueHashes() images in the Database;
     *    i.e. map keyed by hash to all images in that bin
     */
    void processPossibleDbMatches(Map<Integer, List<PatchWrapper> > dbPatches) {
        approximatingPatches = new ArrayList<PatchWrapper>(
            Collections.nCopies(patches.size(), (PatchWrapper) null));

        for (Map.Entry<Integer, Set<Integer> > entry : localHashes.entrySet()) {
            Integer hash = entry.getKey();
            List<PatchWrapper> existingPatches = dbPatches.get(hash);
            List<PatchWrapper> existingLocalPatches = new ArrayList<PatchWrapper>();
            for (Integer patchIndex : entry.getValue()) {
                PatchInfo newPatch = patches.get(patchIndex);
                PatchInfo closest = findMostSimilarPatch(existingLocalPatches, newPatch.pwrapper);
                if (existingPatches != null) {
                    closest = findMostSimilarPatch(existingPatches, newPatch.pwrapper, closest);
                }
                if (closest == null ||
                    !MiscUtils.lessThan(closest.distance, Constants.getMaxDistance())) {
                    // I.e. no match
                    patchesToStore.add(newPatch.pwrapper);
                    existingLocalPatches.add(newPatch.pwrapper);
                    closest = newPatch;
                }
                approximatingPatches.set(patchIndex, closest.pwrapper);
            }
        }
    }

    public PatchInfo findMostSimilarPatch(List<PatchWrapper> existingPatches, PatchWrapper patch) {
        return findMostSimilarPatch(existingPatches, patch, null);
    }

    public PatchInfo findMostSimilarPatch(List<PatchWrapper> existingPatches, PatchWrapper patch, PatchInfo closest_so_far) {
		PatchInfo approxBest = closest_so_far;
		for (PatchWrapper candidate : existingPatches) {
            PatchInfo sim = new PatchInfo(candidate);
			sim.distance = ImageUtils.computeDistance(patch, candidate);
			if (approxBest == null || MiscUtils.lessThan(sim.distance, approxBest.distance)) {
				approxBest = sim;
			}
		}
        return approxBest;
    }
}


    // /**
    //  * @param remoteHashes map from hash to remote patch ID
    //  * @param initPatchId The first ID that does not exist in the database.
    //  */
    // public void setRemoteExistingHashes(PatchCache cache, Map<Integer, Map<Integer, BufferedImage>> remoteHashes, int initPatchId) {
    //     SimpleTimer timer = new SimpleTimer();
    // 	System.out.print("Compressing patches against similar patches in the database... ");
    //     for (PatchInfo patchInfo : patchInfos) {
    //         if (remoteHashes.containsKey(patchInfo.hash)) {
    //             List<Integer> indices = new ArrayList<Integer>();
    //             List<BufferedImage> similarImages = new ArrayList<BufferedImage>();
    //             for (Map.Entry<Integer, BufferedImage> entry : remoteHashes.get(patchInfo.hash).entrySet()) {
    //                 indices.add(entry.getKey());
    //                 similarImages.add(entry.getValue());
    //             }
    //             int mostSimilarIndex = findMostSimilarPatch(patchInfo.image, similarImages);
    //             if (mostSimilarIndex != -1) {
    //                 patchInfo.duplicatedRemote = true;
    //                 patchInfo.patchId = indices.get(mostSimilarIndex);
    //             }
    //         }
    //         if (!patchInfo.duplicatedRemote) {
    //             if (!patchInfo.duplicatedLocal) {
    //                 int patchId = initPatchId;
    //                 patchInfo.patchId = patchId;
    //                 patchesToStore.put(patchId, patchInfo.image);
    //                 initPatchId++;

    //             } else {
    //                 patchInfo.patchId = patchInfos[patchInfo.localSameAsIndex].patchId;
    //             }
    //         }
    //     }
    //     System.out.println("done in " + timer.getMs() + "ms");
    // }

    // public Map<Integer, BufferedImage> getPatchesToStore() {
    //     return patchesToStore;
    // }

    // // List of patch IDs in the order of the patches
    // public int[] getPointersToStore() {
    //     int[] result = new int[patchInfos.length];
    //     for (int i=0;i<patchInfos.length;i++) {
    //         result[i] = patchInfos[i].patchId;
    //     }
    //     return result;
    // }

    // public int findMostSimilarPatch(BufferedImage to, List<BufferedImage> images) {
    //     int bestImageIndex = -1;
    //     Vector<Double> approxBest = null;
    //     for (int i=0;i<images.size();i++) {
    //         Vector<Double> distance = ImageUtils.computeDistance(to, images.get(i));
    //         if (approxBest == null || distanceLessThan(distance, approxBest)) {
    //             approxBest = distance;
    //             bestImageIndex = i;
    //         }
    //     }
    //     if (lessThan(approxBest, Constants.getMaxDistance())) {
    //         return bestImageIndex;
    //     }
    //     return -1;
    // }

    // //assumes both vectors are of the same length
    // private boolean lessThan(Vector<Double> v1, Vector<Double> v2) {
    //     boolean retVal = true;
    //     for (int i = 0; i < v1.size(); i++){
    //         retVal = retVal && v1.get(i) <= v2.get(i);
    //     }
    //     return retVal;
    // }

    // private boolean distanceLessThan(Vector<Double> v1, Vector<Double> v2) {
    //     return magnitude(v1) < magnitude(v2);
    // }

    // private double magnitude(Vector<Double> v) {
    //     double sq = 0;
    //     for (Double d : v) {
    //         sq+=d*d;
    //     }
    //     return Math.sqrt(sq);
    // }
//}
