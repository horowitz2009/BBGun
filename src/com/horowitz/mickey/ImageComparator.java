package com.horowitz.mickey;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface ImageComparator {

  /**
   * Compares two images with allowed roughness.
   * 
   * @param image1
   *          an image to compare.
   * @param image2
   *          an image to compare.
   * 
   * @return true if images have the same sizes and number of unmatching pixels less or equal to hmm
   */
  public boolean compare(BufferedImage image1, BufferedImage image2);

  public boolean compareOld(BufferedImage image1, BufferedImage image2, Pixel[] indices);

  public boolean compare(BufferedImage image1, BufferedImage image2, Map<Integer, Color[]> colors, Pixel[] indices);

  public List<Pixel> findSimilarities(BufferedImage image1, BufferedImage image2, Pixel[] indices);

  public boolean compare(BufferedImage image, Pixel[] mask, Color[] colors);

  public boolean compareInt(BufferedImage image, Pixel[] mask, Color[] colors);

  public Point findPoint(BufferedImage image, Pixel[] mask, Color[] colors);

  public Pixel findImage(BufferedImage image, BufferedImage area);
  
  public abstract void setErrors(int errors);

  public abstract int getErrors();

  public abstract void setPrecision(int precision);

  public abstract int getPrecision();

  public abstract void setRoughness(double roughness);

  public abstract double getRoughness();

}