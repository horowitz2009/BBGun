package com.horowitz.mickey.trainScanner;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.Settings;
import com.horowitz.mickey.DateUtils;
import com.horowitz.mickey.ImageComparator;
import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.MouseRobot;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;
import com.horowitz.mickey.ScreenScanner;
import com.horowitz.mickey.SimilarityImageComparator;
import com.horowitz.mickey.data.DataStore;

public class TrainScanner {

  private boolean         _locoOnly;
  private ScreenScanner   _scanner;
  private ImageComparator _comparator;
  private MouseRobot      _mouse;
  Logger                  LOGGER;
  private int             _takeABreak;
  private Settings        _settings;

  public TrainScanner(ScreenScanner scanner, Logger logger, int takeABreak) {
    super();
    _scanner = scanner;
    _comparator = new SimilarityImageComparator(0.04, 2000);
    // _comparator.setPrecision(5000);
    // _comparator.setErrors(25);
    _takeABreak = takeABreak;
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e) {
    }
    LOGGER = logger;
  }

  public List<Train> analyzeIntTrains(boolean all) {
    List<Train> trains = new ArrayList<>();
    try {
      int xx = (_scanner.getGameWidth() - 780) / 2;
      int yy = (_scanner.getGameHeight() - 585) / 2;
      xx += _scanner.getTopLeft().x;
      yy += _scanner.getTopLeft().y;

      //click Trains management icon
      _mouse.click(_scanner.getTopLeft().x + 37, _scanner.getBottomRight().y - 33);
      _mouse.delay(1300);
      _mouse.click(xx + 206, yy + 78);
      _mouse.delay(2300);
      _mouse.mouseMove(_scanner.getBottomRight());

      // make sure it is trains and it is international
      Pixel p = _scanner.getTrainsAnchor().findImage();
      if (p != null) {
        int x = p.x - 38;
        int y = p.y - 28;
        LOGGER.info("Scanning int. trains...");

        int xt = x + 38;
        int yt = y + 111;
        trains = scanIntTrains(x, y, xt, yt, all);
      } else {
        System.err.println("can't find trains anchor");
      }

    } catch (RobotInterruptedException | IOException | AWTException e) {
      LOGGER.severe("Scanning interrupted! The data is incomplete!!!");
    } finally {
      closeTrains();
    }
    return trains;
  }

  private List<Train> scanIntTrains(int x, int y, int xt, int yt, boolean all) throws RobotInterruptedException, IOException, AWTException {
    List<Train> trains = new ArrayList<>();

    try {
    // click the beginning of the scroller
    _mouse.mouseMove(x + 737, y + 111 + 21 + 5);
    _mouse.click();
    _mouse.delay(1000);

    // scan first five
    
    Train lastAdded = scanSlots(xt, yt, 1, trains, all);
    boolean isIdle = lastAdded != null ? lastAdded.isIdle() : false;

    while (!reachedEnd(x, y) && lastAdded != null && (all || isIdle)) {
      scrollDown(x, y, 2); // two clicks on down arrow
      yt = findExactlyWhere(x, y);
      lastAdded =  scanSlots(xt, yt, 5, trains, all);
      isIdle = lastAdded != null ? lastAdded.isIdle() : false;
    }

    // scanSlots(xt, yt, 1, trains, all);
    // if (hasScroller) {
    // pageDown(x, y, 2);
    // _mouse.delay(2000);
    // scanSlots(xt, yt, 2, trains, all);
    // _mouse.delay(1000);
    //
    // pageDown(x, y, 3);
    // _mouse.delay(2000);
    // scanSlots(xt, yt, 3, trains, all);
    // _mouse.delay(1000);
    //
    // LOGGER.info("DONE");
    // }
    } catch (RobotInterruptedException e) {
      LOGGER.info("Enough is enough");
      LOGGER.info("Scanned " + trains.size() + " trains");
    }
    return trains;
  }

  private int findExactlyWhere(int x, int y) throws IOException {
    int yt = 0;
    int x1 = x + 724 - 10;
    int y1 = y + 539 - 130;
    Rectangle area = new Rectangle(x1, y1, 20, 130);
    ImageData edgeData = _scanner.generateImageData("int/brEdgeNew.bmp");
    Pixel p = edgeData.findImage(area);
    if (p != null) {
      LOGGER.info("found the edge: " + p);
      yt = p.y - 4 * 85;
    } else {
      LOGGER.info("UH OH");
      yt = x + 111;
    }
    return yt;
  }

  private Train scanSlots(int xt, int yt, int firstSlot, List<Train> trains, boolean all) throws AWTException, IOException {
    boolean isIdle = false;
    boolean isFreeSlot = false;
    Train lastAdded = null;
    
    try {
      for (int slot = firstSlot; slot <= 5; slot++) {
        _mouse.delay(400);
        Rectangle slotArea = new Rectangle(xt, yt + (slot - 1) * 85, 685, 82);
        Rectangle onRoadArea = new Rectangle(slotArea.x + 25, slotArea.y + 49, 75, 20);
        ImageData onRoadData = _scanner.generateImageData("int/dispatched.bmp");
        ImageData freeSlotData = _scanner.generateImageData("int/Add.bmp");
        isIdle = onRoadData.findImage(onRoadArea) == null;
        isFreeSlot = freeSlotData.findImage(onRoadArea) != null;
        
        if (!isFreeSlot && (all || isIdle)) {
          int number = trains.size() + 1;
          String trainId = number + "  " + DateUtils.formatDateForFile(System.currentTimeMillis());
          String fullImageFilename = "data/int/train " + trainId + ".png";
          writeImage(slotArea, fullImageFilename);
          Rectangle newArea = new Rectangle(slotArea.x + 151, slotArea.y + 9, 530, 38);
          String scanImageFilename = "data/int/train " + trainId + "_scanThis.bmp";
          writeImage(newArea, scanImageFilename);
          Robot robot = new Robot();
          Train train = new Train(robot.createScreenCapture(slotArea), robot.createScreenCapture(newArea));
          train.setFullImageFilename(fullImageFilename);
          train.setScanImageFilename(scanImageFilename);

          if (!isIdle) {
            LOGGER.info("The train is on road " + slot);
            // scan additional info
            _mouse.savePosition();
            _mouse.mouseMove(slotArea.x + 120 + 15, slotArea.y + 41);
            _mouse.click();
            _mouse.delay(800);
            Rectangle infoArea = new Rectangle(slotArea.x + 151, slotArea.y, slotArea.width - 151, 50);
            String addInfoFilename = "data/int/trainInfo " + trainId + ".bmp";
            writeImage(infoArea, addInfoFilename);
            train.setAdditionalInfo((robot.createScreenCapture(infoArea)));// cutToEdge was here

            train.setAdditionalInfoFilename(addInfoFilename);

            // // short info
            // ImageData westEnd = _scanner.generateImageData("int/westEnd.bmp");
            // ImageData westEnd2 = _scanner.generateImageData("int/westEnd2.bmp");
            // Pixel pwest = westEnd.findImage(train.getAdditionalInfo().getSubimage(60, 0, 64, train.getAdditionalInfo().getHeight()));
            // if (pwest == null) {
            // pwest = westEnd2.findImage(train.getAdditionalInfo().getSubimage(60, 0, 64, train.getAdditionalInfo().getHeight()));
            // } else {
            // System.err.println("found westEnd");
            // }
            // if (pwest == null) {
            // pwest = new Pixel(13, 0);
            // } else {
            // System.err.println("found westEnd2");
            // }
            //
            // System.err.println(pwest);
            //
            // //train.setAdditionalInfoShort(train.getAdditionalInfo());
            //
            // //String shortFilename = "data/int/trainInfo " + trainId + "_short.png";
            // //_scanner.writeImage(train.getAdditionalInfoShort(), shortFilename);
            // //train.setAdditionalInfoShortFilename(shortFilename);
            train.setIdle(false);

            List<String> contractorNames = scanContractorsSimple(train);

            train.setContractors(contractorNames);
            train.setTimeToSendNext(0l);// TODO ocr capture time and set it OR DEPRECATE IT!
            _mouse.restorePosition();
          } else {
            train.setContractors(new ArrayList<String>());
            train.setIdle(true);
            train.setTimeToSendNext(0l);
          }
          trains.add(train);
          lastAdded = train;
        }// if all or is idle
      }
    } catch (RobotInterruptedException e) {
      LOGGER.info("Interrupted by user! Data may be incomplete!");
      LOGGER.info("So far " + trains.size() + " have been scanned");
    }
    return lastAdded;
  }

  private BufferedImage cutToEdge(BufferedImage image) {
    BufferedImage result = image;
    try {
      BufferedImage edgeImage = ImageIO.read(ImageManager.getImageURL("int/brEdge.bmp"));
      Pixel p = _comparator.findImage(edgeImage, image);
      if (p != null) {
        System.err.println("found edge " + p);
        result = image.getSubimage(0, 0, p.x + edgeImage.getWidth(), p.y + edgeImage.getHeight());
      }
    } catch (IOException e) {
    }
    return result;
  }

  private void pageDown(int x, int y, int page) {
    Pixel p = new Pixel(x + 719, y + 157);
    System.err.println("Page: " + page);
    LOGGER.info("Page: " + page);
    _mouse.mouseMove(p.x, p.y + (147 * (page - 1)));
    try {
      _mouse.delay(200);
      _mouse.click();
      _mouse.delay(300);
      _mouse.click();
      _mouse.delay(500);
      _mouse.click();
      _mouse.delay(500);
    } catch (RobotInterruptedException e) {
    }

  }

  public boolean sendTrains(List<Train> trains) {
    boolean atLeastOneSent = false;
    int x = 0;
    int y = 0;
    try {
      int xx = (_scanner.getGameWidth() - 780) / 2;
      int yy = (_scanner.getGameHeight() - 585) / 2;
      xx += _scanner.getTopLeft().x;
      yy += _scanner.getTopLeft().y;

      //click Trains management icon
      _mouse.click(_scanner.getTopLeft().x + 54, _scanner.getBottomRight().y - 39);
      _mouse.delay(1300);
      _mouse.click(xx + 206, yy + 78);
      _mouse.delay(2300, false);
      _mouse.mouseMove(_scanner.getBottomRight());

      Pixel p = _scanner.getTrainsAnchor().findImage();
      if (p != null) {
        x = p.x - 38;
        y = p.y - 28;
        LOGGER.info("Sending international...");

        int xt = x + 38;
        int yt = y + 111;

        atLeastOneSent = sendIntTrains(trains, x, y, xt, yt);

      } else {
        System.err.println("can't find trains anchor");
        LOGGER.severe("Trains not opened!");
      }
    } catch (IOException | AWTException e) {
      LOGGER.severe("Ooops. Something went wrong!");
      e.printStackTrace();
      closeTrains();
    } catch (RobotInterruptedException e) {
      LOGGER.severe("Interrupted by user");
      closeTrains();
    } finally {
      if (x != 0)
        closeTrains();
    }

    return atLeastOneSent;
  }

  private void closeTrains() {
    Pixel p = _scanner.getTrainsAnchor().findImage();
    if (p != null) {
      int x = p.x - 38;
      int y = p.y - 28;
      _mouse.mouseMove(x + 762, y + 19);
      try {
        _mouse.delay(250, false);
      } catch (RobotInterruptedException e) {
      }
      _mouse.click();
      try {
        _mouse.delay(750);
      } catch (RobotInterruptedException e) {
      }
    }

  }

  private boolean sendIntTrains(List<Train> trains, int x, int y, int xt, int yt) throws RobotInterruptedException, IOException, AWTException {

    int numberSent = 0;
    _mouse.mouseMove(x + 737, y + 111 + 21 + 5);
    _mouse.click();
    _mouse.delay(1000);
    int locoOnlyLength = 110;
    boolean atLeastOneSent = false;
    String defaultContractor = getDefaultContractor(); 
    do {
      List<Train> newTrains = scanSlotsForCompare(xt, yt, 1);
      atLeastOneSent = false;
      
      if (trains.isEmpty()) {
        if (defaultContractor != null && !newTrains.isEmpty() && newTrains.get(0).isIdle()) {
          LOGGER.info("Sending to default contractor: " + defaultContractor);
          if (sendTrain(null, xt, yt)) {
            atLeastOneSent = true;
            numberSent++;
          }
          break;
        }
      } else {

        for (int i = newTrains.size() - 1; i >= 0 && !atLeastOneSent; --i) {
          Train t = newTrains.get(i);
          if (t.isIdle()) {
            boolean found = false;
            for (Train train : trains) {
              
                Pixel p = _comparator.findImage(t.getScanImage(), train.getScanImage());
                if (p == null && isLocoOnly()) {
                  BufferedImage i1 = t.getScanImage();
                  BufferedImage i2 = train.getScanImage();
                  p = _comparator.findImage(i1.getSubimage(i1.getWidth() - locoOnlyLength, 0, locoOnlyLength, i1.getHeight()),
                      i2.getSubimage(i2.getWidth() - locoOnlyLength, 0, locoOnlyLength, i2.getHeight()));
                  //System.err.println("locoOnly " + p);
                }
                if (p != null) {
                  found = true;
                  if (!train.getContractors().isEmpty()) {
                    if (sendTrain(train, xt, yt + (i) * 85 + p.y)) {
                      atLeastOneSent = true;
                      numberSent++;
                    }
                  } else {
                    LOGGER.info("Found match, but contractor not set");
                  }
                  break;
                }
              
            } // inner for
            
            if (!found && defaultContractor != null) {
              LOGGER.info("Can't find a match. Sending to default: " + defaultContractor);
              if (sendTrain(null, xt, yt + (i) * 85)) {
                atLeastOneSent = true;
                numberSent++;
              }
            }
            
          } else {
            LOGGER.info("Train " + (i + 1) + " is not idle");
          }
        } // for newTrains
      }
      System.err.println("...");
    } while (atLeastOneSent && numberSent < _takeABreak);
    return atLeastOneSent;
  }

  public String getDefaultContractor() {
    return _settings.getProperty("IntTrains.defaultContractor", null);
  }
  
  private boolean sendTrain(Train train, int x, int y) throws RobotInterruptedException, IOException, AWTException {
    _mouse.mouseMove(x + 64, y + 60);
    _mouse.delay(250);
    _mouse.click();
    _mouse.delay(650);
    // it is expected a SendDialiog been opened
    int xx = (_scanner.getGameWidth() - 781) / 2;
    int yy = (_scanner.getGameHeight() - 587) / 2;
    xx += _scanner.getTopLeft().x;
    yy += _scanner.getTopLeft().y;

    ImageData selectAnchor = _scanner.generateImageData("int/Select.bmp");
    Pixel p = selectAnchor.findImage(new Rectangle(xx, yy, 175, 75));
    if (p != null) {
      String defaultContractor = getDefaultContractor();
      if (defaultContractor == null) defaultContractor = "";
      
      Pixel tl = new Pixel(p.x - 2, p.y + 62);
      try {
        
        //scroll to top
        _mouse.mouseMove(tl.x + 347, tl.y + 30);
        _mouse.click();
        _mouse.delay(400);
        
        // find and select contractors
        Rectangle carea = new Rectangle(tl.x, tl.y, 147, 260);
        String lastContractor = train == null ? defaultContractor : train.getContractors().get(train.getContractors().size() - 1);
        if (lastContractor.equals("XP")) {
          // do it fast
          _mouse.mouseMove(tl.x + 20, tl.y + 20);
          for(int i = 0; i < 8; i++) {
            _mouse.click();
            _mouse.delay(350);
          }
        } else if(lastContractor.equals("Special")) {
          _mouse.mouseMove(tl.x + 20, tl.y + 20);
          _mouse.click();
          _mouse.delay(350);
        } else {
          if (train == null) {
            sendToContractor(defaultContractor, carea, tl);
          } else {
            for (String cname : train.getContractors()) {
              sendToContractor(cname, carea, tl);
            }
          }
        }
      } catch (Exception e) {
        // no matter what try to send the bloody train
      }

      //click send and choose 4h way
      _mouse.mouseMove(tl.x + 350, tl.y + 417);
      _mouse.delay(300);
      _mouse.click();
      _mouse.delay(700);
      _mouse.click(tl.x + 455, tl.y + 414);
      _mouse.delay(2000);

      // not used
      //////train.setTimeToSendNext(4 * 60 * 60000 + 60000 + System.currentTimeMillis()); // 4h 1m in the future

      return true;
      // at the end
      // train.setSentTime(System.currentTimeMillis());
    }
    return false;
  }
  
  private void sendToContractor(String contractorName, Rectangle carea, Pixel tl) throws AWTException, IOException, RobotInterruptedException {
    for (int page = 0; page < 2; page++) {
      BufferedImage cimage = new Robot().createScreenCapture(carea);
      // writeImage(carea, "carea.bmp");
      BufferedImage contractorImage = ImageIO.read(ImageManager.getImageURL("int/" + contractorName + "_name.bmp"));
      Pixel cp = _comparator.findImage(contractorImage, cimage);
      if (cp != null) {
        // found the contractor
        _mouse.mouseMove(carea.x + cp.x, carea.y + cp.y + 4);
        _mouse.delay(450);
        _mouse.click();
        _mouse.delay(950);
        break;
      } else {
        //scrolldown
        _mouse.delay(200);
        for(int clicks = 0; clicks < 10; clicks++) {
          _mouse.click(tl.x + 347, tl.y + 250);
          _mouse.delay(150);
        }
      }
    }

  }

  private List<Train> scanSlotsForCompare(final int xt, final int yt, final int page) throws AWTException, IOException, RobotInterruptedException {

    List<Train> trains = new ArrayList<>();
    for (int slot = 0; slot < 5; slot++) {
      // _mouse.delay(400);

      Rectangle slotArea = new Rectangle(xt, yt + (slot) * 85, 685, 82);
      Rectangle onRoadArea = new Rectangle(slotArea.x + 25, slotArea.y + 49, 75, 20);
      ImageData onRoadData = _scanner.generateImageData("int/dispatched.bmp");
      boolean isIdle = onRoadData.findImage(onRoadArea) == null;
      Train train;
      if (isIdle) {
        Rectangle newArea = new Rectangle(slotArea.x + 151, slotArea.y + 9 + 6, 530, 25);

        // for debug only
        // String scanImageFilename = "data/int/trainCOMPARE" + (slot + 1) + "_scanThis.bmp";
        // writeImage(newArea, scanImageFilename);

        Robot robot = new Robot();
        train = new Train(robot.createScreenCapture(slotArea), robot.createScreenCapture(newArea));
      } else {
        train = new Train(null, null);
      }
      train.setIdle(isIdle);
      trains.add(train);
    }
    return trains;
  }

  private List<String> scanContractorsSimple(Train train) {
    List<String> result = new ArrayList<>();

    if (train.getAdditionalInfo() != null) {
      BufferedImage subimage = train.getAdditionalInfo().getSubimage(0, 0, train.getAdditionalInfo().getWidth(),
          train.getAdditionalInfo().getHeight() - 0);
      try {
        List<String> activeContractorNames = new DataStore().getActiveContractorNames();
        while (subimage.getWidth() >= 38) {
          boolean atLeastOneFound = false;
          for (String cname : activeContractorNames) {
            if (!result.contains(cname)) {
              String filename = "int/" + cname + "4.bmp";
              URL imageURL = ImageManager.getImageURL(filename);
              if (imageURL != null) {
                BufferedImage cimage = ImageIO.read(imageURL);
                if (cimage != null) {
                  Pixel p = _comparator.findImage(cimage, subimage);
                  if (p != null) {
                    atLeastOneFound = true;
                    System.err.println("Found " + cname);
                    subimage = subimage.getSubimage(cimage.getWidth() + 3, 0, subimage.getWidth() - cimage.getWidth() - 3, subimage.getHeight());
                    result.add(cname);
                    break;
                  }// if
                }// if cimage != null
              }
            }// if contains
          }// for
          if (!atLeastOneFound) {
            break;
          }
        } // while
      } catch (IOException e) {
        e.printStackTrace();
      }
    }// if
    return result;
  }

  private void scrollDown(int x, int y, int clicks) throws RobotInterruptedException {

    _mouse.mouseMove(x + 732 + 5, y + 539 - 8);
    _mouse.delay(200);
    for (int i = 0; i < clicks; i++) {
      _mouse.click();
      _mouse.delay(100);
    }
    _mouse.delay(500);
    LOGGER.info("scrolling down a bit...");
  }

  private boolean reachedEnd(int x, int y) {
    try {
      final ImageData scrollerEnded = _scanner.generateImageData("int/reachedEnd.bmp");
      Rectangle area = new Rectangle(x + 731, y + 539 - 21 - 20, 12, 35);
      return scrollerEnded.findImage(area) != null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private void writeImage(Rectangle area, String filename) {

    _scanner.writeImage(area, filename);

  }

  public void addNewTrains(List<Train> trains, List<Train> newTrains) {
    List<Train> notFound = new ArrayList<>();
    for (Train newTrain : newTrains) {
      boolean found = false;
      for (Train train : trains) {
        Pixel p = _comparator.findImage(train.getScanImage(), newTrain.getScanImage());
        if (p == null && isLocoOnly()) {
          BufferedImage i1 = train.getScanImage();
          BufferedImage i2 = newTrain.getScanImage();
          p = _comparator.findImage(i1.getSubimage(i1.getWidth() - 93, 0, 93, i1.getHeight()),
              i2.getSubimage(i2.getWidth() - 93, 0, 93, i2.getHeight()));

        }
        if (p != null) {
          merge(train, newTrain);
          found = true;
          break;
        }
      }
      if (!found) {
        notFound.add(newTrain);
      }
    }// for newTrains
    trains.addAll(notFound);
  }

  public void mergeTrains(List<Train> trains, List<Train> newTrains) {
    List<Train> notFound = new ArrayList<>();
    List<Train> old = new ArrayList<>();
    for (Train newTrain : newTrains) {
      boolean found = false;
      for (Train train : trains) {
        Pixel p = _comparator.findImage(train.getScanImage(), newTrain.getScanImage());
        if (p == null && isLocoOnly()) {
          BufferedImage i1 = train.getScanImage();
          BufferedImage i2 = newTrain.getScanImage();
          p = _comparator.findImage(i1.getSubimage(i1.getWidth() - 93, 0, 93, i1.getHeight()),
              i2.getSubimage(i2.getWidth() - 93, 0, 93, i2.getHeight()));

        }
        if (p != null) {
          merge(train, newTrain);

          found = true;

          old.add(train);
          trains.remove(train);
          break;
        }
      }
      if (!found) {
        notFound.add(newTrain);
      }
    }// for newTrains

    trains.clear();
    trains.addAll(old);
    trains.addAll(notFound);

  }

  private void merge(Train train, Train newTrain) {

    train.mergeWith(newTrain);
    //
    // train.setFullImageFilename(newTrain.getFullImageFileName());
    // train.setFullImage(newTrain.getFullImage());
    //
    // if (newTrain.isIdle())
    // train.setScanImageFilename(newTrain.getScanImageFileName());
    // else {
    // train.setContractorsBeenSent(newTrain.getContractorsBeenSent());
    // }
    //
    // train.setAdditionalInfoFilename(newTrain.getAdditionalInfoFileName());
    // train.setAdditionalInfo(newTrain.getAdditionalInfo());
    //
    // train.setAdditionalInfoShortFilename(newTrain.getAdditionalInfoShortFileName());
    // train.setAdditionalInfoShort(newTrain.getAdditionalInfoShort());
    //
    // train.setIdle(true);
    // train.setTimeToSendNext(0l);
  }

  // public static void main(String[] args) {
  // List<String> trains = new ArrayList<>();
  //
  // trains.add("1");
  // trains.add("2");
  // trains.add("3");
  // trains.add("4");
  // trains.add("5");
  // String[] s = new String[] { "3", "1", "5" };
  // for (int i = 0; i < 3; i++) {
  // for (String t : trains) {
  // System.err.println(t);
  // if (t.equals(s[i])) {
  // trains.remove(t);
  // System.err.println("removing " + s[i]);
  // break;
  // }
  // }
  // System.err.println();
  // }
  // }

  public boolean isLocoOnly() {
    return _locoOnly;
  }

  public void setLocoOnly(boolean locoOnly) {
    _locoOnly = locoOnly;
  }

  public void setSettings(Settings settings) {
    _settings = settings;
  }

}
