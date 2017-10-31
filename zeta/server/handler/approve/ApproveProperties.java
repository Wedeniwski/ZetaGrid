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

package zeta.server.handler.approve;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.DatabaseUtils;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  Approve a request to change user properties.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class ApproveProperties extends ApproveBase {
  /**
   *  @param servlet servlet which owns the approve handler.
   *  @param userId id of the user where the data will be changed.
   *  @param user name of the user.
   *  @param eMail e-mail address of the user.
  **/
  public ApproveProperties(DispatcherServlet servlet, int userId, String user, String eMail) {
    super(servlet, userId, user, eMail);
  }

  /**
   *  Returns the key name of the change request.
   *  A key name should contains only alpha characters.
   *  @return the key name of the change request.
  **/
  public String getKey() {
    return "user properties";
  }

  /**
   *  Returns a row name which will be need as an additional information in the approve process.
   *  @param stmt statement object's database
   *  @param value which will be changed.
   *  @param timeMillis timestamp of the request.
   *  @return HTML text as information of the change.
  **/
  final public String approve(Statement stmt, String value, long timeMillis) throws SQLException, ServletException {
    ResultSet rs = stmt.executeQuery("SELECT data FROM zeta.approve WHERE server_id=" + servlet.getServer().getId() + " AND user_id=" + userId + " AND approved IS NULL AND key='" + value + '\'');
    if (!rs.next()) {
      rs.close();
      return "<html><body><b>Error:</b> The key is invalid! Please check if the URL is similar to that in your e-mail.</body></html>";
    }
    byte[] data = rs.getBytes(1);
    rs.close();
    StringBuffer result = new StringBuffer(500);
    result.append("<html><body>The following properties are successfully changed:<p>&nbsp;");
    rs = stmt.executeQuery("SELECT email_valid_YN,team_name,properties FROM zeta.user WHERE server_id=" + servlet.getServer().getId() + " AND id=" + userId);
    if (rs.next()) {
      String messages = rs.getString(1);
      String team = rs.getString(2);
      String prop = rs.getString(3);
      rs.close();
      if (prop == null) {
        prop = "";
      }
      String userOrig = user;
      String eMailOrig = eMail;
      String messagesOrig = messages;
      String teamOrig = team;
      Properties properties = null;
      try {
        properties = new Properties();
        properties.load(new ByteArrayInputStream(prop.getBytes()));
        ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(data));
        while (true) {
          ZipEntry entry = in.getNextEntry();
          if (entry == null) {
            break;
          }
          String entryName = entry.getName();
          ByteArrayOutputStream out = new ByteArrayOutputStream(300);
          StreamUtils.writeData(in, out, false, true);
          String val = out.toString();
          if (entryName.equals("user")) {
            user = val;
          } else if (entryName.equals("email")) {
            eMail = val;
          } else if (entryName.equals("messages")) {
            messages = val;
          } else if (entryName.equals("team")) {
            team = val;
          } else {
            properties.setProperty(entryName, val);
          }
          result.append("<br>");
          result.append(entryName);
          result.append(": ");
          result.append(val);
        }
        in.close();
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      }
      StringBuffer sql = new StringBuffer(500);
      StringBuffer values = new StringBuffer(2048);
      if (!user.equals(userOrig)) {
        if (sql.length() > 0) {
          sql.append(',');
          values.append(',');
        }
        sql.append("name");
        values.append('\'');
        values.append(user);
        values.append('\'');
      }
      if (!eMail.equals(eMailOrig)) {
        if (sql.length() > 0) {
          sql.append(',');
          values.append(',');
        }
        sql.append("email");
        values.append('\'');
        values.append(eMail);
        values.append('\'');
      }
      if (!messages.equals(messagesOrig)) {
        if (sql.length() > 0) {
          sql.append(',');
          values.append(',');
        }
        sql.append("email_valid_YN");
        values.append('\'');
        values.append(messages);
        values.append('\'');
      }
      if (!team.equals(teamOrig)) {
        if (sql.length() > 0) {
          sql.append(',');
          values.append(',');
        }
        sql.append("team_name,join_in_team");
        if (team.trim().length() == 0) {
          values.append("NULL,NULL");
        } else {
          values.append('\'');
          values.append(team);
          values.append("','");
          values.append(new Timestamp(timeMillis));
          values.append('\'');
        }
      }
      if (sql.length() > 0) {
        sql.append(',');
        values.append(',');
      }
      sql.append("properties");
      if (properties != null) {
        try {
          ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
          properties.remove("user_id");
          properties.remove("user_orig");
          properties.remove("email_orig");
          properties.store(out, null);
          out.close();
          String val = out.toString();
          if (val.length() > 1024) {
            val = val.substring(0, 1024);
          }
          values.append(DatabaseUtils.encodeName(val));
        } catch (IOException ioe) {
        }
      } else {
        values.append("''");
      }
      DatabaseUtils.executeAndLogUpdate(servlet, "UPDATE zeta.user SET (" + sql.toString() + ")=(" + values.toString()
                                               + ") WHERE server_id=" + servlet.getServer().getId() + " AND id=" + userId);
    }
    stmt.executeUpdate("UPDATE zeta.approve SET approved=CURRENT TIMESTAMP WHERE server_id=" + servlet.getServer().getId() + " AND user_id=" + userId + " AND approved IS NULL AND key='" + value + '\'');
    result.append("<br>&nbsp;<p><b>Remark:</b> The server needs at least 1 hour to update your changes in the statistics.</body></html>");
    return result.toString();
  }
}
