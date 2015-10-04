package com.horowitz.mickey.common;

import java.util.HashSet;
import java.util.Set;

public class Scheduler {

  public static long parse(String s) {
    long res = 0;

    try {
      String[] ss = s.trim().split(" ");

      for (int i = 0; i < ss.length; i++) {
        res += parseSimple(ss[i]);
      }
    } catch (Exception e) {
    }

    return res;

  }

  public static long parseSimple(String s) {
    long res = 0;

    Set<String> letters = new HashSet<>();
    letters.add("d");
    letters.add("h");
    letters.add("m");
    letters.add("s");
    long multiplier = 0;
    String ch = s.substring(s.length() - 1).toLowerCase();
    if (letters.contains(ch)) {
      String sn = s.substring(0, s.length() - 1);
      res = Long.parseLong(sn);
      if (ch.equalsIgnoreCase("d")) {
        multiplier = 24 * 60 * 60 * 1000; // a day
      } else if (ch.equalsIgnoreCase("h")) {
        multiplier = 60 * 60 * 1000; // an hour
      } else if (ch.equalsIgnoreCase("m")) {
        multiplier = 60 * 1000; // a minute
      } else {
        multiplier = 1000; // a second
      }

      res *= multiplier;
    }

    return res;
  }
}
