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

package zeta.server.handler.approve;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.handler.ApproveHandler;
import zeta.server.util.DatabaseUtils;
import zeta.util.Base64;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  Interface to approve a change request.
 *
 *  @version 2.0, August 6, 2005
**/
public abstract class ApproveBase {
  /**
   *  @param servlet servlet which owns the approve handler.
   *  @param userId id of the user where the data will be changed.
   *  @param user name of the user.
   *  @param eMail e-mail address of the user.
  **/
  public ApproveBase(DispatcherServlet servlet, int userId, String user, String eMail) {
    this.servlet = servlet;
    this.userId = userId;
    this.user = user;
    this.eMail = eMail;
  }

  /**
   *  Returns the key name of the change request.
   *  A key name should contains only alpha characters.
   *  @return the key name of the change request.
  **/
  public abstract String getKey();

  /**
   *  Returns the value(s) of the change request.
   *  @return null if the specified text is not defined for the key.
  **/
  public String getValue(String text) {
    String sub = getKey() + ':' + servlet.getServer().getId() + ',' + userId + ',' + user + ',' + eMail + ',';
    if (text.startsWith(sub)) {
      return text.substring(sub.length(), text.lastIndexOf(','));
    }
    return null;
  }

  /**
   *  Returns a row name which will be need as an additional information in the approve process.
   *  @param stmt statement object's database
   *  @param value which will be changed.
   *  @param timeMillis timestamp of the request.
   *  @return HTML text as information of the change.
  **/
  public abstract String approve(Statement stmt, String value, long timeMillis) throws SQLException, ServletException;

  /**
   *  Generates a link to approve the specified data.
   *  @param data data to encrypt
   *  @return a link to approve the specified data
   *  @exception  SQLException  if a database access error occurs.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public String generateAddressToApprove(byte[] data) throws SQLException, IOException, ServletException {
    byte[] key = new byte[100];
    for (int i = 0; i < data.length; i += 100) {
      for (int j = 0; j < 100 && i+j < data.length; ++j) {
        key[j] ^= data[i+j];
      }
    }
    String code = Base64.encode(key);
    String messageKey = getKey() + ':' + servlet.getServer().getId() + ',' + userId + ',' + user + ',' + eMail + ',' + code + ',' + System.currentTimeMillis();
    Connection con = null;
    PreparedStatement pStmt = null;
    try {
      con  = servlet.getConnection();
      pStmt = con.prepareStatement("SELECT data FROM zeta.approve WHERE server_id=? AND user_id=? AND key=?");
      pStmt.setInt(1, servlet.getServer().getId());
      pStmt.setInt(2, userId);
      pStmt.setString(3, code);
      ResultSet rs = pStmt.executeQuery();
      if (rs.next()) {
        byte[] data2 = rs.getBytes(1);
        rs.close();
        if (Arrays.equals(data, data2)) {
          return ApproveHandler.generateAddressToApprove(servlet, messageKey);
        } else {
          pStmt = con.prepareStatement("UPDATE zeta.approve SET approved=requested WHERE server_id=? AND user_id=? AND key=?");
          pStmt.setInt(1, servlet.getServer().getId());
          pStmt.setInt(2, userId);
          pStmt.setString(3, code);
          pStmt.executeUpdate();
        }
      } else {
        rs.close();
      }
      pStmt = con.prepareStatement("INSERT INTO zeta.approve (server_id,user_id,key,data) VALUES (?,?,?,?)");
      pStmt.setInt(1, servlet.getServer().getId());
      pStmt.setInt(2, userId);
      pStmt.setString(3, code);
      pStmt.setBytes(4, data);
      pStmt.executeUpdate();
    } finally {
      DatabaseUtils.close(pStmt);
      DatabaseUtils.close(con);
    }
    return ApproveHandler.generateAddressToApprove(servlet, messageKey);
  }

  public static Integer getServerId(String text) {
    int idx = text.indexOf(':');
    if (idx >= 0) {
      int idx2 = text.indexOf(',', idx+1);
      if (idx2 > idx) {
        return new Integer(text.substring(idx+1, idx2));
      }
    }
    return null;
  }

  public static Integer getUserId(String text) {
    int idx = text.indexOf(':');
    if (idx >= 0) {
      int idx2 = text.indexOf(',', idx+1);
      if (idx2 > idx) {
        idx = idx2;
        idx2 = text.indexOf(',', idx+1);
        if (idx2 > idx) {
          return new Integer(text.substring(idx+1, idx2));
        }
      }
    }
    return null;
  }

  public static long getTimeMillis(String text) {
    int idx = text.lastIndexOf(',');
    return (idx >= 0)? Long.parseLong(text.substring(idx+1)) : 0;
  }

  public static void main(String[] args) {
    if (args.length < 1 || args[0].equals("?")) {
      System.out.println("USAGE: <start timestamp>");
      return;
    }
    Connection con = null;
    PreparedStatement pStmt = null;
    try {
      DateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
      con = zeta.server.tool.Database.getConnection();
      pStmt = con.prepareStatement("SELECT server_id,user_id,requested,approved,data FROM zeta.approve WHERE requested>?");
      pStmt.setTimestamp(1, Timestamp.valueOf(args[0]));
      ResultSet rs = pStmt.executeQuery();
      while (rs.next()) {
        FileOutputStream out = null;
        try {
          int serverId = rs.getInt(1);
          int userId = rs.getInt(2);
          Timestamp requested = rs.getTimestamp(3);
          Timestamp approved = rs.getTimestamp(4);
          String filename = (approved == null)? "requested_" + serverId + '_' + userId + '_' + timeFormatter.format(requested) + ".zip" : "approved_" + serverId + '_' + userId + '_' + timeFormatter.format(requested) + '_' + timeFormatter.format(requested) + ".zip";
          out = new FileOutputStream(filename);
          StreamUtils.writeData(new ByteArrayInputStream(rs.getBytes(5)), out, false, false);
          System.out.println(filename);
        } catch (IOException ioe) {
          ThrowableHandler.handle(ioe);
        } finally {
          StreamUtils.close(out);
        }
      }
      rs.close();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(pStmt);
      DatabaseUtils.close(con);
    }
  }

  protected DispatcherServlet servlet;
  protected int userId;
  protected String user;
  protected String eMail;
}
