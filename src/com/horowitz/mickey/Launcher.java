/**
 * 
 */
package com.horowitz.mickey;

import java.awt.Dimension;

/**
 * @author zhristov
 * 
 */
public class Launcher {

  /**
   * @param args
   */
  public static void main(String[] args) {

    MainFrame frame = new MainFrame();

    frame.pack();
    frame.setSize(new Dimension(frame.getSize().width + 8, frame.getSize().height + 8));

    frame.setLocationRelativeTo(null);

    frame.setVisible(true);

  }

}
