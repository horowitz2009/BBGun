package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.horowitz.mickey.data.ContractAnalysis;
import com.horowitz.mickey.data.ContractAnalysisAll;
import com.horowitz.mickey.data.ContractAnalysisMAT;
import com.horowitz.mickey.data.ContractAnalysisMin;
import com.horowitz.mickey.data.Need;

public class ContractorAdvisor extends JPanel {

  private static final String VERSION = "v.120";
  private static final String TITLE   = "Contract Advisor " + VERSION;

  private ContractorsPanel    _contractorsPanel;
  private ContractTablePanel  _contractTablePanel;
  private ContractTablePanel  _contractTablePanel2;
  private ContractTablePanel  _contractTablePanel3;
  private TotalsPanel         _totalsPanel;
  private TotalsPanel         _totalsPanel2;

  public ContractorAdvisor() {
    super();
    setLayout(new BorderLayout());
    JTabbedPane pane = new JTabbedPane();
    _contractorsPanel = new ContractorsPanel();
    pane.addTab("Contractors", _contractorsPanel);

    ContractAnalysisMin contractAnalysys = new ContractAnalysisMin();
    contractAnalysys.calcALLNeeds();
    _contractTablePanel = new ContractTablePanel(contractAnalysys);
    _contractTablePanel2 = new ContractTablePanel(new ContractAnalysisAll());
    _contractTablePanel3 = new ContractTablePanel(new ContractAnalysisMAT());
    _totalsPanel = new TotalsPanel(true);
    pane.addTab("Table", _contractTablePanel);
    // _contractTablePanel2.setMap(null);
    pane.addTab("Table2", _contractTablePanel2);
    pane.addTab("Table3", _contractTablePanel3);
    pane.addTab("Totals", _totalsPanel);

    _totalsPanel2 = new TotalsPanel(false);
    pane.addTab("Totals2", _totalsPanel2);
    add(pane);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame(TITLE);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final ContractorAdvisor advisor = new ContractorAdvisor();
    frame.getContentPane().add(advisor, BorderLayout.CENTER);

    frame.setSize(new Dimension(850, 665));

    frame.setLocationRelativeTo(null);

    frame.setVisible(true);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        advisor.reloadAll();

      }
    });

  }

  private void reloadAll() {
    _contractorsPanel.reloadAll();

    ContractAnalysis ca = new ContractAnalysis();
    Map<String, Map<String, Need>> map1 = ca.collectCurrentNeeds();
    Map<String, Map<String, Need>> map2 = ca.collectCurrentNeedsALL();
    Map<String, Map<String, Need>> map3 = ca.collectCurrentNeedsMAT();

    ca.calcALLNeeds();
    _contractTablePanel.setMap(map1);
    _contractTablePanel2.setMap(map2);
    _contractTablePanel3.setMap(map3);
  }

}
