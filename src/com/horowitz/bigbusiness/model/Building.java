package com.horowitz.bigbusiness.model;

import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;

import com.horowitz.mickey.Pixel;

public class Building extends BasicElement implements Cloneable, Serializable {

	private static final long serialVersionUID = -2556252202052545991L;
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		
		Building clone = (Building) super.clone();
		clone._position = (Pixel) SerializationUtils.clone(clone._position);
		return clone;
		// return SerializationUtils.clone(this);
	}

	public Building copy() throws CloneNotSupportedException {
		return (Building) clone();
	}
}
