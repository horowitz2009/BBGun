package com.horowitz.mickey.trainScanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.horowitz.mickey.DateUtils;
import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.JCanvas;
import com.horowitz.mickey.common.Scheduler;
import com.horowitz.mickey.data.DataStore;

public class TrainManagementWindow extends JFrame {
  class TrainView {
    public List<JToggleButton> _buttons;
    JPanel                     _panel;
    Train                      _train;
    public JCheckBox           _checkBox;

  }

  public String           _fancyTime;

  private JScrollPane     _jScrollPane;

  private Thread          _scheduleThread;
  private JLabel          _timeLabel;
  private long            _timeLeft;
  private JTextField      _timeTF;

  private List<Train>     _trains;

  private List<TrainView> _trainViews;

  TrainScanner            _tscanner;

  private JCheckBox       _locoOnly;

  private int             _numberTrains;

  public TrainManagementWindow(List<Train> trains, TrainScanner tscanner) {
    super();
    _trains = trains;
    _tscanner = tscanner;
    _tscanner.setLocoOnly(true);

    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setTitle("Int. Train Manager");
    init();
    setSize(740, 550);
    setLocationRelativeTo(null);
  }

  private TrainView buildTrainView(Train t) {
    _numberTrains++;
    final TrainView trainView = new TrainView();
    JPanel panel = new JPanel(new BorderLayout());
    trainView._panel = panel;
    trainView._train = t;
    trainView._checkBox = new JCheckBox();

    // train image
    JCanvas trainCanvas = new JCanvas();
    trainCanvas.setImage(t.getFullImage());
    Box b = Box.createHorizontalBox();
    b.add(trainCanvas);
    b.add(trainView._checkBox);
    b.add(Box.createHorizontalGlue());
    panel.add(b, BorderLayout.NORTH);

    // additional info image
    JCanvas addInfoCanvas = new JCanvas();
    if (!t.isIdle()) {
      addInfoCanvas.setImage(t.getAdditionalInfoShort());
    } else {
      try {
        BufferedImage read = ImageIO.read(ImageManager.getImageURL("int/empty.png"));
        addInfoCanvas.setImage(read);
      } catch (IOException e) {
      }
    }
    panel.add(addInfoCanvas, BorderLayout.WEST);

    // contractor buttons
    Box box = Box.createHorizontalBox();
    trainView._buttons = new ArrayList<>();

    try {
      List<String> activeContractorNames = new DataStore().getActiveContractorNames();
      activeContractorNames.add("XP");
      activeContractorNames.add("Special");
      for (String cname : activeContractorNames) {
        JToggleButton cbutton = createContractorButton(cname);
        box.add(Box.createHorizontalStrut(2));
        box.add(cbutton);
        trainView._buttons.add(cbutton);
      }
      selectContractors(trainView);
    } catch (IOException e) {
      e.printStackTrace();
    }

    JPanel buttonsPanel = new JPanel(new BorderLayout());
    buttonsPanel.add(new JScrollPane(box), BorderLayout.NORTH);

    Box box2 = Box.createHorizontalBox();
    box2.add(new JLabel(" " + _numberTrains + "  "));

    JButton clearButton = new JButton(new AbstractAction("Clear") {

      @Override
      public void actionPerformed(ActionEvent evt) {
        clear(trainView);
      }
    });
    box2.add(Box.createHorizontalStrut(10));
    box2.add(clearButton);

    JButton removeButton = new JButton(new AbstractAction("Remove this train") {

      @Override
      public void actionPerformed(ActionEvent evt) {
        removeThisTrain(trainView);
      }
    });
    box2.add(Box.createHorizontalStrut(10));
    box2.add(removeButton);

    JButton mergeButton = new JButton(new AbstractAction("Merge") {

      @Override
      public void actionPerformed(ActionEvent evt) {
        mergeWithSelected(trainView);
      }
    });
    box2.add(Box.createHorizontalStrut(10));
    box2.add(mergeButton);

    long time = trainView._train.getTimeToSendNext() - System.currentTimeMillis();
    if (time > 0)
      box2.add(new JLabel("Scheduled for " + DateUtils.fancyTime2(time)));

    buttonsPanel.add(box2, BorderLayout.SOUTH);

    panel.add(buttonsPanel); // center

    return trainView;
  }

  protected void removeThisTrain(TrainView trainView) {
    save();
    // trainView._panel;
    for (Train train : _trains) {
      if (train.getFullImageFileName().equals(trainView._train.getFullImageFileName())) {
        _trains.remove(train);
        break;
      }
    }
    updateView();
  }

  protected void mergeWithSelected(TrainView trainView) {
    // trainView._panel;
    for (TrainView tv : _trainViews) {
      if (tv._checkBox.isSelected()) {
        // tv._train vs trainView._train
        trainView._train.mergeWith(tv._train);
        removeThisTrain(tv);
        break;
      }
    }
    // updateView();
  }

  private void clear(TrainView tv) {
    for (JToggleButton button : tv._buttons) {
      button.setSelected(false);
    }
  }

