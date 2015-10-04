package com.horowitz.mickey.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Need {

  private Objective _objective;
  private String    _contractorName;

  public Need() {

  }

  public Need(Objective objective, String contractorName) {
    super();
    _objective = objective;
    _contractorName = contractorName;
  }

  public Objective getObjective() {
    return _objective;
  }

  public void setObjective(Objective objective) {
    _objective = objective;
  }

  public String getContractorName() {
    return _contractorName;
  }

  public void setContractorName(String contractorName) {
    _contractorName = contractorName;
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

}
