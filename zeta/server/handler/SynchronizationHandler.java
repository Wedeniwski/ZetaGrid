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

package zeta.server.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.util.StreamUtils;

import BlowfishJ.BlowfishECB;

/**
 *  Handles a GET and a POST request to synchronize the data of this server with the data of another server.
 *  The GET request <code>req</code> can contain the following parameters:
 *  <ul>
 *  <li><code>successful</code> - timestamp of the last synchronized SQL statement of the other server.</li>
 *  <li><code>timestamp</code> - smallest timestamp of the not synchronized SQL statement of the other server.</li>
 *  </ul>
 *  Then the synchronized data are cleaned up. Or the GET request contains no parameter,
 *  then the synchronization data of this server will be generated.
 *  The response <code>resp</code> contains the parameters (size of the not aligned data, timestamp, successful, data) of the new work units.
 *
 *  The POST request must contains the following parameters which are transfered
 *  at the header name 'Param-String':
 *  <ul>
 *  <li><code>server_id</code> - server ID.</li>
 *  <li><code>statements_length</code> - size of the not aligned data.</li>
 *  </ul>
 *  The response contains an 'ok' or an error.
 *  The data will be sequentially synchronized.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class SynchronizationHandler implements GetHandler, PostHandler {

  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public SynchronizationHandler(DispatcherServlet servlet) {
    this.servlet = servlet;
  }

  /**
   *  Handles a GET request to synchronize the data of this server with the data of another server.
   *  The request <code>req</code> must contains the following parameters:
   *  <ul>
   *  <li><code>successful</code> - timestamp of the last synchronized SQL statement of the other server.</li>
   *  <li><code>timestamp</code> - smallest timestamp of the not synchronized SQL statement of the other server.</li>
   *  </ul>
   *  Then the synchronized data are cleaned up. Or the GET request contains no parameter,
   *  then the synchronization data of this server will be generated.
   *  The response <code>resp</code> contains the parameters (size of data, timestamp, successful, data) of the new work units.
  **/
  public void doGet(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Connection con  = null;
    Statement  stmt = null;
    try {
      con   = servlet.getConnection();
      stmt  = con.createStatement();
      int serverId = servlet.getServer().getId();
      String successful = req.getParameter("successful");
      if (successful != null && successful.length() > 0) {  // ToDo: this entry is not critical but not secure
        String timestamp = req.getParameter("timestamp");
        if (timestamp != null) {
          stmt.executeUpdate("DELETE FROM zeta.server_synchronization WHERE timestamp<'" + timestamp + '\'');
          String s = "UPDATE zeta.server SET last_synchronization='" + successful + "' WHERE server_id=" + serverId;
          servlet.log(s);
          stmt.executeUpdate(s);
        }
      } else {
        servlet.log("generates synchronization data of this server " + serverId);
        ResultSet rs = stmt.executeQuery("SELECT key FROM zeta.server WHERE server_id=" + serverId);
        byte[] serverKey = (rs.next())? rs.getBytes(1) : null;
        rs.close();
        if (serverKey == null) {
          throw new ServletException("Missing key for server " + serverId);
        }
        StringWriter sWriter = new StringWriter(50000);
        BufferedWriter writer = new BufferedWriter(sWriter);
        Timestamp lastSynchronization = null;
        Timestamp timestamp = null;
        int count = 0;
        int maxStmtSynchronization = 0;
        try {
          maxStmtSynchronization = Integer.parseInt(Parameter.getValue(stmt, "max_stmt_synchronization", "0"));
        } catch (NumberFormatException nfe) {
          maxStmtSynchronization = 0;
        }
        rs = stmt.executeQuery("SELECT MAX(timestamp) FROM zeta.server_synchronization");
        if (rs.next()) {
          timestamp = rs.getTimestamp(1);
          rs.close();
          rs = stmt.executeQuery("SELECT sql_statement,timestamp FROM zeta.server_synchronization WHERE timestamp<'" + timestamp + "' ORDER BY timestamp");
          while (rs.next()) {
            if (++count == maxStmtSynchronization) {
              timestamp = rs.getTimestamp(2);
              break;
            }
            String s = rs.getString(1) + '\n';
            writer.write(s, 0, s.length());
            lastSynchronization = rs.getTimestamp(2);
          }
          rs.close();
        }
        writer.close();
        if (lastSynchronization != null) {
          byte[] statements = sWriter.toString().getBytes("UTF-8");
          ByteArrayOutputStream out = new ByteArrayOutputStream(statements.length);
          ZipOutputStream zip = new ZipOutputStream(out);
          zip.setLevel(Deflater.BEST_COMPRESSION);
          zip.putNextEntry(new ZipEntry("synchronization.sql"));
          zip.write(statements);
          zip.flush();
          zip.close();
          out.close();
          statements = out.toByteArray();
          int statementsLength = statements.length;
          statements = StreamUtils.align8(statements);
          BlowfishECB bfecb = new BlowfishECB(serverKey);
          bfecb.encrypt(statements);
          bfecb.cleanUp();
          out = new ByteArrayOutputStream(statements.length+100);
          zip = new ZipOutputStream(out);
          zip.setLevel(Deflater.BEST_COMPRESSION);
          zip.putNextEntry(new ZipEntry("statementsLength"));
          zip.write(String.valueOf(statementsLength).getBytes("UTF-8"));
          zip.putNextEntry(new ZipEntry("timestamp"));
          zip.write(timestamp.toString().getBytes("UTF-8"));
          zip.putNextEntry(new ZipEntry("lastSynchronization"));
          zip.write(lastSynchronization.toString().getBytes("UTF-8"));
          zip.putNextEntry(new ZipEntry("synchronization.sql"));
          zip.write(statements);
          zip.flush();
          zip.close();
          out.close();
          resp.setContentType("application/octet-stream");
          resp.setContentLength(out.size());
          out.writeTo(resp.getOutputStream());
        }
      }
    } catch(SQLException e) {
      throw new ServletException(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(con);
    }
  }

  /**
   *  Timestamp in millis of the last update of the data of the page.
   *  Returns always the current timestamp.
   *  @return always the current timestamp.
  **/
  public long getTimestampOfPage() {
    return System.currentTimeMillis();
  }

  /**
   *  Handles a POST request to synchronize the data of this server with the data of another server.
   *  The request must contains the following parameters which are transfered
   *  at the header name 'Param-String':
   *  <ul>
   *  <li><code>server_id</code> - server ID.</li>
   *  <li><code>statements_length</code> - size of the not aligned data.</li>
   *  </ul>
   *  The response <code>resp</code> contains an 'ok' or an error.
   *  The data will be sequentially synchronized.
  **/
  public void doPost(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (req.getContentType().equals("application/octet-stream")) {
      String paramString = req.getHeader("Param-String"); // not a standard property !!
      Map parameter = HttpUtils.parseQueryString(paramString);

      String serverId = getParameter(parameter, "server_id");
      int statementsLength = Integer.parseInt(getParameter(parameter, "statements_length"));

      Connection con  = null;
      Statement  stmt = null;
      try {
        con   = servlet.getConnection();
        stmt  = con.createStatement();
        servlet.log("start synchronization with server " + serverId + " (" + statementsLength + ')');
        ResultSet rs = stmt.executeQuery("SELECT key FROM zeta.server WHERE server_id=" + serverId);
        byte[] serverKey = (rs.next())? rs.getBytes(1) : null;
        rs.close();
        if (serverKey == null) {
          throw new ServletException("Missing key for server " + serverId);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(req.getContentLength());
        StreamUtils.writeData(req.getInputStream(), out, false, true);
        BlowfishECB bfecb = new BlowfishECB(serverKey);
        byte[] statements = out.toByteArray();
        bfecb.decrypt(statements);
        bfecb.cleanUp();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(statements, 0, statementsLength));
        ZipEntry zEntry = zip.getNextEntry();
        if (zEntry == null) {
          throw new IOException("An error occur in the synchronization data!");
        }
        out = new ByteArrayOutputStream(100000);
        StreamUtils.writeData(zip, out, true, true);
        BufferedReader reader = new BufferedReader(new StringReader(new String(out.toByteArray(), "UTF-8")));
        int countStatements = 0;
        StringBuffer statement = new StringBuffer(1000);
        while (true) {
          String line = reader.readLine();
          if (statement.length() > 0) {
            String trimLine = (line == null)? null : line.trim();
            if (line == null || trimLine.regionMatches(true, 0, "INSERT", 0, 6) || trimLine.regionMatches(true, 0, "UPDATE", 0, 6)) {
              try {
                stmt.executeUpdate(statement.toString());
              } catch (SQLException se) {
                stmt.executeUpdate("INSERT INTO zeta.error (server_id,timestamp,sql_statement) VALUES (" + serverId + ",CURRENT TIMESTAMP," + DatabaseUtils.encodeName(statement.toString()) + ')');
              }
              ++countStatements;
              statement.delete(0, statement.length());
            } else {
              statement.append('\n');
            }
          }
          if (line == null) {
            break;
          }
          statement.append(line);
        }
        reader.close();
        servlet.log(String.valueOf(countStatements) + " synchronizations from server " + serverId + " are executed.");
      } catch(SQLException e) {
        throw new ServletException(e);
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(con);
      }
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.getOutputStream().print("ok");
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
  }

  /**
   *  Returns a value for a key from the specified map with parameters.
   *  @param parameter map with parameters
   *  @param key key
   *  @return value for a key from the specified map with parameters; empty string if no value was found.
  **/
  private static String getParameter(Map parameter, String key) {
    String[] values = (String[])parameter.get(key);
    return (values == null || values.length == 0)? "" : values[0];
  }

  /**
   *  Servlet which owns this handler.
  **/
  private DispatcherServlet servlet;
}
