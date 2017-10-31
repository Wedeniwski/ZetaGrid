/*--
  This file is a part of ZetaGrid, a simple and secure Grid Computing
  kernel.

  Copyright (c) 2001-2005 Sebastian Wedeniwski.  All rights reserved.

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

package zeta.server.handler.statistic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import zeta.WorkUnit;
import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.handler.ApproveHandler;
import zeta.server.handler.approve.ApproveProperties;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Query;
import zeta.server.util.QueryWithSum;
import zeta.util.SendMail;
import zeta.util.StreamUtils;
import zeta.util.StringUtils;
import zeta.util.Table;
import zeta.util.ThrowableHandler;

/**
 *  Handles a GET request for the statistic 'top producers'.
 *
 *  @version 2.0, August 6, 2005
**/
public class TopProducersHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public TopProducersHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Returns the complete top producers statistic.
   *  @param taskId task identifier
   *  @return the complete top producers statistic.
  **/
  Table getTopProducersTable(int taskId) {
    return (Table)topProducersTables.get(new Integer(taskId));
  }

  /**
   *  Creates HTML page with the content of the statistic 'top producers'
   *  where the user is not defined.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'top producers'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    StringBuffer buffer = new StringBuffer(100*1024);
    buffer.append("<tr><td><center>");
    HtmlTableGenerator generator = new HtmlTableGeneratorWithSum(servlet, servlet.getRootPath() + servlet.getHandlerPath(TopProducersHandler.class), "user");
    Table table = new Table(7);
    table.setColumnName(0, "place");
    table.setType(0, Types.VARCHAR);
    table.setAlignment(0, Table.LEFT);
    table.setColumnName(1, "trend");
    table.setType(1, Types.VARCHAR);
    table.setAlignment(1, Table.CENTER);
    table.setColumnName(2, "user");
    table.setType(2, Types.VARCHAR);
    table.setAlignment(2, Table.LEFT);
    table.setColumnName(3, "age (days)");
    table.setType(3, Types.INTEGER);
    table.setAlignment(3, Table.RIGHT);
    table.setColumnName(4, "work units");
    table.setType(4, Types.INTEGER);
    table.setAlignment(4, Table.RIGHT);
    table.setColumnName(5, "computers used");
    table.setType(5, Types.INTEGER);
    table.setAlignment(5, Table.RIGHT);
    table.setColumnName(6, "zeros");
    table.setType(6, Types.INTEGER);
    table.setAlignment(6, Table.RIGHT);
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      long maxDays = CachedQueries.getMaxDays(taskId, stmt);
      Map names = CachedQueries.getUserNames(con);
      Set lowerUserNamesSet = names.keySet();
      String[] lowerUserNames = new String[lowerUserNamesSet.size()];
      lowerUserNamesSet.toArray(lowerUserNames);
      Arrays.sort(lowerUserNames);
      synchronized (lowerUserNameListOrderByZerosYesterday) {
        if (maxDays != daysOfLowerUserNameListOrderByZerosDayBeforeYesterday+2) {
          if (daysOfLowerUserNameListOrderByZerosYesterday+2 == maxDays) {
            lowerUserNameListOrderByZerosDayBeforeYesterday = lowerUserNameListOrderByZerosYesterday;
            daysOfLowerUserNameListOrderByZerosDayBeforeYesterday = daysOfLowerUserNameListOrderByZerosYesterday;
          } else {
            lowerUserNameListOrderByZerosDayBeforeYesterday = new ArrayList(lowerUserNamesSet.size());
            Table table1 = new Query("SELECT LOWER(b.name) AS \"user\","
                                   + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                                   + " FROM zeta.computation a, zeta.user b, zeta.result r"
                                   + " WHERE a.task_id=r.task_id"
                                   + " AND a.task_id=" + taskId
                                   + " AND a.work_unit_id=r.work_unit_id"
                                   + " AND a.user_id=b.id"
                                   + " AND a.server_id=b.server_id"
                                   + " AND DAYS(r.stop)<=" + (maxDays-2)
                                   + " GROUP BY LOWER(b.name) ORDER BY \"zeros\" DESC", con, servlet).getResult();
            Table table2 = new Query("SELECT LOWER(b.name) AS \"user\","
                                   + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                                   + " FROM zeta.recomputation a, zeta.user b"
                                   + " WHERE a.task_id=" + taskId
                                   + " AND a.user_id=b.id"
                                   + " AND a.server_id=b.server_id"
                                   + " AND DAYS(a.stop)<=" + (maxDays-2)
                                   + " GROUP BY LOWER(b.name) ORDER BY \"zeros\" DESC", con, servlet).getResult();
            table1.addAndOrderByLastDesc(table2, new String[] { "user" }, "zeros", false);
            for (int i = 0, l = table1.getRowCount(); i < l; ++i) {
              String user = (String)table1.getValue(i, 0);
              int idx = Arrays.binarySearch(lowerUserNames, user);
              lowerUserNameListOrderByZerosDayBeforeYesterday.add((idx >= 0 && user.equals(lowerUserNames[idx]))? lowerUserNames[idx] : user);
            }
            daysOfLowerUserNameListOrderByZerosDayBeforeYesterday = maxDays-2;
          }
        }
        if (maxDays != daysOfLowerUserNameListOrderByZerosYesterday+1) {
          lowerUserNameListOrderByZerosYesterday = new ArrayList(lowerUserNamesSet.size());
          Table table1 = new Query("SELECT LOWER(b.name) AS \"user\","
                                 + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                                 + " FROM zeta.computation a, zeta.user b, zeta.result r"
                                 + " WHERE a.task_id=r.task_id"
                                 + " AND a.task_id=" + taskId
                                 + " AND a.work_unit_id=r.work_unit_id"
                                 + " AND a.user_id=b.id"
                                 + " AND a.server_id=b.server_id"
                                 + " AND DAYS(r.stop)<=" + (maxDays-1)
                                 + " GROUP BY LOWER(b.name) ORDER BY \"zeros\" DESC", con, servlet).getResult();
          Table table2 = new Query("SELECT LOWER(b.name) AS \"user\","
                                 + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                                 + " FROM zeta.recomputation a, zeta.user b"
                                 + " WHERE a.task_id=" + taskId
                                 + " AND a.user_id=b.id"
                                 + " AND a.server_id=b.server_id"
                                 + " AND DAYS(a.stop)<=" + (maxDays-1)
                                 + " GROUP BY LOWER(b.name) ORDER BY \"zeros\" DESC", con, servlet).getResult();
          table1.addAndOrderByLastDesc(table2, new String[] { "user" }, "zeros", false);
          for (int i = 0, l = table1.getRowCount(); i < l; ++i) {
            String user = (String)table1.getValue(i, 0);
            int idx = Arrays.binarySearch(lowerUserNames, user);
            lowerUserNameListOrderByZerosYesterday.add((idx >= 0 && user.equals(lowerUserNames[idx]))? lowerUserNames[idx] : user);
          }
          daysOfLowerUserNameListOrderByZerosYesterday = maxDays-1;
        }
      }
      Table table1 = new Query("SELECT LOWER(b.name) AS \"user\","
                             + " COUNT(*) AS \"work units\","
                             + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                             + " FROM zeta.computation a, zeta.user b, zeta.result r"
                             + " WHERE a.task_id=r.task_id"
                             + " AND a.task_id=" + taskId
                             + " AND a.work_unit_id=r.work_unit_id"
                             + " AND a.user_id=b.id"
                             + " AND a.server_id=b.server_id"
                             + " GROUP BY LOWER(b.name) ORDER BY \"zeros\" DESC", con, servlet).getResult();
      Table table2 = new Query("SELECT LOWER(b.name) AS \"user\","
                             + " COUNT(*) AS \"work units\","
                             + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                             + " FROM zeta.recomputation a, zeta.user b"
                             + " WHERE a.task_id=" + taskId
                             + " AND a.user_id=b.id"
                             + " AND a.server_id=b.server_id"
                             + " AND a.stop IS NOT NULL"
                             + " GROUP BY LOWER(b.name) ORDER BY \"zeros\" DESC", con, servlet).getResult();
      table1.addAndOrderByLastDesc(table2, new String[] { "user" }, "zeros", false);
      for (int place = 0, l = table1.getRowCount(); place < l; ++place) {
        table.addRow();
        table.setValue(place, 0, Integer.toString(place+1) + '.');
        String user = (String)table1.getValue(place, 0);//rs.getString(1);
        int idx = Arrays.binarySearch(lowerUserNames, user);
        if (idx >= 0 && user.equals(lowerUserNames[idx])) {
          user = lowerUserNames[idx];
        }
        idx = 0;
        int placeYesterday = lowerUserNameListOrderByZerosYesterday.indexOf(user);
        int placeDayBeforeYesterday = lowerUserNameListOrderByZerosDayBeforeYesterday.indexOf(user);
        if (placeYesterday < placeDayBeforeYesterday) {
          ++idx;
        } else if (placeYesterday > placeDayBeforeYesterday) {
          --idx;
        }
        if (place < placeYesterday) {
          ++idx;
        } else if (place > placeYesterday) {
          --idx;
        }
        if (idx < 0) {
          table.setValue(place, 1, (idx < -1)? "--" : "-");
        } else if (idx > 0) {
          table.setValue(place, 1, (idx > 1)? "++" : "+");
        } else {
          table.setValue(place, 1, "&nbsp;");
        }
        String s = (String)names.get(user);
        table.setValue(place, 2, (s == null)? user : s);
        Long userMinStartDays = CachedQueries.getUserMinStartDays(taskId, con, user);
        if (userMinStartDays == null) {
          table.setValue(place, 3, new Long(0));
        } else {
          table.setValue(place, 3, new Long(Math.max(0, maxDays - userMinStartDays.longValue())));
        }
        table.setValue(place, 4, table1.getValue(place, 1));//new Integer(rs.getInt(2)));
        table.setValue(place, 5, new Integer(CachedQueries.getMaxComputersUsed(taskId, user)));
        table.setValue(place, 6, table1.getValue(place, 2));//new Long(rs.getLong(3)));
      }
      //rs.close();
    } finally {
      DatabaseUtils.close(stmt);
    }
    QueryWithSum.addSum(table);
    CachedQueries.setNumberOfComputers(((Integer)table.getValue(table.getRowCount()-1, 5)).intValue());
    buffer.append(generator.generate(table));
    topProducersTables.put(new Integer(taskId), table);
    buffer.append("</center></td></tr><tr><td><br><center>");
    generator = new HtmlTableGenerator(servlet);
    buffer.append(generator.generate(CachedQueries.getUserData(con)));
    buffer.append("</center></td></tr>");
    return buffer.toString();
  }

  /**
   *  Creates HTML page with the content of the statistic 'top producers'
   *  where the user is defined.
   *  @param taskId task identifier
   *  @param  req  GET request
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'top producers'.
  **/
  protected String createPage(int taskId, HttpServletRequest req, Connection con) throws SQLException, ServletException {
    String workUnit = req.getParameter("work_unit");
    String user = req.getParameter("user");
    if (user == null) {
      if (req.getParameter("all") != null) {
        InnerPageBuffer innerPageBuffer = null;
        try {
          if (innerPageBuffer != null) {
            innerPageBuffer = getInnerPageBuffer(taskId);
            ByteArrayOutputStream out = new ByteArrayOutputStream(10*1024);
            StreamUtils.writeData(innerPageBuffer.buffer, out, false, true);
            return out.toString("ISO-8859-1");
          }
        } catch (IOException ioe) {
        } finally {
          if (innerPageBuffer != null) {
            innerPageBuffer.close();
          }
        }
      }
      StringBuffer buffer = new StringBuffer(1024);
      inputUser(taskId, null, null, workUnit, false, buffer);
      return buffer.toString();
    }
    String key = req.getParameter("key");
    String keyLength = req.getParameter("length");
    String email = req.getParameter("email");
    if (key != null && keyLength != null) {
      StringBuffer buffer = new StringBuffer(1024);
      requestApprovalToChangeProperties(con, key, keyLength, req, buffer);
      return buffer.toString();
    }
    synchronized (this) { // just to reduce memory since innerPageBuffer can be large
      InnerPageBuffer innerPageBuffer = null;
      try {
        innerPageBuffer = getInnerPageBuffer(taskId);
        if (innerPageBuffer == null) {
          return "<tr><td>This statistic is not available at moment! Please try again later.</td></tr>";
        }
        String beginTable = StreamUtils.between(innerPageBuffer.buffer, "<table", "</tr>", false);
        StreamUtils.skip(innerPageBuffer.buffer, "<tr>", false);
        String[] userLines = StreamUtils.getLines(innerPageBuffer.buffer, NUMBER_ABOVE_BELOW_USER+1, "</td><td><a href=\"" + servlet.getRootPath() + servlet.getHandlerPath(getClass()) + "?user=" + URLEncoder.encode(user, "ISO-8859-1") + "\">" + user + "</a></td><td class=\"r\">", true);
        int userId = 0;
        if (userLines != null && workUnit != null) {
          userId = isLoginNeeded(taskId, user, email, workUnit);
          if (userId == 0) {
            StringBuffer buffer = new StringBuffer(2048);
            if (workUnit.length() > 0) {
              buffer.append("<tr><td><b>Error:</b> Work unit id '");
              buffer.append(workUnit);
              buffer.append("' is invalid.</td></tr>");
            }
            inputUser(taskId, user, email, workUnit, false, buffer);
            return buffer.toString();
          }
        }
        if (userLines == null) {
          StringBuffer buffer = new StringBuffer(1024);
          buffer.append("<tr><td><b>Error:</b> User '");
          buffer.append(user);
          buffer.append("' is unknown.</td></tr>");
          inputUser(taskId, null, null, workUnit, false, buffer);
          return buffer.toString();
        } if (workUnit != null) {
          StringBuffer buffer = new StringBuffer(4096);
          changeProperties(con, userId, workUnit, buffer);
          return buffer.toString();
        } else {
          String[] nextUsers = StreamUtils.getLines(innerPageBuffer.buffer, NUMBER_ABOVE_BELOW_USER);
          String[] endTable = StreamUtils.getLines(innerPageBuffer.buffer, 2, "</table>", false);
  
          String viewWorkstations = req.getParameter("view");
          if (viewWorkstations == null || !viewWorkstations.equals("all")) {
            viewWorkstations = "active";
          }
          boolean viewAllWorkstations = viewWorkstations.equals("all");
          StringBuffer buffer = new StringBuffer(70*1024);
  
          // user data:
          String userLine = null;
          if (email != null) {
            userLine = StreamUtils.between(innerPageBuffer.buffer, "><td>" + user + "</td><td>" + ((email.trim().length() == 0)? "&nbsp;" : email) + "</td><td", "</tr>", true);
            if (userLine == null) {
              if (email.trim().length() > 0) {
                buffer.append("<tr><td><b>Error:</b> E-mail '");
                buffer.append(email);
                buffer.append("' is unknown.</td></tr>");
              }
              innerPageBuffer.close();
              innerPageBuffer = null;
              innerPageBuffer = getInnerPageBuffer(taskId);
              StreamUtils.skip(innerPageBuffer.buffer, "</table>", false);
              email = null;
            }
          }
          if (email == null) {
            userLine = StreamUtils.between(innerPageBuffer.buffer, "><td>" + user + "</td><td", "</tr>", true);
            if (userLine != null) {
              int pos = userLine.indexOf('>');
              int pos2 = userLine.indexOf("</td>", pos+1);
              if (pos >= 0 && pos2 > pos) {
                userLine = userLine.substring(pos2+5);
              }
            }
          }
          String[] userData = getUserData(userLine);
          if (userData != null && (userData[5] == null || userData[5].length() == 0) && (userData[3] == null || userData[3].length() == 0)) {
            while (true) {
              if (email != null) {
                userLine = StreamUtils.between(innerPageBuffer.buffer, "><td>" + user + "</td><td>" + ((email.trim().length() == 0)? "&nbsp;" : email) + "</td><td", "</tr>", true);
              } else {
                userLine = StreamUtils.between(innerPageBuffer.buffer, "><td>" + user + "</td><td", "</tr>", true);
                if (userLine != null) {
                  int pos = userLine.indexOf('>');
                  int pos2 = userLine.indexOf("</td>", pos+1);
                  if (pos >= 0 && pos2 > pos) {
                    userLine = userLine.substring(pos2+5);
                  }
                }
              }
              if (userLine == null) {
                break;
              }
              String[] userData2 = getUserData(userLine);
              if (userData2 != null && ((userData2[5] != null && userData2[5].length() > 0) || (userData2[3] != null && userData2[3].length() > 0))) {
                userData = userData2;
                break;
              }
            }
          }
          inputUser(taskId, user, email, null, viewAllWorkstations, buffer);
  
          // Properties of first top producer:
          buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Top producer '");
          buffer.append(user);
          buffer.append("'</center></td></tr><tr><td>");
    
          if (userData != null) {
            Properties properties = new Properties();
            if (userData[5] == null) {
              userData[5] = "";
            }
            try {
              properties.load(new ByteArrayInputStream(userData[5].getBytes()));
            } catch (IOException ioe) {
            }
            String s = properties.getProperty("homepage", "");
            if (s.length() > 0 || properties.getProperty("freetext", "").length() > 0 || properties.getProperty("visibleemail", "").length() > 0) {
              buffer.append("<br><center><table cellspacing=\"0\" width=\"90%\" border=\"1\"><colgroup><col width=\"30%\"><col width=\"70%\"></colgroup>");
              if (s.length() > 0) {
                buffer.append("<tr><td>Homepage&nbsp;&nbsp;</td><td><a href=\"");
                buffer.append(s);
                buffer.append("\">");
                buffer.append(s);
                buffer.append("</a></td></tr>");
              }
              s = properties.getProperty("visibleemail", "");
              if (s.length() > 0) {
                buffer.append("<tr><td>Contact&nbsp;&nbsp;</td><td><a href=\"mailto:");
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
            } else {
              buffer.append("<br>");
            }
            String teamName = userData[3];
            if (teamName != null && teamName.length() > 0) {
              buffer.append("<center><table cellspacing=\"0\" width=\"90%\" border=\"1\"><colgroup><col width=\"30%\"><col width=\"70%\"></colgroup>");
              buffer.append("<tr><td>Member of team&nbsp;&nbsp;</td><td><a href=\"");
              buffer.append(servlet.getRootPath());
              buffer.append(servlet.getHandlerPath(TeamMembersHandler.class));
              buffer.append("?team=");
              buffer.append(URLEncoder.encode(teamName, "ISO-8859-1"));
              buffer.append("\">");
              buffer.append(teamName);
              buffer.append("</a></td></tr>");
              s = properties.getProperty("teamintroduction", "");
              if (s.length() > 0) {
                buffer.append("<tr bgcolor=\"#eeeeee\"><td colspan=\"2\">");
                buffer.append(s);
                buffer.append("</td></tr>");
              }
              buffer.append("</table></center>");
            }
            if (email != null) {
              buffer.append("<br><center><table cellspacing=\"0\" width=\"90%\" border=\"1\"><colgroup><col width=\"60%\"><col width=\"40%\"></colgroup>");
              buffer.append("<tr><td bgcolor=\"#eeeeee\">Number of redistributed work units (not delivered in ");
              ServerTask task = servlet.getTaskManager().getServerTask(taskId);
              if (task != null) {
                appendTimeInfo(task.getRedistributionConnected(), buffer);
              } else {
                buffer.append('?');
              }
              buffer.append(")&nbsp;&nbsp;</td><td>");
              buffer.append(userData[0]);
              try {
                if (userData[1].length() > 0 && !userData[1].equals("&nbsp;")) {
                  Long redistributedWorkUnitId = new Long(userData[1]);
                  if (redistributedWorkUnitId != null && redistributedWorkUnitId.longValue() > 0) {
                    buffer.append("</td></tr><tr><td bgcolor=\"#eeeeee\">Last redistributed work unit ID&nbsp;&nbsp;</td><td>");
                    DecimalFormat decFormatter = new DecimalFormat("#,###");
                    buffer.append(decFormatter.format(redistributedWorkUnitId));
                    buffer.append("</td></tr>");
                    try {
                      DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.GERMANY);
                      String time = dateFormatter.format(Timestamp.valueOf(userData[2]));
                      buffer.append("<tr><td bgcolor=\"#eeeeee\">Last redistributed work unit at&nbsp;&nbsp;</td><td>");
                      buffer.append(time);
                    } catch (IllegalArgumentException iae) {
                    }
                  }
                }
              } catch (NumberFormatException nfe) {
              }
              buffer.append("</table></center>");
            }
          }
    
          // Overall ranking:
          buffer.append("</td></tr>\n<style type=\"text/css\">\n.c { text-align:center }\n.r { text-align:right }\n</style>\n<tr><td height=\"30pt\" class=\"second-head-gray\">Overall ranking (");
          int l = endTable[0].length();
          int i = 0;
          while (++i < l && !Character.isDigit(endTable[0].charAt(i)));
          while (i < l) {
            char c = endTable[0].charAt(i);
            if (!Character.isDigit(c)) {
              break;
            }
            buffer.append(c);
            ++i;
          }
          buffer.append(" users):</td></tr><tr><td><br><center><table");
          buffer.append(beginTable);
          buffer.append("</tr>\n");
          for (i = 0; i+1 < userLines.length && userLines[i+1] != null; ++i) {
            buffer.append(userLines[i]);
          }
          buffer.append("\n<tr bgcolor=\"#ffffe0\">");
          buffer.append(userLines[i].substring(userLines[i].indexOf('>')+1));
          for (i = 0; i < nextUsers.length && nextUsers[i] != null; ++i) {
            buffer.append(nextUsers[i]);
          }
    
          // Statistics: last 24 hours, last 7 days, close zeros
          final Class[] handlerClass = { TopProducers24HoursHandler.class, TopProducers7DaysHandler.class, CloseZerosHandler.class };
          final String[] handlerTitle = { "Last 24 hours", "Last 7 days", "Close zeros" };
          final String[] handlerNoWU = { "No work units delivered in the last 24 hours.", "No work units delivered in the last 7 days.", "No delivered work unit contains a close zero." };
          final boolean[] handlerCountUser = { true, true, false };
          for (int classIdx = 0; classIdx < handlerClass.length; ++classIdx) {
            AbstractHandler handler = (AbstractHandler)servlet.getHandlerInstance(handlerClass[classIdx]);
            if (handler != null) {
              String pageBuffer = handler.createPage(taskId, req, con);
              int tableBegin = pageBuffer.indexOf("<th>user</th>")+13;
              int userPlace = StringUtils.indexOfIgnoreCase(pageBuffer, "\">" + user + "</a></td><td", tableBegin);
              if (userPlace == -1) {
                String search = "\">" + user + ' ';
                userPlace = StringUtils.indexOfIgnoreCase(pageBuffer, search, tableBegin);
                final int lp = pageBuffer.length();
                int idx;
                for (idx = userPlace+search.length(); idx < lp && pageBuffer.charAt(idx) == ' '; ++idx);
                if (idx == lp || pageBuffer.charAt(idx) != '<') {
                  userPlace = -1;
                }
              }
              buffer.append("\n</table></center></td></tr>\n<tr><td height=\"30pt\" class=\"second-head-gray\">");
              buffer.append(handlerTitle[classIdx]);
              int lp = pageBuffer.indexOf("</table>", tableBegin);
              if (lp == -1) {
                lp = pageBuffer.length();
              }
              if (handlerCountUser[classIdx]) {
                buffer.append(" (");
                //i = pageBuffer.indexOf("<tr><td><span style=\"font-family:");
                i = pageBuffer.indexOf("&sum;");
                while (++i < lp && !Character.isDigit(pageBuffer.charAt(i)));
                while (i < lp) {
                  char c = pageBuffer.charAt(i);
                  if (!Character.isDigit(c)) {
                    break;
                  }
                  buffer.append(c);
                  ++i;
                }
                buffer.append(" users)");
              }
              buffer.append(":</td></tr><tr><td><br><center>");
              if (userPlace < 0) {
                buffer.append("<table><tr><td>");
                buffer.append(handlerNoWU[classIdx]);
                buffer.append("</td></tr>");
              } else {
                int statisticBegin = tableBegin;
                for (int count = 1; statisticBegin > 0 && (pageBuffer.charAt(statisticBegin) != '\n' || --count >= 0); --statisticBegin);
                while (tableBegin < lp && pageBuffer.charAt(tableBegin) != '\n') {
                  ++tableBegin;
                }
                buffer.append(pageBuffer.substring(statisticBegin+1, tableBegin));
                // table with user
                while (userPlace > tableBegin && pageBuffer.charAt(userPlace) != '\n') {
                  --userPlace;
                }
                i = userPlace;
                for (int count = NUMBER_ABOVE_BELOW_USER; i > tableBegin && (pageBuffer.charAt(i) != '\n' || --count >= 0); --i);
                buffer.append(pageBuffer.substring(i, userPlace));
                buffer.append("\n<tr bgcolor=\"#ffffe0\">");
                while (userPlace < lp && pageBuffer.charAt(userPlace) != '>') {
                  ++userPlace;
                }
                i = ++userPlace;
                for (int count = NUMBER_ABOVE_BELOW_USER; i < lp && (pageBuffer.charAt(i) != '\n' || --count >= 0); ++i);
                buffer.append(pageBuffer.substring(userPlace, i));
              }
            }
          }
          buffer.append("</table></center></td></tr>");
          if (email != null) {
            // Workstations:
            WorkstationsHandler handler = (WorkstationsHandler)servlet.getHandlerInstance(WorkstationsHandler.class); //new WorkstationsHandler(servlet);
            if (handler != null) {
              ListGenerator generator = new ListGenerator();
              String pageBuffer = handler.createPage(taskId, req, con);
              int tableBegin = 0;
              String search = "\n" + handler.encode("user") + user + ',' + email + ',';
              for (tableBegin = StringUtils.indexOfIgnoreCase(pageBuffer, search, tableBegin); tableBegin >= 0; tableBegin = StringUtils.indexOfIgnoreCase(pageBuffer, search, tableBegin)) {
                int wsEnd = pageBuffer.indexOf('\n', tableBegin+search.length());
                if (wsEnd == -1) {
                  break;
                }
                String hostname = pageBuffer.substring(tableBegin+search.length(), wsEnd);
                final int lp = pageBuffer.length();
                tableBegin = wsEnd+1;
                int tableEnd = pageBuffer.indexOf("\n" + handler.encode("user"), tableBegin);
                String content = (tableEnd == -1)? pageBuffer.substring(tableBegin) : pageBuffer.substring(tableBegin, tableEnd);
                if (viewAllWorkstations || handler.isActive(content)) {
                  buffer.append("\n<tr><td height=\"30pt\" class=\"second-head-gray\">Workstation '");
                  buffer.append(hostname);
                  buffer.append("':</td></tr><tr><td><br><center>");
                  buffer.append(generator.generateHTML(content, handler));
                  buffer.append("</center></td></tr>");
                }
                tableBegin = tableEnd;
              }
            }
          }
          return buffer.toString();
        }
      } catch (IOException ioe) {
        throw new ServletException(ioe);    // ToDo: reload when exception "Unexpected end of ZLIB input stream" occurs because of an update of the statistics
      } finally {
        if (innerPageBuffer != null) {
          innerPageBuffer.close();
        }
      }
    }
  }

  private static String[] getUserData(String line) {
    if (line != null) {
      int pos = line.indexOf('>');
      int pos2 = line.indexOf("</td>", pos+1);
      if (pos >= 0 && pos2 > pos) {
        String numberOfRedistributions = line.substring(pos+1, pos2);
        pos = line.indexOf('>', line.indexOf("<td", pos2+5)+3);
        pos2 = line.indexOf("</td>", pos+1);
        if (pos >= 0 && pos2 > pos) {
          String lastRedistributedWorkUnit = line.substring(pos+1, pos2);
          pos = line.indexOf('>', line.indexOf("<td", pos2+5)+3);
          pos2 = line.indexOf("</td>", pos+1);
          if (pos >= 0 && pos2 > pos) {
            String lastRedistributedTimestamp = line.substring(pos+1, pos2);
            pos = line.indexOf('>', line.indexOf("<td", pos2+5)+3);
            pos2 = line.indexOf("</td>", pos+1);
            if (pos >= 0 && pos2 > pos) {
              String teamName = line.substring(pos+1, pos2).trim();
              if (teamName.equals("&nbsp;")) {
                teamName = null;
              }
              pos = line.indexOf('>', line.indexOf("<td", pos2+5)+3);
              pos2 = line.indexOf("</td>", pos+1);
              if (pos >= 0 && pos2 > pos) {
                String messages = line.substring(pos+1, pos2);
                pos = line.indexOf('>', line.indexOf("<td", pos2+5)+3);
                pos2 = line.indexOf("</td>", pos+1);
                if (pos >= 0 && pos2 > pos) {
                  String properties = line.substring(pos+1, pos2).trim();
                  if (properties.equals("&nbsp;")) {
                    properties = null;
                  }
                  return new String[] { numberOfRedistributions, lastRedistributedWorkUnit, lastRedistributedTimestamp, teamName, messages, properties };
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   *  Generates an HTTP form to get the user name.
   *  @param taskId task identifier
   *  @param user user name as default value
   *  @param email email of the user as default value
   *  @param workUnit work unit identifier to login
   *  @param viewAllWorkstations options how to view the list of the workstations
   *  @param buffer buffer for HTTP page
  **/
  private void inputUser(int taskId, String user, String email, String workUnit, boolean viewAllWorkstations, StringBuffer buffer) throws ServletException {
    buffer.append("<tr><td><form action=\"");
    buffer.append(servlet.getRootPath());
    buffer.append(servlet.getHandlerPath(getClass()));
    buffer.append("\"><p>Please type your registered user name (exactly as it appears in the list) into the entry field.");
    if (workUnit == null) {
      buffer.append("<br>Your position and three places above and below will be displayed.");
      if (email == null) {
        buffer.append("<br>To get more information about your computer(s) fill in your email address.");
      } else {
        buffer.append("<br>You can <a href=\"");
        buffer.append(servlet.getRootPath());
        buffer.append(servlet.getHandlerPath(getClass()));
        buffer.append("?user=");
        try {
          buffer.append(URLEncoder.encode(user, "ISO-8859-1"));
          buffer.append("&email=");
          buffer.append(URLEncoder.encode(email, "ISO-8859-1"));
        } catch (UnsupportedEncodingException uee) {
        }
        buffer.append("&work_unit=");
        buffer.append("\">change</a> any properties at the server which are associated with your user id.");
      }
    } else {
      buffer.append("<p>You can change any properties at the server which are associated with your user id.");
    }
    buffer.append("<p><table><tr><td>User name&nbsp;&nbsp;</td><td><input name=\"user\" type=\"text\" size=\"60\" maxlength=\"100");
    if (user != null) {
      buffer.append("\" value=\"");
      buffer.append(user);
    }
    buffer.append("\"></td><td>&nbsp;</td></tr><tr><td>E-mail&nbsp;&nbsp;</td><td><input name=\"email\" type=\"text\" size=\"60\" maxlength=\"100");
    if (email != null) {
      buffer.append("\" value=\"");
      buffer.append(email);
    }
    if (workUnit != null) {
      buffer.append("\"></td><td>&nbsp;</td></tr><tr><td>Active work unit id&nbsp;&nbsp;</td><td><input name=\"work_unit\" type=\"file\" size=\"60\" maxlength=\"100\" value=\"");
      buffer.append(workUnit);
      buffer.append("\"></td><td>&nbsp;</td></tr>\n");
      buffer.append("<tr><td colspan=\"3\"><table><tr><td>Notes:</td><td>1. Use either the entire logfile name ");
      ServerTask task = servlet.getTaskManager().getServerTask(taskId);
      if (task != null) {
        buffer.append("(e.g., '");
        buffer.append(task.createWorkUnit(1, 1, false).getLogFileName());
        buffer.append("') ");
      }
      buffer.append("or just the identifier of the work unit.</td></tr>\n");
      buffer.append("<tr><td></td><td>2. You cannot use a local work unit which is older than ");
      if (task != null) {
        appendTimeInfo(task.getRedistributionConnected(), buffer);
      } else {
        buffer.append('?');
      }
      buffer.append(" since it may be redistributed.</td></tr></table></td></tr>\n");
      buffer.append("<tr><td><input type=\"submit\" value=\"Login\"></td></tr>\n");
    } else {
      buffer.append("\">&nbsp;&nbsp;<td><input type=\"submit\" value=\"View\"></td></tr>");
    }
    if (workUnit == null && email != null && email.length() > 0) {
      buffer.append("<tr><td><input type=\"radio\" name=\"view\" value=\"active\"");
      if (!viewAllWorkstations) {
        buffer.append(" checked");
      }
      buffer.append("></td><td>View only active workstations</td><td>&nbsp;</td></tr>");
      buffer.append("<tr><td><input type=\"radio\" name=\"view\" value=\"all\"");
      if (viewAllWorkstations) {
        buffer.append(" checked");
      }
      buffer.append("></td><td>View all workstations</td><td>&nbsp;</td></tr>");
    }
    buffer.append("</table></form></td></tr>");
  }

  /**
   *  Returns the user id (>0) if the user is defined. Otherwise 0 will be returned.
   *  @return the user id (>0) if the user is defined. Otherwise 0 will be returned.
  **/
  private int isLoginNeeded(int taskId, String user, String email, String workUnit) throws ServletException {
    if (user != null && user.length() > 0 && email != null && email.length() > 0 && workUnit != null && workUnit.length() > 0) {
      ServerTask task = servlet.getTaskManager().getServerTask(taskId);
      if (task != null) {
        List validWorkUnit = task.createWorkUnits(new String[] {workUnit});
        if (!validWorkUnit.isEmpty()) {
          workUnit = String.valueOf(((WorkUnit)validWorkUnit.get(0)).getWorkUnitId());
        }
      }

      Connection con = null;
      Statement stmt = null;
      try {
        con = servlet.getConnection();
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT c.user_id FROM zeta.computation c, zeta.user u WHERE c.task_id=" + taskId
                                       + " AND c.work_unit_id=" + workUnit
                                       //+ " AND c.server_id=" + servlet.getServer().getId()
                                       + " AND c.server_id=u.server_id AND c.user_id=u.id AND u.name='" + user + "' AND u.email='" + email
                                       + "' AND c.task_id NOT IN (SELECT task_id FROM zeta.result WHERE task_id=c.task_id AND work_unit_id=c.work_unit_id)"
                                       + " AND user_id>0 ORDER BY user_id");
        if (rs.next()) {
          int userId = rs.getInt(1);
          rs.close();
          return userId;
        }
        rs.close();
      } catch (SQLException e) {
        ThrowableHandler.handle(e);
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(con);
      }
    }
    return 0;
  }

  private void changeProperties(Connection con, int userId, String workUnit, StringBuffer buffer) throws ServletException {
    String rootPath = DispatcherServlet.getRootPath();
    int i = rootPath.indexOf(DispatcherServlet.getServletPath());
    if (i >= 0) {
      rootPath = rootPath.substring(0, i);
    }
    buffer.append("<tr><td>You can change any properties at the server which are associated with your user id.");
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT name,email,email_valid_YN,team_name,properties FROM zeta.user WHERE server_id=" + servlet.getServer().getId() + " AND id=" + userId);
      if (rs.next()) {
        buffer.append("<p><form action=\"");
        buffer.append(servlet.getRootPath());
        buffer.append(servlet.getHandlerPath(getClass()));
        buffer.append("\"><table>");
        buffer.append("<tr><td>user&nbsp;&nbsp;</td><td>");
        String user = rs.getString(1);
        buffer.append(user);
        buffer.append("</td></tr><tr><td></td><td><input name=\"user\" type=\"text\" size=\"60\" maxlength=\"100\" value=\"");
        buffer.append(user);
        buffer.append("\"></td><td>&nbsp;</td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        buffer.append("<tr><td>e-mail address&nbsp;&nbsp;</td><td>");
        String email = rs.getString(2);
        buffer.append(email);
        buffer.append("</td></tr><tr><td></td><td><input name=\"email\" type=\"text\" size=\"60\" maxlength=\"100\" value=\"");
        buffer.append(email);
        buffer.append("\"></td><td>&nbsp;</td></tr>");
        buffer.append("<tr><td>visible to others&nbsp;&nbsp;</td><td>");
        Properties properties = new Properties();
        String s = rs.getString(5);
        if (s == null) {
          s = "";
        }
        try {
          properties.load(new ByteArrayInputStream(s.getBytes()));
        } catch (IOException ioe) {
        }
        s = properties.getProperty("visibleemail", "");
        buffer.append(s);
        buffer.append("</td></tr><tr><td></td><td><input name=\"visibleemail\" type=\"text\" size=\"60\" maxlength=\"100\" value=\"");
        buffer.append(s);
        buffer.append("\"></td><td>&nbsp;</td></tr><tr><td colspan=\"2\">note: Everybody could see your detailed statistic about your computer(s) if you make your e-mail address visible.</td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        buffer.append("<tr><td>receive announcements&nbsp;&nbsp;</td><td>");
        String messages = rs.getString(3);
        buffer.append((messages.equals("Y")? "yes" : "no"));
        buffer.append("</td></tr>");
        buffer.append("<tr><td></td><td><input name=\"messages\" type=\"checkbox\" value=\"Y\"");
        if (messages.equals("Y")) {
          buffer.append(" checked");
        }
        buffer.append("></td><td>&nbsp;</td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        buffer.append("<tr><td>team&nbsp;&nbsp;</td><td>");
        String team = rs.getString(4);
        if (team == null) {
          team = "";
        }
        buffer.append(team);
        buffer.append("</td></tr>");
        buffer.append("<tr><td></td><td><input name=\"team\" type=\"text\" size=\"60\" maxlength=\"100\" value=\"");
        buffer.append(team);
        buffer.append("\"></td><td>&nbsp;</td></tr><tr><td colspan=\"2\">note: 'Join in team date' will be set to current date if you change your team membership.</td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        buffer.append("<tr><td>current<br>team introduction&nbsp;&nbsp;</td><td><textarea cols=\"60\" rows=\"4\" readonly>");
        s = properties.getProperty("teamintroduction", "");
        buffer.append(s);
        buffer.append("</textarea></td></tr>");
        buffer.append("<tr><td>new team introduction<br>(max. 255 characters)</td><td><textarea name=\"teamintroduction\" cols=\"60\" rows=\"4\">");
        buffer.append(s);
        buffer.append("</textarea></td><td>&nbsp;</td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        buffer.append("<tr><td>homepage&nbsp;&nbsp;</td><td><a href=\"");
        s = properties.getProperty("homepage", "");
        buffer.append(s);
        buffer.append("\">");
        buffer.append(s);
        buffer.append("</a></td></tr>");
        buffer.append("<tr><td></td><td><input name=\"homepage\" type=\"text\" size=\"60\" maxlength=\"100\" value=\"");
        buffer.append(s);
        buffer.append("\"></td><td>&nbsp;</td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        buffer.append("<tr><td>is team homepage&nbsp;&nbsp;</td><td>");
        s = properties.getProperty("teamhomepage", "");
        buffer.append((s.equals("true")? "yes" : "no"));
        buffer.append("</td></tr>");
        buffer.append("<tr><td></td><td><input name=\"teamhomepage\" type=\"checkbox\" value=\"true\"");
        if (s.equals("true")) {
          buffer.append(" checked");
        }
        buffer.append("></td><td>&nbsp;</td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        buffer.append("<tr><td>current<br>free text&nbsp;&nbsp;</td><td><textarea cols=\"60\" rows=\"4\" readonly>");
        s = properties.getProperty("freetext", "");
        buffer.append(s);
        buffer.append("</textarea></td></tr>");
        buffer.append("<tr><td>new free text<br>(max. 255 characters)</td><td><textarea name=\"freetext\" cols=\"60\" rows=\"4\">");
        buffer.append(s);
        buffer.append("</textarea></td></tr>");
        buffer.append("<tr><td colspan=\"2\" background=\"");
        buffer.append(rootPath);
        buffer.append("/images/back_dots_66f.gif\"><img width=\"2\" src=\"");
        buffer.append(rootPath);
        buffer.append("/images/odot.gif\" height=\"2\" border=\"0\" alt=\"\"/></td></tr>");
        String sessionKey = ApproveHandler.generateAddressToApprove(servlet, String.valueOf(userId) + ':' + workUnit + ':' + email + ':' + user);
        int idx = sessionKey.indexOf("?key=");
        int idx2 = sessionKey.indexOf("&length=", idx);
        buffer.append("<tr><td></td><td><input name=\"key\" type=\"hidden\" value=\"");
        buffer.append(sessionKey.substring(idx+5, idx2));
        buffer.append("\"></td></tr>");
        buffer.append("<tr><td></td><td><input name=\"length\" type=\"hidden\" value=\"");
        buffer.append(sessionKey.substring(idx2+8));
        buffer.append("\"></td></tr>");
        buffer.append("<tr><td colspan=\"2\">A link to approve the changes will be sent to '");
        buffer.append(email);
        buffer.append("' if you change some properties.</td></tr>");
        buffer.append("<tr><td></td><td align=right><input type=\"submit\" value=\"Change\"></td></tr>");
        buffer.append("</table></form>");
      }
      rs.close();
    } catch (SQLException e) {
      throw new ServletException(e);
    } catch (IOException e) {
      throw new ServletException(e);
    } finally {
      DatabaseUtils.close(stmt);
    }
    buffer.append("</td></tr>");
  }

  private void requestApprovalToChangeProperties(Connection con, String key, String keyLength, HttpServletRequest req, StringBuffer buffer) throws SQLException, ServletException {
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      String sessionKey = ApproveHandler.decrypt(servlet, stmt, key,  keyLength);
      int idx = sessionKey.indexOf(':');
      if (idx == -1) {
        buffer.append("<tr><td><b>Error:</b> Session key is invalid.</td></tr>");
        return;
      }
      int userId = Integer.parseInt(sessionKey.substring(0, idx));
      idx = sessionKey.indexOf(':', idx+1);
      if (idx == -1) {
        buffer.append("<tr><td><b>Error:</b> Session key is invalid.</td></tr>");
        return;
      }
      int idx2 = sessionKey.indexOf(':', idx+1);
      if (idx2 == -1) {
        buffer.append("<tr><td><b>Error:</b> Session key is invalid.</td></tr>");
        return;
      }
      String emailOrig = sessionKey.substring(idx+1, idx2);
      String userOrig = sessionKey.substring(idx2+1);
      ResultSet rs = stmt.executeQuery("SELECT email_valid_YN,team_name,properties FROM zeta.user WHERE server_id=" + servlet.getServer().getId() + " AND id=" + userId
                                     + " AND name='" + userOrig + "' AND email='" + emailOrig + '\'');
      if (!rs.next()) {
        rs.close();
        buffer.append("<tr><td><b>Error:</b> Session key is invalid or user '");
        buffer.append(userOrig);
        buffer.append("' (in combination with the defined e-mail address) is unknown.</td></tr>");
        return;
      }
      String messagesOrig = rs.getString(1);
      String teamOrig = rs.getString(2);
      Properties properties = new Properties();
      String prop = rs.getString(3);
      rs.close();
      if (prop == null) {
        prop = "";
      }
      properties.load(new ByteArrayInputStream(prop.getBytes()));
      String teamintroductionOrig = properties.getProperty("teamintroduction", "");
      String homepageOrig = properties.getProperty("homepage", "");
      String teamhomepageOrig = properties.getProperty("teamhomepage", "");
      String freetextOrig = properties.getProperty("freetext", "");
      String visibleemailOrig = properties.getProperty("visibleemail", "");
      String user = req.getParameter("user");
      String email = req.getParameter("email");
      String messages = req.getParameter("messages");
      if (messages == null || !messages.equals("Y")) {
        messages = "N";
      }
      String team = req.getParameter("team");
      String teamintroduction = req.getParameter("teamintroduction");
      if (teamintroduction != null && teamintroduction.length() > 255) {
        teamintroduction = teamintroduction.substring(0, 255);
      }
      String homepage = req.getParameter("homepage");
      if (homepage != null && homepage.trim().length() > 0 && homepage.indexOf(':') == -1) {
        homepage = "http://" + homepage;
      }
      String teamhomepage = req.getParameter("teamhomepage");
      if (teamhomepage == null || !teamhomepage.equals("true")) {
        teamhomepage = "false";
      }
      String freetext = req.getParameter("freetext");
      if (freetext != null && freetext.length() > 255) {
        freetext = freetext.substring(0, 255);
      }
      String visibleemail = req.getParameter("visibleemail");
      if (visibleemail != null && visibleemail.length() > 100) {
        visibleemail = visibleemail.substring(0, 100);
      }
      if (!user.equals(userOrig) || !email.equals(emailOrig)) {
        rs = stmt.executeQuery("SELECT id FROM zeta.user WHERE server_id=" + servlet.getServer().getId() + " AND name='" + user + "' AND email='" + email + '\'');
        if (rs.next()) {
          rs.close();
          buffer.append("<tr><td><b>Error:</b> User '");
          buffer.append(user);
          buffer.append("' (in combination with the defined e-mail address) is already defined.</td></tr>");
          return;
        }
        rs.close();
      }
      StringBuffer message1 = new StringBuffer(4000);
      StringBuffer message2 = new StringBuffer(2000);
      StringBuffer changeLocalConfig = new StringBuffer(500);
      message1.append("If you want to change your current properties\n");
      ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
      ZipOutputStream zip = new ZipOutputStream(out);
      zip.setLevel(Deflater.BEST_COMPRESSION);
      zip.putNextEntry(new ZipEntry("user_id"));
      StreamUtils.writeData(new ByteArrayInputStream(String.valueOf(userId).getBytes()), zip, true, false);
      zip.putNextEntry(new ZipEntry("user_orig"));
      StreamUtils.writeData(new ByteArrayInputStream(userOrig.getBytes()), zip, true, false);
      zip.putNextEntry(new ZipEntry("email_orig"));
      StreamUtils.writeData(new ByteArrayInputStream(emailOrig.getBytes()), zip, true, false);
      if (!user.equals(userOrig)) {
        zip.putNextEntry(new ZipEntry("user"));
        StreamUtils.writeData(new ByteArrayInputStream(user.getBytes()), zip, true, false);
        message1.append("\nuser: ");
        message1.append(userOrig);
        message2.append("\nuser: ");
        message2.append(user);
        changeLocalConfig.append("\nname=");
        changeLocalConfig.append(user);
      }
      if (!email.equals(emailOrig)) {
        zip.putNextEntry(new ZipEntry("email"));
        StreamUtils.writeData(new ByteArrayInputStream(email.getBytes()), zip, true, false);
        message1.append("\nemail: ");
        message1.append(emailOrig);
        message2.append("\nemail: ");
        message2.append(email);
        changeLocalConfig.append("\neMail=");
        changeLocalConfig.append(email);
      }
      if (!messages.equals(messagesOrig)) {
        zip.putNextEntry(new ZipEntry("messages"));
        StreamUtils.writeData(new ByteArrayInputStream(messages.getBytes()), zip, true, false);
        message1.append("\nreceive announcements: ");
        message1.append((messagesOrig.equals("Y"))? "yes" : "no");
        message2.append("\nreceive announcements: ");
        message2.append((messages.equals("Y"))? "yes" : "no");
        changeLocalConfig.append("\nmessages=");
        changeLocalConfig.append((messages.equals("Y"))? "true" : "false");
      }
      if (!team.equals(teamOrig)) {
        zip.putNextEntry(new ZipEntry("team"));
        StreamUtils.writeData(new ByteArrayInputStream(team.getBytes()), zip, true, false);
        message1.append("\nteam: ");
        message1.append(teamOrig);
        message2.append("\nteam: ");
        message2.append(team);
        changeLocalConfig.append("\nteam=");
        changeLocalConfig.append(team);
      }
      if (!teamintroduction.equals(teamintroductionOrig)) {
        zip.putNextEntry(new ZipEntry("teamintroduction"));
        StreamUtils.writeData(new ByteArrayInputStream(teamintroduction.getBytes()), zip, true, false);
        message1.append("\nteamintroduction: ");
        message1.append(teamintroductionOrig);
        message2.append("\nteamintroduction: ");
        message2.append(teamintroduction);
      }
      if (!homepage.equals(homepageOrig)) {
        zip.putNextEntry(new ZipEntry("homepage"));
        StreamUtils.writeData(new ByteArrayInputStream(homepage.getBytes()), zip, true, false);
        message1.append("\nhomepage: ");
        message1.append(homepageOrig);
        message2.append("\nhomepage: ");
        message2.append(homepage);
      }
      if (!teamhomepage.equals(teamhomepageOrig)) {
        zip.putNextEntry(new ZipEntry("teamhomepage"));
        StreamUtils.writeData(new ByteArrayInputStream(teamhomepage.getBytes()), zip, true, false);
        message1.append("\nis homepage for the team: ");
        message1.append((teamhomepageOrig.equals("true"))? "yes" : "no");
        message2.append("\nis homepage for the team: ");
        message2.append((teamhomepage.equals("true"))? "yes" : "no");
      }
      if (!freetext.equals(freetextOrig)) {
        zip.putNextEntry(new ZipEntry("freetext"));
        StreamUtils.writeData(new ByteArrayInputStream(freetext.getBytes()), zip, true, false);
        message1.append("\nfreetext: ");
        message1.append(freetextOrig);
        message2.append("\nfreetext: ");
        message2.append(freetext);
      }
      if (!visibleemail.equals(visibleemailOrig)) {
        zip.putNextEntry(new ZipEntry("visibleemail"));
        StreamUtils.writeData(new ByteArrayInputStream(visibleemail.getBytes()), zip, true, false);
        message1.append("\ne-mail address is visible to others: ");
        message1.append(visibleemailOrig);
        message2.append("\ne-mail address is visible to others: ");
        message2.append(visibleemail);
      }
      zip.flush();
      zip.close();
      message1.append("\n\nto the new properties\n");
      String title = "ZetaGrid needs your approval to change your current properties";
      message1.append(message2.toString());
      message1.append("\n\nthen click the following link to approve this:\n\n");
      ApproveProperties approveProperties = new ApproveProperties(servlet, userId, userOrig, emailOrig);
      message1.append(approveProperties.generateAddressToApprove(out.toByteArray()));
      message1.append("\n\nRemark: The server needs at least 1 hour to update your changes in the statistics after your approval.");
      if (changeLocalConfig.length() > 0) {
        message1.append("\n\n! Please remember to change the following line in your local configuration file 'zeta.cfg' before you restart your client (after the approval):\n");
        message1.append(changeLocalConfig.toString());
      }
      message1.append("\n\nThis key is valid for 7 days.");
      String messageText = message1.toString();
      if (SendMail.isValidEmailAddressSyntax(emailOrig)) {
        String smtpHostname = servlet.getServer().getSmtpHostName();
        int smtpPort = servlet.getServer().getSmtpPort();
        String smtpLoginName = servlet.getServer().getSmtpLoginName();
        String smtpPassword = servlet.getServer().getSmtpLoginPassword();
        servlet.log("send email to " + emailOrig + " using " + smtpHostname + ':' + smtpPort + " change properties");
        SendMail sendMail = new SendMail(smtpHostname, smtpPort, smtpLoginName, smtpPassword, "<office@zetagrid.net>", emailOrig, title, messageText);
        sendMail.setPop3Authentication(110);  // ToDo: configurable
        sendMail.setRealNameFrom(servlet.getServer().getSendMailFrom());
        sendMail.setRealNameTo("\"" + userOrig + "\" <" + emailOrig + '>');
        sendMail.start();
        buffer.append("The e-mail is successfully sent to '");
        buffer.append(emailOrig);
        buffer.append("' with the following content:\n<p>&nbsp;<br>");
        messageText = StringUtils.replace(messageText, "\n\n", "<br>&nbsp;<br>");
        messageText = StringUtils.replace(messageText, "\n", "<br>");
        idx = messageText.indexOf("to approve this:");
        if (idx >= 0) {
          messageText = messageText.substring(0, idx+16) + " ...";
        }
        buffer.append(messageText);
      } else {
        buffer.append("<b>Error:</b> The e-mail address '");
        buffer.append(emailOrig);
        buffer.append("' is not valid!");
      }
      buffer.append("<p><a href=\"");
      buffer.append(servlet.getRootPath());
      buffer.append(servlet.getHandlerPath(TopProducersHandler.class));
      buffer.append("\">Back to statistic 'top producers'</a></td></tr>");
      return;
    } catch (IOException ioe) {
    } finally {
      DatabaseUtils.close(stmt);
    }
    buffer.append("Internal server error!");
  }

  /**
   *  Appends time information.
  **/
  private void appendTimeInfo(int minutes, StringBuffer buffer) {
    if (minutes >= 2*24*60) {
      int days = minutes/(24*60);
      buffer.append(days);
      buffer.append(" days");
      minutes -= days*24*60;
      if (minutes > 0) {
        buffer.append(' ');
      }
    } else if (minutes >= 24*60) {
      buffer.append("1 day");
      minutes -= 24*60;
      if (minutes > 0) {
        buffer.append(' ');
      }
    }
    if (minutes >= 2*60) {
      int hours = minutes/(2*60);
      buffer.append(hours);
      buffer.append(" hours");
      minutes -= hours*60;
      if (minutes > 0) {
        buffer.append(' ');
      }
    } else if (minutes >= 60) {
      buffer.append("1 hour");
      minutes -= 60;
      if (minutes > 0) {
        buffer.append(' ');
      }
    }
    if (minutes > 1) {
      buffer.append(minutes);
      buffer.append(" minutes");
    } else if (minutes == 1) {
      buffer.append("1 minute");
    }
  }

  /**
   *  Contains the complete top producers statistic.
   *  The key is the task identifier and the value is the statistic table.
  **/
  private Map topProducersTables = new HashMap();

  /**
   *  DAYS of top producer list of yesterday.
  **/
  private long daysOfLowerUserNameListOrderByZerosYesterday = 0;

  /**
   *  DAYS of top producer list of the day before yesterday.
  **/
  private long daysOfLowerUserNameListOrderByZerosDayBeforeYesterday = 0;

  /**
   *  top producer list of yesterday.
  **/
  private List lowerUserNameListOrderByZerosYesterday = new ArrayList(1);

  /**
   *  top producer list of the day before yesterday.
  **/
  private List lowerUserNameListOrderByZerosDayBeforeYesterday = null;

  private final int NUMBER_ABOVE_BELOW_USER = 3;
}
