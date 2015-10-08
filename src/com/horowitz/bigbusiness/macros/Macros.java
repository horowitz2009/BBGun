package com.horowitz.bigbusiness.macros;

import java.io.Serializable;

public class Macros implements Serializable {

  private static final long serialVersionUID = -4336238263778301896L;
  
  private String _name;
  
  public void doTheJob() {
    //dyra byra
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }
}
