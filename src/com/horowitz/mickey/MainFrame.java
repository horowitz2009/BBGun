/**
 * 
 */
package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Catalano.Core.IntPoint;
import Catalano.Imaging.Tools.Blob;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.Settings;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.ocr.OCRB;
import com.horowitz.mickey.service.Service;
import com.horowitz.mickey.trainScanner.TrainManagementWindow;
import com.horowitz.mickey.trainScanner.TrainScanner;

/**
 * @author zhristov
 * 
 */
public final class MainFrame extends JFrame {

  private final static Logger   LOGGER              = Logger.getLogger(MainFrame.class.getName());

  private static final String   APP_TITLE           = "v0.954";

  private boolean               _devMode            = false;

  private ScreenScanner         _scanner;
  private MouseRobot            _mouse;
  private boolean               _stopThread         = false;
  private Statistics            _stats;
  private JLabel                _trainsNumberLabel;
  private JLabel                _trainsNumberLabelA;
  private JLabel                _freightTrainsNumberLabel;
  private JLabel                _freightTrainsNumberLabelA;
  private JLabel                _expressTrainsNumberLabel;
  private JLabel                _expressTrainsNumberLabelA;
  private JLabel                _refreshNumberLabel;
  private JLabel                _refreshMNumberLabel;
  private JLabel                _refreshTNumberLabel;
  private JLabel                _refreshSNumberLabel;
  private JLabel                _refreshNumberLabelA;
  private JLabel                _lastActivityLabel;
  private JLabel                _startedLabel;

  private JButton               _locateAction;
  private JButton               _resetAction;
  private JButton               _doMagicAction;

  private Location              _freeTime           = Locations.LOC_6MIN;
  private Location              _xpTime             = Locations.LOC_6MIN;
  private Location              _freightTime        = Locations.LOC_10MIN;
  private Location              _expressTime        = Locations.LOC_30MIN;

  private Settings              _settings;
  private Settings              _commands;

  private JToggleButton         _autoRefreshClick;

  private JToggleButton         _pingClick;
  private long                  _lastPingTime;

  private JToggleButton         _resumeClick;
  private JToggleButton         _autoPassClick;
  private JToggleButton         _lettersClick;

  private JToolBar              _frToolbar1;
  private JToolBar              _freeToolbar1;
  private JToolBar              _freeToolbar2;

  private JToolBar              _frToolbar2;

  private JToolBar              _exToolbar1;
  private JToolBar              _xpToolbar1;

  private JToolBar              _exToolbar2;

  private List<String>          _captureContractors = new ArrayList<String>();
  private boolean               _captureHome        = false;
  private boolean               _homeClicked        = false;

  private List<BufferedImage>   _lastImageList      = new ArrayList<>();

  private JToggleButton         _sendInternational;

  private int                   _pingTurn           = 1;

  private TrainManagementWindow _trainManagementWindow;

  private OCRB                  _ocrb;

  public MainFrame() throws HeadlessException {
    super();
    _settings = Settings.createSettings("mickey.properties");
    _commands = Settings.createSettings("mickey.commands");
    _stats = new Statistics();

    setupLogger();

    init();

    // KEYS TURNED OFF. NO NEED AT THIS POINT
    // KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MyKeyEventDispatcher());

    runSettingsListener();
  }

  private void setupLogger() {
    try {
      try {
        MyLogger.setup();
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Problems with creating the log files");
      }

      _scanner = new ScreenScanner(_settings);
      _mouse = new MouseRobot();
    } catch (AWTException e2) {
      LOGGER.log(Level.SEVERE, e2.getMessage());
      System.exit(ERROR);
    }
  }

  class MyCanvas extends JPanel {

    public MyCanvas() {
      setMaximumSize(new Dimension(1000, 150));
    }

    Image image = null;

    public void paint(Graphics g) {
      super.paint(g);
      if (image != null) {
        g.drawImage(image, 0, 0, null);
      }
    }

  }

