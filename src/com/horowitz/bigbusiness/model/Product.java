package com.horowitz.bigbusiness.model;

public class Product extends BasicElement {
  private String _buildingName;
  private int _position;
  private int _time;
  private int _levelRequired;
  private String _resource1;
  private int _resource1Quantity;
  private String _resource2;
  private int _resource2Quantity;

  public Product(String name) {
    super(name);
    _levelRequired = 1;
  }

  public String getBuildingName() {
    return _buildingName;
  }

  public void setBuildingName(String buildingName) {
    _buildingName = buildingName;
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

  public int getLevelRequired() {
    return _levelRequired;
  }

  public void setLevelRequired(int levelRequired) {
    _levelRequired = levelRequired;
  }

  @Override
  public void postDeserialize(Object[] transientObjects) throws Exception {
    super.postDeserialize(transientObjects);
    // _building = transientObjects[]
    // TODO
  }

  public String getResource1() {
    return _resource1;
  }

  public void setResource1(String resource1) {
    _resource1 = resource1;
  }

  public int getResource1Quantity() {
    return _resource1Quantity;
  }

  public void setResource1Quantity(int resource1Quantity) {
    _resource1Quantity = resource1Quantity;
  }

  public String getResource2() {
    return _resource2;
  }

  public void setResource2(String resource2) {
    _resource2 = resource2;
  }

  public int getResource2Quantity() {
    return _resource2Quantity;
  }

  public void setResource2Quantity(int resource2Quantity) {
    _resource2Quantity = resource2Quantity;
  }

}
