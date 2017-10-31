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

package zeta.server.tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import zeta.ClientTask;
import zeta.WorkUnit;
import zeta.server.ServerTask;
import zeta.server.TaskManager;
import zeta.server.WorkUnitVerifier;
import zeta.crypto.Decrypter;
import zeta.crypto.KeyManager;
import zeta.server.util.DatabaseUtils;
import zeta.util.Base64;
import zeta.util.Properties;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

import BlowfishJ.BlowfishECB;

/**
 *  Retrieves and removes asynchronous the results of the complete transferred work units.
 *  There are three implemented ways to retrieve the data. The data can be retrieved
 *  from the tables zeta.result or zeta.recomputation of the database, from a shared file server of the
 *  application servers via FTP or via HTTP. But only the retrieval from the database is supported
 *  since the other implementations are not generic enough for multiple tasks.
 *
 *  The work units will also be decrypted and verified for completeness and correctness.
 *  A recomputation will be triggered if the decryption or the verification of the work unit fails.
 *  The file extension ".$$$" is reserved for temporary files during the decryption.
 *
 *  The property "temp_dir" is a temporary directory where the work units will be decrypted and verified.
 *  The default temporary directory is "z:/" which can be a RAM disk to improve performance.
 *  The property "final_dir" specifies the directory where the complete verified work units should be stored.
 *
 *  @see zeta.server.WorkUnitVerifier
 *  @version 1.9.4, August 27, 2004
**/
public class GetData {

  public static void main(String[] args) throws IOException {
    if (args.length == 2) {
      Connection con = null;
      try {
        con = Database.getConnection();
        taskManager = TaskManager.getInstance(con);
        int taskId = Integer.parseInt(args[0]);
        ServerTask serverTask = taskManager.getServerTask(taskId);
        if (serverTask != null) {
          decrypt(serverTask.createWorkUnit(Long.parseLong(args[1]), Integer.parseInt(args[2]), false), args[3]);
        } else {
          System.err.println("The task ID " + taskId + " is undefined!");
        }
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      } finally {
        DatabaseUtils.close(con);
      }
    } else {
      get();
    }
  }

  public static void decrypt(WorkUnit workUnit, String filename) throws IOException {
    decrypt(workUnit, filename, null);
  }

  static boolean get() {
    if (taskManager == null) {
      Connection con = null;
      try {
        con = Database.getConnection();
        taskManager = TaskManager.getInstance(con);
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      } finally {
        DatabaseUtils.close(con);
      }
    }
    getViaHttp();
    getViaFtp();
    boolean result = getFromDatabase("SELECT comp.work_unit_id,comp.size,comp.result,ws.hostname,comp.task_id FROM"
                         + " zeta.recomputation comp, zeta.workstation ws WHERE comp.result IS NOT NULL"
                         + " AND ws.id=comp.workstation_id AND ws.server_id=comp.server_id ORDER BY comp.work_unit_id", true);
    if (getFromDatabase("SELECT res.work_unit_id,comp.size,res.result,ws.hostname,comp.task_id FROM"
            + " zeta.result res, zeta.workstation ws, zeta.computation comp WHERE res.result IS NOT NULL"
            + " AND ws.id=comp.workstation_id AND ws.server_id=comp.server_id AND comp.work_unit_id=res.work_unit_id AND comp.task_id=res.task_id ORDER BY res.work_unit_id", false)) {
      result = true;
    }
    return result;
  }

