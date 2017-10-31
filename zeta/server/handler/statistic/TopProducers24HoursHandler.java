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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Query;
import zeta.server.util.QueryWithSum;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'top producers 24 hours'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class TopProducers24HoursHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public TopProducers24HoursHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'top producers 24 hours'.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'top producers 24 hours'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    Map names = CachedQueries.getUserNames(con);
    StringBuffer buffer = new StringBuffer(70*1024);
    HtmlTableGenerator generator = new HtmlTableGeneratorWithSum(servlet, servlet.getRootPath() + servlet.getHandlerPath(TopProducersHandler.class), "user");
    Table table = new QueryWithSum(names, "user", "SELECT LOWER(c.name) AS \"user\","
                                                + " COUNT(*) AS \"work units\","
                                                + " COUNT(DISTINCT workstation_id) AS \"computers used\","
                                                + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                                                + " FROM zeta.computation a, zeta.result b, zeta.user c"
                                                + " WHERE b.stop IS NOT NULL"
                                                + " AND TIMESTAMPDIFF(4, CAST(CURRENT TIMESTAMP-b.stop AS CHAR(22)))<=1440"
                                                + " AND a.task_id=b.task_id"
                                                + " AND a.task_id=" + taskId
                                                + " AND a.work_unit_id=b.work_unit_id"
                                                + " AND a.user_id=c.id"
                                                + " AND a.server_id=c.server_id"
                                                + " GROUP BY LOWER(c.name) ORDER BY \"zeros\" DESC", true, con, servlet).getResult();
    // recomputed zeros
    Table table2 = new Query(names, "user", "SELECT LOWER(c.name) AS \"user\","
                                          + " COUNT(*) AS \"work units\","
                                          + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                                          + " FROM zeta.recomputation a, zeta.user c"
                                          + " WHERE a.stop IS NOT NULL"
                                          + " AND a.task_id=" + taskId
                                          + " AND TIMESTAMPDIFF(4, CAST(CURRENT TIMESTAMP-a.stop AS CHAR(22)))<=1440"
                                          + " AND a.user_id=c.id"
                                          + " AND a.server_id=c.server_id"
                                          + " GROUP BY LOWER(c.name) ORDER BY \"zeros\" DESC", true, con, servlet).getResult();
    table.addAndOrderByLastDesc(table2, new String[] { "user" }, "zeros", true);

    Table userDataTable = CachedQueries.getUserData(con);
    if (userDataTable != null && table.getRowCount() > 2 && table.getColumnCount() > 1) {
      String user = (String)table.getValue(0, 1);
      int userDataIdx = userDataTable.indexOfRow(user, 0);
      if (userDataIdx >= 0) {
        Object[] userData = userDataTable.getRow(userDataIdx);
        Properties properties = new Properties();
        if (userData[7] == null) {
          userData[7] = "";
        }
        try {
          properties.load(new ByteArrayInputStream(((String)userData[7]).getBytes()));
        } catch (IOException ioe) {
        }
        String s = properties.getProperty("homepage", "");
        if (s.length() > 0 || properties.getProperty("freetext", "").length() > 0) {
          buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Top producer '");
          buffer.append(user);
          buffer.append("'</center></td></tr>");
          buffer.append("<tr><td><br><center><table cellspacing=\"0\" width=\"90%\" border=\"1\"><colgroup><col width=\"30%\"><col width=\"70%\"></colgroup>");
          if (s.length() > 0) {
            buffer.append("<tr><td>Homepage&nbsp;&nbsp;</td><td><a href=\"");
            buffer.append(s);
            buffer.append("\">");
            buffer.append(s);
            buffer.append("</a></td></tr>");
          }
          s = properties.getProperty("freetext", "");
          if (s.length() > 0) {
            buffer.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"2\">");
            buffer.append(s);
            buffer.append("</td></tr>");
          }
          buffer.append("</table></center><p>");
        }
      }
    }
    buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Top producers of the last 24 hours:</center></td></tr>");
    buffer.append("<tr><td><br><center>");
    buffer.append(generator.generate(table));
    buffer.append("</center></td></tr>");
    if (table2.getRowCount() > 0) {
      QueryWithSum.addSum(table2);
      buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Recomputation of the last 24 hours:</center></td></tr>");
      buffer.append("<tr><td><br><center>");
      buffer.append(generator.generate(table2));
      buffer.append("</center></td></tr>");
    }
    return buffer.toString();
  }
}
