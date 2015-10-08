package com.horowitz.bigbusiness.model.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.horowitz.bigbusiness.model.Building;

public class JsonStorage {
  private Gson _gson = new GsonBuilder().setPrettyPrinting().create();

  public List<Building> loadBuildings() throws IOException {
    String json = FileUtils.readFileToString(new File("data/buildings.json"));

    Building[] buildings = _gson.fromJson(json, Building[].class);

    return new ArrayList<Building>(Arrays.asList(buildings));
  }

  public void saveBuildings(List<Building> buildings) throws IOException {

    String json = _gson.toJson(buildings);

    FileUtils.writeStringToFile(new File("data/buildings.json"), json);
  }

}
