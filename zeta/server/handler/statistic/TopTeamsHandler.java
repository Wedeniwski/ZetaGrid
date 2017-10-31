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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Query;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'top teams'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class TopTeamsHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public TopTeamsHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'top teams'.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'top teams'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    StringBuffer buffer = new StringBuffer(50*1024);
    TeamMembersHandler handler = (TeamMembersHandler)servlet.getHandlerInstance(TeamMembersHandler.class);
    if (handler != null) {
      /*Table table = new Query(names, "SELECT LOWER(RTRIM(b.team_name)) AS \"team\","
                                   + " DAYS(CURRENT TIMESTAMP)-DAYS(MIN(b.join_in_team)) AS \"age (days)\","
                                   + " COUNT(*) AS \"work units\","
                                   + " COUNT(DISTINCT LOWER(b.name)) AS \"members\","
                                   + " COUNT(DISTINCT LOWER(hostname)) AS \"computers used\","
                                   + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                                   + " FROM zeta.computation a,zeta.user b,zeta.workstation c,zeta.result r"
                                   + " WHERE r.task_id=a.task_id"
                                   + " AND a.task_id=" + taskId
                                   + " AND r.work_unit_id=a.work_unit_id"
                                   + " AND a.user_id=b.id"
                                   + " AND a.server_id=b.server_id"
                                   + " AND a.workstation_id=c.id"
                                   + " AND a.server_id=c.server_id"
                                   + " AND NOT b.team_name IS NULL"
                                   + " AND LENGTH(RTRIM(b.team_name))>0"
                                   + " GROUP BY LOWER(RTRIM(b.team_name)) ORDER BY \"zeros\" DESC", true, null, con, servlet).getResult();*/
      Table table = handler.getTopTeamTable(taskId);
      if (table == null) {
        handler.createPage(taskId, con);
        table = handler.getTopTeamTable(taskId);
      }
      if (table != null) {
        buffer.append("<tr><td><br><center>");
        HtmlTableGenerator generator = new HtmlTableGenerator(servlet, servlet.getRootPath() + servlet.getHandlerPath(TeamMembersHandler.class), "team");
        if (table.getRowCount() > 0) {
          String team = (String)table.getValue(0, 0);
          Statement stmt = null;
          try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT properties,join_in_team FROM zeta.user WHERE LOWER(RTRIM(team_name))='" + team + "' ORDER BY join_in_team FETCH FIRST 1 ROWS ONLY");
            if (rs.next()) {
              String s = rs.getString(1);
              if (s != null) {
                buffer.append("<p>&nbsp;<br><table>");
                Properties properties = new Properties();
                try {
                  properties.load(new ByteArrayInputStream(s.getBytes()));
                } catch (IOException ioe) {
                }
                s = properties.getProperty("homepage", "");
                if (s.length() > 0 && properties.getProperty("team.homepage", "").equals("true")) {
                  buffer.append("<tr><td>Homepage:</td><td><a href=\"");
                  buffer.append(s);
                  buffer.append("\">");
                  buffer.append(s);
                  buffer.append("</a></td></tr>");
                }
                s = properties.getProperty("teamintroduction", "");
                if (s.length() > 0) {
                  buffer.append("<tr><td colspan=\"2\"><textarea col=\"60\" rows=\"");
                  buffer.append((s.length()+59)/60);
                  buffer.append("\" readonly>");
                  buffer.append(s);
                  buffer.append("</textarea></td></tr>");
                }
                buffer.append("</table><p>");
              }
            }
            rs.close();
          } catch (SQLException e) {
            throw new ServletException(e);
          } finally {
            DatabaseUtils.close(stmt);
          }
        }
        buffer.append(generator.generate(table));
        buffer.append("</center></td></tr>");
      }
    }
    return buffer.toString();
  }
}
