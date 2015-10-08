package com.horowitz.bigbusiness.model;

import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;

import com.horowitz.bigbusiness.macros.Macros;
import com.horowitz.mickey.Pixel;

public class Building extends BasicElement implements Cloneable, Serializable {

  private static final long serialVersionUID = -2556252202052545991L;
  private Pixel _position;
  private Pixel _relativePosition;
  private transient Macros _macros;
  private String _macrosClass;

  public Building(String name) {
    super(name);
    _position = null;
    _relativePosition = null;
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
    clone._relativePosition = (Pixel) SerializationUtils.clone(clone._relativePosition);
    return clone;
    // return SerializationUtils.clone(this);
  }

  public Building copy() throws CloneNotSupportedException {
    return (Building) clone();
  }

  public void setMacros(Macros macros) {
    _macrosClass = macros.getClass().getName();
    _macros = macros;
  }

  public boolean isIdle() {
    // TODO check with zzz.bmp
    return true;
  }

  public Pixel getRelativePosition() {
    return _relativePosition;
  }

  public void setRelativePosition(Pixel relativePosition) {
    _relativePosition = relativePosition;
  }

  public Macros getMacros() {
    return _macros;
  }
  
  @Override
  public void postDeserialize(Object[] transientObjects) throws Exception {
    super.postDeserialize(transientObjects);
    System.out.println("mocros class is" +_macrosClass);
  }

}
