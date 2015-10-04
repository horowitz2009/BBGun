package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.horowitz.mickey.data.Contractor;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.service.Service;

public class ContractorsPanel extends JPanel {

  private CardLayout   _cardLayout;
  private JPanel       _cardPanel;
  private Contractor[] _contractors;
  private JProgressBar _progressBar;

  public ContractorsPanel() {
    super();
    try {
      _contractors = new DataStore().readContractors();
      init();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void init() {
    setLayout(new BorderLayout());
    JToolBar mainToolbar = new JToolBar();
    mainToolbar.setFloatable(false);
    add(mainToolbar, BorderLayout.NORTH);

    mainToolbar.add(new AbstractAction("Reload all") {
      @Override
      public void actionPerformed(ActionEvent e) {
        reloadAll();
      }

    });

    mainToolbar.add(new AbstractAction("Rescan all") {
      @Override
      public void actionPerformed(ActionEvent e) {
        rescanAll();
      }
    });
    mainToolbar.add(new AbstractAction("Request capture all") {
      @Override
      public void actionPerformed(ActionEvent e) {
        requestAll();
      }
    });

    _progressBar = new JProgressBar(0, _contractors.length + 1);
    _progressBar.setVisible(false);
    mainToolbar.add(_progressBar);
    _cardLayout = new CardLayout();
    _cardPanel = new JPanel(_cardLayout);
    add(_cardPanel);
    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createVerticalBox();

    JScrollPane sp = new JScrollPane(box);
    sp.setMinimumSize(new Dimension(100, 500));
    sp.setPreferredSize(new Dimension(77, 500));
    sp.getVerticalScrollBar().setUnitIncrement(20);
    add(sp, BorderLayout.WEST);
    JToggleButton firstButton = null;

    HomeContractorPanel hcp = new HomeContractorPanel();
    _cardPanel.add(hcp, "Home");
    JToggleButton hbutton = createContractorButton("Home");
    firstButton = hbutton;
    bg.add(hbutton);
    box.add(hbutton);

    for (Contractor contractor : _contractors) {
      if (contractor.isActive()) {
        ContractorPanel cp = new ContractorPanel(contractor.getName());
        _cardPanel.add(cp, contractor.getName());
        JToggleButton button = createContractorButton(contractor.getName());
        bg.add(button);
        box.add(button);
      }
    }

    if (firstButton != null) {
      bg.setSelected(firstButton.getModel(), true);
    }
  }

  public void rescanAll() {
    Thread scanThread = new Thread(new Runnable() {
      public void run() {
        _progressBar.setValue(0);
        _progressBar.setVisible(true);
        int n = 0;
        // List<Mission> currentMissions = new ArrayList<>();
        Component[] components = _cardPanel.getComponents();
        for (Component component : components) {
          if (component instanceof IContractorPanel) {
            IContractorPanel conPanel = (IContractorPanel) component;
            if (conPanel.isScan()) {
              conPanel.rescan();
            }
            _progressBar.setValue(++n);
          }
        }

        _progressBar.setValue(++n);

        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
        }
        _progressBar.setVisible(false);
      }
    });
    scanThread.start();
  }

  public void reloadAll() {
    Thread scanThread = new Thread(new Runnable() {
      public void run() {
        _progressBar.setValue(0);
        _progressBar.setVisible(true);
        int n = 0;
        Component[] components = _cardPanel.getComponents();
        for (Component component : components) {
          if (component instanceof IContractorPanel) {
            IContractorPanel conPanel = (IContractorPanel) component;
            conPanel.reload();
            _progressBar.setValue(++n);
          }
        }

        _progressBar.setValue(++n);
        _progressBar.setVisible(false);
      }
    });
    scanThread.start();
  }

  private void requestAll() {
    final String request = new Service().request("captureAll");
    Thread csThread = new Thread(new Runnable() {

      public void run() {
        boolean weredone = false;
        int n = 0;
        _progressBar.setValue(n);
        int old = _progressBar.getMaximum();
        _progressBar.setMaximum(120 * 5);
        _progressBar.setVisible(true);
        do {
          System.err.println("hmm " + n);
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e) {
          }
          n += 3;
          _progressBar.setValue(n);
          boolean isDone = new Service().isDone(request);
          if (isDone) {
            weredone = true;
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            rescanAll();
          }
        } while (!weredone && n < 200); // 2 minutes
        _progressBar.setVisible(false);
        _progressBar.setMaximum(old);
        _progressBar.setValue(0);
      }
    });
    csThread.start();
  }

  private JToggleButton createContractorButton(String name) {
    JToggleButton button = new JToggleButton(new ConctratorAction(name));
    ImageIcon icon1 = ImageManager.getImage("contracts/" + name + "49.png");
    // ImageIcon icon2 = ImageManager.getImage("contracts/blueArea40flat.png");

    // BufferedImage bi = new BufferedImage(icon1.getIconWidth(), icon1.getIconHeight() + icon2.getIconHeight(), BufferedImage.TYPE_INT_RGB);
    // Graphics g = bi.createGraphics();
    // paint the Icon to the BufferedImage.
    // icon1.paintIcon(null, g, 0, 0);
    // icon2.paintIcon(null, g, 0, 40);
    // g.dispose();
    // bi = bi.getSubimage(0, 1, bi.getWidth(), bi.getHeight() - 1);// cut first line

    // button.setIcon(new ImageIcon(bi));
    button.setIcon(icon1);

    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    // button.setPreferredSize(new Dimension(40+4, 54+4));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setBorder(BorderFactory.createLineBorder(new Color(224, 10, 2), 5));
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

  private class ConctratorAction extends AbstractAction {

    private String _contractor;

    ConctratorAction(String contractor) {
      super();
      _contractor = contractor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      _cardLayout.show(_cardPanel, _contractor);
    }

  }
}