  private JToggleButton createContractorButton(String name) {
    JToggleButton button = new JToggleButton();
    button.setName(name);
    // button.setText(name);
    ImageIcon icon1 = ImageManager.getImage("int/" + name + "3.png");
    button.setIcon(icon1);

    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    // button.setPreferredSize(new Dimension(40+4, 54+4));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setBorder(BorderFactory.createLineBorder(new Color(224, 10, 2), 7));
    // button.setPreferredSize(new Dimension(80, 80));
    // button.setMinimumSize(new Dimension(70, 70));
    button.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        JToggleButton b = (JToggleButton) e.getSource();
        b.setBorderPainted(b.isSelected());
        b.revalidate();
      }
    });

    return button;
  }

  private void init() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    initToolbar(mainPanel);

    Box box = Box.createVerticalBox();

    _jScrollPane = new JScrollPane(box);
    mainPanel.add(_jScrollPane);

    getContentPane().add(mainPanel);

    reload();
  }

  private void initToolbar(JPanel mainPanel) {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    {
      JButton button = new JButton(new AbstractAction("Reload") {

        @Override
        public void actionPerformed(ActionEvent e) {
          reload();
        }
      });

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Put default to all") {
        
        @Override
        public void actionPerformed(ActionEvent e) {
          putDefaultToAll();
        }
      });
      
      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Rescan ALL") {

        @Override
        public void actionPerformed(ActionEvent e) {
          scan(true, false, _locoOnly.isSelected());
        }
      });

      toolbar.add(button);
    }
