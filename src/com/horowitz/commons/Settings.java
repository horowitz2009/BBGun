package com.horowitz.commons;

import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.horowitz.mickey.Pixel;

public class Settings {
  Properties                  _properties;
  String                      _filename;

  public Settings(String filename) {
    super();
    _properties = new Properties();
    _filename = filename;
  }

  public static Settings createSettings(String filename) {
    Settings s = new Settings(filename);
    s.loadSettings();
    return s;
  }
  
  public boolean loadSettings() {

    try {
      File file = new File(_filename);
      if (file.exists()) {
        FileInputStream fis = new FileInputStream(file);
        _properties.load(fis);
        return true;
      } else {
        //System.err.println("Settings file " + _filename + " does not exist! Setting defaults");
        //setDefaults();
        return false;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
  
  /**
   * @deprecated
   */
  public void setDefaults() {

    _properties.setProperty("mandatoryRefresh.time", "45");

    _properties.setProperty("railsHome", "5");
    _properties.setProperty("railsHome", "104, 108, 116, 124, 143");

    _properties.setProperty("railsOut", "6");
    _properties.setProperty("railsOut", "100, 112, 124, 136, 148, 157");

    _properties.setProperty("railYOffset", "9");
    _properties.setProperty("xOffset", "32");

    _properties.setProperty("street1Y", "150");

    _properties.setProperty("ping.time", "5");
    _properties.setProperty("resume.time", "10");
  }

  public int getInt(String key) {
    String val = _properties.getProperty(key, "0");
    return Integer.parseInt(val.trim());
  }

  public int getInt(String key, int defaultValue) {
    String val = _properties.getProperty(key, "" + defaultValue);
    return Integer.parseInt(val.trim());
  }

  public int[] getArray(String key) {
    String val = _properties.getProperty(key);
    String[] split = val.split(",");
    int[] res = new int[split.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = Integer.parseInt(split[i].trim());
    }
    return res;
  }

  public Rectangle getArea(String key, Pixel p) {
    if (p != null)
      return getArea(key, p.x, p.y);
    else
      return getArea(key, 0, 0);
  }

  public Rectangle getArea(String key, int baseX, int baseY) {
    String val = _properties.getProperty(key);
    String[] split = val.split(",");
    Rectangle res = new Rectangle(baseX + Integer.parseInt(split[0].trim()), baseY + Integer.parseInt(split[1].trim()), Integer.parseInt(split[2]
        .trim()), Integer.parseInt(split[3].trim()));
    return res;
  }

  public String getProperty(String key) {
    return _properties.getProperty(key);
  }

  public boolean containsKey(Object key) {
    return _properties.containsKey(key);
  }

  public String getProperty(String key, String defaultValue) {
    return _properties.getProperty(key, defaultValue);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    String v =  _properties.getProperty(key, defaultValue ? "true": "false");
    return "true".equalsIgnoreCase(v);
  }
  
  public void saveSettings() {
    try {
      FileOutputStream fos = new FileOutputStream(new File(_filename));
      _properties.store(fos, "");
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void saveSettingsSorted() {

    try {
      Set<Object> keySet = _properties.keySet();
      SortedSet<String> sortedKeys = new TreeSet<String>();
      for (Object object : keySet) {
        sortedKeys.add(object.toString());
      }

      FileOutputStream fos = new FileOutputStream(new File(_filename));
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));
      try {
        for (String key : sortedKeys) {
          String value = _properties.getProperty(key);
          bw.write(key + "=" + value);
          bw.newLine();
        }
        bw.flush();
      } finally {
        bw.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void removeKey(String key) {
    _properties.remove(key);
  }

  public void setProperty(String key, String value) {
    _properties.setProperty(key, value);
  }

}
