package com.horowitz.bigbusiness;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.horowitz.commons.GameLocator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.ImageMask;
import com.horowitz.commons.Settings;
import com.horowitz.mickey.ImageComparator;
import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;
import com.horowitz.mickey.SimilarityImageComparator;
import com.horowitz.mickey.common.MyImageIO;

public class ScreenScanner {

  public final static Logger     LOGGER          = Logger.getLogger(ScreenScanner.class.getName());
  private static final boolean   DEBUG           = false;

  private ImageComparator        _comparator;

  private Pixel                  _br             = null;
  private Pixel                  _tl             = null;
  private boolean                _fullyOptimized = false;

  private ImageData              _hooray         = null;

  private Rectangle              _scanArea       = null;

  private Settings               _settings;
  private GameLocator            _gameLocator;

  private Map<String, ImageData> _imageDataCache;
  private Pixel _safePoint;
	private Rectangle _labelArea;
	private int _labelWidth;

  public ScreenScanner(Settings settings) {
    _settings = settings;
    _comparator = new SimilarityImageComparator(0.04, 2000);
    _gameLocator = new GameLocator();
    _imageDataCache = new Hashtable<String, ImageData>();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle area = new Rectangle(20, 340, screenSize.width - 20 - 404, screenSize.height - 340 - 110);
    try {

      _tl = new Pixel(0, 0);
      _br = new Pixel(screenSize.width - 3, screenSize.height - 3);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void setKeyAreas() throws IOException {
	
	  _fullyOptimized = true;
	  Rectangle area;	  
	  int xx; int yy;
	
	  _scanArea = new Rectangle(_tl.x + 327, _tl.y, getGameWidth() - 327 - 360, getGameHeight());
	  
	  //label area
	  _labelWidth = 380;
	  xx = (getGameWidth() - _labelWidth) / 2;
	  _labelArea = new Rectangle(_tl.x + xx, _tl.y + 71, _labelWidth, 46);
	  
	  _safePoint = new Pixel(0, _tl.y + getGameHeight() / 2);
	
	  // HOORAY
	  xx = (getGameWidth() - 580) / 2;
	  yy = (getGameHeight() - 453) / 2;
	  xx += _tl.x;
	  yy += _tl.y;
	  area = new Rectangle(xx + 194, yy + 397, 192, 39);
	
	  _hooray = new ImageData("Hooray.bmp", area, _comparator, 23, 6);
	
	  getImageData("tags/zzz.bmp", _scanArea, 0, 7);
	  getImageData("tags/coins.bmp", _scanArea, 0, 9);
	  getImageData("tags/houses.bmp", _scanArea, 0, 9);
	  getImageData("tags/fire.bmp", _scanArea, 0, 7);
	  getImageData("tags/medical.bmp", _scanArea, 14, 9);
	  getImageData("tags/greenDown.bmp", _scanArea, 18, -37);
	
	}

	public Rectangle getScanArea() {
    return _scanArea;
  }

  public ImageData getImageData(String filename) throws IOException {
    return getImageData(filename, _scanArea, 0, 0);
  }

  public ImageData getImageData(String filename, Rectangle defaultArea, int xOff, int yOff) throws IOException {
  	if (!new File(filename).exists())
  		return null;
  	
    if (_imageDataCache.containsKey(filename)) {
      return _imageDataCache.get(filename);
    } else {
      ImageData imageData = new ImageData(filename, defaultArea, _comparator, xOff, yOff);
      _imageDataCache.put(filename, imageData);
      return imageData;
    }
  }

  public boolean locateGameArea() throws AWTException, IOException, RobotInterruptedException {
    LOGGER.fine("Locating game area ... ");

    boolean found = _gameLocator.locateGameArea(new ImageData("topLeft.bmp", null, _comparator, -4, -7), new ImageData("bottomRight.bmp", null,
        _comparator, 38 + 6, 19 + 5), false);
    if (found) {
      _tl = _gameLocator.getTopLeft();
      _br = _gameLocator.getBottomRight();
      setKeyAreas();
      return true;
    } else {
      _tl = new Pixel(0, 0);
      _br = new Pixel(1600, 1000);
    }
    return false;
  }

  public void writeImage(Rectangle rect, String filename) {
    try {
      writeImage(new Robot().createScreenCapture(rect), filename);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage2(Rectangle rect, String filename) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
      String date = sdf.format(Calendar.getInstance().getTime());
      String filename2 = filename + " " + date + ".png";

      writeImage(new Robot().createScreenCapture(rect), filename2);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage(BufferedImage image, String filename) {

    try {
      int ind = filename.lastIndexOf("/");
      if (ind > 0) {
        String path = filename.substring(0, ind);
        File f = new File(path);
        f.mkdirs();
      }
      File file = new File(filename);
      MyImageIO.write(image, filename.substring(filename.length() - 3).toUpperCase(), file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void captureGame() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
    String date = sdf.format(Calendar.getInstance().getTime());
    String filename = "popup " + date + ".png";
    captureGame(filename);
  }

  public void captureGame(String filename) {
    writeImage(new Rectangle(new Point(_tl.x, _tl.y), new Dimension(getGameWidth(), getGameHeight())), filename);
  }

  public Pixel locateImageCoords(String imageName, Rectangle[] area, int xOff, int yOff) throws AWTException, IOException, RobotInterruptedException {

    final Robot robot = new Robot();
    final BufferedImage image = ImageIO.read(ImageManager.getImageURL(imageName));
    Pixel[] mask = new ImageMask(imageName).getMask();
    BufferedImage screen;
    int turn = 0;
    Pixel resultPixel = null;
    // MouseRobot mouse = new MouseRobot();
    // mouse.saveCurrentPosition();
    while (turn < area.length) {

      screen = robot.createScreenCapture(area[turn]);
      List<Pixel> foundEdges = findEdge(image, screen, _comparator, null, mask);
      if (foundEdges.size() >= 1) {
        // found
        // AppConsole.print("found it! ");
        int y = area[turn].y;
        int x = area[turn].x;
        resultPixel = new Pixel(foundEdges.get(0).x + x + xOff, foundEdges.get(0).y + y + yOff);
        // System.err.println("AREA: [" + turn + "] " + area[turn]);
        break;
      }
      turn++;
    }
    // mouse.checkUserMovement();
    // AppConsole.println();
    return resultPixel;
  }

  public boolean isOptimized() {
    return _fullyOptimized && _br != null && _tl != null;
  }

  private List<Pixel> findEdge(final BufferedImage targetImage, final BufferedImage area, ImageComparator comparator, Map<Integer, Color[]> colors,
      Pixel[] indices) {
    if (DEBUG)
      try {
        MyImageIO.write(area, "PNG", new File("C:/area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i < (area.getWidth() - targetImage.getWidth()); i++) {
      for (int j = 0; j < (area.getHeight() - targetImage.getHeight()); j++) {
        final BufferedImage subimage = area.getSubimage(i, j, targetImage.getWidth(), targetImage.getHeight());
        if (DEBUG)
          try {
            MyImageIO.write(subimage, "PNG", new File("C:/subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        if (comparator.compare(targetImage, subimage, colors, indices)) {
          // System.err.println("FOUND: " + i + ", " + j);
          result.add(new Pixel(i, j));
          if (result.size() > 0) {// increase in case of trouble
            break;
          }
        }
      }
    }
    return result;
  }

  public void scan() {
    try {
      Robot robot = new Robot();

      BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
      if (DEBUG)
        MyImageIO.write(screenshot, "PNG", new File("screenshot.png"));
    } catch (HeadlessException | AWTException | IOException e) {

      e.printStackTrace();
    }

  }

  public void compare(String imageName1, String imageName2) throws IOException {
    final BufferedImage image1 = ImageIO.read(ImageManager.getImageURL(imageName1));
    Pixel[] mask1 = new ImageMask(imageName1).getMask();
    final BufferedImage image2 = ImageIO.read(ImageManager.getImageURL(imageName2));
    Pixel[] mask2 = new ImageMask(imageName2).getMask();

    List<Pixel> res = compareImages(image1, image2, _comparator, mask2);

    // System.err.println(res);
  }

  public List<Pixel> compareImages(final BufferedImage image1, final BufferedImage image2, ImageComparator comparator, Pixel[] indices) {
    if (DEBUG)
      try {
        ImageIO.write(image2, "PNG", new File("area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i <= (image2.getWidth() - image1.getWidth()); i++) {
      for (int j = 0; j <= (image2.getHeight() - image1.getHeight()); j++) {
        final BufferedImage subimage = image2.getSubimage(i, j, image1.getWidth(), image1.getHeight());
        if (DEBUG)
          try {
            MyImageIO.write(subimage, "PNG", new File("subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }

        boolean b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal: " + b);
        indices = null;
        b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal2: " + b);
        List<Pixel> list = comparator.findSimilarities(image1, subimage, indices);
        // System.err.println("FOUND: " + list);
      }
    }
    return result;
  }

  public Pixel getBottomRight() {
    return _br;
  }

  public Pixel getTopLeft() {
    return _tl;
  }

  public int getGameWidth() {
    int width = isOptimized() ? _br.x - _tl.x : Toolkit.getDefaultToolkit().getScreenSize().width;
    return width != 0 ? width : Toolkit.getDefaultToolkit().getScreenSize().width;
  }

  public int getGameHeight() {
    if (isOptimized()) {
      return _br.y - _tl.y == 0 ? Toolkit.getDefaultToolkit().getScreenSize().height : _br.y - _tl.y;
    } else {
      return Toolkit.getDefaultToolkit().getScreenSize().height;
    }
  }

  public void addHandler(Handler handler) {
    LOGGER.addHandler(handler);
  }

  public ImageData generateImageData(String imageFilename) throws IOException {
    return new ImageData(imageFilename, null, _comparator, 0, 0);
  }

  public ImageData setImageData(String imageFilename) throws IOException {
    return getImageData(imageFilename, _scanArea, 0, 0);
  }

  public ImageData generateImageData(String imageFilename, int xOff, int yOff) throws IOException {
    return new ImageData(imageFilename, null, _comparator, xOff, yOff);
  }

  public ImageComparator getComparator() {
    return _comparator;
  }

  public Pixel getSafePoint() {
    return _safePoint;
  }

	public Rectangle getLabelArea() {
		return _labelArea;
	}

}
