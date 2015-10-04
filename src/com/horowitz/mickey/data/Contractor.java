package com.horowitz.mickey.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Contractor {

  private String     _name;

  private boolean    _scan;
  private boolean    _scanMaterials2;
  private boolean    _active;
  private Material[] _materials;

  private Material[] _materialsMore;

  private int        _currentMissionNumber;

  private int        _endMissionNumber;

  private String     _accepts;

  public Contractor() {
    super();
  }

  public Contractor(String name) {
    this(name, null);
  }

  public Contractor(String name, Material[] materials) {
    super();
    _name = name;
    _materials = materials;
    _scan = false;
    _scanMaterials2 = false;
    _active = false;
  }

  public Material[] getMaterials() {
    return _materials;
  }

  public void setMaterials(Material[] materials) {
    _materials = materials;
  }

  public Material[] getMaterialsMore() {
    return _materialsMore;
  }

  public void setMaterialsMore(Material[] materialsMore) {
    _materialsMore = materialsMore;
  }

  public int getCurrentMissionNumber() {
    return _currentMissionNumber;
  }

  public int getEndMissionNumber() {
    return _endMissionNumber;
  }

  public void setEndMissionNumber(int endMissionNumber) {
    _endMissionNumber = endMissionNumber;
  }

  public void setCurrentMissionNumber(int currentMissionNumber) {
    this._currentMissionNumber = currentMissionNumber;
  }

  public boolean isScan() {
    return _scan;
  }

  public void setScan(boolean scan) {
    _scan = scan;
  }

  public boolean isScanMaterials2() {
    return _scanMaterials2;
  }

  public void setScanMaterials2(boolean scanMaterials2) {
    _scanMaterials2 = scanMaterials2;
  }

  public boolean isActive() {
    return _active;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public String getAccepts() {
    return _accepts;
  }

  public void setAccepts(String accepts) {
    _accepts = accepts;
  }

  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public void extract(Contractor contractor) {
    _name = contractor._name;
    _currentMissionNumber = contractor._currentMissionNumber;
    _endMissionNumber = contractor._endMissionNumber;
    _materials = contractor._materials;
  }
}
