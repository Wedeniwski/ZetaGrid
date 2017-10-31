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

package zeta.server.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import zeta.server.util.DatabaseUtils;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  @version 2.0, August 6, 2005
**/
public class DatabaseBackup {
  private static int maxCharsPerInsert = 25000;
  private static String backupFolder = "backup";
  private static final int INSERT = 0;
  private static final int UPDATE = 1;

  public static void main(String[] args) {
    if (args.length >= 3 && args[0].equals("r")) {
      if (args.length == 3) {
        restore(args[1], args[2], null);
        return;
      } else if (args.length == 4) {
        restore(args[1], args[2], args[3]);
        return;
      }
    } else if (args.length == 2 && args[0].equals("e")) {
      errorTable(Integer.parseInt(args[1]));
      return;
    } else if (args.length == 3 && args[0].equals("u")) {
      backupFolder = ".";
      try {
        backupTable(args[1], UPDATE, args[2]);
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      }
      return;
    } else if (args.length >= 1 && args[0].equals("b")) {
      SimpleDateFormat sqlFormatter = new SimpleDateFormat("yyyy-MM-dd");
      if (args.length == 1) {
        backup(sqlFormatter.format(new Date()), null);
        return;
      } else if (args.length == 2) {
        backup(sqlFormatter.format(new Date()), args[1]);
        return;
      }
    }
    System.err.println("USAGE: java zeta.tool.ZetaBackup r <user> <password> [<table>]\n"
                     + "       java zeta.tool.ZetaBackup e <server id>\n"
                     + "       java zeta.tool.ZetaBackup u <table> <append sql>\n"
                     + "       java zeta.tool.ZetaBackup b [<table>]");
  }

  static void backup(String date) {
    backup(date, null);
  }

