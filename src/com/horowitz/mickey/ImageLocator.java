package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.horowitz.commons.Settings;

public class ImageLocator extends JPanel {

  private Settings      _settings;
  private ScreenScanner _scanner;
  private MouseRobot    _mouse;
  private JTextArea _console;
  private JPanel _buttonsPanel;

  public ImageLocator() {
    super();
    
    
    _buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    setLayout(new BorderLayout());
    
    add(_buttonsPanel);
    
    _console = new JTextArea(5, 20);
    add(new JScrollPane(_console), BorderLayout.SOUTH);
    
    
    _settings = Settings.createSettings("mickey.settings");
    _scanner = new ScreenScanner(_settings);
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e) {
    }

//    registerImageButton("Bobby2.bmp");
//    registerImageButton("Mahatma2.bmp");
//    registerImageButton("George2.bmp");
//    registerImageButton("Otto2.bmp");
//    registerImageButton("Jules2.bmp");
//    registerImageButton("Sam2.bmp");
//    registerImageButton("Alan2.bmp");
//    registerImageButton("Wolfgang2.bmp");
//    registerImageButton("Mizuki2.bmp");
//    registerImageButton("Lucy2.bmp");
//    registerImageButton("Giovanni2.bmp");
//    registerImageButton("Ethan2.bmp");
//    registerImageButton("int/brCorner.bmp");
//    registerImageButton("loginFB.bmp");
      registerImageButton("int/Del.bmp");
      registerImageButton("int/brEdge.bmp");
      //registerImageButton("publish3.bmp");

  }

  private void registerImageButton(final String imageName) {
    JButton button = new JButton(new AbstractAction(imageName) {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread(new Runnable() {
          public void run() {
            locate(imageName);
          }
        }).start();
      }
    });
    _buttonsPanel.add(button);
  }

  private void locate(String imageName) {
    try {
      Pixel p = _scanner.generateImageData(imageName).findImage(new Rectangle(0, 0, 1000, 800));
      if (p != null) {
        System.err.println("Found it " + p);
        _console.append("Found it " + p + "\n");
        _mouse.savePosition();
        _mouse.mouseMove(p);
        _mouse.delay(2000);
        _mouse.restorePosition();
      } else {
        _console.append("Coudn't find " + imageName + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Image Locator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final ImageLocator il = new ImageLocator();
    frame.getContentPane().add(il, BorderLayout.CENTER);

    frame.pack();

    frame.setLocationRelativeTo(null);

    frame.setVisible(true);

  }

}
