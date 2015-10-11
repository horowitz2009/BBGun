package com.horowitz.bigbusiness;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;

import com.horowitz.bigbusiness.macros.Macros;
import com.horowitz.bigbusiness.model.Product;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;

public class TerminalMacros extends Macros {

  private static final long serialVersionUID = 1L;

  @Override
  public boolean doTheJob(Product pr) throws AWTException, IOException, RobotInterruptedException {
    // TODO Auto-generated method stub

    Pixel p = _scanner.scanOne("labels/Terminal.bmp", _scanner.getLabelArea(), false);
    System.err.println("PIXEL1= " + p);
    _scanner.writeImage(_scanner.getLabelArea(), "terminal1.png");
    if (p != null) {
      Rectangle area2 = new Rectangle(p.x - 255, p.y - 17, 586, 405);
      _scanner.writeImage(area2, "terminal4.png");
      for (int i = 0; i < 5; i++) {
        _mouse.click(area2.x + 558, area2.y + 228);
      }
      _mouse.delay(1500);

      // send trip
      Pixel pp = null;
      do {
        Rectangle area3 = new Rectangle(area2.x + 267, area2.y + 136, 88, 20);
        _scanner.writeImage(area3, "terminal_area3.png");
        pp = _scanner.scanOne("newTrip.bmp", area3, false);
        if (pp != null) {
          _mouse.click(area3.x + 42, area3.y);
          _mouse.delay(2000);

          // looking for available trips
          int xx = (_scanner.getGameWidth() - 594) / 2;
          Rectangle area4 = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getTopLeft().y + 107, 594, 570);
          _scanner.writeImage(area4, "terminal_area4.png");
          Rectangle area5 = new Rectangle(area4.x + 286, area4.y + 55, 163, area4.height - 75 - 70);
          _scanner.writeImage(area5, "terminal_area5.png");
          if (_scanner.scanOne("startTrip.bmp", area5, true) != null)
            _mouse.delay(1000);

        } else {
          // all busy
          return false;
        }
      } while (pp != null);
    }

    return false;
  }

  public boolean doTheJobOLD(Product pr) throws AWTException, IOException, RobotInterruptedException {
    // TODO Auto-generated method stub

    Pixel p = _scanner.scanOne("labels/Terminal.bmp", _scanner.getLabelArea(), false);
    _scanner.writeImage(_scanner.getLabelArea(), "terminal1.png");
    if (p != null) {

      Rectangle area = new Rectangle(p.x - 266, p.y - 15, 608, 447);
      _scanner.writeImage(area, "terminal2.png");
      Rectangle buttonArea = new Rectangle(area.x + 421, area.y + 88, 125, 139);
      _scanner.writeImage(buttonArea, "terminal3.png");
      if (_scanner.scanOne("toTripButton.bmp", buttonArea, true) != null) {
        _mouse.delay(500);
        Pixel pp = _scanner.scanOne("labels/Terminal.bmp", area, false);
        if (pp != null) {
          Rectangle area2 = new Rectangle(pp.x - 255, pp.x - 17, 586, 405);
          _scanner.writeImage(area2, "terminal4.png");
          for (int i = 0; i < 5; i++) {
            _mouse.click(area2.x + 558, area2.y + 228);
          }
          _mouse.delay(500);

          // send trip
          _mouse.click(area2.x + 294, area2.y + 210);
          _mouse.delay(1000);

          // looking for available trips
          int xx = (_scanner.getGameHeight() - 594) / 2;
          Rectangle area3 = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getTopLeft().y + 107, 594, 570);
          _scanner.writeImage(area3, "terminal5.png");
          if (_scanner.scanOne("startTrip.bmp", area3, true) != null)
            _mouse.delay(1000);
          ;
        }
      }

    }
    return false;
  }
}
