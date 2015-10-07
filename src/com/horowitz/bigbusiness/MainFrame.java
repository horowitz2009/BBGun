package com.horowitz.bigbusiness;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.horowitz.bigbusiness.macros.RawMaterials;
import com.horowitz.bigbusiness.model.BasicElement;
import com.horowitz.bigbusiness.model.Building;
import com.horowitz.bigbusiness.model.Contract;
import com.horowitz.bigbusiness.model.Product;
import com.horowitz.bigbusiness.model.ProductionProtocol;
import com.horowitz.bigbusiness.model.ProductionProtocol.Entry;
import com.horowitz.bigbusiness.model.storage.JsonStorage;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.mickey.MouseRobot;
import com.horowitz.mickey.MyLogger;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;
import com.horowitz.mickey.SimilarityImageComparator;

public class MainFrame extends JFrame {

  private static final long serialVersionUID = -4827959393249146870L;

  private final static Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

  private static final String APP_TITLE = "BB Gun v0.001";

  private Settings _settings;
  private MouseRobot _mouse;
  private ScreenScanner _scanner;

  private JLabel _mouseInfoLabel;

  private CaptureDialog captureDialog;

  private boolean _stopAllThreads;

  private JTextField _findThisTF;

  private ProductionProtocol _protocol;

  private List<Contract> _contracts;
  private Map<String, Building> _buildings;
  private List<Building> _buildingLocations;

  private TemplateMatcher _matcher;

