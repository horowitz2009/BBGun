package com.horowitz.mickey.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.horowitz.mickey.DateUtils;

public class Service {

  private String _inprogress = "_inprogress";

  public Service() {
  }

  public Service(String inprogress) {
    _inprogress = inprogress;
  }

  public String request(String request) {
    File f = new File("requests");
    if (!f.exists()) {
      f.mkdir();
    }
    String fname = request + "_" + DateUtils.formatDateForFile(System.currentTimeMillis());
    f = new File("requests/" + fname);
    try {
      // f.setLastModified(System.currentTimeMillis());
      f.createNewFile();
      // FileUtils.writeStringToFile(f, request);
      return fname;
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    return null;
  }

  public boolean done(String request) {
    String fname = "requests/" + request;

    File f = new File(fname + _inprogress);
    if (f.exists()) {
      return f.renameTo(new File(fname + "_done"));
    }
    return false;
  }

  public boolean inProgress(String request) {
    String fname = "requests/" + request;

    File f = new File(fname);
    if (f.exists()) {
      return f.renameTo(new File(fname + _inprogress));
    }
    return false;
  }

  public boolean isDone(String request) {
    File f = new File("requests/" + request + "_done");
    return f.exists();
  }

  public String[] getActiveRequests() {
    File d = new File("requests");
    File[] listFiles = d.listFiles(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return !f.isDirectory() && !f.getName().endsWith("_done") && !f.getName().endsWith(_inprogress);
      }
    });
    List<String> res = new ArrayList<>();
    if (listFiles != null) {
      for (File file : listFiles) {
        res.add(file.getName());
      }
    }
    return res.toArray(new String[0]);
  }

  public static void main(String[] args) {
    Service service = new Service();
    String request = service.request("Jeff_is_here");

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    String[] activeRequests = service.getActiveRequests();
    for (String name : activeRequests) {
      System.err.println(name);
    }

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    service.done(request);

    service.isDone(request);

    service.purgeOld(1000 * 60 * 3);
  }

  public void purgeAll() {
    purgeOld("", 0l);
  }

  public void purgeOld(final long howMuchOld) {
    purgeOld("_done", howMuchOld);
  }

  public void purgeOld(final String suffix, final long howMuchOld) {
    File d = new File("requests");
    File[] listFiles = d.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        long now = System.currentTimeMillis();
        return (pathname.isFile() && pathname.getName().endsWith(suffix) && (now - pathname.lastModified() > howMuchOld));
      }
    });
    if (listFiles != null)
      for (File file : listFiles) {
        file.delete();
      }
  }

  public String getInprogress() {
    return _inprogress;
  }

  public void setInprogress(String inprogress) {
    this._inprogress = inprogress;
  }
}
