package com.horowitz.bigbusiness.model;

import com.horowitz.mickey.Pixel;

public class Building extends BasicElement {

	private Pixel _position;

	public Building(String name) {
		super(name);

	}

	public Pixel getPosition() {
		return _position;
	}

	public void setPosition(Pixel position) {
		_position = position;
	}
	
	

}
