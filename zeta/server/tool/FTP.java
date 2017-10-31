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

import java.util.StringTokenizer;
import java.util.List;
import java.util.LinkedList;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 *  Implementation of the File Transfer Protocol.
 *  Only the passive mode is implemented for connections,
 *  i.e. forces the client to open the data connection to
 *  the FTP server which is useful to getting past Firewalls.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class FTP {
  /**
   *  Use ASCII mode (attempt to handle new-line's in a sane manner)
  **/
  public static final boolean MODE_ASCII = true;

  /**
   *  Use binary mode - no translation is performed.
  **/
  public static final boolean MODE_BINARY = false;

  /**
   *  Connect to the specified host using standard FTP port (21).
   *  The default network socket timeout is 50 milliseconds.
   *  @param host host name of the FTP server
   *  @exception  IOException  if an I/O error occurs.
  **/
  public void connect(String host) throws IOException {
    connect(host, 21);
  }

  /**
   *  Connect to the specified host using standard FTP port (21)
   *  The default network socket timeout is 50 milliseconds.
   *  @param host host name of the FTP server
   *  @param port port number of the FTP server
   *  @exception  IOException  if an I/O error occurs.
  **/
  public void connect(String host, int port) throws IOException {
    this.host = host;
    try {
      controlSocket = new Socket(host, port);
      controlSocket.setSoTimeout(socketTimeout);
      controlSocket.getLocalAddress();
      controlInputStream = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
      controlOutputStream = new PrintStream(controlSocket.getOutputStream());
      if (debug != null) {
        debug.println("FTP connection to " + host + " opened");
      }
      checkResponseCode("Connect");
    } catch (SocketException se) {
      if (debug != null) {
        se.printStackTrace(debug);
      }
      throw new SocketException("Socket error trying to connect: " + se.getMessage());
    } catch (IOException ioe) {
      if (debug != null) {
        ioe.printStackTrace(debug);
      }
      throw new IOException("IO error trying to connect: " + ioe.getMessage());
    }
  }

  /**
   *  Authenticate with the FTP server which is required for most operations.
   *  An authentication failure will result in an I/O exception.
  **/
  public void login(String user, String password) throws IOException {
    checkConnection("login");
    executeCommand("USER", user);
    executeCommand("PASS", password);
    authenticated = true;
  }

  /**
   *  Attempt to gracefully abort active transfer.
   *  Note that servers implement this differently -
   *  some may leave semi-uploaded files in place,
   *  others may delete them.
   *  An abort while dowloading will result in the
   *  file being truncated.
  **/
  public void abort() throws IOException {
    abort = true;
    executeCommand("ABOR", null);
  }

  /**
   *  Closes the control connection to the FTP server.
   *  Synonymous to the method disconnect().
  **/
  public void close() {
    disconnect();
  }

  /**
   *  Closes the control connection to the FTP server.
  **/
  public void disconnect() {
    try {
      executeCommand("QUIT", null);
    } catch (Exception e) {
    }
    try {
      controlOutputStream.close();
      controlInputStream.close();
      controlSocket.close();
    } catch (Exception e) {
    }
  }

  /**
   *  Switches the transfer mode.
   *  Valid modes are <code>MODE_ASCII</code> and <code>MODE_BINARY</code>.
   *  ASCII mode attempts to convert newlines to a format understandable by the target system.
   *  This works for text files, but corrupts data files.
   *  Binary mode ensures that the file is transferred "as-is."
   *  For transfers between like-systems this is the preferred mode, as it is much faster.
  **/
  public void setMode(boolean mode) throws IOException {
    executeCommand("TYPE", (mode == MODE_ASCII)? "A" : "I");
  }

  /**
   *  Downloads the specified remote file from FTP server to the local file using the specified mode.
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
   *  The remote file must be in the current directory, or the full path must be given.
  **/
  public void get(String remoteFile, String localFile, boolean mode) throws IOException {
    get(null, remoteFile, localFile, mode);
  }

  /**
   *  Downloads the specified remote file residing in the remote directory
   *  from FTP server to the local file using the specified mode.
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
  **/
  public void get(String remoteDir, String remoteFile, String localFile, boolean mode) throws IOException {
    long startAt = 0;
    if (allowRestart) {
      File file = new File(localFile);
      if (file.exists()) {
        startAt = file.length();
      }
    }
    OutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(localFile));
      get(remoteDir, remoteFile, out, mode, startAt);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException ioe) {
        }
      }
    }
  }

  /**
   *  Downloads the specified remote file from FTP server in the output stream using the specified mode.
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
   *  The remote file must be in the current directory, or the full path must be given.
  **/
  public void get(String remoteFile, OutputStream output, boolean mode) throws IOException {
    get(remoteFile, output, mode, 0);
  }

  /**
   *  Downloads the specified remote file from FTP server in the output stream using the specified mode,
   *  starting at the specified position (byte).
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
   *  The remote file must be in the current directory, or the full path must be given.
  **/
  public void get(String remoteFile, OutputStream output, boolean mode, long startAt) throws IOException {
    get(null, remoteFile, output, mode, startAt);
  }

  /**
   *  Downloads the specified remote file residing in the remote directory
   *  from FTP server in the output stream using the specified mode,
   *  starting at the specified position (byte).
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
   *  The remote file must be in the current directory, or the full path must be given.
  **/
  public void get(String remoteDir, String remoteFile, OutputStream output, boolean mode, long startAt) throws IOException {
    if (remoteDir != null && remoteDir.length() > 0) {
      cd(remoteDir);
    }
    setMode(mode);
    executeCommand("REST", String.valueOf(startAt));
    Socket socket = null;
    DataInputStream in = null;
    try {
      socket = getDataSocket("RETR " + remoteFile);
      in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
      PrintStream ps = new PrintStream(new BufferedOutputStream(output));
      if (mode == MODE_ASCII) { // ASCII mode
        String sep = System.getProperty("line.separator");
        while (!abort) {
          int i = in.read();
          if (i == -1) {
            break;
          }
          if (i == '\r') {
            i = in.read();
            if (i == '\n') { // a normal DOS file
              ps.print(sep);
            } else if (i == '\r') {
              i = in.read();
              if (i == '\n') {
                // this was a DOS text file after bin transfer to Unix host
                // where we are now pulling it from via ASCII (instead of bin)
                // these have a 0d0d0a - go figure...
                // this is technically-speaking out-of-spec for FTP
                ps.print(sep);
              } else {
                ps.print('\r');
                ps.print('\r');
                ps.print((char)i);
              }
            } else {
              ps.print('\r');
              ps.print((char)i);
            }
          } else {
            ps.print((char)i);
          }
          if (transferSleep > 0) {
            try {
              Thread.sleep(transferSleep);
            } catch (InterruptedException ie) {
            }
          }
        }
      } else { // binary mode
        byte data[] = new byte[bufferSize];
        while (!abort) {
          int l = in.read(data);
          if (l == -1) {
            break;
          }
          ps.write(data, 0, l);
          if (transferSleep > 0) {
            try {
              Thread.sleep(transferSleep);
            } catch (InterruptedException ie) {
            }
          }
        }
      }
      ps.flush();
    } finally {
      if (in != null) {
        in.close();
      }
      if (socket != null) {
        socket.close();
      }
    }
    checkResponseCode("RETR2 " + remoteFile);
  }

  /**
   *  Uploads the specified local file to the remote file (in current remote directory
   *  of the FTP server) using the specified mode.
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
   *  The local file should include a fully qualified path.
  **/
  public void put(String localFile, String remoteFile, boolean mode) throws IOException {
    put(localFile, null, remoteFile, mode);
  }

  /**
   *  Uploads the specified local file to the remote file in the remote directory
   *  of the FTP server using the specified mode.
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
   *  The local file should include a fully qualified path.
  **/
  public void put(String localFile, String remoteDir, String remoteFile, boolean mode) throws IOException {
    FileInputStream fin = null;
    try {
      fin = new FileInputStream(localFile);
      put(fin, remoteDir, remoteFile, mode);
    } finally {
      if (fin != null) {
        try {
          fin.close();
        } catch (IOException ioe) {
        }
      }
    }
  }

  /**
   *  Uploads the contents of the specified input stream to the remote file (in current remote directory
   *  of the FTP server) using the specified mode.
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
  **/
  public void put(InputStream in, String remoteFile, boolean mode) throws IOException {
    put(in, null, remoteFile, mode);
  }

  /**
   *  Uploads the contents of the specified input stream to the remote file in the remote directory
   *  of the FTP server using the specified mode.
   *  The mode can be <code>MODE_ASCII</code> or <code>MODE_BINARY</code>.
  **/
  public void put(InputStream in, String remoteDir, String remoteFile, boolean mode) throws IOException {
    if (remoteDir != null && remoteDir.length() > 0) {
      cd(remoteDir);
    }
    setMode(mode);
    Socket socket = null;
    DataInputStream dataIn = null;
    try {
      socket = getDataSocket("STOR " + remoteFile);
      dataIn = new DataInputStream(new BufferedInputStream(in));
      PrintStream ps = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
      if (mode == MODE_ASCII) {
        String sep = System.getProperty("line.separator");
        while (!abort) {
          int i = dataIn.read();
          if (i == -1) {
            break;
          } else if (i == '\r') {
            if (sep.equals("\r")) {
              ps.print("\r\n");
            } else if (sep.equals("\n")) {
              ps.print('\r');
            } else if (sep.equals("\r\n")) {
              i = in.read();
              if (i == '\n') {
                ps.print("\r\n");
              } else {
                ps.print('\r');
                ps.print((char)i);
              }
            }
          } else if (i == '\n') {
            ps.print("\r\n");
          } else {
            ps.print((char)i);
          }
        }
      } else {
        byte data[] = new byte[bufferSize];
        while (!abort) {
          int l = dataIn.read(data);
          if (l == -1) {
            break;
          }
          ps.write(data, 0, l);
        }
      }
      ps.flush();
    } finally {
      if (dataIn != null) {
        dataIn.close();
      }
      if (socket != null) {
        socket.close();
      }
    }
    checkResponseCode("STOR2 " + remoteFile);
  }

  /**
   *  Retrieves the file list in the current working directory on the FTP server.
   *  The list contains Strings containing the returned filename.
   *  Note that no distinction is made between files and subdirectories.
   *  Some servers are known to return an error message if the current directory contains no files,
   *  i.e. this in turn causes an exception.
  **/
  public List dir() throws IOException {
    List rc = new LinkedList();
    Socket socket = null;
    BufferedReader in = null;
    try {
      socket = getDataSocket("NLST");
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        rc.add(line);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (socket != null) {
        socket.close();
      }
    }
    checkResponseCode("NLST");
    return rc;
  }

  /**
   *  Retrieves the extended file list in the current working directory on the FTP server.
   *  The format used is highly dependent on the FTP server, but will generally include some
   *  indication of the file name, size, create date, and type (directory/filename).
   *  Use the systemID() call to determine how to parse the output.
   *  Some servers are known to return an error message if the current directory contains no files,
   *  i.e. this in turn causes an exception.
  **/
  public List longDir() throws IOException {
    List rc = new LinkedList();
    Socket socket = null;
    BufferedReader in = null;
    try {
      socket = getDataSocket("LIST");
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        rc.add(line);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (socket != null) {
        socket.close();
      }
    }
    checkResponseCode("LIST");
    return rc;
  }

  /**
   *  Returns the System ID as supplied by the FTP server.
   *  The reply can be used to determine how to
   *  interpret full directory listings for example.
  **/
  public String systemID() throws IOException {
    return executeCommand("SYST", null).getText();
  }
  
  /**
   *  Switches current directory on the FTP server.
   *  If the directory does not exist the FTP server generally reports an error,
   *  which results in an exception.
  **/
  public void cd(String newDir) throws IOException {
    executeCommand("CWD", newDir);
  }

  /**
   *  Creates a directory on the FTP server.
   *  The directory is created under the current working directory, see the method pwd().
   *  You must generally create one directory at a time, i.e. you cannot call makeDir("/tmp/foo/bar")
   *  if the directory "/tmp/foo" does not already exist.
   *  Note that syntax rules of the client machine may differ from the host's - upper/lowercase
   *  conventions, allowable characters, etc.
  **/
  public void makeDir(String newDir) throws IOException {
    executeCommand("MKD", newDir);
  }

  /**
   *  Returns the current working directory as seen by the FTP server.
   *  Note that because of links, filesystem structure
   *  may not follow a sequence of "cd" commands.
   */
  public String pwd() throws IOException {
    String resp = executeCommand("PWD", null).getText();
    int i = resp.indexOf('\"');
    int j = resp.lastIndexOf('\"');
    if (i < 0 || i == j) {
      throw new IOException("Cannot parse PWD response: " + resp);
    }
    return resp.substring(i+1, j);
  }

  /**
   *  Renames a specified file on the FTP server.
   *  On most servers the source and target files must
   *  reside in the current working directory.
   *  Exception may result if the client lacks the
   *  necessary permissions, if the source file does
   *  not exist, or if the target filename is invalid on the FTP host.
  **/
  public void rename(String oldName, String newName) throws IOException {
    executeCommand("RNFR", oldName);
    executeCommand("RNTO", newName);
  }

  /**
   *  Removes a directory on the FTP server.
   *  On most systems the directory to be removed must
   *  reside directly beneath the current working directory.
   *  Exceptions may result if the client lacks the
   *  necessary permissions, if the subdirectory cannot
   *  be located, or if it is in use.
  **/
  public void deleteDir(String dirName) throws IOException {
    executeCommand("RMD", dirName);
  }

  /**
   *  Removes a specified file on the FTP server.
   *  On most systems the target file must reside in
   *  the current working directory.
   *  Exceptions may result if the file cannot be found,
   *  or if the client lacks the necessary permissions.
  **/
  public void deleteFile(String fileName) throws IOException {
    executeCommand("DELE", fileName);
  }

  /**
   *  Executes an arbitrary command on the FTP server.
   *  The command is passed directly to the FTP server.
   *  The response is the string reply on the command channel.
   *  No attempt is made to open data connections.
  **/
  public String quote(String param) throws IOException {
    return executeCommand(param, null).getText();
  }

  /**
   *  Executes a SITE command on the FTP server.
   *  The SITE command allows the FTP client to change server behavior,
   *  i.e. on MS's FTP server is can be used to switch directory listings
   *  to a unix-like format.
   *  The supported syntax varies among FTP implementations.
  **/
  public String site(String siteCmd) throws IOException {
    return executeCommand("SITE", siteCmd).getText();
  }

  /**
   *  Returns the host name of the FTP server.
   *  The returned string is whatever was used to
   *  connect to the host originally - a DNS name, or
   *  an IP address.  No DNS lookup is performed here.
  **/
  public String getHost() {
    return host;
  }

  /**
   *  Set the send/receive buffer size for binary transfers.
   *  The buffer defaults to 10k, which is sufficient for most operations.
   *  If you are transferring lots of small files and you want to reduce
   *  the memory overhead you can make it smaller.
   *  For very fast connections you might benefit from a larger buffer.
   *  Changes to the buffer size have no impact on active transfers.
  **/
  public void setBufferSize(int size) {
    if (size > 0) {
      bufferSize = size;
    }
  }

  /**
   *  Returns the current send/receive buffer size for binary transfers.
  **/
  public int getBufferSize() {
    return bufferSize;
  }

  /**
   *  Causes the currently executing thread to sleep for the specified number of milliseconds
   *  after during each send/receive.
  **/
  public void setTransferSleep(int millis) {
    this.transferSleep = millis;
  }

  /**
   *  Sets the network socket timeout in milliseconds.
   *  This is the amount of time that the system will wait before considering
   *  a link dead.
   *  The default value is 50 seconds, i.e. 50000 msec.
   *  For satellite-linkup or other high-latency connections this can be made higher.
   *  For high-speed internal networks you can lower the default to get faster
   *  notification of errors.
  **/
  public void setSocketTimeout(int timeout) {
    if (timeout > 0) {
      socketTimeout = timeout;
    }
  }

  /**
   *  Returns the current socket timeout in milliseconds.
  **/
  public int getSocketTimeout() {
    return socketTimeout;
  }

  /**
   *  Allow the library to automatically restart downloads.
   *  When set to <code>true></code>, the libarary will check the existing
   *  file size, and re-start the download at that point.
   *  This feature does not take into account corrupted data, etc.
   *  The default setting of <code>false</code> means that existing files
   *  will get overwritten.
   *  Note that this has no effect on downloads to streams.
  **/
  public void setAllowRestart(boolean allowRestart) {
    this.allowRestart = allowRestart;
  }

  /**
   *  Returns <code>true</code> if code attempts to restart downloads,
   *  <code>false</code> if existing files are overwritten.
   *  By default the library will overwrite existing files.
   *  Note that this has no effect on downloads to streams.
  **/
  public boolean isAllowRestart() {
    return allowRestart;
  }

  /**
   *  Enable or disable debugging.
   *  When in debug mode, the library will print to the specified print stream
   *  a "transcript" of the session, excluding of course the content of files
   *  or passwords being transferred.
  **/
  public void setDebug(PrintStream debug) {
    this.debug = debug;
    if (debug != null) {
      debug.println("FTP debugging enabled");
    }
  }

  /**
   *  Returns current debug state.
  **/
  public boolean isDebug() {
    return (debug != null);
  }

  /**
   *  Checks whether the FTP connection has been authenticated.
   *  An exception is raised if the connection had not
   *  been authenticated.
  **/
  private void checkConnection(String command) throws IOException {
    if (controlOutputStream == null) {
      throw new IOException("No connection to server exists. " + command);
    }
    if ((command.startsWith("RETR") || command.startsWith("STOR")) && !authenticated) {
      throw new IOException("Authentication required before attempting " + command);
    }
  }

  /**
   *  Convinience routine to simplify the creation of exceptions
  **/
  private Response checkResponseCode(String command) throws IOException {
    Response response = null;
    try {
      StringBuffer reply = new StringBuffer(200);
      String line = controlInputStream.readLine();
      if (line == null) {
        throw new IOException(command + " failed.");
      }
      reply.append(line);
      if (!Response.isValid(line)) {
        reply.append('\n');
        for (int i = 0; i < 5 && !Response.isValid(line); ++i) {
          line = controlInputStream.readLine();
          if (line == null) {
            throw new IOException(command + " failed.");
          }
          reply.append(line);
          reply.append('\n');
        }
      }
      if (debug != null) {
        debug.println("< " + reply);
      }
      response = new Response(reply.toString());
    } catch (IOException ioe) {
      if (debug != null) {
        ioe.printStackTrace(debug);
      }
      throw ioe;
    }
    if (response == null) {
      throw new IOException(command + " failed.");
    }
    if (!response.canContinue()) {
      throw new IOException(command + " failed with " + response.getText());
    }
    return response;
  }

  /**
   *  Executes an FTP command and checks the response
  **/
  private Response executeCommand(String commandName, String arguments) throws IOException {
    checkConnection(commandName);
    StringBuffer buffer = new StringBuffer(commandName.length()+1+((arguments != null)? arguments.length() : 0));
    buffer.append(commandName);
    if (arguments != null) {
      buffer.append(' ');
      buffer.append(arguments);
    }
    controlOutputStream.print(buffer.toString());
    controlOutputStream.print("\r\n");
    if (debug != null) {
      if (!commandName.equals("PASS")) {
        debug.print("> ");
        debug.println(buffer.toString());
      } else {
        debug.println("> PASS XXXX");
      }
    }
    return checkResponseCode(commandName);
  }

  /**
   *  Creates a passive socket and executes the specified command.
  **/
  private Socket getDataSocket(String command) throws IOException {
    StringTokenizer token = new StringTokenizer(executeCommand("PASV", null).getText(), ",)\r\n");
    token.nextToken(); // stepping to port
    token.nextToken(); // stepping to port
    token.nextToken(); // stepping to port
    token.nextToken(); // stepping to port
    try {
      Socket socket = new Socket(host,  Integer.parseInt(token.nextToken())*256 + Integer.parseInt(token.nextToken()));
      socket.setSoTimeout(socketTimeout);
      executeCommand(command, null);
      return socket;
    } catch (Exception e) {
      if (debug != null) {
        e.printStackTrace(debug);
      }
      throw new IOException("I/O error trying to open PASV connection for " + command);
    }
  }
  
  /**
   *  Network socket timeout.
  **/  
  private int socketTimeout = 50000;  // in msec

  /**
   *  Printer for debugging statements.
  **/
  private PrintStream debug = null;

  /**
   *  Transmit buffer defaults to 10k (binary transfers only)
  **/
  private int bufferSize = 10240;

  private int transferSleep = 0;

  // overwrite files by default
  private boolean allowRestart = false;

  // host we are connected to (or at least what we asked to connect to)
  private String host = null;

  // control connection
  private Socket controlSocket;
  private BufferedReader controlInputStream;
  private PrintStream controlOutputStream;

  // flag to indicate that we had already authenticated
  // on the active connection
  private boolean authenticated = false;

  // when this is set, various routines will attempt to complete/abort ASAP
  private boolean abort = false;


  /**
   *  Simple test program.
  **/
  public static void main(String args[]) {
    FTP ftp = null;
    try {
      zeta.util.Properties properties = new zeta.util.Properties(zeta.util.Properties.ZETA_TOOLS_CFG);
      ftp = new FTP();
      ftp.connect(properties.get("ftp.host", ""));
      ftp.login(properties.get("ftp.user", ""), properties.get("ftp.password", ""));
      if (args[0].equals("put")) {
        for (int i = 1; i < args.length; ++i) {
          File file = new File(args[i]);
          if (file.exists()) {
            System.out.println("put: " + file.getName());
            ftp.put(file.getAbsolutePath(), file.getName(), MODE_BINARY);
          }
        }
      } else if (args[0].equals("get")) {
        for (int i = 1; i < args.length; ++i) {
          File file = new File(args[i]);
          ftp.get(args[i], file.getName(), MODE_BINARY);
        }
      } else {
        System.out.println("USAGE: put {list of filenames separated by blanks}\n"
                         + "       get {list of filenames separated by blanks}");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (ftp != null) {
        ftp.disconnect();
      }
    }
  }


  /**
   *  Encompasses the FTP server's response - code and message
  **/
  private static class Response {
    /**
     *  Constructor based on an FTP server's reply
    **/
    public Response(String replyLine) throws IOException {
      text = null;
      if (replyLine != null && replyLine.length() >= 3) {
        try {
          code = Integer.parseInt(replyLine.substring(0, 3));
          text = replyLine;
        } catch (NumberFormatException nfe) {
        }
      }
      if (text == null) {
        throw new IOException("Could not parse response: " + replyLine);
      }
    }

    /**
     *  Determines if the response has a correct format:
     *  The first 3 characters are digits followed by a blank.
     *  @param line response
    **/
    public static boolean isValid(String line) {
      return (line != null && line.length() >= 4 && line.charAt(3) == ' ' && Character.isDigit(line.charAt(0)) && Character.isDigit(line.charAt(1)) && Character.isDigit(line.charAt(2)));
    }

    /**
     *  Returns the replay code, i.e. the numeric presentation of reply as a number.
     *  @return the replay code, i.e. the numeric presentation of reply as a number.
    **/
    public int getCode() {
      return code;
    }

    /**
     *  Returns the reply.
     *  @return reply as string
    **/
    public String getText() {
      return text;
    }

    /**
     *  Determines if the response code is affirmative, i.e.
     *  the codes must be between 100 and 399 (inclusive),
     *  other codes indicate errors/problems
    **/
    public boolean canContinue() {
      return (code >= 100 && code < 400);
    }

    private int code;
    private String text;
  }
}
