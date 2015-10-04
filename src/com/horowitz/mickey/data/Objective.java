package com.horowitz.mickey.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Objective {
  public static enum Type {
    DELIVER, BUILD, SEND, SELL
  }

  private String _type;
  /**
   * if type is deliver or build, the material is one of the 17 materials, including gold. Else it could be train type, specific locomotive, wagon or
   * train set.
   */
  private String _material;
  private long   _neededAmount;
  private long   _currentAmount;
  private long   _initialAmount;

  public Objective(String type, String material) {
    this(type, material, 0, 0, 0);
  }

  public Objective(String type, String material, long neededAmount) {
    this(type, material, 0l, neededAmount, 0l);
  }

  public Objective(String type, String material, long initialAmount, long neededAmount, long currentAmount) {
    super();
    _type = type;
    _material = material;
    _initialAmount = initialAmount;
    _neededAmount = neededAmount;
    _currentAmount = currentAmount;
  }

  public String getType() {
    return _type;
  }

  public void setType(String type) {
    _type = type;
  }

  public String getMaterial() {
    return _material;
  }

  public void setMaterial(String material) {
    _material = material;
  }

  public long getNeededAmount() {
    return _neededAmount;
  }

  public void setNeededAmount(long neededAmount) {
    _neededAmount = neededAmount;
  }

  public long getCurrentAmount() {
    return _currentAmount;
  }

  public void setCurrentAmount(long currentAmount) {
    _currentAmount = currentAmount;
  }

  public long getInitialAmount() {
    return _initialAmount;
  }

  public void setInitialAmount(long initialAmount) {
    _initialAmount = initialAmount;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
  }

  public Objective copy() {
    Objective copy = new Objective(_type, _material, _initialAmount, _neededAmount, _currentAmount);
    return copy;
  }

  public Objective copyNeeded() {
    Objective copy = new Objective(_type, _material, 0, _neededAmount, 0);
    return copy;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

}
