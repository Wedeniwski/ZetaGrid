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

package zeta;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import zeta.crypto.Encrypter;
import zeta.crypto.Key;
import zeta.crypto.KeyManager;
import zeta.crypto.Signature;
import zeta.util.Base64;
import zeta.util.Properties;
import zeta.util.StreamUtils;

/**
 *  Kernel of ZetaGrid.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class ZetaClient {
  /**
   *  Main program to start the client.
   *  @param args argumens from the command line; 'exit' will terminate the active client
  **/
  public static void main(String[] args) {
    try {
      properties = new Properties(Properties.ZETA_CFG, Properties.DEFAULT_CFG);
    } catch (IOException e) {
      ZetaInfo.handle(e);
      return;
    }
    String f = properties.get("exit.filename");
    if (args != null && args.length > 0 && args[0].equals("exit")) {
      exit();
      if (f != null && f.length() > 0) {
        new File(f).delete();
      }
    } else if (f != null && f.length() > 0) {
      File file = new File(f);
      if (file.exists()) {
        exit();
        file.delete();
      }
    } else {
      System.setProperty("file.encoding", "8859_15");
      ZetaClient zeta = new ZetaClient();
    }
  }

  /**
   *  Returns <code>true</code> if an UnknownHostException occurs.
   *  @return <code>true</code> if an UnknownHostException occurs.
  **/
  public static boolean isUnknownHostExceptionOccur() {
    return unknownHostExceptionOccur;
  }

  /**
   *  Constructs the environment for the client. Downloads the managing part of the client and the required libraries for the defined task.
   *  A socket will be opened on default port 10000 to block a second start of the same client on the same computer which occur synchronization problems.
  **/
  private ZetaClient() {
    try {
      ZetaInfo.init(properties);
      // check if already running
      Thread t = new Thread() {
        public void run() {
          int portNumber = properties.get("port", 10000);
          try {
            ServerSocket serverSocket = new ServerSocket(portNumber, 0, InetAddress.getByName("127.0.0.1"));
            active = true;
            serverSocket.accept();
          } catch (BindException be) {
            ZetaInfo.write("Error: The program is already running or the port number " + portNumber + " is not available (can be changed in the configuration).");
          } catch (Throwable e) {
            ZetaInfo.handle(e);
          } finally {
            System.exit(1);
          }
        }
      };
      t.start();
      while (!active) {
        Thread.sleep(100);
      }
      t.setPriority(Thread.MIN_PRIORITY);
      ZetaInfo.write("Initialization of client");
      setProxyAuthentification();
      downloadFiles();
      startComputationManager();
    } catch (Throwable e) {
      ZetaInfo.handle(e);
      System.exit(1);
    }
  }

  /**
   *  Sends local information (filenames of the local directory (max. 900 characters), hostname, host address, os name, os version, os architecture, task name)
   *  to the server and receives required libraries for the runtime environment of the task.
   *  Every downloaded library must have a correct digital signature before it will be stored on hard disk.
   *  The first file "signature.txt" contains all digital signatures.
  **/
  private void downloadFiles() {
    unknownHostExceptionOccur = false;
    StringBuffer localFiles = new StringBuffer(1000);
    String[] list = new File(".").list();
    if (list != null && list.length > 0) {
      for (int i = 0; i < list.length && localFiles.length() < 900; ++i) {  // IO-Error at WEB-Server if more than about 900 characters
        String s = list[i].toLowerCase();
        if (!ignoreFilename(s)) {
          if (localFiles.length() == 0) {
            localFiles.append('\'');
            localFiles.append(s);
          } else {
            localFiles.append("','");
            localFiles.append(s);
          }
        }
      }
      localFiles.append('\'');
    }
    HttpURLConnection connection = null;
    try {
      InetAddress localHost = InetAddress.getLocalHost();
      String osName = System.getProperty("os.name", "?");
      String arch = System.getProperty("os.arch", "?");
      URL url = new URL("http", properties.get("host.name", "www.zetagrid.net"),
                        properties.get("host.port", 80),
                        encryptURLFile(properties.get("downloadURL", "/servlet/service/getClient")
                                       + "?hostname=" + URLEncoder.encode(localHost.getHostName().toLowerCase())
                                       + "&hostaddr=" + URLEncoder.encode(localHost.getHostAddress())
                                       + "&key=" + URLEncoder.encode(getKey())
                                       + "&os_name=" + URLEncoder.encode(osName)
                                       + "&os_version=" + URLEncoder.encode(System.getProperty("os.version", "?"))
                                       + "&os_arch=" + URLEncoder.encode(arch)
                                       + "&task=" + URLEncoder.encode(properties.get("task", "zeta-zeros"))
                                       + "&processors=" + URLEncoder.encode(properties.get("processors", "1"))
                                       + "&files=" + URLEncoder.encode(localFiles.toString())));
      connection = (HttpURLConnection)url.openConnection();
      connection.setUseCaches(false);
      connection.setRequestMethod("GET");
      connection.setDoInput(true);
      connection.connect();
      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        int idx = osName.indexOf(' ');
        if (idx > 0) {
          osName = osName.substring(0, idx);
        }
        ZipInputStream zip = new ZipInputStream(connection.getInputStream());
        ZipEntry entry = zip.getNextEntry();
        if (entry != null && entry.getName().equalsIgnoreCase("signature.txt")) {
          String signatures = getSignatures(zip);
          int count = 0;
          while (true) {
            entry = zip.getNextEntry();
            if (entry == null) {
              break;
            }
            if (++count == 1) {
              if (!extractTasks(signatures)) {
                ZetaInfo.write("The task definition is invalid.");
              }
              if (verifySignatures(signatures)) {  // signature is in the last line
                signatures = signatures.substring(signatures.indexOf('\n', signatures.indexOf('\n')+1)+1);
              } else {
                ZetaInfo.write("The digital signatures are invalid.");
                return;
              }
            }
            try {
              String outName = entry.getName().toLowerCase();
              Object[] o = extractSignature(signatures, osName, arch, outName);
              String signature = null;
              Key key = null;
              String fileFromUser = null;
              if (o != null) {
                signature = (String)o[0];
                key = (Key)o[1];
                fileFromUser = (String)o[2];
              }
              ZetaInfo.write("The file '" + outName + "' comes from the user '" + fileFromUser + '\'');
              if (isTrustedUser(fileFromUser)) {
                writeData(signature, key, zip, outName);
              } else {
                ZetaInfo.write("You do not trust this file.");
              }
            } catch (IOException ioe) {
              ZetaInfo.handle(ioe);
            }
          }
          if (count > 0) {
            downloadCompleted = true;
          }
          return;
        }
      }
    } catch (UnknownHostException uhe) {
      ZetaInfo.write("Unknown host: " + uhe.getMessage());
      unknownHostExceptionOccur = true;
    } catch (MalformedURLException e) {
      ZetaInfo.handle(e);
    } catch (IOException e) {
      ZetaInfo.handle(e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    if (!unknownHostExceptionOccur) {
      ZetaInfo.write("Could not download client.");
    }
  }

  private boolean ignoreFilename(String filename) {
    return filename.endsWith(".log") || filename.endsWith(".tmp") || filename.endsWith(".txt");
  }

  /**
   *  Starts the computation manager.
  **/
  private void startComputationManager() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    Class mainClass = Class.forName(properties.get("exec.class", "zeta.ZetaCalc"), true, new ClassLoader(ClassLoader.getSystemClassLoader()) {
      protected Class findClass(String name) throws ClassNotFoundException {
        if (downloadCompleted) {
          ZetaInfo.write("All necessary files are downloaded. Please start again.");
          System.exit(1);
          return null;
        } else {
          throw new ClassNotFoundException(name);
        }
      }
    });
    Object obj = mainClass.newInstance();
    mainClass.getDeclaredMethod(properties.get("exec.method", "run"), new Class[] {}).invoke(obj, new Object[] {});
  }

  /**
   *  Terminates the client process.
  **/
  private static void exit() {
    try {
      ZetaInfo.write("The client process will be terminated.");
      Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), properties.get("port", 10000));
      socket.close();
    } catch (IOException ioe) {
      ZetaInfo.handle(ioe);
    }
  }

  /**
   *  Writes a program on hard disk if the digital signature is correct.
   *  @param  signature  digital signature of the program
   *  @param  key public key of the digital signature
   *  @param  in      input stream with the program data
   *  @param  outName filename of the program
   *  @exception  IOException  if an I/O error occurs.
  **/
  private void writeData(String signature, Key key, InputStream in, String outName) throws IOException {
    ZetaInfo.write("Download file '" + outName + '\'');
    ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
    StreamUtils.writeData(in, out, false, true);
    ZetaInfo.write("Check digital signature of file '" + outName + '\'');
    if (signature != null) {
      Signature sig = new Signature(key);
      if (sig.verify(signature, out.toByteArray())) {
        File file = new File(outName);
        file.delete();    // important for UNIX
        FileOutputStream fout = null;
        try {
          fout = new FileOutputStream(outName);
          out.writeTo(fout);
        } finally {
          StreamUtils.close(fout);
        }
        return;
      }
    }
    throw new IOException("Wrong signature for " + outName);
  } 

  /**
   *  Converts the digital signatures from an input stream to a string.
   *  @param  in  input stream with the digital signatures
   *  @returm the digital signatures as a string
   *  @exception  IOException  if an I/O error occurs.
  **/
  private String getSignatures(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(2 * 1024);
    StreamUtils.writeData(in, out, false, true);
    return out.toString("UTF-8");
  }

  /**
   *  Verifies the digital signature of all signatures.
   *  The digital signature of all signatures is in the last line.
   *  The default key will be used for the verification.
   *  @param  signatures  all digital signatures
   *  @return <code>true</code> if the digital signature of all signatures is correct.
   *  @exception  IOException  if an I/O error occurs.
  **/
  private boolean verifySignatures(String signatures) throws IOException {
    int i = signatures.lastIndexOf('\n');
    if (i >= 0 && i+1 < signatures.length()) {
      return verify(null, signatures.substring(i+1), signatures.substring(0, i+1).getBytes("UTF-8"));
    }
    return false;
  }

  /**
   *  Searches the digital signature of a specified program, os name and architecture in all downloaded digital signatures.
   *  @param  signatures  all downloaded digital signatures
   *  @param  osName      os name
   *  @param  arch        processor architecture
   *  @param  program     name of the program
   *  @return  (the digital signature of the specified program and environment, key of the digital signature, file from user)
   *  @exception  IOException  if an I/O error occurs.
  **/
  private Object[] extractSignature(String signatures, String osName, String arch, String program) throws IOException {
    // key class name
    // newLine, program from user
    // newLine, name, os name, os arch
    // newLine, signature
    boolean ignorePrograms = (program.equalsIgnoreCase("zeta.jar") || program.equalsIgnoreCase("zeta_client.jar") || program.equalsIgnoreCase("default.cfg"));
    BufferedReader reader = new BufferedReader(new StringReader(signatures));
    try {
      while (true) {
        String keyClassName = reader.readLine();
        String fileFromUser = reader.readLine();
        String filename = reader.readLine();
        if (filename == null) {
          break;
        }
        int idx1 = filename.indexOf(',');
        int idx2 = filename.indexOf(',', idx1+1);
        if (idx1 < idx2 && idx1 > 0 && idx2+1 < filename.length() && !ignorePrograms) {
          if (filename.substring(idx1+1, idx2).equalsIgnoreCase(osName) && filename.substring(0, idx1).equalsIgnoreCase(program) && filename.substring(idx2+1).equalsIgnoreCase(arch)) {
            return new Object[] { reader.readLine(), KeyManager.getKey(keyClassName), fileFromUser };
          }
        } else if (filename.equals(program)) {
          return new Object[] { reader.readLine(), KeyManager.getKey(keyClassName), fileFromUser };
        }
        reader.readLine();
      }
      return null;
    } finally {
      reader.close();
    }
  }

  private boolean extractTasks(String signatures) throws IOException {
    if (signatures.startsWith("Tasks:")) {
      int i = signatures.indexOf('\n');
      int j = signatures.indexOf('\n', i+1);
      if (i >= 0 && j > i && j+1 < signatures.length()) {
        ZetaInfo.write("Check digital signature of the task definitions");
        if (verify(null, signatures.substring(i+1, j), signatures.substring(0, i).getBytes("UTF-8"))) {
          String filename = InetAddress.getLocalHost().getHostName().toLowerCase() + ".tasks";
          File file = new File(filename);
          file.delete();    // important for UNIX
          FileOutputStream fout = null;
          try {
            fout = new FileOutputStream(filename);
            fout.write(signatures.substring(0, j).getBytes("UTF-8"));
          } finally {
            StreamUtils.close(fout);
          }
          return true;
        }
      }
    }
    return false;
  }

  private boolean isTrustedUser(String fileFromUser) {
    String trustFilesFromUsers = properties.get("trust_files_from_users");
    if (trustFilesFromUsers != null && trustFilesFromUsers.length() > 0) {
      StringTokenizer st = new StringTokenizer(trustFilesFromUsers, ",");
      while (st.hasMoreTokens()) {
        String user = st.nextToken().trim();
        if (fileFromUser.equals(user)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  private void setProxyAuthentification() {
    String proxyHost = System.getProperties().getProperty("http.proxyHost");
    if (proxyHost != null && proxyHost.length() > 0) {
      final String username = properties.get("proxy.authentication.username");
      final String password = properties.get("proxy.authentication.password");
      if (username != null && username.length() > 0 && password != null && password.length() > 0) {
        Authenticator.setDefault(new Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
          }
        });
      }
    }
  }

  /**
   *  The unique key of this computer contains the user name, the email address and the local host name.
   *  @return a unique key of this computer that is Base64 encoded.
  **/
  public static String getKey() {
    String key = "";
    try {
      key = properties.get("name") + properties.get("eMail") + InetAddress.getLocalHost().getHostName();
      key = Base64.encode(key.toLowerCase().getBytes("UTF-8"));
    } catch (Exception e) {
      ZetaInfo.write("Could not create key of workstation.");
    }
    return key;
  }

  /**
   *  Encrypts a specified file using the default encryption key.
   *  Every transfer of a result uses a key establishment protocol (half-certified Diffie-Hellman)
   *  with keys which have a length of 1024 Bit. 
   *  @param randomize random number for the key establishment protocol
   *  @param inFilename name of the file which should be encrypted
   *  @param outFilename name of the encrypted file
   *  @exception  IOException  if an I/O error occurs.
  **/
  static void encrypt(int randomize, String inFilename, String outFilename) throws IOException {
    encrypt(randomize, null, inFilename, outFilename);
  }

  /**
   *  Encrypts a specified file.
   *  Every transfer of a result uses a key establishment protocol (half-certified Diffie-Hellman)
   *  with keys which have a length of 1024 Bit. 
   *  @param randomize random number for the key establishment protocol
   *  @param keyClassData the bytes that make up the class data which contains the key for the encryption algorithm
   *  @param inFilename name of the file which should be encrypted
   *  @param outFilename name of the encrypted file
   *  @exception  IOException  if an I/O error occurs.
  **/
  static void encrypt(int randomize, byte[] keyClassData, String inFilename, String outFilename) throws IOException {
    Key key = null;
    try {
      key = KeyManager.getEncryptorKey(keyClassData);
    } catch (Exception e) {
      ZetaInfo.handle(e);
    }
    if (key == null) {
      ZetaInfo.write("Encryption key is invalid.");
      throw new IOException("Encryption key is invalid");
    }
    Encrypter encrypter = new Encrypter(key);
    encrypter.encrypt(randomize, inFilename, outFilename);
  }

  /**
   *  Encrypts the file attribute of a URL if the parameter 'encryption.url' is equal to true.
   *  The default encryption algorithmus will be used.
   *  @param urlFile file attribute of a URL
   *  @return encrypted file attribute of a URL where the parameters are packed in a new parameter 'param'
   *  @exception  IOException  if an I/O error occurs.
  **/
  static String encryptURLFile(String urlFile) throws IOException {
    if (urlFile != null && "true".equals(properties.get("encryption.url"))) {
      Key key = null;
      try {
        key = KeyManager.getEncryptorKey(null);
      } catch (Exception e) {
        ZetaInfo.handle(e);
      }
      if (key == null) {
        ZetaInfo.write("Encryption key is invalid.");
        throw new IOException("Encryption key is invalid");
      }
      Encrypter encrypter = new Encrypter(key);
      urlFile = encrypter.encryptURLFile(urlFile);
    }
    return urlFile;
  }

  /**
   *  Verifies the digital signature of the data using the specified key
   *  @exception  IOException  if an I/O error occurs.
  **/
  static boolean verify(String key, String signature, byte[] data) throws IOException {
    Signature sig = new Signature(KeyManager.getKey(key));
    return sig.verify(signature, data);
  }

  /**
   *  Is true if and only if the client is running.
  **/
  private boolean active = false;
  private static boolean downloadCompleted = false;

  /**
   *  <code>true</code> if an UnknownHostException occurs.
  **/
  private static boolean unknownHostExceptionOccur = false;

  /**
   *  Contains a persistent set of the ZetaGrid properties.
  **/
  private static Properties properties = null;
}
