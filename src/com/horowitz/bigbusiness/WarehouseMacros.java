package com.horowitz.bigbusiness;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import com.horowitz.bigbusiness.macros.Macros;
import com.horowitz.bigbusiness.model.Product;
import com.horowitz.bigbusiness.model.ProductionProtocol;
import com.horowitz.bigbusiness.model.ProductionProtocol.Entry;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;

public class WarehouseMacros extends Macros {

  private static final long serialVersionUID = 7142986851416912527L;

  private transient ProductionProtocol _protocol;

  public ProductionProtocol getProtocol() {
    return _protocol;
  }

  public void setProtocol(ProductionProtocol protocol) {
    _protocol = protocol;
  }

  @Override
  public void postDeserialize(Object[] transientObjects) throws Exception {
    super.postDeserialize(transientObjects);
    _protocol = (ProductionProtocol) transientObjects[2];
  }

  public boolean doTheJob(Product pr) throws AWTException, IOException, RobotInterruptedException {
    // _scanner.reduceThreshold();
    Rectangle area = _scanner.getWarehouseArea();

    // make sure we're in the beginning
    _mouse.click(area.x + 22, area.y + 221);
    _mouse.click(area.x + 22, area.y + 221);
    _mouse.click(area.x + 22, area.y + 221);
    _mouse.click(area.x + 22, area.y + 221);

    Rectangle[] cells = new Rectangle[8];
    cells[0] = new Rectangle(area.x + 55, area.y + 57, 170, 156);
    cells[1] = new Rectangle(area.x + 55, area.y + 213, 170, 156);
    cells[2] = new Rectangle(area.x + 225, area.y + 57, 170, 156);
    cells[3] = new Rectangle(area.x + 225, area.y + 213, 170, 156);
    cells[4] = new Rectangle(area.x + 395, area.y + 57, 170, 156);
    cells[5] = new Rectangle(area.x + 395, area.y + 213, 170, 156);
    cells[6] = new Rectangle(area.x + 565, area.y + 57, 170, 156);
    cells[7] = new Rectangle(area.x + 565, area.y + 213, 170, 156);

    // do the end products first
    Pixel p = null;
    boolean sold = false;
    do {
      System.err.println("do");
      int cellNumber = findEndProduct(cells);
      if (cellNumber >= 0) {
        // click
        p = new Pixel(cells[cellNumber].x + 170 / 2, cells[cellNumber].y + 156 / 2);
        _mouse.click(p);
        _mouse.delay(250);

        _mouse.click(area.x + 331, area.y + 346);
        _mouse.delay(1000);
        sold = true;
      } else {
        p = null;
      }
    } while (p != null);
    System.err.println("done with endProducts");

    if (!sold) {
      Pixel prP = _scanner.scanOne("labels/warehouse/" + pr.getName() + ".bmp", area, true);
      if (prP != null) {
        _mouse.delay(1000);
        // how much?
        // well do it one for now
        _mouse.click(area.x + 298, area.y + 286);
        _mouse.delay(1500);
        _mouse.click(area.x + 331, area.y + 346);
        _mouse.delay(1500);
        System.err.println("Sold one. the end");
        sold = true;
      }
    }

    if (!sold) {

      // sell something from protocol
      _scanner.reduceThreshold();
      System.err.println(_protocol);
      List<Entry> entries = _protocol.getEntries();
      for (Entry entry : entries) {
        System.err.println("selling " + entry.product.getName());
        Pixel prP = _scanner.scanOne("labels/warehouse/" + entry.product.getName() + ".bmp", area, true);
        if (prP != null) {
          _mouse.delay(1000);
          // how much?
          // well do it one for now
          _mouse.click(area.x + 298, area.y + 286);
          _mouse.delay(1500);
          _mouse.click(area.x + 331, area.y + 346);
          _mouse.delay(1500);
          System.err.println("Sold one. the end");
          sold = true;
        }

      }
      _scanner.restoreThreshold();
    }
    _mouse.click(_scanner.getSafePoint());
    _mouse.click(_scanner.getSafePoint());
    _mouse.click(_scanner.getSafePoint());
    _mouse.delay(500);
    return sold;
  }

  private int findEndProduct(Rectangle[] cells) throws IOException, AWTException, RobotInterruptedException {
    for (int i = 0; i < cells.length; i++) {
      Rectangle cell = cells[i];
      if (isEndProduct(cell)) {
        // i;
        return i;
      }
    }
    return -1;
  }

  private Rectangle getSubLabelArea(Rectangle cell) {
    return new Rectangle(cell.x + 8, cell.y + 3, 154, 19);
  }

  private boolean isEndProduct(Rectangle cell) throws IOException, AWTException, RobotInterruptedException {
    return _scanner.scanOne("labels/warehouse/dollarBag.bmp", new Rectangle(cell.x + 127, cell.y + 17, 36, 46), false) != null;
  }

  protected void tryProduct(Product pr) throws AWTException, RobotInterruptedException {
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
    } else {
      // TODO paging
    }

  }
}
