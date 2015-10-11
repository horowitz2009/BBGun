package com.horowitz.bigbusiness;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;

import com.horowitz.bigbusiness.macros.Macros;
import com.horowitz.bigbusiness.model.Product;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;

public class FactoryMacros extends Macros {

  private static final long serialVersionUID = -7479781935487662427L;

  public boolean doTheJob(Product pr) throws AWTException, IOException, RobotInterruptedException {
    boolean result = false;
    // dyra byra

    // Pixel pp = _scanner.scanOne(_scanner.getImageData("labels/Ranch2.bmp"),
    // _scanner.getLabelArea(), false);
    // LOGGER.info("pp=" + pp);
    // if (pp != null) {
    // Pixel ppp =
    // _scanner.scanOne(_scanner.getImageData("labels/Production.bmp"), new
    // Rectangle(pp.x - 10, pp.y - 2,
    // 275, 19), false);
    // we are on the right place
    // LOGGER.info("I'm pretty sure the production is open");
    _scanner.reduceThreshold();
    Pixel p = _scanner.scanOne(_scanner.getImageData("labels/Production.bmp"), _scanner.getLabelArea(), false);
    if (p != null)
      result = tryProduct(pr);

    _scanner.restoreThreshold();
    // }
    return result;
  }

  protected boolean tryProduct(Product pr) throws AWTException, RobotInterruptedException {
    Rectangle area = _scanner.getProductionArea2();

    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);

    int pos = pr.getPosition();
    if (pos <= 2) {
      // no need to move
      _scanner.scanOne(pr.getLabelImage(), area, true);
      return true;
    } else {
      // TODO paging
    }
    return false;
  }

}
