package com.horowitz.mickey.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Mission implements Comparable<Mission> {
  private List<Objective> _objectives;

  private int             _number;
  private String          _contractor;
  private String          _description;
  private boolean         _selected;
  private boolean         _done;
  private int             _level;

  /**
   * if true means any of the objective if being done, then mission is considered accomplished. If false, then all objectives must be done.
   */
  private boolean         _any;

  public Mission(String contractor, String description, int number) {
    super();
    _contractor = contractor;
    _description = description;
    _number = number;
    _selected = false;
    _done = false;
    _level = 0;
  }

  public Mission() {
    super();
    _objectives = new ArrayList<Objective>();
  }

  public List<Objective> getObjectives() {
    return _objectives;
  }

  public void setObjectives(List<Objective> objectives) {
    _objectives = objectives;
  }

  public int getNumber() {
    return _number;
  }

  public void setNumber(int number) {
    _number = number;
  }

  public String getContractor() {
    return _contractor;
  }

  public void setContractor(String contractor) {
    _contractor = contractor;
  }

  public boolean isAny() {
    return _any;
  }

  public void setAny(boolean any) {
    _any = any;
  }

  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public boolean isSelected() {
    return _selected;
  }

  public void setSelected(boolean selected) {
    _selected = selected;
  }

  public boolean isDone() {
    return _done;
  }

  public void setDone(boolean done) {
    _done = done;
  }

  public int getLevel() {
    return _level;
  }

  public void setLevel(int level) {
    _level = level;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
  }

  public Mission copy() {
    Mission copy = new Mission();
    copy._any = _any;
    copy._contractor = _contractor;
    copy._number = _number;
    List<Objective> copyObj = new ArrayList<>();

    for (Objective obj : _objectives) {
      copyObj.add(obj.copy());
    }
    copy._objectives = copyObj;

    return copy;
  }

  public Mission copyNeeded() {
    Mission copy = new Mission();
    copy._any = _any;
    copy._contractor = _contractor;
    copy._number = _number;
    List<Objective> copyObj = new ArrayList<>();

    for (Objective obj : _objectives) {
      copyObj.add(obj.copyNeeded());
    }
    copy._objectives = copyObj;

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

  public void mergeWithDB(Mission mdb) {
    for (Objective o : _objectives) {
      for (Objective odb : mdb.getObjectives()) {
        if (o.getMaterial().equals(odb.getMaterial()))
          o.setNeededAmount(odb.getNeededAmount());
      }
    }
  }

  public void populate(Mission m) {
    m.setAny(isAny());
    m.setDescription(getDescription());
    m.setObjectives(getObjectives());
    m.setNumber(getNumber());
    m.setSelected(isSelected());
    m.setDone(isDone());
    m.setLevel(getLevel());
  }
  
  @Override
  public int compareTo(Mission o) {
    return new CompareToBuilder().append(this.getLevel(), o.getLevel()).toComparison();
    //return new CompareToBuilder().append(this.getNumber(), o.getNumber()).toComparison();
  }

}
