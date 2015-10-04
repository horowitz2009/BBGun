package com.horowitz.mickey;

import java.awt.Rectangle;

public class MaterialLocation {
  private String _name;
  private String _homeName;
  private int    _page;

  public int getPage() {
    return _page;
  }

  public String getName() {
    return _name;
  }

  
  public String getHomeName() {
    return _homeName;
  }

  public Rectangle getArea() {
    return _area;
  }

  private Rectangle _area;

  public MaterialLocation(String name, String homeName, Rectangle area, int page) {
    super();
    _name = name;
    _homeName = homeName;
    _area = area;
    _page = page;
  }
}
