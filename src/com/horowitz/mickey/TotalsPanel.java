package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.horowitz.mickey.ContractorPanel.FocusTextField;
import com.horowitz.mickey.data.ContractAnalysis;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Material;

public class TotalsPanel extends JPanel {

  private JPanel      _tablePanel;
  private JScrollPane _tableScrollPane;
  private boolean     _shortTerm;
  private JPanel      _toolbar;

  public TotalsPanel(boolean shortTerm) {
    super();
    _shortTerm = shortTerm;
    setLayout(new BorderLayout());
    _tablePanel = new JPanel();
    _tableScrollPane = new JScrollPane(_tablePanel);
    initToolbar();
    add(_tableScrollPane, BorderLayout.CENTER);
    reload();
  }

  private void initToolbar() {
    //setLayout(new BorderLayout());
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

    _toolbar.add(reloadButton);

    // JButton rescanButton = new JButton(new AbstractAction("Rescan") {
    //
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // Thread t = new Thread(new Runnable() {
    // public void run() {
    // rescan();
    // }
    // });
    // t.start();
    // }
    // });
    //
    // _toolbar.add(rescanButton);

    add(_toolbar, BorderLayout.NORTH);
  }

  private void reload() {
    _tablePanel.removeAll();

    _tablePanel.setLayout(new GridBagLayout());
    ContractAnalysis ca = new ContractAnalysis();
    ca.calcALLNeeds();

    try {
      Material[] mats = ca.getConsolidatedMaterials(_shortTerm);
      Material[] matsAvailable = new DataStore().getHome().getMaterials();

      _tablePanel.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.insets = new Insets(2, 10, 2, 5);
//      JTextArea area = new JTextArea(3, 35);
//      area.setOpaque(false);
//      area.setText("");
//      area.setWrapStyleWord(true);
//      _tablePanel.add(area, gbc);
//
//      gbc.gridy = 1;
//
      NumberFormat nf = NumberFormat.getIntegerInstance();

      for (Material mat : mats) {

        ImageIcon icon = ImageManager.getImage("contracts/" + mat.getName() + "24.png");
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
        tf1.setColumns(8);
        tf1.setName(mat + "_currentAmount");
        Material matCurrent = null;
        for (Material mm : matsAvailable) {
          if (mat.getName().equals(mm.getName())) {
            matCurrent = mm;
            break;
          }
        }
        tf1.setHorizontalAlignment(JTextField.RIGHT);
        tf1.setValue(matCurrent == null ? 0l : matCurrent.getAmount());

        FocusTextField tf2 = new FocusTextField(nf);
        tf2.setEditable(false);
        tf2.setOpaque(false);
        tf2.setBorder(BorderFactory.createEmptyBorder());
        tf2.setMargin(new Insets(1, 1, 1, 1));
        tf2.setColumns(8);
        tf2.setName(mat + "_neededAmount");
        tf2.setHorizontalAlignment(JTextField.RIGHT);
        tf2.setValue(mat.getAmount());

        // NumberFormat nf2 = NumberFormat.getIntegerInstance();
        // nf2.setMinimumIntegerDigits(0);
        //
        JTextField tf3 = new JTextField();

        tf3.setMargin(new Insets(1, 1, 1, 1));
        tf3.setColumns(8);
        tf3.setHorizontalAlignment(JTextField.RIGHT);

        long d = mat.getAmount() - (matCurrent == null ? 0l : matCurrent.getAmount());
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
        _tablePanel.add(box, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 10, 2, 5);
      }

      gbc.gridwidth = 2;
      gbc.weightx = 1;
      gbc.weighty = 1;

      JLabel fake = new JLabel(" ");
      _tablePanel.add(fake, gbc);

      _tableScrollPane.getViewport().setView(_tablePanel);

    } catch (IOException e) {
    }
  }

}
