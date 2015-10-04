package com.horowitz.mickey;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DateUtils {

  public static String fancyTime(long time) {
    String res = "";
    long s = time / 1000;

    long m = s / 60;
    long ss = s % 60;
    long h = m / 60;
    long mm = m % 60;
    long d = h / 24;
    long hh = h % 24;

    res = d + "d " + formatNumber(hh, 2) + ":" + formatNumber(mm, 2) + ":" + formatNumber(ss, 2);

    return res;
  }

  public static String fancyTime2(long time) {
    String res = "";
    long s = time / 1000;

    long m = s / 60;
    long ss = s % 60;
    long h = m / 60;
    long mm = m % 60;
    long d = h / 24;
    long hh = h % 24;
    if (d > 0) {
      res = res + (d + "d ");
    }

    if (hh > 0) {
      res = res + (hh + "h ");
    }

    if (mm > 0) {
      res = res + (mm + "m ");
    }

    res = res + (ss + "s");

    return res;
  }

  public static String formatNumber(long number, int leadingZeros) {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMinimumIntegerDigits(leadingZeros);
    return nf.format(number);
  }

  public static String formatDateForFile(long time) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk mm ss");
    return df.format(time);
  }
  
  public static String formatDateForFile2(long time) {
    SimpleDateFormat df = new SimpleDateFormat("MM-dd  kk mm");
    return df.format(time);
  }

  public static void main(String[] args) {
    Calendar cal = Calendar.getInstance();
    long t1 = cal.getTimeInMillis();
    // cal.add(Calendar.MINUTE, 13);
    cal.add(Calendar.MILLISECOND, 980);
    // cal.add(Calendar.DAY_OF_MONTH, 3);
    long t2 = cal.getTimeInMillis();
    System.out.println(fancyTime(t2 - t1));
    System.out.println(fancyTime2(t2 - t1));

    File f = new File(".");
    File[] files = f.listFiles();
    List<File> pingFiles = new ArrayList<File>(6);
    int cnt = 0;
    for (File file : files) {
      if (!file.isDirectory() && file.getName().startsWith("ping")) {
        System.err.println(file.getName() + " - " + file.lastModified());
        pingFiles.add(file);
        cnt++;
      }
    }

    if (cnt > 5) {
      // delete some files
      System.err.println();
      Collections.sort(pingFiles, new Comparator<File>() {
        public int compare(File o1, File o2) {
          if (o1.lastModified() > o2.lastModified())
            return 1;
          else if (o1.lastModified() < o2.lastModified())
            return -1;
          return 0;
        };
      });

      for (Iterator<File> it = pingFiles.iterator(); it.hasNext();) {
        File file = it.next();
        System.err.println(file.getName() + " " + file.lastModified());
      }
      int c = cnt - 5;
      for (int i = 0; i < c; i++) {
        File fd = pingFiles.get(i);
        fd.delete();
      }

    }

    File fn = new File("ping" + formatDateForFile(System.currentTimeMillis()) + ".png");
    try {
      fn.createNewFile();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
