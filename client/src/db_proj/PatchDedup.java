package db_proj;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * User: Wei
 * Date: 12/3/2014
 * Time: 11:46 PM
 */
public class PatchDedup {
    private static class PatchInfo {
        // The hash of the patch
        public int hash;

        // Whether it is duplicated with another earlier patch in this image
        public boolean duplicatedLocal;

        // If it's duplicatedLocal, the index of the earlier duplicate patch
        public int localSameAsIndex;

        // Whether it's duplicated with another existing patch in the database.
        public boolean duplicatedRemote;

        // The patch ID we'll eventually decide to put in to the database
        public int patchId;

        public BufferedImage image;

        private PatchInfo(int hash, BufferedImage image) {
            this.hash = hash;
            this.image = image;
        }
    }

    PatchInfo[] patchInfos;

    LshHelper lshHelper = new LshHelper();
    Map<Integer, List<Integer>> localHashes = new HashMap<Integer, List<Integer>>();
    Map<Integer, BufferedImage> patchesToStore = new HashMap<Integer, BufferedImage>();

    public PatchDedup(BufferedImage[] patches) {
        System.out.print("Compressing patches locally for the same image... ");
        patchInfos = new PatchInfo[patches.length];
        for(int i=0;i<patches.length;i++) {
            int hash = getHash(patches[i]);
            patchInfos[i] = new PatchInfo(hash, patches[i]);
            if (localHashes.containsKey(hash)) {
                List<BufferedImage> similarImages = new ArrayList<BufferedImage>();
                for(int j : localHashes.get(hash)) {
                    similarImages.add(patches[j]);
                }
                int mostSimilarIndex = findMostSimilarPatch(patches[i], similarImages);
                if (mostSimilarIndex != -1) {
                    patchInfos[i].duplicatedLocal = true;
                    patchInfos[i].localSameAsIndex = localHashes.get(hash).get(mostSimilarIndex);
                }
            }
            if (!patchInfos[i].duplicatedLocal) {
                if (!localHashes.containsKey(hash)) {
                    localHashes.put(hash, new ArrayList<Integer>());
                }
                localHashes.get(hash).add(i);
            }
        }
        System.out.println("done.");

    }

    public Set<Integer> getUniqueHashesForThisImage() {
        return localHashes.keySet();
    }

    /**
     * @param remoteHashes map from hash to remote patch ID
     * @param initPatchId The first ID that does not exist in the database.
     */
    public void setRemoteExistingHashes(PatchCache cache, Map<Integer, Map<Integer, BufferedImage>> remoteHashes, int initPatchId) {
        System.out.print("Compressing patches against similar patches in the database... ");
        for (PatchInfo patchInfo : patchInfos) {
            if (remoteHashes.containsKey(patchInfo.hash)) {
                List<Integer> indices = new ArrayList<Integer>();
                List<BufferedImage> similarImages = new ArrayList<BufferedImage>();
                for (Map.Entry<Integer, BufferedImage> entry : remoteHashes.get(patchInfo.hash).entrySet()) {
                    indices.add(entry.getKey());
                    similarImages.add(entry.getValue());
                }
                int mostSimilarIndex = findMostSimilarPatch(patchInfo.image, similarImages);
                if (mostSimilarIndex != -1) {
                    patchInfo.duplicatedRemote = true;
                    patchInfo.patchId = indices.get(mostSimilarIndex);
                }
            }
            if (!patchInfo.duplicatedRemote) {
                if (!patchInfo.duplicatedLocal) {
                    int patchId = initPatchId;
                    patchInfo.patchId = patchId;
                    patchesToStore.put(patchId, patchInfo.image);
                    initPatchId++;

                } else {
                    patchInfo.patchId = patchInfos[patchInfo.localSameAsIndex].patchId;
                }
            }
        }
        System.out.println("done.");
    }

    public Map<Integer, BufferedImage> getPatchesToStore() {
        return patchesToStore;
    }

    // List of patch IDs in the order of the patches
    public int[] getPointersToStore() {
        int[] result = new int[patchInfos.length];
        for (int i=0;i<patchInfos.length;i++) {
            result[i] = patchInfos[i].patchId;
        }
        return result;
    }

    private int getHash(BufferedImage patch) {
        double[] imgVector = ImageUtils.toLuv(ImageUtils.toRgbVector(patch));
        int[] hashes = lshHelper.getHashes(imgVector, 10);
        return computeSingleIntegerHash(hashes);
    }


    private int computeSingleIntegerHash(int[] hashes) {
        int result = 0;
        for(int i=0;i<hashes.length;i++) {
            result <<= 3;
            result += hashes[i];
        }
        return result;
    }

    public int findMostSimilarPatch(BufferedImage to, List<BufferedImage> images) {
        int bestImageIndex = -1;
        Vector<Double> approxBest = null;
        for (int i=0;i<images.size();i++) {
            Vector<Double> distance = ImageUtils.computeDistance(to, images.get(i));
            if (approxBest == null || distanceLessThan(distance, approxBest)) {
                approxBest = distance;
                bestImageIndex = i;
            }
        }
        if (lessThan(approxBest, Constants.getMaxDistance())) {
            return bestImageIndex;
        }
        return -1;
    }

    //assumes both vectors are of the same length
    private boolean lessThan(Vector<Double> v1, Vector<Double> v2) {
        boolean retVal = true;
        for (int i = 0; i < v1.size(); i++){
            retVal = retVal && v1.get(i) <= v2.get(i);
        }
        return retVal;
    }

    private boolean distanceLessThan(Vector<Double> v1, Vector<Double> v2) {
        return magnitude(v1) < magnitude(v2);
    }

    private double magnitude(Vector<Double> v) {
        double sq = 0;
        for (Double d : v) {
            sq+=d*d;
        }
        return Math.sqrt(sq);
    }
}
