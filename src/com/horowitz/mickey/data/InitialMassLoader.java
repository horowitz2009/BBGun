package com.horowitz.mickey.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class InitialMassLoader {

  public static void main(String[] args) {
    System.out.println("generating contractors and missions");
    // generateContractors();
    // generateMissions();
    
    //loadHomeMissions(100);
    //addMoreHomeMissions(200);
    //addMoreHomeMissions2(300);
    
    addNewContractor("Ethan");
    System.out.println("Done.");
  }

  private static void addMoreHomeMissions2(Integer n) {
    try {
      DataStore ds = new DataStore();
      Home home = new Home();
      home.setMaterials(Material.createArray());
      ds.saveHome(home);

      List<Mission> newMissions = new ArrayList<>();

      Mission m;
      List<Objective> objectives;

      m = new Mission("Home", "Local slot #32", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Carbon", 1080000));
      objectives.add(new Objective("b", "Titanium", 1080000));
      objectives.add(new Objective("b", "Silicon", 1040000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(271);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #33", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 1240000));
      objectives.add(new Objective("b", "U-235", 1120000));
      objectives.add(new Objective("b", "Marble", 1040000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(285);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #34", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wires", 960000));
      objectives.add(new Objective("b", "Plastics", 960000));
      objectives.add(new Objective("b", "Silicon", 960000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(299);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #35", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1600000));
      objectives.add(new Objective("b", "Nails", 1600000));
      objectives.add(new Objective("b", "Rubber", 800000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(313);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #36", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 32000000));
      objectives.add(new Objective("b", "Fuel", 1600000));
      objectives.add(new Objective("b", "Titanium", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(327);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #37", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 1400000));
      objectives.add(new Objective("b", "Rubber", 1400000));
      objectives.add(new Objective("b", "Wires", 1400000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(341);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #38", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "U-235", 1600000));
      objectives.add(new Objective("b", "Cement", 1520000));
      objectives.add(new Objective("b", "Plastics", 1440000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(355);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #39", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1600000));
      objectives.add(new Objective("b", "Gravel", 1600000));
      objectives.add(new Objective("b", "Wires", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(369);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #40", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 1600000));
      objectives.add(new Objective("b", "Steel", 1600000));
      objectives.add(new Objective("b", "Silicon", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(383);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #41", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 1600000));
      objectives.add(new Objective("b", "Fuel", 1600000));
      objectives.add(new Objective("b", "Titanium", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(397);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #42", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 1600000));
      objectives.add(new Objective("b", "Carbon", 1600000));
      objectives.add(new Objective("b", "Marble", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(411);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #43", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 1600000));
      objectives.add(new Objective("b", "Plastics", 1600000));
      objectives.add(new Objective("b", "Silicon", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(425);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #44", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1600000));
      objectives.add(new Objective("b", "Cement", 1600000));
      objectives.add(new Objective("b", "Rubber", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(439);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #45", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 1600000));
      objectives.add(new Objective("b", "U-235", 1600000));
      objectives.add(new Objective("b", "Silicon", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(453);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #46", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1920000));
      objectives.add(new Objective("b", "Fuel", 1840000));
      objectives.add(new Objective("b", "Gravel", 2000000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(467);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #47", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 2240000));
      objectives.add(new Objective("b", "Marble", 2240000));
      objectives.add(new Objective("b", "Wires", 2240000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(481);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #48", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 2480000));
      objectives.add(new Objective("b", "Steel", 2480000));
      objectives.add(new Objective("b", "Carbon", 2480000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(495);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #49", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 2720000));
      objectives.add(new Objective("b", "Gravel", 2720000));
      objectives.add(new Objective("b", "Titanium", 2720000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(509);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #50", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 2980000));
      objectives.add(new Objective("b", "U-235", 2980000));
      objectives.add(new Objective("b", "Marble", 2980000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(523);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #51", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 3280000));
      objectives.add(new Objective("b", "Cement", 3280000));
      objectives.add(new Objective("b", "Wires", 3280000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(537);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #52", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Fuel", 3600000));
      objectives.add(new Objective("b", "Rubber", 3600000));
      objectives.add(new Objective("b", "Plastics", 3600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(551);
      newMissions.add(m);

      m = new Mission("Home", "International slot #19", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Fuel", 1360000));
      objectives.add(new Objective("b", "Wires", 1360000));
      objectives.add(new Objective("b", "Plastics", 1360000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(267);
      newMissions.add(m);

      m = new Mission("Home", "International slot #20", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 1440000));
      objectives.add(new Objective("b", "U-235", 1440000));
      objectives.add(new Objective("b", "Carbon", 1440000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(282);
      newMissions.add(m);

      m = new Mission("Home", "International slot #21", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Fuel", 1536000));
      objectives.add(new Objective("b", "U-235", 1560000));
      objectives.add(new Objective("b", "Cement", 1544000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(268);
      newMissions.add(m);

      m = new Mission("Home", "International slot #22", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 1704000));
      objectives.add(new Objective("b", "Marble", 1680000));
      objectives.add(new Objective("b", "Wires", 1600000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(282);
      newMissions.add(m);

      m = new Mission("Home", "International slot #23", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 1824000));
      objectives.add(new Objective("b", "Titanium", 1768000));
      objectives.add(new Objective("b", "Plastics", 1920000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(296);
      newMissions.add(m);

      m = new Mission("Home", "International slot #24", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 1915000));
      objectives.add(new Objective("b", "Gravel", 1920000));
      objectives.add(new Objective("b", "Marble", 2000000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(310);
      newMissions.add(m);

      m = new Mission("Home", "International slot #25", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 2010400));
      objectives.add(new Objective("b", "Steel", 2040000));
      objectives.add(new Objective("b", "Carbon", 1984000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(324);
      newMissions.add(m);

      m = new Mission("Home", "International slot #26", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wires", 2112000));
      objectives.add(new Objective("b", "Plastics", 2080000));
      objectives.add(new Objective("b", "Marble", 2144000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(338);
      newMissions.add(m);

      m = new Mission("Home", "International slot #27", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 2213600));
      objectives.add(new Objective("b", "U-235", 2213600));
      objectives.add(new Objective("b", "Titanium", 2213600));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(352);
      newMissions.add(m);

      m = new Mission("Home", "International slot #28", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 2315200));
      objectives.add(new Objective("b", "Glass", 2315200));
      objectives.add(new Objective("b", "Cement", 2315200));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(366);
      newMissions.add(m);

      m = new Mission("Home", "International slot #29", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Fuel", 2416800));
      objectives.add(new Objective("b", "Gravel", 2416800));
      objectives.add(new Objective("b", "Rubber", 2416800));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(417);
      newMissions.add(m);

      m = new Mission("Home", "International slot #30", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 2518400));
      objectives.add(new Objective("b", "Carbon", 2518400));
      objectives.add(new Objective("b", "Plastics", 2518400));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(432);
      newMissions.add(m);

      m = new Mission("Home", "International slot #31", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 2620000));
      objectives.add(new Objective("b", "Marble", 2620000));
      objectives.add(new Objective("b", "Silicon", 2620000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(447);
      newMissions.add(m);

      Mission[] oldMissions = ds.getHomeMissions();

      Mission[] newMissionsArray = newMissions.toArray(new Mission[0]);
      Mission[] all = (Mission[]) ArrayUtils.addAll(oldMissions, newMissionsArray);

      ds.writeMissions("Home", all);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static void addMoreHomeMissions(Integer n) {
    try {
      DataStore ds = new DataStore();
      Home home = new Home();
      home.setMaterials(Material.createArray());
      ds.saveHome(home);

      List<Mission> newMissions = new ArrayList<>();

      Mission m;
      List<Objective> objectives;

      m = new Mission("Home", "Storage 800-900", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "U-235", 160000));
      objectives.add(new Objective("b", "Wires", 104000));
      objectives.add(new Objective("b", "Plastics", 104000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(95);

      newMissions.add(m);

      m = new Mission("Home", "Storage 900-1000", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 1760000));
      objectives.add(new Objective("b", "Wood", 160000));
      objectives.add(new Objective("b", "Plastics", 120000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(110);

      newMissions.add(m);

      m = new Mission("Home", "Storage 1000-1100", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 200000));
      objectives.add(new Objective("b", "Fuel", 192000));
      objectives.add(new Objective("b", "Silicon", 184000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(125);
      newMissions.add(m);

      m = new Mission("Home", "Storage 1100-1200", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 3200000));
      objectives.add(new Objective("b", "U-235", 240000));
      objectives.add(new Objective("b", "Silicon", 240000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(140);
      newMissions.add(m);

      m = new Mission("Home", "Storage 1200-1300", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 240000));
      objectives.add(new Objective("b", "Titanium", 200000));
      objectives.add(new Objective("b", "Silicon", 220000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(155);
      newMissions.add(m);

      m = new Mission("Home", "Storage 1300-1400", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 3600000));
      objectives.add(new Objective("b", "Wood", 240000));
      objectives.add(new Objective("b", "Glass", 240000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(170);
      newMissions.add(m);

      m = new Mission("Home", "Storage 1400-1500", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 300000));
      objectives.add(new Objective("b", "Carbon", 280000));
      objectives.add(new Objective("b", "Plastics", 280000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(185);
      newMissions.add(m);

      m = new Mission("Home", "Storage 1500-1600", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 320000));
      objectives.add(new Objective("b", "Gravel", 320000));
      objectives.add(new Objective("b", "Marble", 304000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(200);
      newMissions.add(m);

      m = new Mission("Home", "Storage 1600-1700", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 384000));
      objectives.add(new Objective("b", "Cement", 384000));
      objectives.add(new Objective("b", "Silicon", 320000));
      m.setObjectives(objectives);
      m.setSelected(false);
      m.setLevel(215);
      newMissions.add(m);

      Mission[] oldMissions = ds.getHomeMissions();

      Mission[] newMissionsArray = newMissions.toArray(new Mission[0]);
      Mission[] all = (Mission[]) ArrayUtils.addAll(oldMissions, newMissionsArray);

      ds.writeMissions("Home", all);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static void loadHomeMissions(Integer n) {
    try {
      DataStore ds = new DataStore();
      Home home = new Home();
      home.setMaterials(Material.createArray());
      ds.saveHome(home);

      List<Mission> newMissions = new ArrayList<>();

      Mission m;
      List<Objective> objectives;

      m = new Mission("Home", "International slot #13", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 8000000));
      objectives.add(new Objective("b", "Wires", 1040000));
      objectives.add(new Objective("b", "Silicon", 1040000));
      m.setObjectives(objectives);
      m.setSelected(true);
      m.setLevel(156);
      newMissions.add(m);

      m = new Mission("Home", "International slot #14", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 1116000));
      objectives.add(new Objective("b", "U-235", 1044000));
      objectives.add(new Objective("b", "Carbon", 1044000));
      m.setObjectives(objectives);
      m.setLevel(170);
      newMissions.add(m);

      m = new Mission("Home", "International slot #15", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 1200000));
      objectives.add(new Objective("b", "Titanium", 1120000));
      objectives.add(new Objective("b", "Plastics", 1120000));
      m.setObjectives(objectives);
      m.setLevel(184);
      newMissions.add(m);

      m = new Mission("Home", "Second Maglev Rail", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Titanium", 524000));
      objectives.add(new Objective("b", "Wires", 476000));
      objectives.add(new Objective("b", "Silicon", 452000));
      m.setObjectives(objectives);
      m.setSelected(true);
      m.setLevel(155);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #21", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 1800000));
      objectives.add(new Objective("b", "Steel", 1480000));
      objectives.add(new Objective("b", "Cement", 880000));
      m.setSelected(true);
      m.setObjectives(objectives);
      m.setLevel(131);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #23", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1800000));
      objectives.add(new Objective("b", "Gravel", 1480000));
      objectives.add(new Objective("b", "Cement", 880000));
      m.setObjectives(objectives);
      m.setLevel(155);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #24", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1800000));
      objectives.add(new Objective("b", "Steel", 1480000));
      objectives.add(new Objective("b", "Rubber", 880000));
      m.setObjectives(objectives);
      m.setLevel(159);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #25", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 1400000));
      objectives.add(new Objective("b", "Rubber", 1400000));
      objectives.add(new Objective("b", "Titanium", 800000));
      m.setObjectives(objectives);
      m.setLevel(173);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #26", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 1560000));
      objectives.add(new Objective("b", "Cement", 1400000));
      objectives.add(new Objective("b", "Carbon", 1000000));
      m.setObjectives(objectives);
      m.setLevel(187);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #27", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 3960000));
      objectives.add(new Objective("b", "Fuel", 1560000));
      objectives.add(new Objective("b", "U-235", 1320000));
      m.setObjectives(objectives);
      m.setLevel(201);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #28", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 16000000));
      objectives.add(new Objective("b", "Titanium", 200000));
      m.setObjectives(objectives);
      m.setLevel(215);
      newMissions.add(m);

      // level 229
      m = new Mission("Home", "Local slot #29", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 15200000));
      objectives.add(new Objective("b", "Nails", 680000));
      m.setObjectives(objectives);
      m.setLevel(229);
      newMissions.add(m);

      // level 243
      m = new Mission("Home", "Local slot #30", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 1200000));
      objectives.add(new Objective("b", "Rubber", 1160000));
      objectives.add(new Objective("b", "Wires", 1120000));
      m.setObjectives(objectives);
      m.setLevel(243);
      newMissions.add(m);

      // level 257
      m = new Mission("Home", "Local slot #31", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1600000));
      objectives.add(new Objective("b", "Nails", 1600000));
      objectives.add(new Objective("b", "Plastics", 1240000));
      m.setObjectives(objectives);
      m.setLevel(257);
      newMissions.add(m);

      // level 222
      m = new Mission("Home", "International slot #16", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 1200000));
      objectives.add(new Objective("b", "U-235", 1200000));
      objectives.add(new Objective("b", "Carbon", 1200000));
      m.setObjectives(objectives);
      m.setLevel(222);
      newMissions.add(m);

      // level 237
      m = new Mission("Home", "International slot #17", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Fuel", 1320000));
      objectives.add(new Objective("b", "Titanium", 1240000));
      objectives.add(new Objective("b", "Marble", 1320000));
      m.setObjectives(objectives);
      m.setLevel(237);
      newMissions.add(m);

      // level 252
      m = new Mission("Home", "International slot #18", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 1360000));
      objectives.add(new Objective("b", "Cement", 1280000));
      objectives.add(new Objective("b", "Rubber", 1200000));
      m.setObjectives(objectives);
      m.setLevel(252);
      newMissions.add(m);

      m = new Mission("Home", "8 destinations", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 240000));
      objectives.add(new Objective("b", "Rubber", 200000));
      objectives.add(new Objective("b", "Carbon", 200000));
      m.setObjectives(objectives);
      m.setLevel(135);
      newMissions.add(m);

      // second station
      m = new Mission("Home", "Second Station", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 660000));
      objectives.add(new Objective("b", "U-235", 710000));
      objectives.add(new Objective("b", "Cement", 649600));
      m.setSelected(true);
      m.setObjectives(objectives);
      m.setLevel(125);
      newMissions.add(m);

      // third station
      m = new Mission("Home", "Third Station", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 6800000));
      objectives.add(new Objective("b", "Wood", 560000));
      objectives.add(new Objective("b", "Marble", 560000));
      m.setObjectives(objectives);
      m.setLevel(145);
      newMissions.add(m);

      ds.writeMissions("Home", newMissions.toArray(new Mission[0]));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void addNewContractor(String contractorName) {
    try {
      Contractor contractor = new Contractor(contractorName, Material.createArray());
      new DataStore().saveContractor(contractor);
      Mission[] missions = readMissions(contractorName);
      new DataStore().writeMissions(contractorName, missions);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  private static Mission[] readMissions(String contractor) throws Exception {
    Set<String> mat = new HashSet<>();

    mat.add("xp");
    mat.add("money");
    mat.add("gold");
    mat.add("loco");
    mat.add("wagon");
    mat.add("steam");
    mat.add("diesel");
    mat.add("electric");
    mat.add("maglev");
    mat.add("ts");
    mat.add("tsany");
    Material[] ma = Material.createArray();
    for (Material material : ma) {
      mat.add(material.getName());
    }

    List<Mission> missions = new ArrayList<>();

    List<String> lines = FileUtils.readLines(new File("data/" + contractor + "_missions.txt"));
    for (String line : lines) {
      try {
        String[] s = line.split(",");
        Mission m = new Mission();
        m.setContractor(StringUtils.capitalize(contractor));
        m.setNumber(Integer.parseInt(s[0]));// 0 number
        String t = s[1].trim();// 1 type
        boolean any = false;
        if (t.length() > 1) {
          t = t.substring(0, 1);
          any = true;
        }
        m.setAny(any);

        if (t.equals("s") || t.equals("b") || t.equals("d")) {
          // ok
        } else {
          throw new Exception("ERROR! not valid type: " + contractor + " " + line);
        }
        if (s.length > 2) {

          for (int i = 2; i < s.length; i++) {
            if (i > 2 && t.equals("s"))
              t = "d";

            String[] what = s[i].trim().split(" ");

            String w = what[1];
            if (w.equals("money"))
              w = "gold";

            if (!mat.contains(w)) {
              throw new Exception("ERROR! not valid type: " + contractor + " " + line);
            }

            w = StringUtils.capitalize(w);
            if (w.equals("Ts")) {
              w = "TS";
            }
            if (w.equals("Xp")) {
              w = "XP";
            }

            if (w.equals("Tsany")) {
              w = "TSany";
            }

            Objective obj = new Objective(t, w);
            obj.setNeededAmount(Integer.parseInt(what[0]));
            m.getObjectives().add(obj);
          }
        }
        missions.add(m);
      } catch (Throwable t) {
        t.printStackTrace();
        throw new Exception("ERROR reading line: " + contractor + " " + line);
      }
    }

    return missions.toArray(new Mission[0]);

  }

}
