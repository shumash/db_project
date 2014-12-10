package db_proj;

import java.awt.image.BufferedImage;

public class PatchWrapper {
	private BufferedImage img = null;
	private double[] imgVector = null;  // LUV
	private Integer id = null;
	private int[] hashes = null;
	private Integer singleHash = null;
    private Boolean isUni = null;
    private ImageColorStats imgStats = null;

	PatchWrapper(BufferedImage inImg) {
		img = inImg;
	}
	PatchWrapper(BufferedImage inImg, int inId) {
		img = inImg;
		id = new Integer(inId);
	}

	public BufferedImage getImg() {
		return img;
	}

    public ImageColorStats getStats() {
        if (imgStats == null) {
            imgStats = new ImageColorStats(getImgVector());
        }
        return imgStats;
    }

    public Boolean isUniform() {
        if (isUni == null) {
            isUni = getStats().isUniform();
        }
        return isUni;
    }

	public double[] getImgVector() {
		if (imgVector == null) {
			imgVector = ImageUtils.toLuv(ImageUtils.toRgbVector(img));
		}
		return imgVector;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int[] getHashes() {
		if (hashes == null) {
			 hashes = Constants.lshHelper().getHashes(getImgVector(), 10);
		}
		return hashes;
	}

	public Integer getSingleHash() {
		if (singleHash == null) {
            if (Constants.ENABLE_UNIFORM_PATCH_HASHING &&
                getStats().isUniform()) {
                singleHash = getStats().getMean().qunatize();
            } else {
                singleHash = Constants.lshHelper().computeSingleIntegerHash(getHashes());
            }
		}
		return singleHash;
	}

    public boolean hasComputedHash() {
        return hashes != null;
    }

}
