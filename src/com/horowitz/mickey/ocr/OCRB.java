package com.horowitz.mickey.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.horowitz.mickey.ImageComparator;
import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.SimilarityImageComparator;
import com.horowitz.mickey.common.MyImageIO;

public class OCRB {

  private ImageComparator     _comparator;

  private List<BufferedImage> _digits;
  private int                 _minWidth;
  private int                 _maxWidth;
  private int                 _maxHeight;

  public OCRB(String prefix) throws IOException {
    this(prefix, new SimilarityImageComparator(0.04, 2000));
  }

  public OCRB(String prefix, ImageComparator comparator) throws IOException {
    _comparator = comparator;
    _digits = new ArrayList<BufferedImage>(10);
    for (int i = 0; i < 10; i++) {
      _digits.add(ImageIO.read(ImageManager.getImageURL("ocr/" + prefix + i + ".bmp")));
    }
    _minWidth = Integer.MAX_VALUE;
    _maxWidth = 0;
    _maxHeight = 0;
    for (BufferedImage bi : _digits) {
      int w = bi.getWidth();
      int h = bi.getHeight();
      if (w > _maxWidth)
        _maxWidth = w;
      if (w < _minWidth)
        _minWidth = w;
      if (h > _maxHeight)
        _maxHeight = h;
    }

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
    // subimage = cutEdges(subimage, _foreground);
    // writeImage(subimage, 2);
    BufferedImage subimage2 = subimage.getSubimage(0, 0, subimage.getWidth(), subimage.getHeight());
    String result = "";

    int w = _maxWidth;
    int wmin = _minWidth;
    // int h = masks.getMaxHeight();

    while (subimage.getWidth() >= wmin) {
      // we have space to work
      int ww = w;
      if (subimage.getWidth() < w) {
        ww = subimage.getWidth();
      }
      subimage2 = subimage.getSubimage(0, 0, ww, subimage.getHeight());
      writeImage(subimage2, 101);

      
      List<Integer> found = new ArrayList<Integer>();
      for (int i = 0; i < _digits.size(); i++) {
        BufferedImage bi = _digits.get(i);
        Pixel p = _comparator.findImage(bi, subimage2);
        if (p != null) {
          found.add(i);
        }
        if (found.size() > 1) {
          // not good
          break;
        }
      }
        
      if (found.size() == 1) {
        // yahoooo
        Integer m = found.get(0);
        result += ("" + m);
        // cut the chunk and move forward
        if (subimage.getWidth() - _digits.get(m).getWidth() <= 0) {
          // it's over
          break;
        }
        subimage = subimage.getSubimage(0 + _digits.get(m).getWidth(), 0, subimage.getWidth() - _digits.get(m).getWidth(), subimage.getHeight());
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
        
        //SKIP FOR NOW
        
        System.err.println(found);
        /*
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
        }*/
      }

    }// while

    return result;
  }

  /*
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
*/

  public static void main(String[] args) {
    try {
      OCRB ocr = new OCRB("digit");
      testImage(ocr, "test_253012.bmp", "253012");
      testImage(ocr, "test_415592.bmp", "415592");
      testImage(ocr, "test_102088.bmp", "102088");

      ocr = new OCRB("g");
      testImage(ocr, "test_27194549823.bmp", "27194549823");
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void testImage(OCRB ocr, String filename, String expectedText) throws IOException {
    BufferedImage image = ImageIO.read(ImageManager.getImageURL(filename));
    String res = ocr.scanImage(image);
    System.out.println("testing " + filename);
    System.out.println(expectedText);
    System.out.println(res);
    System.out.println(expectedText.equals(res) ? "ok" : "KO");
    System.out.println();

  }

}
