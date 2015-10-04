package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.horowitz.mickey.ContractorPanel.FocusTextField;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Home;
import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.data.Mission;
import com.horowitz.mickey.data.Objective;
import com.horowitz.mickey.service.Service;

public final class HomeContractorPanel extends JPanel implements PropertyChangeListener, IContractorPanel {
  private JPanel       _toolbar;
  private JProgressBar _progressBar;

  private String       _contractorName;

  public String getContractorName() {
    return _contractorName;
  }

  private Home        _home;
  private Mission     _currentMission;
  private Mission     _missionDB;
  private JPanel      _objectivesPanel;
  private JTextArea   _area;
  private JPanel      _missionsPanel;
  private Mission[]   _missions;
  private JSplitPane  _splitPane;
  private JScrollPane _missionScrollPane;
  private JScrollPane _objScrollPane;

  public HomeContractorPanel() {
    super();
    _contractorName = "Home";

    _home = null;
    _currentMission = null;
    _missionDB = null;

    initToolbar();
    initForm();
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Contractor Assistant");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    HomeContractorPanel cp = new HomeContractorPanel();

    frame.getContentPane().add(cp, BorderLayout.CENTER);

    frame.setSize(new Dimension(600, 350));

    frame.setLocationRelativeTo(null);

    frame.setVisible(true);

  }

