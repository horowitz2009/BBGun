package com.horowitz.mickey;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class Locations {

  public static int                RAIL1           = 100;
  /**
   * @deprecated
   */
  public static int[]              RAILS2          = { RAIL1, RAIL1 + 8 };
  /**
   * @deprecated
   */
  public static int[]              RAILS4A         = { RAIL1, RAIL1 + 4, RAIL1 + 7, RAIL1 + 11 };                                                 // home
                                                                                                                                                   // and
                                                                                                                                                   // mahatma
                                                                                                                                                   // small
  /**
   * @deprecated
   */
  public static int[]              RAILS4_WOLF     = { RAIL1 - 6, RAIL1 - 2, RAIL1 + 3, RAIL1 + 8 };                                              // wolfgang
                                                                                                                                                   // +
                                                                                                                                                   // maglev
                                                                                                                                                   // small
  /**
   * @deprecated
   */
  public static int[]              RAILS4_SMIX     = { RAIL1 - 2, RAIL1, RAIL1 + 3, RAIL1 + 8, RAIL1 + 11, RAIL1 + 16, RAIL1 + 21, RAIL1 + 26,
      RAIL1 + 31                                  };                                                                                             // small
                                                                                                                                                   // mix

  /**
   * @deprecated
   */
  public static int[]              RAILS4          = { RAIL1, RAIL1 + 7, RAIL1 + 13, RAIL1 + 20 };                                                // home
                                                                                                                                                   // and
                                                                                                                                                   // mahatma
                                                                                                                                                   // big
  /**
   * @deprecated
   */
  public static int[]              RAILS4_WOLF_BIG = { RAIL1 + 27, RAIL1 + 37, RAIL1 + 46, RAIL1 + 56 };                                          // wolfgang
                                                                                                                                                   // +
                                                                                                                                                   // maglev
                                                                                                                                                   // small
  /**
   * @deprecated
   */
  public static int[]              RAILS4_BMIX     = { RAIL1, RAIL1 + 7, RAIL1 + 13, RAIL1 + 20, RAIL1 + 27, RAIL1 + 37, RAIL1 + 46, RAIL1 + 56 };

  /**
   * @deprecated
   */
  public static float              RAIL_Y_OFFSET   = 4f;
  /**
   * @deprecated
   */
  public static int[]              RAILS6OLD       = { RAIL1, RAIL1 + 4, RAIL1 + 8, RAIL1 + 12, RAIL1 + 19, RAIL1 + 23 };
  /**
   * @deprecated
   */
  public static int[]              RAILS6          = { RAIL1 - 6, RAIL1 - 2, RAIL1 + 3, RAIL1 + 8, RAIL1 + 15, RAIL1 + 19 };                      // wolfgang
                                                                                                                                                   // +
                                                                                                                                                   // maglev
  /**
   * @deprecated
   */
  public static int                STREET2         = 132;
  /**
   * @deprecated
   */
  public static int                STREET2HARBOUR  = 125;
  /**
   * @deprecated
   */
  public static int                STREET4         = 128;
  /**
   * @deprecated
   */
  public static int                STREET6         = 143;
  /**
   * @deprecated
   */
  public static int                STREET6THEME    = 148;

  public static Location           LOC_0MIN        = new Location(" 0 ", 0, new Point(-196, 487), 1);
  
  public static Location           LOC_6MIN        = new Location(" 6m ", 6, new Point(85, 447), 1);
  public static Location           LOC_10MIN       = new Location(" 10m ", 10, new Point(263, 447), 1);
  public static Location           LOC_30MIN       = new Location(" 30m ", 30, new Point(441, 447), 1);
  public static Location           LOC_1HOUR       = new Location(" 1h ", 60, new Point(619, 447), 1);
  
  public static Location           LOC_2HOURS      = new Location(" 2h ", 120, new Point(85, 447), 2);
  public static Location           LOC_3HOURS      = new Location(" 3h ", 180, new Point(263, 447), 2);
  public static Location           LOC_4HOURS      = new Location(" 4h ", 4 * 60, new Point(441, 447), 2);
  public static Location           LOC_6HOURS      = new Location(" 6h ", 6 * 60, new Point(619, 447), 2);
  
  public static Location           LOC_8HOURS      = new Location(" 8h ", 8 * 60, new Point(85, 447), 3);
  public static Location           LOC_10HOURS     = new Location("10h", 10 * 60, new Point(263, 447), 3);
  public static Location           LOC_12HOURS     = new Location("12h", 12 * 60, new Point(441, 447), 3);
  public static Location           LOC_18HOURS     = new Location("18h", 18 * 60, new Point(619, 447), 3);

  public static Location           LOC_1DAY        = new Location(" 1d ", 24 * 60, new Point(263, 447), 4);
  public static Location           LOC_2DAYS       = new Location(" 2d ", 48 * 60, new Point(441, 447), 4);
  public static Location           LOC_1WEEK       = new Location(" 1w ", 168 * 60, new Point(619, 447), 4);

  public static Location[]         LOC_PAGE_F1     = new Location[] { LOC_0MIN, LOC_6MIN, LOC_10MIN, LOC_30MIN, LOC_1HOUR };
  public static Location[]         LOC_PAGE_F2     = new Location[] { LOC_2HOURS, LOC_3HOURS, LOC_4HOURS, LOC_8HOURS, LOC_10HOURS, LOC_12HOURS };
  public static Location[]         LOC_PAGE_E1     = new Location[] { LOC_0MIN, LOC_6MIN, LOC_10MIN, LOC_30MIN, LOC_1HOUR };
  public static Location[]         LOC_PAGE_E2     = new Location[] { LOC_2HOURS, LOC_3HOURS, LOC_4HOURS, LOC_8HOURS, LOC_10HOURS, LOC_12HOURS, };

  public static Location[]         ALL             = new Location[] { LOC_0MIN, LOC_6MIN, LOC_10MIN, LOC_30MIN, LOC_1HOUR, LOC_2HOURS, LOC_3HOURS,
      LOC_4HOURS, LOC_6HOURS, LOC_8HOURS, LOC_10HOURS, LOC_12HOURS, LOC_18HOURS, };

  public static MaterialLocation   MAT_1_1         = new MaterialLocation("Gold", "Wood",    new Rectangle( 46, 140, 156, 28), 1);
  public static MaterialLocation   MAT_1_2         = new MaterialLocation("Wood", "Nails",   new Rectangle(222, 140, 156, 28), 1);
  public static MaterialLocation   MAT_1_3         = new MaterialLocation("Nails", "Bricks", new Rectangle(398, 140, 156, 28), 1);
  public static MaterialLocation   MAT_1_4         = new MaterialLocation("Bricks", "Glass", new Rectangle(574, 140, 156, 28), 1);

  public static MaterialLocation   MAT_2_1         = new MaterialLocation("Glass", "Fuel",   new Rectangle( 46, 261, 156, 28), 1);
  public static MaterialLocation   MAT_2_2         = new MaterialLocation("Fuel", "Steel",   new Rectangle(222, 261, 156, 28), 1);
  public static MaterialLocation   MAT_2_3         = new MaterialLocation("Steel", "Gravel", new Rectangle(398, 261, 156, 28), 1);
  public static MaterialLocation   MAT_2_4         = new MaterialLocation("Gravel", "U-235", new Rectangle(574, 261, 156, 28), 1);

  public static MaterialLocation   MAT_3_1         = new MaterialLocation("U-235", "Cement",    new Rectangle( 46, 382, 156, 28), 1);
  public static MaterialLocation   MAT_3_2         = new MaterialLocation("Cement", "Rubber",   new Rectangle(222, 382, 156, 28), 1);
  public static MaterialLocation   MAT_3_3         = new MaterialLocation("Rubber", "Carbon",   new Rectangle(398, 382, 156, 28), 1);
  public static MaterialLocation   MAT_3_4         = new MaterialLocation("Carbon", "Titanium", new Rectangle(574, 382, 156, 28), 1);

  public static MaterialLocation   MAT_4_1         = new MaterialLocation("Titanium", "Marble",  new Rectangle( 46, 503, 156, 28), 1);
  public static MaterialLocation   MAT_4_2         = new MaterialLocation("Marble", "Wires",     new Rectangle(222, 503, 156, 28), 1);
  public static MaterialLocation   MAT_4_3         = new MaterialLocation("Wires", "Plastics",   new Rectangle(398, 503, 156, 28), 1);
  public static MaterialLocation   MAT_4_4         = new MaterialLocation("Plastics", "Silicon", new Rectangle(574, 503, 156, 28), 1);

  public static MaterialLocation   MAT_1_1_2       = new MaterialLocation("Silicon", "Silicon", new Rectangle( 46, 140, 156, 28), 2);

  public static MaterialLocation[] MATERIALS_1     = new MaterialLocation[] { MAT_1_1, MAT_1_2, MAT_1_3, MAT_1_4, MAT_2_1, MAT_2_2, MAT_2_3, MAT_2_4,
      MAT_3_1, MAT_3_2, MAT_3_3, MAT_3_4, MAT_4_1, MAT_4_2, MAT_4_3, MAT_4_4, };
  public static MaterialLocation[] MATERIALS_2     = new MaterialLocation[] { MAT_1_1_2 };

  /**
   * @deprecated
   */
  public static Dimension          ARROW_LR_SIZE   = new Dimension(65, 33);

  // this is minimum size. DO NOT USE IT DIRECTLY!
  /**
   * @deprecated
   */
  public static Dimension          GAME_SIZE       = new Dimension(775, 585);

  /**
   * @deprecated
   */
  public static Rectangle          LEFTARROW_ZONE  = new Rectangle(0, -RAIL1 - ARROW_LR_SIZE.height * 3, ARROW_LR_SIZE.width + 45,
                                                       ARROW_LR_SIZE.height * 3);

  /**
   * @deprecated
   */
  public static Rectangle getAbsoluteArea(int x, int y, Rectangle area) {
    Rectangle res = new Rectangle(x + area.x, y + area.y, area.width, area.height);
    return res;
  }

}
