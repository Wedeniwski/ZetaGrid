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
     S. Wedeniwski
--*/

package zeta.server.handler.statistic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.DatabaseUtils;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'close zeros'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class CloseZerosHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public CloseZerosHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'close zeros'.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'close zeros'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    List closeZeros = new ArrayList(100);
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT c.work_unit_id,c.start,r.stop,u.name,f.found"
                                     + " FROM zeta.computation c,zeta.result r,zeta.user u,zeta.found f"
                                     + " WHERE c.task_id=" + taskId
                                     + " AND c.task_id=r.task_id"
                                     + " AND c.task_id=f.task_id"
                                     + " AND c.work_unit_id=r.work_unit_id"
                                     + " AND c.work_unit_id=f.work_unit_id"
                                     + " AND c.user_id=u.id"
                                     + " AND c.server_id=u.server_id"
                                     + " AND f.type='close zeros'"
                                     + " AND f.approved_YN='Y'");
      while (rs.next()) {
        String found = rs.getString(5);
        final int l = found.length();
        int i = 0;
        while (i < l && !Character.isDigit(found.charAt(i))) {
          ++i;
        }
        int j = found.indexOf(' ', i);
        if (i > 0 && j > i) {
          BigDecimal t1 = new BigDecimal(found.substring(i, j));
          for (i = j; i < l && !Character.isDigit(found.charAt(i)); ++i);
          j = found.indexOf(' ', i);
          if (i > 0 && j > i) {
            BigDecimal t2 = new BigDecimal(found.substring(i, j));
            t2 = t2.subtract(t1);
            t2 = t2.add(new BigDecimal(0.000000005));
            i = found.indexOf("This happened at ", j);
            if (i > 0) {
              final Object[] obj = { t2, new Long(rs.getLong(1)), rs.getTimestamp(2), rs.getTimestamp(3), rs.getString(4), new Long(found.substring(i+17)) };
              closeZeros.add(obj);
            }
          }
        }
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
    }
    HtmlTableGenerator generator = new HtmlTableGenerator(servlet, servlet.getRootPath() + servlet.getHandlerPath(TopProducersHandler.class), "user");
    Table table = new Table(6);
    table.setColumnName(0, "place");
    table.setType(0, Types.VARCHAR);
    table.setAlignment(0, Table.LEFT);
    table.setColumnName(1, "user");
    table.setType(1, Types.VARCHAR);
    table.setAlignment(1, Table.LEFT);
    table.setColumnName(2, "gram point");
    table.setType(2, Types.INTEGER);
    table.setAlignment(2, Table.RIGHT);
    table.setColumnName(3, "work unit requested");
    table.setType(3, Types.TIMESTAMP);
    table.setAlignment(3, Table.CENTER);
    table.setFormat(3, new SimpleDateFormat("MM/dd/yyyy"));
    table.setColumnName(4, "work unit delivered");
    table.setType(4, Types.TIMESTAMP);
    table.setAlignment(4, Table.CENTER);
    table.setFormat(4, new SimpleDateFormat("MM/dd/yyyy"));
    table.setColumnName(5, "distance less than");
    table.setType(5, Types.DOUBLE);
    table.setAlignment(5, Table.RIGHT);
    table.setFormat(5, new DecimalFormat("0.00000000"));
    Object[] closeZerosArray = closeZeros.toArray();
    Arrays.sort(closeZerosArray, new Comparator() {
        public int compare(Object o1, Object o2) {
          int cmp = ((BigDecimal)((Object[])o1)[0]).compareTo(((Object[])o2)[0]);
          return (cmp == 0)? ((Long)((Object[])o1)[1]).compareTo(((Object[])o2)[1]) : cmp;
        }

        public boolean equals(Object obj) {
          return (((Object[])(Object)this)[0].equals(((Object[])obj)[0]) && ((Object[])(Object)this)[1].equals(((Object[])obj)[1]));
        }
      }
    );
    int row = 0;
    for (int i = 0; i < closeZerosArray.length; ++i) {
      Object[] obj = (Object[])closeZerosArray[i];
      table.addRow();
      table.setValue(row, 0, String.valueOf(row+1)+'.');
      table.setValue(row, 1, obj[4]);
      table.setValue(row, 2, obj[5]);
      table.setValue(row, 3, obj[2]);
      table.setValue(row, 4, obj[3]);
      table.setValue(row, 5, obj[0]);
      ++row;
    }
    StringBuffer buffer = new StringBuffer(100*1024);
    buffer.append("<tr><td>Only close zeros with a distance less than 0.0002 are stored in this list.");
    buffer.append(" To learn more about close zeros, see <a href=\"http://mathworld.wolfram.com/LehmersPhenomenon.html\">Lehmer's Phenomenon</a>.");
    buffer.append("<br>At moment this list is not completed because only a ZetaGrid library version 1.32 or above (released at Nov 9, 2002)");
    buffer.append(" evaluates close zeros and stores them directly.");
    buffer.append(" All other close zeros that have been found by previous ZetaGrid library versions are currently being analyzed.");
    buffer.append(" It will take some months until all results will be reflected in this list (status currently: less than 400 billion zeros about 99.4%, above 400 billion zeros about 77%).");
    buffer.append("<br>The close zero at 1,048,449,114 was found in previous computation by J. van de Lune, H. J. J. te Riele and D. T. Winter  (1986).");
    buffer.append("<br>The close zero at 3,570,918,900 was found in previous computation by J. van de Lune (Sep 24, 2000).");
    buffer.append("<tr><td>");
    buffer.append("<p><center>");
    buffer.append(generator.generate(table));
    buffer.append("</center></td></tr>");
    return buffer.toString();
  }
}