  public static void main(String[] args) {

    try {
      MainFrame frame = new MainFrame();

      frame.pack();
      frame.setSize(new Dimension(frame.getSize().width + 8, frame.getSize().height + 8));
      int w = frame.getSize().width;
      int h = frame.getSize().height;
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int x = screenSize.width - w;
      int y = (screenSize.height - h) / 2;
      frame.setBounds(x, y, w, h);

      frame.setVisible(true);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void createLabelImageData(BasicElement element) throws IOException {
    element.setLabelImage(_scanner.getImageData("labels/" + element.getName() + ".bmp", _scanner.getLabelArea(), 0, 0));
  }

  private void createPictureImageData(BasicElement element, String folder) throws IOException {
    element.setPictureImage(_scanner.getImageData(folder + "/" + element.getName() + ".bmp", _scanner.getScanArea(), 0,
        0));
  }

  private void init() throws AWTException {
    setTitle(APP_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setAlwaysOnTop(true);

    _settings = Settings.createSettings("bbgun.properties");
    _mouse = new MouseRobot();
    _scanner = new ScreenScanner(_settings);

    JPanel rootPanel = new JPanel(new BorderLayout());
    getContentPane().add(rootPanel, BorderLayout.CENTER);

    final JTextArea outputConsole = new JTextArea(8, 14);

    rootPanel.add(new JScrollPane(outputConsole), BorderLayout.CENTER);

    Handler handler = new Handler() {

      @Override
      public void publish(LogRecord record) {
        String text = outputConsole.getText();
        if (text.length() > 3000) {
          outputConsole.setText("");
        }
        outputConsole.append(record.getMessage());
        outputConsole.append("\n");
        outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
        // outputConsole.repaint();
      }

      @Override
      public void flush() {
        outputConsole.repaint();
      }

      @Override
      public void close() throws SecurityException {
        // do nothing

      }
    };
    LOGGER.addHandler(handler);
    ScreenScanner.LOGGER.addHandler(handler);

    JToolBar mainToolbar1 = createToolbar1();
    JToolBar mainToolbar2 = createToolbar2();
    mainToolbar2.setFloatable(false);

    JPanel toolbars = new JPanel(new GridLayout(9, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);

    Box north = Box.createVerticalBox();
    north.add(toolbars);

    _findThisTF = new JTextField();
    Box box = Box.createHorizontalBox();
    box.add(_findThisTF);
    JButton findButton = new JButton(new AbstractAction("Find") {

      @Override
      public void actionPerformed(ActionEvent ae) {
        LOGGER.info("scan for " + _findThisTF.getText());
        final String filename = _findThisTF.getText();
        new Thread(new Runnable() {
          public void run() {
            try {

              _scanner.getImageData(filename);
              Pixel p = scanOne(filename, null, true);
              if (p != null) {
                LOGGER.info("found it: " + p);
              } else {
                LOGGER.info(filename + " not found");
                LOGGER.info("trying with redused threshold");
                double old = _matcher.getSimilarityThreshold();
                _matcher.setSimilarityThreshold(0.91d);
                p = scanOne(filename, null, true);
                if (p != null) {
                  LOGGER.info("found it: " + p);
                } else {
                  LOGGER.info(filename + " not found");
                }
                _matcher.setSimilarityThreshold(old);

              }
            } catch (RobotInterruptedException e) {
              LOGGER.log(Level.WARNING, e.getMessage());
              e.printStackTrace();
            } catch (IOException e) {
              LOGGER.log(Level.WARNING, e.getMessage());
              e.printStackTrace();
            } catch (AWTException e) {
              LOGGER.log(Level.WARNING, e.getMessage());
              e.printStackTrace();
            }

          }
        }).start();

      }
    });
    box.add(findButton);
    north.add(box);
    _mouseInfoLabel = new JLabel(" ");
    north.add(_mouseInfoLabel);
    rootPanel.add(north, BorderLayout.NORTH);

  }

  private JToolBar createToolbar1() {
    JToolBar mainToolbar1 = new JToolBar();
    mainToolbar1.setFloatable(false);

    // SCAN
    {
      AbstractAction action = new AbstractAction("Scan") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              scan();
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    // RUN MAGIC
    {
      AbstractAction action = new AbstractAction("Run") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              LOGGER.info("Let's get rolling...");
              if (!_scanner.isOptimized()) {
                scan();
              }

              if (_scanner.isOptimized()) {
                // DO THE JOB
                doMagic();
              } else {
                LOGGER.info("I need to know where the game is!");
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    // STOP MAGIC
    {
      AbstractAction action = new AbstractAction("Stop") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {

            @Override
            public void run() {
              LOGGER.info("Stopping BB Gun");
              _stopAllThreads = true;
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // RECORD
    {
      AbstractAction action = new AbstractAction("R") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              record();
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // COINS
    {
      AbstractAction action = new AbstractAction("F") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {
                  _mouse.savePosition();
                  scanOne("tags/medical.bmp", null, true);
                  scanOne("tags/fire.bmp", null, true);
                  _mouse.restorePosition();
                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (AWTException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // COINS
    {
      AbstractAction action = new AbstractAction("Coins") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {

                  // scanCoins();
                  LOGGER.info("Scan for coins...");
                  _mouse.savePosition();
                  scanMany("tags/coins.bmp", null, true);
                  _mouse.restorePosition();
                  LOGGER.info("Done");

                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (AWTException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // Houses
    {
      AbstractAction action = new AbstractAction("Houses") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {

                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {
                  LOGGER.info("Scan for houses...");
                  _mouse.savePosition();

                  if (!scanMany("tags/houses.bmp", null, true).isEmpty()) {
                    _mouse.delay(200);
                  }
                  _mouse.click(_scanner.getSafePoint());
                  _mouse.delay(100);
                  _mouse.click(_scanner.getSafePoint());
                  _mouse.restorePosition();
                  LOGGER.info("Done.");

                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (AWTException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    return mainToolbar1;
  }

  private JToolBar createToolbar2() {
    JToolBar mainToolbar1 = new JToolBar();
    mainToolbar1.setFloatable(false);

    // SCAN
    {
      AbstractAction action = new AbstractAction("Locate buildings") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {

                  locateKeyBuildings();

                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();

              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    return mainToolbar1;
  }

  private void setupLogger() {
    try {
      MyLogger.setup();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Problems with creating the log files");
    }
  }

  private static class CaptureDialog extends JFrame {
    Point _startPoint;
    Point _endPoint;
    Rectangle _rect;
    boolean inDrag;

    public CaptureDialog() {
      super("hmm");
      setUndecorated(true);
      getRootPane().setOpaque(false);
      getContentPane().setBackground(new Color(0, 0, 0, 0.05f));
      setBackground(new Color(0, 0, 0, 0.05f));

      _startPoint = null;
      _endPoint = null;
      inDrag = false;

      // events

      addMouseListener(new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
          inDrag = true;

        }

        @Override
        public void mouseClicked(MouseEvent e) {

          if (e.getButton() == MouseEvent.BUTTON1) {
            if (_startPoint == null) {
              LOGGER.info("clicked once " + e.getButton() + " (" + e.getX() + ", " + e.getY() + ")");
              _startPoint = e.getPoint();
              repaint();
            } else {
              _endPoint = e.getPoint();
              // LOGGER.info("clicked twice " + e.getButton() +
              // " (" + e.getX() + ", " + e.getY() + ")");
              setVisible(false);
              LOGGER.info("AREA: " + _rect);
            }
          } else if (e.getButton() == MouseEvent.BUTTON3) {
            _startPoint = null;
            _endPoint = null;
            repaint();
          }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          // LOGGER.info("REL:"+e);

          if (inDrag && _endPoint != null && _startPoint != null) {
            // LOGGER.info("end of drag " + e.getButton() + " (" +
            // e.getX() + ", " + e.getY() + ")");
            inDrag = false;
            setVisible(false);
            LOGGER.info("AREA: " + _rect);
            // HMM
            dispose();
          }

        }

      });

      addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          // LOGGER.info("move " + e.getPoint());
          _endPoint = e.getPoint();
          repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
          if (_startPoint == null) {
            _startPoint = e.getPoint();
          }
          _endPoint = e.getPoint();
          repaint();
          // LOGGER.info("DRAG:" + e);
        }

      });

    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);
      if (_startPoint != null && _endPoint != null) {
        g.setColor(Color.RED);
        int x = Math.min(_startPoint.x, _endPoint.x);
        int y = Math.min(_startPoint.y, _endPoint.y);
        int w = Math.abs(_startPoint.x - _endPoint.x);
        int h = Math.abs(_startPoint.y - _endPoint.y);
        _rect = new Rectangle(x, y, w, h);

        g.drawRect(x, y, w, h);

        // g.setColor(Color.GRAY);
        // g.drawString("[" + w + ", " + h + "]", w / 2 - 13, h / 2 -
        // 3);
        g.setColor(Color.RED);
        g.drawString(x + ", " + y + ", [" + w + ", " + h + "]", x + 3, y + 13);
      }
    }
  }

  private void scan() {
    try {
      LOGGER.info("Scanning...");

      boolean found = _scanner.locateGameArea();
      if (found) {
        LOGGER.info("GAME FOUND! BB GUN READY.");
        // fixTheGame();
        _buildingLocations = new JsonStorage().loadBuildings();
        postDeserialize(_buildingLocations);
        recalcPositions();
        setTitle(APP_TITLE + " READY");
      } else {
        LOGGER.info("CAN'T FIND THE GAME!");
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.log(Level.SEVERE, "Interrupted by user", e);
      e.printStackTrace();
    }

  }

  private void recalcPositions() {
    // TODO Auto-generated method stub
    assert _buildingLocations != null;
    //find the administrative building first
    try {
      Pixel admP = scanOne("buildings/Administrative.bmp", null, false);
      if (admP != null) {
        LOGGER.info("Administrative Building: " + admP);
        for (Building building : _buildingLocations) {
          Pixel rel = building.getRelativePosition();
          Pixel newAbsPos = new Pixel(admP.x - rel.x, admP.y - rel.y);
          building.setPosition(newAbsPos);
          //for test only
          _mouse.mouseMove(newAbsPos);
          LOGGER.info("position of " +building.getName() + " is " + newAbsPos);
          _mouse.delay(2000);
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AWTException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void postDeserialize(List<Building> buildingLocations) {
    try {
      for (Building building : buildingLocations) {
        
        building.getLabelImage().postDeserialize(new Object[]{_scanner.getImageComparator()});
        building.getPictureImage().postDeserialize(new Object[]{_scanner.getImageComparator()});
        
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

  private void record() {
    try {
      LOGGER.info("Recording the mouse movement (for now)");

      captureDialog = new CaptureDialog();
      if (_scanner.isOptimized()) {
        captureDialog.setBounds(_scanner.getTopLeft().x, _scanner.getTopLeft().y, _scanner.getGameWidth(),
            _scanner.getGameHeight());
      } else {
        captureDialog.setBounds(0, 0, 1679, 1009);
      }
      captureDialog.setVisible(true);
      try {
        while (true) {
          Point loc = MouseInfo.getPointerInfo().getLocation();
          // LOGGER.info("location: " + loc.x + ", " + loc.y);
          _mouseInfoLabel.setText("location: " + loc.x + ", " + loc.y);
          _mouse.delay(250, false);

        }
      } catch (RobotInterruptedException e) {
        LOGGER.info("interrupted");
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    }

  }

  private void scanCoins() throws RobotInterruptedException, IOException, AWTException {
    LOGGER.info("Click coins...");
    _mouse.savePosition();
    ImageData imageData = _scanner.getImageData("tags/coin.bmp");

    Rectangle area = imageData.getDefaultArea();
    BufferedImage screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen);
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);
      // LOGGER.info("found coins at ");
      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        // LOGGER.info("" + pixel);
        _mouse.click(pixel.x, pixel.y);
        // _mouse.delay(100);
      }
    }
    _mouse.restorePosition();
    LOGGER.info("Done!");
  }

  private List<Pixel> scanMany(String filename, BufferedImage screen, boolean click) throws RobotInterruptedException,
      IOException, AWTException {

    ImageData imageData = _scanner.getImageData(filename);
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    return scanMany(imageData, screen, click);
  }

  private List<Pixel> scanMany(ImageData imageData, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    Rectangle area = imageData.getDefaultArea();
    if (screen == null)
      screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen);
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);

      // filter similar
      if (matches.size() > 1) {
        for (int i = matches.size() - 1; i > 0; --i) {
          for (int j = i - 1; j >= 0; --j) {
            Pixel p1 = matches.get(i);
            Pixel p2 = matches.get(j);
            if (Math.abs(p1.x - p2.x) <= 3 && Math.abs(p1.y - p2.y) <= 3) {
              // too close to each other
              // remove one
              matches.remove(j);
              --i;
            }
          }
        }
      }

      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        if (click)
          _mouse.click(pixel.x, pixel.y);
      }
    }
    return matches;
  }

  private List<Pixel> findMany(String filename) throws RobotInterruptedException, IOException, AWTException {
    ImageData imageData = _scanner.getImageData(filename);
    Rectangle area = imageData.getDefaultArea();
    BufferedImage screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen);
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);
      // LOGGER.info("found houses at ");
      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        // LOGGER.info("" + pixel);
      }
    }
    return matches;
  }

  private Pixel scanOne(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen);
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.info("found: " + pixel);
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  private Pixel scanOne(String filename, Rectangle area, boolean click) throws RobotInterruptedException, IOException,
      AWTException {
    ImageData imageData = _scanner.getImageData(filename);
    if (imageData == null)
      return null;
    if (area == null)
      area = imageData.getDefaultArea();

    BufferedImage screen = new Robot().createScreenCapture(area);
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen);
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.info("found: " + pixel);
      if (click) {
        _mouse.click(pixel.x, pixel.y);
        _mouse.delay(100);
      }
    }
    return pixel;
  }

  private Building registerBuilding(String buildingName) {
    try {
      Building building = new Building(buildingName);
      createLabelImageData(building);
      createPictureImageData(building, "buildings");
      _buildings.put(buildingName, building);
      return building;
    } catch (IOException e) {
      LOGGER.warning("Failed to register " + buildingName);
      e.printStackTrace();
    }
    return null;

  }

  private Building getBuilding(String name) {
    for (Building b : _buildingLocations) {
      if (b.getName().equals(name)) {
        return b;
      }
    }
    return null;
  }

  private void locateKeyBuildings() {
    // TODO Auto-generated method stub
    try {
      LOGGER.info("Locating buildings. Please wait!");

      _buildingLocations.clear();
      _buildings.clear();

      Building ter = registerBuilding("Terminal");
      Building ranch = registerBuilding("Ranch");
      ranch.setMacros(new RawMaterials());

      _protocol = new ProductionProtocol();

      Product milk = new Product("Milk");
      milk.setPosition(1);
      milk.setTime(2);
      milk.setBuilding(ranch);
      createLabelImageData(milk);

      _protocol.addEntry(milk, 1, 0, 100);

      Pixel p = null;

      List<Pixel> greens = scanMany("tags/greenDown.bmp", null, false);
      LOGGER.info("greens: " + greens.size());
      List<Pixel> idles = scanMany("tags/zzz.bmp", null, false);
      LOGGER.info("idles: " + idles.size());

      // merge them all
      greens.addAll(idles);

      // Iterate over all registered buildings
      for (int i = 0; i < greens.size(); i++) {
        p = greens.get(i);
        LOGGER.info("" + p);
        Rectangle area = new Rectangle(p.x - 27, p.y + 38, 70, 28);
        Iterator<String> it = _buildings.keySet().iterator();
        while (it.hasNext()) {
          String key = (String) it.next();
          Building building = _buildings.get(key);
          Pixel pp = scanOne(building.getPictureImage(), area, false);
          if (pp != null) {
            LOGGER.info(building.getName() + ":" + pp);
            building.setPosition(p);
            _buildingLocations.add(building);
          }
        }
      }

      // NOW Register buildings having no tags
      Building war = registerBuilding("Warehouse");

      // Warehouse
      // FIREFOX AND CHROME paint differently the warehouse. Reducing the
      // threshold.
      double oldThreshold = _matcher.getSimilarityThreshold();
      _matcher.setSimilarityThreshold(.91d);
      Pixel pWar = scanOne(war.getPictureImage(), null, false);
      if (pWar != null) {
        LOGGER.info("Found warehouse ");
        // even there're more than one warehouses, we'll operate with the one
        // found first
        Building warehouse = war.copy();// is it necessary to clone?
        warehouse.setPosition(pWar);
        _buildingLocations.add(warehouse);
      }
      // restore the threshold
      _matcher.setSimilarityThreshold(oldThreshold);

      // Administrative Building
      // It will be used as anchor. The other buildings' position will be made
      // relative to its position
      Pixel admP = scanOne("buildings/Administrative.bmp", null, false);
      if (admP != null) {
        LOGGER.info("Administrative Building: " + admP);
      }

      for (Building building : _buildingLocations) {
        if (building.getPosition() != null) {
          Pixel pos = building.getPosition();
          int xRelative = admP.x - pos.x;
          int yRelative = admP.y - pos.y;
          building.setRelativePosition(new Pixel(xRelative, yRelative));
        }
      }
      
      new JsonStorage().saveBuildings(_buildingLocations);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      e.printStackTrace();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
  }

  private boolean closerToEachOther(Pixel p1, Pixel p2, int distance) {
    return (Math.abs(p1.x - p2.x) <= distance && Math.abs(p1.y - p2.y) <= distance);
  }

  private void stopMagic() {
    assert _scanner.isOptimized();
    setTitle(APP_TITLE + " RUNNING");

  }

  public MainFrame() throws HeadlessException, AWTException {
    super();
    setupLogger();
    init();

    _contracts = new ArrayList<Contract>();
    _buildings = new Hashtable<String, Building>();
    _buildingLocations = new ArrayList<Building>(20);

    _matcher = new TemplateMatcher();
    _stopAllThreads = false;

  }

  private void doMagic() {
    assert _scanner.isOptimized();
    setTitle(APP_TITLE + " RUNNING");

    try {
      do {
        BufferedImage screen = new Robot().createScreenCapture(_scanner.getScanArea());
        LOGGER.info("Coins...");
        List<Pixel> coins = scanMany("tags/coins.bmp", screen, false);
        // _mouse.delay(1500);
        // LOGGER.info("Houses...");
        List<Pixel> houses = scanMany("tags/houses.bmp", screen, false);
        coins.addAll(houses);
        Collections.sort(coins);
        Collections.reverse(coins);
        for (Pixel pixel : coins) {
          _mouse.click(pixel);
        }
        // _mouse.delay(200);
        _mouse.click(_scanner.getSafePoint());
        _mouse.delay(1300);

        LOGGER.info("Med...");
        Pixel p = scanOne("tags/medical.bmp", null, true);
        if (p == null) {
          LOGGER.info("Fire...");
          p = scanOne("tags/fire.bmp", null, true);
        }
        if (p != null) {
          LOGGER.info("...");
          _mouse.delay(1500);
        }
        if (_protocol != null) {
          List<Entry> entries = _protocol.getEntries();
          for (Entry entry : entries) {
            doEntry(entry);
          }
        }

      } while (true);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AWTException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.info("interrupted");
      setTitle(APP_TITLE);
      // e.printStackTrace();
    }

  }

  private void doEntry(Entry entry) throws RobotInterruptedException {
    // TODO Auto-generated method stub
    Product pr = entry.product;
    String buildingName = pr.getBuilding().getName();
    Building building = getBuilding(buildingName);

    try {
      if (building != null && building.getPosition() != null) {
        _mouse.click(building.getPosition());
        _mouse.delay(300);
        Pixel pp = scanOne(building.getLabelImage(), _scanner.getLabelArea(), false);
        if (pp != null) {
          LOGGER.info(building.getName());

          if (building.isIdle()) {
            // TODO
          }

          Pixel ppp = scanOne(_scanner.getImageData("productionButton.bmp"), _scanner.getPopupArea(), true);
          LOGGER.info("production " + ppp);
          if (ppp != null) {
            _mouse.delay(500);
            pp = scanOne(_scanner.getImageData("labels/Ranch2.bmp"), _scanner.getLabelArea(), false);
            LOGGER.info("pp=" + pp);
            if (pp != null) {
              ppp = scanOne(_scanner.getImageData("labels/Production.bmp"),
                  new Rectangle(pp.x - 10, pp.y - 2, 275, 19), false);
              // we are on the right place
              LOGGER.info("I'm pretty sure the production is open");
              tryProduct(pr);
            }

          } else {
            LOGGER.info("Busy. Moving on...");
            _mouse.click(_scanner.getSafePoint());
            _mouse.mouseMove(_scanner.getParkingPoint());
          }
        }

      }
    } catch (AWTException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void tryProduct(Product pr) throws AWTException, RobotInterruptedException {
    Rectangle area = _scanner.getProductionArea3();

    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);
    _mouse.click(area.x + 27, area.y + 215);

    int pos = pr.getPosition();
    if (pos <= 6) {
      // no need to move
      scanOne(pr.getLabelImage(), area, true);
    }

  }

}
