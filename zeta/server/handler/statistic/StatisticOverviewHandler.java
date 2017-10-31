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

package zeta.server.handler.statistic;

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.util.StreamUtils;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'overview'.
 *
 *  @version 2.0, August 6, 2005
**/
public class StatisticOverviewHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public StatisticOverviewHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'overview'.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'overview'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.GERMANY);
    StringBuffer buffer = new StringBuffer(5*1024);
    buffer.append("<ul>\n");
    buffer.append(" <li>Participating (");
    buffer.append(dateFormat.format(new Date()));
    buffer.append("):</li>\n");
    buffer.append(" <ul>\n");
    buffer.append("  <li>");
    Statement stmt = null;
    TopProducersHandler handler = (TopProducersHandler)servlet.getHandlerInstance(TopProducersHandler.class);
    if (handler != null) {
      Table table = handler.getTopProducersTable(taskId);
      if (table != null) {
        int l = table.getRowCount()-2;
        Set users = new HashSet(2*l);
        for (int i = 0; i < l; ++i) {
          String name = ((String)table.getValue(i, 0)).toLowerCase();
          int idx = name.lastIndexOf(',');
          if (idx > 0) {
            name = name.substring(0, idx);
          }
          users.add(name);
        }
        buffer.append(decimalFormat.format(new Integer(users.size())));
        buffer.append(" users and ");
        buffer.append(decimalFormat.format(new Integer(CachedQueries.getNumberOfComputers())));
        buffer.append(" computers</li>\n");
      } else {
        buffer.append("? users and ? computers</li>\n");
      }
    } else {
      buffer.append("? users and ? computers</li>\n");
    }
    buffer.append(" </ul>\n");
    buffer.append(" <li>");
    try {
      stmt = con.createStatement();
      double fpo = 0.0;
      double recomputedBillionZeros = 0.0;
      ResultSet rs = stmt.executeQuery("SELECT SUM(SQRT((comp.work_unit_id+comp.size)/12.566370614359)*148*1.245*1.246*comp.size),SUM(CAST(size AS DECIMAL(15, 0)))"
                           + " FROM zeta.computation comp,zeta.result res"
                           + " WHERE res.task_id=comp.task_id"
                           + " AND comp.task_id=" + taskId
                           + " AND res.work_unit_id=comp.work_unit_id");
      if (rs.next()) {
        fpo = rs.getDouble(1);
        double billionZeros = rs.getDouble(2)/100000000.0;
        rs.close();
        rs = stmt.executeQuery("SELECT SUM(SQRT((work_unit_id+size)/12.566370614359)*148*1.245*1.246*size),SUM(CAST(size AS DECIMAL(15, 0)))"
                             + " FROM zeta.recomputation"
                             + " WHERE stop IS NOT NULL AND task_id=" + taskId);
        if (rs.next()) {
          fpo += rs.getDouble(1);
          billionZeros += recomputedBillionZeros = rs.getDouble(2)/100000000.0;
        }
        billionZeros = Math.floor(billionZeros);
        double d = fpo;
        int exp = 0;
        while (d > 10.0) {
          d /= 10.0;
          ++exp;
        }
        int n = (int)Math.floor(d);
        buffer.append(n);
        buffer.append('.');
        buffer.append((int)Math.floor((d-n)*10.0));
        buffer.append("<font face=\"Symbol\">×</font>10<sup>");
        buffer.append(exp);
        buffer.append("</sup> floating-point operations for computing about ");
        buffer.append(((int)billionZeros)/10);
        buffer.append('.');
        buffer.append(((int)billionZeros)%10);
        buffer.append(" billion nontrivial zeros of the Riemann zeta function in ");
      }
      rs.close();
      int days = 1;
      rs = stmt.executeQuery("SELECT DAYS(CURRENT TIMESTAMP)-DAYS(MIN(start)) FROM zeta.computation WHERE task_id=" + taskId);
      if (rs.next()) {
        days = Math.max(1, rs.getInt(1));
        buffer.append(days);
        buffer.append(" days</li>");
      }
      rs.close();
      if (recomputedBillionZeros > 0.0) {
        buffer.append(" where ");
        buffer.append(((int)recomputedBillionZeros)/10);
        buffer.append('.');
        buffer.append(((int)recomputedBillionZeros)%10);
        buffer.append(" billion nontrivial zeros are computed twice");
      }
      buffer.append("</li><ul>\n");
      buffer.append("  <li>~");
      buffer.append((int)((fpo/(86400.0*days))/1000000000.0));
      buffer.append(" GFLOPS</li>\n");
      buffer.append("  <li>~");
      buffer.append((int)(fpo/7226000000000.0/3600.0));
      buffer.append(" hours maximal performance of IBM ASCI White, 8192 Power3 375 MHz processors (place 2, 06/2002, <a href=\"http://www.top500.org/list/2002/06/\">www.top500.org</a>)</li>\n");
      buffer.append("  <li>~");
      buffer.append((int)(fpo/250000000.0/3600.0/24.0/365.0));
      buffer.append(" years maximal performance of one Intel Pentium 4 processor with 2 GHz, 250 MFLOPS</li>\n");
      // ToDo: add number of recomputed zeros
      rs = stmt.executeQuery("SELECT DAYS(res.stop),MIN(res.stop),SUM(SQRT((comp.work_unit_id+comp.size)/12.566370614359)*148*1.245*1.246*comp.size) AS flops"
                           + " FROM zeta.computation comp,zeta.result res"
                           + " WHERE res.task_id=comp.task_id"
                           + " AND comp.task_id=" + taskId
                           + " AND res.work_unit_id=comp.work_unit_id"
                           + " GROUP BY DAYS(res.stop) ORDER BY flops DESC FETCH FIRST 1 ROWS ONLY");
      if (rs.next()) {
        buffer.append(" </ul>\n<li>Day with best performance (");
        days = rs.getInt(1);
        buffer.append(dateFormat.format(rs.getTimestamp(2)));
        buffer.append("):</li>\n <ul>\n  <li>");
        double d = fpo = rs.getDouble(3);
        int exp = 0;
        while (d > 10.0) {
          d /= 10.0;
          ++exp;
        }
        int n = (int)Math.floor(d);
        buffer.append(n);
        buffer.append('.');
        buffer.append((int)Math.floor((d-n)*10.0));
        buffer.append("<font face=\"Symbol\">×</font>10<sup>");
        buffer.append(exp);
        buffer.append("</sup> floating-point operations for calculating more than ");
        rs.close();
        rs = stmt.executeQuery("SELECT SUM(CAST(comp.size AS DECIMAL(15, 0)))"
                             + " FROM zeta.computation comp,zeta.result res"
                             + " WHERE comp.task_id=res.task_id"
                             + " AND comp.task_id=" + taskId
                             + " AND comp.work_unit_id=res.work_unit_id"
                             + " AND DAYS(res.stop)=" + days);
        if (rs.next()) {
          d = rs.getDouble(1)/1000000000.0;
          n = (int)Math.floor(d);
          buffer.append(n);
          buffer.append('.');
          buffer.append((int)Math.floor((d-n)*10.0));
          buffer.append(" billion zeros</li>\n");
        }
        buffer.append("  <li>~");
        buffer.append((int)(fpo/3600.0/24.0/1000000000.0));
        buffer.append(" GFLOPS</li>\n </ul>");
      }
      rs.close();
      // ToDo: add number of recomputed zeros
      rs = stmt.executeQuery("SELECT DAYS(res.stop),HOUR(res.stop),MIN(res.stop),SUM(SQRT((comp.work_unit_id+comp.size)/12.566370614359)*148*1.245*1.246*comp.size) AS flops"
                           + " FROM zeta.computation comp,zeta.result res"
                           + " WHERE res.task_id=comp.task_id"
                           + " AND comp.task_id=" + taskId
                           + " AND res.work_unit_id=comp.work_unit_id"
                           + " GROUP BY DAYS(res.stop),HOUR(res.stop)"
                           + " ORDER BY flops DESC FETCH FIRST 1 ROWS ONLY");
      if (rs.next()) {
        buffer.append(" <li>Hour with best performance (");
        buffer.append(new SimpleDateFormat("MM/dd/yyyy, hh:00-hh:59 a", Locale.GERMANY).format(rs.getTimestamp(3)));
        buffer.append("):</li>\n <ul>\n  <li>");
        double d = fpo = rs.getDouble(4);
        int exp = 0;
        while (d > 10.0) {
          d /= 10.0;
          ++exp;
        }
        int n = (int)Math.floor(d);
        buffer.append(n);
        buffer.append('.');
        buffer.append((int)Math.floor((d-n)*10.0));
        buffer.append("<font face=\"Symbol\">×</font>10<sup>");
        buffer.append(exp);
        buffer.append("</sup> floating-point operations</li>\n<li>~");
        buffer.append((int)(fpo/3600.0/1000000000.0));
        buffer.append(" GFLOPS</li>\n </ul>");
      }
      rs.close();
    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      DatabaseUtils.close(stmt);
    }
    return buffer.toString();
  }

  /**
   *  Creates HTML page with the content of the statistic 'overview'.
   *  @param taskId task identifier
   *  @param  req  GET request
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'overview'.
  **/
  protected String createPage(int taskId, HttpServletRequest req, Connection con) throws SQLException, ServletException {
    String enlarge = req.getParameter("enlarge");
    StringBuffer buffer = new StringBuffer(20*1024);
    final String[] titles = { "Performance characteristics:", "Summary of the computational results:", "Number of resource providers:",
                              "Number of computers:", "Reserved zeros:" };
    final String[] names = { "performance", "computational_results", "resource_providers", "computers", "reserved_zeros" };
    if (enlarge == null) {
      enlarge = names[0];
    } else {
      for (int i = names.length-1; i >= 0; --i) {
        if (enlarge.equals(names[i])) {
          enlarge = names[i];
          break;
        } else if (i == 0) {
          enlarge = names[i];
          break;
        }
      }
    }
    buffer.append("<tr><td><table>");
    for (int i = 0; i < titles.length; ++i) {
      if (enlarge == names[i]) {
        if (i == 0) {
          buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\">");
          buffer.append(titles[i]);
          buffer.append("</td></tr>\n");
          buffer.append("<tr><td>\n");
          InnerPageBuffer innerPageBuffer = null;
          try {
            innerPageBuffer = getInnerPageBuffer(taskId);
            if (innerPageBuffer != null) {
              ByteArrayOutputStream out = new ByteArrayOutputStream(10*1024);
              StreamUtils.writeData(innerPageBuffer.buffer, out, false, true);
              buffer.append(out.toString("ISO-8859-1"));
            }
          } catch (IOException ioe) {
          } finally {
            if (innerPageBuffer != null) {
              innerPageBuffer.close();
            }
          }
          buffer.append("</td></tr>\n");
        } else {
          buffer.append("<tr><td>\n");
          buffer.append("<p><center><a name=\"");
          buffer.append(names[i]);
          buffer.append("\"><img src=\"");
          buffer.append(servlet.getRootPath());
          buffer.append(servlet.getHandlerPath(getClass()));
          buffer.append("?task=");
          ServerTask serverTask = servlet.getTaskManager().getServerTask(taskId);
          if (serverTask != null) {
            buffer.append(URLEncoder.encode(serverTask.getName()));
          }
          buffer.append("&image=");
          buffer.append(names[i]);
          buffer.append("\" width=\"");
          buffer.append(IMAGE_WIDTH);
          buffer.append("\" height=\"");
          buffer.append(IMAGE_HEIGHT);
          buffer.append("\"></a></center></p>\n");
          buffer.append("</td></tr>\n");
        }
        break;
      }
    }
    buffer.append("<tr><td><center><table cellpadding=\"5\" cellspacing=\"5\"><colgroup><col width=\"200\"><col width=\"200\"></colgroup>\n");
    String rootPath = DispatcherServlet.getRootPath();
    int idx = rootPath.indexOf(DispatcherServlet.getServletPath());
    if (idx >= 0) {
      rootPath = rootPath.substring(0, idx);
    }
    idx = 0;
    int prevI = 0;
    for (int i = 0; i < titles.length; ++i) {
      if (enlarge != names[i]) {
        if (idx == 0 || idx == 2) {
          buffer.append("<tr>");
        }
        if (idx <= 1) {
          buffer.append("<td><center><b>");
          buffer.append(titles[i]);
          buffer.append("</b></center></td>");
        } else {
          buffer.append("<td><p><center><a href=\"statistic?enlarge=");
          buffer.append(names[i]);
          buffer.append("\"><img src=\"");
          buffer.append(rootPath);
          buffer.append("/images/");
          buffer.append(names[i]);
          buffer.append(".gif\" width=\"140\" height=\"100\"></a></center></td>");
        }
        if (idx == 1) {
          buffer.append("</tr>\n");
          i = prevI-1;
        } else if (idx == 3) {
          buffer.append("</tr>\n");
        }
        if (++idx >= 4) {
          idx = 0;
        }
        prevI = i;
      }
    }
    buffer.append("</table></center></td></tr>\n");
    buffer.append("<tr><td><p>&nbsp;<center><table>\n<tr><td><b>Download statistic data:</b></td></tr>\n");
    for (int i = 0, l = servlet.getNumberOfHandlers(); i < l; ++i) {
      Class c = servlet.getHandlerClass(i);
      if (c != StatisticOverviewHandler.class && (servlet.getHandlerInstance(c) instanceof AbstractHandler)) {
        String displayName = servlet.getHandlerDisplayName(c);
        if (displayName != null) {
          buffer.append("<tr><td><a href=\"");
          buffer.append(servlet.getRootPath());
          buffer.append("/statistic/");
          buffer.append(c.getName());
          buffer.append('.');
          buffer.append(taskId);
          buffer.append(".xml.zip\">");
          buffer.append(displayName);
          buffer.append("</a></td></tr>\n");
        }
      }
    }
    buffer.append("</table></center></p></td></tr></table></td></tr>");
    return buffer.toString();
  }

  /**
   *  No image is available on this statistic.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @param  imageName name of the image
   *  @return null
  **/
  public BufferedImage createImage(int taskId, Connection con, String imageName) throws SQLException, ServletException {
    if (imageName.equals("computational_results")) {
      final String[] verticalAxisLabels = { "Number of computed zeros", "Number of computed zeros per day", "1,000,000,000,000 floating-point operations per day",
                                            "Last record", "First target", "Second target", "Third target", "Fourth target" };
      final Paint[] colors = { Color.blue, Color.red, Color.green, Color.orange, new Color(0.0f, 1.0f, 0.0f), new Color(1.0f, 0.5f, 0.0f), new Color(0.0f, 0.0f, 0.5f), new Color(0.0f, 0.75f, 0.75f) };
      final int[] weight = { 5, 1, 1 };
      final int[][] combination = { {0, 3, 4, 5, 6, 7}, {1}, {2} };
      return Charts.generateChart(IMAGE_WIDTH, IMAGE_HEIGHT, "Summary of the computational results", "Date", verticalAxisLabels, colors, weight, combination, Charts.createNumberOfComputedZeros(taskId, con));
    } else if (imageName.equals("resource_providers")) {
      final String[] verticalAxisLabels = { "Number of resource providers", "Number of new resource providers", "Number of active resource providers" };
      final Paint[] colors = { Color.blue, Color.red, Color.green };
      final int[] weight = { 5, 1, 1 };
      final int[][] combination = { {0}, {1}, {2} };
      return Charts.generateChart(IMAGE_WIDTH, IMAGE_HEIGHT, "Number of resource providers", "Date", verticalAxisLabels, colors, weight, combination, Charts.createNumberOfParticipants(taskId, con));
    } else if (imageName.equals("computers")) {
      final String[] verticalAxisLabels = { "Number of computers", "Number of new computers", "Number of active computers" };
      final Paint[] colors = { Color.blue, Color.red, Color.green };
      final int[] weight = { 5, 1, 1 };
      final int[][] combination = { {0}, {1}, {2} };
      return Charts.generateChart(IMAGE_WIDTH, IMAGE_HEIGHT, "Number of computers", "Date", verticalAxisLabels, colors, weight, combination, Charts.createNumberOfComputers(taskId, con));
    } else if (imageName.equals("reserved_zeros")) {
      final String[] verticalAxisLabels = { "Reserved zeros today (" + (new SimpleDateFormat("MM/dd/yyyy", Locale.GERMANY).format(new Date())) + ')',
                                            "Reserved zeros yesterday", "Number of reserved work units today", "Number of reserved work units yesterday" };
      final Paint[] colors = { Color.blue, Color.cyan, Color.red, Color.orange };
      final int[] weight = { 6, 1 };
      final int[][] combination = { {0, 1}, {2, 3} };
      return Charts.generateChart(IMAGE_WIDTH, IMAGE_HEIGHT, "Reserved zeros", "Hour", verticalAxisLabels, colors, weight, combination, Charts.createNewReservedZeros(taskId, con));
    }
    return null;
  }

  private static final int IMAGE_WIDTH = 700;
  private static final int IMAGE_HEIGHT = 500;
}
