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

package zeta.server.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.handler.approve.ApproveBase;
import zeta.server.util.DatabaseUtils;
import zeta.util.Base64;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

import BlowfishJ.BlowfishECB;

/**
 *  Handles a GET request to approve a change request.
 *
 *  @version 2.0, August 6, 2005
**/
public class ApproveHandler implements GetHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public ApproveHandler(DispatcherServlet servlet) {
    this.servlet = servlet;
  }

  /**
   *  Handles a GET request to approve a change request.
   *  The request <code>req</code> must contains the following parameters:
   *  <ul>
   *  <li><code>key</code> - key of the change request.</li>
   *  </ul>
   *  The response <code>resp</code> contains the approve status.
  **/
  public void doGet(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String key = req.getParameter("key");
    String keyLength = req.getParameter("length");
    if (key != null && key.length() > 0 && keyLength != null) {
      Connection con  = null;
      Statement  stmt = null;
      try {
        con  = servlet.getConnection();
        stmt = con.createStatement();
        String result = approve(servlet, stmt, key, keyLength);
        resp.setContentType("text/html");
        resp.getOutputStream().print(result);
      } catch (SQLException e) {
        throw new ServletException(e);
      } catch (NumberFormatException e) {
        throw new ServletException(e);
      } catch (IndexOutOfBoundsException e) {
        throw new ServletException(e);
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(con);
      }
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
   *  Encrypts a specified text.
   *  @param serverId ID of the server
   *  @param text text to encrypt
   *  @return encrypted text
   *  @exception  SQLException  if a database access error occurs.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static String generateAddressToApprove(DispatcherServlet servlet, String text) throws SQLException, IOException, ServletException {
    Connection con = null;
    Statement stmt = null;
    try {
      con = servlet.getConnection();
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT web_hostname,web_port,key FROM zeta.server WHERE server_id=" + servlet.getServer().getId());
      byte[] serverKey = (rs.next())? rs.getBytes(3) : null;
      if (serverKey == null) {
        throw new SQLException("Missing key for server " + servlet.getServer().getId());
      }
      BlowfishECB bfecb = new BlowfishECB(serverKey);
      byte[] code = text.getBytes("UTF-8");
      int codeLength = code.length;
      code = StreamUtils.align8(code);
      bfecb.encrypt(code);
      bfecb.cleanUp();
      String link = "http://" + rs.getString(1) + ':' + rs.getInt(2) + servlet.getRootPath() + servlet.getHandlerPath(ApproveHandler.class)
                  + "?key=" + URLEncoder.encode(Base64.encode(code)) + "&length=" + codeLength;
      rs.close();
      return link;
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(con);
    }
  }

  public static String decrypt(DispatcherServlet servlet, Statement stmt, String key, String keyLength) throws IOException, SQLException {
    int serverId = servlet.getServer().getId();
    ResultSet rs = stmt.executeQuery("SELECT key FROM zeta.server WHERE server_id=" + serverId);
    byte[] serverKey = (rs.next())? rs.getBytes(1) : null;
    rs.close();
    if (serverKey == null) {
      throw new SQLException("Missing key for server " + serverId);
    }
    BlowfishECB bfecb = new BlowfishECB(serverKey);
    byte[] code = Base64.decode(key);
    bfecb.decrypt(code);
    bfecb.cleanUp();
    return new String(code, 0, Integer.parseInt(keyLength), "UTF-8");
  }

  static String approve(DispatcherServlet servlet, Statement stmt, String key, String keyLength) throws IOException, SQLException {
    key = decrypt(servlet, stmt, key, keyLength);
    String result = "<html><body><b>Error:</b> The key <p>" + key + "<p>is invalid! Please check if the URL is similar to that in your e-mail.</body></html>";
    Integer serverId = ApproveBase.getServerId(key);
    if (serverId != null && serverId.intValue() == servlet.getServer().getId()) {
      String user = "";
      String eMail = "";
      Integer userId = ApproveBase.getUserId(key);
      ResultSet rs = stmt.executeQuery("SELECT name,email FROM zeta.user WHERE server_id=" + serverId + " AND id=" + userId);
      if (rs.next()) {
        user = rs.getString(1);
        eMail = rs.getString(2);
      }
      rs.close();

      // ToDo: configurable
      // ToDo: check that the keys are unique
      final Class[] approveClasses = { zeta.server.handler.approve.ApproveTeamName.class,
                                       zeta.server.handler.approve.ApproveMessages.class,
                                       zeta.server.handler.approve.ApproveProperties.class };
      for (int i = 0; i < approveClasses.length; ++i) {
        try {
          ApproveBase approveObj = (ApproveBase)approveClasses[i].getConstructor(new Class[] { DispatcherServlet.class, int.class, String.class, String.class }).newInstance(new Object[] { servlet, userId, user, eMail });
          String value = approveObj.getValue(key);
          if (value != null) {
            long timeMillis = ApproveBase.getTimeMillis(key);
            if (System.currentTimeMillis()-timeMillis < 7*24*3600*1000) {
              result = approveObj.approve(stmt, value, timeMillis);
            } else {
              result = "<html><body><b>Error:</b> Key expired!</body></html>";
            }
            break;
          }
        } catch (Exception e) {
          ThrowableHandler.handle(e);
        }
      }
    }
    return result;
  }

  /**
   *  Servlet which owns this handler.
  **/
  private DispatcherServlet servlet;
}
