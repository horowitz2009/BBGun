package com.horowitz.bigbusiness.model;

import com.horowitz.mickey.Pixel;

public class Contract {
	private Product _product;

	private long _start;

	private Building _building;

	private Pixel _coordinates;

	public long getStart() {
		return _start;
	}

	public void setStart(long start) {
		_start = start;
	}

	public Building getBuilding() {
		return _building;
	}

	public void setBuilding(Building building) {
		_building = building;
	}

	public Pixel getCoordinates() {
		return _coordinates;
	}

	public void setCoordinates(Pixel coordinates) {
		_coordinates = coordinates;
	}

	public Product getProduct() {
		return _product;
	}

	public void setProduct(Product product) {
		_product = product;
	}

}
