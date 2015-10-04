package com.horowitz.mickey;

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id: RoughImageComparator.java,v 1.2.192.1 2006/07/01 05:03:48 jtulach Exp $ $Revision: 1.2.192.1 $ $Date: 2006/07/01 05:03:48 $
 *
 */

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compares two images roughly (i.e. not all of the pixel colors should match).
 * 
 * @author Me
 */
public class RoughImageComparator implements ImageComparator {
  private static final int MAX_X     = 9;
  private static final int MAX_Y     = 13;
  double                   roughness = .0;
  private int              precision;

  /**
   * Creates a comparator with <code>roughness</code> allowed roughness.
   * 
   * @param roughness
   *          Allowed comparison roughness.
   * @param precision
   *          TODO
   */
  public RoughImageComparator(double roughness, int precision) {
    this.roughness = roughness;
    this.precision = precision;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IImageComparator#compare(java.awt.image.BufferedImage, java.awt.image.BufferedImage)
   */
  @Override
  public boolean compare(BufferedImage image1, BufferedImage image2) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return (false);
    }
    double maxRoughPixels = (double) (image1.getWidth() * image1.getHeight()) * roughness;
    int errorCount = 0;
    for (int x = 0; x < image1.getWidth(); x++) {
      for (int y = 0; y < image1.getHeight(); y++) {
        if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
          errorCount++;
          if (errorCount > maxRoughPixels) {
            return (false);
          }
        }
      }
    }
    return (true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IImageComparator#compareOld(java.awt.image.BufferedImage , java.awt.image.BufferedImage, com.horowitz.mickey.Pixel[])
   */
  @Override
  public boolean compareOld(BufferedImage image1, BufferedImage image2, Pixel[] indices) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return (false);
    }
    // double maxRoughPixels = (double) (image1.getWidth() * image1.getHeight())
    // * roughness;
    // int errorCount = 0;
    if ((indices == null) || (indices.length == 0)) {
      for (int x = 0; x < image1.getWidth(); x++) {
        for (int y = 0; y < image1.getHeight(); y++) {
          final int rgb1 = image1.getRGB(x, y);
          final int rgb2 = image2.getRGB(x, y);
          if ((Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) > precision)
              || (Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) > precision)
              || (Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) > precision)) {
            return false;
          }
        }
      }
    } else {
      for (int i = 0; i < indices.length; i++) {
        // System.out.println("color1["+ indices[i].x+", "+
        // indices[i].y+"]="+
        // new Color(image1.getRGB(indices[i].x, indices[i].y)));
        // System.out.println("color2["+ indices[i].x+", "+
        // indices[i].y+"]="+
        // new Color(image2.getRGB(indices[i].x, indices[i].y)));
        final int rgb1 = image1.getRGB(indices[i].x, indices[i].y);
        final int rgb2 = image2.getRGB(indices[i].x, indices[i].y);
        if ((Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) > precision)
            || (Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) > precision)
            || (Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) > precision)) {
          return false;
        }
      }
    }
    return (true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IImageComparator#compare(java.awt.image.BufferedImage, java.awt.image.BufferedImage, com.horowitz.mickey.Pixel[])
   */
  @Override
  public boolean compare(BufferedImage image1, BufferedImage image2, Map<Integer, Color[]> colors, Pixel[] indices) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return (false);
    }
    // double maxRoughPixels = (double) (image1.getWidth() * image1.getHeight())
    // * roughness;
    // int errorCount = 0;
    // int sum = 0;
    // int count = 0;
    int countErrors = 0;
    if ((indices == null) || (indices.length == 0)) {
      // count = image1.getWidth() * image1.getHeight();
      for (int x = 0; x < image1.getWidth(); x++) {
        for (int y = 0; y < image1.getHeight(); y++) {
          final int rgb1 = image1.getRGB(x, y);
          final int rgb2 = image2.getRGB(x, y);
          int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
              + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
              + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
          // System.err.print(diff < 900 ? 1 : 0);
          // sum += (diff < 900 ? 1 : 0);
          if (diff > 1100)
            countErrors++;

          if (countErrors > 4) {
            return false;
          }
          // count++;

        }
        // System.err.println();
      }
    } else {
      // count = indices.length;
      for (int i = 0; i < indices.length; i++) {
        final int rgb1 = image1.getRGB(indices[i].x, indices[i].y);
        final int rgb2 = image2.getRGB(indices[i].x, indices[i].y);
        int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
            + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
            + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        // System.err.print(diff < 900 ? 1 : 0);
        // sum += (diff < 900 ? 1 : 0);
        if (diff > 900)
          countErrors++;
        // float[] fromRGB =
        // ColorSpace.getInstance(ColorSpace.CS_CIEXYZ).fromRGB(new float[]
        // {(rgb1 >> 16) & 0xFF, (rgb1 >> 8) & 0xFF, (rgb1 >> 0) & 0xFF});
        // System.err.print(fromRGB[0]+" "+fromRGB[1]+" " +fromRGB[2]+ "   ");
        // float[] fromRGB2 =
        // ColorSpace.getInstance(ColorSpace.CS_CIEXYZ).fromRGB(new float[]
        // {(rgb2 >> 16) & 0xFF, (rgb2 >> 8) & 0xFF, (rgb2 >> 0) & 0xFF});
        // System.err.println(fromRGB2[0]+" "+fromRGB2[1]+" " +fromRGB2[2]+
        // "   ");
        if (countErrors > 4) {
          return false;
        }

      }
      // System.err.println();
    }
    // return (count * 0.8 <= sum);
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IImageComparator#findSimilarities(java.awt.image. BufferedImage, java.awt.image.BufferedImage,
   * com.horowitz.mickey.Pixel[])
   */
  @Override
  public List<Pixel> findSimilarities(BufferedImage image1, BufferedImage image2, Pixel[] indices) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return (null);
    }
    List<Pixel> result = new ArrayList<Pixel>();

    if ((indices == null) || (indices.length == 0)) {
      for (int x = 0; x < image1.getWidth(); x++) {
        for (int y = 0; y < image1.getHeight(); y++) {
          final int rgb1 = image1.getRGB(x, y);
          final int rgb2 = image2.getRGB(x, y);
          // System.err.print(x + ","+y+" ");
          // System.err.print(Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) &
          // 0xFF)) +" ");
          // System.err.print(Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) &
          // 0xFF))+ " ");
          // System.err.println(Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) &
          // 0xFF)));

          // ((r2 - r1)2 + (g2 - g1)2 + (b2 - b1)2)

          int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
              + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
              + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
          // System.err.print(diff < 900 ? 1 : 0);
          result.add(new Pixel(x, y, diff < 900 ? 1 : 0));
        }
        // System.err.println();
      }
    } else {
      for (int i = 0; i < indices.length; i++) {
        // System.out.println("color1["+ indices[i].x+", "+
        // indices[i].y+"]="+
        // new Color(image1.getRGB(indices[i].x, indices[i].y)));
        // System.out.println("color2["+ indices[i].x+", "+
        // indices[i].y+"]="+
        // new Color(image2.getRGB(indices[i].x, indices[i].y)));
        final int rgb1 = image1.getRGB(indices[i].x, indices[i].y);
        final int rgb2 = image2.getRGB(indices[i].x, indices[i].y);
        int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
            + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
            + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        // System.err.print(diff < 900 ? 1 : 0);
        result.add(new Pixel(indices[i].x, indices[i].y, diff));
      }
      // System.err.println();
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IImageComparator#compare(java.awt.image.BufferedImage, com.horowitz.mickey.Pixel[], java.awt.Color[])
   */
  @Override
  public boolean compare(BufferedImage image, Pixel[] mask, Color[] colors) {
    boolean result = false;
    for (int x = 0; x < (image.getWidth() - MAX_X); x++) {
      for (int y = 0; y < (image.getHeight() - MAX_Y); y++) {
        final BufferedImage subimage = image.getSubimage(x, y, MAX_X, MAX_Y);
        // try {
        // MyImageIO.write(subimage, "png", new File("subimage.png"));
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        if (compareInt(subimage, mask, colors)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IImageComparator#compareInt(java.awt.image.BufferedImage , com.horowitz.mickey.Pixel[], java.awt.Color[])
   */
  @Override
  public boolean compareInt(BufferedImage image, Pixel[] mask, Color[] colors) {
    assert (mask != null) && (mask.length > 0);
    for (int i = 0; i < mask.length; i++) {
      final int rgb1 = image.getRGB(mask[i].x, mask[i].y);
      final Color c1 = new Color(rgb1);
      boolean found = false;
      for (int j = 0; (j < colors.length) && !found; j++) {
        final Color c2 = colors[j];
        if (((c1.getRed() <= c2.getRed()) && (c1.getGreen() <= c2.getGreen()) && (c1.getBlue() <= c2.getBlue()))) {
          found = true;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  /*
   * public Pixel compareLetter(BufferedImage image, Letter letter, Color[] colors) { return compareLetter(image, 0, 0, -1, -1, letter, colors); }
   * 
   * public Pixel compareLetter(BufferedImage image, int startY, int startX, int maxOffsetY, int maxOffsetX, Letter letter, Color[] colors) { int stX
   * = startX; int stY = startY; int maxY = startY + maxOffsetY; int maxX = startX + maxOffsetX; if (maxX > image.getWidth() - letter.getWidth()) {
   * maxX = image.getWidth() - letter.getWidth(); } if (maxY > image.getHeight() - letter.getHeight()) { maxY = image.getHeight() -
   * letter.getHeight(); } if (maxOffsetY < 0) { maxY = image.getHeight() - letter.getHeight(); } else { stY = startY -maxOffsetY > 0 ? startY -
   * maxOffsetY : startY; } if (maxOffsetX < 0) { maxX = image.getWidth() - letter.getWidth(); } else { stX = startX - maxOffsetX > 0? startX -
   * maxOffsetX : startX; }
   * 
   * for (int x = stX; x < maxX; x++) { for (int y = stY ; y < maxY; y++) { // for (int x = startX > 0? startX - maxOffsetX : startX; x < maxX; x++) {
   * // for (int y = startY > 0 ? startY - maxOffsetY : startY; y < maxY; y++) { BufferedImage subimage; try { subimage = image.getSubimage(x, y,
   * letter.getWidth(), letter.getHeight()); // try { // ImageIO.write(subimage, "png", new File("subimage2.png")); // } catch (IOException e) { //
   * e.printStackTrace(); // } // System.err.println("LETTER >>>>>>>>>>>>>>>>>>>>>>> " + letter.getName()); assert letter.getPixelMask() != null :
   * "letter mask null" + letter; assert letter.getPixelMask().length > 0 : "letter mask empty" + letter; //System.err.println(letter + " y=" + y +
   * ", x= "+ x + ", width=" + letter.getWidth() + ", height=" + letter.getHeight() + "   image size is: " + image.getWidth() + " x " +
   * image.getHeight()); if (compareLetterInt(subimage, letter.getPixelMask(), colors)) { return new Pixel(y, x); } } catch (RuntimeException e1) {
   * //e1.printStackTrace(); System.err.println(letter + " y=" + y + ", x= "+ x + ", width=" + letter.getWidth() + ", height=" + letter.getHeight() +
   * "   image size is: " + image.getWidth() + " x " + image.getHeight()); } } } return new Pixel(-1, -1); }
   * 
   * public boolean compareLetterInt(BufferedImage image, Pixel[] mask, Color[] colors) { assert (mask != null) && (mask.length > 0); for (int i = 0;
   * i < mask.length; i++) { final int rgb1 = image.getRGB(mask[i].x, mask[i].y); final Color c1 = new Color(rgb1); if (mask[i].weight < 0) { //
   * negative color boolean found = false; for (int j = 0; (j < colors.length) && !found; j++) { final Color c2 = colors[j]; if (((c1.getRed() !=
   * c2.getRed()) || (c1.getGreen() != c2.getGreen()) || (c1.getBlue() != c2.getBlue()))) { found = true; } } if (!found) { return false; } } else {
   * boolean found = false; for (int j = 0; (j < colors.length) && !found; j++) { final Color c2 = colors[j]; if (((c1.getRed() == c2.getRed()) &&
   * (c1.getGreen() == c2.getGreen()) && (c1.getBlue() == c2.getBlue()))) { found = true; } } if (!found) { return false; } } } return true; }
   */

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IImageComparator#findPoint(java.awt.image.BufferedImage , com.horowitz.mickey.Pixel[], java.awt.Color[])
   */
  @Override
  public Point findPoint(BufferedImage image, Pixel[] mask, Color[] colors) {
    Point result = null;
    for (int x = 0; x < (image.getWidth() - MAX_X); x++) {
      for (int y = 0; y < (image.getHeight() - MAX_Y); y++) {
        final BufferedImage subimage = image.getSubimage(x, y, MAX_X, MAX_Y);

        // try {
        // MyImageIO.write(subimage, "png", new File("subimage.png"));
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        if (compareInt(subimage, mask, colors)) {
          result = new Point(x, y);
          break;
        }
      }
    }
    return result;
  }

  @Override
  public Pixel findImage(BufferedImage image, BufferedImage screen) {
    for (int i = 0; i < (screen.getWidth() - image.getWidth()); i++) {
      for (int j = 0; j < (screen.getHeight() - image.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
        if (compare(image, subimage, null, null)) {
          Pixel p = new Pixel(i, j);
          return p;
        }
      }
    }
    return null;
  }

  public void setErrors(int errors) {
  }

  public int getErrors() {
    return 4;
  }

  public double getRoughness() {
    return roughness;
  }

  public void setRoughness(double roughness) {
    this.roughness = roughness;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

}