  private void init() {
    try {
      _ocrb = new OCRB("g");
    } catch (IOException e2) {
      e2.printStackTrace();
    }

    setTitle(APP_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setAlwaysOnTop(true);
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
    _scanner.addHandler(handler);

    _autoRefreshClick = new JToggleButton("Auto refresh");
    _autoRefreshClick.setSelected(Boolean.parseBoolean(_commands.getProperty("autoRefresh", "false")));
    _autoRefreshClick.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        _commands.setProperty("autoRefresh", "" + _autoRefreshClick.isSelected());
        _commands.saveSettingsSorted();
      }
    });

    JToolBar mainToolbar1 = new JToolBar();
    JToolBar mainToolbar2 = new JToolBar();

    _frToolbar1 = new JToolBar();
    _freeToolbar1 = new JToolBar();
    _freeToolbar2 = new JToolBar();
    _frToolbar2 = new JToolBar();
    JLabel frLabel = new JLabel("Freight     ");
    _frToolbar1.add(frLabel);
    _frToolbar2.add(new JLabel("                  "));
    JLabel freeLabel = new JLabel("Free          ");
    _freeToolbar1.add(freeLabel);
    _freeToolbar2.add(new JLabel("                  "));
    _exToolbar1 = new JToolBar();
    _xpToolbar1 = new JToolBar();
    _exToolbar2 = new JToolBar();
    JLabel exLabel = new JLabel("Express  ");
    _exToolbar1.add(exLabel);
    JLabel exLabel2 = new JLabel("         ");
    _exToolbar2.add(exLabel2);

    JLabel xpLabel = new JLabel("XP  ");
    _xpToolbar1.add(xpLabel);

    Dimension d = new Dimension(55, 20);
    frLabel.setPreferredSize(d);
    freeLabel.setPreferredSize(d);
    exLabel.setPreferredSize(d);
    exLabel2.setPreferredSize(d);
    frLabel.setMinimumSize(d);
    freeLabel.setMinimumSize(d);
    exLabel.setMinimumSize(d);
    exLabel2.setMinimumSize(d);
    frLabel.setMaximumSize(d);
    freeLabel.setMaximumSize(d);
    exLabel.setMaximumSize(d);
    exLabel2.setMaximumSize(d);
    xpLabel.setMaximumSize(d);

    JPanel toolbars = new JPanel(new GridLayout(9, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);
    mainToolbar1.setFloatable(false);
    mainToolbar2.setFloatable(false);
    _frToolbar1.setFloatable(false);
    _freeToolbar1.setFloatable(false);
    _freeToolbar2.setFloatable(false);
    _frToolbar2.setFloatable(false);
    _exToolbar1.setFloatable(false);
    _exToolbar2.setFloatable(false);
    _xpToolbar1.setFloatable(false);
    _frToolbar1.setBackground(new Color(201, 177, 133));
    _freeToolbar1.setBackground(new Color(201, 177, 183));// TODO
    _freeToolbar2.setBackground(new Color(201, 177, 183));// TODO
    _frToolbar2.setBackground(new Color(201, 177, 133));
    _exToolbar1.setBackground(new Color(153, 173, 209));
    _exToolbar2.setBackground(new Color(153, 173, 209));
    _xpToolbar1.setBackground(new Color(233, 193, 189));
    toolbars.add(_frToolbar1);
    toolbars.add(_frToolbar2);
    toolbars.add(_freeToolbar1);
    toolbars.add(_freeToolbar2);
    toolbars.add(_exToolbar1);
    toolbars.add(_exToolbar2);
    toolbars.add(_xpToolbar1);
    Box north = Box.createVerticalBox();
    north.add(toolbars);

    JLabel trainsNumberLabel = new JLabel("T:");
    trainsNumberLabel.setForeground(Color.GRAY);
    _trainsNumberLabel = new JLabel("888");
    _trainsNumberLabelA = new JLabel("888");
    _trainsNumberLabelA.setForeground(Color.GRAY);
    trainsNumberLabel.setFont(trainsNumberLabel.getFont().deriveFont(14.0f));
    _trainsNumberLabel.setFont(_trainsNumberLabel.getFont().deriveFont(14.0f));
    _trainsNumberLabelA.setFont(_trainsNumberLabelA.getFont().deriveFont(10.0f));

    JLabel freightTrainsNumberLabel = new JLabel("F:");
    freightTrainsNumberLabel.setForeground(Color.GRAY);
    _freightTrainsNumberLabel = new JLabel("888");
    _freightTrainsNumberLabelA = new JLabel("888");
    _freightTrainsNumberLabelA.setForeground(Color.GRAY);
    freightTrainsNumberLabel.setFont(freightTrainsNumberLabel.getFont().deriveFont(14.0f));
    _freightTrainsNumberLabel.setFont(_freightTrainsNumberLabel.getFont().deriveFont(14.0f));
    _freightTrainsNumberLabelA.setFont(_freightTrainsNumberLabelA.getFont().deriveFont(10.0f));

    JLabel expressTrainsNumberLabel = new JLabel("E:");
    expressTrainsNumberLabel.setForeground(Color.GRAY);
    _expressTrainsNumberLabel = new JLabel("888");
    _expressTrainsNumberLabelA = new JLabel("888");
    _expressTrainsNumberLabelA.setForeground(Color.GRAY);
    expressTrainsNumberLabel.setFont(expressTrainsNumberLabel.getFont().deriveFont(14.0f));
    _expressTrainsNumberLabel.setFont(_expressTrainsNumberLabel.getFont().deriveFont(14.0f));
    _expressTrainsNumberLabelA.setFont(_expressTrainsNumberLabelA.getFont().deriveFont(10.0f));

    JLabel refreshNumberLabel = new JLabel("R:");
    refreshNumberLabel.setForeground(Color.GRAY);
    refreshNumberLabel.setFont(refreshNumberLabel.getFont().deriveFont(14.0f));
    _refreshNumberLabel = new JLabel("" + (_autoRefreshClick.isSelected() ? "88" : "off"));
    _refreshMNumberLabel = new JLabel("" + (_autoRefreshClick.isSelected() ? "88" : "off"));
    _refreshTNumberLabel = new JLabel("" + (_autoRefreshClick.isSelected() ? "88" : "off"));
    _refreshSNumberLabel = new JLabel("" + (_autoRefreshClick.isSelected() ? "88" : "off"));
    _refreshNumberLabelA = new JLabel("88");
    _refreshNumberLabelA.setForeground(Color.GRAY);
    _refreshNumberLabel.setFont(_refreshNumberLabel.getFont().deriveFont(14.0f));
    _refreshMNumberLabel.setFont(_refreshMNumberLabel.getFont().deriveFont(14.0f));
    _refreshTNumberLabel.setFont(_refreshTNumberLabel.getFont().deriveFont(14.0f));
    _refreshSNumberLabel.setFont(_refreshSNumberLabel.getFont().deriveFont(14.0f));
    _refreshNumberLabelA.setFont(_refreshNumberLabelA.getFont().deriveFont(10.0f));

    JLabel lastActivityLabel = new JLabel("L:");
    lastActivityLabel.setForeground(Color.GRAY);
    lastActivityLabel.setFont(lastActivityLabel.getFont().deriveFont(14.0f));
    _lastActivityLabel = new JLabel(" 88:88 ");
    _lastActivityLabel.setFont(_lastActivityLabel.getFont().deriveFont(14.0f));

    JLabel startedLabel = new JLabel("S:");
    startedLabel.setForeground(Color.GRAY);
    startedLabel.setFont(startedLabel.getFont().deriveFont(14.0f));
    _startedLabel = new JLabel(" 88:88 ");
    _startedLabel.setFont(_startedLabel.getFont().deriveFont(14.0f));

    JPanel labelsBox = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.ipadx = 5;
    gbc.ipady = 5;
    gbc.insets = new Insets(0, 4, 0, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LAST_LINE_START;
    gbc.weightx = 0.0;
    labelsBox.add(trainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_trainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_trainsNumberLabelA, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(freightTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_freightTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_freightTrainsNumberLabelA, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(expressTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_expressTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_expressTrainsNumberLabelA, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.weightx = 1.0;
    labelsBox.add(new JLabel(" "), gbc);

    // second row
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.insets = new Insets(0, 4, 0, 0);
    gbc.weightx = 0.0;
    labelsBox.add(refreshNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_refreshNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_refreshNumberLabelA, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(lastActivityLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_lastActivityLabel, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    labelsBox.add(startedLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_startedLabel, gbc);

    // //////////////////////////

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.insets = new Insets(0, 4, 0, 0);
    gbc.weightx = 0.0;
    labelsBox.add(new JLabel("M:"), gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_refreshMNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(new JLabel(" "), gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(new JLabel("T:"), gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_refreshTNumberLabel, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    labelsBox.add(new JLabel("S:"), gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_refreshSNumberLabel, gbc);

    // //////////////////////////

    north.add(labelsBox);
    rootPanel.add(north, BorderLayout.NORTH);

    // SCAN
    AbstractAction scanAction = new AbstractAction("Scan") {
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
    mainToolbar1.add(scanAction);

    // DO MAGIC
    {
      _doMagicAction = new JButton(new AbstractAction("DO MAGIC") {
        public void actionPerformed(ActionEvent e) {
          runMagic();
        }
      });
      mainToolbar1.add(_doMagicAction);
    }

    // REFRESH
    AbstractAction runAction = new AbstractAction("Refresh") {
      public void actionPerformed(ActionEvent e) {
        Thread myThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              refresh(false);
              runMagic();
            } catch (RobotInterruptedException e) {
              LOGGER.log(Level.SEVERE, "Interrupted by user6", e);
            }
          }
        });
        myThread.start();
      }
    };
    mainToolbar1.add(runAction);

    // LOCATE
    {
      _locateAction = new JButton(new AbstractAction("L") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                try {
                  locate();
                } catch (RobotInterruptedException e) {
                  LOGGER.log(Level.SEVERE, "Interrupted by user7", e);
                  e.printStackTrace();
                  _stopThread = true;
                }
              } catch (Exception e1) {
                LOGGER.log(Level.WARNING, e1.getMessage());
                e1.printStackTrace();
              }
            }

          });
          myThread.start();
        }
      });
      mainToolbar1.add(_locateAction);
    }

    // Train Sender
    {
      JButton b1 = new JButton(new AbstractAction("TS") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              scanTrains();
            }
          });
          myThread.start();
        }
      });
      mainToolbar1.add(b1);
    }
    // CAPTURE CONTRACTS 1
    {
      JButton b1 = new JButton(new AbstractAction("C1") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {

            @Override
            public void run() {
              try {
                resetContractors();
                if (!isRunning("MAGIC")) {
                  runMagic();
                }
              } catch (Exception e1) {
                LOGGER.log(Level.WARNING, e1.getMessage());
                e1.printStackTrace();
              }
            }

          });
          myThread.start();
        }
      });
      mainToolbar1.add(b1);
    }

    // // CAPTURE CONTRACTS 2
    // {
    // JButton b1 = new JButton(new AbstractAction("C2") {
    // public void actionPerformed(ActionEvent e) {
    // Thread myThread = new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // try {
    // resetContractors();
    // _scanMaterials2 = false;
    // if (!isRunning("MAGIC")) {
    // runMagic();
    // }
    // } catch (Exception e1) {
    // LOGGER.log(Level.WARNING, e1.getMessage());
    // e1.printStackTrace();
    // }
    // }
    //
    // });
    // myThread.start();
    // }
    // });
    // mainToolbar1.add(b1);
    // }
    // RESET
    {
      _resetAction = new JButton(new AbstractAction("Reset") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              _stats.reset();
              updateLabels();
            }

          });
          myThread.start();
        }
      });
      mainToolbar2.add(_resetAction);
    }

    // Refresh
    {
      mainToolbar2.add(_autoRefreshClick);
    }

    // Ping
    {
      _pingClick = new JToggleButton("Ping");
      _pingClick.setSelected(Boolean.parseBoolean(_commands.getProperty("ping", "true")));
      mainToolbar2.add(_pingClick);
      _pingClick.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("ping", "" + _pingClick.isSelected());
          _commands.saveSettingsSorted();
        }
      });
    }
    {
      _sendInternational = new JToggleButton("SI");
      _sendInternational.setSelected(true);
      mainToolbar2.add(_sendInternational);

      _sendInternational.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("sendInternational", "" + _sendInternational.isSelected());
          _commands.saveSettingsSorted();
        }
      });
    }
    // Resume
    {
      _resumeClick = new JToggleButton("R");
      _resumeClick.setSelected(_commands.getBoolean("resume", false));
      _resumeClick.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("resume", "" + _resumeClick.isSelected());
          _commands.saveSettingsSorted();
        }
      });
      mainToolbar2.add(_resumeClick);
    }
    {
      _autoPassClick = new JToggleButton("AP");
      _autoPassClick.setSelected(_commands.getBoolean("autoPassengers", false));
      _autoPassClick.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("autoPassengers", "" + _autoPassClick.isSelected());
          _commands.saveSettingsSorted();
        }
      });
      mainToolbar2.add(_autoPassClick);
    }
    {
      _lettersClick = new JToggleButton("L");
      _lettersClick.setSelected(_commands.getBoolean("huntLetters", false));
      _lettersClick.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("huntLetters", "" + _lettersClick.isSelected());
          _commands.saveSettingsSorted();
        }
      });
      mainToolbar2.add(_lettersClick);
    }

    // freight
    ButtonGroup bgFr = new ButtonGroup();
    createButtons(_frToolbar1, bgFr, Locations.LOC_PAGE_F1, "freight");
    createButtons(_frToolbar2, bgFr, Locations.LOC_PAGE_F2, "freight");

    // free
    ButtonGroup bgFree = new ButtonGroup();
    createButtons(_freeToolbar1, bgFree, Locations.LOC_PAGE_F1, "free");
    createButtons(_freeToolbar2, bgFree, Locations.LOC_PAGE_F2, "free");

    // express
    ButtonGroup bgEx = new ButtonGroup();
    createButtons(_exToolbar1, bgEx, Locations.LOC_PAGE_E1, "express");
    createButtons(_exToolbar2, bgEx, Locations.LOC_PAGE_E2, "express");
    // createButtons(_exToolbar2, bgEx, Locations.LOC_PAGE3, false);

    // XP
    ButtonGroup bgXP = new ButtonGroup();
    createButtons(_xpToolbar1, bgXP, Locations.LOC_PAGE_F1, "xp");

    ((JToggleButton) _frToolbar1.getComponent(3)).setSelected(true);
    ((JToggleButton) _freeToolbar1.getComponent(2)).setSelected(true);
    ((JToggleButton) _exToolbar1.getComponent(4)).setSelected(true);
    ((JToggleButton) _xpToolbar1.getComponent(2)).setSelected(true);

    /*
     * JToggleButton timeButton1 = new JToggleButton(new AbstractAction(" 6m ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_6MIN; _expressTime = Locations.LOC_6MIN; } }); bg.add(timeButton1);
     * toolbar2.add(timeButton1);
     * 
     * JToggleButton timeButton2 = new JToggleButton(new AbstractAction(" 10m ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_10MIN; _expressTime = Locations.LOC_10MIN;
     * 
     * } }); bg.add(timeButton2); toolbar2.add(timeButton2);
     * 
     * JToggleButton timeButton3 = new JToggleButton(new AbstractAction(" 30m ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_30MIN; _expressTime = Locations.LOC_30MIN;
     * 
     * } }); bg.add(timeButton3); toolbar2.add(timeButton3);
     * 
     * JToggleButton timeButton4 = new JToggleButton(new AbstractAction(" 1h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_1HOUR; _expressTime = Locations.LOC_1HOUR;
     * 
     * } }); bg.add(timeButton4); toolbar2.add(timeButton4);
     * 
     * JToggleButton timeButton5 = new JToggleButton(new AbstractAction(" 2h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_2HOURS; _expressTime = Locations.LOC_2HOURS; } });
     * bg.add(timeButton5); toolbar2.add(timeButton5);
     * 
     * { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 3h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_3HOURS; _expressTime = Locations.LOC_3HOURS; } });
     * bg.add(timeButton6); toolbar2.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 4h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_4HOURS; _expressTime = Locations.LOC_4HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 6h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_6HOURS; _expressTime = Locations.LOC_6HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 8h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_8HOURS; _expressTime = Locations.LOC_8HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 10h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_10HOURS; _expressTime = Locations.LOC_10HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 1d ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_1DAY; _expressTime = Locations.LOC_1DAY; } }); bg.add(timeButton6);
     * toolbar3.add(timeButton6); }
     */
    // timeButton2.setSelected(true);

    LOGGER.info("mandatory refresh time set to " + _settings.getInt("mandatoryRefresh.time", 45) + " minutes.");
    LOGGER.info("ping time set to " + _settings.getInt("ping.time", 2) + " minutes.");

  }

  private void runSettingsListener() {
    Thread settingsThread = new Thread(new Runnable() {
      public void run() {
        new Service().purgeAll();
        boolean stop = false;
        do {
          _settings.loadSettings();
          _commands.loadSettings();
          if ("close".equals(_commands.getProperty("command"))) {
            LOGGER.info("stop everything");
            stop = true;
          } else {
            // if (!isRunning("MAGIC") && _scanner.isOptimized()) {
            // try {
            // checkIsStuck();
            // } catch (GameStuckException e1) {
            // try {
            // refresh();
            // runMagic();
            // } catch (RobotInterruptedException e) {
            // }
            // }
            // }
            try {
              reapplySettings();
              // processCommands();
              processRequests();
            } catch (Throwable t) {
              // hmm
            }
            // if (System.currentTimeMillis() - _lastTime > 60 * 1000) {
            // final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            // _scanner.writeImage(new Rectangle(0, 0, screenSize.width, screenSize.height),
            // "screenshot_" + DateUtils.formatDateForFile2(System.currentTimeMillis()) + ".png");
            // }

            try {
              Thread.sleep(20000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            deleteOlder("screenshot", 5);

          }
        } while (!stop);
      }
    }, "SETTINGS");

    settingsThread.start();

  }

  private void reapplySettings() {
    LOGGER.info(".................");
    int free = _commands.getInt("free", -1);
    int freight = _commands.getInt("freight", -1);
    int express = _commands.getInt("express", -1);
    int xp = _commands.getInt("xp", -1);

    if (free >= 0) {
      reapplyTimes(free, _freeToolbar1.getComponents(), _freeToolbar2.getComponents());
    }

    if (freight >= 0) {
      reapplyTimes(freight, _frToolbar1.getComponents(), _frToolbar2.getComponents());
    }

    if (express >= 0) {
      reapplyTimes(express, _exToolbar1.getComponents(), _exToolbar2.getComponents());
    }

    if (xp >= 0) {
      reapplyTimes(xp, _xpToolbar1.getComponents(), null);
    }

    boolean ping = "true".equalsIgnoreCase(_commands.getProperty("ping"));
    boolean resume = "true".equalsIgnoreCase(_commands.getProperty("resume"));
    boolean autoPassengers = "true".equalsIgnoreCase(_commands.getProperty("autoPassengers"));
    boolean sendInternational = "true".equalsIgnoreCase(_commands.getProperty("sendInternational"));
    boolean autoRefresh = "true".equalsIgnoreCase(_commands.getProperty("autoRefresh"));
    boolean letters = "true".equalsIgnoreCase(_commands.getProperty("huntLetters"));

    if (ping != _pingClick.isSelected()) {
      _pingClick.setSelected(ping);
    }

    if (resume != _resumeClick.isSelected()) {
      _resumeClick.setSelected(resume);
    }

    if (autoPassengers != _autoPassClick.isSelected()) {
      _autoPassClick.setSelected(autoPassengers);
    }

    if (letters != _lettersClick.isSelected()) {
      _lettersClick.setSelected(letters);
    }

    if (sendInternational != _sendInternational.isSelected()) {
      _sendInternational.setSelected(sendInternational);
    }

    if (autoRefresh != _autoRefreshClick.isSelected()) {
      _autoRefreshClick.setSelected(autoRefresh);
    }
  }

  private void reapplyTimes(int time, Component[] components1, Component[] components2) {
    boolean found = false;
    for (int i = 0; i < components1.length && !found; i++) {
      if (components1[i] instanceof LocationToggleButton) {
        LocationToggleButton b = (LocationToggleButton) components1[i];
        if (b.getTrainLocation().getTime() == time) {
          if (!b.isSelected()) {
            b.doClick();
            b.invalidate();
          }
          found = true;
        }
      }
    }
    if (!found && components2 != null) {
      for (int i = 0; i < components2.length && !found; i++) {
        if (components2[i] instanceof LocationToggleButton) {
          LocationToggleButton b = (LocationToggleButton) components2[i];
          if (b.getTrainLocation().getTime() == time) {
            if (!b.isSelected()) {
              b.doClick();
              b.invalidate();
            }
            found = true;
          }
        }
      }
    }
  }

  private void processRequests() {
    Service service = new Service();

    String[] requests = service.getActiveRequests();
    for (String r : requests) {

      if (r.startsWith("capture")) {
        service.inProgress(r);
        String[] ss = r.split("_");
        if (ss.length > 2) {
          // _captureContractors.clear();
          _captureContractors.add(ss[1]);
          hook(r);
        } else {
          if (r.startsWith("captureAll")) {
            resetContractors();
            hook(r);
          } else if (r.startsWith("captureHome")) {
            _captureHome = true;
            hookHome(r);
          }

        }
      } else if (r.startsWith("click")) {
        service.inProgress(r);
        processClick(r);
      } else if (r.startsWith("refresh") || r.startsWith("r")) {
        service.inProgress(r);
        String[] ss = r.split("_");
        Boolean bookmark = ss.length > 1 ? Boolean.parseBoolean(ss[1]) : false;
        try {
          stopMagic();
          refresh(bookmark);
          runMagic();
        } catch (RobotInterruptedException e) {
        }
      } else if (r.startsWith("reload")) {
        service.inProgress(r);
        reload(r);
      } else if (r.startsWith("reset")) {
        service.inProgress(r);
        _stats.reset();
        updateLabels();
      }
    }

    service.purgeOld(1000 * 60 * 60);// 1 hour old
  }

  private void reload(String r) {
    reload();
  }

  private void reload() {
    if (_trainManagementWindow == null) {
      TrainScanner tscanner = new TrainScanner(_scanner, LOGGER, _settings.getInt("IntTrains.takeABreakAfter", 3));
      tscanner.setSettings(_settings);
      _trainManagementWindow = new TrainManagementWindow(null, tscanner);
    } else
      _trainManagementWindow.reload();
  }

  private void processClick(String r) {
    try {
      String[] ss = r.split("_");
      int x = Integer.parseInt(ss[1]);
      int y = Integer.parseInt(ss[2]);
      _mouse.click(x, y);
      try {
        _mouse.delay(1000);
      } catch (RobotInterruptedException e) {
      }
    } finally {
      new Service().done(r);
    }
  }

  private void hook(final String request) {
    Thread hookThread = new Thread(new Runnable() {
      public void run() {
        int n = 0;
        boolean done = false;
        do {
          n++;
          String[] ss = request.split("_");
          String c = null;
          if (ss.length > 2) {
            c = ss[1];
            LOGGER.info("checking (" + n + ") " + c);
          } else {
            c = ss[0];
          }
          if ((ss.length > 2 && !_captureContractors.contains(c)) || (ss.length <= 2 && _captureContractors.size() == 0)) {
            new Service().done(request);
            done = true;
            LOGGER.info(c + " is DONE!!!");
          }
          try {
            Thread.sleep(4000);
          } catch (InterruptedException e) {
          }
        } while (!done && n < 5 * 20);
        if (!done) {
          new Service().purgeAll();
        }
      }
    });
    hookThread.start();
  }

  private void hookHome(final String request) {
    Thread hookThread = new Thread(new Runnable() {
      public void run() {
        int n = 0;
        boolean done = false;
        do {
          n++;
          String[] ss = request.split("_");
          String c = null;
          if (ss.length > 2) {
            c = ss[1];
            LOGGER.info("checking (" + n + ") " + c);
          } else {
            c = ss[0];
          }
          if (!_captureHome) {
            new Service().done(request);
            done = true;
            LOGGER.info(c + " is DONE!!!");
          }
          try {
            Thread.sleep(4000);
          } catch (InterruptedException e) {
          }
        } while (!done && n < 5 * 20);
        if (!done) {
          new Service().purgeAll();
        }
      }
    });
    hookThread.start();
  }

  private void processCommands() {
    String command = _commands.getProperty("command");
    if ("refresh".equals(command)) {
      // try {
      // Thread.sleep(10000);
      // } catch (InterruptedException e1) {
      // }
      _commands.setProperty("command", command + "_ok");
      _commands.saveSettingsSorted();

      stopMagic();
      try {
        refresh(false);
        runMagic();
      } catch (RobotInterruptedException e) {
        e.printStackTrace();
      }

      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("start".equals(command)) {
      _commands.setProperty("command", command + "_ok");
      _commands.saveSettingsSorted();

      stopMagic();
      runMagic();

      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("stop".equals(command)) {
      _commands.setProperty("command", command + "_ok");
      _commands.saveSettingsSorted();

      stopMagic();

      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("contracts".equals(command)) {
      _commands.setProperty("command", command + "_ok");
      // _commands.saveSettingsSorted();
      resetContractors();
      if (!isRunning("MAGIC")) {
        runMagic();
      }
      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("contractor".equals(command)) {
      _commands.setProperty("command", command + "_ok");
      String contractor = _commands.getProperty("contractor");
      rescanContractor(contractor);
      if (!isRunning("MAGIC")) {
        runMagic();
      }
      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    }
  }

  private void resetContractors() {
    try {
      _captureHome = true;
      _captureContractors = new DataStore().getContractorNamesForScan();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void stopMagic() {
    _stopThread = true;
    int tries = 10;
    boolean stillRunning = true;
    for (int i = 0; i < tries && stillRunning; ++i) {
      stillRunning = isRunning("MAGIC");
      if (stillRunning) {
        LOGGER.info("Magic still working...");
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
      }
    }
    _stopThread = false;
  }

  private void createButtons(final JToolBar toolbar, final ButtonGroup bg, final Location[] locations, final String type) {
    for (int i = 0; i < locations.length; i++) {
      final Location l = locations[i];
      LocationToggleButton button = new LocationToggleButton(l, new AbstractAction(l.getName()) {

        public void actionPerformed(ActionEvent e) {
          if (type.equals("free")) {
            _freeTime = l;
          } else if (type.equals("freight")) {
            _freightTime = l;

          } else if (type.equals("xp")) {
            _xpTime = l;
          } else {
            _expressTime = l;
          }
          LOGGER.info("selected " + type + ": " + l.getName());
          _commands.setProperty(type, "" + l.getTime());
          _commands.saveSettingsSorted();
        }
      });
      bg.add(button);
      toolbar.add(button);

    }
  }

  private class LocationToggleButton extends JToggleButton {
    private Location _location;

    public LocationToggleButton(Location location, Action a) {
      super(a);
      _location = location;
    }

    public Location getTrainLocation() {
      return _location;
    }

  }

  private void updateLabels() {
    _stats.updateTime();
    _trainsNumberLabel.setText("" + _stats.getTotalTrainCount());
    _freightTrainsNumberLabel.setText("" + _stats.getFreightTrainCount());
    _expressTrainsNumberLabel.setText("" + _stats.getExpressTrainCount());
    _refreshNumberLabel.setText("" + _stats.getRefreshCount());
    _refreshMNumberLabel.setText("" + _stats.getRefreshMandatoryCount());
    _refreshTNumberLabel.setText("" + _stats.getRefreshTimeoutCount());
    _refreshSNumberLabel.setText("" + _stats.getRefreshStuckCount());
    _lastActivityLabel.setText("" + _stats.getLastActivityTimeAsString());
    _startedLabel.setText("" + _stats.getStartedTimeAsString());

    _trainsNumberLabelA.setText(_stats.getAverageTrainTimeAsString());
    _freightTrainsNumberLabelA.setText(_stats.getAverageFreightTimeAsString());
    _expressTrainsNumberLabelA.setText(_stats.getAverageExpressTimeAsString());
    _refreshNumberLabelA.setText(_stats.getAverageRefreshTimeAsString());
  }

  protected void runMagic() {
    Thread myThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          if (_scanner.isOptimized()) {
            if (_sendInternational.isSelected()) {
              reload();
            }
            LOGGER.info("Let's get rolling...");
            Thread.sleep(200);
            doMagic();
          } else
            LOGGER.info("Scan first!");
        } catch (Exception e1) {
          LOGGER.severe(e1.getMessage());
          e1.printStackTrace();
        }
      }
    }, "MAGIC");

    myThread.start();
  }

  private void scan() {
    try {
      LOGGER.info("Scanning...");

      boolean found = _scanner.locateGameArea();
      if (found) {
        LOGGER.info("GAME FOUND! MICKEY READY.");
        // fixTheGame();
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

  private void refresh(boolean bookmark) throws RobotInterruptedException {
    deleteOlder("refresh", 5);
    LOGGER.info("Time to refresh...");
    captureScreen("refresh ");
    try {
      Pixel p;
      if (!bookmark) {
        if (_scanner.isOptimized()) {
          p = _scanner.getBottomRight();
          p.y = _scanner.getTopLeft().y + 100;
          p.x = _scanner.getBottomRight().x + 4;
        } else {
          p = new Pixel(0, 110);
        }
        _mouse.click(p.x, p.y);
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_F5);
        robot.keyRelease(KeyEvent.VK_F5);
      } else {
        try {
          p = _scanner.generateImageData("tsFavicon2.bmp", 8, 7).findImage(new Rectangle(0, 30, 400, 200));
          _mouse.click(p.x, p.y);
        } catch (IOException e) {
        }
      }

      LOGGER.fine("Wait 10 seconds...");
      _mouse.delay(10000);
      boolean done = false;
      try {
        for (int i = 0; i < 17 && !done; i++) {
          LOGGER.info("after refresh recovery try " + (i + 1));
          handleRarePopups(false);

          // OTHER POPUPS
          handlePopups();// hmm
          _mouse.delay(1000);

          // LOCATE THE GAME
          if (_scanner.locateGameArea()) {
            LOGGER.info("Game located successfully!");
            done = true;
          } else {
            processRequests();
          }
          if (i > 8) {
            captureScreen("refresh trouble ");
          }
        }
      } catch (AWTException | IOException e) {
        LOGGER.info("whaaaat again?");
      } catch (InterruptedException e) {
        LOGGER.info("interrupted");
      } catch (SessionTimeOutException e) {
        LOGGER.info("Session time out. Stopping.");
      }
      if (done) {
        LOGGER.info("Refresh done");
        captureScreen("refresh done ");
      } else {
        LOGGER.info("Refresh failed");
        if (!bookmark) {
          LOGGER.info("Trying refresh through bookmark");
          refresh(true);
        }
      }
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private void deleteOlder(String prefix, int amountFiles) {
    File f = new File(".");
    File[] files = f.listFiles();
    List<File> targetFiles = new ArrayList<File>(6);
    int cnt = 0;
    for (File file : files) {
      if (!file.isDirectory() && file.getName().startsWith(prefix)) {
        targetFiles.add(file);
        cnt++;
      }
    }

    if (cnt > amountFiles) {
      // delete some files
      Collections.sort(targetFiles, new Comparator<File>() {
        public int compare(File o1, File o2) {
          if (o1.lastModified() > o2.lastModified())
            return 1;
          else if (o1.lastModified() < o2.lastModified())
            return -1;
          return 0;
        };
      });

      int c = cnt - 5;
      for (int i = 0; i < c; i++) {
        File fd = targetFiles.get(i);
        fd.delete();
      }
    }
  }

  private void handleRarePopups(boolean fast) throws InterruptedException, RobotInterruptedException {
    LOGGER.info("Checking for FB login and daily rewards...");
    _mouse.savePosition();
    _mouse.mouseMove(0, 0);
    int wait = 100;
    if (!fast) {
      wait = 1000;
      // FB LOGIN
      if (scanAndClick(_scanner.getLoginWIthFB(), null))
        _mouse.delay(2000);

      if (scanAndClick(_scanner.getLoginFB(), null))
        _mouse.delay(5000);
      else
        _mouse.delay(3000);
    }

    // INVITE
    if (scanAndClick(_scanner.getInvite(), null))
      _mouse.delay(wait);

    // DAILY
    if (scanAndClick(_scanner.getDailyRewards(), null))
      _mouse.delay(wait);

    // PROMO
    if (scanAndClick(_scanner.getPromoX(), null))
      _mouse.delay(wait);

    // SHOP X
    if (scanAndClick(_scanner.getShopX(), null))
      _mouse.delay(wait);

    _mouse.restorePosition();
  }

  private boolean scanAndClick(ImageData imageData, Rectangle area) {
    Pixel p = imageData.findImage(area);
    if (p != null) {
      _mouse.mouseMove(p);
      _mouse.click();
      LOGGER.info(imageData.getName() + " clicked.");
      return true;
    }
    return false;
  }

  private String getNow() {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm  dd MMM");
    String date = sdf.format(Calendar.getInstance().getTime());
    return date;
  }

  public void doMagic() {
    setTitle(APP_TITLE + " RUNNING");
    _lastPingTime = System.currentTimeMillis();

    _stopThread = false;
    _homeClicked = false;
    if (_autoRefreshClick.isSelected())
      updateLabels();

    long start = System.currentTimeMillis();
    long fstart = System.currentTimeMillis();

    int turn = 1;
    while (!_stopThread) {
      turn *= 2;
      if (turn > 8)
        turn = 1;
      LOGGER.fine("T: " + turn);

      try {
        if (_autoPassClick.isSelected()) {
          try {
            scanPassengers();
          } catch (Throwable t) {
            LOGGER.info("DAMN IT! " + t.getMessage());
            LOGGER.info(t.toString());
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
            t.printStackTrace();
          }
        }

        int timeForRefresh = (getShortestTime() * 60000 / 2);
        int mandatoryRefresh = _settings.getInt("mandatoryRefresh.time", 45) * 60000;

        updateLabels();

        goHomeIfNeeded();

        // antistuck prevention
        checkIsStuck();

        // OTHER LOCATIONS
        boolean flag = scanOtherLocations(1);

        captureContracts();

        if (_captureContractors.size() == 0) {
          if (_sendInternational.isSelected())
            sendInternational();

          // scanOtherLocations(true, 2);

          if (_pingClick.isSelected()) {
            if (ping())
              scanOtherLocations(3);
          }

          // REFRESH
          if (_autoRefreshClick.isSelected() && timeForRefresh > 3 * 60000) {
            // if "0" chosen no refresh
            long now = System.currentTimeMillis();
            LOGGER.info("time: " + DateUtils.fancyTime2(now - start));

            if (now - start >= timeForRefresh) {
              LOGGER.info("Warning: no trains for last " + DateUtils.fancyTime2(now - start));
              refresh(false);
              _stats.registerTimeOutRefresh();
              updateLabels();
              fstart = start = System.currentTimeMillis();
            }

            if (mandatoryRefresh > 0 && now - fstart >= mandatoryRefresh) {
              LOGGER.info("Mandatory refresh");
              refresh(false);
              _stats.registerMandatoryRefresh();
              updateLabels();

              fstart = start = System.currentTimeMillis();
            }

            //
            // No need since checkIsStuck works fine
            //
            // // check again has refresh gone well after 4 minutes
            // if (_lastTime != null && now - _lastTime >= 4 * 60 * 1000) {
            // handleRarePopups();
            // _lastTime = System.currentTimeMillis();
            // }
            //
          }

          // POPUPS
          handlePopups();
          goHomeIfNeeded();
          // scanOtherLocations(true, 4);

          // HOME
          flag = clickHomeFaster();

          if (flag) {
            // true means train has been sent or other locations've been visited. Refresh postponed.
            start = System.currentTimeMillis();
          } else {
            // lookForPackages();
          }

          // LETTERS
          // int h = _settings.getInt("huntLetters", 2);
          if (_lettersClick.isSelected())
            huntLetters();

          // PACKAGES
          int hp = _settings.getInt("huntPackages", 1);
          int n = 16;
          for (int i = 0; i < hp; i++) {
            n = n / 2;
          }

          if (turn % n == 0)
            lookForPackages2();

          _mouse.delay(200);
        } // _captureContractors == 0
      } catch (AWTException | IOException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
        break;
      } catch (RobotInterruptedException e) {
        LOGGER.log(Level.SEVERE, "Interrupted by user", e);
        // LOGGER.info("Interrupted by user");
        setTitle(APP_TITLE + " READY");
        _stopThread = true;
        break;
      } catch (SessionTimeOutException e) {
        LOGGER.info("Session time out. Stopping...");
        setTitle(APP_TITLE + " READY");
        _stopThread = true;
        break;
      } catch (GameStuckException e) {
        _stopThread = true;
        new Thread(new Runnable() {
          public void run() {
            try {
              _stats.registerStuckRefresh();
              updateLabels();
              Thread.sleep(2000);
              refresh(false);
              runMagic();
            } catch (RobotInterruptedException e) {
            } catch (InterruptedException e) {
            }
          }
        }).start();
        break;
      } catch (Throwable e) {
        LOGGER.severe("SOMETHING WENT WRONG!");
        e.printStackTrace();
        LOGGER.log(Level.SEVERE, "UH OH " + e.getMessage() + " UH OH", e);
        setTitle("SOMETHING WENT WRONG!");
        // break; //please don't stop the music!
      }

    } // while
    if (_stopThread) {
      LOGGER.info("Mickey has being stopped");
    }
  }

  private void scanPassengers() throws AWTException, IOException {
    Rectangle area = _scanner.getPassengersArea();
    Robot robot = new Robot();
    BufferedImage screen = robot.createScreenCapture(area);
    // try {
    // ImageIO.write(screen, "PNG", new File("passengers.png"));
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    String pass = _ocrb.scanImage(screen);
    if (pass != null && pass.length() > 0) {
      Long passNumber = Long.parseLong(pass);
      int min = _commands.getInt("autoPassengers.min", 1000000);
      int max = _commands.getInt("autoPassengers.max", 4000000);
      int normal = _commands.getInt("express.default", 30);
      int expressMin = _commands.getInt("express.min", 10);
      int expressMax = _commands.getInt("express.max", 60);
      LOGGER.info("passengers: " + pass);
      LOGGER.info("min: " + min + "  max: " + max);
      if (passNumber < min) {
        // slow down trains
        if (_expressTime.getTime() != expressMax) {
          LOGGER.info("Passengers get under " + min);
          LOGGER.info("Slowing down express trains to " + expressMax);
          reapplyTimes(expressMax, _exToolbar1.getComponents(), _exToolbar2.getComponents());
        }
      } else {
        if (passNumber > max) {
          // speed up trains
          if (_expressTime.getTime() != expressMin) {
            LOGGER.info("Passengers get over " + max);
            LOGGER.info("Speeding up express trains to " + expressMin);
            reapplyTimes(expressMin, _exToolbar1.getComponents(), _exToolbar2.getComponents());
          }
        } else {
          // it is in the middle
          if (_expressTime.getTime() != normal) {
            LOGGER.info("Passengers between " + min + " and " + max);
            LOGGER.info("Setting trains to " + normal);
            reapplyTimes(normal, _exToolbar1.getComponents(), _exToolbar2.getComponents());
          }
        }
      }
    }
  }

  private void checkIsStuck() throws GameStuckException {
    boolean check = "true".equalsIgnoreCase(_settings.getProperty("checkIsStuck", "true"));
    if (check) {
      LOGGER.info("checking is game stuck...");
      int howMuch = 3;
      putNewImage(howMuch);

      if (_lastImageList.size() >= howMuch) {
        ImageComparator comparator = new SimilarityImageComparator(0.01, 1000);
        boolean uhoh = comparator.compare(_lastImageList.get(0), _lastImageList.get(1));
        for (int i = 1; i < howMuch - 1; ++i) {
          uhoh = uhoh && comparator.compare(_lastImageList.get(i), _lastImageList.get(i + 1));
        }

        if (uhoh) {
          try {
            handleRarePopups(true);
            _mouse.delay(200);
            howMuch = 4;
            putNewImage(howMuch);
          } catch (InterruptedException e) {
          } catch (RobotInterruptedException e) {
          }
        }

        // check again
        uhoh = comparator.compare(_lastImageList.get(0), _lastImageList.get(1));
        for (int i = 1; i < howMuch - 1; ++i) {
          uhoh = uhoh && comparator.compare(_lastImageList.get(i), _lastImageList.get(i + 1));
        }

        if (uhoh) {
          _lastImageList.clear();
          LOGGER.severe("THE GAME PROBABLY GOT STUCK");
          throw new GameStuckException();
        }
      }
    }
  }

  private void putNewImage(int howMuch) {
    try {
      Pixel tl = _scanner.getTopLeft();
      // int w = (_scanner.getGameWidth() - 214) / 2;
      // Rectangle rect = new Rectangle(tl.x + 33, tl.y + 7, 104, 15);
      Rectangle rect = new Rectangle(tl.x + 61, tl.y + 13, 50, 40);

      if (_lastImageList.size() < howMuch) {
        BufferedImage image = new Robot().createScreenCapture(rect);
        _lastImageList.add(image);
      } else if (_lastImageList.size() >= howMuch) {
        _lastImageList.remove(0);
        BufferedImage image = new Robot().createScreenCapture(rect);
        _lastImageList.add(image);
      }
    } catch (AWTException e) {
    }
  }

  private boolean ping() {
    boolean res = false;
    long now = System.currentTimeMillis();
    long time = _settings.getInt("ping.time", 2) * 60000; // from minutes to millseconds
    String pingPrefix = "ping ";
    int xx = (_scanner.getGameWidth() - 780) / 2;
    int yy = (_scanner.getGameHeight() - 585) / 2;
    xx += _scanner.getTopLeft().x;
    yy += _scanner.getTopLeft().y;

    if (now - _lastPingTime >= time) {
      LOGGER.info("ping");
      if (_pingTurn == 2) {
        try {
          // capture trains
          pingPrefix += " trains ";
          _mouse.click(_scanner.getTopLeft().x + 54, _scanner.getBottomRight().y - 39);
          _mouse.delay(1300);
          _mouse.click(xx + 103, yy + 83);
          _mouse.delay(2300);
        } catch (RobotInterruptedException e) {
        }
      } else if (_pingTurn == 3) {
        try {
          // capture international trains
          pingPrefix += " int trains ";
          _mouse.click(_scanner.getTopLeft().x + 54, _scanner.getBottomRight().y - 39);
          _mouse.delay(1300);
          _mouse.click(xx + 209, yy + 83);
          _mouse.delay(2300);
        } catch (RobotInterruptedException e) {
        }
      }

      captureScreen(pingPrefix);

      if (_pingTurn == 2 || _pingTurn == 3) {
        try {
          // need to close trains window
          _mouse.click(xx + 764, yy + 22);
          _mouse.delay(500);
          res = true;
        } catch (RobotInterruptedException e) {
        }
      }

      _pingTurn++;
      if (_pingTurn >= 4)
        _pingTurn = 1;

      deleteOlder("ping", 12);
      _lastPingTime = System.currentTimeMillis();
    }
    return res;
  }

  private void captureScreen(String filename) {
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    _scanner.writeImage(new Rectangle(0, 0, screenSize.width, screenSize.height), filename + DateUtils.formatDateForFile2(System.currentTimeMillis())
        + ".png");
  }

  private void captureHome() throws RobotInterruptedException, AWTException, IOException, SessionTimeOutException {
    handlePopups();
    goHomeIfNeeded();

    Pixel p = _scanner.getGoldIcon().findImage();
    if (p != null) {
      Rectangle rect = new Rectangle(p.x, p.y, 100, 13);
      _scanner.writeImage(rect, "data/Home_gold.bmp");
    }

    p = _scanner.getMaterialsButton().findImage();
    if (p != null) {
      _mouse.click(p.x, p.y);
      _mouse.delay(1000);

      Pixel pm = null;
      int tries = 0;
      do {
        pm = _scanner.getMaterials().findImage();
        if (pm == null) {
          // try again after 1 second
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
          }
        }
        tries++;
      } while (pm == null && tries < 3);

      if (pm != null) {
        pm = new Pixel(pm.x - 40, pm.y - 28);
        Rectangle rect = new Rectangle(pm.x, pm.y, 780, 585);
        _scanner.writeImage(rect, "data/Home_materials.bmp");
        _mouse.delay(300);
      }

      // click close
      scanAndClick(_scanner.getShopX(), null);
      _mouse.delay(200);
      _captureHome = false;
    }
  }

  private void sendInternational() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
    long timeLeft = _trainManagementWindow != null ? _trainManagementWindow.getTimeLeft() - System.currentTimeMillis() : 10000000;
    LOGGER.info("INTERNATIONAL " + DateUtils.fancyTime2(timeLeft));
    if (_trainManagementWindow != null && timeLeft <= 0) {
      handlePopups();

      _trainManagementWindow.reloadNow();
      boolean atLeastOneSent = _trainManagementWindow.sendTrainsNow();// in this thread please
      if (atLeastOneSent)
        _trainManagementWindow.reschedule(_settings.getInt("IntTrains.rescheduleAgain", 30) * 1000);// 30 sec
      else
        _trainManagementWindow.reschedule(_settings.getInt("IntTrains.reschedule", 2 * 60) * 1000);
    }
  }

  private void captureContracts() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
    if (_captureHome) {
      captureHome();
    }

    if (_captureContractors.size() > 0) {
      String contractorName = _captureContractors.get(0);
      String fname = "data/" + contractorName;
      // String index = StringUtils.leftPad(ss[0], 2, "0");
      handlePopups();

      // open contracts
      _mouse.click(_scanner.getTopLeft().x + 352, _scanner.getBottomRight().y - 38);
      _mouse.delay(1000);

      Pixel p = _scanner.getContracts().findImage();
      Rectangle area;

      if (p != null) {
        // we're on the right track
        _commands.setProperty("contractor.scan", "running");
        _commands.saveSettingsSorted();

        Pixel scrollerTop = new Pixel(p.x + 106, p.y + 40);
        // _mouse.mouseMove(scrollerTop);
        // _mouse.click();
        // _mouse.delay(300);

        int track = 500;
        area = new Rectangle(p.x, p.y + 31, 100, 484);
        String cname = contractorName + ".bmp";
        boolean found = false;
        if (findAndClick(cname, area, 15, 7, true, false)) {
          found = true;
        } else {
          _mouse.mouseMove(scrollerTop);
          _mouse.click();
          _mouse.delay(300);
        }

        if (findAndClick(cname, area, 15, 7, true, false)) {
          found = true;
        } else {
          _mouse.mouseMove(scrollerTop);
          _mouse.click();
          _mouse.delay(300);

          // drag until get it
          int drag = 0;
          found = false;
          do {
            LOGGER.info("drag from " + drag + " to " + (drag + 75));
            _mouse.drag(scrollerTop.x, scrollerTop.y + drag, scrollerTop.x, scrollerTop.y + drag + 45);
            found = findAndClick(cname, area, 15, 7, true, false);
            drag += 50;
          } while (!found && drag < track);
        }
        if (found) {
          _mouse.delay(500);
          // _scanner.writeImage(new Rectangle(p.x - 38, p.y - 28, 780, 585), contractor + "_mission.bmp");
          _scanner.writeImage(new Rectangle(p.x - 38 + 376, p.y - 28 + 388, 71, 21), fname + "_missionNumber.bmp");
          _scanner.writeImage(new Rectangle(p.x - 38 + 456, p.y - 28 + 60 + 93, 291, 262), fname + "_objectives.bmp");

          // _scanner.captureGame("status " + index + "A " + contractor + ".bmp");
          _mouse.delay(100);

          // click visit
          _mouse.click(p.x + 267, p.y + 335);
          _mouse.delay(200);
          _mouse.click(p.x + 267, p.y + 335 + 154);
          _mouse.delay(2000);

          Pixel p2 = null;
          int tries = 0;
          do {
            tries++;
            p2 = _scanner.getGoldIcon().findImage();
            LOGGER.info("Looking for gold: " + p2);
            _mouse.delay(2000);
          } while (p2 == null && tries < 3);

          if (p2 != null) {
            LOGGER.info("found it: " + p2);
            Rectangle rect = new Rectangle(p2.x, p2.y, 100, 13);
            _scanner.writeImage(rect, fname + "_gold.bmp");
          }

          // click materials
          p2 = _scanner.getMaterialsButton().findImage();
          if (p2 != null) {
            _mouse.click(p2.x, p2.y);
            _mouse.delay(1000);

            Pixel pm = null;
            tries = 0;
            do {
              pm = _scanner.getMaterials().findImage();
              if (pm == null) {
                // try again after 1 second
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
              }
              tries++;
            } while (pm == null && tries < 3);

            if (pm != null) {
              pm = new Pixel(pm.x - 40, pm.y - 28);
              Rectangle rect = new Rectangle(pm.x, pm.y, 780, 585);
              _scanner.writeImage(rect, fname + "_materials.bmp");
              _mouse.delay(300);
            }

            // click close
            scanAndClick(_scanner.getShopX(), null);
            _mouse.delay(200);

            goHomeIfNeeded();
            _mouse.delay(300);
            handlePopups();
          }
        } else {
          LOGGER.info("Couldn't find " + contractorName);
        }
        _captureContractors.remove(0);
      }

      // _commands.setProperty("contractor.scan", "done");
      // _commands.saveSettingsSorted();
    } else {
      // all contractors captured
      // processContractors(_contractors);
    }
  }

  private void goHomeIfNeeded() throws AWTException, IOException, RobotInterruptedException {
    Rectangle area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y - 85, 100, 85);
    Pixel p = _scanner.getHome().findImage(area);
    if (p != null && !_homeClicked) {
      _homeClicked = true;
      new Thread(new Runnable() {
        public void run() {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          _homeClicked = false;
        }
      }).start();

      goHome();
      // _mouse.click(p.x, p.y);
    }
  }

  private int getShortestTime() {
    return _freightTime.getTime() < _expressTime.getTime() ? _freightTime.getTime() : _expressTime.getTime();
  }

  private boolean findAndClick(String imageName, Rectangle area, int xOff, int yOff, boolean click) throws AWTException, IOException,
      RobotInterruptedException {
    return findAndClick(imageName, area, xOff, yOff, click, false);
  }

  private boolean findAndClick(String imageName, Rectangle area, int xOff, int yOff, boolean click, boolean capture) throws AWTException,
      IOException, RobotInterruptedException {

    // FOR DEBUG ONLY _scanner.writeImage2(area, "area");
    Pixel p = _scanner.locateImageCoords(imageName, new Rectangle[] { area }, xOff, yOff);
    if (p != null) {
      LOGGER.fine("Found pointer " + p);
      LOGGER.fine("Found pointer " + imageName);
      _mouse.mouseMove(p);
      _mouse.delay(100);
      if (capture) {
        _scanner.captureGame();
        deleteOlder("popup", 5);
      }
      if (click) {
        _mouse.click();
        _mouse.delay(100);
      }
      return true;
    }
    return false;
  }

  private void goHome() throws AWTException, IOException, RobotInterruptedException {
    _mouse.saveCurrentPosition();
    _mouse.click(_scanner.getTopLeft().x + 54, _scanner.getBottomRight().y - 38);
    _mouse.delay(500, false);
    // int diff = 60;
    // if (!_lastDiffs.isEmpty()) {
    // diff = _lastDiffs.toArray(new Integer[0])[_lastDiffs.size() - 1];
    // }

    /*
     * no drag now int x1 = _scanner.getBottomRight().x - 50; int y = _scanner.getBottomRight().y - 160; LOGGER.fine("drag home: " + diff);
     * _mouse.drag(x1, y, x1 - diff, y);
     */
  }

  private void handlePopups() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
    long t1 = System.currentTimeMillis();
    long t2;
    // LOGGER.info("Scanning for popups...");

    _mouse.mouseMove(_scanner.getBottomRight());

    // first scan popups that need to be closed
    Rectangle area;

    // NO BUTTON
    boolean found = scanAndClick(_scanner.getNoButton(), null);
    // //long t2 = System.currentTimeMillis();
    // //LOGGER.info("> handle No: " + (t2 - t1));

    // //t1 = t2 = System.currentTimeMillis();
    // SESSION
    checkSession();
    // //t2 = System.currentTimeMillis();
    // //LOGGER.info("> handle Session: " + (t2 - t1));
    // //t1 = t2 = System.currentTimeMillis();

    // INVITE
    found = scanAndClick(_scanner.getInvite(), null);
    if (found) {
      LOGGER.info("found Invite popup...");
    }

    // DAILY REWARDS
    found = scanAndClick(_scanner.getDailyRewards(), null);

    // HOORREY!!
    found = scanAndClick(_scanner.getHooray(), null);

    if (found) {
      // check for FB Share popup
      try {
        LOGGER.info("found Daily Rewards. Sleep a while and look for FB share popup...");
        Thread.sleep(8000);
      } catch (InterruptedException e) {
      }
      found = scanAndClick(_scanner.getFBShare(), null);
      if (found) {
        try {
          LOGGER.info("Wait 1sec and relocate the game...");
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        _scanner.locateGameArea();
      }
    }

    // FB SHARE
    found = scanAndClick(_scanner.getFBShare(), null);
    if (found) {
      try {
        LOGGER.info("Wait 1sec and relocate the game...");
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      _scanner.locateGameArea();
    }

    // SHOP
    found = found || scanAndClick(_scanner.getShopX(), null);

    // PROMO
    found = found || scanAndClick(_scanner.getPromoX(), null);

    // CLOSE - no passengers
    int xx = (_scanner.getGameWidth() - 262) / 2;
    int yy = (_scanner.getGameHeight() - 280) / 2;
    area = new Rectangle(_scanner.getTopLeft().x + xx + 85, _scanner.getTopLeft().y + yy + 225, 90, 32);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE_IMAGE, area, 21, 6, true, true);

    // CLOSE button of train Management
    xx = (_scanner.getGameWidth() - 760) / 2;
    area = new Rectangle(_scanner.getTopLeft().x + xx + 100, _scanner.getBottomRight().y - 91, 78, 32);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE_IMAGE, area, 21, 6, true, true);

    xx = (_scanner.getGameWidth() - 277) / 2;

    // now check other popups that need to refresh the game
    area = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getTopLeft().y + 184, 200, 80);
    found = findAndClick("sync.bmp", area, 84, 256, true, true);
    // found = found || findAndClick(ScreenScanner.POINTER_CLOSE4_IMAGE, area, 23, 10, true, true);
    if (found) {
      LOGGER.info("Game out of sync! Refreshing...");
      // refresh(false);
      _stats.registerRefresh();
      updateLabels();
    }

    // CANCEL
    xx = (_scanner.getGameWidth() - 782) / 2;
    area = new Rectangle(_scanner.getTopLeft().x + xx + 227, _scanner.getBottomRight().y - 105, 100, 44);
    found = found || findAndClick(ScreenScanner.POINTER_CANCEL_IMAGE, area, 25, 7, true, true);
    if (found) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      // CLOSE button of train Management
      xx = (_scanner.getGameWidth() - 760) / 2;
      area = new Rectangle(_scanner.getTopLeft().x + xx + 100, _scanner.getBottomRight().y - 91, 78, 32);
      found = findAndClick(ScreenScanner.POINTER_CLOSE_IMAGE, area, 21, 6, true, true);
    }

    t2 = System.currentTimeMillis();
    LOGGER.info("POPUPS " + (t2 - t1));
  }

  private boolean scanOtherLocations(int number) throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException,
      DragFailureException {
    LOGGER.info("Locations... ");// + number
    Rectangle area = new Rectangle(_scanner.getTopLeft().x + 1, _scanner.getTopLeft().y + 85, 148, 28);
    if (findAndClick(ScreenScanner.POINTER_LOADING_IMAGE, area, 10, 10, true)) {
      _mouse.delay(200);
      LOGGER.fine("Going to location...");

      loadTrains();
      return true;
    }
    return false;
  }

  private void checkSession() throws SessionTimeOutException, RobotInterruptedException {
    if (_scanner.isOptimized()) {
      Pixel p = _scanner.getSessionTimeOut().findImage();
      if (p != null) {
        if (_resumeClick.isSelected()) {
          LOGGER.info("Session expired, but resume is ON.");
          int time = _settings.getInt("resume.time", 10);
          LOGGER.info("Waiting " + time + " minutes...");
          try {
            Thread.sleep(time * 60000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          refresh(false);
        } else {
          throw new SessionTimeOutException();
        }
      }
    }
  }

  private void lookForPackages2() throws RobotInterruptedException, AWTException, IOException, DragFailureException, SessionTimeOutException {
    LOGGER.info("Looking of mail packages...");
    int timeGiven = 2000; // 2 secs
    long start = System.currentTimeMillis();

    LOGGER.info("looking for pointer down...");

    Pixel p = null;
    boolean done = false;
    long curr = start;
    boolean moved = false;
    do {
      curr = System.currentTimeMillis();
      _mouse.saveCurrentPosition();

      moved = moveIfNecessary();
      Rectangle leftArea = new Rectangle(_scanner.getPackagesArea().x, _scanner.getPackagesArea().y, 36, _scanner.getPackagesArea().height);
      p = _scanner.getPointerDownR().findImage(leftArea);
      if (p == null) {
        p = detectPointerDown(_scanner.getPackagesArea());
      }
      if (p != null) {
        p.x = p.x + 2;
        p.y = _scanner.getBottomRight().y - _scanner.getStreet1Y() - 4 - 10;
        clickCareful(p, false, true);
        p.y = p.y + 10;
        clickCareful(p, false, true);
        done = true;
      }
    } while (!done && curr - start <= timeGiven && !_stopThread);

    _mouse.delay(200);

    if (moved) {
      if (!scanOtherLocations(33)) {
        // go to somewhere and go back just to reposition the game
        _mouse.click(_scanner.getBottomRight().x - 138, _scanner.getBottomRight().y - 40);
        _mouse.delay(1000);
        goHomeIfNeeded();
      }
      ;
    }
  }

  private Thread  _tmThread            = null;
  private boolean _trainManagementOpen = false;
  private boolean _clickingDone        = false;

  private boolean clickHomeFaster() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException, DragFailureException {
    boolean trainHasBeenSent = false;
    boolean hadOtherLocations = false;
    int timeGiven = _settings.getInt("clickHomeFaster.time", 4000);
    long start = System.currentTimeMillis();

    Pixel p = null;
    long curr = start;

    int maxTurns = _settings.getInt("clickHomeFaster.turns", 4) + 1;
    int turn = 1;
    int h = _settings.getInt("huntLetters", 2);
    int nn = (int) Math.pow(2, maxTurns + 1);
    int n = nn;
    for (int i = 0; i < h; i++)
      n = n / 2;
    do {
      LOGGER.info("turn " + turn++);
      curr = System.currentTimeMillis();
      _mouse.saveCurrentPosition();
      int xOff = _settings.getInt("xOff", 150);

      p = new Pixel(_scanner.getBottomRight().x - xOff, _scanner.getBottomRight().y - 100);

      int[] rails = _scanner.getRailsHome();
      _trainManagementOpen = false;
      _clickingDone = false;

      if (_tmThread == null || !_tmThread.isAlive()) {
        _tmThread = new Thread(new Runnable() {
          public void run() {
            int i = 0;
            for (; !_trainManagementOpen && !_clickingDone; i++) {//
              try {
                _mouse.delay(25, false);
              } catch (RobotInterruptedException e) {
              }
              Pixel tm = _scanner.getTrainManagementAnchor().findImage();
              _trainManagementOpen = tm != null;
            }
            LOGGER.fine("Checked TM " + i + " times: " + _trainManagementOpen + " " + _clickingDone);
          }
        }, "TRAIN_MAN");
        _tmThread.start();
      }
      for (int i = rails.length - 1; !_trainManagementOpen && i >= 0; i--) {
        p.y = _scanner.getBottomRight().y - rails[i] - 4;
        clickCareful(p, false, false);
        _mouse.checkUserMovement();
      }
      for (int i = 0; !_trainManagementOpen && i < rails.length; i++) {
        p.y = _scanner.getBottomRight().y - rails[i] - 4;
        clickCareful(p, false, false);
        _mouse.checkUserMovement();
      }
      _clickingDone = true;
      _mouse.saveCurrentPosition();// ???

      if (!_trainManagementOpen)
        _mouse.delay(150);
      trainHasBeenSent = checkTrainManagement();
      if (trainHasBeenSent) {
        // _mouse.delay(250);
        start = System.currentTimeMillis();
      }

      // LETTERS
      int t = (int) Math.pow(2, turn);
      if (t % n == 0) {
        huntLetters();
      }

      // LOCATIONS
      if (turn % _settings.getInt("checkLocations", 2) == 0)
        hadOtherLocations = scanOtherLocations(11);

      // SHOP POPUP CHECK
      if ((turn + 1) % _settings.getInt("checkShop", 2) == 0)
        scanAndClick(_scanner.getShopX(), null);

    } while (curr - start <= timeGiven && !_stopThread && turn < maxTurns);

    return trainHasBeenSent || hadOtherLocations;
  }

  private void huntLetters() throws RobotInterruptedException, AWTException {
    LOGGER.info("Scanning for letters...");
    Rectangle letterArea = _scanner.getLetterArea();
    long start = System.currentTimeMillis();
    boolean success = false;
    do {
      BufferedImage image1 = new Robot().createScreenCapture(letterArea);
      _mouse.delay(170, true);
      BufferedImage image2 = new Robot().createScreenCapture(letterArea);

      List<Blob> blobs = new MotionDetector(_settings).detect(image1, image2);
      // LOGGER.info("blobs found: " + blobs.size());
      // System.err.println(blobs);
      if (blobs.size() > 0) {
        // we have movement
        for (Blob blob : blobs) {
          IntPoint c = blob.getCenter();
          Pixel p = new Pixel(letterArea.x + c.y, letterArea.y + c.x);
          // LOGGER.info("COORDS: " + p);
          _stats.registerRedLetter();
          clickLetter(p);
          // registerBlob(blob, image1, image2);
        }
        success = true;
        // _mouse.delay(500);

      }

    } while (!success && System.currentTimeMillis() - start < 5500);
    // }

    // for (BlobInfo blobInfo : _blobs) {
    // //FastBitmap fb1 = new FastBitmap(blobInfo.image1);
    // FastBitmap fb2 = new FastBitmap(blobInfo.image2);
    // fb2.saveAsPNG("blob_" + blobInfo.blob.getCenter().y + "_" + blobInfo.blob.getCenter().x + "_" + System.currentTimeMillis()+".png");
    // }

  }

  static class BlobInfo {
    Blob          blob;
    BufferedImage image1;
    BufferedImage image2;

    public BlobInfo(Blob blob, BufferedImage image1, BufferedImage image2) {
      super();
      this.blob = blob;
      this.image1 = image1;
      this.image2 = image2;
    }

  }

  private List<BlobInfo> _blobs = new ArrayList<MainFrame.BlobInfo>();

  private void registerBlob(Blob blob, BufferedImage image1, BufferedImage image2) {
    _blobs.add(new BlobInfo(blob, image1, image2));
  }

  private void clickLetter(Pixel p) throws RobotInterruptedException {
    int off = 2;
    int d = 10;
    if (p != null) {
      int y = _scanner.getBottomRight().y - p.y;
      if (y < 188) {
        // fast
        _mouse.click(p.x, p.y - off - 12);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 12);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 11);
        _mouse.delay(d);
        _mouse.checkUserMovement();
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 11);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 11);
      } else if (y < 230) {
        // mid
        _mouse.click(p.x, p.y - off - 9);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 9);
        _mouse.delay(d);
        _mouse.checkUserMovement();
        _mouse.click(p.x, p.y - off - 8);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 8);
      } else {
        // slow
        _mouse.click(p.x, p.y - off - 6);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 5);
        _mouse.delay(d);
        _mouse.checkUserMovement();
        _mouse.click(p.x, p.y - off - 4);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 3);
      }
      _mouse.checkUserMovement();
      LOGGER.info("Letters cought: " + _stats.getRedLetter());
      // LOGGER.info("found letter: " + p);
      _mouse.mouseMove(_scanner.getBottomRight());

    }
  }

  private boolean isRunning(String threadName) {
    boolean isRunning = false;
    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    for (Iterator<Thread> it = threadSet.iterator(); it.hasNext();) {
      Thread thread = it.next();
      if (thread.getName().equals(threadName)) {
        isRunning = true;
        break;
      }
    }
    return isRunning;
  }

  private void loadTrains() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException, DragFailureException {
    _mouse.delay(300);
    loadTrainsFast();
    goHomeIfNeeded();
  }

  private void loadTrainsFast() throws RobotInterruptedException {
    int[] rails = _scanner.getRailsOut();
    int xx = _scanner.getBottomRight().x - 80; // safe zone
    for (int i = 0; i < rails.length; i++) {
      _mouse.click(xx, _scanner.getBottomRight().y - rails[i] - 4);
    }
    for (int i = 0; i < rails.length; i++) {
      _mouse.click(xx, _scanner.getBottomRight().y - rails[i] - 4);
    }

    _mouse.delay(300);
    scanAndClick(_scanner.getNoButton(), null);

    // int diff = 30;
    // int x1 = _scanner.getBottomRight().x - 57;
    // int y = _scanner.getBottomRight().y - 160;
    // _mouse.drag(x1, y, x1 - diff, y);

    // xx = _scanner.getBottomRight().x - 140; // safe zone
    // for (int i = 0; i < rails.length; i++) {
    // _mouse.click(xx, _scanner.getBottomRight().y - rails[i] - 4);
    // }
  }

  protected void handleDragFailure() {
    LOGGER.severe("Drag failure...");
    LOGGER.severe("NO IDEA WHAT TO DO!!!");
    try {
      refresh(false);
      _stats.registerRefresh();
      updateLabels();

      Thread.sleep(20000);
      runMagic();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.log(Level.SEVERE, "Interrupted by user5", e);
      e.printStackTrace();
    }
  }

  private void locate() throws RobotInterruptedException, AWTException, IOException {

    Pixel p = _scanner.getTrainManagementAnchor().findImage();
    Rectangle defaultArea = _scanner.getTrainManagementAnchor().getDefaultArea();
    _scanner.writeImage(defaultArea, "defaultarea.bmp");
    if (p != null) {
      _mouse.savePosition();
      _mouse.mouseMove(p);
      // _mouse.click();
      _mouse.delay(2000);
      _mouse.restorePosition();
    }

    // Pixel p = new Pixel(_scanner.getBottomRight().x - 100, _scanner.getBottomRight().y - 140);
    // _mouse.mouseMove(p);
    //
    // _mouse.drag(p.x, p.y, p.x - 66, p.y);
    // _mouse.delay(2000);
    //
    // p.x = p.x - 66;
    // _mouse.mouseMove(p);

    try {

      // handleRarePopups();
      // ////////////////////////////handlePopups();

      // captureContractors(true);

      // Pixel p = _scanner.generateImageData("trainStationBookmarkFirefox.bmp", 23, 8).findImage(new Rectangle(0, 0, 400, 200));
      // Pixel p = _scanner.generateImageData("expressTrain3.bmp", 0, 0).findImage(new Rectangle(0, 0, 910, 220));

      // Pixel p = _scanner.getXPTrain().findImage(new Rectangle(_scanner.getTopLeft().x, _scanner.getTopLeft().y, 910, 220));
      // if (p != null) {
      // _mouse.savePosition();
      // _mouse.mouseMove(p);
      // //_mouse.click();
      // _mouse.delay(2000);
      // _mouse.restorePosition();
      // }

      // fixTheGame();
    } catch (Throwable e) {
      e.printStackTrace();
    }

    // goHomeIfNeeded();

    // Pixel p = _scanner.getPointerDown().findImage(_scanner.getTrainArea());
    // if (p != null) {
    // _mouse.mouseMove(p);
    // }

    // p = _scanner.getPointerRight().findImage();
    // if (p != null) {
    // _mouse.mouseMove(p);
    // }

    // try {
    // handleRarePopups();
    // } catch (InterruptedException e1) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // }

    /*
     * { Pixel p = _scanner.getLoginWIthFB().findImage(); if (p != null) _mouse.mouseMove(p); }
     */
    // {
    // Pixel p = _scanner.getDailyRewards().findImage();
    // if (p != null)
    // _mouse.mouseMove(p);
    // }
    // {
    // Pixel p = _scanner.getSessionTimeOut().findImage();
    // if (p != null)
    // _mouse.mouseMove(p);
    // }
    // {
    // Pixel tm = _scanner.getPointerTrainManagement().findImage();
    //
    // // (ScreenScanner.POINTER_TRAIN_MANAGEMENT_IMAGE, area,
    // // time.get_coordinates().x,
    // // time.get_coordinates().y, false);
    //
    // if (tm != null) {
    // // is it freight or express?
    // Rectangle area = new Rectangle(tm.x + 310, tm.y + 25, 43, 55);
    // Pixel exP = _scanner.getTopLeftImage().findImage(area);
    // _devMode = true;
    // drawImage(area);
    // if (exP != null) {
    // LOGGER.info("EXPRESSSSSSS");
    // }
    // _devMode = false;
    // }
    // }
    // moveIfNecessary();

    /*
     * try { loadTrains(); } catch (SessionTimeOutException e) { // TODO Auto-generated catch block e.printStackTrace(); }
     */

    // checkTrainManagement();
    /*
     * long start = System.currentTimeMillis(); Robot robot = new Robot(); long now = start; do { System.err.println("trying something");
     * robot.delay(200); Point position = MouseInfo.getPointerInfo().getLocation();
     * 
     * LOGGER.info("mouse: " + position);
     * 
     * now = System.currentTimeMillis(); } while (now - start <= 10000);
     */

    LOGGER.info("done");
  }

  private boolean moveIfNecessary() throws RobotInterruptedException {
    boolean moved = false;
    Pixel p = _scanner.getPointerLeft().findImage();
    if (p != null) {
      LOGGER.info("Found left arrow. moving a bit...");
      // _mouse.mouseMove(p);
      int x1 = _scanner.getTopLeft().x + 5;
      int y = _scanner.getBottomRight().y - Locations.RAIL1;
      _mouse.drag(x1, y, x1 + 520, y);
      // _mouse.delay(500);
      moved = true;
    } else {
      p = _scanner.getPointerRight().findImage();
      if (p != null) {
        LOGGER.info("Found right arrow. moving a bit...");
        // _mouse.mouseMove(p);
        int x1 = _scanner.getBottomRight().x - 5;
        int y = _scanner.getBottomRight().y - Locations.RAIL1;
        _mouse.drag(x1, y, x1 - 520, y);
        // _mouse.delay(500);
        moved = true;
      } else {
        ImageData pointerDown = _scanner.getPointerDownL();
        Rectangle area = new Rectangle(_scanner.getBottomRight().x - 26, _scanner.getBottomRight().y - Locations.RAIL1 - 150, 26, 150);
        p = findPointerDownInt(area, pointerDown, 4);
        if (p != null) {
          int x1 = _scanner.getBottomRight().x - 5;
          int y = _scanner.getBottomRight().y - Locations.RAIL1;
          _mouse.drag(x1, y, x1 - 30, y);
          moved = true;
        }
        if (p == null) {
          pointerDown = _scanner.getPointerDownR();
          area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y - Locations.RAIL1 - 150, 26, 150);
          p = findPointerDownInt(area, pointerDown, 4);
          if (p != null) {
            int x1 = _scanner.getTopLeft().x + 5;
            int y = _scanner.getBottomRight().y - Locations.RAIL1;
            _mouse.drag(x1, y, x1 + 30, y);
            moved = true;
          }
        }
      }
    }
    return moved;
  }

  private Pixel detectPointerDown(Rectangle area) throws RobotInterruptedException {
    ImageData pointerDown = _scanner.getPointerDown();
    Pixel p = pointerDown.findImage(area == null ? _scanner.getTrainArea() : area);
    if (p != null && p.x >= 2)
      p.x -= 2;// DOUBLE LOCO problem
    return p;
  }

  private Pixel detectLetter() throws RobotInterruptedException, AWTException, IOException {
    Rectangle area = _scanner.getLetterArea();
    Pixel p = null;
    for (int i = 0; i < 3 && p == null; i++) {
      for (int l = 1; l <= 8 && p == null; l++) {
        ImageData letter = _scanner.generateLetterImageData(l);
        p = letter.findImage(area);
        _mouse.checkUserMovement();
      }
      // _mouse.checkUserMovement();
    }
    if (p != null) {
      _stats.registerRedLetter();
      return p;
    }

    return null;
  }

  private Pixel findPointerDownInt(Rectangle area, ImageData pointerDown, int railNumber) throws RobotInterruptedException {
    Pixel p = pointerDown.findImage(area);
    if (p != null) {
      LOGGER.info("FOUND Down pointer" + p);
      int maxY = getMaxY(p);
      // LOGGER.info("Maxxed pointer is" + p);
      // LOGGER.info("maxY=" + maxY);
      p.x = p.x - 2;
      p.y = (int) (maxY + 18);
      // LOGGER.info("p.y=" + p.y);
      if (p.y > _scanner.getBottomRight().y - Locations.RAIL1) {
        p.y = _scanner.getBottomRight().y - Locations.RAIL1 - 3;
        LOGGER.warning("Point below rail 1! + " + p);
      }
    }
    return p;
  }

  private boolean clickCareful(Pixel p, boolean careful, boolean adjust) throws AWTException, IOException, RobotInterruptedException {
    if (careful) {
      if (checkTrainManagement())
        return true;
    }
    _mouse.mouseMove(p);
    _mouse.click();
    if (careful) {
      _mouse.delay(500);
      if (checkTrainManagement())
        return true;
    }

    if (adjust) {
      Rectangle miniArea = new Rectangle(p.x - 24, p.y - 90, 48, 180);
      Pixel p2 = _scanner.getPointerDown().findImage(miniArea);
      if (p2 != null) {
        // click didn't work
        int tries = 2;
        boolean worked = false;
        int newY = p.y;
        for (int i = 1; i <= tries && !worked; i++) {
          newY = p.y + i * 4;
          p2 = new Pixel(p.x, newY);
          _mouse.mouseMove(p2);
          _mouse.click();
          _mouse.delay(250);
          p2 = _scanner.getPointerDown().findImage(miniArea);
          worked = (p2 == null);
        }
        if (!worked) {
          for (int i = 1; i <= tries && !worked; i++) {
            newY = p.y - i * 4;
            p2 = new Pixel(p.x, newY);
            _mouse.mouseMove(p2);
            _mouse.click();
            _mouse.delay(250);
            p2 = _scanner.getPointerDown().findImage(miniArea);
            worked = (p2 == null);
          }
        }
        if (worked) {
          p.y = newY;
          if (careful) {
            _mouse.delay(500);
            if (checkTrainManagement())
              return true;
          } else
            return true;
        }
      }
    }

    return false;
  }

  private boolean checkTrainManagement() throws AWTException, IOException, RobotInterruptedException {
    // Rectangle area = new Rectangle(_scanner.getTopLeft().x + 335,
    // _scanner.getTopLeft().y + 47, 130, 90);
    // Location time = _times[getTimeIndex()];
    Location time = _freightTime;
    Pixel tm = _scanner.getTrainManagementAnchor().findImage();

    // (ScreenScanner.POINTER_TRAIN_MANAGEMENT_IMAGE, area,
    // time.get_coordinates().x,
    // time.get_coordinates().y, false);

    if (tm != null) {
      // is it freight or express?
      _mouse.delay(150);
      Rectangle areaXP = new Rectangle(tm.x, tm.y + 281, 31, 56);
      Rectangle areaFree = new Rectangle(tm.x + 604, tm.y + 74, 100, 30);
      Rectangle areaExpress = new Rectangle(tm.x + 459, tm.y + 74, 33, 33);
      boolean isExpress = false;
      Pixel xpP = _scanner.getXPTrain().findImage(areaXP);
      if (xpP != null) {
        time = _xpTime;
        LOGGER.info("XP " + time.getTime());
      } else {
        Pixel exP = _scanner.getExpressTrain().findImage(areaExpress);
        if (exP != null) {
          time = _expressTime;
          isExpress = true;
          LOGGER.info("EXPRESS " + time.getTime());
        } else {
          Pixel freeP = _scanner.getFreeTrain().findImage(areaFree);
          if (freeP != null) {
            time = _freeTime;
            LOGGER.info("FREE " + time.getTime());
          } else {
            time = _freightTime;
            LOGGER.info("FREIGHT " + time.getTime());
          }
        }
      }

      // 1. ensure the page
      Pixel leftStartArrow = new Pixel(tm.x - 19, tm.y + 366);
      Pixel rightArrow = new Pixel(tm.x + 723, tm.y + 308);
      boolean firstPage = false;
      int tries = 3;
      do {
        Pixel sixMinutes = _scanner.getSixMinutes().findImage(new Rectangle(tm.x + 69, tm.y + 433, 67, 29));
        if (sixMinutes != null)
          firstPage = true;
        else {
          tries--;
          _mouse.mouseMove(leftStartArrow);
          _mouse.click();
          _mouse.delay(150);
        }
      } while (!firstPage && tries > 0);

      // _mouse.mouseMove(leftArrow);
      // for (int i = 0; i < 3; i++) {
      // _mouse.click();
      // _mouse.delay(200);
      // }

      // 2. now, go to desired page
      if (time.getPage() > 1) {
        _mouse.mouseMove(rightArrow);
        for (int i = 1; i < time.getPage(); i++) {
          _mouse.click();
          _mouse.delay(150);
        }
      }

      // 3. click the destination

      _mouse.mouseMove(tm.x + time.getCoordinates().x, tm.y + time.getCoordinates().y);
      if (_settings.getProperty("captureTrains", "false").equalsIgnoreCase("true")) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
        String date = sdf.format(Calendar.getInstance().getTime());
        String filename = "train " + date + ".png";
        _scanner.captureGame(filename);
      }
      _mouse.delay(200, false); // give chance to change the location without
      _mouse.savePosition(); // stopping the magic.

      if (!_devMode) {
        _mouse.click();
        _stats.registerTrain(isExpress);
        // String msg = "Trains: " + _stats.getTotalTrainCount();
        // String date = getNow();
        // _trainsNumberLabel.setText(msg + "  (" + date + ")");
        // //LOGGER.severe(msg);
        // _trainsNumberLabel.invalidate();
        updateLabels();
        boolean weredone = false;
        int turns = 0;
        do {
          turns++;
          LOGGER.fine("Check TM again " + turns);
          tm = _scanner.getTrainManagementAnchor().findImage();
          if (tm != null) {
            _mouse.delay(300);
          } else {
            // we're done
            weredone = true;
            LOGGER.fine("Check TM again DONE ");
          }
        } while (!weredone && turns < 6);
      }
      return true;
    }

    return false;
  }

  private int getMaxY(Pixel p) throws RobotInterruptedException {
    Rectangle miniArea = new Rectangle(p.x - 24, p.y - 90, 48, 180);
    Pixel[] ps = new Pixel[80];
    int maxY = p.y;
    for (int i = 0; i < ps.length; i++) {
      ps[i] = _scanner.getPointerDown().findImage(miniArea);
      _mouse.delay(5);
      if (ps[i] != null) {
        if (ps[i].y > maxY) {
          maxY = ps[i].y;
        }
      }
    }
    return maxY;
  }

  public void rescanContractor(String contractor) {
    _captureContractors.clear();
    _captureContractors.add(contractor);
  }

  private void scanTrains() {
    if (isRunning("MAGIC")) {
      stopMagic();
    }
    if (_trainManagementWindow == null) {
      TrainScanner tscanner = new TrainScanner(_scanner, LOGGER, _settings.getInt("IntTrains.takeABreakAfter", 3));
      _trainManagementWindow = new TrainManagementWindow(null, tscanner);
      tscanner.setSettings(_settings);
    }
    _trainManagementWindow.setVisible(true);
  }

}
