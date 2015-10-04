package com.horowitz.mickey;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.Settings;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BinaryOpening;
import Catalano.Imaging.Filters.Difference;
import Catalano.Imaging.Filters.Erosion;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Tools.Blob;
import Catalano.Imaging.Tools.BlobDetection;

public class MotionDetector {

  private final static Logger  LOGGER = Logger.getLogger(MotionDetector.class.getName());
  private static final boolean DEBUG  = false;

  private ImageComparator      _comparator;
  private Settings             _settings;

  public MotionDetector(Settings settings) {
    _settings = settings;
    _comparator = new SimilarityImageComparator(0.04, 2000);

  }

  private boolean compare(BufferedImage image1, BufferedImage image2, int precision, int errors) {
    int countErrors = 0;
    for (int x = 0; x < image1.getWidth(); x++) {
      for (int y = 0; y < image1.getHeight(); y++) {
        final int rgb1 = image1.getRGB(x, y);
        final int rgb2 = image2.getRGB(x, y);
        int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
            + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
            + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        // System.err.print(diff < 900 ? 1 : 0);
        // sum += (diff < 900 ? 1 : 0);
        if (diff > precision)
          countErrors++;

        if (countErrors > errors) {
          return false;
        }
      }
    }
    return false;
  }

  public static void main(String[] args) {

    // JOptionPane.showMessageDialog(null, fb.toIcon());
    // JOptionPane.showMessageDialog(null, fb2.toIcon());

    try {
      for (int i = 5; i <= 5; i++) {
        System.out.println("processing " + i);
        long start = System.currentTimeMillis();        
        FastBitmap fb = new FastBitmap("l1.bmp");
        FastBitmap fb2 = new FastBitmap("l4.bmp");
        fb.toGrayscale();
        fb2.toGrayscale();
        Rectangle rect = new Rectangle(100, 100, 100, 100);
        BufferedImage image = new Robot().createScreenCapture(rect);
        FastBitmap fb0 = new FastBitmap(image);
        
        
        
        Difference difference = new Difference(fb);
        Threshold threshold = new Threshold(i);
        Erosion erosion = new Erosion(1);
        BinaryOpening opening = new BinaryOpening(2);
        

        difference.applyInPlace(fb2);
        threshold.applyInPlace(fb2);
        //ImageIO.write(fb2.toBufferedImage(), "PNG", new File("outputAAA" + i + ".png"));
        //erosion.applyInPlace(fb2);
        //ImageIO.write(fb2.toBufferedImage(), "PNG", new File("outputBBB" + i + ".png"));
        opening.applyInPlace(fb2);
        //ImageIO.write(fb2.toBufferedImage(), "PNG", new File("outputBBB" + i + ".png"));
        
        
        //BlobsFiltering bf = new BlobsFiltering(3,11*5);
        
        
        //bf.applyInPlace(fb2);
        //ImageIO.write(fb2.toBufferedImage(), "PNG", new File("outputCCC" + i + ".png"));
        
        BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
        //bd.setMinArea(1);
        //bd.setMaxArea(100);
        bd.ProcessImage(fb2);
        
        long end = System.currentTimeMillis();
        System.err.println("time: " + (end - start));
        System.err.println(bd.getBlobs());
        
      }
      System.out.println("done.");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  public List<Blob> detect(BufferedImage image1, BufferedImage image2) {
    FastBitmap fb = new FastBitmap(image1);
    FastBitmap fb2 = new FastBitmap(image2);
    fb.toGrayscale();
    fb2.toGrayscale();
    
    Difference difference = new Difference(fb);
    Threshold threshold = new Threshold(5);
    BinaryOpening opening = new BinaryOpening(2);

    difference.applyInPlace(fb2);
    threshold.applyInPlace(fb2);
    opening.applyInPlace(fb2);
    BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
    bd.setMinArea(4);
    bd.setMaxArea(12*8);
    return bd.ProcessImage(fb2);
//    if (bd.getBlobs().size() > 0) {
//      for (Blob blob : bd.getBlobs()) {
//        fb2.saveAsPNG("BLOB_" + blob.getCenter().y + "_" + blob.getCenter().x + "_" + System.currentTimeMillis());
//      }
//    }
    
    //return bd.getBlobs();
  }

}
