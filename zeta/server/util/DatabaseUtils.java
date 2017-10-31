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
     H. Haddorp
     S. Wedeniwski
--*/

package zeta.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.util.ProcessUtils;
import zeta.util.StreamUtils;

/**
 *  Provides database utilities especially for the servlets.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class DatabaseUtils {
  /**
   *  Releases a connection's database and JDBC resources immediately instead of waiting for them to be automatically released.
   *  No exception will be thrown if a database access error occurs.
   *  @param con the connection to the database
  **/
  public static void close(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch(SQLException e) {
      }
    }
  }

  /**
   *  Releases a statement object's database and JDBC resources immediately instead of waiting for this to happen when it is automatically closed.
   *  No exception will be thrown if a database access error occurs.
   *  @param stmt statement object's database
  **/
  public static void close(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch(SQLException e) {
      }
    }
  }

  /**
   *  Logging SQL statements in the database to synchronize different servers.
   *  @param serverId ID of the server
   *  @param stmt statement object's database
   *  @param sqlStatement SQL statement which should be logged
   *  @return the SQL statement where 'CURRENT TIMESTAMP' is replaced by the current timestamp if the synchronization is activated.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static String log(int serverId, Statement stmt, String sqlStatement) throws SQLException {
    if (numberOfServers == -1) {
      synchronized (DatabaseUtils.class) {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM zeta.server");
        if (rs.next()) {
          numberOfServers = rs.getInt(1);
        }
        rs.close();
      }
    }
    if (numberOfServers > 1) {
      StringBuffer buffer = new StringBuffer(sqlStatement.length()+100);
      sqlStatement = getLogStatement(serverId, stmt, sqlStatement, buffer);
      stmt.executeUpdate(buffer.toString());
    }
    return sqlStatement;
  }

  /**
   *  Executing and logging a SQL statement in the database to synchronize different servers.
   *  @param servlet  servlet which has a connection to the server database.
   *  @param sqlStatement SQL statement which should be logged
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static void executeAndLogUpdate(DispatcherServlet servlet, String sqlStatement) throws SQLException, ServletException {
    Connection con = null;
    Statement stmt = null;
    boolean deactivateAutoCommit = false;
    try {
      con = servlet.getConnection();
      stmt = con.createStatement();
      if (numberOfServers == -1) {
        synchronized (DatabaseUtils.class) {
          ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM zeta.server");
          if (rs.next()) {
            numberOfServers = rs.getInt(1);
          }
          rs.close();
        }
      }
      if (numberOfServers > 1) {
        StringBuffer buffer = new StringBuffer(sqlStatement.length()+100);
        deactivateAutoCommit = true;
        con.setAutoCommit(false);
        stmt.executeUpdate(getLogStatement(servlet.getServer().getId(), stmt, sqlStatement, buffer));
        stmt.executeUpdate(buffer.toString());
        con.commit();
      } else {
        stmt.executeUpdate(sqlStatement);
      }
    } catch (SQLException se) {
      servlet.log("SQLException: " + se.getMessage() + '\n' + sqlStatement);
      throw se;
    } finally {
      close(stmt);
      if (con != null) {
        if (deactivateAutoCommit) {
          con.setAutoCommit(true);
        }
        close(con);
      }
    }
  }

  /**
   *  Executing and logging a SQL statement in the database to synchronize different servers.
   *  @param serverId ID of the server
   *  @param stmt statement object's database
   *  @param sqlStatement SQL statement which should be logged
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static void executeAndLogUpdate(int serverId, Statement stmt, String sqlStatement) throws SQLException {
    boolean deactivateAutoCommit = false;
    if (numberOfServers == -1) {
      synchronized (DatabaseUtils.class) {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM zeta.server");
        numberOfServers = (rs.next())? rs.getInt(1) : 0;
        rs.close();
      }
    }
    try {
      if (numberOfServers > 1) {
        StringBuffer buffer = new StringBuffer(sqlStatement.length()+100);
        deactivateAutoCommit = true;
        stmt.getConnection().setAutoCommit(false);
        stmt.executeUpdate(getLogStatement(serverId, stmt, sqlStatement, buffer));
        stmt.executeUpdate(buffer.toString());
        stmt.getConnection().commit();
      } else {
        stmt.executeUpdate(sqlStatement);
      }
    } finally {
      if (deactivateAutoCommit) {
        stmt.getConnection().setAutoCommit(true);
      }
    }
  }

  /**
   *  Encodes a name so that it can be safely inserted in the database.
   *  This method encodes only the character <code>'</code> to <code>\'</code>
   *  or encodes the complete name as hexadecimal string
   *  if at least one character has an hexadecimal code less than 0x20.
   *  It does not encode the characters <code>\</code>, <code>"</code>, <code>%</code>,
   *  or <code>_</code> which are not needed for INSERT statements.
   *
   *  The result either starts with <code>X'</code> and ends with <code>'</code>
   *  if the name was as hexadecimal string encoded
   *  or starts with <code>'</code> and ends with <code>'</code> else.
   *
   *  @param name name to be encoded
   *  @return the encoded name either starts with <code>X'</code> and ends with <code>'</code>
   *          or starts with <code>'</code> and ends with <code>'</code>,
   *          except the name <code>null</code> which will not be encoded.
  **/
  public static String encodeName(String name) {
    if (name == null) {
      return null;
    }
    int l = name.length();
    StringBuffer buffer = new StringBuffer(2*l+3);
    buffer.append('\'');
    for (int i = 0; i < l; ++i) {
        char c = name.charAt(i);
        if (c == '\'') {
          buffer.append('\'');
        } else if (((int)c) < 32) {
          final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
          buffer.delete(0, buffer.length());
          buffer.append("X'");
          for (i = 0; i < l; ++i) {
            short d = (short)name.charAt(i);
            buffer.append(digits[(d >> 4)&15]);
            buffer.append(digits[d&15]);
          }
          break;
        }
        buffer.append(c);
    }
    buffer.append('\'');
    return buffer.toString();
/*
    int i = name.indexOf('\'');
    if (i < 0) {
      return name;
    }
    int k = 1;
    while (true) {
      i = name.indexOf('\'', i+1);
      if (i < 0) {
        break;
      }
      ++k;
    }
    int l = name.length();
    k += l;
    char[] c = new char[k];
    name.getChars(0, l, c, 0);
    while (k != l) {
      if ((c[--k] = c[--l]) == '\'') {
        c[--k] = '\'';
      }
    }
    return new String(c);
*/
  }

  /**
   *  Executes the Command Line Processor.
   *  @param db2 DB2 commands which will be sent to the Command Line Processor. Do not use the DB2 command TERMINATE
   *  @param reportFileName write the report generated by the DB2 commands to the specified file if the name is not <code>null</code>
   *  @param waitInSeconds greater than 0 means that the process waits not longer than the specified number of seconds until the DB2 commands are terminated
  **/
  public static void db2CLP(String db2, String reportFileName, int waitInSeconds) throws IOException {
    FileOutputStream out = null;
    File db2File = null;
    File syncFile = null;
    try {
      syncFile = File.createTempFile("sync", ".tmp");
      syncFile.delete();
      db2 += "\nCONNECT TO zeta;\nEXPORT TO " + syncFile.getAbsolutePath() + " OF DEL SELECT server_id FROM zeta.error WHERE 1=0;\nCONNECT RESET;\nTERMINATE;\n";
      db2File = File.createTempFile("db2clp", ".ddl");
      out = new FileOutputStream(db2File.getAbsolutePath());
      out.write(db2.getBytes());
      out.close();
      out = null;
      ProcessUtils.exec("db2cmd -c \"db2 +o " + ((reportFileName == null)? "" : "-r" + reportFileName) + " -tf " + db2File.getAbsolutePath() + '\"', System.out, true);
      for (int i = 0; i < waitInSeconds && !syncFile.exists(); ++i) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
      }
    } finally {
      StreamUtils.close(out);
      if (syncFile != null) {
        if (syncFile.delete()) {
          db2File.delete();
        } else if (db2File != null) {
          db2File.deleteOnExit();
        }
      } else if (db2File != null) {
        db2File.deleteOnExit();
      }
    }
  }

  /**
   *  Converts days to time millis.
   *  @param days days
   *  @return converted days to time millis.
  **/
  public static long convertDaysToTimeMillis(long days) {
    return days*24*3600000L-DB2_DAYS_MILLIS_DIFF;
  }

  /**
   *  Converts days to time millis.
   *  @param days days
   *  @return converted days to time millis.
  **/
  public static long convertTimeMillisToDays(long timeMillis) {
    return (timeMillis+DB2_DAYS_MILLIS_DIFF)/(24*3600000L);
  }

  /**
   *  Generates logging statements.
   *  @param serverId ID of the server
   *  @param stmt statement object's database
   *  @param sqlStatement SQL statement which should be logged
   *  @return the SQL statement where 'CURRENT TIMESTAMP' is replaced by the current timestamp.
  **/
  private static String getLogStatement(int serverId, Statement stmt, String sqlStatement, StringBuffer buffer) {
    buffer.delete(0, buffer.length());
    int l = sqlStatement.length();
    String currentTime = new Timestamp(System.currentTimeMillis()).toString();
    int idx = 0;
    while (idx < l) {
      int i = sqlStatement.indexOf("CURRENT TIMESTAMP", idx);
      if (i == -1) {
        break;
      }
      buffer.append(sqlStatement.substring(idx, i));
      buffer.append('\'');
      buffer.append(currentTime);
      buffer.append('\'');
      idx = i+17;
    }
    if (idx < l) {
      buffer.append(sqlStatement.substring(idx));
    }
    sqlStatement = buffer.toString();
    buffer.delete(0, buffer.length());
    buffer.append("INSERT INTO zeta.server_synchronization (server_id,sql_statement) VALUES (");
    buffer.append(serverId);
    buffer.append(',');
    buffer.append(encodeName(sqlStatement));
    buffer.append(')');
    return sqlStatement;
  }

  private static int numberOfServers = -1;
  private final static long DB2_DAYS_MILLIS_DIFF = 719162L*24*3600000L;

  // ToDo: remove after migration to v1.9.4
  public static void main(String[] args) {
    try {
      String s = new String(zeta.util.StreamUtils.getFile(args[0], false, false));
      StringBuffer buffer = new StringBuffer((5*s.length())/4);
      int k = 0;
      while (true) {
        int i = s.indexOf('\'', k);
        int j = s.indexOf('\'', i+1);
        if (i < 0 || j < 0) {
          buffer.append(s.substring(k));
          break;
        }
        while (j+2 < s.length() && s.charAt(j+1) == '\'') {
          j = s.indexOf('\'', j+2);
          System.out.println(s.substring(k, j));
        }
        buffer.append(s.substring(k, i));
        buffer.append(encodeName(s.substring(i+1, j)));
        k = j+1;
      }
      zeta.util.StreamUtils.writeData(new java.io.ByteArrayInputStream(zeta.util.StringUtils.replace(buffer.toString(), "''''", "''").getBytes()), new java.io.FileOutputStream(args[0] + ".tmp"), true, true);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
