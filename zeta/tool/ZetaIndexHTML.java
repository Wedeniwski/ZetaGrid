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

package zeta.tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import zeta.server.tool.ConstantProperties;
import zeta.server.tool.Database;
import zeta.server.tool.FTP;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.util.Properties;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ZetaIndexHTML {
  public static void generate() throws Exception {
    Connection connection = null;
    Statement stmt = null;
    try {
      Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
      String source = properties.get("index.html.source");
      String destination = properties.get("index.html.destination.1");
      if (source != null && destination != null) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(20000);
        StreamUtils.writeData(new FileInputStream(source), out, true, true);
        String sourcePage = out.toString();
        StringBuffer destinationPage = new StringBuffer(sourcePage.length()+100);
        connection = Database.getConnection();
        stmt = connection.createStatement();
        int idx = sourcePage.indexOf("This implementation involves more than");
        if (idx == -1) {
          return;
        }
        destinationPage.append(sourcePage.substring(0, idx));
        destinationPage.append("This implementation involves more than ");
        int computers = CachedQueries.getNumberOfComputers();
        destinationPage.append(format(computers, true));
        destinationPage.append(" workstations");
        idx = sourcePage.indexOf(" and ", idx);
        int idx2 = sourcePage.indexOf("peak performance", idx);
        if (idx2 == -1) {
          return;
        }
        destinationPage.append(sourcePage.substring(idx, idx2));
        destinationPage.append("peak performance rate of about ");
        ResultSet rs = stmt.executeQuery("SELECT DAYS(res.stop),HOUR(res.stop),SUM(SQRT((comp.work_unit_id+comp.size)/12.566370614359)*148*1.245*1.246*comp.size) AS flops"
                                       + " FROM zeta.computation comp,zeta.result res"
                                       + " WHERE res.task_id=comp.task_id"
                                       + " AND res.work_unit_id=comp.work_unit_id"
                                       + " GROUP BY DAYS(res.stop),HOUR(res.stop)"
                                       + " ORDER BY flops DESC FETCH FIRST 1 ROWS ONLY");
        if (rs.next()) {
          double fpo = rs.getDouble(3);
          destinationPage.append((int)(fpo/3600.0/1000000000.0));
        }
        rs.close();
        destinationPage.append(" GFLOPS.\n");
        idx = sourcePage.indexOf("More", idx2);
        idx2 = sourcePage.indexOf("Currently participating", idx);
        if (idx2 == -1) {
          return;
        }
        idx2 = sourcePage.indexOf("<a href=\"", idx2);
        if (idx2 == -1) {
          return;
        }
        String linkAddress = "";
        int idx3 = idx2 += 9;
        while (idx2 < sourcePage.length() && !Character.isDigit(sourcePage.charAt(idx2))) {
          if (sourcePage.charAt(idx2) == '/') {
            linkAddress = sourcePage.substring(idx3, idx2+1);
          }
          ++idx2;
        }
        // Currently participating
        destinationPage.append(sourcePage.substring(idx, idx2));
        destinationPage.append(format(computers, false));
        destinationPage.append(" computers</a>:<table>");
        Map os = new HashMap();
        rs = stmt.executeQuery("WITH a (server_id,workstation_id,os_name,os_arch) AS"
                           + " (SELECT DISTINCT comp.server_id,comp.workstation_id,ws.os_name,ws.os_arch"
                           + "   FROM zeta.computation comp, zeta.result res, zeta.workstation ws"
                           + "   WHERE res.task_id=comp.task_id"
                           + "   AND res.work_unit_id=comp.work_unit_id"
                           + "   AND comp.server_id=ws.server_id"
                           + "   AND comp.workstation_id=ws.id"
                           + "   AND ws.os_arch<>'')"
                           + "SELECT os_name AS \"operating system\",os_arch AS \"processor\",COUNT(*) AS \"computers\""
                           + " FROM a GROUP BY os_name,os_arch ORDER BY os_name,os_arch");
        while (rs.next()) {
          String osName = rs.getString(1);
          String osArch = rs.getString(2);
          if (osName.startsWith("Windows")) {
            osName = "Windows";
          } else if (osArch.startsWith("sparc")) {
            osArch = "sparc";
          } else if (osName.startsWith("Linux") && osArch.endsWith("86")) {
            osArch = "x86";
          }
          String text = osArch + " on " + osName;
          Integer count = (Integer)os.get(text);
          if (count == null) {
            count = new Integer(0);
          }
          os.put(text, new Integer(count.intValue()+rs.getInt(3)));
        }
        rs.close();
        while (os.size() > 0) {
          Map.Entry maxEntry = null;
          Iterator iter = os.entrySet().iterator();
          while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (maxEntry == null || ((Integer)entry.getValue()).intValue() > ((Integer)maxEntry.getValue()).intValue()) {
              maxEntry = entry;
            }
          }
          destinationPage.append("\n<tr><td align=right><small>&nbsp;&nbsp;");
          destinationPage.append(format(((Integer)maxEntry.getValue()).intValue(), false));
          destinationPage.append("</td><td><small>&nbsp;");
          destinationPage.append(maxEntry.getKey());
          destinationPage.append("</small></td></tr>");
          os.remove(maxEntry.getKey());
        }
        idx = sourcePage.indexOf("</table>", idx2);
        idx2 = sourcePage.indexOf("Performance", idx);
        if (idx2 == -1) {
          return;
        }
        destinationPage.append(sourcePage.substring(idx, idx2));
        // Performance
        destinationPage.append("Performance</b><p>~");
        rs = stmt.executeQuery("SELECT DAYS(res.stop),SUM(SQRT((comp.work_unit_id+comp.size)/12.566370614359)*148*1.245*1.246*comp.size) AS flops"
                             + " FROM zeta.computation comp,zeta.result res"
                             + " WHERE res.task_id=comp.task_id"
                             + " AND res.work_unit_id=comp.work_unit_id"
                             + " GROUP BY DAYS(res.stop) ORDER BY flops DESC FETCH FIRST 1 ROWS ONLY");
        if (rs.next()) {
          double fpo = rs.getDouble(2);
          destinationPage.append((int)(fpo/3600.0/24.0/1000000000.0));
        }
        rs.close();
        idx = sourcePage.indexOf(" GFLOPS", idx2);
        idx2 = sourcePage.indexOf("Top team<br><small>(last 7 days)", idx);
        if (idx2 == -1) {
          return;
        }
        destinationPage.append(sourcePage.substring(idx, idx2));
        // Top team (last 7 days)
        destinationPage.append("Top team<br><small>(last 7 days)</small></b><p><small>");
        rs = stmt.executeQuery("SELECT LOWER(c.team_name) AS \"team\","
                             + " COUNT(DISTINCT workstation_id) AS \"computers used\","
                             + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\","
                             + " COUNT(DISTINCT LOWER(c.name)) AS \"members\""
                             + " FROM zeta.computation a, zeta.result b, zeta.user c"
                             + " WHERE b.stop IS NOT NULL"
                             + " AND TIMESTAMPDIFF(4, CAST(CURRENT TIMESTAMP-b.stop AS CHAR(22)))<=10080"
                             + " AND a.task_id=b.task_id"
                             + " AND a.work_unit_id=b.work_unit_id"
                             + " AND a.user_id=c.id"
                             + " AND NOT c.team_name IS NULL"
                             + " AND LENGTH(RTRIM(c.team_name))>0"
                             + " AND a.server_id=c.server_id"
                             + " GROUP BY LOWER(c.team_name) ORDER BY \"zeros\" DESC FETCH FIRST 1 ROWS ONLY");
        String name = "";
        int computersUsed = 0;
        long zeros = 0;
        int members = 0;
        if (rs.next()) {
          name = rs.getString(1);
          computersUsed = rs.getInt(2);
          zeros = rs.getLong(3);
          members = rs.getInt(4);
        }
        rs.close();
        if (name != null && name.length() > 0) {
          rs = stmt.executeQuery("SELECT team_name FROM zeta.user WHERE id>0 AND LOWER(team_name)='" + name + "' ORDER BY id");
          if (rs.next()) {
            name = rs.getString(1);
          }
          rs.close();
          destinationPage.append("<a href=\"");
          destinationPage.append(linkAddress);
          destinationPage.append("teammembers?team=");
          destinationPage.append(URLEncoder.encode(name, "ISO-8859-1"));
          destinationPage.append("\">");
          destinationPage.append(name);
          destinationPage.append("</a><br>");
          destinationPage.append(decimalFormat.format(new Integer(members)));
          destinationPage.append(" active members<br>delivered ");
          destinationPage.append(decimalFormat.format(new Long(zeros)));
          destinationPage.append(" zeros<br>used ");
          destinationPage.append(decimalFormat.format(new Integer(computersUsed)));
          destinationPage.append(" computer(s)");
        }
        idx = sourcePage.indexOf("</small>", sourcePage.indexOf("<a ", idx2));
        idx2 = sourcePage.indexOf("Top producer<br><small>(last 7 days)", idx);
        if (idx2 == -1) {
          return;
        }
        destinationPage.append(sourcePage.substring(idx, idx2));
        // Top producer (last 7 days)
        destinationPage.append("Top producer<br><small>(last 7 days)</small></b><p><small>");
        rs = stmt.executeQuery("SELECT LOWER(c.name) AS \"user\","
                             + " COUNT(DISTINCT workstation_id) AS \"computers used\","
                             + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                             + " FROM zeta.computation a, zeta.result b, zeta.user c"
                             + " WHERE b.stop IS NOT NULL"
                             + " AND TIMESTAMPDIFF(4, CAST(CURRENT TIMESTAMP-b.stop AS CHAR(22)))<=10080"
                             + " AND a.task_id=b.task_id"
                             + " AND a.work_unit_id=b.work_unit_id"
                             + " AND a.user_id=c.id"
                             + " AND a.server_id=c.server_id"
                             + " GROUP BY LOWER(c.name) ORDER BY \"zeros\" DESC FETCH FIRST 1 ROWS ONLY");
        name = "";
        computersUsed = 0;
        zeros = 0;
        if (rs.next()) {
          name = rs.getString(1);
          computersUsed = rs.getInt(2);
          zeros = rs.getLong(3);
        }
        rs.close();
        if (name != null && name.length() > 0) {
          rs = stmt.executeQuery("SELECT name FROM zeta.user WHERE id>0 AND LOWER(name)='" + name + "' ORDER BY id");
          if (rs.next()) {
            name = rs.getString(1);
          }
          rs.close();
          destinationPage.append("<a href=\"");
          destinationPage.append(linkAddress);
          destinationPage.append("producers?user=");
          destinationPage.append(URLEncoder.encode(name, "ISO-8859-1"));
          destinationPage.append("\">");
          destinationPage.append(name);
          destinationPage.append("</a><br>delivered ");
          destinationPage.append(decimalFormat.format(new Long(zeros)));
          destinationPage.append(" zeros<br>used ");
          destinationPage.append(decimalFormat.format(new Integer(computersUsed)));
          destinationPage.append(" computer(s)");
        }
        idx = sourcePage.indexOf("</small>", sourcePage.indexOf("<a ", idx2));
        idx2 = sourcePage.indexOf("Active producer<br><small>(random)", idx);
        if (idx2 == -1) {
          return;
        }
        destinationPage.append(sourcePage.substring(idx, idx2));
        // Active producer (random)
        destinationPage.append("Active producer<br><small>(random of last 24 h)</small></b><p><small>");
        rs = stmt.executeQuery("SELECT LOWER(c.name) AS \"user\","
                             + " COUNT(DISTINCT workstation_id) AS \"computers used\","
                             + " SUM(CAST(a.size AS DECIMAL(15, 0))) AS \"zeros\""
                             + " FROM zeta.computation a, zeta.result b, zeta.user c"
                             + " WHERE b.stop IS NOT NULL"
                             + " AND TIMESTAMPDIFF(4, CAST(CURRENT TIMESTAMP-b.stop AS CHAR(22)))<=1440"
                             + " AND a.task_id=b.task_id"
                             + " AND a.work_unit_id=b.work_unit_id"
                             + " AND a.user_id=c.id"
                             + " AND a.server_id=c.server_id"
                             + " GROUP BY LOWER(c.name)");
        name = "";
        computersUsed = 0;
        zeros = 0;
        List producers = new ArrayList(500);
        while (rs.next()) {
          producers.add(new Object[] { rs.getString(1), new Integer(rs.getInt(2)), new Long(rs.getLong(3)) });
        }
        rs.close();
        if (producers.size() > 0) {
          Random random = new Random();
          Object[] o = (Object[])producers.get(random.nextInt(producers.size()));
          name = (String)o[0];
          computersUsed = ((Integer)o[1]).intValue();
          zeros = ((Long)o[2]).longValue();
        }
        if (name != null && name.length() > 0) {
          rs = stmt.executeQuery("SELECT name FROM zeta.user WHERE id>0 AND LOWER(name)='" + name + "' ORDER BY id");
          if (rs.next()) {
            name = rs.getString(1);
          }
          rs.close();
          destinationPage.append("<a href=\"");
          destinationPage.append(linkAddress);
          destinationPage.append("producers?user=");
          destinationPage.append(URLEncoder.encode(name, "ISO-8859-1"));
          destinationPage.append("\">");
          destinationPage.append(name);
          destinationPage.append("</a><br>delivered ");
          destinationPage.append(decimalFormat.format(new Long(zeros)));
          destinationPage.append(" zeros<br>used ");
          destinationPage.append(decimalFormat.format(new Integer(computersUsed)));
          destinationPage.append(" computer(s)");
        }
        idx = sourcePage.indexOf("</small>", sourcePage.indexOf("<a ", idx2));
        destinationPage.append(sourcePage.substring(idx));
        // Transfer modified page to server
        StreamUtils.writeData(new ByteArrayInputStream(destinationPage.toString().getBytes()), new FileOutputStream(ConstantProperties.TEMP_DIR + "index.html"), true, true);
        if (properties.get("ftp.host") != null) {
          FTP ftp = null;
          try {
      			ftp = new FTP();
      			ftp.connect(properties.get("ftp.host", ""));
      			ftp.login(properties.get("ftp.user", ""), properties.get("ftp.password", ""));
            ftp.cd(destination);
            ftp.put(ConstantProperties.TEMP_DIR + "index.html", "index.html", FTP.MODE_BINARY);
            destination = properties.get("index.html.destination.2");
            if (destination != null) {
              ftp.cd(destination);
              ftp.put(ConstantProperties.TEMP_DIR + "index.html", "index.html", FTP.MODE_BINARY);
            }
          } catch (Exception e) {
            ThrowableHandler.handle(e);
          } finally {
      	    if (ftp != null) {
      	      ftp.disconnect();
      	    }
          }
        } else {
          StreamUtils.writeData(new FileInputStream(ConstantProperties.TEMP_DIR + "index.html"), new FileOutputStream(destination + "/index.html"), true, true);
          destination = properties.get("index.html.destination.2");
          if (destination != null) {
            StreamUtils.writeData(new FileInputStream(ConstantProperties.TEMP_DIR + "index.html"), new FileOutputStream(destination + "/index.html"), true, true);
          }
        }
      }
    } catch (SQLException se) {
      ThrowableHandler.handle(se);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(connection);
      new File(ConstantProperties.TEMP_DIR + "index.html").delete();
    }
  }

  private static String format(int number, boolean reduce) {
    if (reduce) {
      if (number > 1000) {
        number /= 1000;
        number *= 1000;
      } else if (number > 100) {
        number /= 100;
        number *= 100;
      } else if (number > 10) {
        number /= 10;
        number *= 10;
      }
    }
    return decimalFormat.format(new Integer(number));
  }

  private static DecimalFormat decimalFormat = new DecimalFormat("#,###");
}
