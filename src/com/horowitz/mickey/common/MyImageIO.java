package com.horowitz.mickey.common;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class MyImageIO {

  private MyImageIO() {
  }

  private static ImageWriter getWriter(RenderedImage im, String formatName) {
    ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(im);
    Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, formatName);

    if (iter.hasNext()) {
      return iter.next();
    } else {
      return null;
    }
  }

  public static boolean write(RenderedImage im, String formatName, File output) throws IOException {
    if (output == null) {
      throw new IllegalArgumentException("output == null!");
    }
    ImageOutputStream stream = null;

    ImageWriter writer = getWriter(im, formatName);
    if (writer == null) {
      /*
       * Do not make changes in the file system if we have no appropriate writer.
       */
      return false;
    }

    try {
      // output.delete();
      stream = ImageIO.createImageOutputStream(output);
    } catch (IOException e) {
      throw new IIOException("Can't create output stream!", e);
    }

    try {
      return doWrite(im, writer, stream);
    } finally {
      stream.close();
    }
  }

  private static boolean doWrite(RenderedImage im, ImageWriter writer, ImageOutputStream output) throws IOException {
    if (writer == null) {
      return false;
    }
    writer.setOutput(output);
    try {
      writer.write(im);
    } finally {
      writer.dispose();
      output.flush();
    }
    return true;
  }
}