//    {
//      JButton button = new JButton(new AbstractAction("Rescan Idle") {
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//          scan(false, true, _locoOnly.isSelected());
//        }
//      });
//
//      toolbar.add(button);
//    }
    {
      JButton button = new JButton(new AbstractAction("ADD Idle") {

        @Override
        public void actionPerformed(ActionEvent e) {
          scan(false, false, _locoOnly.isSelected());
        }
      });

      toolbar.add(button);
    }
    {
      _locoOnly = new JCheckBox("only locos");
      _locoOnly.setSelected(true);
      toolbar.add(_locoOnly);
    }
    {
      JButton button = new JButton(new AbstractAction("Remove all") {

        @Override
        public void actionPerformed(ActionEvent e) {
          removeAllTrains();
        }
      });

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Save") {

        @Override
        public void actionPerformed(ActionEvent e) {
          save();
        }
      });

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Send Now") {

        @Override
        public void actionPerformed(ActionEvent e) {
          sendTrains();
        }
      });

      toolbar.add(button);
    }
    {
      JButton button = new JButton(new AbstractAction("Schedule") {

        @Override
        public void actionPerformed(ActionEvent e) {
          schedule();
        }
      });

      toolbar.add(button);
    }

    _timeTF = new JTextField(8);
    _timeTF.setMaximumSize(new Dimension(50, 20));
    _timeTF.setMinimumSize(new Dimension(50, 20));
    _timeLabel = new JLabel("   no schedule at the moment");

    toolbar.add(_timeTF);
    toolbar.add(_timeLabel);

    mainPanel.add(toolbar, BorderLayout.NORTH);
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

  public void reloadNow() {
    _trains = new ArrayList<>();
    try {
      Train[] trains = new DataStore().readTrains();
      if (trains != null)
        for (Train train : trains) {
          if (train.getFullImage() == null) {
            BufferedImage image = ImageIO.read(new File(train.getFullImageFileName()));
            train.setFullImage(image);
            String additionalInfoShortFileName = train.getAdditionalInfoShortFileName();
            if (additionalInfoShortFileName != null) {
              image = ImageIO.read(new File(additionalInfoShortFileName));
              train.setAdditionalInfoShort(image);
            }
            image = ImageIO.read(new File(train.getScanImageFileName()));
            train.setScanImage(image);
          }
          _trains.add(train);
        }
    } catch (IOException e) {
      //JOptionPane.showMessageDialog(null, "I/O Error!");
      e.printStackTrace();
    }
    // TrainManagementWindow.this.setVisible(true);
    updateView();
  }

  public void putDefaultNow() {
    reloadNow();
    if (_trains != null)
      for (Train train : _trains) {
        train.getContractors().add(_tscanner.getDefaultContractor());
      }
    save();
    updateView();
  }
  
  public void reload() {
    Thread t = new Thread(new Runnable() {
      public void run() {
        reloadNow();
      }

    });
    t.start();
  }

  public void putDefaultToAll() {
    Thread t = new Thread(new Runnable() {
      public void run() {
        putDefaultNow();
      }
      
    });
    t.start();
  }

  private void removeAllTrains() {
    if (_trains != null) {
      _trains.clear();
      save();
      updateView();
    }
  }

  private void save() {
    if (_trains != null)
      try {
        updateTrainStatus();
        new DataStore().writeTrains(_trains.toArray(new Train[0]));
        deleteUnUsedImages();
        // new Thread(new Runnable() {
        // public void run() {
        // }
        // }).start();

      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  private void deleteUnUsedImages() {
    final List<String> usedImages = new ArrayList<>();
    for (Train t : _trains) {
      if (t.getAdditionalInfoFileName() != null) {
        usedImages.add(t.getAdditionalInfoFileName());
      }
      if (t.getAdditionalInfoShortFileName() != null) {
        usedImages.add(t.getAdditionalInfoShortFileName());
      }
      if (t.getFullImageFileName() != null) {
        usedImages.add(t.getFullImageFileName());
      }
      if (t.getScanImageFileName() != null) {
        usedImages.add(t.getScanImageFileName());
      }
    }

    File d = new File("data/int");
    File[] listFiles = d.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File f, String fn) {

        return !usedImages.contains("data/int/" + fn) && !fn.endsWith("json");
      }
    });

    for (File file : listFiles) {
      file.delete();
    }
  }

  protected void scan(final boolean all, final boolean removeNotFound, boolean locoOnly) {
    _tscanner.setLocoOnly(locoOnly);
    Thread t = new Thread(new Runnable() {
      public void run() {
        TrainManagementWindow.this.setVisible(false);

        reload();
        List<Train> newTrains = _tscanner.analyzeIntTrains(all);
        if (_trains != null) {
          if (removeNotFound)
            _tscanner.mergeTrains(_trains, newTrains);
          else
            _tscanner.addNewTrains(_trains, newTrains);
        } else {
          _trains = newTrains;
        }

        save();
        reload();
        TrainManagementWindow.this.setVisible(true);
      }
    });
    t.start();
  }

  /**
   * @deprecated
   * @param time
   */
  private void runScheduleThread(final long time) {
    // long timeNext = time + System.currentTimeMillis();
    // if (_trains != null) {
    // for (Train train : _trains) {
    // train.setTimeToSendNext(timeNext);
    // }
    // }
    // save();

    _scheduleThread = new Thread(new Runnable() {

      public void run() {
        while (true) {
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
            System.err.println("Scheduler interrupted");
            break;
          }
          _timeLeft = time - System.currentTimeMillis();
          if (_timeLeft <= 0) {
            // it's time
            _timeLabel.setText("");
            if (!isRunning("MAGIC"))
              sendTrains();
            break;
          } else {
            _fancyTime = DateUtils.fancyTime2(_timeLeft) + " left to send int. trains...";
            _tscanner.LOGGER.info(">>> " + _fancyTime);
            System.err.println(_fancyTime);
            _timeLabel.setText(_fancyTime);
          }
          try {
            Thread.sleep(19800);
          } catch (InterruptedException e) {
            System.err.println("Scheduler interrupted!");
            break;
          }
        }
      }
    }, "SCHEDULER");
    _scheduleThread.start();
  }

  private void selectContractors(TrainView tv) {
    clear(tv);
    List<String> contractors = tv._train.getContractors();
    for (String cname : contractors) {
      for (JToggleButton button : tv._buttons) {
        if (cname.equals(button.getName())) {
          button.setSelected(true);
          break;
        }
      }
    }
  }

  private void updateTrainStatus() {
    for (TrainView tv : _trainViews) {
      Train t = tv._train;
      List<String> contractors = t.getContractors();
      contractors.clear();

      for (JToggleButton b : tv._buttons) {
        if (b.isSelected()) {
          contractors.add(b.getName());
        }
      }
    }
  }

  public void sendTrains() {
    if (!isRunning("SENDING")) {
      Thread t = new Thread(new Runnable() {
        public void run() {
          sendTrainsNow();
        }
      }, "SENDING");
      t.start();
    }
  }

  public boolean sendTrainsNow() {
    updateTrainStatus();
    boolean sendTrains = _tscanner.sendTrains(_trains);
    if (sendTrains)
      save();
    return sendTrains;
  }

  public boolean isTrainWaiting() {
    long now = System.currentTimeMillis();
    for (Train t : _trains) {
      if (!t.getContractors().isEmpty() && t.getTimeToSendNext() - now <= 0) {
        return true;
      }
    }
    return false;
  }

  private void updateView() {
    Box box = Box.createVerticalBox();
    _trainViews = new ArrayList<>();
    _numberTrains = 0;
    if (_trains != null) {
      for (Train t : _trains) {
        TrainView tv = buildTrainView(t);
        box.add(tv._panel);
        box.add(Box.createVerticalStrut(5));
        _trainViews.add(tv);
      }
    }

    _jScrollPane.getViewport().setView(box);
    _jScrollPane.getVerticalScrollBar().setUnitIncrement(20);
  }

  public long getTimeLeft() {
    return _timeLeft;
  }

  public void setTimeLeft(long timeLeft) {
    _timeLeft = timeLeft;
  }

  protected void schedule() {
    long timeLeft = Scheduler.parse(_timeTF.getText());
    reschedule(timeLeft);
  }

  public void reschedule(long time) {
    setTimeLeft(time + System.currentTimeMillis());
    _tscanner.LOGGER.info("Scheduling sending for " + DateUtils.fancyTime2(time));
  }
}