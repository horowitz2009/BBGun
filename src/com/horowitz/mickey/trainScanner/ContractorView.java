package com.horowitz.mickey.trainScanner;

import java.awt.image.BufferedImage;
/**
 * @deprecated
 * @author Zhivko Hristov
 *
 */
public class ContractorView {
  private String        _name;
  private BufferedImage _image;

  public ContractorView(String name, BufferedImage image) {
    super();
    _name = name;
    _image = image;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public BufferedImage getImage() {
    return _image;
  }

  public void setImage(BufferedImage image) {
    _image = image;
  }

}
