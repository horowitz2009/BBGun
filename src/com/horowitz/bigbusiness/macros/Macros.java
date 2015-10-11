package com.horowitz.bigbusiness.macros;

import java.awt.AWTException;
import java.io.IOException;
import java.io.Serializable;

import com.horowitz.bigbusiness.ScreenScanner;
import com.horowitz.bigbusiness.model.Deserializable;
import com.horowitz.bigbusiness.model.Product;
import com.horowitz.mickey.MouseRobot;
import com.horowitz.mickey.RobotInterruptedException;

public abstract class Macros implements Serializable, Deserializable {

  private static final long serialVersionUID = -4336238263778301896L;

  private String _name;
  protected transient ScreenScanner _scanner;
  public ScreenScanner getScanner() {
    return _scanner;
  }

  public void setScanner(ScreenScanner scanner) {
    _scanner = scanner;
  }

  protected transient MouseRobot _mouse;

  
  public Macros() {
    super();
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public abstract boolean doTheJob(Product pr) throws AWTException, IOException, RobotInterruptedException;

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;

  }

  @Override
  public void postDeserialize(Object[] transientObjects) throws Exception {
    _scanner = (ScreenScanner) transientObjects[1];

  }
}
