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
     S. Wedeniwski
--*/

package zeta.server.handler.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.TaskManager;
import zeta.server.handler.GetHandler;
import zeta.server.util.DatabaseUtils;

/**
 *  @version 2.0, August 6, 2005
**/
public class WorkUnitStatusReportHandler implements GetHandler {

  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public WorkUnitStatusReportHandler(DispatcherServlet servlet) {
    this.servlet = servlet;
  }

  /**
   *  Handles a GET request <code>resp</code> for the specified statistic.
   *  The response <code>resp</code> contains either the HTML page or an image.
  **/
  final public void doGet(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    int taskId = (task == null)? 0 : task.getId();
    Connection con = null;
    try {
      con = servlet.getConnection();
      StringBuffer buffer = new StringBuffer(5000);
      for (int i = 0;; ++i) {
        String workUnit = req.getParameter(String.valueOf(i));
        if (workUnit == null) {
          break;
        }
        int j = workUnit.indexOf('_');
        if (j < 0 || j+1 == workUnit.length()) {
          break;
        }
        Statement stmt = null;
        try {
          stmt = con.createStatement();
          stmt.executeUpdate("INSERT INTO zeta.result (task_id,work_unit_id) VALUES (" + workUnit.substring(0, j) + ',' + workUnit.substring(j+1) + ')');
          workUnitsLastUpdateTimeMillis = 0;
        } catch (SQLException se) {
          throw new ServletException(se);
        } finally {
          DatabaseUtils.close(stmt);
        }
      }
      if (System.currentTimeMillis()-workUnitsLastUpdateTimeMillis > 10*60*1000 || workUnits.size() == 0) {
        workUnitsLastUpdateTimeMillis = System.currentTimeMillis();
        synchronized (workUnits) {
          workUnits.clear();
          Statement stmt = null;
          try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT work_unit_id FROM zeta.computation WHERE task_id=" + taskId
                                           + " AND work_unit_id NOT IN (SELECT work_unit_id FROM zeta.result WHERE task_id=" + taskId
                                           + ") ORDER BY start");
            while (rs.next()) {
              workUnits.add(new Long(rs.getLong(1)));
            }
            rs.close();
          } catch (SQLException se) {
            throw new ServletException(se);
          } finally {
            DatabaseUtils.close(stmt);
          }
        }
        workUnitsLastUpdateTimeMillis = System.currentTimeMillis();
      }
      buffer.append("<tr><td><br><form type=\"GET\" action=\"");
      String servletPath = servlet.getRootPath() + servlet.getHandlerPath(getClass());
      buffer.append(servletPath);
      buffer.append("\">Task name: <select name=\"task\">");
      TaskManager taskManager = TaskManager.getInstance(con);
      Iterator iter = taskManager.getServerTasks();
      while (iter.hasNext()) {
        ServerTask serverTask = (ServerTask)iter.next();
        if (task == null) {
          task = serverTask;
          taskId = task.getId();
        }
        buffer.append("<option value=\"");
        buffer.append(serverTask.getName());
        buffer.append((serverTask.getId() == task.getId())? "\" selected>" : "\">");
        buffer.append(serverTask.getName());
      }
      buffer.append("</select>&nbsp;&nbsp;<input type=\"submit\" value=\"Select\"></form></td></tr>");
      buffer.append("<tr><td><p>Currently ");
      synchronized (workUnits) {
        buffer.append(workUnits.size());
      }
      buffer.append(" active work units:<br><small>");
      long startWorkUnit = 0;
      String start = req.getParameter("start");
      if (start != null) {
        try {
          startWorkUnit = Long.parseLong(start);
        } catch (NumberFormatException nfe) {
          startWorkUnit = 0;
        }
      }
      synchronized (workUnits) {
        if (startWorkUnit == 0 && workUnits.size() > 0) {
          startWorkUnit = ((Long)workUnits.get(0)).longValue();
        }
      }
      addPages(taskId, startWorkUnit, servletPath, buffer);
      buffer.append("</small><center><form method=\"GET\" action=\"");
      buffer.append(servletPath);
      buffer.append("\"><table border=\"0\" width=\"100%\"><tr><td align=\"left\"><input type=\"submit\" value=\"Delete\"></td></tr>");
      buffer.append("<tr><td><table border=\"1\" width=\"100%\"><colgroup><col width=\"4%\"><col width=\"11%\"/><col width=\"40%\"/><col width=\"20%\"/><col width=\"25%\"/></colgroup>");
      buffer.append("<tr bgcolor=\"dddddd\"><th>&nbsp;</th><th>Work unit</th><th>Hostname</th><th>OS</th><th>Start</th></tr>");
      boolean colorLine = false;
      synchronized (workUnits) {
        StringBuffer sql = new StringBuffer(20000);
        sql.append("SELECT comp.work_unit_id,ws.hostname,ws.os_name,ws.os_arch,comp.start FROM zeta.workstation ws, zeta.computation comp");
        sql.append(" WHERE ws.server_id=comp.server_id AND ws.id=comp.workstation_id AND task_id=");
        sql.append(taskId);
        sql.append(" AND work_unit_id IN (");
        int i = workUnits.indexOf(new Long(startWorkUnit));
        if (i >= 0) {
          sql.append(workUnits.get(i));
          for (int j = 0, l = workUnits.size(); ++i < l && j < 99; ++j) {
            sql.append(',');
            sql.append(workUnits.get(i));
          }
          sql.append(") ORDER BY comp.start");
          Statement stmt = null;
          try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString());
            i = 0;
            while (rs.next()) {
              buffer.append((colorLine)? "<tr bgcolor=\"eeeeee\">" : "<tr>");
              colorLine = !colorLine;
              buffer.append("<td><input type=\"checkbox\" name=\"");
              buffer.append(i++);
              buffer.append("\" value=\"");
              buffer.append(taskId);
              buffer.append('_');
              long workUnitId = rs.getLong(1);
              buffer.append(workUnitId);
              buffer.append("\"></td><td>");
              buffer.append(workUnitId);
              buffer.append("</td><td>");
              buffer.append(rs.getString(2));
              buffer.append("</td><td>");
              buffer.append(rs.getString(3));
              buffer.append(" (");
              buffer.append(rs.getString(4));
              buffer.append(")</td><td>");
              buffer.append(rs.getTimestamp(5));
              buffer.append("</td></tr>");
            }
            rs.close();
          } catch (SQLException se) {
            throw new ServletException(se);
          } finally {
            DatabaseUtils.close(stmt);
          }
        }
      }
      buffer.append("</td></tr></table><tr><td align=\"left\"><input type=\"submit\" value=\"Delete\"></td></tr></table>\n</form></center></td></tr>");

      /*buffer.append("<h2>Already finished work units</h2><center>");
      table = new Query(null, "SELECT comp.work_unit_id AS \"work unit\","
                                  + "ws.hostname AS \"hostname\","
                                  + "ws.os_name AS \"operating system\","
                                  + "ws.os_arch AS \"processor\","
                                  + "comp.start AS \"start\","
                                  + "res.stop AS \"stop\""
                                  + " FROM zeta.workstation ws, zeta.computation comp, zeta.result res"
                                  + " WHERE ws.server_id=comp.server_id"
                                  + " AND ws.id=comp.workstation_id"
                                  + " AND comp.task_id=res.task_id"
                                  + " AND comp.work_unit_id=res.work_unit_id"
                                  + " ORDER BY comp.work_unit_id", con, servlet).getResult();
      buffer.append(generator.generate(table));*/

      resp.setContentType("text/html");
      resp.setContentLength(buffer.length());
      resp.getWriter().print(buffer.toString());
    } catch (SQLException se) {
      throw new ServletException(se);
    } finally {
      DatabaseUtils.close(con);
    }
  }

  /**
   *  Timestamp in millis of the last update of the data of the page.
  **/
  public long getTimestampOfPage() {
    return workUnitsLastUpdateTimeMillis;
  }

  private void addPages(int taskId, long startWorkUnit, String servletPath, StringBuffer buffer) {
    synchronized (workUnits) {
      int l = workUnits.size();
      int pos = 0;
      for (int i = 0; i < l; i += 100) {
        long from = ((Long)workUnits.get(i)).longValue();
        int j = (i+99 < l)? i+99 : l-1;
        long to = ((Long)workUnits.get(j)).longValue();
        if (startWorkUnit >= from && startWorkUnit <= to) {
          pos = i;
        }
      }
      if (l <= 500) {
        addPagesFromTo(0, l, startWorkUnit, servletPath, buffer);
      } else {
        if (pos < 100) {
          addPagesFromTo(0, 200, startWorkUnit, servletPath, buffer);
        } else if (pos < 200) {
          addPagesFromTo(0, 300, startWorkUnit, servletPath, buffer);
        } else if (pos < 300) {
          addPagesFromTo(0, 400, startWorkUnit, servletPath, buffer);
        } else {
          addPagesFromTo(0, 100, startWorkUnit, servletPath, buffer);
        }
        int endPos = (((l+99)/100)-1)*100;
        buffer.append("...&nbsp;&nbsp;");
        if (pos >= endPos) {
          addPagesFromTo(endPos-100, l, startWorkUnit, servletPath, buffer);
        } else if (pos >= endPos-100) {
          addPagesFromTo(endPos-200, l, startWorkUnit, servletPath, buffer);
        } else if (pos >= endPos-200) {
          addPagesFromTo(endPos-300, l, startWorkUnit, servletPath, buffer);
        } else {
          if (pos >= 300) {
            addPagesFromTo(pos-100, pos+200, startWorkUnit, servletPath, buffer);
            buffer.append("...&nbsp;&nbsp;");
          }
          addPagesFromTo(endPos, l, startWorkUnit, servletPath, buffer);
        }
      }
    }
  }

  private static void addPagesFromTo(int fromPageIdx, int toPageIdx, long startWorkUnit, String servletPath, StringBuffer buffer) {
    for (int i = fromPageIdx; i < toPageIdx; i += 100) {
      long from = ((Long)workUnits.get(i)).longValue();
      int j = (i+99 < toPageIdx)? i+99 : toPageIdx;
      long to = ((Long)workUnits.get(j-1)).longValue();
      if (startWorkUnit < from || startWorkUnit > to) {
        buffer.append("<a href=\"");
        buffer.append(servletPath);
        buffer.append("?start=");
        buffer.append(from);
        buffer.append("\">");
      }
      buffer.append(from);
      buffer.append("...");
      buffer.append(to);
      if (startWorkUnit < from || startWorkUnit > to) {
        buffer.append("</a>");
      }
      buffer.append("&nbsp;&nbsp;");
    }
  }

  private static List workUnits = new ArrayList(1000);
  private static long workUnitsLastUpdateTimeMillis = 0;

  /**
   *  Servlet which owns this handler.
  **/
  private DispatcherServlet servlet;
}
