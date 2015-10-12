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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.horowitz.bigbusiness.macros.Macros;
import com.horowitz.bigbusiness.macros.RawMaterialsMacros;
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
import com.horowitz.mickey.ocr.OCRB;

public class MainFrame extends JFrame {

  private static final long serialVersionUID = -4827959393249146870L;

  private final static Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

  private static final String APP_TITLE = "BB Gun v0.006";

  private Settings _settings;
  private MouseRobot _mouse;
  private ScreenScanner _scanner;

  private JLabel _mouseInfoLabel;

  private CaptureDialog captureDialog;

  private boolean _stopAllThreads;

  private JTextField _findThisTF;

  private ProductionProtocol _protocol;

  private List<Contract> _contracts;
  private Map<String, Building> _buildingTemplates;
  private List<Building> _buildingLocations;

  private TemplateMatcher _matcher;
  private OCRB _ocrbLevel;
  private OCRB _ocrbWarehouse;

  private JTextField _timeTF;

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

  @SuppressWarnings("serial")
  private void init() throws AWTException {
    setTitle(APP_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setAlwaysOnTop(true);

    _settings = Settings.createSettings("bbgun.properties");
    _scanner = new ScreenScanner(_settings);
    _mouse = _scanner.getMouse();

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
    JToolBar mainToolbar3 = createToolbar3();
    mainToolbar2.setFloatable(false);

    JPanel toolbars = new JPanel(new GridLayout(9, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);
    toolbars.add(mainToolbar3);

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
              Pixel p = _scanner.scanOne(filename, null, true);
              if (p != null) {
                LOGGER.info("found it: " + p);
              } else {
                LOGGER.info(filename + " not found");
                LOGGER.info("trying with redused threshold");
                double old = _matcher.getSimilarityThreshold();
                _matcher.setSimilarityThreshold(0.91d);
                p = _scanner.scanOne(filename, null, true);
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

  @SuppressWarnings("serial")
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
              try {
                scan();
              } catch (RobotInterruptedException e) {
                e.printStackTrace();
              }
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
                try {
                  scan();
                } catch (RobotInterruptedException e) {
                  e.printStackTrace();
                }
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
                  _scanner.scanOne("tags/medical.bmp", null, true);
                  _scanner.scanOne("tags/fire.bmp", null, true);
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
                  _scanner.scanMany("tags/coins.bmp", null, true);
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

                  if (!_scanner.scanMany("tags/houses.bmp", null, true).isEmpty()) {
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

  @SuppressWarnings("serial")
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
              } catch (RobotInterruptedException e) {
                LOGGER.info("interrupted");
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    // TEST
    {
      AbstractAction action = new AbstractAction("Test") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                if (!_scanner.isOptimized()) {
                  scan();
                }

                if (_scanner.isOptimized()) {

                  testBuildings();

                } else {
                  LOGGER.info("I need to know where the game is!");
                }
              } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();

              } catch (RobotInterruptedException e) {
                LOGGER.info("interrupted");
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    // FAST FORWARD
    {
      AbstractAction action = new AbstractAction("+15min") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              changeTime(15);
            }

          });
          
          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // FAST FORWARD
    {
      AbstractAction action = new AbstractAction("-15min") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              changeTime(-15);
            }
            
          });
          
          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    
    return mainToolbar1;
  }

