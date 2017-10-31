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

package zeta.server.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import zeta.server.DispatcherServlet;
import zeta.util.Table;
import zeta.util.ThrowableHandler;

/**
 *  Generate a table for a SQL statement.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class Query {
  protected Table table;
  protected DispatcherServlet servlet;

  public Query(String query, Connection con, DispatcherServlet servlet) {
    this.servlet = servlet;
    table = executeQuery(null, null, query, false, con);
  }

  public Query(Map names, String namesColumnName, String query, boolean viewPlace, Connection con, DispatcherServlet servlet) {
    this.servlet = servlet;
    table = executeQuery(names, namesColumnName, query, viewPlace, con);
  }

  public Table getResult() {
    return table;
  }

  protected Table executeQuery(Map names, String namesColumnName, String query, boolean viewPlace, Connection con) {
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      ResultSetMetaData rsmd = rs.getMetaData();
      int columns = rsmd.getColumnCount();
      while (columns > 0) {
        String s = rsmd.getColumnName(columns);
        if (s != null && s.length() > 0 && !Character.isDigit(s.charAt(0))) {
          break;
        }
        --columns;
      }
      if (viewPlace) {
        ++columns;
      }
      Table table = new Table(columns);
      int colNames = columns;
      for (int i = 0, idx = 1; i < columns; ++i) {
        int type = 0;
        if (i == 0 && viewPlace) {
          type = Types.CHAR;
          table.setColumnName(0, "place");
        } else {
          String columnName = rsmd.getColumnName(idx);
          if (columnName.equals(namesColumnName)) {
            colNames = i;
          }
          table.setColumnName(i, columnName);
          table.setPrecision(i, rsmd.getPrecision(idx));
          table.setScale(i, rsmd.getScale(idx));
          type = rsmd.getColumnType(idx);
          ++idx;
        }
        table.setType(i, type);
        switch (type) {
          case Types.DATE       :
          case Types.DECIMAL    :
          case Types.DOUBLE     :
          case Types.FLOAT      :
          case Types.INTEGER    :
          case Types.NUMERIC    :
            table.setAlignment(i, Table.RIGHT);
            break;
          case Types.TIME       :
          case Types.TIMESTAMP  :
            table.setAlignment(i, Table.CENTER);
            break;
          case Types.CHAR       :
          case Types.LONGVARCHAR:
          case Types.VARCHAR    :
            table.setAlignment(i, Table.LEFT);
            break;
        }
      }
      for (int row = 0; rs.next(); ++row) {
        table.addRow();
        for (int col = 0, idx = 1, l = table.getColumnCount(); col < l; ++col) {
          Object value = null;
          if (col == 0 && viewPlace) {
            value = Integer.toString(row+1) + '.';
          } else if (col == colNames) {
            value = (names == null)? rs.getString(idx) : names.get(rs.getString(idx));
            ++idx;
          } else {
            switch (table.getType(col)) {
              case Types.DATE:
                value = toString(rs.getDate(idx));
                break;
              case Types.DECIMAL:
              case Types.NUMERIC:
                if (table.getScale(col) > 0) {
                  value = new Double(rs.getDouble(idx));
                } else {
                  value = new Long(rs.getLong(idx));
                }
                break;
              case Types.DOUBLE:
              case Types.FLOAT:
                value = new Double(rs.getDouble(idx));
                break;
              case Types.BIGINT:
                value = new Long(rs.getLong(idx));
                break;
              case Types.INTEGER:
                value = new Integer(rs.getInt(idx));
                break;
              case Types.SMALLINT:
              case Types.TINYINT:
                value = new Short(rs.getShort(idx));
                break;
              case Types.TIME:
                value = rs.getTime(idx);
                break;
              case Types.TIMESTAMP:
                value = rs.getTimestamp(idx);
                break;
              case Types.CHAR:
              case Types.LONGVARCHAR:
              case Types.VARCHAR:
                value = rs.getString(idx);
                break;
              default:
                value = rs.getString(idx);
            }
            ++idx;
          }
          table.setValue(row, col, value);
        }
      }
      rs.close();
      return table;
    } catch (SQLException e) {
      servlet.log(query);
      servlet.log(e);
    } finally {
      DatabaseUtils.close(stmt);
    }
    return null;
  }

  protected String toString(java.sql.Date date) {
    return (date != null)? dateFormater.format(date) : null;
  }

  static DateFormat dateFormater = new SimpleDateFormat("MM/dd/yyyy");
}
