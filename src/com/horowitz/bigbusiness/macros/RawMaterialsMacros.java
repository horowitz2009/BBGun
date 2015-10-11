package com.horowitz.bigbusiness.macros;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;

import com.horowitz.bigbusiness.model.Product;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;

public class RawMaterialsMacros extends Macros {

  private static final long serialVersionUID = -4027449839548848876L;

  public boolean doTheJob(Product pr) throws AWTException, IOException, RobotInterruptedException {
    // dyra byra
    boolean result = false;

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
    Rectangle area = _scanner.getProductionArea3();

    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    
    Rectangle[] cells = new Rectangle[6];
    cells[0] = new Rectangle(area.x + 60, area.y + 61, 258, 154);
    cells[1] = new Rectangle(area.x + 60, area.y + 221, 258, 154);
    cells[2] = new Rectangle(area.x + 333, area.y + 61, 258, 154);
    cells[3] = new Rectangle(area.x + 333, area.y + 221, 258, 154);
    cells[4] = new Rectangle(area.x + 606, area.y + 61, 258, 154);
    cells[5] = new Rectangle(area.x + 606, area.y + 221, 258, 154);


    int pos = pr.getPosition();
    if (pos <= 6) {
      // no need to move
      //_scanner.scanOne(pr.getLabelImage(), new Rectangle(cells[pos - 1].x + 17, cells[pos - 1].y + 3, 222, 23), true);
      _mouse.click(cells[pos - 1].x + 130, cells[pos -1].y + 75);
      return true;
    } else {
      // TODO paging
    }
    return false;
  }

}
