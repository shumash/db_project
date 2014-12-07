package db_proj;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

public class UnitTests {

	@Test
	public void testColor() {
		LuvColor l1 = new LuvColor(new Color(255, 0, 0));
		System.out.print("Color " + l1.toString());
	}
	
	@Test
	public void testPatchCache() {
		PatchCache cache = new PatchCache(3);
		cache.addPatch(new PatchWrapper(new BufferedImage(1, 1, 1), 1));
		assertEquals(1, cache.size());
		cache.addPatch(new PatchWrapper(new BufferedImage(1, 1, 1), 2));
		assertEquals(2, cache.size());
		cache.addPatch(new PatchWrapper(new BufferedImage(1, 1, 1), 3));
		assertEquals(3, cache.size());
		cache.addPatch(new PatchWrapper(new BufferedImage(1, 1, 1), 4));
		assertEquals(3, cache.size());
		assertTrue(cache.getPatch(1) == null);
		assertTrue(cache.getPatch(2) != null);
		assertTrue(cache.getPatch(3) != null);
		assertTrue(cache.getPatch(4) != null);
	}
	
	@Test
	public void testFilePaths() {
		ArrayList<String> paths = new ArrayList<String>();
		MiscUtils.listFilesForFolder(new File("../tiny_data"), paths);
		System.out.println("Num files: " + paths.size());
	}

}
