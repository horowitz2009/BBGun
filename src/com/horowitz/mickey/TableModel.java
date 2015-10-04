package com.horowitz.mickey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.horowitz.mickey.data.Contractor;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.data.Need;
import com.horowitz.mickey.data.Objective;

class TableModel extends AbstractTableModel {

  Map<String, Map<String, Need>> _map;

  private List<Contractor>       _contractors;

  private Material[]             _allmats;

  private String[]               _other;
  private String[]               _rows;

  public TableModel(Map<String, Map<String, Need>> map) {
    super();
    _map = map;
    _allmats = Material.createArray();
    _other = Material.OTHER;
    if (_map != null) {
      List<String> rows = new ArrayList<>();
      for (int i = 0; i < _other.length; i++) {
        if (_map.containsKey(_other[i])) {
          rows.add(_other[i]);
        }
      }
      for (int i = 0; i < _allmats.length; i++) {
        if (_map.containsKey(_allmats[i].getName())) {
          rows.add(_allmats[i].getName());
        }
      }

      _rows = rows.toArray(new String[0]);
    }

    try {
      _contractors = new DataStore().getActiveContractors();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public int getColumnCount() {
    if (_contractors == null)
      return 1;
    return _contractors.size() + 1;
  }

  @Override
  public int getRowCount() {
    if (_rows == null)
      return 0;
    return _rows.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object value = null;
    if (_rows == null || _contractors == null || _map == null)
      return value;

    String mat = _rows[rowIndex];
    if (columnIndex == 0) {
      value = mat;
    } else {
      Long v1 = 0l;
      Long v2 = 0l;
      Contractor contractor = _contractors.get(columnIndex - 1);
      Map<String, Need> innerMap = _map.get(mat);

      Need need = innerMap.get(contractor.getName());
      if (need != null) {
        Objective o = need.getObjective();
        v1 = (o.getNeededAmount() - o.getCurrentAmount());
        // NumberFormat nf = NumberFormat.getIntegerInstance();
        // value = nf.format(value);
      }
      if (contractor.getMaterialsMore() != null) {
        for (Material mm : contractor.getMaterialsMore()) {
          if (mm.getName().equals(mat)) {
            v2 = mm.getAmount();
            break;
          }
        }
      }

      if (value == null)
        value = 0l;
      if (v2 == null) {
        v2 = 0l;
      }
      // NumberFormat nf = NumberFormat.getIntegerInstance();
      // String sv = nf.format(v2);

      value = v1 + ":" + v2;
    }
    return value;
  }

  @Override
  public String getColumnName(int column) {
    if (column == 0) {
      return "";
    }
    Contractor contractor = _contractors.get(column - 1);
    String s = contractor.getAccepts();
    if (s == null) {
      s = "";
    } else {
      s = s.substring(s.length() - 1).toLowerCase();
      s = "(" + s + ")";
    }
    return contractor.getName() + "  " + s;
  }

}