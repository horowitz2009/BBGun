package com.horowitz.mickey.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;

public class ZipCodesReader {
  static List<String> goodZips = new ArrayList<>();
  static List<String> badZips  = new ArrayList<>();

  public static void main(String[] args) {
    // step1();
    step2();

  }

  private static void step1() {
    long cnt = 0;
    String filename = "C:/prj/wamp/www/zipcodes.txt";
    String good = "C:/prj/wamp/www/zip_good1.txt";
    String bad = "C:/prj/wamp/www/zip_bad11.txt";

    try {

      File f = new File(filename);
      FileReader fr = new FileReader(f);

      BufferedReader br = new BufferedReader(fr);
      try {
        String line;
        do {
          line = br.readLine();
          if (line != null && line.length() > 0) {
            cnt++;
            registerZip1(line);
          }

        } while (line != null);
      } finally {
        br.close();
      }

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("CNT = " + cnt);
    System.out.println("Good = " + goodZips.size());
    System.out.println("Bad = " + badZips.size());
    System.out.println("TOT = " + (goodZips.size() + badZips.size()));

    saveList(bad, badZips);
    saveList(good, goodZips);
  }

  private static void registerZip1(String line) {
    assert line != null && line.length() > 0;

    String[] split = line.split(" ");
    String zip = split[0];
    String city;
    String municipality;
    String county;
    String newLine;
    if (split.length >= 5) {
      // System.out.println(line);
      int ii = -1;
      for (int i = 1; i < split.length; i++) {
        if (split[i].equalsIgnoreCase("район")) {
          ii = i;
        }
      }

      if (ii > 0) {
        city = split[1];
        for (int j = 2; j < ii; j++) {
          city = city + " " + split[j];
        }

        municipality = split[ii];
        for (int j = ii + 1; j < split.length - 1; j++) {
          municipality = municipality + " " + split[j];
        }

        county = split[split.length - 1];
        // System.err.print(zip + ",");
        // System.err.print(city + ",");
        // System.err.print(municipality + ",");
        // System.err.println(county);
        newLine = zip + "," + city + "," + municipality + "," + county;
        goodZips.add(newLine);
      } else {
        // NOT SOFIA
        if (zip.startsWith("60") || zip.startsWith("61") || zip.startsWith("62") || zip.startsWith("50") || zip.startsWith("51")
            || zip.startsWith("52")) {
          // System.err.println("STARA ZAGORA?? " + line);
          newLine = line.replace(" ", ",");
          int li = newLine.lastIndexOf(",");
          newLine = newLine.substring(0, li) + " " + newLine.substring(li + 1);
          String[] ss = newLine.split(",");
          if (ss.length != 4) {
            // System.err.println(newLine);
            badZips.add(newLine);// SZ, VT but needs fixing
          } else {
            // System.out.println(newLine);
            goodZips.add(newLine);// fixed SZ, VT and 4 or less
          }
        } else {
          newLine = line.replace(" ", ",");
          // System.err.println(newLine);
          badZips.add(newLine);
        }

      }

    } else {// 4 or less
      newLine = line.replace(" ", ",");
      // System.out.println(newLine);
      goodZips.add(newLine);// 4 or less
    }
  }

  private static void step2() {
    long cnt = 0;
    String filename = "C:/prj/wamp/www/zip_good2.txt";
    String good = "C:/prj/wamp/www/zip_good22.txt";
    String bad = "C:/prj/wamp/www/zip_bad22.txt";

    try {

      File f = new File(filename);
      FileReader fr = new FileReader(f);

      BufferedReader br = new BufferedReader(fr);
      try {
        String line;
        do {
          line = br.readLine();
          if (line != null && line.length() > 0) {
            cnt++;
            registerZip2(line);
          }

        } while (line != null);
      } finally {
        br.close();
      }

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("CNT = " + cnt);
    System.out.println("Good = " + goodZips.size());
    System.out.println("Bad = " + badZips.size());
    System.out.println("TOT = " + (goodZips.size() + badZips.size()));

    removeDups(goodZips);
    // sort by zip
    Collections.sort(goodZips, new Comparator<String>() {
      public int compare(String o1, String o2) {
        String s1 = o1.substring(0, 4);
        String s2 = o2.substring(0, 4);
        if (o1.equals(o2))
          System.out.println(o1);
        return new CompareToBuilder().append(s1, s2).toComparison();
      };
    });

    System.out.println("CNT = " + cnt);
    System.out.println("Good = " + goodZips.size());
    System.out.println("Bad = " + badZips.size());
    System.out.println("TOT = " + (goodZips.size() + badZips.size()));
    
    saveList(bad, badZips);
    saveList(good, goodZips);

  }

  private static void removeDups(List<String> goodZips2) {
    List<String> toRemove = new ArrayList<>();
    for (int i = 0; i < goodZips2.size() - 1; i++) {
      String s1 = goodZips2.get(i);

      for (int j = i + 1; j < goodZips2.size(); j++) {
        String s2 = goodZips2.get(j);
        if (s1.equalsIgnoreCase(s2)) {
          String ss1 = s1.toUpperCase();
          if (s1.equals(ss1)) {
            // good
            toRemove.add(s2);
          } else {
            toRemove.add(s1);
          }
        }
      }
    }

    for (String s : toRemove) {
      System.out.println("removing " + s);
      goodZips2.remove(s);
    }

  }

  private static void saveList(String bad, List<String> zips) {
    try {
      FileWriter fw = new FileWriter(new File(bad));
      BufferedWriter bw = new BufferedWriter(fw);
      try {
        for (String line : zips) {
          bw.write(line);
          bw.newLine();
        }
      } finally {
        bw.close();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static void registerZip2(String line) {
    assert line != null && line.length() > 0;

    String[] split = line.split(",");
    String zip = split[0];
    String city;
    String municipality;
    String county;
    String newLine;
    if (split.length != 4) {
      System.out.println(line);
      badZips.add(line);
    } else {// 4 or less
      // newLine = line.replace(" ", ",");
      // System.out.println(newLine);
      goodZips.add(line);// 4 or less
    }

  }

}
