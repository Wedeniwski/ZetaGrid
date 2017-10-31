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

  Version 1.9.3, May 29, 2004

  This program is based on the work of:
     S. Wedeniwski
--*/

package zeta.tool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import zeta.server.tool.Daemon;
import zeta.server.tool.Database;
import zeta.server.tool.FTP;
import zeta.server.util.DatabaseUtils;
import zeta.util.ProcessUtils;
import zeta.util.Properties;
import zeta.util.StreamUtils;
import zeta.util.StringUtils;
import zeta.util.ThrowableHandler;


public class KeepAlive {
  public static void main(String[] args) {
    try {
      if (args.length == 1) {
        if (args[0].equals("r")) {
          Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
          new KeepAlive(null).checkPage("POST", properties.get("reload.host", ""), properties.get("reload.url", ""), false, null);
        } else if (args[0].equals("?")) {
          System.out.println("USAGE: [r] [y/n]");
        } else {
          new KeepAlive(args[0]);
        }
      } else {
        new KeepAlive(null);
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  public KeepAlive(String arg0) throws IOException {
    Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
    if (properties.get("keep_alive.database.restart.sleep") != null) {
      keepAliveDatabase = new KeepAliveDatabase();
      keepAliveDatabase.start();
    }
    if (properties.get("keep_alive.connection") != null) {
      keepAliveConnection = new KeepAliveConnection(arg0);
      keepAliveConnection.start();
    }
    if (properties.get("keep_alive.tomcat") != null) {
      new KeepAliveTomcat().start();
    }
  }

  static String getPPPClientAddress() {
    BufferedReader reader = null;
    try {
      Process process = Runtime.getRuntime().exec("ipconfig");
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      boolean found = false;
      while (true) {
        String s = reader.readLine();
        if (s == null) {
          break;
        }
        if (s.startsWith("PPP")) {
          found = true;
        }
        if (found && s.indexOf("ateway") >= 0) {
          int idx = s.indexOf(':');
          if (idx >= 0 && idx < s.length()-2) {
            return s.substring(idx+2);
          }
        }
      }
      process.waitFor();
    } catch (InterruptedException ie) {
    } catch (IOException ioe) {
    } finally {
      StreamUtils.close(reader);
    }
    return null;
  }

  private void ping() {
    BufferedReader reader = null;
    try {
      if (++pingIdx >= pingAddresses.length) {
        pingIdx = 0;
      }
      Process process = Runtime.getRuntime().exec(new String[] { "ping", pingAddresses[pingIdx] });
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while (reader.readLine() != null) {
      }
      process.waitFor();
    } catch (InterruptedException ie) {
    } catch (IOException ioe) {
    } finally {
      StreamUtils.close(reader);
    }
  }

  private static boolean checkPage(String requestMethod, String host, String page, boolean verbose, PrintStream printInput) {
    if (host != null && page != null && host.length() > 0) {
      if (verbose) {
        trace("check " + host, true, true);
      }
      HttpURLConnection connection = null;
      try {
        URL url = new URL("http", host, 80, page);
        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.setUseCaches(false);
        connection.connect();
        if (printInput != null) {
          if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StreamUtils.writeData(connection.getInputStream(), printInput, false, false);
            return true;
          } else {
            return false;
          }
        }
        return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
      } catch (UnknownHostException uhe) {
        ThrowableHandler.handle(uhe);
      } catch (MalformedURLException e) {
        ThrowableHandler.handle(e);
      } catch (IOException e) {
        ThrowableHandler.handle(e);
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }
    }
    return false;
  }

  private void sendURL(String address) throws IOException {
    trace("send URL: " + address);
    Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
    String tmpDir = properties.get("tmp_dir", "tmp");
    while (true) {
      new File(tmpDir + "/database.connection.url").delete();
      Writer writer = null;
      BufferedReader reader = null;
      FTP ftp = null;
      try {
        String filename = properties.get("database.connection.url", ".") + "/database.connection.url";
        writer = new FileWriter(filename);
        writer.write("jdbc:db2://" + address + ":8082/zeta");
        writer.close();
        writer = null;

  			ftp = new FTP();
  			ftp.connect(properties.get("ftp.host", ""));
  			ftp.login(properties.get("ftp.user", ""), properties.get("ftp.password", ""));
        ftp.cd(properties.get("ftp.config.path", "."));
        ftp.put(filename, "database.connection.url", FTP.MODE_BINARY);
        ftp.get("database.connection.url", tmpDir + "/database.connection.url", FTP.MODE_BINARY);

        /*Process process = Runtime.getRuntime().exec(new String[] { "ftp", properties.get("ftp", "-s:ftp_update_url") });
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (reader.readLine() != null);
        process.waitFor();*/
        // Check:
        boolean ok = false;
        reader = new BufferedReader(new FileReader(tmpDir + "/database.connection.url"));
        while (!ok) {
          String s = reader.readLine();
          if (s == null) {
            break;
          }
          ok = (s.indexOf(address) >= 0);
        }
        reader.close();
        reader = null;
        if (ok) {
          //checkPage("GET", properties.get("zetagrid.forum.host", "www.zetagrid.net"), properties.get("zetagrid.forum.url", "/forum/admin/restart.jsp"), false, System.out);
          String reloadHost = properties.get("reload.host");
          String reloadURL = properties.get("reload.url", "");
          if (reloadHost != null && reloadURL != null) {
            checkPage("POST", reloadHost, reloadURL, false, System.out);
          }
          return;
        } else {
          System.err.println("Error: new address not in file database.connection.url");
        }
      } catch (IOException ioe) {
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      } finally {
        StreamUtils.close(reader);
        StreamUtils.close(writer);
  	    if (ftp != null) {
  	      ftp.disconnect();
  	    }
      }
      trace(" error to send URL");
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {
      }
    }
  }

  private static void trace(String text) {
    trace(text, true, true);
  }

  private static void trace(String text, boolean timestamp, boolean newLine) {
    if (timestamp) {
      if (newLine) {
        System.out.println(formatter.format(new Date()) + text);
      } else {
        System.out.print(formatter.format(new Date()) + text);
        System.out.flush();
      }
    } else {
      if (newLine) {
        System.out.println(text);
      } else {
        System.out.print(text);
        System.out.flush();
      }
    }
  }

  private InetAddress address = null;
  private final static String[] pingAddresses = { "www.zetagrid.net", "www.ibm.com", "www.hipilib.de", "www.google.com", "www.yahoo.com" };
  private int pingIdx = 0;
  private ServerSocket serverSocket = null;
  private Throwable serverSocketThrowable = null;
  private String previousHostAddress = "";
  private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss ");


  class KeepAliveConnection extends Thread {
    boolean activeReconnect = false;
    boolean traceTimeConnected = false;
    long lastConnection = System.currentTimeMillis();
    String arg0;
    
    KeepAliveConnection(String arg0) {
      this.arg0 = arg0;
    }

    public void run() {
      boolean firstTime = true;
      trace("Keep alive connection");
      while (true) {
        try {
          final Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
          final long restartSleep = properties.get("keep_alive.restart.sleep", 0);
          if (firstTime && restartSleep > 0) {
            char ch = ' ';
            if (arg0 != null && arg0.length() > 0) {
              ch = arg0.charAt(0);
            } else {
              System.out.println("Do you want to reconnect now? [Y/N]");
              ch = (char)System.in.read();
            }
            if (ch == 'Y' || ch == 'y') {
              lastConnection = 0;
            }
          }
          long time = System.currentTimeMillis()-lastConnection;
          if (restartSleep > 0 && time >= restartSleep) {
            trace("address will be changed now");
            if (keepAliveDatabase != null) {
              synchronized (keepAliveDatabase) {
                keepAliveDatabase.lastDBReset = keepAliveDatabase.dbRestart("db2 stop since address will be changed", false);
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
                Thread t = new Thread() {
                  public void run() {
                    String command = properties.get("keep_alive.disconnect.cmd");
                    try {
                      for (int idx = 1; command != null; ++idx) {
                        trace("command: " + command);
                        ProcessUtils.exec(command, null, false, 5000);
                        trace("executed");
                        try {
                          Thread.sleep(3000);
                        } catch (InterruptedException ie) {
                        }
                        command = properties.get("keep_alive.connection.cmd." + idx);
                      }
                    } finally {
                      activeReconnect = false;
                    }
                  }
                };
                activeReconnect = true;
                t.start();
                for (int wait = 0; wait < 15 && activeReconnect || wait < 30 && getPPPClientAddress() == null; ++wait) {
                  try {
                    Thread.sleep(500);
                  } catch (InterruptedException ie) {
                  }
                }
              }
            }
          } else if (restartSleep > 0 && time+3600000 >= restartSleep) {
            trace("address will be changed in about " + ((restartSleep-time)/60000) + " minutes");
          }
          int wait = 0;
          long connectionTime = properties.get("keep_alive.reboot.wait", 0);
          if (connectionTime > 0) {
            connectionTime += System.currentTimeMillis();
          }
          while (getPPPClientAddress() == null) {
            if (++wait == 10) {
              trace(".", false, false);
              if (System.currentTimeMillis() > connectionTime) {
                String cmd = properties.get("keep_alive.reboot.cmd", null);
                if (cmd != null) {
                  ProcessUtils.exec(cmd);
                  // no further command can be executed
                  System.exit(1);
                  // Problem: another program can stop the reboot, e.g. an unsaved file in an editor
                }
              }
              if (!activeReconnect && properties.get("keep_alive.connection.cmd.1") != null) {
                activeReconnect = true;
                for (int idx = 1;; ++idx) {
                  final String command = properties.get("keep_alive.connection.cmd." + idx);
                  if (command == null) {
                    break;
                  } else {
                    Thread t = new Thread() {
                      public void run() {
                        try {
                          ProcessUtils.exec(command, null, false, 5000);
                        } finally {
                          activeReconnect = false;
                        }
                      }
                    };
                    t.start();
                  }
                }
              }
              for (int i = 0; i < 10 && activeReconnect; ++i) {
                try {
                  Thread.sleep(500);
                } catch (InterruptedException ie) {
                }
              }
              wait = 0;
            } else {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ie) {
              }
            }
          }
          address = InetAddress.getByName(getPPPClientAddress());
          serverSocket = null;
          serverSocketThrowable = null;
          Thread t = new Thread() {
            public void run() {
              try {
                trace("socket ", true, false);
                serverSocket = new ServerSocket(SOCKET_PORT, 50, address);
                trace(serverSocket.getInetAddress().getHostAddress() + ':' + SOCKET_PORT + ' ', false, false);
                if (!serverSocket.isClosed()) {
                  Socket socket = serverSocket.accept();
                  trace("connected", traceTimeConnected, true);
                  traceTimeConnected = false;
                  for (int i = 0; i < 10; ++i) {
                    try {
                      Thread.sleep(120*1000);
                    } catch (InterruptedException ie) {
                    }
                    ping();
                  }
                  socket.getOutputStream().write(-1);
                }
              } catch (Throwable e) {
                serverSocketThrowable = e;
                ThrowableHandler.handle(e);
              }
            }
          };
          t.start();
          do {
            try {
              Thread.sleep(500);
            } catch (InterruptedException ie) {
            }
          } while (serverSocket == null && serverSocketThrowable == null);
          if (!previousHostAddress.equals(address.getHostAddress())) {
            lastConnection = System.currentTimeMillis();
            String s = address.getHostAddress();
            if (firstTime || previousHostAddress.length() > 0 || !checkPage("GET", properties.get("zetagrid.check.host", "www.zetagrid.net"), properties.get("zetagrid.check.url", "/servlet/service/statistic"), true, null)) {
              int sleep = 60000;
              while (true) {
                sendURL(s);
                if (keepAliveDatabase != null) {
                  synchronized (keepAliveDatabase) {
                    keepAliveDatabase.lastDBReset = keepAliveDatabase.dbRestart("db2 start since address changed", true);
                  }
                }
                try {
                  Thread.sleep(5000);
                } catch (InterruptedException ie) {
                }
                if (checkPage("GET", properties.get("zetagrid.check.host", "www.zetagrid.net"), properties.get("zetagrid.check.url", "/servlet/service/statistic"), true, null)) {
                  if (firstTime) {
                    String cmd = properties.get("keep_alive.daemon.cmd", null);
                    if (cmd != null) {
                      trace("exec daemon: " + cmd);
                      Runtime.getRuntime().exec(cmd);
                    }
                    firstTime = false;
                  }
                  traceTimeConnected = true;
                  break;
                } else {
                  try {
                    Thread.sleep(sleep);
                  } catch (InterruptedException ie) {
                  }
                  sleep = (sleep == 60000)? 900000 : 60000;
                }
                address = InetAddress.getByName(getPPPClientAddress());
                s = address.getHostAddress();
              }
            }
            previousHostAddress = s;
          }
          Socket socket = new Socket();
          socket.bind(new InetSocketAddress(0));
          socket.connect(new InetSocketAddress(address, SOCKET_PORT), 20*3600*1000);
          socket.getInputStream().read();
          socket.close();
          serverSocket.close();
          serverSocket = null;
        } catch (IOException ioe) {
          ThrowableHandler.handle(ioe);
        }
        if (serverSocket != null) {
          try {
            serverSocket.close();
            serverSocket = null;
          } catch (IOException ioe) {
            ThrowableHandler.handle(ioe);
          }
        }
        if (getPPPClientAddress() == null) {
          trace("reconnect.");
        }
      }
    }
  }

  class KeepAliveDatabase extends Thread {
    public void run() {
      trace("Keep alive database");
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ie) {
      }
      try {
        Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
        int sleep = properties.get("keep_alive.database.check.sleep", 40000);
        int sleepDBReset = properties.get("keep_alive.database.restart.sleep", 3600000);
        ByteArrayOutputStream out = new ByteArrayOutputStream(20000);
        while (true) {
          try {
            if (keepAliveConnection == null || previousHostAddress.equals(InetAddress.getByName(getPPPClientAddress()).getHostAddress())) {
              synchronized (keepAliveDatabase) {
                if (con == null) {
                  con = Database.getConnection();
                  stmt = con.createStatement();
                }
                stmt.executeQuery("SELECT id FROM zeta.task WHERE id=1").close();
                /*out.reset();
                if (checkPage("GET", properties.get("keep_alive.database.check.host", "www.zetagrid.net"), properties.get("keep_alive.database.check.url", "/servlet/service/statistic"), false, new PrintStream(out))) {
                  out.flush();
                  String s = out.toString();
                  int idx = s.indexOf("Exception");
                  if (idx >= 0) {
                    int startIdx = s.lastIndexOf('\n', idx);
                    int endIdx = s.indexOf('\n', idx);
                    if (startIdx >= 0 && endIdx > startIdx && endIdx >= idx) {
                      trace(s.substring(startIdx, endIdx));
                    }
                    throw new SQLException("Database Exception at Web server");
                  }
                }*/
              }
            }
          } catch (SQLException se) {
            DatabaseUtils.close(stmt);
            stmt = null;
            DatabaseUtils.close(con);
            con = null;
            trace(se.getMessage());
            synchronized (keepAliveDatabase) {
              if (System.currentTimeMillis()-lastDBReset >= 60000) {
                lastDBReset = dbRestart("db2 restart since an exception occurred", true);
              }
            }
          } catch (Exception e) {
            trace(e.getMessage());
            ThrowableHandler.handle(e);
          }
          synchronized (keepAliveDatabase) {
            if (System.currentTimeMillis()-lastDBReset >= sleepDBReset) {
              lastDBReset = dbRestart("db2 restart", true);
            }
          }
          try {
            Thread.sleep(sleep);
          } catch (InterruptedException ie) {
          }
        }
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(con);
        trace("Stop to keep alive database");
      }
    }

    private long dbRestart(String text, boolean start) {
      for (int wait = 0; !Daemon.allowDbRestart() && wait < 36; ++wait) { // wait up to 6 min.
        try {
          Thread.sleep(10*1000);  // 10 sec.
        } catch (InterruptedException ie) {
        }
      }
      boolean killDB = true;
      for (int restart = 0; restart < 3 && killDB; ++restart) {
        killDB = false;
        DatabaseUtils.close(stmt);
        stmt = null;
        DatabaseUtils.close(con);
        con = null;
        try {
          ByteArrayOutputStream out = new ByteArrayOutputStream(5000);
          Thread thread = new Thread() {
            public void run() {
              ProcessUtils.exec("db2stop force");
            }
          };
          dbForceApplicationsAll();
          trace(text);
          thread.start();
          try {
            long startTime = System.currentTimeMillis();
            while (true) {
              out.reset();
              if (ProcessUtils.exec("pslist", out, true) == 0) {
                String s = out.toString();
                int idx = s.indexOf("db2syscs ");
                if (idx < 0 && s.indexOf("db2stop ") < 0) {
                  break;
                }
                int idx2 = s.indexOf(':', idx); // check Mem
                int l = s.length();
                while (--idx2 > idx && s.charAt(idx2) != ' ');
                while (--idx2 > idx && !Character.isDigit(s.charAt(idx2)));
                for (idx = idx2-1; idx > 0 && Character.isDigit(s.charAt(idx)); --idx);
                long mem = Long.parseLong(s.substring(idx+1, idx2+1));
                trace("db2syscs mem: " + mem);
                long timeDiff = System.currentTimeMillis()-startTime;
                if (mem < 90000 && timeDiff >= 90000 || timeDiff >= 300000) {
                  break;
                }
              } else {
                trace("Error: result of pslist is not valid");
                System.err.println(out.toString());
              }
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ie) {
              }
            }
            out.reset();
            if (ProcessUtils.exec("pslist", out, true) == 0) {
              String s = out.toString();
              int idx = s.indexOf("db2syscs ");    // kill DB2 since db2stop hangs
              if (idx >= 0) {
                idx += 8;
                int l = s.length();
                while (idx < l && !Character.isDigit(s.charAt(idx))) {
                  ++idx;
                }
                int idx2 = idx+1;
                while (idx2 < l && Character.isDigit(s.charAt(idx2))) {
                  ++idx2;
                }
                int pid = Integer.parseInt(s.substring(idx, idx2));
                trace("db2syscs: " + pid);
                out.reset();
                ProcessUtils.exec("pskill " + pid, out, true);
                s = out.toString();
                System.out.println(s);
                killDB = (StringUtils.indexOfIgnoreCase(s, "killed", 0) >= 0);
              }
            }
          } catch (Exception e) {
            ThrowableHandler.handle(e);
          }
          out.reset();
          if (ProcessUtils.exec("pslist", out, true) == 0) {
            try {
              String s = out.toString();
              for (int idx = s.indexOf("db2fmp "); idx >= 0; idx = s.indexOf("db2fmp ", idx+1)) {
                idx += 6;
                int l = s.length();
                while (idx < l && !Character.isDigit(s.charAt(idx))) {
                  ++idx;
                }
                int idx2 = idx+1;
                while (idx2 < l && Character.isDigit(s.charAt(idx2))) {
                  ++idx2;
                }
                int pid = Integer.parseInt(s.substring(idx, idx2));
                trace("db2fmp: " + pid);
                ProcessUtils.exec("pskill " + pid, System.out, true);
              }
              if (killDB) {
                ProcessUtils.exec("net stop \"DB2 Remote Command Server\"");
                for (int idx = s.indexOf("db2rcmd "); idx >= 0; idx = s.indexOf("db2rcmd ", idx+1)) {
                  idx += 7;
                  int l = s.length();
                  while (idx < l && !Character.isDigit(s.charAt(idx))) {
                    ++idx;
                  }
                  int idx2 = idx+1;
                  while (idx2 < l && Character.isDigit(s.charAt(idx2))) {
                    ++idx2;
                  }
                  int pid = Integer.parseInt(s.substring(idx, idx2));
                  trace("db2rcmd: " + pid);
                  ProcessUtils.exec("pskill " + pid, System.out, true);
                }
              }
            } catch (Exception e) {
              ThrowableHandler.handle(e);
            }
          }
          try {
            ProcessUtils.exec("db2stop");
          } catch (Exception e) {
          }
        } finally {
          if (start) {
            ProcessUtils.exec("net start \"DB2 Remote Command Server\"");
            ProcessUtils.exec("db2start");
          }
        }
        try {
          Thread.sleep(restart*10000);
        } catch (InterruptedException ie) {
        }
      }
      return System.currentTimeMillis();
    }

    private long dbForceApplicationsAll() {
      try {
        trace("db2 force application all");
        ProcessUtils.exec("db2start");
        DatabaseUtils.db2CLP("FORCE APPLICATION ALL;\n", null, 240);
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      }
      return System.currentTimeMillis();
    }

    private long lastDBReset = 0;
    private Connection con = null;
    private Statement stmt = null;
  }

  class KeepAliveTomcat extends Thread {
    public void run() {
      trace("Keep alive Tomcat");
      try {
        String lastOut = "";
        while (true) {
          ByteArrayOutputStream out = new ByteArrayOutputStream(500);
          ProcessUtils.exec("net start tomcat", out, false);
          StreamUtils.close(out);
          String s = new String(out.toByteArray());
          if (!s.equals(lastOut)) {
            lastOut = s;
            trace(s, false, true);
            trace(" tomcat restart");
          }
          try {
            Thread.sleep(20000);
          } catch (InterruptedException ie) {
          }
        }
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      }
    }
  }

  private KeepAliveDatabase keepAliveDatabase = null;
  private KeepAliveConnection keepAliveConnection = null;
  private final static int SOCKET_PORT = 20000;
}
