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
     W. Westje
--*/

package zeta.server.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.processor.TaskResultProcessor;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.util.Base64;
import zeta.util.ProcessUtils;
import zeta.util.StreamUtils;


/**
 *  Handles a POST request with the result of a work unit.
 *  The request must contains the following parameters which are transfered
 *  at the header name 'Param-String':
 *  <ul>
 *  <li><code>task</code> - name of task of the work unit.</li>
 *  <li><code>work_unit_id</code> - identification of the work unit.</li>
 *  <li><code>user</code> - name of the resource provider.</li>
 *  <li><code>hostname</code> - name of the host of the resource provider.</li>
 *  <li><code>hostaddr</code> - TCP/IP address of the host of the resource provider.</li>
 *  </ul>
 *  The response contains an 'ok' or an error.
 *  The results are stored by the following algorithm:
 *  <ol>
 *  <li>Stores the result of the work unit with current timestamp in the recomputation table if a result exists for
 *      this work unit but there exist a reserved work unit for recomputation without a result.</li>
 *  <li>Stores the result of the work unit with current timestamp in the result table if no result exists.</li>
 *  </ol>
 *
 *  @version 2.0, August 6, 2005
**/
public class ResultHandler implements PostHandler {

  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public ResultHandler(DispatcherServlet servlet) {
    this.servlet = servlet;
  }

