package com.horowitz.mickey.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.horowitz.mickey.trainScanner.Train;

public class DataStore {

  private Gson _gson = new GsonBuilder().setPrettyPrinting().create();

  public Contractor[] readContractors() throws IOException {
    String json = FileUtils.readFileToString(new File("data/contractors.json"));

    Contractor[] contractors = _gson.fromJson(json, Contractor[].class);

    return contractors;
  }

  public Contractor getContractor(String name) throws IOException {
    Contractor[] contractors = readContractors();
    for (Contractor c : contractors) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  public Home getHome() throws IOException {
    String json = FileUtils.readFileToString(new File("data/home.json"));
    return _gson.fromJson(json, Home.class);
  }

  public void saveContractor(Contractor contractor) throws IOException {
    Contractor[] contractors = readContractors();
    boolean found = false;
    for (Contractor c : contractors) {
      if (c.getName().equals(contractor.getName())) {
        c.extract(contractor);
        found = true;
        break;
      }
    }
    if (!found) {
      contractors = (Contractor[]) ArrayUtils.add(contractors, contractor);
    }
    writeContractors(contractors);
  }

  public void saveHome(Home home) throws IOException {
    String json = _gson.toJson(home);

    FileUtils.writeStringToFile(new File("data/home.json"), json);
  }

  public void writeContractors(Contractor[] contractors) throws IOException {

    String json = _gson.toJson(contractors);

    FileUtils.writeStringToFile(new File("data/contractors.json"), json);
  }

  public void writeTrains(Train[] trains) throws IOException {

    String json = _gson.toJson(trains);

    FileUtils.writeStringToFile(new File("data/int/trains.json"), json);
  }

  public Train[] readTrains() throws IOException {
    File file = new File("data/int/trains.json");
    Train[] trains = null;
    if (file.exists()) {
      String json = FileUtils.readFileToString(file);
      trains = _gson.fromJson(json, Train[].class);
    }

    return trains;
  }

  public Mission[] getHomeMissions() throws IOException {
    return readMissions("Home");
  }

  public Mission[] getSelectedHomeMissions() throws IOException {
    Mission[] readMissions = readMissions("Home");
    List<Mission> res = new ArrayList<>();
    for (Mission mission : readMissions) {
      if (mission.isSelected()) {
        res.add(mission);
      }
    }
    return res.toArray(new Mission[0]);
  }

  public Mission[] readMissions(String contractor) throws IOException {
    return readMissions(contractor, "");
  }

  public Mission[] readMissions(String contractor, String type) throws IOException {
    File file = new File("data/" + contractor + "_missions" + type + ".json");
    if (file.exists()) {
      String json = FileUtils.readFileToString(file);

      Mission[] missions = _gson.fromJson(json, Mission[].class);

      return missions;
    } else {
      return new Mission[0];
    }
  }

  public Mission getMission(String contractor, int number) throws IOException {
    Mission[] missions = readMissions(contractor);
    for (Mission mission : missions) {
      if (mission.getNumber() == number) {
        return mission;
      }
    }
    return null;
  }

  public Mission getMissionWithExtra(String contractor, int number) throws IOException {
    Mission m = null;
    Mission[] missions = readMissions(contractor);
    for (Mission mission : missions) {
      if (mission.getNumber() == number) {
        m = mission;
        break;
      }
    }
    if (m == null) {
      missions = readMissions(contractor, "_OPT");
      if (missions.length == 0) {
        missions = readMissions(contractor, "_BEST");
      }
      for (Mission mission : missions) {
        if (mission.getNumber() == number) {
          m = mission;
          break;
        }
      }
    }
    return m;
  }

  public Mission getCurrentMission(String contractor, int number) throws IOException {
    Mission[] missions = readCurrentMissions();
    for (Mission mission : missions) {
      if (mission.getContractor().equalsIgnoreCase(contractor)) {
        return mission;
      }
    }
    return null;
  }

  public void writeMissions(String contractor, Mission[] missions) throws IOException {
    writeMissions(contractor, missions, "");
  }

  public void writeMissions(String contractor, Mission[] missions, String type) throws IOException {

    String json = _gson.toJson(missions);

    FileUtils.writeStringToFile(new File("data/" + contractor + "_missions" + type + ".json"), json);
  }

  public void writeMission(Mission newMission) throws IOException {
    Mission m = null;
    String type = "";
    Mission[] missions;
    if (newMission.getNumber() > 100) {
      type = "_OPT";
      missions = readMissions(newMission.getContractor(), type);
      if (missions.length == 0) {
        type = "_BEST";
        missions = readMissions(newMission.getContractor(), type);
      }
    } else {
      missions = readMissions(newMission.getContractor());
    }

    for (Mission mission : missions) {
      if (mission.getNumber() == newMission.getNumber()) {
        m = mission;
        break;
      }
    }

    if (m != null) {
      newMission.populate(m);
    }
    writeMissions(newMission.getContractor(), missions, type);
  }

  public Mission[] readCurrentMissions() throws IOException {
    File file = new File("data/current_missions.json");
    if (file.exists()) {
      String json = FileUtils.readFileToString(file);

      Mission[] missions = _gson.fromJson(json, Mission[].class);

      return missions;
    } else {
      return null;
    }
  }

  public void mergeCurrentWithDB(Mission[] currentMissions) throws IOException {
    for (int i = 0; i < currentMissions.length; i++) {
      Mission cm = currentMissions[i];
      Mission mdb = getMissionWithExtra(cm.getContractor(), cm.getNumber());
      if (mdb != null)
        cm.mergeWithDB(mdb);
    }
  }

  public void writeCurrentMissions(Mission[] currentMissions) throws IOException {
    String json = _gson.toJson(currentMissions);

    FileUtils.writeStringToFile(new File("data/current_missions.json"), json);
  }

  public void writeCurrentMission(Mission currentMission) throws IOException {
    Mission[] missions = readCurrentMissions();
    boolean found = false;
    for (Mission mission : missions) {
      if (mission.getContractor().equals(currentMission.getContractor())) {
        mission.setAny(currentMission.isAny());
        mission.setNumber(currentMission.getNumber());
        mission.setObjectives(currentMission.getObjectives());
        found = true;
        break;
      }
    }
    if (!found) {
      // it's new
      missions = (Mission[]) ArrayUtils.add(missions, currentMission);
    }

    writeCurrentMissions(missions);
  }

  public List<String> getActiveContractorNames() throws IOException {
    List<String> active = new ArrayList<>();
    for (Contractor c : readContractors()) {
      if (c.isActive()) {
        active.add(c.getName());
      }
    }
    return active;
  }

  public List<Contractor> getActiveContractors() throws IOException {
    List<Contractor> active = new ArrayList<>();
    for (Contractor c : readContractors()) {
      if (c.isActive()) {
        active.add(c);
      }
    }
    return active;
  }

  public List<String> getContractorNamesForScan() throws IOException {
    List<String> forScan = new ArrayList<>();
    for (Contractor c : readContractors()) {
      if (c.isScan()) {
        forScan.add(c.getName());
      }
    }
    return forScan;
  }

}
