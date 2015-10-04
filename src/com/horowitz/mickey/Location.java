package com.horowitz.mickey;

import java.awt.Point;

public class Location {
  private String _name;
  private int   _time;
  private Point _coordinates;
  private int   _page;

  public Location(String name, int _time, Point _coordinates, int page) {
    super();
    _name = name;
    this._time = _time;
    this._coordinates = _coordinates;
    this._page = page;
  }

  public String getName() {
    return _name;
  }

  public int getTime() {
    return _time;
  }

  public int getPage() {
    return _page;
  }

  public Point getCoordinates() {
    return _coordinates;
  }

}