  static void backup(String date, String table) {
    ZipOutputStream zip = null;
    try {
      File f = new File(backupFolder);
      f.mkdir();
      if (table != null) {
        backupTable(table, INSERT, "");
      } else {
        File[] list = f.listFiles();
        if (list != null) {
          for (int i = 0; i < list.length; ++i) {
            if (list[i].getName().endsWith(".bak")) {
              list[i].delete();
            }
          }
          for (int i = 0; i < list.length; ++i) {
            if (list[i].getName().endsWith(".ddl")) {
              list[i].renameTo(new File(list[i].getPath() + ".bak"));
            }
          }
        }
        backupTable("zeta.computation", INSERT, "");
        reorgTable("zeta.computation", null);
        backupTable("zeta.result", INSERT, "");
        reorgTable("zeta.result", null);
        backupTable("zeta.recomputation", INSERT, "");
        reorgTable("zeta.recomputation", null);
        backupTable("zeta.found", INSERT, "");
        reorgTable("zeta.found", "zeta.found_type");
        backupTable("zeta.workstation", INSERT, "");
        reorgTable("zeta.workstation", null);
        backupTable("zeta.user", INSERT, "");
        reorgTable("zeta.user", null);
        backupTable("zeta.server", INSERT, "");
        reorgTable("zeta.server", null);
        backupTable("zeta.server_size", INSERT, "");
        reorgTable("zeta.server_size", null);
        backupTable("zeta.server_synchronization", INSERT, "");
        reorgTable("zeta.server_synchronization", null);
        backupTable("zeta.parameter", INSERT, "");
        reorgTable("zeta.parameter", null);
        backupTable("zeta.error", INSERT, "");
        reorgTable("zeta.error", null);
        backupTable("zeta.task", INSERT, "");
        backupTable("zeta.program", INSERT, "");
        reorgTable("zeta.program", null);
        reorgTable("zeta.approve", "zeta.approve_key");

        zip = new ZipOutputStream(new FileOutputStream(backupFolder + '/' + date + ".zip"));
        zip.setLevel(Deflater.BEST_COMPRESSION);
        writeFile("zeta.computation.ddl", zip);
        writeFile("zeta.result.ddl", zip);
        writeFile("zeta.recomputation.ddl", zip);
        writeFile("zeta.found.ddl", zip);
        writeFile("zeta.workstation.ddl", zip);
        writeFile("zeta.user.ddl", zip);
        writeFile("zeta.server.ddl", zip);
        writeFile("zeta.server_size.ddl", zip);
        writeFile("zeta.parameter.ddl", zip);
        writeFile("zeta.error.ddl", zip);
        writeFile("zeta.task.ddl", zip);
        writeFile("zeta.program.ddl", zip);

        DatabaseUtils.db2CLP("CONNECT TO zeta;\nREORGCHK UPDATE STATISTICS;\nREORGCHK UPDATE STATISTICS ON TABLE SYSTEM;\nCONNECT RESET;\n", "reorgchk.txt", 60);    // ToDo: not fix zeta
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      StreamUtils.close(zip);
    }
  }

  static void restore(String user, String password) {
    restore(user, password, null);
  }

  static void restore(String user, String password, String table) {
    Connection con = null;
    Statement stmt = null;
    PreparedStatement pStmt = null;
    BufferedReader reader = null;
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    try {
      con = Database.getConnection(user, password);
      stmt = con.createStatement();
      File file = new File(backupFolder);
      File[] list = file.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; ++i) {
          String filename = list[i].getName();
          if ((table == null && filename.startsWith("zeta.") || table != null && filename.startsWith(table)) && filename.endsWith(".ddl")) {
            reader = new BufferedReader(new FileReader(list[i]));
            String tablename = filename.substring(0, filename.length()-4);
            System.out.println(formatter.format(new Date()) + ": restore table " + tablename);
            stmt.executeUpdate("DELETE FROM " + tablename);
            if (new File(tablename + ".1.zip").exists()) {
              String sql = reader.readLine();
              for (int idx = 1;; ++idx) {
                String s = reader.readLine();
                if (s == null) {
                  break;
                }
                s = s.substring(0, s.lastIndexOf(",NULL"));   // assumption: blob is only at the last column
                pStmt = con.prepareStatement(sql + s + ",?)");
                pStmt.setBytes(1, StreamUtils.getFile(backupFolder + '/' + tablename + '.' + idx + ".zip", false, false));
                if (pStmt.executeUpdate() != 1) {
                  System.err.println("Error at idx=" + idx);
                }
                DatabaseUtils.close(pStmt);
                pStmt = null;
              }
            } else {
              StringBuffer sql = new StringBuffer(100000);
              while (true) {
                String line = reader.readLine();
                if (line == null) {
                  break;
                }
                if (line.length() > 0) {
                  sql.append(line);
                  if (line.charAt(line.length()-1) == ';') {
                    stmt.executeUpdate(sql.substring(0, sql.length()-1));
                    sql.delete(0, sql.length());
                  }
                }
              }
            }
            StreamUtils.close(reader);
            reader = null;
          }
        }
      }
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(pStmt);
      DatabaseUtils.close(con);
      StreamUtils.close(reader);
    }
  }

  private static void errorTable(int serverId) {
    Connection con = null;
    Statement stmt = null;
    BufferedWriter writer = null;
    try {
      File file = new File("error.ddl");
      file.delete();
      con = Database.getConnection();
      stmt = con.createStatement();
      writer = new BufferedWriter(new FileWriter(file));
      StringBuffer buffer = new StringBuffer(1000);
      ResultSet rs = stmt.executeQuery("SELECT timestamp,sql_statement FROM zeta.error WHERE server_id=" + serverId + " ORDER BY timestamp");
      while (rs.next()) {
        buffer.delete(0, buffer.length());
        buffer.append(rs.getString(2));
        buffer.append(";\nDELETE FROM zeta.error WHERE timestamp=\'" + rs.getTimestamp(1) + "\';\n");
        writer.write(buffer.toString());
      }
      rs.close();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      StreamUtils.close(writer);
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(con);
    }
  }

  private static void backupTable(String table, int backupType, String appendSQL) throws Exception {
    List columns = new ArrayList(15);
    int countPrimaryKey = 0;
    if (table.equals("zeta.approve")) {
      countPrimaryKey = 0;
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("user_id", Integer.class));
      columns.add(new BackupTableColumn("key", String.class));
      columns.add(new BackupTableColumn("requested", Timestamp.class));
      columns.add(new BackupTableColumn("approved", Timestamp.class));
      columns.add(new BackupTableColumn("data", Blob.class));
    } else if (table.equals("zeta.computation")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("task_id", Integer.class));
      columns.add(new BackupTableColumn("work_unit_id", Long.class));
      columns.add(new BackupTableColumn("size", Integer.class));
      columns.add(new BackupTableColumn("redistributed_YN", String.class));
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("workstation_id", Integer.class));
      columns.add(new BackupTableColumn("user_id", Integer.class));
      columns.add(new BackupTableColumn("version", String.class));
      columns.add(new BackupTableColumn("start", Timestamp.class));
      columns.add(new BackupTableColumn("parameters", String.class));
    } else if (table.equals("zeta.result")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("task_id", Integer.class));
      columns.add(new BackupTableColumn("work_unit_id", Long.class));
      columns.add(new BackupTableColumn("stop", Timestamp.class));
    } else if (table.equals("zeta.recomputation")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("task_id", Integer.class));
      columns.add(new BackupTableColumn("work_unit_id", Long.class));
      columns.add(new BackupTableColumn("size", Integer.class));
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("workstation_id", Integer.class));
      columns.add(new BackupTableColumn("user_id", Integer.class));
      columns.add(new BackupTableColumn("version", String.class));
      columns.add(new BackupTableColumn("count", Integer.class));
      columns.add(new BackupTableColumn("reason", String.class));
      columns.add(new BackupTableColumn("start", Timestamp.class));
      columns.add(new BackupTableColumn("stop", Timestamp.class));
      columns.add(new BackupTableColumn("parameters", String.class));
    } else if (table.equals("zeta.found")) {
      countPrimaryKey = 4;
      columns.add(new BackupTableColumn("task_id", Integer.class));
      columns.add(new BackupTableColumn("work_unit_id", Long.class));
      columns.add(new BackupTableColumn("type", String.class));
      columns.add(new BackupTableColumn("found", String.class));
      columns.add(new BackupTableColumn("approved_YN", String.class));
      columns.add(new BackupTableColumn("timestamp", Timestamp.class));
    } else if (table.equals("zeta.workstation")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("id", Integer.class));
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("active_YN", String.class));
      columns.add(new BackupTableColumn("key", String.class));
      columns.add(new BackupTableColumn("hostname", String.class));
      columns.add(new BackupTableColumn("hostaddress", String.class));
      columns.add(new BackupTableColumn("last_update", Timestamp.class));
      columns.add(new BackupTableColumn("os_name", String.class));
      columns.add(new BackupTableColumn("os_version", String.class));
      columns.add(new BackupTableColumn("os_arch", String.class));
      columns.add(new BackupTableColumn("processors", Integer.class));
      columns.add(new BackupTableColumn("processors_approved", Integer.class));
      columns.add(new BackupTableColumn("number_of_redistributions", Integer.class));
      columns.add(new BackupTableColumn("last_redistributed_work_unit", Long.class));
      columns.add(new BackupTableColumn("last_redistributed_timestamp", Timestamp.class));
    } else if (table.equals("zeta.user")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("id", Integer.class));
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("fail", Integer.class));
      columns.add(new BackupTableColumn("trust", Integer.class));
      columns.add(new BackupTableColumn("active_YN", String.class));
      columns.add(new BackupTableColumn("recomputation_YN", String.class));
      columns.add(new BackupTableColumn("name", String.class));
      columns.add(new BackupTableColumn("email", String.class));
      columns.add(new BackupTableColumn("email_valid_YN", String.class));
      columns.add(new BackupTableColumn("team_name", String.class));
      columns.add(new BackupTableColumn("join_in_team", Timestamp.class));
      columns.add(new BackupTableColumn("number_of_redistributions", Integer.class));
      columns.add(new BackupTableColumn("last_redistributed_work_unit", Long.class));
      columns.add(new BackupTableColumn("last_redistributed_timestamp", Timestamp.class));
      columns.add(new BackupTableColumn("properties", String.class));
    } else if (table.equals("zeta.server")) {
      countPrimaryKey = 1;
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("active_YN", String.class));
      columns.add(new BackupTableColumn("logging_path", String.class));
      columns.add(new BackupTableColumn("logging_max_backup", Integer.class));
      columns.add(new BackupTableColumn("logging_max_filesize", Long.class));
      columns.add(new BackupTableColumn("post_YN", String.class));
      columns.add(new BackupTableColumn("get_YN", String.class));
      columns.add(new BackupTableColumn("web_hostname", String.class));
      columns.add(new BackupTableColumn("web_port", Integer.class));
      columns.add(new BackupTableColumn("smtp_hostname", String.class));
      columns.add(new BackupTableColumn("smtp_port", Integer.class));
      columns.add(new BackupTableColumn("smtp_login_name", String.class));
      columns.add(new BackupTableColumn("smtp_login_password", String.class));
      columns.add(new BackupTableColumn("send_mail_from", String.class));
      columns.add(new BackupTableColumn("proxy_host", String.class));
      columns.add(new BackupTableColumn("proxy_port", Integer.class));
      columns.add(new BackupTableColumn("size", Long.class));
      columns.add(new BackupTableColumn("synchronization_url", String.class));
      columns.add(new BackupTableColumn("last_synchronization", Timestamp.class));
      columns.add(new BackupTableColumn("key", Blob.class));
    } else if (table.equals("zeta.server_size")) {
      countPrimaryKey = 3;
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("task_id", Integer.class));
      columns.add(new BackupTableColumn("work_unit_id", Long.class));
      columns.add(new BackupTableColumn("size", Long.class));
      columns.add(new BackupTableColumn("start", Timestamp.class));
    } else if (table.equals("zeta.server_synchronization")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("timestamp", Timestamp.class));
      columns.add(new BackupTableColumn("sql_statement", String.class));
    } else if (table.equals("zeta.parameter")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("task_id", Integer.class));
      columns.add(new BackupTableColumn("parameter", String.class));
      columns.add(new BackupTableColumn("value", String.class));
    } else if (table.equals("zeta.error")) {
      countPrimaryKey = 2;
      columns.add(new BackupTableColumn("server_id", Integer.class));
      columns.add(new BackupTableColumn("timestamp", Timestamp.class));
      columns.add(new BackupTableColumn("sql_statement", String.class));
    } else if (table.equals("zeta.task")) {
      countPrimaryKey = 1;
      columns.add(new BackupTableColumn("id", Integer.class));
      columns.add(new BackupTableColumn("name", String.class));
      columns.add(new BackupTableColumn("client_task_class_name", String.class));
      columns.add(new BackupTableColumn("work_unit_class_name", String.class));
      columns.add(new BackupTableColumn("encryption_class", String.class));
      columns.add(new BackupTableColumn("encryption_signature", String.class));
      columns.add(new BackupTableColumn("decryption_number", String.class));
      columns.add(new BackupTableColumn("request_processor", String.class));
      columns.add(new BackupTableColumn("result_processor", String.class));
      columns.add(new BackupTableColumn("parameters", String.class));
      columns.add(new BackupTableColumn("verifier_class_name", String.class));
      columns.add(new BackupTableColumn("overall_signature", String.class));
      columns.add(new BackupTableColumn("redistrib_connected", Integer.class));
      columns.add(new BackupTableColumn("redistrib_unconnected", Integer.class));
    } else if (table.equals("zeta.program")) {
      countPrimaryKey = 6;
      columns.add(new BackupTableColumn("task_id", Integer.class));
      columns.add(new BackupTableColumn("name", String.class));
      columns.add(new BackupTableColumn("os_name", String.class));
      columns.add(new BackupTableColumn("os_version", String.class));
      columns.add(new BackupTableColumn("os_arch", String.class));
      columns.add(new BackupTableColumn("processors", Integer.class));
      columns.add(new BackupTableColumn("version", String.class));
      columns.add(new BackupTableColumn("key_class_name", String.class));
      columns.add(new BackupTableColumn("program_from_user", String.class));
      columns.add(new BackupTableColumn("compressed_YN", String.class));
      columns.add(new BackupTableColumn("last_update", Timestamp.class));
      columns.add(new BackupTableColumn("signature", String.class));
      columns.add(new BackupTableColumn("overall_signature", String.class));
      columns.add(new BackupTableColumn("program", Blob.class));
    }

    if (columns.size() > 0) {
      BackupTableColumn[] c = new BackupTableColumn[columns.size()];
      for (int i = 0; i < c.length; ++i) {
        c[i] = (BackupTableColumn)columns.get(i);
      }
      backupTable(table, c, backupType, appendSQL, countPrimaryKey);
    } else {
      throw new IllegalArgumentException("table '" + table + "' is not defined");
    }
  }

  private static void backupTable(String table, BackupTableColumn[] columns, int backupType, String appendSQL, int countPrimaryKey) throws Exception {
    int originMaxCharsPerInsert = maxCharsPerInsert;
    StringBuffer s = new StringBuffer(30000);
    s.append("SELECT ");
    final int l = columns.length;
    for (int i = 0; i < l; ++i) {
      if (columns[i].type != null) {
        if (i > 0) {
          s.append(',');
        }
        s.append(columns[i].column);
      }
    }
    s.append(" FROM ");
    s.append(table);
    if (appendSQL != null && appendSQL.length() > 0) {
      s.append(' ');
      s.append(appendSQL);
    }
    if (backupType == UPDATE) {
      s.append(" ORDER BY ");
      for (int i = 0; i < countPrimaryKey; ++i) {
        if (i > 0) {
          s.append(',');
        }
        s.append(columns[i].column);
      }
    }
    String select = s.toString();
    s.delete(0, s.length());
    if (backupType == INSERT) {
      s.append("INSERT INTO ");
      s.append(table);
      s.append(" (");
    } else if (backupType == UPDATE) {
      if (countPrimaryKey < 1) {
        throw new IllegalArgumentException("countPrimaryKey undefined");
      }
      maxCharsPerInsert = 1;
      s.append("UPDATE ");
      s.append(table);
      s.append(" SET (");
    } else {
      throw new IllegalArgumentException();
    }
    boolean first = true;
    for (int i = (backupType != UPDATE)? 0 : countPrimaryKey; i < l; ++i) {
      if (!first) {
        s.append(',');
      }
      first = false;
      s.append(columns[i].column);
    }
    if (backupType == INSERT) {
      s.append(") VALUES\n");
    } else {
      s.append(")=\n");
    }
    String insert = s.toString();
    s.delete(0, s.length());
    Connection con = Database.getConnection();
    con.setReadOnly(true);
    int transactionIsolation = con.getTransactionIsolation();
    if (con.getMetaData().supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED) && (transactionIsolation == Connection.TRANSACTION_REPEATABLE_READ || transactionIsolation == Connection.TRANSACTION_SERIALIZABLE || transactionIsolation == Connection.TRANSACTION_NONE)) {
      con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }
    Statement stmt = null;
    BufferedWriter writer = null;
    try {
      stmt = con.createStatement();
      writer = new BufferedWriter(new FileWriter(backupFolder + '/' + table + ((backupType > 0)? Integer.toString(backupType) : "") + ".ddl"));
      int charsPerInsert = maxCharsPerInsert;
      System.out.println(select);
      ResultSet rs = stmt.executeQuery(select);
      int idx = 0;
      while (rs.next()) {
        ++idx;
        if (charsPerInsert >= maxCharsPerInsert) {
          if (s.length() > 0) {
            if (backupType == UPDATE) {
              s.append(" WHERE ");
              int posValue = insert.length();
              while (posValue < s.length() && s.charAt(posValue) != '(') {
                ++posValue;
              }
              int startValue = posValue+1;
              for (int i = 0; i < countPrimaryKey && posValue > 0 && posValue+1 < s.length(); ++i) {
                s.append(columns[i].column);
                s.append('=');
                if (columns[i].type == Integer.class || columns[i].type == Long.class || columns[i].type == String.class || columns[i].type == Timestamp.class) {
                  int posValueEnd = ++posValue;
                  while (posValueEnd < s.length() && s.charAt(posValueEnd) != ',') {
                    ++posValueEnd;
                  }
                  s.append(s.substring(posValue, posValueEnd));
                  posValue = posValueEnd;
                } else {
                  throw new IllegalArgumentException("primary key type not supported");
                }
                if (i+1 < countPrimaryKey) {
                  s.append(" AND ");
                }
              }
              s.delete(startValue, posValue+1);
            }
            s.append(";\n");
            writer.write(s.toString());
          }
          s.delete(0, s.length());
          charsPerInsert = 0;
        }
        final int size = s.length();
        s.append((charsPerInsert == 0)? insert : ",\n");
        s.append(" (");
        for (int i = 0; i < l; ++i) {
          if (i > 0) {
            s.append(',');
          }
          if (columns[i].type == Integer.class) {
            s.append(rs.getInt(i+1));
          } else if (columns[i].type == String.class) {
            String s2 = rs.getString(i+1);
            if (s2 == null) {
              s.append("NULL");
            } else {
              s.append('\'');
              s.append(DatabaseUtils.encodeName(s2));
              s.append('\'');
            }
          } else if (columns[i].type == Timestamp.class) {
            Timestamp t = rs.getTimestamp(i+1);
            if (t == null) {
              s.append("NULL");
            } else {
              s.append('\'');
              s.append(t);
              s.append('\'');
            }
          } else if (columns[i].type == Long.class) {
            s.append(rs.getLong(i+1));
          } else if (columns[i].type == Blob.class) {
            s.append("NULL");
            InputStream in = rs.getBinaryStream(i+1);
            if (in != null) {
              StreamUtils.writeData(in, new FileOutputStream(backupFolder + '/' + table + '.' + idx + ".zip"), true, true);
            }
          } else if (columns[i].type == null) {
            s.append("NULL");
          } else throw new SQLException("Not implemented!");
        }
        s.append(')');
        charsPerInsert += s.length()-size;
      }
      if (s.length() > 0) {
        if (backupType == UPDATE) {
          s.append(" WHERE ");
          int posValue = insert.length();
          while (posValue < s.length() && s.charAt(posValue) != '(') {
            ++posValue;
          }
          int startValue = posValue+1;
          for (int i = 0; i < countPrimaryKey && posValue > 0 && posValue+1 < s.length(); ++i) {
            s.append(columns[i].column);
            s.append('=');
            if (columns[i].type == Integer.class || columns[i].type == Long.class || columns[i].type == String.class || columns[i].type == Timestamp.class) {
              int posValueEnd = ++posValue;
              while (posValueEnd < s.length() && s.charAt(posValueEnd) != ',') {
                ++posValueEnd;
              }
              s.append(s.substring(posValue, posValueEnd));
              posValue = posValueEnd;
            } else {
              throw new IllegalArgumentException("primary key type not supported");
            }
            if (i+1 < countPrimaryKey) {
              s.append(" AND ");
            }
          }
          s.delete(startValue, posValue+1);
        }
        s.append(";\n");
        writer.write(s.toString());
      }
      rs.close();
    } finally {
      StreamUtils.close(writer);
      DatabaseUtils.close(stmt);
      if (con != null) {
        try {
          con.setReadOnly(false);
          con.setTransactionIsolation(transactionIsolation);
        } catch (SQLException se) {
        }
        DatabaseUtils.close(con);
      }
    }
    maxCharsPerInsert = originMaxCharsPerInsert;
  }

  private static void reorgTable(String table, String primaryKeyName) throws Exception {
    Connection con = Database.getConnection();
    try {
      String origTable = table;
      String schema = "";
      int idx = table.indexOf('.');
      if (idx > 0) {
        schema = table.substring(0, idx);
        table = table.substring(idx+1);
      }
      if (primaryKeyName == null) {
        DatabaseMetaData dmd = con.getMetaData();
        ResultSet rs = dmd.getPrimaryKeys(null, schema, table);
        if (rs.next()) {
          primaryKeyName = rs.getString(6);
        }
        rs.close();
      }
      StringBuffer sql = new StringBuffer(300);
      sql.append("CONNECT TO zeta;\n");    // ToDo: not fix zeta
      sql.append("REORG TABLE ");
      sql.append(origTable);
      if (primaryKeyName != null) {
        idx = primaryKeyName.indexOf('.');
        if (idx == 0) {
          primaryKeyName = "sysibm." + primaryKeyName;
        }
        sql.append(" INDEX ");
        sql.append(primaryKeyName);
      }
      sql.append(";\n");
      sql.append("-- REORG INDEXES ALL FOR TABLE ");
      sql.append(origTable);
      sql.append(";\n");
      sql.append("RUNSTATS ON TABLE ");
      sql.append(origTable);
      sql.append(" AND DETAILED INDEXES ALL SHRLEVEL CHANGE;\n");
      sql.append("CONNECT RESET;\n");
      DatabaseUtils.db2CLP(sql.toString(), null, 120);
    } finally {
      DatabaseUtils.close(con);
    }
  }

  private static void writeFile(String filename, ZipOutputStream zip) throws IOException {
    filename = backupFolder + '/' + filename;
    zip.putNextEntry(new ZipEntry(filename));
    StreamUtils.writeData(new FileInputStream(filename), zip, true, false);
  }

  static class BackupTableColumn {
    BackupTableColumn(String column, Class type) {
      this.type = type;
      this.column = column;
    }

    Class type;
    String column;
  }
}