  /**
   *  Handles a POST request with the result of a work unit.
   *  The request must contains the following parameters which are transfered
   *  at the header name 'Param-String':
   *  <ul>
   *  <li><code>task</code> - name of task of the work unit.</li>
   *  <li><code>work_unit_id</code> - identification of the work unit.</li>
   *  <li><code>user</code> - name of the resource provider.</li>
   *  <li><code>hostname</code> - name of the host of the resource provider.</li>
   *  <li><code>hostaddr</code> - TCP/IP address of the host of the resource provider.</li>
   *  </ul>
   *  The response <code>resp</code> contains an 'ok' or an error.
  **/
  public void doPost(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, SQLException, IOException {
    Connection con = null;
    PreparedStatement pStmt = null;
    Statement stmt = null;
    String contentType = req.getContentType();
    if (contentType.startsWith("application/")) {
      if (!contentType.equals("application/octet-stream")) {
        servlet.log("wrong content type: " + contentType);
      }
      if (task == null) {
        servlet.log("no valid task is defined.");
        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        return;
      }
      if (task.getParameter() == null) {
        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        return;
      }
      if (concurrentConnections >= MAX_CONCURRENT_CONNECTIONS) {
        // Too many concurrent connections
        resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        servlet.log("too many concurrent connections!");
        //throw new ServletException("Too many connections!");
        return;
      }
      int bufferSize = req.getContentLength();
      String workUnitId = null;
      try {
        workUnitId = task.getParameter("work_unit_id");
        // Wrong work unit id
        if (workUnitId == null || workUnitId.length() == 0 || Long.parseLong(workUnitId) <= 0) {
          servlet.log("wrong work unit ID: " + workUnitId + ", bufferSize=" + bufferSize);
          resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
          return;
        }
      } catch (Exception e) {
        servlet.log("exception: wrong work unit ID: " + workUnitId + ", bufferSize=" + bufferSize);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setContentLength(2);
        resp.getOutputStream().print("ok");
        return;
      }
      try {
        ++concurrentConnections;    // number of concurrent database connections
        servlet.log("concurrent connections: " + concurrentConnections + ", work unit ID: " + workUnitId);

        // check the service and request the name of the encryption class
        if (bufferSize <= 2) {
          synchronized (encryptionClass) {
            if (task.getId() != encryptionClassTaskId) {
              ByteArrayOutputStream out = new ByteArrayOutputStream(2000);
              ZipOutputStream zip = new ZipOutputStream(out);
              zip.setLevel(Deflater.BEST_COMPRESSION);
              zip.putNextEntry(new ZipEntry("className"));
              StreamUtils.writeData(new ByteArrayInputStream(task.getEncryptionClass()), zip, true, false);
              zip.putNextEntry(new ZipEntry("signature"));
              StreamUtils.writeData(new ByteArrayInputStream(task.getEncryptionSignature().getBytes("UTF-8")), zip, true, false);
              zip.flush();
              zip.close();
              encryptionClass = Base64.encode(out.toByteArray());
              encryptionClassTaskId = task.getId();
            }
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            resp.setContentType("text/plain");
            resp.setContentLength(encryptionClass.length());
            resp.getOutputStream().print(encryptionClass);
          }
          return;
        }

        // get parameters
        String user     = DatabaseUtils.encodeName(task.getParameter("user"));
        String hostname = task.getParameter("hostname");
        String hostaddr = task.getParameter("hostaddr");

        con  = servlet.getConnection();
        stmt = con.createStatement();

        int taskId = task.getId();
        TaskResultProcessor processor = task.getResultProcessor();
        if (processor == null) {
          servlet.log("could not resolve result processor for task ID: " + taskId + ").");
        }
        int size = -1;
        String parameters = null;
        ResultSet rs = stmt.executeQuery("SELECT size,parameters FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
        if (rs.next()) {
          size = rs.getInt(1);
          parameters = rs.getString(2);
        }
        rs.close();
        if (size < 0) {
          servlet.log("exception: undefined work unit ID: " + workUnitId);
          resp.setStatus(HttpServletResponse.SC_OK);
          resp.setContentType("text/plain");
          resp.setContentLength(2);
          resp.getOutputStream().print("ok");
          return;
        }
        rs = stmt.executeQuery("SELECT work_unit_id FROM zeta.result WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
        if (!rs.next()) { // Result not in database
          rs.close();
          Timestamp currentTime = new Timestamp(System.currentTimeMillis());
          try {
            // retrieve data from client
            DatabaseUtils.close(stmt);
            stmt = null;
            DatabaseUtils.close(con);
            con = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
            StreamUtils.writeData(req.getInputStream(), out, false, true);
            byte[] buffer = out.toByteArray();
            if (buffer.length != bufferSize) {
              if (dataNotCompleteWorkUnitId.equals(workUnitId)) { 
                ++dataNotCompleteCount;
              } else {
                dataNotCompleteWorkUnitId = workUnitId;
                dataNotCompleteCount = 1;
              }
              if (dataNotCompleteCount >= MAX_DATA_NOT_COMPLETE_COUNT) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/plain");
                resp.setContentLength(2);
                resp.getOutputStream().print("ok");
                servlet.log("data not complete recomputation: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr + ", buffer.length=" + buffer.length + ", bufferSize=" + bufferSize);
              } else {
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                servlet.log("data not complete: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr + ", buffer.length=" + buffer.length + ", bufferSize=" + bufferSize);
              }
              return;
            }

            // check result
            con = servlet.getConnection();
            stmt = con.createStatement();

            if (processor != null) {
              try {
                processor.checkResult(task.createWorkUnit(Long.parseLong(workUnitId), size, parameters, false), buffer);
              } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                servlet.log("data corrupted: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
                return;
              }
            }

            // process results
            con.setAutoCommit(false);
            if (processor != null) { // task specific processing 
              if (!processor.processResult(stmt, task.createWorkUnit(Long.parseLong(workUnitId), size, parameters, false), buffer)) {
                buffer = null;
              }
            }

            pStmt = con.prepareStatement("INSERT INTO zeta.result (task_id,work_unit_id,stop,result) VALUES ("
                                         + taskId + ',' + workUnitId + ",'" + currentTime.toString() + "',?)");
            pStmt.setBytes(1, buffer);
            //pStmt.setBinaryStream(1, req.getInputStream(), bufferSize);
            int affectetRows = pStmt.executeUpdate();
            if (affectetRows == 0) {
              resp.setStatus(HttpServletResponse.SC_OK);
              resp.setContentType("text/plain");
              resp.setContentLength(2);
              resp.getOutputStream().print("ok");
              //resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
              servlet.log("entry not found: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
              return;
            }
            DatabaseUtils.log(servlet.getServer().getId(), stmt,
                              "INSERT INTO zeta.result (task_id,work_unit_id,stop) VALUES ("
                              + taskId + ',' + workUnitId + ",'" + currentTime.toString() + "')");
          } catch (IOException ioe) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            servlet.log("IOException: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
            return;
          } finally {
            if (con != null) {
              con.commit();
              con.setAutoCommit(true);
            }
          }
        } else {
          rs.close();  // result exists
          rs = stmt.executeQuery("SELECT stop,size,parameters FROM zeta.recomputation WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
          if (rs.next() && rs.getTimestamp(1) == null) {
            size = rs.getInt(2);
            parameters = rs.getString(3);
            rs.close();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            try {
              // retrieve data from client
              DatabaseUtils.close(stmt);
              stmt = null;
              DatabaseUtils.close(con);
              con = null;
              ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
              StreamUtils.writeData(req.getInputStream(), out, false, true);
              byte[] buffer = out.toByteArray();
              if (buffer.length != bufferSize) {
                if (dataNotCompleteWorkUnitId.equals(workUnitId)) { 
                  ++dataNotCompleteCount;
                } else {
                  dataNotCompleteWorkUnitId = workUnitId;
                  dataNotCompleteCount = 1;
                }
                if (dataNotCompleteCount >= MAX_DATA_NOT_COMPLETE_COUNT) {
                  resp.setStatus(HttpServletResponse.SC_OK);
                  resp.setContentType("text/plain");
                  resp.setContentLength(2);
                  resp.getOutputStream().print("ok");
                  servlet.log("data not complete recomputation: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr + ", buffer.length=" + buffer.length + ", bufferSize=" + bufferSize);
                } else {
                  resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                  servlet.log("data not complete: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr + ", buffer.length=" + buffer.length + ", bufferSize=" + bufferSize);
                }
                return;
              }

              // check result
              if (processor != null) {
                try {
                  processor.checkResult(task.createWorkUnit(Long.parseLong(workUnitId), size, parameters, true), buffer);
                } catch (Exception e) {
                  resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                  servlet.log("data corrupted: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
                  return;
                }
              }

              // process result
              con = servlet.getConnection();
              stmt = con.createStatement();
              con.setAutoCommit(false);
              if (processor != null) {
                if (!processor.processResult(stmt, task.createWorkUnit(Long.parseLong(workUnitId), size, parameters, true), buffer)) {
                  buffer = null;
                }
              }
              pStmt = con.prepareStatement("UPDATE zeta.recomputation SET (stop,result)=('" + currentTime.toString() + "',?) WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
              pStmt.setBytes(1, buffer);
              //pStmt.setBinaryStream(1, req.getInputStream(), bufferSize);
              int affectetRows = pStmt.executeUpdate();
              if (affectetRows == 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/plain");
                resp.setContentLength(2);
                resp.getOutputStream().print("ok");
                //resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                servlet.log("entry not found: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
                return;
              }
              DatabaseUtils.log(servlet.getServer().getId(), stmt,
                                "UPDATE zeta.recomputation SET stop='" + currentTime.toString() + "' WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
            } catch (IOException ioe) {
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
              servlet.log("IOException: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
              return;
            } finally {
              if (con != null) {
                con.commit();
                con.setAutoCommit(true);
              }
            }
          } else {
            rs.close();
            servlet.log("work unit exists already: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
            try {
              StreamUtils.writeData(req.getInputStream(), null, false, false);
            } catch (IOException ioe) {
              servlet.log("IOException: task_id=" + taskId + ", work_unit_id=" + workUnitId + ", user=" + user + ", hostname=" + hostname + ", hostaddr=" + hostaddr);
            }
          }
        }
      } catch (SQLException e) {
        throw e;
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(pStmt);
        DatabaseUtils.close(con);
        --concurrentConnections;
      }
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("text/plain");
      resp.setContentLength(2);
      resp.getOutputStream().print("ok");
    } else {
      servlet.log("wrong content type: " + contentType);
      resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
  }

  private final int MAX_CONCURRENT_CONNECTIONS = 20;

  private int concurrentConnections = 0;

  private String dataNotCompleteWorkUnitId = "";
  private short dataNotCompleteCount = 0;
  private final short MAX_DATA_NOT_COMPLETE_COUNT = 5;

  /**
   *  Servlet which owns this handler.
  **/
  private DispatcherServlet servlet;

  private static String encryptionClass = "";
  private static int encryptionClassTaskId = -1;
}
