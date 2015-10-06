package com.horowitz.bigbusiness.model;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.horowitz.commons.ImageData;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.SimilarityImageComparator;

public class BuildingTest {

	@Test
	public void testClone1() {
		Building building1 = new Building("Building1");

		try {
			Building building2 = (Building) building1.clone();
			assertTrue(building1 != building2);
			assertTrue(building1.getName().equals(building2.getName()));
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testClone2() {
		try {
			Building building1 = new Building("Building1");
			ImageData image;
			image = new ImageData("buildings/warehouse.bmp", null,
			    new SimilarityImageComparator(4000, 4000), 0, 0);

			building1.setLabelImage(image);
			building1.setPosition(new Pixel(1, 1));

			Building building2 = (Building) building1.clone();
			assertTrue(building1 != building2);
			assertTrue(building1.getLabelImage() == building2.getLabelImage());
			assertTrue(building1.getName().equals(building2.getName()));
			assertTrue(building1.getPosition().equals(building2.getPosition()));
			
			building1.setPosition(new Pixel(1,1));
			assertTrue(building1.getPosition().equals(building2.getPosition()));
			assertTrue(building1.getPosition() != building2.getPosition());
			
			building1.setPosition(new Pixel(1,2));
			assertTrue(!building1.getPosition().equals(building2.getPosition()));
			
			building2.setPosition(null);
			assertTrue(!building1.getPosition().equals(building2.getPosition()));

		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testBuilding() {
	}

	@Test
	public void testCopy() {
	}

	@Test
	public void testGetName() {
	}

	@Test
	public void testGetLabelImage() {
	}

}
