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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import zeta.server.Server;
import zeta.server.util.DatabaseUtils;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

import BlowfishJ.BlowfishECB;

/**
 *  @version 2.0, August 6, 2005
**/
public class ServerSynchronization {
  public static void main(String[] args) {
    if (args.length == 0) {
      Integer serverId = null;
      Connection connection = null;
      try {
        ServerSynchronization sync = new ServerSynchronization(null);
        connection = Database.getConnection();
        serverId = new Integer(Server.getInstance(connection).getId());
        DatabaseUtils.close(connection);
        connection = null;
        sync.synchronization(serverId);
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      } finally {
        DatabaseUtils.close(connection);
      }
    } else if (args.length == 1) {
      if (args[0].equals("?")) {
        System.out.println("USAGE: java zeta.tool.ServerSynchronization d <filename>\n"
                         + "       java zeta.tool.ServerSynchronization u <server_id> <filename>\n"
                         + "       java zeta.tool.ServerSynchronization s <sql statement> <user> <password>\n"
                         + "       java zeta.tool.ServerSynchronization k <user> <password>");
        return;
      }
      generateKey(Integer.parseInt(args[0]));
    } else if (args.length == 2 && args[0].length() == 1 && args[0].charAt(0) == 'd') {
      decrypt(args[1]);
    } else if (args.length == 3 && args[0].length() == 1) {
      if (args[0].charAt(0) == 'u') {
        try {
          ServerSynchronization sync = new ServerSynchronization(null);
          sync.syncData(new Integer(args[1]), new FileInputStream(args[2]));
        } catch (IOException ioe) {
        }
      } else if (args[0].charAt(0) == 'k') {
        updateKeys(args[1], args[2]);
      }
    } else if (args.length == 4 && args[0].length() == 1 && args[0].charAt(0) == 's') {
      executeSql(args[1], args[2], args[3]);
    }
  }

  public ServerSynchronization(Connection connection) {
    Statement stmt = null;
    Connection con = connection;
    try {
      if (connection == null) {
        connection = Database.getConnection();
      }
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT server_id,web_hostname,web_port,proxy_host,proxy_port,synchronization_url,post_YN,get_YN FROM zeta.server ORDER BY server_id");
      while (rs.next()) {
        Object[] obj = { new Integer(rs.getInt(1)), rs.getString(2), new Integer(rs.getInt(3)), rs.getString(4), new Integer(rs.getInt(5)), rs.getString(6),
                         new Boolean(rs.getString(7).equals("Y")), new Boolean(rs.getString(8).equals("Y")) };
        servers.add(obj);
      }
      rs.close();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      if (con == null) {
        DatabaseUtils.close(connection);
      }
    }
  }

  public int size() {
    Statement stmt = null;
    Connection con = null;
    try {
      con = Database.getConnection();
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM zeta.server_synchronization");
      int sz = (rs.next())? rs.getInt(1) : 0;
      rs.close();
      return sz;
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
    }
    return 0;
  }

