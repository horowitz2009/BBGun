package com.horowitz.mickey.ocr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.common.MyImageIO;

public class OCR2 {
  private Map<Integer, Color> _colors;
  private String              _masksFilename;
  private int                 _masksEmpty;
  private int                 _colorThreshold;
  private double              _errRatio = 0.05;

  public OCR2(String masksFilename, int maskEmpty, double errRatio, int colorThreshold, Color[] foregrounds) {
    super();
    _masksFilename = masksFilename;
    _masksEmpty = maskEmpty;
    _errRatio = errRatio;
    _colorThreshold = colorThreshold;
    _colors = new HashMap<>(2);
    _colors.put(0, foregrounds[0]);
    _colors.put(1, foregrounds[1]);
    _colors.put(2, foregrounds[2]);
    _colors.put(3, foregrounds[3]);
    _colors.put(4, foregrounds[4]);

  }

  public static OCR2 createRed() {
    return new OCR2("masksMissionNEW.txt", 9, 0.05, 5000, new Color[] { new Color(200, 1, 1), new Color(200, 1, 1), new Color(203, 30, 30),
        new Color(215, 88, 88), new Color(229, 158, 158) });
  }

  public static OCR2 createRed8() {
    return new OCR2("masksGray.txt", 0, 0.05, 5000, new Color[] { new Color(200, 1, 1), new Color(200, 1, 1), new Color(203, 30, 30),
        new Color(215, 88, 88), new Color(229, 158, 158) });
  }

  public static OCR2 createGray() {
    return new OCR2("masksMissionNEW.txt", 9, 0.05, 2500, new Color[] { new Color(103, 103, 103), new Color(103, 103, 103), new Color(125, 125, 125),
        new Color(188, 188, 188), new Color(207, 207, 207) });
  }

  public static OCR2 createGray8() {
    return new OCR2("masksGray.txt", 0, 0.05, 2500, new Color[] { new Color(103, 103, 103), new Color(103, 103, 103), new Color(125, 125, 125),
        new Color(188, 188, 188), new Color(207, 207, 207) });
  }

  public static OCR2 createBlue() {
    return new OCR2("masksMissionNEW2.txt", 9, 0.04, 9000, new Color[] { new Color(255, 255, 255), new Color(255, 255, 255),
        new Color(236, 248, 255), new Color(200, 234, 254), new Color(185, 229, 254), });
  }

  /**
   * @deprecated
   * @param masksFilename
   */
  public OCR2(String masksFilename) {
    this(masksFilename, 0, 0.05, 1500, new Color[] { new Color(34, 34, 34), new Color(34, 34, 34), new Color(34, 34, 34), new Color(34, 34, 34),
        new Color(34, 34, 34) });
  }

