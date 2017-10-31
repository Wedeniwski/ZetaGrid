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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.util.Base64;
import zeta.util.StreamUtils;

import BlowfishJ.BlowfishECB;

/**
 *  Handles GET requests to retrieve work units of another server.
 *  The GET request <code>req</code> can contain the following parameters:
 *  <ul>
 *  <li><code>list</code> - returns a list of of all available work units.</li>
 *  <li><code>dir</code> - returns a list of all available work units except the latest.</li>
 *  <li><code>get</code> - get a specified work unit.</li>
 *  <li><code>put</code> - put a specified work unit.</li>
 *  <li><code>del</code> - delete a specified work unit.</li>
 *  </ul>
 *  The response <code>resp</code> contains alist of work units or the data of a specified work unit.
 *
 *  @version 2.0, August 6, 2005
**/
public class DataHandler implements GetHandler, PostHandler {

  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public DataHandler(DispatcherServlet servlet) {
    this.servlet = servlet;
  }

  /**
   *  Handles GET requests to retrieve work units of another server.
   *  The GET request <code>req</code> can contain the following parameters:
   *  <ul>
   *  <li><code>list</code> - returns a list of of all available work units.</li>
   *  <li><code>dir</code> - returns a list of all available work units except the latest.</li>
   *  <li><code>get</code> - get a specified work unit.</li>
   *  <li><code>del</code> - delete a specified work unit.</li>
   *  </ul>
   *  The response <code>resp</code> contains a list of work units or the data of a specified work unit.
  **/
  public void doGet(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    InputStream in = null;
    Connection con = null;
    Statement stmt = null;
    try {
      con = servlet.getConnection();
      stmt = con.createStatement();
      int parameterScope = (task == null)? Parameter.GLOBAL_PARAMETER : task.getId();
      String hostnames = Parameter.getValue(stmt, "grant_data_hostnames", parameterScope, "", 3600000);
      if (hostnames.length() > 0) {
        String host = req.getRemoteHost() + '(' + req.getRemoteAddr() + ')';
        boolean hostHasAccess = false;
        StringTokenizer st = new StringTokenizer(hostnames, ",");
        while (st.hasMoreTokens()) {
          if (host.startsWith(st.nextToken())) {
            hostHasAccess = true;
            break;
          }
        }
        if (!hostHasAccess) {
          throw new ServletException("Access is denied - " + host + '.');
        }
      }
      int serverId = servlet.getServer().getId();
      String list = req.getParameter("list");
      String dir = req.getParameter("dir");
      String get = req.getParameter("get");
      String del = req.getParameter("del");
      File pathData = new File(Parameter.getValue(stmt, "path_data", parameterScope, "", 3600000));
      if (!Parameter.getValue(stmt, "encryption", Parameter.GLOBAL_PARAMETER, "Y", 3600000).equals("N")) {
        list = dir = get = del = null;
        ResultSet rs = stmt.executeQuery("SELECT key FROM zeta.server WHERE server_id=" + serverId);
        byte[] serverKey = (rs.next())? rs.getBytes(1) : null;
        rs.close();
        if (serverKey == null) {
          throw new ServletException("Missing key for server " + serverId);
        }
        String params = null;
        try {
          int actionsLength = Integer.parseInt(req.getParameter("actions_length"));
          BlowfishECB bfecb = new BlowfishECB(serverKey);
          byte[] actions = Base64.decode(req.getParameter("actions"));
          bfecb.decrypt(actions);
          bfecb.cleanUp();
          params = new String(actions, 0, actionsLength, "UTF-8");
        } catch (Exception e) {
          throw new ServletException("Access is denied.", e);
        }
        if (params == null) {
          throw new ServletException("Access is denied.");
        }
        int i = params.indexOf("dir=");
        if (i >= 0) {
          dir = params.substring(i+4);
        } else {
          i = params.indexOf("list=");
          if (i >= 0) {
            list = params.substring(i+5);
          } else {
            i = params.indexOf("get=");
            if (i >= 0) {
              int j = params.indexOf('&', i+4);
              get = (j < 0)? params.substring(i+4) : params.substring(i+4, j);
            }
            i = params.indexOf("del=");
            if (i >= 0) {
              int j = params.indexOf('&', i+4);
              del = (j < 0)? params.substring(i+4) : params.substring(i+4, j);
            }
          }
        }
      }
      if (dir != null && dir.length() > 0) {
        if (!Parameter.getValue(stmt, "grant_data_dir", parameterScope, "", 3600000).equals("Y") || dir.indexOf("..") >= 0) {
          throw new ServletException("Access is denied.");
        }
        File[] files = new File(pathData.getPath() + '/' + dir).listFiles();
        if (files != null && files.length > 0) {
          long maxTime = files[0].lastModified();
          for (int i = 1; i < files.length; ++i) {
            long m = files[i].lastModified();
            if (m > maxTime) {
              maxTime = m;
            }
          }
          ByteArrayOutputStream out = new ByteArrayOutputStream(50*files.length);
          for (int i = 0; i < files.length; ++i) {
            long m = files[i].lastModified();
            if (m < maxTime) {
              String s = files[i].getName() + '\n';
              out.write(s.getBytes("UTF-8"));
            }
          }
          out.close();
          resp.setContentType("application/octet-stream");
          resp.setContentLength(out.size());
          out.writeTo(resp.getOutputStream());
        }
      } else if (list != null && list.length() > 0) {
        if (!Parameter.getValue(stmt, "grant_data_list", parameterScope, "", 3600000).equals("Y") || list.indexOf("..") >= 0) {
          throw new ServletException("Access is denied.");
        }
        File[] files = new File(pathData.getPath() + '/' + list).listFiles();
        if (files != null && files.length > 0) {
          ByteArrayOutputStream out = new ByteArrayOutputStream(50*files.length);
          for (int i = 0; i < files.length; ++i) {
            String s = files[i].getName() + '\n';
            out.write(s.getBytes("UTF-8"));
          }
          out.close();
          resp.setContentType("application/octet-stream");
          resp.setContentLength(out.size());
          out.writeTo(resp.getOutputStream());
        }
      } else {
        if (get != null && get.length() > 0) {
          if (!Parameter.getValue(stmt, "grant_data_get", parameterScope, "", 3600000).equals("Y") || get.indexOf("..") >= 0) {
            throw new ServletException("Access is denied.");
          }
          File file = new File(pathData.getPath() + '/' + get);
          if (file.exists()) {
            resp.setContentType("application/octet-stream");
            resp.setContentLength((int)file.length());
            in = new FileInputStream(file);
            StreamUtils.writeData(in, resp.getOutputStream(), true, true);
          }
        }
        if (del != null && del.length() > 0) {
          if (!Parameter.getValue(stmt, "grant_data_del", parameterScope, "", 3600000).equals("Y") || del.indexOf("..") >= 0) {
            throw new ServletException("Access is denied.");
          }
          File file = new File(pathData.getPath() + '/' + del);
          if (!file.delete()) {
            throw new IOException("file '" + del + "' is not successfully deleted!");
          }
        }
      }
    } catch (IOException e) {
      throw new ServletException(e);
    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      StreamUtils.close(in);
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
   *  Handles a POST request to put work units from another server.
   *  The POST request <code>req</code> can contain the following parameters:
   *  <ul>
   *  <li><code>dir</code> - returns a list of work units.</li>
   *  <li><code>get</code> - get a specified work unit.</li>
   *  <li><code>del</code> - delete a specified work unit.</li>
   *  </ul>
   *  The response <code>resp</code> contains an 'ok' or an error.
  **/
  public void doPost(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (req.getContentType().equals("application/octet-stream")) {
      String paramString = req.getHeader("Param-String"); // not a standard property !!
      Map parameter = HttpUtils.parseQueryString(paramString);

      OutputStream out = null;
      Connection con = null;
      Statement stmt = null;
      try {
        con = servlet.getConnection();
        stmt = con.createStatement();
        int parameterScope = (task == null)? Parameter.GLOBAL_PARAMETER : task.getId();
        String hostnames = Parameter.getValue(stmt, "grant_data_hostnames", parameterScope, "", 3600000);
        if (hostnames.length() > 0) {
          String s = req.getRemoteHost() + '(' + req.getRemoteAddr() + ')';
          int idx = hostnames.indexOf(s);
          if (idx == -1 || idx > 0 && hostnames.charAt(idx-1) != ',' || idx+s.length() < hostnames.length() && hostnames.charAt(idx+s.length()) != ',') {
            throw new ServletException("Access is denied - " + s + '.');
          }
        }
        String put = getParameter(parameter, "put");
        int serverId = servlet.getServer().getId();
        File pathData = new File(Parameter.getValue(stmt, "path_data", parameterScope, "", 3600000));
        if (!Parameter.getValue(stmt, "encryption", Parameter.GLOBAL_PARAMETER, "Y", 3600000).equals("N")) {
          put = null;
          ResultSet rs = stmt.executeQuery("SELECT key FROM zeta.server WHERE server_id=" + serverId);
          byte[] serverKey = (rs.next())? rs.getBytes(1) : null;
          rs.close();
          if (serverKey == null) {
            throw new ServletException("Missing key for server " + serverId);
          }
          int actionsLength = Integer.parseInt(getParameter(parameter, "actions_length"));
          BlowfishECB bfecb = new BlowfishECB(serverKey);
          byte[] actions = Base64.decode(getParameter(parameter, "actions"));
          bfecb.decrypt(actions);
          bfecb.cleanUp();
          String params = new String(actions, 0, actionsLength, "UTF-8");
          int i = params.indexOf("put=");
          if (i >= 0) {
            put = params.substring(i+4);
          }
        }
        if (put != null && put.length() > 0) {
          if (!Parameter.getValue(stmt, "grant_data_put", parameterScope, "", 3600000).equals("Y") || put.indexOf("..") >= 0) {
            throw new ServletException("Access is denied.");
          }
          File file = new File(pathData.getPath() + '/' + put);
          out = new FileOutputStream(file);
          StreamUtils.writeData(req.getInputStream(), out, false, true);
        }
      } catch (IOException e) {
        throw new ServletException(e);
      } catch (SQLException e) {
        throw new ServletException(e);
      } finally {
        StreamUtils.close(out);
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