  @SuppressWarnings("serial")
  private JToolBar createToolbar3() {
    JToolBar mainToolbar1 = new JToolBar();
    mainToolbar1.setFloatable(false);
    
    // SCAN
    {
      _timeTF = new JTextField(10);
      mainToolbar1.add(_timeTF);
    }
    
    // FAST FORWARD
    {
      AbstractAction action = new AbstractAction("++") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                String s = _timeTF.getText();
                int n = Integer.parseInt(s);
                changeTime(n);
              } catch (NumberFormatException e) {
                LOGGER.info("Not a number!");
              }

            }
            
          });
          
          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    
    // REWIND
    {
      AbstractAction action = new AbstractAction("--") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                String s = _timeTF.getText();
                int n = Integer.parseInt(s);
                changeTime(-n);
              } catch (NumberFormatException e) {
                LOGGER.info("Not a number!");
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

  @SuppressWarnings("serial")
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

  private void scan() throws RobotInterruptedException {
    try {
      LOGGER.info("Scanning...");
      setTitle(APP_TITLE + " ...");
      boolean found = _scanner.locateGameArea();
      if (found) {

        LOGGER.info("GAME FOUND! BB GUN READY.");
        // fixTheGame();
        _protocol = new ProductionProtocol();
        _buildingLocations = new JsonStorage().loadBuildings();
        postDeserialize(_buildingLocations);
        recalcPositions(false);

        Product milk = new Product("Milk");
        milk.setPosition(1);
        milk.setTime(2);
        milk.setBuildingName("Ranch");
        milk.setLevelRequired(1);
        createLabelImageData(milk);
        _protocol.addEntry(milk, 1, 2, 8);
        {
          Product grain = new Product("Grain");
          createLabelImageData(grain);
          grain.setPosition(1);
          grain.setTime(2);
          grain.setBuildingName("Farm");
          grain.setLevelRequired(1);
          _protocol.addEntry(grain, 1, 3, 6);
        }
        {
          Product product = new Product("Polyethylene");
          createLabelImageData(product);
          product.setPosition(1);
          product.setTime(2);
          product.setBuildingName("PaperMill");
          product.setLevelRequired(1);
          _protocol.addEntry(product, 1, 4, 8);
        }

        setTitle(APP_TITLE + " READY");
      } else {
        LOGGER.info("CAN'T FIND THE GAME!");
        setTitle(APP_TITLE);
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    }

  }

  private void recalcPositions(boolean click) throws RobotInterruptedException {
    assert _buildingLocations != null;
    // find the administrative building first
    try {
      Pixel admP = _scanner.findAdminOffice();
      if (admP != null) {
        LOGGER.info("Administrative Building: " + admP);
        for (Building building : _buildingLocations) {
          Pixel rel = building.getRelativePosition();
          Pixel newAbsPos = new Pixel(admP.x - rel.x, admP.y - rel.y);
          building.setPosition(newAbsPos);
          if (click) {
            _mouse.mouseMove(newAbsPos);
            LOGGER.info(building.getName() + " - " + newAbsPos);
            _mouse.delay(2000);
            _mouse.checkUserMovement();
            _mouse.click(_scanner.getSafePoint());
            _mouse.delay(200);
            _mouse.checkUserMovement();
          }

        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private void postDeserialize(List<Building> buildingLocations) {
    try {
      for (Building building : buildingLocations) {
        building.postDeserialize(new Object[] { _scanner.getImageComparator(), _scanner, _protocol });
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

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
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

  private Building registerBuilding(String buildingName, Macros macros) {
    try {
      Building building = new Building(buildingName);
      building.setMacros(macros);
      createLabelImageData(building);

      try {
        createPictureImageData(building, "buildings");
      } catch (Exception e) {
        LOGGER.warning("Warning: no image for " + buildingName + " in buildings folder");
      }
      _buildingTemplates.put(buildingName, building);
      return building;
    } catch (IOException e) {
      LOGGER.warning("Failed to register " + buildingName);
      e.printStackTrace();
    }
    return null;

  }

  private List<Building> getBuildingLocations(String name, int levelRequired) {
    List<Building> result = new ArrayList<>();
    for (Building b : _buildingLocations) {
      if (b.getName().equals(name) && b.getLevel() >= levelRequired) {
        result.add(b);
      }
    }
    return result;
  }

  private void testBuildings() throws RobotInterruptedException {
    recalcPositions(true);
  }

  @SuppressWarnings("unchecked")
  private void locateKeyBuildings() {
    // TODO Auto-generated method stub
    try {
      LOGGER.info("Locating buildings. Please wait!");

      _buildingLocations.clear();
      _buildingTemplates.clear();

      Building ter = registerBuilding("Terminal", new TerminalMacros());
      registerBuilding("Farm", new RawMaterialsMacros());
      registerBuilding("PaperMill", new RawMaterialsMacros());
      Building ranch = registerBuilding("Ranch", new RawMaterialsMacros());

      registerBuilding("MillingPlant", new FactoryMacros());
      registerBuilding("DairyFactory", new FactoryMacros());
      registerBuilding("Confectionery", new FactoryMacros());

      Pixel p = null;

      List<Pixel> greens = _scanner.scanMany("tags/greenDown.bmp", null, false);
      LOGGER.info("greens: " + greens.size());
      List<Pixel> idles = _scanner.scanMany("tags/zzz.bmp", null, false);
      LOGGER.info("idles: " + idles.size());

      // merge them all
      // greens.addAll(idles);

      // Iterate over all registered buildings
      analizeBuildings(greens, true);
      analizeBuildings(idles, false);

      // NOW Register buildings having no tags
      Building war = registerBuilding("Warehouse", new WarehouseMacros());

      // Warehouse
      // FIREFOX AND CHROME paint differently the warehouse. Reducing the
      // threshold.
      double oldThreshold = _matcher.getSimilarityThreshold();
      _matcher.setSimilarityThreshold(.91d);
      LOGGER.info("looking for warehouse...");
      Pixel pWar = _scanner.scanOne(war.getPictureImage(), null, false);
      if (pWar != null) {
        LOGGER.info("Found warehouse ");
        // even there're more than one warehouses, we'll operate with the one
        // found first
        Building warehouse = war.copy();// is it necessary to clone?
        warehouse.setPosition(pWar);
        warehouse.setLevel(1);
        warehouse.setMacros(new WarehouseMacros());
        _buildingLocations.add(warehouse);
      }
      // restore the threshold
      _matcher.setSimilarityThreshold(oldThreshold);

      // Administrative Building
      // It will be used as anchor. The other buildings' position will be made
      // relative to its position
      LOGGER.info("looking for Administrative Building...");
      Pixel admP = _scanner.findAdminOffice();
      if (admP != null) {
        LOGGER.info("FOUND Administrative Building: " + admP);

        for (Building building : _buildingLocations) {
          if (building.getPosition() != null) {
            Pixel pos = building.getPosition();
            int xRelative = admP.x - pos.x;
            int yRelative = admP.y - pos.y;
            building.setRelativePosition(new Pixel(xRelative, yRelative));
          }
        }
        LOGGER.info("saving building locations...");
        Collections.sort(_buildingLocations);
        new JsonStorage().saveBuildings(_buildingLocations);
        LOGGER.info("done");
      } else {
        LOGGER.info("SORRY. I need the administration building!");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.info("interrupted");
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
  }

  private void analizeBuildings(List<Pixel> spots, boolean areGreens) throws RobotInterruptedException, IOException,
      AWTException, CloneNotSupportedException {
    Pixel p;
    for (int i = 0; i < spots.size(); i++) {
      p = spots.get(i);
      LOGGER.info("Working on " + p);
      // _mouse.mouseMove(p);
      // _mouse.delay(2000);
      // _mouse.checkUserMovement();
      _mouse.mouseMove(_scanner.getSafePoint());
      _mouse.delay(100);

      Rectangle miniArea = generateMiniArea(p);
      if (areGreens) {
        if (_scanner.scanOne("tags/coins.bmp", miniArea, false) != null) {
          // it's a house, skip it
          LOGGER.info("It's a entertainment.");
          continue;
        }
      }

      // fire med
      preprocessBuilding(p, miniArea, null);

      Iterator<String> it = _buildingTemplates.keySet().iterator();
      while (it.hasNext()) {
        String key = (String) it.next();
        Building building = _buildingTemplates.get(key);
        Pixel pp = _scanner.scanOne(building.getLabelImage(), _scanner.getLabelArea(), false);
        if (pp != null) {

          LOGGER.info("FOUND " + building.getName() + ":" + pp);

          // TODO scan for level of the building
          String s = _ocrbLevel.scanImage(new Robot().createScreenCapture(_scanner.getLevelArea()));

          if (s.length() > 0) {
            String[] ss = s.split("/");
            building.setLevel(Integer.parseInt(ss[0]));
            LOGGER.info("Level: " + ss[0]);
          }

          building = building.copy();
          building.setPosition(p);
          _buildingLocations.add(building);
          _mouse.click(_scanner.getSafePoint());
          _mouse.click(_scanner.getSafePoint());
          _mouse.checkUserMovement();

        }
      }
      _mouse.click(_scanner.getSafePoint());
      _mouse.click(_scanner.getSafePoint());
      _mouse.checkUserMovement();

    }
  }

  private boolean preprocessBuilding(Pixel p, Rectangle miniArea, Product pr) throws RobotInterruptedException,
      IOException, AWTException {
    // fire or medical
    boolean fireOrMedical = false;
    if (_scanner.scanOne("tags/fire.bmp", miniArea, false) != null) {
      // it's a house, skip it
      LOGGER.info("It's a fire.");
      fireOrMedical = true;
    }
    if (_scanner.scanOne("tags/medical.bmp", miniArea, false) != null) {
      // it's a house, skip it
      LOGGER.info("It's a red cross.");
      fireOrMedical = true;
    }

    if (fireOrMedical) {
      _mouse.click(p);
      // wait 10seconds or moveing forward???
      // LOGGER.info("waiting 10s");
      // _mouse.mouseMove(_scanner.getSafePoint());
      // _mouse.delay(10000);
      return false;
    }

    // THE CLICK
    _mouse.click(p);
    _mouse.delay(30);
    _mouse.mouseMove(_scanner.getSafePoint());
    _mouse.delay(300);
    _mouse.checkUserMovement();

    if (_scanner.scanOne("labels/HelpNeeded.bmp", _scanner.getLabelArea(), false) != null) {
      _mouse.click(_scanner.getSafePoint());
      _mouse.delay(200);
      _mouse.click(p);
      _mouse.delay(30);
      _mouse.mouseMove(_scanner.getSafePoint());
      _mouse.delay(300);
      _mouse.checkUserMovement();
    }

    boolean weregood = true;

    // check is warehouse full
    Rectangle attentionArea = new Rectangle(_scanner.getLabelArea());
    attentionArea.y += (_scanner.getTopLeft().y + 224 - attentionArea.y);
    Pixel pp = _scanner.scanOne("Attention.bmp", attentionArea, false);
    if (pp != null) {
      attentionArea.y += (_scanner.getTopLeft().y + 320 - attentionArea.y);
      pp = _scanner.scanOne("toWarehouse.bmp", attentionArea, true);
      if (pp != null) {
        System.err.println("dowarehouse");
        weregood = doWarehouse(pr);
      }
    }

    // what if it is ready
    if (weregood && _scanner.scanOne("tags/zzz.bmp", miniArea, false) != null) {
      LOGGER.info("it's idle now");
      _mouse.click(p);
      _mouse.delay(30);
      _mouse.mouseMove(_scanner.getSafePoint());
      _mouse.delay(300);
      _mouse.checkUserMovement();
    }
    return weregood;
  }

  /**
   * 
   * @param pr
   * @return true if warehouse has been successfully emptied
   * @throws AWTException
   * @throws RobotInterruptedException
   * @throws IOException
   */
  private boolean doWarehouse(Product pr) throws AWTException, RobotInterruptedException, IOException {
    List<Building> warehouses = getBuildingLocations("Warehouse", 1);
    Building building = warehouses.get(0);// TODO improve it later
    if (pr == null) {
      _mouse.click(building.getPosition());
      _mouse.delay(500);
      Pixel pp = _scanner.scanOne(building.getLabelImage(), _scanner.getLabelArea(), false);

      if (pp != null) {
        LOGGER.info(building.getName());
        Pixel ppp = _scanner.scanOne(_scanner.getImageData("labels/warehouse/toWarehouseButton2.bmp"),
            _scanner.getPopupArea(), true);
        if (ppp != null) {
          _mouse.delay(500);
        }
      }
    }
    return building.getMacros().doTheJob(pr);
  }

  private Rectangle generateMiniArea(Pixel p) {
    return new Rectangle(p.x - 2 - 18, p.y - 50 + 35, 44, 60);
  }

  @SuppressWarnings("unused")
  private boolean closerToEachOther(Pixel p1, Pixel p2, int distance) {
    return (Math.abs(p1.x - p2.x) <= distance && Math.abs(p1.y - p2.y) <= distance);
  }

  public MainFrame() throws HeadlessException, AWTException {
    super();
    setupLogger();
    init();

    _contracts = new ArrayList<Contract>();
    _buildingTemplates = new Hashtable<String, Building>();
    _buildingLocations = new ArrayList<Building>(20);

    _matcher = _scanner.getMatcher();
    try {
      _ocrbLevel = new OCRB("ocrLevel/");
      // _ocrbWarehouse = new OCRB("ocrWarehouse/");
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    _stopAllThreads = false;

  }

  private void doMagic() {
    assert _scanner.isOptimized();
    setTitle(APP_TITLE + " RUNNING");
    _stopAllThreads = false;
    try {
      do {
        _mouse.click(_scanner.getSafePoint());
        _mouse.click(_scanner.getSafePoint());
        _mouse.click(_scanner.getSafePoint());
        _mouse.delay(200);
        recalcPositions(false);
        
        
        //1. TERMINAL
        doTerminal();
        
        //2. COINS, HOUSES, FIRE and MED
        if (true) {
          BufferedImage screen = new Robot().createScreenCapture(_scanner.getScanArea());
          LOGGER.info("Coins...");
          _mouse.saveCurrentPosition();
          _scanner.scanMany("tags/coins.bmp", screen, true);
          _mouse.checkUserMovement();
          _mouse.click(_scanner.getSafePoint());
          _mouse.delay(200);
          Pixel p = _scanner.scanOne("populationRed.bmp", null, false);
          if (p != null) {
            LOGGER.info("Population full...");
          } else {
            LOGGER.info("Houses...");
            _mouse.delay(1500);
            _scanner.scanMany("tags/houses.bmp", screen, true);
            _mouse.click(_scanner.getSafePoint());
            _mouse.delay(1300);
          }
          LOGGER.info("Med...");
          p = _scanner.scanOneFast("tags/medical.bmp", null, true);
          if (p == null) {
            LOGGER.info("Fire...");
            p = _scanner.scanOneFast("tags/fire.bmp", null, true);
          }
          if (p != null) {
            LOGGER.info("...");
            _mouse.delay(1500);
          }
        }
        

        if (true && _protocol != null) {
          List<Entry> entries = _protocol.getEntries();
          for (Entry entry : entries) {
            doEntry(entry);
            _mouse.delay(1000);

          }
        }
        // doWarehouse(false);

      } while (!_stopAllThreads);

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

  private void rescan() {
    // TODO Auto-generated method stub

  }

  private void doTerminal() throws RobotInterruptedException {
    try {
      List<Building> buildings = getBuildingLocations("Terminal", 1);
      if (!buildings.isEmpty()) {
        for (Building building : buildings) {
          int t = 0;
          Pixel p = null;
          do {
            Pixel bp = building.getPosition();
            _mouse.click(bp);
            t++;
            _mouse.delay(50);
            p = _scanner.scanOne("labels/Terminal.bmp", _scanner.getLabelArea(), false);
            if (p == null) {
              p = _scanner.scanOne("tags/zzz.bmp", generateMiniArea(bp), false);
              if (p != null) {
                _mouse.click(bp);
                _mouse.delay(50);
              }
            }
            System.err.println("trying " + t);
          } while (p != null && t < building.getLevel());

          p = _scanner.scanOne("labels/Terminal.bmp", _scanner.getLabelArea(), false);
          if (p != null) {
            LOGGER.info(building.getName());

            Pixel ppp = _scanner.scanOne(_scanner.getImageData("toTripButton.bmp"), _scanner.getPopupArea(), true);
            if (ppp != null) {
              _mouse.delay(500);
              building.getMacros().doTheJob(null);

            } else {
              LOGGER.info("Busy. Moving on...");
              _mouse.click(_scanner.getSafePoint());
              _mouse.mouseMove(_scanner.getParkingPoint());
            }

          }

        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private void doEntry(Entry entry) throws RobotInterruptedException {
    Product pr = entry.product;
    String buildingName = pr.getBuildingName();
    List<Building> buildings = getBuildingLocations(buildingName, pr.getLevelRequired());
    for (Building building : buildings) {
      try {
        if (building != null && building.getPosition() != null) {
          Pixel p = building.getPosition();
          if (p != null) {
            Rectangle miniArea = generateMiniArea(p);
            _mouse.click(_scanner.getSafePoint());
            _mouse.click(_scanner.getSafePoint());
            _mouse.click(_scanner.getSafePoint());
            _mouse.click(_scanner.getSafePoint());

            if (preprocessBuilding(p, miniArea, pr)) {

              Pixel pp = _scanner.scanOne(building.getLabelImage(), _scanner.getLabelArea(), false);
              if (pp != null) {
                LOGGER.info(building.getName());

                Pixel ppp = _scanner.scanOne(_scanner.getImageData("productionButton.bmp"), _scanner.getPopupArea(),
                    true);
                if (ppp != null) {
                  _mouse.delay(500);
                  building.getMacros().doTheJob(pr);

                } else {
                  LOGGER.info("Busy. Moving on...");
                  _mouse.click(_scanner.getSafePoint());
                  _mouse.mouseMove(_scanner.getParkingPoint());
                }
              }
            } else {
              LOGGER.info("Preprocess error! Moving on...");
              _mouse.click(_scanner.getSafePoint());
              _mouse.mouseMove(_scanner.getParkingPoint());
            }
          }
        }
        _mouse.delay(2000);
      } catch (AWTException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
  }
  
  private void changeTime(int minutes) {
    // TODO Auto-generated method stub
    //Runtime.getRuntime().exec("cmd /C date " + strDateToSet); // dd-MM-yy
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, minutes);
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    try {
      Runtime.getRuntime().exec("cmd /C time " + sdf.format(cal.getTime())); // hh:mm:ss
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
