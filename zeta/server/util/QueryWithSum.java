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
import java.util.Map;

import zeta.server.DispatcherServlet;
import zeta.util.Table;

/**
 *  Generate a table with an addition summary row for a SQL statement.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class QueryWithSum extends Query {
  public QueryWithSum(String query, Connection con, DispatcherServlet servlet) {
    super(query, con, servlet);
  }

  public QueryWithSum(Map names, String namesColumnName, String query, boolean viewPlace, Connection con, DispatcherServlet servlet) {
    super(names, namesColumnName, query, viewPlace, con, servlet);
  }

  /**
   *  Add a summation row in the table.
   *  @param table table where a summation row will be added
  **/
  public static void addSum(Table table) {
    if (table != null) {
      table.addRow();
      table.addRow();
      int rows = table.getRowCount();
      table.setValue(rows - 1, 0, "&sum;&nbsp;&nbsp;&nbsp;" + (rows-2));  // HTML 4.0
      //table.setValue(rows - 1, 0, "<span style=\"font-family:'symbol'\">S</span>&nbsp;&nbsp;&nbsp;" + (rows-2));
      for (int col = 1; col < table.getColumnCount(); ++col) {
        Object o = null;
        for (int row = 0; row < rows - 1; ++row) {
          Object value = table.getValue(row, col);
          if (value != null) {
            if (o == null) {
              o = value;
            } else if (!o.getClass().isInstance(value)) {
              o = null;
              break;
            }
          }
        }
        if (o instanceof String) {
          table.setValue(rows - 1, col, "&nbsp;");
        } else if (o instanceof Integer) {
          int sum = 0;
          for (int row = 0; row < rows - 1; ++row) {
            Integer value = (Integer)table.getValue(row, col);
            if (value != null) {
              sum += value.intValue();
            }
          }
          table.setValue(rows - 1, col, new Integer(sum));
        } else if (o instanceof Long) {
          long sum = 0;
          for (int row = 0; row < rows - 1; ++row) {
            Long value = (Long)table.getValue(row, col);
            if (value != null) {
              sum += value.longValue();
            }
          }
          table.setValue(rows - 1, col, new Long(sum));
        }
      }
    }
  }

  protected Table executeQuery(Map names, String namesColumnName, String query, boolean viewPlace, Connection con) {
    Table table = super.executeQuery(names, namesColumnName, query, viewPlace, con);
    addSum(table);
    return table;
  }
}
