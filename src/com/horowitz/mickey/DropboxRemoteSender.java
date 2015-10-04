package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.horowitz.mickey.common.MyImageIO;
import com.horowitz.mickey.service.Service;

public class DropboxRemoteSender {
  public static void main(String[] args) {
    JFrame frame = new JFrame("DropboxRemoteSender 926");
    frame.setSize(550, 450);
    outputConsole = new JTextArea(8, 14);
    frame.getContentPane().add(new JScrollPane(outputConsole), BorderLayout.CENTER);
    frame.setAlwaysOnTop(true);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    DropboxRemoteSender dr = new DropboxRemoteSender();
    dr.runListener();
  }

  private int              turn    = 0;
  private MouseRobot       _mouse;
  private Service          service = new Service("-tiktak");
  private static JTextArea outputConsole;

  public DropboxRemoteSender() {
    super();
    try {
      _mouse = new MouseRobot();

      File d = new File("requests");
      publish("Listening to " + d.getAbsolutePath());

    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private void processRequests() {
    publish("" + ++turn);
    String[] requests = service.getActiveRequests();
    for (String r : requests) {
      if (r.startsWith("c")) {
        System.out.println("captured a request: " + r);
        publish("captured a request: " + r);

        service.inProgress(r);
        processClick(r);
      } else {
        // publish("passing this " + r);
      }
    }

    service.purgeOld(1000 * 60 * 60);// 1 hour old
  }

  private void processClick(String r) {
    try {
      captureScreen();
      _mouse.delay(100, false);
      String[] ss = r.split("[-_.]");
      int x = Integer.parseInt(ss[1]);
      int y = Integer.parseInt(ss[2]);
      _mouse.click(x, y);
      System.out.println("click " + x + ", " + y);
      publish("click " + x + ", " + y);
      _mouse.delay(1000, false);
      captureScreen();
      _mouse.delay(10000, false);
      captureScreen();
    } catch (Throwable e) {
      e.printStackTrace();
      publish("ERROR " + e.getMessage());
    } finally {
      System.out.println("done");
      publish("done");
      service.done(r);
    }
  }

  private void captureScreen() {
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    writeImage(new Rectangle(0, 0, screenSize.width, screenSize.height), "ping " + DateUtils.formatDateForFile(System.currentTimeMillis()) + ".png");

  }

  public void writeImage(Rectangle rect, String filename) {
    try {
      writeImage(new Robot().createScreenCapture(rect), filename);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage(BufferedImage image, String filename) {

    try {
      int ind = filename.lastIndexOf("/");
      if (ind > 0) {
        String path = filename.substring(0, ind);
        File f = new File(path);
        f.mkdirs();
      }
      File file = new File(filename);
      MyImageIO.write(image, filename.substring(filename.length() - 3).toUpperCase(), file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void runListener() {
    Thread settingsThread = new Thread(new Runnable() {
      public void run() {
        boolean stop = false;
        do {
          try {
            processRequests();
          } catch (Throwable t) {
            publish("-= ERROR =-");
            publish(t.getMessage());
          }

          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

        } while (!stop);
      }
    }, "SETTINGS");

    settingsThread.start();

  }

  public void publish(String msg) {
    String text = outputConsole.getText();
    if (text.length() > 3000) {
      outputConsole.setText("");
    }
    outputConsole.append(msg);
    outputConsole.append("\n");
    outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
    // outputConsole.repaint();
  }

}
