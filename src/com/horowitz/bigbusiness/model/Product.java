package com.horowitz.bigbusiness.model;

public class Product extends BasicElement {
	private Building _building;
	private int _position;
	private int _time;
	private Product _resource1;
	private Product _resource2;

	private int _quantity;

	public Product(String name) {
		super(name);
	}

	public Building getBuilding() {
		return _building;
	}

	public void setBuilding(Building building) {
		_building = building;
	}

	public Product getResource1() {
		return _resource1;
	}

	public void setResource1(Product resource1) {
		_resource1 = resource1;
	}

	public Product getResource2() {
		return _resource2;
	}

	public void setResource2(Product resource2) {
		_resource2 = resource2;
	}

	public int getQuantity() {
		return _quantity;
	}

	public void setQuantity(int quantity) {
		_quantity = quantity;
	}

	public int getPosition() {
		return _position;
	}

	public void setPosition(int position) {
		_position = position;
	}

	public int getTime() {
		return _time;
	}

	public void setTime(int time) {
		_time = time;
	}

}
