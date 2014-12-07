package db_proj;

import java.util.*;

class PatchClustering {
    // Needed b/c not all patches are processed here
    // (i.e. if we add uniform patch lookup)
    int [][] neighbors = new int[Constants.getPatchesPerSide()][Constants.getPatchesPerSide()];

    List<PatchDedup.ClusterInfo> clusters = new ArrayList<PatchDedup.ClusterInfo>();
    Map<Integer, Integer> patch_to_custer = new HashMap<Integer, Integer>();
    List<PatchDedup.PatchInfo> patches = null;
    Set<Integer> unclustered = new HashSet<Integer>();
    Random rand = new Random();

    Vector<Double> maxDistFrac = null;

    PatchClustering(List<PatchDedup.PatchInfo> in_patches) {
        maxDistFrac = new Vector<Double>();
        for (Double v : Constants.getMaxDistance()) {
            maxDistFrac.add((double) v / 4.0);
        }

        for (int row = 0; row < Constants.getPatchesPerSide(); ++row) {
            for (int col = 0; col < Constants.getPatchesPerSide(); ++col) {
                neighbors[row][col] = -1;
            }
        }

        patches = in_patches;
        for (int i = 0; i < patches.size(); ++i) {
            unclustered.add(i);

            PatchDedup.PatchInfo pinfo = patches.get(i);
            neighbors[pinfo.getPatchIdX()][pinfo.getPatchIdY()] = i;
        }
    }

    public void cluster() {
        Integer seed_id = popUnclusteredPatchId();
        while (seed_id != null) {
            PatchDedup.ClusterInfo cluster = new PatchDedup.ClusterInfo(seed_id);
            clusters.add(cluster);
            growCluster(cluster, seed_id);
            seed_id = popUnclusteredPatchId();
        }
    }

    private void growCluster(PatchDedup.ClusterInfo cluster, int growth_seed) {
        Set<Integer> neighbors = getUnclusteredNeighbors(growth_seed);
        if (neighbors.isEmpty()) {
            return;
        }

        for (Integer pi : neighbors) {
            if (maybeAddToCluster(cluster, pi)) {
                growCluster(cluster, pi);
            }
        }
    }

    private boolean maybeAddToCluster(PatchDedup.ClusterInfo cluster, int new_member) {
        PatchDedup.PatchInfo seedInfo = patches.get(cluster.seed_id);
        PatchDedup.PatchInfo newInfo = patches.get(new_member);

        Vector<Double> dist = ImageUtils.computeNormChannelDistanceSquared(
            seedInfo.pwrapper, newInfo.pwrapper);
        if (MiscUtils.lessThan(dist, maxDistFrac)) {
            addToCluster(cluster, new_member);
            return true;
        }
        return false;
    }

    private void addToCluster(PatchDedup.ClusterInfo cluster, int new_member) {
        unclustered.remove(new_member);
        cluster.member_ids.add(new_member);
    }

    private Set<Integer> getUnclusteredNeighbors(Integer localId) {
        Set<Integer> res = new HashSet<Integer>();
        PatchDedup.PatchInfo pinfo = patches.get(localId);

        // Left
        Integer neigh = getUnclusteredPatchIdAt(pinfo.getPatchIdX() - 1, pinfo.getPatchIdY());
        if (neigh != null) {
            res.add(neigh);
        }
        // Right
        neigh = getUnclusteredPatchIdAt(pinfo.getPatchIdX() + 1, pinfo.getPatchIdY());
        if (neigh != null) {
            res.add(neigh);
        }
        // Top
        neigh = getUnclusteredPatchIdAt(pinfo.getPatchIdX(), pinfo.getPatchIdY() - 1);
        if (neigh != null) {
            res.add(neigh);
        }
        // Bottom
        neigh = getUnclusteredPatchIdAt(pinfo.getPatchIdX(), pinfo.getPatchIdY() + 1);
        if (neigh != null) {
            res.add(neigh);
        }

        return res;
    }

    private Integer getUnclusteredPatchIdAt(int x, int y) {
        if (x < 0 || y < 0) {
            return null;
        }
        if (x >= Constants.getPatchesPerSide() ||
            y >= Constants.getPatchesPerSide()) {
            return null;
        }
        int localId = neighbors[x][y];
        if (localId < 0) {
            return null;
        }
        if (!unclustered.contains(localId)) {
            return null;
        }
        return localId;
    }

    private Integer popUnclusteredPatchId() {
        if (unclustered.size() == 0) {
            return null;
        }

        int elem = rand.nextInt(unclustered.size());
        int i = 0;
        for (Integer pId : unclustered) {
            if (i == elem) {
                unclustered.remove(pId);
                return pId;
            }
            ++i;
        }
        throw new RuntimeException("Should never happen");
    }

	public int num_clusters() {
		return clusters.size();
	}

	public List<PatchDedup.ClusterInfo> getClusters() {
		return clusters;
	}

}