  private void initToolbar() {
    setLayout(new BorderLayout());
    _toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));

    JButton reloadButton = new JButton(new AbstractAction("Reload") {

      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            reload();
          }
        });
        t.start();
      }
    });

    // _toolbar.add(Box.createHorizontalStrut(4));
    _toolbar.add(reloadButton);

    JButton rescanButton = new JButton(new AbstractAction("Rescan") {

      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            rescan();
          }
        });
        t.start();
      }
    });

    _toolbar.add(rescanButton);

    JButton requestScan = new JButton(new AbstractAction("Request capture") {

      @Override
      public void actionPerformed(ActionEvent e) {
        capture();
      }
    });
    _toolbar.add(requestScan);

    _progressBar = new JProgressBar(0, 120);
    _progressBar.setVisible(false);
    _toolbar.add(_progressBar);

    add(_toolbar, BorderLayout.NORTH);
  }

  private void initForm() {
    JPanel formPanel = new JPanel(new BorderLayout());
    _objectivesPanel = new JPanel();
    _missionsPanel = new JPanel(new GridBagLayout());

    _missionScrollPane = new JScrollPane(_missionsPanel);
    _missionScrollPane.setMinimumSize(new Dimension(200, 300));
    _missionScrollPane.setPreferredSize(new Dimension(300, 300));
    _missionScrollPane.getVerticalScrollBar().setUnitIncrement(10);

    _objScrollPane = new JScrollPane(_objectivesPanel);
    _objScrollPane.setMinimumSize(new Dimension(200, 300));

    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _missionScrollPane, _objScrollPane);
    _splitPane.setContinuousLayout(true);
    formPanel.add(_splitPane);

    JButton saveButton = new JButton(new AbstractAction("Save") {

      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            save();
          }
        });
        t.start();
      }
    });

    Box saveBox = Box.createHorizontalBox();

    saveBox.add(Box.createHorizontalGlue());
    saveBox.add(saveButton);
    saveBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    formPanel.add(saveBox, BorderLayout.SOUTH);

    add(formPanel);// to the center of the main panel
    reload();
  }

  private void buildMissionRow(final Mission m, GridBagConstraints gbc) {
    gbc.anchor = GridBagConstraints.WEST;

    final JCheckBox checkBoxDone = new JCheckBox();
    checkBoxDone.setName("" + m.getNumber());
    checkBoxDone.setText("" + m.getLevel());
    checkBoxDone.setSelected(m.isDone());

    final JCheckBox checkBox = new JCheckBox();
    checkBox.setName("" + m.getNumber());
    checkBox.setSelected(m.isSelected());
    checkBox.setText("" + m.getNumber());
    checkBox.setEnabled(!m.isDone());

    checkBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int number = Integer.parseInt(e.getActionCommand());
        for (Mission m : _missions) {
          if (m.getNumber() == number) {
            m.setSelected(checkBox.isSelected());
          }
        }
        updateObjectives();
      }
    });

    final JLabel desc = new JLabel();
    String d = m.getDescription();
    if (d == null)
      d = "";
    desc.setText(d);
    desc.setLabelFor(checkBox);

    if (m.isDone()) {
      desc.setForeground(Color.GRAY);
    } else {
      desc.setForeground(Color.BLACK);
    }
    MouseAdapter mouseListener = new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        // checkBox.setSelected(!checkBox.isSelected());
        checkBox.doClick(40);
      }
    };
    desc.addMouseListener(mouseListener);

    checkBoxDone.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        m.setDone(checkBoxDone.isSelected());
        if (checkBoxDone.isSelected()) {
          if (checkBox.isSelected()) {
            checkBox.setSelected(false);
            m.setSelected(false);
            updateObjectives();
          }
          checkBox.setEnabled(false);
          desc.setForeground(Color.GRAY);
        } else {
          checkBox.setEnabled(true);
          desc.setForeground(Color.BLACK);
        }
        checkBox.repaint();
        desc.repaint();
      }
    });

    gbc.insets = new Insets(4, 2, 4, 4);
    _missionsPanel.add(checkBoxDone, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(2, 2, 2, 2);

    _missionsPanel.add(checkBox, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(2, 2, 2, 12);
    _missionsPanel.add(desc, gbc);

    NumberFormat nf = NumberFormat.getIntegerInstance();

    gbc.insets = new Insets(2, 2, 2, 6);
    gbc.anchor = GridBagConstraints.EAST;

    for (Objective o : m.getObjectives()) {
      JLabel matLabel = new JLabel(nf.format(o.getNeededAmount()));
      matLabel.setFont(matLabel.getFont().deriveFont(matLabel.getFont().getSize() - 2f));
      // o.getMaterial()
      matLabel.setIcon(ImageManager.getImage("contracts/" + o.getMaterial() + "24.png"));
      matLabel.addMouseListener(mouseListener);
      gbc.gridx++;
      _missionsPanel.add(matLabel, gbc);
    }

    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridy++;
    gbc.gridx = 0;
  }

  protected void updateObjectives() {
    // merge objectives
    Material[] mats = Material.createArray();
    for (Mission m : _missions) {
      if (m.isSelected())
        for (Material mat : mats) {
          for (Objective o : m.getObjectives()) {
            if (mat.getName().equals(o.getMaterial())) {
              mat.setAmount(mat.getAmount() + o.getNeededAmount());
            }
          }
        }
    }

    // amounts are consolidated. now generate new objectives
    List<Objective> newObjectives = new ArrayList<>();
    for (Material mat : mats) {
      if (mat.getAmount() > 0) {
        Objective o = new Objective("b", mat.getName());
        o.setNeededAmount(mat.getAmount());
        for (Material mm : _home.getMaterials()) {
          if (mm.getName().equals(mat.getName())) {
            o.setCurrentAmount(mm.getAmount());
          }
        }

        newObjectives.add(o);
      }
    }

    _objectivesPanel.removeAll();

    _objectivesPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(10, 10, 5, 5);
    _area = new JTextArea(3, 35);
    _area.setOpaque(false);
    _area.setText("These objectives are for all selected missions");
    _area.setWrapStyleWord(true);
    _objectivesPanel.add(_area, gbc);

    gbc.gridy = 1;

    NumberFormat nf = NumberFormat.getIntegerInstance();

    for (Objective obj : newObjectives) {

      String mat = obj.getMaterial();
      ImageIcon icon = ImageManager.getImage("contracts/" + mat + "24.png");
      if (icon == null) {
        icon = ImageManager.getImage("contracts/" + "unknown" + "24.png");
      }
      JLabel objLabel = new JLabel(icon);
      JLabel objLabel2 = new JLabel(" / ");
      objLabel2.setFont(objLabel2.getFont().deriveFont(15f));

      FocusTextField tf1 = new FocusTextField(nf);
      tf1.setEditable(false);
      tf1.setOpaque(false);
      tf1.setBorder(BorderFactory.createEmptyBorder());

      tf1.setMargin(new Insets(1, 1, 1, 4));
      tf1.setColumns(6);
      tf1.setName(mat + "_currentAmount");
      tf1.setHorizontalAlignment(JTextField.RIGHT);
      tf1.setValue(obj.getCurrentAmount());

      FocusTextField tf2 = new FocusTextField(nf);
      tf2.setEditable(false);
      tf2.setOpaque(false);
      tf2.setBorder(BorderFactory.createEmptyBorder());
      tf2.setMargin(new Insets(1, 1, 1, 1));
      tf2.setColumns(6);
      tf2.setName(mat + "_neededAmount");
      tf2.setHorizontalAlignment(JTextField.RIGHT);
      tf2.setValue(obj.getNeededAmount());

      // NumberFormat nf2 = NumberFormat.getIntegerInstance();
      // nf2.setMinimumIntegerDigits(0);
      //
      JTextField tf3 = new JTextField();

      tf3.setMargin(new Insets(1, 1, 1, 1));
      tf3.setColumns(6);
      tf3.setHorizontalAlignment(JTextField.RIGHT);

      long d = obj.getNeededAmount() - obj.getCurrentAmount();
      if (d > 0) {
        tf3.setText(nf.format(d));
        tf3.setForeground(Color.RED);
      } else {
        tf3.setText("");
      }
      tf3.setEditable(false);
      tf3.setOpaque(false);
      tf3.setBorder(BorderFactory.createEmptyBorder());

      Box box = Box.createHorizontalBox();

      box.add(objLabel);
      box.add(Box.createHorizontalStrut(5));
      box.add(tf1);
      box.add(objLabel2);
      box.add(tf2);
      box.add(Box.createHorizontalStrut(6));
      box.add(tf3);

      gbc.anchor = GridBagConstraints.WEST;
      _objectivesPanel.add(box, gbc);

      gbc.gridy++;
      gbc.insets = new Insets(5, 10, 5, 5);
    }

    gbc.gridwidth = 2;
    gbc.weightx = 1;
    gbc.weighty = 1;

    JLabel fake = new JLabel(" ");
    _objectivesPanel.add(fake, gbc);

    _objScrollPane.getViewport().setView(_objectivesPanel);

    _currentMission = new Mission("Home", "MIX", 1000);
    _currentMission.setObjectives(newObjectives);
    _currentMission.setSelected(true);
  }

  private void createView() {
    try {
      DataStore ds = new DataStore();
      _missions = ds.getHomeMissions();
      Arrays.sort(_missions);
      clear();
      if (_missions != null && _missions.length > 0) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        for (Mission m : _missions) {
          buildMissionRow(m, gbc);
        }
        gbc.gridx = 10;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        _missionsPanel.add(new JLabel(""), gbc);
      }

      updateObjectives();
      // _missionsPanel.invalidate();
      _missionScrollPane.getViewport().setView(_missionsPanel);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void clear() {

    _missionsPanel.removeAll();
    // _canvas._image = null;
    // _canvas.repaint();
    //
    // Component[] components = _objectivesPanel.getComponents();
    // for (Component c : components) {
    // if (c instanceof JFormattedTextField) {
    // c.removePropertyChangeListener("value", HomeContractorPanel.this);
    // }
    // }
    // _objectivesPanel.removeAll();
    // _objectivesPanel.repaint();
  }

  public void updateView() {
    // SwingUtilities.invokeLater(new Runnable() {
    //
    // public void run() {
    createView();
    // }
    //
    // });

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

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    JFormattedTextField tf = (JFormattedTextField) evt.getSource();
    String[] ss = tf.getName().split("_");

    if (ss[1].equals("currentAmount")) {
      List<Objective> objectives = _currentMission.getObjectives();
      for (Objective o : objectives) {
        if (tf.getName().startsWith(o.getMaterial())) {
          o.setCurrentAmount((Long) tf.getValue());
        }
      }
    } else {
      // rarely but sometimes DB needs to be amended
      List<Objective> objectives = _missionDB.getObjectives();
      for (Objective o : objectives) {
        if (tf.getName().startsWith(o.getMaterial())) {
          o.setNeededAmount((Long) tf.getValue());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IContractorPanel#reload()
   */
  @Override
  public void reload() {
    try {
      DataStore ds = new DataStore();
      _home = ds.getHome();

      updateView();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.horowitz.mickey.IContractorPanel#rescan()
   */
  @Override
  public void rescan() {
    try {
      DataStore ds = new DataStore();
      _home = ds.getHome();
      if (_home != null) {
        // MATERIALS SCAN
        MaterialsScanner matscanner = new MaterialsScanner();
        // mscanner.scanMaterials(materialsImage, materials)
        File f = new File("data/Home_materials.bmp");
        BufferedImage image = ImageIO.read(f);
        Material[] materials = matscanner.scanMaterials(image, Locations.MATERIALS_1, true);
        _home.setMaterials(materials);
        updateView();
        save();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void capture() {
    if (_home != null && !isRunning("CSTHREAD_Home")) {

      final String requestName = new Service().request("captureHome");

      Thread csThread = new Thread(new Runnable() {

        public void run() {
          boolean weredone = false;
          int n = 0;
          _progressBar.setValue(n);
          _progressBar.setVisible(true);
          do {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            _progressBar.setValue(++n);
            boolean isDone = new Service().isDone(requestName);
            if (isDone) {
              weredone = true;
              _progressBar.setVisible(false);
              int start = _progressBar.getValue();
              int end = _progressBar.getMaximum();
              int diff = end - start;
              for (int i = 1; i <= 10; ++i) {
                try {
                  Thread.sleep(1000);// wait Dropbox to download the new image
                } catch (InterruptedException e) {
                }
                _progressBar.setValue(start + diff * i / 10);
              }
              rescan();
            }
          } while (!weredone && n < 120); // 2 minutes
          if (!weredone)
            _progressBar.setVisible(false);

        }
      }, "CSTHREAD_Home");
      csThread.start();
    }
  }

  private void save() {
    DataStore dataStore = new DataStore();
    try {
      if (_home != null) {
        if (_currentMission != null) {
          _home.setCurrentMission(_currentMission);
        }
        dataStore.saveHome(_home);
      }
      if (_missions != null) {
        dataStore.writeMissions("Home", _missions);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean isScan() {
    return false;// rescan all will actually ask Mickey to scan Home together with all scanable contractors.
  }
}