  /**
   *  1: work_unit_id, 2: size, 3: result, 4: hostname, 5: task_id
  **/
  private static boolean getFromDatabase(String sql, boolean removeData) {
    StringBuffer remove = new StringBuffer(500);
    boolean newFiles = false;
    Connection connection = null;
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
      String path1 = properties.get("data.servlet.url");
      String path2 = properties.get("ftp.path");
      if (path1 != null && path1.length() > 0 || path2 != null && path2.length() > 0) {
        return false;
      }
      System.out.println("database");
      connection = Database.getConnection();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        Blob blob = rs.getBlob(3);
        InputStream in = blob.getBinaryStream();
        long workUnitId = rs.getLong(1);
        int size = rs.getInt(2);
        int taskId = rs.getInt(5);
        ServerTask serverTask = taskManager.getServerTask(taskId);
        if (serverTask != null) {
          WorkUnit workUnit = serverTask.createWorkUnit(workUnitId, size, false);
          getData(connection, stmt2, workUnit, in, rs.getString(4), ConstantProperties.TEMP_DIR + workUnit.getWorkUnitFileName(), remove);
          newFiles = true;
          try {
            Thread.sleep(100);
          } catch (InterruptedException ie) {
          }
        } else {
          System.err.println("The task ID " + taskId + " is undefined!");
        }
      }
      rs.close();
      if (removeData && remove.length() > 0) {
        remove.append(')');
        stmt.executeUpdate(remove.toString());
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
      DatabaseUtils.close(connection);
    }
    return newFiles;
  }

  /**
   *  The URI will be encrypted by the Blowfish algorithm if the global parameter encryption is enabled.
  **/
  private static void getViaHttp() {
    Connection connection = null;
    Statement stmt = null;
    HttpURLConnection connectionGet = null;
    int proxyIndex = 0;
    boolean again2 = false;
    Iterator iterServerTasks = taskManager.getServerTasks();
    while (iterServerTasks.hasNext()) {
      ServerTask serverTask = (ServerTask)iterServerTasks.next();
      do {
        String[] proxies = null;
        again2 = false;
        try {
          Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
          String path = properties.get("data.servlet.url");
          if (path == null || path.length() == 0) {
            return;
          }
          String prp = properties.get("proxies");
          if (prp != null) {
            StringTokenizer st = new StringTokenizer(prp, ",");
            proxies = new String[st.countTokens()];
            for (int i = 0; i < proxies.length; ++i) {
              proxies[i] = st.nextToken();
            }
          }
          java.util.Properties props = System.getProperties();
          props.put("sun.net.inetaddr.ttl", "0");
          props.put("networkaddress.cache.ttl", "0");
          props.put("networkaddress.cache.negative.ttl", "0");
          connection = Database.getConnection();
          stmt = connection.createStatement();
          byte[] serverKey = null;
          int encryptionId = properties.get("data.servlet.encryption.id", 0);
          if (encryptionId > 0) {
            ResultSet rs = stmt.executeQuery("SELECT key FROM zeta.server WHERE server_id=" + encryptionId);
            serverKey = (rs.next())? rs.getBytes(1) : null;
            rs.close();
          }
          if (proxies != null) {
            props.put("http.proxyHost", proxies[proxyIndex]);
            System.out.println("using proxy " + proxies[proxyIndex]);
          }
          URL url = new URL("http", properties.get("data.servlet.host", ""), properties.get("data.servlet.port", 80), path + "?task=" + serverTask.getId()
                           + '&' + encrypt(serverKey, "dir=."));
          connectionGet = (HttpURLConnection)url.openConnection();
          connectionGet.setRequestMethod("GET");
          connectionGet.setDoInput(true);
          connectionGet.connect();
          if (connectionGet.getResponseCode() == HttpURLConnection.HTTP_OK) {
            int size = connectionGet.getContentLength();
            ByteArrayOutputStream out = new ByteArrayOutputStream(size+10);
            StreamUtils.writeData(connectionGet.getInputStream(), out, false, true);
            connectionGet.disconnect();
            connectionGet = null;
            byte[] buffer = out.toByteArray();
            if (buffer.length == size) {
              proxyIndex = 0;
              String s = new String(buffer);
              final int l = s.length();
              List filenames = new ArrayList(Math.max(1, l/20));
              for (int i = 0; i < l; ++i) {
                int j = s.indexOf('\n', i);
                if (j == -1) {
                  filenames.add(s.substring(i));
                  break;
                }
                filenames.add(s.substring(i, j));
                i = j;
              }
              System.out.println("download " + filenames.size() + " files via servlet.");
              String filename = null;
              String deleteFilename = null;
              boolean again = false;
              Iterator iter = filenames.iterator();
              while (true) {
                if (!again) {
                  if (iter.hasNext()) {
                    filename = (String)iter.next();
                  } else {
                    filename = null;
                  }
                }
                WorkUnit workUnit = null;
                if (filename != null) {
                  List list = serverTask.createWorkUnits(new String[] {filename});
                  if (!list.isEmpty()) {
                    workUnit = (WorkUnit)list.get(0);
                  }
                }
                again = false;
                if (filename == null || workUnit != null) {
                  String hostname = "";
                  if (workUnit != null) {
                    ResultSet rs = stmt.executeQuery("SELECT ws.hostname FROM zeta.computation comp, zeta.workstation ws WHERE task_id=" + workUnit.getTaskId()
                                                   + " AND comp.workstation_id=ws.id AND comp.server_id=ws.server_id AND comp.work_unit_id=" + workUnit.getWorkUnitId());
                    if (rs.next()) {
                      hostname = rs.getString(1);
                    }
                    rs.close();
                  }
                  InputStream in = null;
                  try {
                    StringBuffer pathExt = new StringBuffer(200);
                    pathExt.append(path);
                    pathExt.append("?task=");
                    pathExt.append(serverTask.getId());
                    pathExt.append('&');
                    if (size > 0 && filename != null) {
                      if (deleteFilename != null && !deleteFilename.equals(filename)) {
                        pathExt.append(encrypt(serverKey, "get=" + filename + "&del=" + deleteFilename));
                      } else {
                        pathExt.append(encrypt(serverKey, "get=" + filename));
                      }
                    } else if (deleteFilename != null) {
                      pathExt.append(encrypt(serverKey, "del=" + deleteFilename));
                    }
                    url = new URL("http", properties.get("data.servlet.host", ""), properties.get("data.servlet.port", 80), pathExt.toString());
                    connectionGet = (HttpURLConnection)url.openConnection();
                    connectionGet.setRequestMethod("GET");
                    connectionGet.setDoInput(true);
                    connectionGet.connect();
                    if (connectionGet.getResponseCode() == HttpURLConnection.HTTP_OK) {
                      deleteFilename = filename;
                      if (size > 0) {
                        size = connectionGet.getContentLength();
                        out = new ByteArrayOutputStream(size+10);
                        StreamUtils.writeData(connectionGet.getInputStream(), out, false, true);
                        connectionGet.disconnect();
                        connectionGet = null;
                        buffer = out.toByteArray();
                        if (buffer.length == size) {
                          in = new ByteArrayInputStream(buffer);
                          getData(connection, stmt, workUnit, in, hostname, ConstantProperties.TEMP_DIR + workUnit.getWorkUnitFileName(), null);
                        } else {
                          throw new IOException("data not completed (received " + buffer.length + " instead of " + size + ')');
                        }
                      }
                      if (proxyIndex != 0) {
                        proxyIndex = 0;
                        props.put("http.proxyHost", proxies[proxyIndex]);
                        System.out.println("use proxy=" + proxies[proxyIndex]);
                      }
                    }
                  } catch (IOException ioe) {
                    if (proxies != null) {
                      if (++proxyIndex < proxies.length) {
                        props.put("http.proxyHost", proxies[proxyIndex]);
                        System.out.println("use proxy=" + proxies[proxyIndex]);
                        again = true;
                        try {
                          Thread.sleep(500);
                        } catch (InterruptedException ie) {
                        }
                      } else {
                        ThrowableHandler.handle(ioe);
                        proxyIndex = 0;
                      }
                    }
                  } catch (Exception e) {
                    ThrowableHandler.handle(e);
                  } finally {
                    StreamUtils.close(in);
                  }
                }
                if (filename == null) {
                  break;
                }
              }
            } else {
              throw new IOException("data not completed (received " + buffer.length + " instead " + size + ')');
            }
          }
        } catch (Exception e) {
          ThrowableHandler.handle(e);
          if (proxies != null && ++proxyIndex < proxies.length) {
            again2 = true;
          } else {
            proxyIndex = 0;
          }
        } finally {
          if (connectionGet != null) {
            connectionGet.disconnect();
          }
          DatabaseUtils.close(stmt);
          DatabaseUtils.close(connection);
        }
      } while (again2);
    }
  }

  private static String encrypt(byte[] serverKey, String text) {
    if (serverKey != null) {
      try {
        byte[] statements = text.getBytes("UTF-8");
        int statementsLength = statements.length;
        statements = StreamUtils.align8(statements);
        BlowfishECB bfecb = new BlowfishECB(serverKey);
        bfecb.encrypt(statements);
        bfecb.cleanUp();
        text = "actions=" + Base64.encode(statements) + "&actions_length=" + statementsLength;
      } catch (UnsupportedEncodingException ue) {
      }
    }
    return text;
  }

  private static void getViaFtp() {
    Connection connection = null;
    Statement stmt = null;
    Statement stmt2 = null;
    FTP ftp = null;
    try {
      Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
      String path = properties.get("ftp.path");
      if (path == null || path.length() == 0) {
        return;
      }
      connection = Database.getConnection();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      ftp = new FTP();
      ftp.setSocketTimeout(properties.get("ftp.socket.timeout", 50000));
      ftp.setBufferSize(properties.get("ftp.buffer.size", 10240));
      if (properties.get("ftp.debug", "true").equals("true")) {
        ftp.setDebug(System.out);
      } else {
        ftp.setDebug(null);
      }
      ftp.connect(properties.get("ftp.host", ""));
      ftp.login(properties.get("ftp.user", ""), properties.get("ftp.password", ""));
      ftp.cd(path);
      boolean again = false;
      int numberOfFiles = -1;
      do {
        List dir = ftp.longDir();
        Iterator j = taskManager.getServerTasks();
        while (j.hasNext()) {
          ServerTask serverTask = (ServerTask)j.next();
          WorkUnit dummyWorkUnit = serverTask.createWorkUnit(1, 1, null, false);
          again = false;
          numberOfFiles = -1;
          String maxTime = "";
          Iterator i = dir.iterator();
          while (i.hasNext()) {
            String s = (String)i.next();
            int idx = s.indexOf(dummyWorkUnit.getFileNamePrefix());
            if (idx >= 13) {
              int shift = (s.charAt(idx-13) == ' ')? 12 : 13;
              s = s.substring(idx-shift);
              if (s.length() > 12 && (!again || maxTime.substring(0, maxTime.indexOf(':')-3).equals(s.substring(0, s.indexOf(':')-3))) && maxTime.compareTo(s) < 0) {
                maxTime = s.substring(0, 12);
              }
              if (!again) {
                idx = maxTime.indexOf(':');
                int idx2 = s.indexOf(':');
                if (idx > 2 && idx2 > 2 && (idx != idx2 || !maxTime.substring(0, idx-3).equals(s.substring(0, idx2-3)))) {
                  final String[] month = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
                  for (idx = 0; idx < month.length && !month[idx].equals(maxTime.substring(0, 3)); ++idx);
                  for (idx2 = 0; idx2 < month.length && !month[idx2].equals(s.substring(0, 3)); ++idx2);
                  if (idx == month.length || idx2 == month.length) {
                    System.out.println("Wrong month " + maxTime + ", " + s);
                    System.exit(1);
                  }
                  if (idx2 < idx) {
                    maxTime = s;
                  } else if (idx2 == idx && maxTime.compareTo(s.substring(0, maxTime.length())) > 0) {
                    maxTime = s;
                  }
                  again = true;
                }
              }
              ++numberOfFiles;
            }
          }

        System.out.println("download " + Math.max(0, numberOfFiles) + " files via ftp.");
        ftp.setTransferSleep(properties.get("ftp.sleep", 200));
        for (i = dir.iterator(); i.hasNext();) {
          String s = (String)i.next();
          int idx = s.indexOf(dummyWorkUnit.getFileNamePrefix());
          if (idx >= 13) {
            int shift = (s.charAt(idx-13) == ' ')? 12 : 13; // maybe does not work with various FTP servers
            if (again && maxTime.substring(0, maxTime.indexOf(':')-3).equals(s.substring(idx-shift, s.indexOf(':')-3)) || !again && maxTime.compareTo(s.substring(idx-shift, idx)) > 0) {
              try {
                String filename = s.substring(idx);


                idx = filename.indexOf('_', 11);
                if (idx > 0) {
                  System.out.print(formatter.format(new Date()));
                  System.out.flush();
                  ftp.get(filename, ConstantProperties.TEMP_DIR + filename, FTP.MODE_BINARY);
                  long workUnitId = Long.parseLong(filename.substring(11, idx)); //ToDo: not fix
                  int size = Integer.parseInt(filename.substring(idx+1, filename.length()-4));
                  ResultSet rs = stmt.executeQuery("SELECT ws.hostname FROM zeta.computation comp, zeta.workstation ws WHERE task_id=" + serverTask.getId()
                                                 + " AND comp.workstation_id=ws.id AND comp.server_id=ws.server_id AND comp.work_unit_id=" + workUnitId);
                  if (rs.next()) {
                    InputStream in = null;
                    try {
                      in = new FileInputStream(ConstantProperties.TEMP_DIR + filename);
                      WorkUnit workUnit = serverTask.createWorkUnit(workUnitId, size, false);
                      getData(connection, stmt2, workUnit, in, rs.getString(1), ConstantProperties.TEMP_DIR + workUnit.getWorkUnitFileName(), null);
                    } catch (IOException ioe) {
                      ThrowableHandler.handle(ioe);
                    } finally {
                      StreamUtils.close(in);
                    }
                    new File(ConstantProperties.TEMP_DIR + filename).delete();
                  }
                  rs.close();
                  ftp.deleteFile(filename);
                }
              } catch (SQLException se) {
                ThrowableHandler.handle(se);
                DatabaseUtils.close(stmt);
                DatabaseUtils.close(stmt2);
                DatabaseUtils.close(connection);
                try {
                  Thread.sleep(2000);
                } catch (InterruptedException ie) {
                }
                connection = Database.getConnection();
                stmt = connection.createStatement();
                stmt2 = connection.createStatement();
              } catch (SocketTimeoutException ste) {
                ThrowableHandler.handle(ste);
                ftp.disconnect();
                ftp = null;
                ftp = new FTP();
                ftp.setSocketTimeout(properties.get("ftp.socket.timeout", 50000));
                ftp.setBufferSize(properties.get("ftp.buffer.size", 10240));
                if (properties.get("ftp.debug", "true").equals("true")) {
                  ftp.setDebug(System.out);
                } else {
                  ftp.setDebug(null);
                }
                ftp.connect(properties.get("ftp.host", ""));
                ftp.login(properties.get("ftp.user", ""), properties.get("ftp.password", ""));
                ftp.cd(path);
              }
            }
          }
        }


        }
      } while (again && numberOfFiles >= 20);
    } catch (Throwable e) {   // an OutOfMemoryError may occur
      ThrowableHandler.handle(e);
      if (e.getMessage().indexOf("closing control connection") >= 0) {
        ftp = null;
      }
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
      DatabaseUtils.close(connection);
      if (ftp != null) {
        ftp.disconnect();
      }
    }
  }

  private static boolean getData(Connection connection, Statement stmt2, WorkUnit workUnit, InputStream dataIn, String hostname, String outputFilename, StringBuffer remove) throws IOException, SQLException {
    File file = new File(outputFilename);
    System.out.print(hostname + ": " + file.getName());
    System.out.flush();
    FileOutputStream out = new FileOutputStream(file);
    StreamUtils.writeData(dataIn, out, true, true);
    try {
      decrypt(workUnit, outputFilename, connection);
      File file1 = new File(ConstantProperties.FINAL_DIR + '/' + workUnit.getTaskId() + '/' + outputFilename.substring(ConstantProperties.TEMP_DIR.length()));
      File file2 = new File(outputFilename);
      if (file2.exists() && (!file1.exists() || file1.delete())) {
        file2.renameTo(file1);
      }
      if (file1.exists() && file1.length() > 0) {
        if (remove != null) {
          if (remove.length() == 0) {
            remove.append("UPDATE zeta.recomputation SET result=NULL WHERE NOT result IS NULL AND task_id=" + workUnit.getTaskId() + " AND work_unit_id IN (");
            remove.append(workUnit.getWorkUnitId());
          } else {
            remove.append(',');
            remove.append(workUnit.getWorkUnitId());
          }
        }
        try {
          stmt2.executeUpdate("UPDATE zeta.result SET result=NULL WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
        } catch (SQLException se) {
          ThrowableHandler.handle(se);
        }
      }
      System.out.println(".");
      return true;
    } catch (FileNotFoundException fnfe) {
      ThrowableHandler.handle(fnfe);
    } catch (Exception ioe) {
      System.out.println(file.getName() + ": ZIP Error!");
      file.delete();
      StringWriter writer = new StringWriter(1000);
      ioe.printStackTrace(new PrintWriter(writer));
      writer.flush();
      if (ioe instanceof IOException) {
        try {
          if (!StreamUtils.checkAvailDiskSpace(new ByteArrayInputStream(new byte[6*1024*1024]), new File(ConstantProperties.TEMP_DIR + outputFilename + ".$$$"))) {
            System.err.println(writer.toString());
            return false;
          }
        } catch (IOException ie) {
          ThrowableHandler.handle(ie);
          return false;
        }
      }
      Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
      int serverId = 1;
      ResultSet rs2 = stmt2.executeQuery("SELECT server_id FROM zeta.computation WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
      if (rs2.next()) {
        serverId = rs2.getInt(1);
      } else {
        System.err.println("FATAL Error: server_id is undefined");
        System.exit(1);
      }
      rs2.close();
      rs2 = stmt2.executeQuery("SELECT stop FROM zeta.recomputation WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
      if (rs2.next()) {
        Timestamp stop = rs2.getTimestamp(1);
        rs2.close();
        if (stop != null) {
          DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                            "UPDATE zeta.recomputation SET (start,count,stop,result,reason)=(CURRENT TIMESTAMP,count+1,NULL,NULL," + DatabaseUtils.encodeName(writer.toString()) + ") WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
        }
      } else {
        rs2.close();
        rs2 = stmt2.executeQuery("SELECT version,workstation_id,user_id,parameters FROM zeta.computation WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
        if (rs2.next()) {
          String version = rs2.getString(1);
          int workstationId = rs2.getInt(2);
          int userId = rs2.getInt(3);
          String parameters = rs2.getString(4);
          rs2.close();
          DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                            "INSERT INTO zeta.recomputation (task_id,work_unit_id,size,server_id,version,workstation_id,user_id,start,reason,parameters) VALUES ("
                                            + workUnit.getTaskId() + ',' + workUnit.getWorkUnitId() + ',' + workUnit.getSize() + ',' + serverId + ",'" + version + "'," + workstationId + ',' + userId + ",CURRENT TIMESTAMP,"
                                            + DatabaseUtils.encodeName(writer.toString())
                                            + ((parameters == null)? ",NULL)" : ",'" + parameters + "')"));
          stmt2.executeUpdate("UPDATE zeta.result SET result=NULL where task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
        } else {
          rs2.close();
        }
      }
    }
    return false;
  }

  private static void decrypt(WorkUnit workUnit, String filename, Connection connection) throws IOException {
    if (taskManager == null) {
      Connection con = null;
      try {
        if (connection == null) {
          con = Database.getConnection();
        }
        taskManager = TaskManager.getInstance((con == null)? connection : con);
      } catch (Exception e) {
        ThrowableHandler.handle(e);
        return;
      } finally {
        DatabaseUtils.close(con);
      }
    }
    ServerTask serverTask = taskManager.getServerTask(workUnit.getTaskId());
    if (serverTask == null) {
      System.err.println("The task ID " + workUnit.getTaskId() + " is undefined!");
      return;
    }
    Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
    ZipInputStream zip = null;
    ZipOutputStream zipOut = null;
    FileOutputStream out = null;
    FileInputStream in = null;
    try {
      zip = new ZipInputStream(new FileInputStream(filename));
      zipOut = new ZipOutputStream(new FileOutputStream(ConstantProperties.TEMP_DIR + filename + ".$$$"));
      zipOut.setLevel(Deflater.BEST_COMPRESSION);
      String[] wuFileNames = workUnit.containsFileNames();
      String[] workUnitFileNames = new String[wuFileNames.length];
      for (int i = 0; i < workUnitFileNames.length; ++i) {
        workUnitFileNames[i] = wuFileNames[i] + ".$$$";
      }
      for (int k = 0; k < workUnitFileNames.length; ++k) {
        ZipEntry zEntry = zip.getNextEntry();
        if (zEntry == null) {
          throw new IOException(filename + " contains not enough entries!");
        }
        String s = zEntry.getName();
        int i = workUnitFileNames.length;
        while (--i >= 0 && !s.equals(workUnitFileNames[i]));
        if (i < 0) {
          throw new IOException(filename + " contains the wrong entry '" + s + "'!");
        }
        workUnitFileNames[i] = null;
        out = new FileOutputStream(ConstantProperties.TEMP_DIR + s);
        StreamUtils.writeData(zip, out, false, true);
        out = null;
        String s2 = s;
        if (s.endsWith(".$$$")) {
          s2 = s.substring(0, s.length()-4);
          File file = new File(ConstantProperties.TEMP_DIR + s);
          if (file.length() == 0) {
            System.err.println(file.getName() + ": Empty!");
            if (!file.renameTo(new File(ConstantProperties.TEMP_DIR + s2))) {
              System.err.println("File cannot be renamed: " + file.getPath());
            }
          } else {
            Decrypter decrypter = new Decrypter(KeyManager.getEncryptorKey(null));    // ToDo: configurable
            try {
              decrypter.decrypt(serverTask.getDecryptionNumber(), ConstantProperties.TEMP_DIR + s, ConstantProperties.TEMP_DIR + s2);
            } catch (RuntimeException e) {
              file = new File(ConstantProperties.TEMP_DIR + s2);
              if (!file.delete()) {
                System.err.println("File cannot be deleted: " + file.getPath());
              }
              file = new File(ConstantProperties.TEMP_DIR + s);
              throw e;
            } finally {
              if (!file.delete()) {
                System.err.println("File cannot be deleted: " + file.getPath());
              }
            }
          }
        }
        File file = new File(ConstantProperties.TEMP_DIR + s2);
        if (!file.exists()) {
          System.err.println("File does not exist: " + s2);
          if (connection != null) {
            Statement stmt = null;
            try {
              stmt = connection.createStatement();
              ResultSet rs = stmt.executeQuery("SELECT work_unit_id FROM zeta.recomputation WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
              if (rs.next()) {
                rs.close();
                stmt.executeUpdate("UPDATE zeta.recomputation SET (count,start,stop,result)=(count+1,CURRENT TIMESTAMP,NULL,NULL) WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
              } else {
                rs.close();
                int serverId = 1;
                String parameters = null;
                rs = stmt.executeQuery("SELECT server_id,parameters FROM zeta.computation WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
                if (rs.next()) {
                  serverId = rs.getInt(1);
                  parameters = rs.getString(2);
                } else {
                  System.err.println("FATAL Error: server_id");
                  System.exit(1);
                }
                rs.close();
                DatabaseUtils.executeAndLogUpdate(serverId, stmt,
                                                  "INSERT INTO zeta.recomputation (task_id,work_unit_id,size,server_id,parameters) VALUES ("
                                                  + workUnit.getTaskId() + ',' + workUnit.getWorkUnitId() + ',' + workUnit.getSize() + ',' + serverId
                                                  + ((parameters == null)? ",NULL)" : ",'" + parameters + "')"));
                stmt.executeUpdate("UPDATE zeta.result SET result=NULL where task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnit.getWorkUnitId());
              }
            } catch (SQLException se) {
              ThrowableHandler.handle(se);
              System.exit(1);
            } finally {
              DatabaseUtils.close(stmt);
            }
          }
          throw new FileNotFoundException(s2);
        }
        try {
          WorkUnitVerifier verifier = serverTask.getWorkUnitVerifier();
          if (verifier != null) {
            if (!verifier.verify(workUnit, ConstantProperties.TEMP_DIR + s2, connection)) {
              throw new RuntimeException("Check consistency failed!");
            }
          }
          zipOut.putNextEntry(new ZipEntry(s2));
          in = new FileInputStream(ConstantProperties.TEMP_DIR + s2);
          StreamUtils.writeData(in, zipOut, true, false);
          zipOut.flush();
          in = null;
        } finally {
          file = new File(ConstantProperties.TEMP_DIR + s2);
          file.delete();
        }
      }
      zipOut.close();
      zipOut = null;
      zip.close();
      zip = null;
      File file1 = new File(filename);
      File file2 = new File(ConstantProperties.TEMP_DIR + filename + ".$$$");
      if (file2.exists() && file1.delete()) file2.renameTo(file1);
    } finally {
      StreamUtils.close(in);
      StreamUtils.close(out);
      StreamUtils.close(zip);
      StreamUtils.close(zipOut);
    }
  }

  private static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss ");
  private static TaskManager taskManager = null;
}
