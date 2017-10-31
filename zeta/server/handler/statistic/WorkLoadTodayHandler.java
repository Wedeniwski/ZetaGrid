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

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.QueryWithSum;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'workload today'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class WorkLoadTodayHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public WorkLoadTodayHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'workload today'.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'workload today'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    StringBuffer buffer = new StringBuffer(70*1024);
    buffer.append("<tr><td><center><img src=\"");
    buffer.append(servlet.getRootPath());
    buffer.append(servlet.getHandlerPath(getClass()));
    buffer.append("?task=");
    buffer.append(URLEncoder.encode(servlet.getTaskManager().getServerTask(taskId).getName()));
    buffer.append("&image=workload\"/ width=\"");
    buffer.append(IMAGE_WIDTH);
    buffer.append("\" height=\"");
    buffer.append(IMAGE_HEIGHT);
    buffer.append("\"></center>");
    buffer.append("</td></tr>");
    buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Workload today:</center></td></tr>");
    buffer.append("<tr><td>");
    buffer.append("<br><center>");

    HtmlTableGenerator generator = new HtmlTableGeneratorWithSum(servlet);
    /*Table table = new QueryWithSum(null, "SELECT SUBSTR(CHAR(TIME(res.stop),ISO),1,2) || ':00 - ' || SUBSTR(CHAR(TIME(res.stop),ISO),1,2) || ':59' AS \"time\","
                                       + "COUNT(res.work_unit_id) AS \"work units\","
                                       + "COUNT(DISTINCT comp.workstation_id) AS \"workstations\","
                                       + "COUNT(DISTINCT user_id) AS \"users\","
                                       + "SUM(comp.size) AS \"zeros\" FROM zeta.result res, zeta.computation comp"
                                       + " WHERE comp.task_id=res.task_id AND comp.task_id=" + taskId
                                       + " AND comp.work_unit_id=res.work_unit_id"
                                       + " AND DAYS(stop) IN (select max(DAYS(stop)) FROM zeta.result)"
                                       + " GROUP BY SUBSTR(CHAR(TIME(res.stop),ISO),1,2)", con, servlet).getResult();*/

    Map workloadSummary = getWorkloadSummary(taskId, con);
    Table table = new Table(5);
    table.setColumnName(0, "time");
    table.setType(0, Types.VARCHAR);
    table.setAlignment(0, Table.LEFT);
    table.setColumnName(1, "work units");
    table.setType(1, Types.INTEGER);
    table.setAlignment(1, Table.RIGHT);
    table.setColumnName(2, "workstations");
    table.setType(2, Types.INTEGER);
    table.setAlignment(2, Table.RIGHT);
    table.setColumnName(3, "users");
    table.setType(3, Types.INTEGER);
    table.setAlignment(3, Table.RIGHT);
    table.setColumnName(4, "zeros");
    table.setType(4, Types.INTEGER);
    table.setAlignment(4, Table.RIGHT);
    int day = 0;
    Iterator i = workloadSummary.keySet().iterator();
    while (i.hasNext()) {
      day = Math.max(day, ((Integer)i.next()).intValue()/24);
    }
    int hours = 0;
    i = workloadSummary.keySet().iterator();
    while (i.hasNext()) {
      if (((Integer)i.next()).intValue()/24 == day) {
        ++hours;
      }
    }
    for (int hour = 0; hour < 24; ++hour) {
      Object[] values = (Object[])workloadSummary.get(new Integer(day*24+hour));
      if (values == null) {
        if (hours > 0) {
          continue;
        }
        break;
      }
      --hours;
      int row = table.getRowCount();
      table.addRow();
      table.setValue(row, 0, ((hour < 10)? "0" : "") + hour + ":00 - " + ((hour < 10)? "0" : "") + hour + ":59");
      table.setValue(row, 1, values[0]);
      table.setValue(row, 2, values[1]);
      table.setValue(row, 3, values[2]);
      table.setValue(row, 4, values[3]);
    }
    QueryWithSum.addSum(table);
    buffer.append(generator.generate(table));
    buffer.append("</center></td></tr>");
    return buffer.toString();
  }

  /**
   *  Creates PNG image for a specified name which is defined in the HTML page.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @param  imageName name of the image
   *  @return PNG image for a specified name which is defined in the HTML page.
  **/
  public BufferedImage createImage(int taskId, Connection con, String imageName) throws SQLException, ServletException {
    final String[] verticalAxisLabels = { "Workload today (" + (new SimpleDateFormat("MM/dd/yyyy", Locale.GERMANY).format(new Date())) + ')',
                                          "Workload yesterday", "Workload per hour today", "Workload per hour yesterday" };
    final Paint[] colors = { Color.blue, Color.cyan, Color.red, Color.orange };
    final int[] weight = { 6, 1 };
    final int[][] combination = { {0, 1}, {2, 3} };
    return Charts.generateChart(IMAGE_WIDTH, IMAGE_HEIGHT, "Workload", "Hour", verticalAxisLabels, colors, weight, combination, Charts.createWorkLoad(con, getWorkloadSummary(taskId, con)));
  }

  // Map: DAY_OF_YEAR(stop)*24+HOUR(stop) -> COUNT(work_unit_id),COUNT(DISTINCT workstation_id),COUNT(DISTINCT user_id),SUM(size)
  // Type: Integer -> Timestamp,Integer,Integer,Integer,Long
  private Map getWorkloadSummary(int taskId, Connection con) throws SQLException {
    Map workloadSummary = new HashMap(50);
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      long maxDays = CachedQueries.getMaxDays(taskId, stmt);
      Calendar cal = Calendar.getInstance();
      ResultSet rs = stmt.executeQuery("SELECT res.stop,comp.server_id,comp.workstation_id,comp.user_id,comp.size"
                                     + " FROM zeta.result res, zeta.computation comp"
                                     + " WHERE res.task_id=comp.task_id AND comp.task_id=" + taskId
                                     + " AND res.work_unit_id=comp.work_unit_id"
                                     + " AND (DAYS(stop)=" + (maxDays-1) + " OR DAYS(stop)=" + maxDays + ')');
      for (int i = 0; i < 2; ++i) {
        while (rs.next()) {
          Timestamp stop = rs.getTimestamp(1);
          Integer serverId = new Integer(rs.getInt(2));
          Integer workstationId = new Integer(rs.getInt(3));
          Integer userId = new Integer(rs.getInt(4));
          int size = rs.getInt(5);
          cal.setTime(stop);
          Integer key = new Integer(cal.get(Calendar.DAY_OF_YEAR)*24 + cal.get(Calendar.HOUR_OF_DAY));
          Object[] value = (Object[])workloadSummary.get(key);
          if (value == null) {
            value = new Object[] { new Integer(1), new HashMap(), new HashMap(), new Long(size) };
            workloadSummary.put(key, value);
          } else {
            value[0] = new Integer(((Integer)value[0]).intValue() + 1);
            value[3] = new Long(((Long)value[3]).longValue() + size);
          }
          Set set = (Set)((Map)value[1]).get(serverId);
          if (set == null) {
            set = new HashSet();
            ((Map)value[1]).put(serverId, set);
          }
          set.add(workstationId);
          set = (Set)((Map)value[2]).get(serverId);
          if (set == null) {
            set = new HashSet();
            ((Map)value[2]).put(serverId, set);
          }
          set.add(userId);
        }
        rs.close();
        if (i == 0) {
          rs = stmt.executeQuery("SELECT stop,server_id,workstation_id,user_id,size"
                               + " FROM zeta.recomputation"
                               + " WHERE task_id=" + taskId + " AND stop IS NOT NULL"
                               + " AND (DAYS(stop)=" + (maxDays-1) + " OR DAYS(stop)=" + maxDays + ')');
        } else {
          break;
        }
      }
      Iterator i = workloadSummary.keySet().iterator();
      while (i.hasNext()) {
        Integer key = (Integer)i.next();
        Object[] value = (Object[])workloadSummary.get(key);
        for (int k = 1; k <= 2; ++k) {
          int count = 0;
          Iterator j = ((Map)value[k]).values().iterator();
          while (j.hasNext()) {
            count += ((Set)j.next()).size();
          }
          value[k] = new Integer(count);
        }
      }
    } finally {
      DatabaseUtils.close(stmt);
    }
    return workloadSummary;
  }

  private static final int IMAGE_WIDTH = 700;
  private static final int IMAGE_HEIGHT = 500;
}
