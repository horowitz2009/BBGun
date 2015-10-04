package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class MouseRobot {
  private int _clickDelay;
  private int _doubleClickDelay;
  private int _delayBetweenActions;
  private Point _position;
  private Point _lastMousePos;

  private Robot getInstance() {
    try {
      Robot robot = new Robot();
      robot.setAutoWaitForIdle(true);
      robot.setAutoDelay(30);
      return robot;
    } catch (AWTException e) {
      e.printStackTrace();
      return null;
    }
  }

  public MouseRobot(int clickDelay, int doubleClickDelay, int delayBetweenActions) throws AWTException {
    _clickDelay = clickDelay;
    _doubleClickDelay = doubleClickDelay;
    _delayBetweenActions = delayBetweenActions;
  }

  public MouseRobot() throws AWTException {
    this(0, 0, 0);// 40 30 200
  }

  public void mouseMove(Pixel p) {
    mouseMove(p.x, p.y);
  }

  public void mouseMove(int x, int y) {
    getInstance().mouseMove(x, y);
    saveCurrentPosition();
  }

  public void click() {
    Robot robot = getInstance();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_delayBetweenActions);
  }

  public void click(Pixel p) {
    click(p.x, p.y);  
  }
  
  public void click(int x, int y) {
    mouseMove(x, y);
    click();
  }

  public void drag(int x1, int y1, int x2, int y2) throws RobotInterruptedException {
    Robot robot = getInstance();
    mouseMove(x1, y1);
    saveCurrentPosition();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    delay(400);
    // checkUserMovement();
    int x = x1;
    int y = y1;
    int maxStep = 20;
    int step = maxStep;

    if (x1 != x2) {// move horizontally with high precision
      int a = x2 - x1;
      int d = Math.abs(a);
      step = d <= maxStep ? d : maxStep;
      double turns = d / step;
      // case 1 - a > 0 => moving east

      x = x + (a > 0 ? 7 : -7);
      mouseMove(x, y);
      delay(200);

      for (int i = 0; i < turns; i++) {
        x = x + (a > 0 ? step : -step);
        mouseMove(x, y);
        delay(100);
      }
      int rest = d % step;
      x = x + (a > 0 ? rest : -rest);
      mouseMove(x, y);
      delay(100);
    }

    if (y1 != y2) {// move vertically
      int b = y2 - y1;
      step = Math.abs(b) <= maxStep ? Math.abs(b) : maxStep;
      b = b / step;
      for (int i = 0; i < Math.abs(b); i++) {
        y = y + (b > 0 ? step : b == 0 ? 0 : -step);
        mouseMove(x, y);
        delay(100);
        // checkUserMovement();
      }
    }
    delay(400);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    saveCurrentPosition();
  }

  public void doubleClick(int x, int y) {
    mouseMove(x, y);
    doubleClick();
  }

  public void doubleClick() {
    Robot robot = getInstance();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_doubleClickDelay);
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_delayBetweenActions);
  }

  public void rightClick() {
    Robot robot = getInstance();
    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    robot.delay(_delayBetweenActions);
  }

  public void rightClick(int x, int y) {
    mouseMove(x, y);
    click();

  }

  public void delay(int ms) throws RobotInterruptedException {
    delay(ms, true);
  }

  public void delay(int ms, boolean checkUserMovement) throws RobotInterruptedException {
    if (checkUserMovement)
      saveCurrentPosition();
    getInstance().delay(ms);
    if (checkUserMovement)
      checkUserMovement();
  }

  public void savePosition() {
    _position = MouseInfo.getPointerInfo().getLocation();
  }

  public void restorePosition() {
    mouseMove(_position.x, _position.y);
  }

  public void checkUserMovement() throws RobotInterruptedException {
    Point currentPos = MouseInfo.getPointerInfo().getLocation();
    if (Math.abs(_lastMousePos.x - currentPos.x) > 4 || Math.abs(_lastMousePos.y - currentPos.y) > 4) {

      getInstance().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
      // mouseMove(_lastMousePos.x, _lastMousePos.y);
      throw new RobotInterruptedException();
    }
    saveCurrentPosition();
  }

  public void saveCurrentPosition() {
    _lastMousePos = MouseInfo.getPointerInfo().getLocation();
  }

  public Point getCurrentPosition() {
    return MouseInfo.getPointerInfo().getLocation();
  }
  
  public void wheelDown(int notches) {
    try {
      new Robot().mouseWheel(notches);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }
}
