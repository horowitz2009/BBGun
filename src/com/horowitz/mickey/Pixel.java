/*
 * $Id$
 *
 * Copyright (c) 2006
 */
package com.horowitz.mickey;

import java.awt.Point;
import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * 
 * @author zhristov
 */
public class Pixel implements Comparable<Pixel>, Cloneable, Serializable {

  private static final long serialVersionUID = -5887402026506046524L;

	public int weight = 0;

	public int x;

	public int y;

	public Pixel(Point p) {
		this(p.x, p.y);
	}

	public Pixel(int x, int y) {
		super();
		this.x = x;
		this.y = y;
		this.weight = 0;
	}

	public Pixel(int x, int y, int weight) {
		super();
		this.x = x;
		this.y = y;
		this.weight = weight;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	@Override
	public int compareTo(Pixel o) {
		return new CompareToBuilder().append(weight, o.weight).append(y, o.y)
		    .append(x, o.x).toComparison();
	}

	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Pixel)) {
			return false;
		}
		Pixel castOther = (Pixel) other;
		return new EqualsBuilder().append(weight, castOther.weight)
		    .append(x, castOther.x).append(y, castOther.y).isEquals();
	}

	public String toString() {
		return "[" + x + "," + y + ":" + weight + "]";
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(weight).append(x).append(y)
		    .toHashCode();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
