package com.horowitz.mickey;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class JCanvas extends JPanel {

  public JCanvas() {
    setMaximumSize(new Dimension(1000, 150));
  }

  BufferedImage _image = null;

  public BufferedImage getImage() {
    return _image;
  }

  public void setImage(BufferedImage image) {
    _image = image;
  }

  public void paint(Graphics g) {
    super.paint(g);
    if (_image != null) {
      g.drawImage(_image, 0, 0, null);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension ps = super.getPreferredSize();
    if (_image != null) {
      ps = new Dimension(_image.getWidth(), _image.getHeight());
    }
    return ps;
  }
}