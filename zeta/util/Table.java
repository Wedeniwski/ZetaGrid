/*--
  This file is a part of ZetaGrid, a simple and secure Grid Computing
  kernel.

  Copyright (c) 2001-2004 Sebastian Wedeniwski.  All rights reserved.

  Use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

  1. The source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

  2. The origin of this software must not be misrepresented; you must
     not claim that you wrote the original software.  If you plan to
     use this software in a product, please contact the author.

  3. Altered source versions must be plainly marked as such, and must
     not be misrepresented as being the original software. The author
     must be informed about these changes.

  4. The name of the author may not be used to endorse or promote
     products derived from this software without specific prior written
     permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  This program is based on the work of:
     H. Haddorp
     S. Wedeniwski
--*/

package zeta.util;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

/**
 *  A two dimensional array.
 *
 *  @version 1.9.2, April 12, 2004
**/
public class Table {
  public static final int LEFT   = -1;
  public static final int CENTER =  0;
  public static final int RIGHT  =  1;

  public Table(int columns) {
    columnNames = new String[columns];
    format      = new Format[columns];
    alignment   = new int[columns];
    rows        = new ArrayList();
    types       = new int[columns];
    precision   = new int[columns];
    scale       = new int[columns];
  }

  public Table(Table table, int rows) {
    columnNames = table.columnNames;
    format      = table.format;
    alignment   = table.alignment;
    this.rows   = new ArrayList(rows);
    types       = table.types;
    precision   = table.precision;
    scale       = table.scale;
    int l = Math.min((table != null)? table.rows.size() : 0, rows);
    for (int i = 0; i < l; ++i) {
      this.rows.add(table.rows.get(i));
    }
  }

  public void clear() {
    rows.clear();
  }

  public int getHiddenColumnCount() {
    return hiddenColumnCount;
  }

  public void setHiddenColumnCount(int hiddenColumnCount) {
    this.hiddenColumnCount = hiddenColumnCount;
  }

  public int getColumnCount() {
    return columnNames.length-hiddenColumnCount;
  }

  public int getRowCount() {
    return rows.size();
  }

  public String getColumnName(int index) {
    return columnNames[index];
  }

  public void setColumnName(int index, String name) {
    columnNames[index] = name;
  }

  public int getAlignment(int index) {
    return alignment[index];
  }

  public void setAlignment(int index, int alignment) {
    this.alignment[index] = alignment;
  }

  public int getType(int index) {
    return types[index];
  }

  public void setType(int index, int type) {
    types[index] = type;
  }

  public int getPrecision(int index) {
    return precision[index];
  }

  public void setPrecision(int index, int precision) {
    this.precision[index] = precision;
  }

  public int getScale(int index) {
    return scale[index];
  }

  public void setScale(int index, int scale) {
    this.scale[index] = scale;
  }

  public Format getFormat(int index) {
    return format[index];
  }

  public void setFormat(int index, Format format) {
    this.format[index] = format;
  }

  public void addRow() {
    rows.add(new Object[columnNames.length]);
  }

  public void addRow(int row) {
    rows.add(row, new Object[columnNames.length]);
  }

  public Object[] getRow(int row) {
    return (Object[])rows.get(row);
  }

  public Object getValue(int row, int col) {
    return ((Object[])rows.get(row))[col];
  }

  public Object[] getValues(int row, int[] columns) {
    Object[] result = new Object[columns.length];
    Object[] r = (Object[])rows.get(row);
    for (int i = 0; i < result.length; ++i) {
     result[i] = r[columns[i]];
    }
    return result;
  }

  public void setValue(int row, int col, Object value) {
    ((Object[])rows.get(row))[col] = value;
  }

  public void swapRows(int row1, int row2) {
    int l = rows.size();
    if (row1 < l && row2 < l) {
      Object t = rows.get(row1);
      rows.set(row1, rows.get(row2));
      rows.set(row2, t);
    }
  }

  public int indexOfColumn(String name) {
    for (int i = 0; i < columnNames.length; ++i) {
      if (name == null && columnNames[i] == null || name != null && name.equals(columnNames[i])) {
        return i;
      }
    }
    return -1;
  }

  public int indexOfColumnIgnoreCase(String name) {
    for (int i = 0; i < columnNames.length; ++i) {
      if (name == null && columnNames[i] == null || name != null && name.equalsIgnoreCase(columnNames[i])) {
        return i;
      }
    }
    return -1;
  }

  public int indexOfRow(Object o, int searchColumn) {
    return indexOfRow(o, searchColumn, 0);
  }

  public int indexOfRow(Object o, int searchColumn, int startIdx) {
    for (int l = rows.size(); startIdx < l; ++startIdx) {
      Object[] row = (Object[])rows.get(startIdx);
      if (o.equals(row[searchColumn])) {
        return startIdx;
      }
    }
    return -1;
  }

  public int indexOfRow(Object[] o, int[] searchColumns) {
    return indexOfRow(o, searchColumns, 0);
  }