  public void synchronization(Integer masterServerId) {
    Properties props = System.getProperties();
    props.put("sun.net.inetaddr.ttl", "0");
    props.put("networkaddress.cache.ttl", "0");
    props.put("networkaddress.cache.negative.ttl", "0");
    String[] proxies = null;
    final int l = servers.size();
    if (masterServerId != null) {
      for (int i = 0; i < l; ++i) {
        Object[] obj = (Object[])servers.get(i);
        if (masterServerId.equals(obj[0])) {
          if (obj[3] != null && ((String)obj[3]).length() > 0) {
            StringTokenizer st = new StringTokenizer((String)obj[3], ",");
            proxies = new String[st.countTokens()];
            for (int j = 0; j < proxies.length; ++j) {
              proxies[j] = st.nextToken();
            }
            props.put("http.proxyPort", ((Integer)obj[4]).toString());
          }
          break;
        }
      }
    }
    boolean error = true;
    for (int proxyIdx = 0; error; ++proxyIdx) {
      error = false;
      if (proxies != null) {
        props.put("http.proxyHost", proxies[proxyIdx]);
        System.out.print("proxy " + proxies[proxyIdx] + ": ");
        System.out.flush();
      }
      BufferedWriter writer = null;
      try {
        for (int i = 0; i < l; ++i) {
          try {
            Object[] obj = (Object[])servers.get(i);
            if (!((Boolean)obj[7]).booleanValue()) {
              continue;
            }
            Integer serverId = (Integer)obj[0];
            String synchronizationURL = (String)obj[5];
            URL url = new URL("http", (String)obj[1], ((Integer)obj[2]).intValue(), synchronizationURL);
            HttpURLConnection connectionGet = (HttpURLConnection)url.openConnection();
            System.out.println("synchronize " + obj[1] + ':' + obj[2] + synchronizationURL);
            try {
              connectionGet.setRequestMethod("GET");
              connectionGet.setDoInput(true);
              connectionGet.connect();
              if (connectionGet.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Timestamp[] times = syncData(serverId, connectionGet.getInputStream());
                if (times == null) {
                  continue;
                }
                connectionGet.disconnect();
                obj = (Object[])servers.get(i);
                url = new URL("http", (String)obj[1], ((Integer)obj[2]).intValue(), synchronizationURL + "?successful=" + URLEncoder.encode(times[1].toString())
                                                                                    + "&timestamp=" + URLEncoder.encode(times[0].toString()));
                connectionGet = (HttpURLConnection)url.openConnection();
                connectionGet.setRequestMethod("GET");
                connectionGet.connect();
                if (connectionGet.getResponseCode() != HttpURLConnection.HTTP_OK) {
                  throw new IOException("An error occur at server " + serverId);
                }
              }
            } finally {
              connectionGet.disconnect();
            }
          } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
          } catch (IOException e) {
            System.out.println(e.getMessage());
            error = true;
          } catch (Exception e) {
            ThrowableHandler.handle(e);
          }
        }
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      } finally {
        StreamUtils.close(writer);
      }
      if (!error || proxies == null || proxyIdx+1 >= proxies.length) {
        break;
      }
    }
  }

  static void decrypt(String filename) {
    if (filename.startsWith("synchronization_")) {
      Connection con = null;
      Statement stmt = null;
      try {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
        int statementsLength = 0;
        ByteArrayOutputStream data = null;
        ByteArrayOutputStream tmp = new ByteArrayOutputStream(100);
        try {
          for (int k = 0; k < 4; ++k) {
            ZipEntry zEntry = zip.getNextEntry();
            String name = zEntry.getName();
            if (name.equals("statementsLength")) {
              tmp.reset();
              StreamUtils.writeData(zip, tmp, false, true);
              statementsLength = Integer.parseInt(new String(tmp.toByteArray(), "UTF-8"));
            } else if (name.equals("timestamp")) {
            } else if (name.equals("lastSynchronization")) {
            } else if (name.equals("synchronization.sql")) {
              data = new ByteArrayOutputStream(100000);
              StreamUtils.writeData(zip, data, false, true);
            } else {
              throw new IOException("Wrong format!");
            }
          }
        } catch (NullPointerException npe) {
          throw new IOException("Wrong format!");
        }
        zip.close();

        String serverId = filename.substring(16, filename.indexOf('_', 16));

        con = Database.getConnection();
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT key FROM zeta.server WHERE server_id=" + serverId);
        byte[] serverKey = (rs.next())? rs.getBytes(1) : null;
        rs.close();
        if (serverKey == null) {
          throw new SQLException("Missing key for server " + serverId);
        }
        BlowfishECB bfecb = new BlowfishECB(serverKey);
        byte[] statements = data.toByteArray();
        bfecb.decrypt(statements);
        bfecb.cleanUp();
        zip = new ZipInputStream(new ByteArrayInputStream(statements, 0, statementsLength));
        ZipEntry zEntry = zip.getNextEntry();
        if (zEntry == null) {
          throw new IOException("An error occur in the synchronization data!");
        }
        FileOutputStream out = new FileOutputStream(filename.substring(0, filename.length()-4) + ".sql");
        StreamUtils.writeData(zip, out, true, true);
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(con);
      }
    }
  }

  private Timestamp[] syncData(Integer serverId, InputStream in) throws IOException {
    final int l = servers.size();
    int i = 0;
    while (i < l && !((Object[])servers.get(i))[0].equals(serverId)) {
      ++i;
    }
    if (i == l) {
      return null;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream(100000);
    StreamUtils.writeData(in, out, false, true);
    if (out.size() == 0) {
      return null;
    }
    ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
    int statementsLength = 0;
    Timestamp timestamp = null;
    Timestamp lastSynchronization = null;
    ByteArrayOutputStream data = null;
    ByteArrayOutputStream tmp = new ByteArrayOutputStream(100);
    try {
      for (int k = 0; k < 4; ++k) {
        ZipEntry zEntry = zip.getNextEntry();
        String name = zEntry.getName();
        if (name.equals("statementsLength")) {
          tmp.reset();
          StreamUtils.writeData(zip, tmp, false, true);
          statementsLength = Integer.parseInt(new String(tmp.toByteArray(), "UTF-8"));
        } else if (name.equals("timestamp")) {
          tmp.reset();
          StreamUtils.writeData(zip, tmp, false, true);
          timestamp = Timestamp.valueOf(new String(tmp.toByteArray(), "UTF-8"));
        } else if (name.equals("lastSynchronization")) {
          lastSynchronization = Timestamp.valueOf(new String(tmp.toByteArray(), "UTF-8"));
        } else if (name.equals("synchronization.sql")) {
          data = new ByteArrayOutputStream(100000);
          StreamUtils.writeData(zip, data, false, true);
        } else {
          throw new IOException("Wrong format!");
        }
      }
    } catch (NullPointerException npe) {
      throw new IOException("Wrong format!");
    }
    zip.close();
    byte[] buffer = data.toByteArray();
    boolean error = false;
    for (int j = 0; j < l; ++j) {
      if (j != i) {
        Object[] obj = (Object[])servers.get(j);
        if (!((Boolean)obj[6]).booleanValue()) {
          continue;
        }
        String synchronizationURL2 = (String)obj[5];
        System.out.println("> send data to " + obj[1] + ':' + obj[2] + synchronizationURL2 + "  (size=" + buffer.length + ')');
        String paramString = "server_id=" + serverId + "&statements_length=" + statementsLength;
        URL url = new URL("http", (String)obj[1], ((Integer)obj[2]).intValue(), synchronizationURL2);
        HttpURLConnection connectionPost = (HttpURLConnection)url.openConnection();
        OutputStream outBuffer = null;
        try {
          connectionPost.setUseCaches(false);
          connectionPost.setRequestProperty("Content-Length", Long.toString(buffer.length));
          connectionPost.setRequestProperty("Content-Type", "application/octet-stream");
          connectionPost.setRequestProperty("Param-String",  paramString); // not a standard property !!
          connectionPost.setDoOutput(true);
          connectionPost.setDoInput(true);
          connectionPost.setRequestMethod("POST");
          connectionPost.connect();
          outBuffer = connectionPost.getOutputStream();
          outBuffer.write(buffer);
          if (connectionPost.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Could not synchronize the servers " + serverId + ',' + obj[0] + ": return code " + connectionPost.getResponseCode());
          }
        } catch (MalformedURLException e) {
          System.out.println(e.getMessage());
          error = true;
          return null;
        } catch (IOException e) {
          System.out.println(e.getMessage());
          error = true;
          return null;
        } finally {
          if (outBuffer != null) {
            outBuffer.flush();
            outBuffer.close();
          }
          if (connectionPost != null) {
            connectionPost.disconnect();
          }
        }

      }
    }
    if (error) {
      FileOutputStream file = new FileOutputStream("synchronization_" + serverId + '_' + (new Date(System.currentTimeMillis())) + '_' + System.currentTimeMillis() + ".zip");
      file.write(out.toByteArray());
      file.close();
    }
    final Timestamp[] t = { timestamp, lastSynchronization };
    return t;
  }

  private static void generateKey(int serverId) {
    FileOutputStream file = null;
    try {
      byte[] key = new byte[BlowfishECB.MAXKEYLENGTH-1];
      SecureRandom rand = new SecureRandom();
      rand.setSeed(System.currentTimeMillis());
      for (int i = 0; i < key.length; ++i) {
        key[i] = (byte) (rand.nextInt() & 0x0ff);
      }
      file = new FileOutputStream("server_" + serverId + ".key");
      file.write(key);
    } catch (IOException e) {
      ThrowableHandler.handle(e);
    } finally {
      StreamUtils.close(file);
    }
  }

  private static void updateKeys(String user, String password) {
    Connection connection = null;
    PreparedStatement stmt = null;
    try {
      connection = Database.getConnection(user, password);
      File file = new File(".");
      File[] list = file.listFiles();
      boolean found = false;
      if (list != null && list.length > 0) {
        for (int i = 0; i < list.length; ++i) {
          String s = list[i].getName();
          if (s.startsWith("server_") && s.endsWith(".key")) {
            String sql = "UPDATE zeta.server SET key=? WHERE server_id=" + Integer.parseInt(s.substring(7, s.length()-4));
            System.out.println(sql);
            stmt = connection.prepareStatement(sql);
            stmt.setBytes(1, StreamUtils.getFile(s, false, false));
            int result = stmt.executeUpdate();
            stmt.close();
            if (result != 1) {
              System.out.println("ERROR!");
            }
          }
        }
      }
    } catch (IOException e) {
      ThrowableHandler.handle(e);
    } catch (SQLException e) {
      ThrowableHandler.handle(e);
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(connection);
    }
  }

  private static void executeSql(String sql, String user, String password) {
    Connection connection = null;
    Statement stmt = null;
    try {
      connection = Database.getConnection(user, password);
      int serverId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      DatabaseUtils.executeAndLogUpdate(serverId, stmt, sql);
    } catch (IOException e) {
      ThrowableHandler.handle(e);
    } catch (SQLException e) {
      ThrowableHandler.handle(e);
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(connection);
    }
  }

  private List servers = new ArrayList(10);
}
