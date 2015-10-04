package com.horowitz.mickey.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.horowitz.commons.Settings;
import com.sun.xml.internal.ws.util.StringUtils;

public class CurrentMissions {

  private Mission[] _currentMissions;

  public CurrentMissions() {
    super();
  }

  private void readCurrentMissions() throws IOException {
    try {
      _currentMissions = new DataStore().readCurrentMissions();
      if (_currentMissions == null) {
        _currentMissions = generateInitial();
        new DataStore().writeCurrentMissions(_currentMissions);
      }
    } catch (IOException e) {
      throw new IOException(e);
    }
  }

  private Mission[] generateInitial() {
    List<Mission> currentMissions = new ArrayList<>();

    Settings settings = new Settings("mickey.properties");
    settings.loadSettings();
    String[] contractors = settings.getProperty("contractors").split(",");
    for (String c : contractors) {
      String contractorName = StringUtils.capitalize(c.trim());
      Mission m = new Mission();
      m.setContractor(contractorName);
      m.setNumber(0);
      currentMissions.add(m);
    }
    
    return currentMissions.toArray(new Mission[0]);
  }

  public static void main(String[] args) {
    CurrentMissions currentMissions = new CurrentMissions();
    try {
      currentMissions.readCurrentMissions();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