  private void writeImage(BufferedImage image, int n) {
    if (System.getenv("DEBUG") != null)
      try {
        MyImageIO.write(image, "BMP", new File("subimage" + n + ".bmp"));
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  public String scanImage(BufferedImage image) {
    BufferedImage subimage = image.getSubimage(0, 0, image.getWidth(), image.getHeight());
    writeImage(subimage, 1);
    subimage = cutEdges(subimage);
    writeImage(subimage, 2);
    BufferedImage subimage2 = subimage.getSubimage(0, 0, subimage.getWidth(), subimage.getHeight());
    String result = "";

    Masks masks = new Masks(_masksFilename, _masksEmpty);
    int w = masks.getMaxWidth();
    int wmin = masks.getMinWidth();

    while (subimage.getWidth() >= wmin) {
      // we have space to work
      int ww = w;
      if (subimage.getWidth() < w) {
        ww = subimage.getWidth();
      }
      subimage2 = subimage.getSubimage(0, 0, ww, subimage.getHeight());
      writeImage(subimage2, 101);

      Iterator<Mask> it = masks.getMasks().iterator();
      List<Mask> found = new ArrayList<Mask>();
      while (it.hasNext()) {
        Mask mask = (Mask) it.next();
        Pixel p = findMask(subimage2, mask);
        if (p != null) {
          found.add(mask);
          if (found.size() > 1) {
            // not good. either one or zero should be found
            break;
          }
        }
      }

      if (found.size() == 1) {
        // yahoooo
        Mask m = found.get(0);
        result += m.getName();
        // cut the chunk and move forward
        if (subimage.getWidth() - m.getWidth() <= 0) {
          // it's over
          break;
        }
        subimage = subimage.getSubimage(0 + m.getWidth(), 0, subimage.getWidth() - m.getWidth(), subimage.getHeight());
        writeImage(subimage, 102);
      } else if (found.isEmpty()) {
        int howMuchToTheRight = 1; // or w
        if (subimage.getWidth() - howMuchToTheRight >= wmin) {
          subimage = subimage.getSubimage(0 + howMuchToTheRight, 0, subimage.getWidth() - howMuchToTheRight, subimage.getHeight());
          writeImage(subimage, 103);
        } else {
          // we're done
          break;
        }
      } else {
        // size is 2 or more -> not good!!!
        // skip for now
        // WAIT WAIT WAIT
        String name = found.get(0).getName();
        boolean same = true;

        for (Mask mask : found) {
          if (!mask.getName().equals(name)) {
            same = false;
            break;
          }
        }
        if (same) {
          // Phew
          result += name;
          Mask m = found.get(0);
          if (subimage.getWidth() - m.getWidth() <= 0) {
            // it's over
            break;
          }
          subimage = subimage.getSubimage(0 + m.getWidth(), 0, subimage.getWidth() - m.getWidth(), subimage.getHeight());
          writeImage(subimage, 102);
        } else {
          // why not try again with more restrictive parameters
          _colorThreshold = _colorThreshold / 2;

          Iterator<Mask> it2 = found.iterator();
          List<Mask> found2 = new ArrayList<Mask>();
          while (it2.hasNext()) {
            Mask mask = (Mask) it2.next();
            Pixel p = findMask(subimage2, mask);
            if (p != null) {
              found2.add(mask);
              if (found2.size() > 1) {
                // not good. either one or zero should be found
                break;
              }
            }
          }
          System.err.println("found2 size is " + found2.size());
          if (found2.size() == 1) {
            // NIIIIICE
            result += name;
            Mask m = found2.get(0);
            if (subimage.getWidth() - m.getWidth() <= 0) {
              // it's over
              break;
            }
            subimage = subimage.getSubimage(0 + m.getWidth(), 0, subimage.getWidth() - m.getWidth(), subimage.getHeight());
            writeImage(subimage, 1022);
          } else if (found2.size() > 1) {
            System.err.println("UH OH!!!");
            for (Mask mask : found) {
              System.err.print(mask.getName());
            }
            break;
          }
        }
      }

    }// while

    return result;
  }

  private boolean lineHasOne(int rgb) {
    int diff1 = compareTwoColors(rgb, _colors.get(1).getRGB());
    int diff2 = compareTwoColors(rgb, _colors.get(2).getRGB());
    int diff3 = compareTwoColors(rgb, _colors.get(3).getRGB());
    int diff4 = compareTwoColors(rgb, _colors.get(4).getRGB());
    return (diff1 <= 1100 || diff2 <= 1100 || diff3 <= 1100 || diff4 <= 4);
  }

  private BufferedImage cutEdges(BufferedImage image) {
    BufferedImage subimage;
    // cut north
    boolean lineClean = true;
    int yStart = 0;
    for (int y = 0; y < image.getHeight(); y++) {

      for (int x = 0; x < image.getWidth(); x++) {
        if (lineHasOne(image.getRGB(x, y))) {
          lineClean = false;
          break;
        }
      }
      if (!lineClean) {
        yStart = y;
        // enough
        break;
      }
    }
    subimage = image.getSubimage(0, yStart, image.getWidth(), image.getHeight() - yStart);
    writeImage(subimage, 3);

    // cut south
    lineClean = true;
    yStart = subimage.getHeight() - 1;
    for (int y = subimage.getHeight() - 1; y >= 0; y--) {

      for (int x = 0; x < subimage.getWidth(); x++) {
        if (lineHasOne(image.getRGB(x, y))) {
          lineClean = false;
          break;
        }
      }
      if (!lineClean) {
        yStart = y;
        // enough
        break;
      }
    }
    subimage = subimage.getSubimage(0, 0, subimage.getWidth(), yStart + 1);
    writeImage(subimage, 4);
    // cut west
    boolean colClean = true;
    int xStart = 0;
    for (int xx = 0; xx < subimage.getWidth(); xx++) {

      for (int y = 0; y < subimage.getHeight(); y++) {
        if (lineHasOne(image.getRGB(xx, y))) {
          lineClean = false;
          break;
        }
      }
      if (!colClean) {
        xStart = xx;
        if (xStart > 0)
          xStart--;
        // enough
        break;
      }
    }
    subimage = subimage.getSubimage(xStart, 0, subimage.getWidth() - xStart, subimage.getHeight());
    writeImage(subimage, 5);
    // cut east
    colClean = true;
    xStart = subimage.getWidth() - 1;
    for (int xx = subimage.getWidth() - 1; xx >= 0; xx--) {

      for (int y = 0; y < subimage.getHeight(); y++) {
        if (lineHasOne(image.getRGB(xx, y))) {
          lineClean = false;
          break;
        }
      }
      if (!colClean) {
        xStart = xx;
        if (xStart < subimage.getWidth() - 1)
          xStart++;
        // enough
        break;
      }
    }
    subimage = subimage.getSubimage(0, 0, xStart + 1, subimage.getHeight());
    writeImage(subimage, 6);
    return subimage;
  }

  public Pixel findMask(BufferedImage screen, Mask mask) {
    if (screen.getWidth() < mask.getWidth()) {
      return null;
    }
    for (int i = 0; i <= (screen.getWidth() - mask.getWidth()); i++) {
      for (int j = 0; j <= (screen.getHeight() - mask.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, mask.getWidth(), mask.getHeight());
        // public boolean compare2(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices, double percentage, int diffIndex) {
        if (compare4(subimage, _colors, mask.getPixelsAsArray(), _errRatio, _colorThreshold)) {
          Pixel p = new Pixel(i, j);
          return p;
        } else {
          if (mask.getName().equals("3"))
            writeImage(subimage, 9000);
        }
      }
    }
    return null;
  }

  private int compareTwoColors(int rgb1, int rgb2) {
    final int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
        + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
        + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
    return diff;
  }

  public boolean compare(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices) {
    int countErrors = 0;
    Set<Integer> keys = colors.keySet();
    for (Integer index : keys) {
      for (int i = 0; i < indices.length; i++) {
        if (indices[i].weight == index) {
          final int rgb1 = colors.get(index).getRGB();
          final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
          int diff = compareTwoColors(rgb1, rgb2);
          if (diff > 1100)
            countErrors++;

          if (countErrors > 4) // this color is bad and that's enough
            return false;
        }
      }
    }
    return true;
  }

  /**
   * This method skips pixel with index 4 and over and treats index 0 as opposite 1, 2 and 3.
   * 
   * @param image
   * @param colors
   * @param indices
   * @param percentage
   * @param diffIndex
   * @return
   */
  public boolean compare3(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices, double percentage, int diffIndex) {
    int countErrors = 0;
    int possibleErrors = (int) (indices.length * percentage);
    List<Pixel> errorsW = new ArrayList<>();
    List<Pixel> errors = new ArrayList<>();
    for (int i = 0; i < indices.length; i++) {
      if (indices[i].weight < 3) {
        Color color = colors.get(indices[i].weight);
        if (color != null) {
          final int rgb1 = color.getRGB();
          final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
          final int diff = compareTwoColors(rgb1, rgb2);
          int w = indices[i].weight;
          if (w == 0 && diff <= diffIndex) {
            errorsW.add(indices[i]);
            countErrors++;
          }
          if (w != 0 && diff > diffIndex) {
            countErrors++;
            errors.add(indices[i]);
          }
          // if (countErrors > possibleErrors)
          // return false;
        }
      }
    }
    // if (countErrors >= possibleErrors) {
    // System.err.println("errorsW: " + errorsW);
    // System.err.println("errors: " + errors);
    // }
    return countErrors < possibleErrors;
  }

  /**
   * This method skips pixel with index 3 and 4 and over and treats index 0 as opposite 1 and 2.
   * 
   * @param image
   * @param colors
   * @param indices
   * @param percentage
   * @param diffIndex
   * @return
   */
  public boolean compare4(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices, double percentage, int diffIndex) {
    int countErrors = 0;
    int possibleErrors = (int) (indices.length * percentage);
    for (int i = 0; i < indices.length; i++) {
      if (indices[i].weight < 3) {
        Color color = colors.get(indices[i].weight);
        if (color != null) {
          final int rgb1 = color.getRGB();
          final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
          final int diff = compareTwoColors(rgb1, rgb2);
          int w = indices[i].weight;
          if (w == 0 && diff <= diffIndex) {
            countErrors++;
          }
          if (w != 0 && diff > diffIndex) {
            countErrors++;
          }
          if (countErrors > possibleErrors)
            return false;
        }
      }
    }
    return true;
  }

  public boolean compare2(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices, double percentage, int diffIndex) {
    int countErrors = 0;
    int possibleErrors = (int) (indices.length * percentage);
    for (int i = 0; i < indices.length; i++) {
      Color color = colors.get(indices[i].weight);
      if (color != null) {
        final int rgb1 = color.getRGB();
        final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
        final int diff = compareTwoColors(rgb1, rgb2);
        if (diff > diffIndex)
          countErrors++;
        if (countErrors > possibleErrors)
          return false;
      }
    }
    return true;
  }

  public boolean compare(BufferedImage image, Color color, Pixel[] indices) {
    int countErrors = 0;
    for (int i = 0; i < indices.length; i++) {
      if (indices[i].weight == 1) {
        final int rgb1 = color.getRGB();
        final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
        final int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
            + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
            + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        if (diff > 1100)
          countErrors++;

        if (countErrors > 4) {
          return false;
        }
      }
    }
    return true;
  }

  public static void main(String[] args) {
    try {
      // OCR2 ocr = new OCR2("masks.txt");
      // testImage(ocr, "test/test.bmp", "218250");
      // testImage(ocr, "test/test2.bmp", "115126");
      // testImage(ocr, "test/test3.bmp", "115126");
      // testImage(ocr, "test/test4.bmp", "775");
      // testImage(ocr, "test/test5.bmp", "775");

      OCR2 ocr = createRed();

      testImage(ocr, "test/test10_1.bmp", "25710");
      testImage(ocr, "test/test10_2.bmp", "145690");
      testImage(ocr, "test/test10_3.bmp", "");
      testImage(ocr, "test/test10_14.bmp", "49224");
      testImage(ocr, "test/test10_14.bmp", "49224");

      ocr = createGray();

      testImage(ocr, "test/test10_1.bmp", "/180000");
      testImage(ocr, "test/test10_2.bmp", "");
      testImage(ocr, "test/test10_3.bmp", "/180000");
      testImage(ocr, "test/test10_4.bmp", "/17500");
      testImage(ocr, "test/test10_5.bmp", "/60");
      testImage(ocr, "test/test10_10.bmp", "/50");
      testImage(ocr, "test/test10_11.bmp", "/60");
      testImage(ocr, "test/test10_12.bmp", "/50000");
      testImage(ocr, "test/test10_13.bmp", "300000/300000");
      testImage(ocr, "test/test10_14.bmp", "/65000");
      testImage(ocr, "test/test10_15.bmp", "65000/65000");
      testImage(ocr, "test/test10_16.bmp", "65000/65000");
      testImage(ocr, "test/test10_17.bmp", "/285000");
      testImage(ocr, "test/test10_18.bmp", "/60000");
      testImage(ocr, "test/test10_19.bmp", "/28000");
      testImage(ocr, "test/test10_20.bmp", "45000/45000");

      ocr = createBlue();
      testImage(ocr, "test/test10_white_blue.bmp", "2/90");
      testImage(ocr, "test/test_blue1.bmp", "18/90");
      testImage(ocr, "test/test_blue2.bmp", "25/60");
      testImage(ocr, "test/test_blue3.bmp", "29/40");
      testImage(ocr, "test/test_blue4.bmp", "28/40");
      testImage(ocr, "test/test_blue5.bmp", "45/60");
      testImage(ocr, "test/test_blue6.bmp", "24/40");
      testImage(ocr, "test/test_blue7.bmp", "4/60");
      testImage(ocr, "test/test_blue8.bmp", "11/90");
      testImage(ocr, "test/test_blue9.bmp", "1/100");
      testImage(ocr, "test/test_blue28_60.bmp", "28/60");
      testImage(ocr, "test/test_blue7_60.bmp", "7/60");
      ocr = createGray8();

      testImage(ocr, "test/test81.bmp", "/90000");
      testImage(ocr, "test/test82.bmp", "/90000");
      testImage(ocr, "test/test83.bmp", "60134/45000");
      testImage(ocr, "test/test84.bmp", "/150");
      testImage(ocr, "test/test85.bmp", "121/150");
      testImage(ocr, "test/test86.bmp", "2923803/100000");
      testImage(ocr, "test/test87.bmp", "144684/6800");
      testImage(ocr, "test/test88.bmp", "/6200");
      testImage(ocr, "test/test89.bmp", "2923803/72000");
      testImage(ocr, "test/test810.bmp", "92655/6600");

      ocr = createRed8();

      testImage(ocr, "test/test81.bmp", "0");
      testImage(ocr, "test/test82.bmp", "12141");
      testImage(ocr, "test/test84.bmp", "71");
      testImage(ocr, "test/test8_35038.bmp", "35038");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void testImage(OCR2 ocr, String filename, String expectedText) throws IOException {
    BufferedImage image = ImageIO.read(new File(filename));
    String res = ocr.scanImage(image);
    System.out.println("testing " + filename);
    System.out.println(expectedText);
    System.out.println(res);
    System.out.println(expectedText.equals(res) ? "ok" : "KOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKOKO");
    System.out.println();

  }

}
