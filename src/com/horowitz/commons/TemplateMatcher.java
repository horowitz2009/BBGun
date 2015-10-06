package com.horowitz.commons;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.horowitz.mickey.Pixel;

public class TemplateMatcher {

  private int    precision;
  private int    errors;
  private double similarityThreshold = 0.95d;

  public Pixel findMatch(BufferedImage template, BufferedImage image) {
    double maxDistance = Math.sqrt(255 * 255 * 3) * template.getWidth() * template.getHeight();
    maxDistance *= (1 - similarityThreshold);

    for (int i = 0; i <= (image.getWidth() - template.getWidth()); i++) {
      for (int j = 0; j <= (image.getHeight() - template.getHeight()); j++) {
        final BufferedImage subimage = image.getSubimage(i, j, template.getWidth(), template.getHeight());
        double distance = analize(template, subimage, maxDistance);
        if (distance <= maxDistance) {
          return new Pixel(i, j);
        }

      }
    }
    return null;
  }

  public List<Pixel> findMatches(BufferedImage template, BufferedImage image) {
    List<Pixel> result = new ArrayList<Pixel>();

    double maxDistance = Math.sqrt(255 * 255 * 3) * template.getWidth() * template.getHeight();
    maxDistance *= (1 - similarityThreshold);

    for (int j = 0; j <= (image.getHeight() - template.getHeight()); j++) {
      for (int i = 0; i <= (image.getWidth() - template.getWidth()); i++) {
        final BufferedImage subimage = image.getSubimage(i, j, template.getWidth(), template.getHeight());
        double distance = analize(template, subimage, maxDistance);
        if (distance <= maxDistance) {
          result.add(new Pixel(i, j));
          i += template.getWidth() - 2;
          // j += template.getHeight() - 2;
          // j += 3;
          i += 5;
        }

      }
    }
    return result;
  }

  public boolean compare(BufferedImage image1, BufferedImage image2) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return (false);
    }
    int countErrors = 0;

    for (int x = 0; x < image1.getWidth(); x++) {
      for (int y = 0; y < image1.getHeight(); y++) {
        final int rgb1 = image1.getRGB(x, y);
        final int rgb2 = image2.getRGB(x, y);
        int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))

        + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))

        + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        if (diff > precision)
          countErrors++;

        if (countErrors > errors) {
          return false;
        }

      }
    }
    System.out.println(countErrors);
    return true;
  }

  public double analize(BufferedImage image1, BufferedImage image2, double maxDistance) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return Integer.MAX_VALUE;
    }
    double res = 0;

    for (int x = 0; x < image1.getWidth(); x++) {
      for (int y = 0; y < image1.getHeight(); y++) {
        final int rgb1 = image1.getRGB(x, y);
        final int rgb2 = image2.getRGB(x, y);

        int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))

        + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))

        + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        res += Math.sqrt(diff);
        if (res > maxDistance) {
          // too different => skip
          return res;
        }
      }
    }
    return res;
  }

  public static void main(String[] args) {
    try {
      BufferedImage image = ImageIO.read(new File("d:/work/repos/bigbusiness/images2/tags/area.bmp"));
      BufferedImage template = ImageIO.read(new File("d:/work/repos/bigbusiness/images2/tags/coins.bmp"));

      TemplateMatcher matcher = new TemplateMatcher();
      matcher.precision = 2500;
      matcher.errors = 20;
      long start = System.currentTimeMillis();
      Pixel p = matcher.findMatch(template, image);
      System.err.println("time: " + (System.currentTimeMillis() - start));
      if (p != null) {
        System.err.println(p);
      } else {
        System.err.println("sorry");
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

	public double getSimilarityThreshold() {
		return similarityThreshold;
	}

	public void setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
	}

}
