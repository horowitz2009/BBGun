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

import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.common.MyImageIO;

public class OCR {
  private Color               _foreground;
  private Map<Integer, Color> _colors;
  private String              _masksFilename;
  private int                 _masksEmpty;

  public OCR(String masksFilename, int maskEmpty, Color foreground, Color background) {
    super();
    _masksFilename = masksFilename;
    _masksEmpty = maskEmpty;
    _colors = new HashMap<>(2);
    _foreground = foreground;
    _colors.put(1, foreground);
    _colors.put(0, background);

  }

  public OCR(String masksFilename) {
    this(masksFilename, 0, new Color(34, 34, 34), Color.WHITE);
  }

  private void writeImage(BufferedImage image, int n) {
    if (false)
      try {
        MyImageIO.write(image, "PNG", new File("subimage" + n + ".png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public String scanImage(BufferedImage image) {
    BufferedImage subimage = image.getSubimage(0, 0, image.getWidth(), image.getHeight());
    writeImage(subimage, 1);
    //subimage = cutEdges(subimage, _foreground);
    //writeImage(subimage, 2);
    BufferedImage subimage2 = subimage.getSubimage(0, 0, subimage.getWidth(), subimage.getHeight());
    String result = "";

    Masks masks = new Masks(_masksFilename, _masksEmpty);
    int w = masks.getMaxWidth();
    int wmin = masks.getMinWidth();
    // int h = masks.getMaxHeight();

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
          System.out.println("UH OH!!!");
          break;
        }
      }

    }// while

    return result;
  }

  private BufferedImage cutEdges(BufferedImage image, Color foreground) {
    BufferedImage subimage;
    // cut north
    boolean lineClean = true;
    int yStart = 0;
    for (int y = 0; y < image.getHeight(); y++) {

      for (int x = 0; x < image.getWidth(); x++) {
        int diff = compareTwoColors(image.getRGB(x, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
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
        int diff = compareTwoColors(subimage.getRGB(x, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
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
        int diff = compareTwoColors(subimage.getRGB(xx, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
          colClean = false;
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
        int diff = compareTwoColors(subimage.getRGB(xx, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
          colClean = false;
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
        writeImage(subimage, 1001);
        // public boolean compare2(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices, double percentage, int diffIndex) {
        if (compare2(subimage, _colors, mask.getPixelsAsArray(), 0.05, 1200)) {
          Pixel p = new Pixel(i, j);
          return p;
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
      OCR ocr = new OCR("masks.txt");
      testImage(ocr, "test.bmp", "218250");
      testImage(ocr, "test2.bmp", "115126");
      testImage(ocr, "test3.bmp", "115126");
      testImage(ocr, "test4.bmp", "775");
      testImage(ocr, "test5.bmp", "775");

      ocr = new OCR("masksMission.txt", 3, new Color(200, 1, 1), new Color(245, 245, 245));

      testImage(ocr, "test10_1.bmp", "25710");
      testImage(ocr, "test10_2.bmp", "145690");
      testImage(ocr, "test10_3.bmp", "");

      ocr = new OCR("masksMission.txt", 3, new Color(103, 103, 103), new Color(245, 245, 245));

      testImage(ocr, "test10_1.bmp", "/180000");
      testImage(ocr, "test10_2.bmp", "");
      testImage(ocr, "test10_3.bmp", "/180000");
      testImage(ocr, "test10_4.bmp", "/17500");
      testImage(ocr, "test10_5.bmp", "/60");

      ocr = new OCR("masksMission.txt", 3, new Color(255, 255, 255), new Color(16, 161, 246));
      testImage(ocr, "test10_white_blue.bmp", "2/90");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void testImage(OCR ocr, String filename, String expectedText) throws IOException {
    BufferedImage image = ImageIO.read(ImageManager.getImageURL(filename));
    String res = ocr.scanImage(image);
    System.out.println("testing " + filename);
    System.out.println(expectedText);
    System.out.println(res);
    System.out.println(expectedText.equals(res) ? "ok" : "KO");
    System.out.println();

  }

  public void setForegroundColor(Color color) {
    _foreground = color;
    _colors.put(1, color);
  }

}
