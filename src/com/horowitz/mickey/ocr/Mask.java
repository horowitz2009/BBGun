package com.horowitz.mickey.ocr;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

import com.horowitz.mickey.Pixel;

public class Mask {
  private String      _name;
  private List<Pixel> _pixels;
  private int         _width;
  private int         _height;

  public Mask(String name, List<Pixel> pixels, int width, int height) {
    super();
    _name = name;
    _pixels = pixels != null ? pixels : new ArrayList<Pixel>(0);
    _width = width;
    _height = height;
  }

  public String getName() {
    return _name;
  }

  public List<Pixel> getPixels() {
    return _pixels;
  }

  public Pixel[] getPixelsAsArray() {
    return _pixels.toArray(new Pixel[0]);
  }

  public int getWidth() {
    return _width;
  }

  public int getHeight() {
    return _height;
  }

  @Override
  public String toString() {
    return _name + "(" + _width + "," + _height + " - " + _pixels.size() + "): " + _pixels.toString();
  }

  public void printMask(PrintStream out) {
    // first sort it
    Collections.sort(_pixels, new Comparator<Pixel>() {
      @Override
      public int compare(Pixel o1, Pixel o2) {
        return new CompareToBuilder().append(o1.y, o2.y).append(o1.x, o2.x).toComparison();
      }
    });
    int xOld = 0; int yOld = 0;
    Iterator<Pixel> i = _pixels.iterator();
    while (i.hasNext()) {
      Pixel pixel = (Pixel) i.next();
      if (pixel.y != yOld) {
        yOld = pixel.y;
        out.println();
      }
      
      if (pixel.weight == 0)
        out.print("-");
      else
        //out.print("" + pixel.weight);
        out.print("8");
    }
    out.println();
    out.println();
  }
}