  public int indexOfRow(Object[] o, int[] searchColumns, int startIdx) {
    for (int l = rows.size(); startIdx < l; ++startIdx) {
      Object[] row = (Object[])rows.get(startIdx);
      for (int i = 0; i < searchColumns.length; ++i) {
        if (o[i].equals(row[searchColumns[i]])) {
          return startIdx;
        }
      }
    }
    return -1;
  }

  public int indexOfRowIgnoreCase(String o, int searchColumn) {
    return indexOfRowIgnoreCase(o, searchColumn, 0);
  }

  public int indexOfRowIgnoreCase(String o, int searchColumn, int startIdx) {
    for (int l = rows.size(); startIdx < l; ++startIdx) {
      Object[] row = (Object[])rows.get(startIdx);
      if (o.equalsIgnoreCase((String)row[searchColumn])) {
        return startIdx;
      }
    }
    return -1;
  }

  public void insertColumn(int col) {
    int columns  = columnNames.length+1;
    String[] columnNames2 = new String[columns];
    Format[] format2 = new Format[columns];
    int[] alignment2 = new int[columns];
    int[] types2 = new int[columns];
    int[] precision2 = new int[columns];
    int[] scale2 = new int[columns];
    for (int i = 0; i < columns; ++i) {
      if (i != col) {
        int j = (i < col)? i : i-1;
        columnNames2[i] = columnNames[j];
        format2[i] = format[j];
        alignment2[i] = alignment[j];
        types2[i] = types[j];
        precision2[i] = precision[j];
        scale2[i] = scale[j];
      }
    }
    columnNames = columnNames2;
    format = format2;
    alignment = alignment2;
    types = types2;
    precision = precision2;
    scale = scale2;
    int l = rows.size();
    for (int i = 0; i < l; ++i) {
      Object[] o = (Object[])rows.get(i);
      Object[] o2 = new Object[columns];
      for (int j = 0; j < columns; ++j) {
        if (j != col) {
          o2[j] = o[(j < col)? j : j-1];
        }
      }
      rows.set(i, o2);
    }
  }

  public void addAndOrderByLastDesc(Table table, String[] uniqueColumnNames, String orderColumnName, boolean keepFirstRow) {
    int[] col1 = new int[uniqueColumnNames.length];
    int[] col2 = new int[uniqueColumnNames.length];
    for (int i = 0; i < col1.length; ++i) {
      col1[i] = indexOfColumnIgnoreCase(uniqueColumnNames[i]);
      col2[i] = table.indexOfColumnIgnoreCase(uniqueColumnNames[i]);
      if (col1[i] == -1 || col2[i] == -1) {
        return;
      }
    }
    int l = table.getRowCount();
    for (int i = 0; i < l; ++i) {
      int row = indexOfRow(table.getValues(i, col2), col1);
      if (row != -1) { // do not add a new row
        for (int j = table.getColumnCount()-1; j >= 0; --j) {
          int k = 0;
          while (k < col2.length && j != col2[k]) {
            ++k;
          }
          if (k == col2.length && (!keepFirstRow || j > 0)) {
            k = indexOfColumnIgnoreCase(table.getColumnName(j));
            if (k != -1) {
              Object o1 = getValue(row, k);
              Object o2 = table.getValue(i, j);
              if ((o1 instanceof Integer) && (o2 instanceof Integer)) {
                Integer n = new Integer(((Integer)o1).intValue()+((Integer)o2).intValue());
                setValue(row, k, n);
              } else if ((o1 instanceof Long) && (o2 instanceof Long)) {
                Long n = new Long(((Long)o1).longValue()+((Long)o2).longValue());
                setValue(row, k, n);
              } else if ((o1 instanceof Double) && (o2 instanceof Double)) {
                Double n = new Double(((Double)o1).doubleValue()+((Double)o2).doubleValue());
                setValue(row, k, n);
              }
            }
          }
        }
        int k = indexOfColumnIgnoreCase(orderColumnName);
        if (k >= 0 && (getValue(row, k) instanceof Number)) {
          Number n = (Number)getValue(row, k);
          while (row > 0) {
            Number n2 = (Number)getValue(--row, k);
            if (n instanceof Integer) {
              if (((Integer)n).compareTo(n2) <= 0) {
                break;
              }
            } else if (n instanceof Long) {
              if (((Long)n).compareTo(n2) <= 0) {
                break;
              }
            } else if (n instanceof Double) {
              if (((Double)n).compareTo(n2) <= 0) {
                break;
              }
            }
            swapRows(row, row+1);
            if (keepFirstRow) {
              Object t = getValue(row, 0);
              setValue(row, 0, getValue(row+1, 0));
              setValue(row+1, 0, t);
            }
          }
        }
      }
    }
  }

  private String[] columnNames;
  private Format[] format;
  private int[]    alignment;
  private List     rows;
  private int[]    types;
  private int[]    precision;
  private int[]    scale;
  private int      hiddenColumnCount = 0;
}
