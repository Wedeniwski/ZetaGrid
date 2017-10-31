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

package zeta.server;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import zeta.server.util.DatabaseUtils;
import zeta.util.ThrowableHandler;

/**
 *  Defines the task at the server side.
 *
 *  @version 2.0, August 6, 2005
**/
public class Server {

  /**
   *  Returns an instance of the active server.
   *  @exception SQLException if a database access error occurs.
   *  @exception IllegalStateException if there are more than one or no server defined as active.
  **/
  public static synchronized Server getInstance(Connection con) throws SQLException {
    if (server == null) {
      Statement stmt = null;
      try {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT server_id,logging_path,logging_max_backup,logging_max_filesize,smtp_hostname,smtp_port,smtp_login_name,smtp_login_password,send_mail_from FROM zeta.server WHERE active_YN='Y'");
        if (rs.next()) {
          int serverId = rs.getInt(1);
          Server s = new Server(serverId, rs.getString(2), rs.getInt(3), rs.getLong(4), rs.getString(5), rs.getInt(6), rs.getString(7), rs.getString(8), rs.getString(9));
          if (rs.next()) {
            throw new IllegalStateException("There are more than one server defined as active (" + serverId + " and " + rs.getInt(1) + ")!");
          }
          server = s;
        } else {
          throw new IllegalStateException("There is no server defined as active!");
        }
      } finally {
        DatabaseUtils.close(stmt);
      }
    }
    return server;
  }

  /**
   *  Implements the usual roll over behaviour of log files.
   *  A dot '.' plus an index number will be appended to the rolled over file name.
  **/
  public void fileRollOver(String fileName) {
    if (maxBackupIndex > 0) {
      File file = new File(fileName + '.' + maxBackupIndex);
      if (file.exists()) {
        file.delete();
      }
      for (int i = maxBackupIndex-1; i >= 1; --i) {
        File f = new File(fileName + '.' + i);
        if (f.exists()) {
          f.renameTo(file);
        }
        file = f;
      }
      new File(fileName).renameTo(file);
    }
  }

  /**
   *  Returns the server idnetifier
   *  @return server idnetifier
  **/
  public int getId() {
    return id;
  }

  /**
   *  Returns the path for the server logging files.
   *  @return path for the server logging files.
  **/
  public String getLoggingPath() {
    return loggingPath;
  }

  /**
   *  Returns the maximum index of the rolled over log files.
   *  @return the maximum index of the rolled over log files.
  **/
  public int getMaxLogBackupIndex() {
    return maxBackupIndex;
  }

  /**
   *  A log file will be rolled over if a log record will be added to the active log file which has a size larger than or equal to
   *  the specified maximum log file size.
   *  @return the "maximum" log file size.
  **/
  public long getMaxLogFileSize() {
    return maxLogFileSize;
  }

  /**
   *  Host name of the SMTP server to send notifications to approve server-side user configuration changes.
   *  @return host name of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  public String getSmtpHostName() {
    return smtpHostName;
  }

  /**
   *  Port of the SMTP server to send notifications to approve server-side user configuration changes.
   *  @return port of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  public int getSmtpPort() {
    return smtpPort;
  }

  /**
   *  Login name of the SMTP server to send notifications to approve server-side user configuration changes.
   *  @return login name of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  public String getSmtpLoginName() {
    return smtpLoginName;
  }

  /**
   *  Login password of the SMTP server to send notifications to approve server-side user configuration changes.
   *  @return login password of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  public String getSmtpLoginPassword() {
    return smtpLoginPassword;
  }

  /**
   *  Real name and e-mail address of the sender that send the notifications to approve server-side user configuration changes.
   *  @return real name and e-mail address of the sender that send the notifications to approve server-side user configuration changes.
  **/
  public String getSendMailFrom() {
    return sendMailFrom;
  }

  /**
   *  The method <code>getInstance</code> must be used.
   *  @see getInstance(Connection)
  **/
  private Server(int serverId, String loggingPath, int maxBackupIndex, long maxLogFileSize, String smtpHostName, int smtpPort, String smtpLoginName, String smtpLoginPassword, String sendMailFrom) {
    id = serverId;
    if (loggingPath != null && loggingPath.length() > 0 && loggingPath.charAt(loggingPath.length()-1) != '/' && loggingPath.charAt(loggingPath.length()-1) != '\\') {
      loggingPath += '/';
    }
    this.loggingPath = loggingPath;
    this.maxBackupIndex = maxBackupIndex;
    this.maxLogFileSize = maxLogFileSize;
    this.smtpHostName = smtpHostName;
    this.smtpPort = smtpPort;
    this.smtpLoginName = smtpLoginName;
    this.smtpLoginPassword = smtpLoginPassword;
    this.sendMailFrom = sendMailFrom;
  }

  /**
   *  Server identifier
  **/
  private int id;

  /**
   *  Path for the server logging files.
  **/
  private String loggingPath;

  /**
   *  Maximum index of the rolled over log files.
  **/
  private int maxBackupIndex;

  /**
   *  A log file will be rolled over if a log record will be added to the active log file which has a size larger than or equal to
   *  the specified maximum log file size.
  **/
  private long maxLogFileSize;

  /**
   *  Host name of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  private String smtpHostName;

  /**
   *  Port of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  private int smtpPort;

  /**
   *  Login name of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  private String smtpLoginName;

  /**
   *  Login password of the SMTP server to send notifications to approve server-side user configuration changes.
  **/
  private String smtpLoginPassword;

  /**
   *  Real name and e-mail address of the sender that send the notifications to approve server-side user configuration changes.
  **/
  private String sendMailFrom;

  private static Server server;
}
