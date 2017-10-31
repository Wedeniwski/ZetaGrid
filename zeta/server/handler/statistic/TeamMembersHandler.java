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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import zeta.server.DispatcherServlet;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.QueryWithSum;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'team members'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class TeamMembersHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public TeamMembersHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Returns the complete top team statistic.
   *  @param taskId task identifier
   *  @return the complete top team statistic.
  **/
  Table getTopTeamTable(int taskId) {
    return (Table)teamTables.get(new Integer(taskId));
  }

  /**
   *  Creates HTML page with the content of the statistic 'team members'
   *  where the user is not defined.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'team members'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    StringBuffer buffer = new StringBuffer(100*1024);
    Map teamNames = CachedQueries.getTeamNames(con);
    Map userNames = CachedQueries.getUserNames(con);
    buffer.append("<tr><td><br><center>");
    HtmlTableGenerator generator = new HtmlTableGenerator(servlet, servlet.getRootPath() + servlet.getHandlerPath(TopProducersHandler.class), "user");

    /*Table table = new QueryWithSum(names, "SELECT LOWER(RTRIM(b.team_name)) AS \"team\","
                                        + " b.name AS \"user\","
                                        + " MIN(b.join_in_team) AS \"joined\","
                                        + " COUNT(*) AS \"work units\","
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
                                        + " GROUP BY LOWER(RTRIM(b.team_name)),b.name ORDER BY LOWER(RTRIM(b.team_name)),MIN(b.join_in_team),\"zeros\" DESC", false, null, con, servlet).getResult();*/
    Table table = new Table(6);
    table.setColumnName(0, "team");
    table.setType(0, Types.VARCHAR);
    table.setAlignment(0, Table.LEFT);
    table.setColumnName(1, "user");
    table.setType(1, Types.VARCHAR);
    table.setAlignment(1, Table.LEFT);
    table.setColumnName(2, "joined");
    table.setType(2, Types.TIMESTAMP);
    table.setAlignment(2, Table.CENTER);
    table.setFormat(2, new SimpleDateFormat("MM/dd/yyyy"));
    table.setColumnName(3, "work units");
    table.setType(3, Types.INTEGER);
    table.setAlignment(3, Table.RIGHT);
    table.setColumnName(4, "computers used");
    table.setType(4, Types.INTEGER);
    table.setAlignment(4, Table.RIGHT);
    table.setColumnName(5, "zeros");
    table.setType(5, Types.INTEGER);
    table.setAlignment(5, Table.RIGHT);

    // generate team table simultaneously
    Table teamTable = new Table(7);
    teamTable.setColumnName(0, "place");
    teamTable.setType(0, Types.VARCHAR);
    teamTable.setAlignment(0, teamTable.LEFT);
    teamTable.setColumnName(1, "team");
    teamTable.setType(1, Types.VARCHAR);
    teamTable.setAlignment(1, teamTable.LEFT);
    teamTable.setColumnName(2, "age (days)");
    teamTable.setType(2, Types.INTEGER);
    teamTable.setAlignment(2, teamTable.RIGHT);
    teamTable.setColumnName(3, "work units");
    teamTable.setType(3, Types.INTEGER);
    teamTable.setAlignment(3, teamTable.RIGHT);
    teamTable.setColumnName(4, "members");
    teamTable.setType(4, Types.INTEGER);
    teamTable.setAlignment(4, teamTable.RIGHT);
    teamTable.setColumnName(5, "computers used");
    teamTable.setType(5, Types.INTEGER);
    teamTable.setAlignment(5, teamTable.RIGHT);
    teamTable.setColumnName(6, "zeros");
    teamTable.setType(6, Types.INTEGER);
    teamTable.setAlignment(6, teamTable.RIGHT);

    Statement stmt = null;
    try {
      String previousTeamname = "";
      int age = 0;
      int workUnits = 0;
      int members = 0;
      int computersUsed = 0;
      long zeros = 0;
      stmt = con.createStatement();
      long maxDays = CachedQueries.getMaxDays(taskId, stmt);
      ResultSet rs = stmt.executeQuery("SELECT LOWER(RTRIM(b.team_name)) AS \"team\","
                                     + " LOWER(b.name) AS \"user\","
                                     + " MIN(b.join_in_team) AS \"joined\""
                                     + " FROM zeta.computation a,zeta.user b,zeta.result r"
                                     + " WHERE r.task_id=a.task_id"
                                     + " AND a.task_id=" + taskId
                                     + " AND r.work_unit_id=a.work_unit_id"
                                     + " AND a.user_id=b.id"
                                     + " AND a.server_id=b.server_id"
                                     + " AND NOT b.team_name IS NULL"
                                     + " AND LENGTH(RTRIM(b.team_name))>0"
                                     + " GROUP BY LOWER(RTRIM(b.team_name)),LOWER(b.name) ORDER BY LOWER(RTRIM(b.team_name)),MIN(b.join_in_team)");
      while (rs.next()) {
        String teamname = rs.getString(1);
        String name = rs.getString(2);
        Timestamp t = rs.getTimestamp(3);
        Object[] o = CachedQueries.getUserData(taskId, name, teamname);
        if (!teamname.equals(previousTeamname)) {
          insertTeam(teamTable, (String)teamNames.get(previousTeamname), age, workUnits, members, computersUsed, zeros);
          previousTeamname = teamname;
          age = 0;
          workUnits = 0;
          members = 0;
          computersUsed = 0;
          zeros = 0;
        }
        int row = table.getRowCount();
        table.addRow();
        table.setValue(row, 0, teamNames.get(teamname));
        table.setValue(row, 1, userNames.get(name));
        table.setValue(row, 2, t);
        table.setValue(row, 3, o[1]);
        table.setValue(row, 4, o[0]);
        table.setValue(row, 5, o[2]);
        if (age == 0) {
          age = (int)(maxDays-DatabaseUtils.convertTimeMillisToDays(t.getTime()));
        }
        workUnits += ((Integer)o[1]).intValue();
        ++members;
        computersUsed += ((Integer)o[0]).intValue();
        zeros += ((Long)o[2]).longValue();
      }
      rs.close();
      insertTeam(teamTable, (String)teamNames.get(previousTeamname), age, workUnits, members, computersUsed, zeros);
      for (int place = 1, l = teamTable.getRowCount(); place <= l; ++place) {
        teamTable.setValue(place-1, 0, String.valueOf(place)+'.');
      }
    } finally {
      DatabaseUtils.close(stmt);
    }
    teamTables.put(new Integer(taskId), teamTable);
    buffer.append(generator.generate(table));

    teamTable = new Table(2);
    teamTable.setAlignment(0, Table.LEFT);
    teamTable.setAlignment(1, Table.LEFT);
    Table userDataTable = CachedQueries.getUserData(con);
    if (userDataTable != null) {
      int l = table.getRowCount()-2;
      String lastTeamName = "";
      for (int i = 0; i < l; ++i) {
        String teamName = (String)table.getValue(i, 0);
        if (!lastTeamName.equals(teamName)) {
          lastTeamName = teamName;
          String teamhomepage = null;
          String teamintroduction = null;
          while (i < l && (teamhomepage == null || teamintroduction == null)) {
            teamName = (String)table.getValue(i, 0);
            if (!lastTeamName.equals(teamName)) {
              --i;
              break;
            }
            String user = (String)table.getValue(i, 1);
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
              if (teamhomepage == null) {
                String s = properties.getProperty("homepage", "");
                if (s.length() > 0 && properties.getProperty("teamhomepage", "").equals("true")) {
                  teamhomepage = s;
                }
              }
              if (teamintroduction == null) {
                String s = properties.getProperty("teamintroduction", "");
                if (s.length() > 0) {
                  teamintroduction = s;
                }
              }
            }
            ++i;
          }
          if (teamhomepage != null || teamintroduction != null) {
            Properties properties = new Properties();
            if (teamhomepage != null) {
              properties.setProperty("teamhomepage", "true");
              properties.setProperty("homepage", teamhomepage);
            }
            if (teamintroduction != null) {
              properties.setProperty("teamintroduction", teamintroduction);
            }
            try {
              ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
              properties.store(out, null);
              out.close();
              teamTable.addRow();
              teamTable.setValue(teamTable.getRowCount()-1, 0, lastTeamName.toLowerCase());
              teamTable.setValue(teamTable.getRowCount()-1, 1, out.toString());
            } catch (IOException ioe) {
            }
          }
        }
      }
    }
    generator = new HtmlTableGenerator(servlet);
    buffer.append(generator.generate(teamTable));
    buffer.append("</center></td></tr>");
    return buffer.toString();
  }

  /**
   *  Creates HTML page with the content of the statistic 'team members'
   *  where the user is defined.
   *  @param taskId task identifier
   *  @param  req  GET request
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'team members'.
  **/
  protected String createPage(int taskId, HttpServletRequest req, Connection con) throws SQLException, ServletException {
    String team = req.getParameter("team");
    if (team == null) {
      StringBuffer buffer = new StringBuffer(1024);
      inputTeam(null, buffer);
      return buffer.toString();
    }
    InnerPageBuffer innerPageBuffer = null;
    try {
      innerPageBuffer = getInnerPageBuffer(taskId);
      if (innerPageBuffer == null) {
        return "<tr><td>This statistic is not available at moment! Please try again later.</td></tr>";
      }
      String search = "><td>" + team + "</td><td><a href=\"";
      String[] teamTable = innerPageBuffer.getLines(search, true);
      if (teamTable == null) {
        StringBuffer buffer = new StringBuffer(1024);
        buffer.append("<tr><td><p><b>Error:</b> Team '");
        buffer.append(team);
        buffer.append("' is unknown.</td></tr>");
        inputTeam(null, buffer);
        return buffer.toString();
      } else {
        StringBuffer buffer = new StringBuffer(10*1024);
        buffer.delete(0, buffer.length());
        inputTeam(team, buffer);
        // Members:
        buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Members of top team '");
        buffer.append(team);
        buffer.append("'</center></td></tr><tr><td><br><center><p>");
        innerPageBuffer.skip("</table>", false);
        String teamhomepage = null;
        String teamintroduction = null;
        String prop = innerPageBuffer.between("><td>" + team.toLowerCase() + "</td><td", "</td></tr>", false);
        if (prop != null) {
          int idx = prop.indexOf('>');
          if (idx >= 0) {
            Properties properties = new Properties();
            try {
              properties.load(new ByteArrayInputStream(prop.substring(idx+1).getBytes()));
            } catch (IOException ioe) {
            }
            if (teamhomepage == null) {
              String s = properties.getProperty("homepage", "");
              if (s.length() > 0 && properties.getProperty("teamhomepage", "").equals("true")) {
                teamhomepage = s;
              }
            }
            if (teamintroduction == null) {
              String s = properties.getProperty("teamintroduction", "");
              if (s.length() > 0) {
                teamintroduction = s;
              }
            }
          }
        }
        if (teamhomepage != null || teamintroduction != null) {
          buffer.append("<center><table cellspacing=\"0\" width=\"90%\" border=\"1\"><colgroup><col width=\"30%\"><col width=\"70%\"></colgroup>");
          if (teamhomepage != null) {
            buffer.append("<tr><td>Homepage&nbsp;&nbsp;</td><td><a href=\"");
            buffer.append(teamhomepage);
            buffer.append("\">");
            buffer.append(teamhomepage);
            buffer.append("</a></td></tr>");
          }
          if (teamintroduction != null) {
            buffer.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"2\">");
            buffer.append(teamintroduction);
            buffer.append("</td></tr>");
          }
          buffer.append("</table></center><p>&nbsp;<br>");
        }
        buffer.append("\n<style type=\"text/css\">\n.c { text-align:center }\n.r { text-align:right }\n</style>\n<center><table border=\"1\" cellspacing=\"0\" width=\"90%\"><colgroup><col width=\"8%\"/><col width=\"24%\"/><col width=\"16%\"/><col width=\"16%\"/><col width=\"16%\"/><col width=\"16%\"/></colgroup>");
        buffer.append("<tr bgcolor=\"#dddddd\"><th>pos</th><th>user</th><th>joined</th><th>work units</th><th>computers used</th><th>zeros</th></tr>\n");
        for (int number = 0; number < teamTable.length; ++number) {
          if ((number&1) == 0) {
            buffer.append("<tr><td>");
          } else {
            buffer.append("<tr bgcolor=\"#eeeeee\"><td>");
          }
          buffer.append(number+1);
          buffer.append(".</td>");
          int idx = teamTable[number].indexOf("</td><td");
          buffer.append((idx < 0)? teamTable[number] : teamTable[number].substring(idx+5));
        }
        buffer.append("</table></center></td></tr>");
        return buffer.toString();
      }
    } finally {
      if (innerPageBuffer != null) {
        innerPageBuffer.close();
      }
    }
  }

  /**
   *  Generates an HTTP form to get the user name.
   *  @param user user name as default value
   *  @param email email of the user as default value
   *  @param buffer buffer for HTTP page
  **/
  private void inputTeam(String team, StringBuffer buffer) {
    buffer.append("<tr><td><form action=\"");
    buffer.append(servlet.getRootPath());
    buffer.append(servlet.getHandlerPath(getClass()));
    buffer.append("\"><p>Please type your registered team name (exactly as it appears in the list) into the entry field.<p>Team name&nbsp;&nbsp;<input name=\"team\" type=\"text\" size=\"60\" maxlength=\"100");
    if (team != null) {
      buffer.append("\" value=\"");
      buffer.append(team);
    }
    buffer.append("\">&nbsp;&nbsp;<input type=\"submit\" value=\"View\"></form></td></tr>");
  }

  /**
   *  Inserts a new row with the specified data in the team table. The team table is ordered by the column zeros.
   *  @param teamTable team table which is ordered by the column zeros.
   *  @param teamname team name
   *  @param age age of the team
   *  @param workUnits number of computed work units by the team
   *  @param members number of members in the team
   *  @param computersUsed number of used computers by the team
   *  @param zeros verified zeros by the team
  **/
  private void insertTeam(Table teamTable, String teamname, int age, int workUnits, int members, int computersUsed, long zeros) {
    if (teamname != null && teamname.length() > 0 && zeros > 0) {
      int l = teamTable.getRowCount();
      int i = 0;
      while (i < l) {
        if (zeros > ((Long)teamTable.getValue(i, 6)).longValue()) {
          break;
        }
        ++i;
      }
      teamTable.addRow(i);
      teamTable.setValue(i, 1, teamname);
      teamTable.setValue(i, 2, new Integer(age));
      teamTable.setValue(i, 3, new Integer(workUnits));
      teamTable.setValue(i, 4, new Integer(members));
      teamTable.setValue(i, 5, new Integer(computersUsed));
      teamTable.setValue(i, 6, new Long(zeros));
    }
  }


  /**
   *  Contains the complete top team statistic.
  **/
  private Map teamTables = new HashMap();
}
