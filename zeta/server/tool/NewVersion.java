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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import zeta.crypto.IONumber;
import zeta.crypto.KeyManager;
import zeta.crypto.Signature;
import zeta.server.TaskManager;
import zeta.server.tool.Database;
import zeta.server.util.DatabaseUtils;
import zeta.util.Base64;
import zeta.util.StreamUtils;
import zeta.util.StringUtils;
import zeta.util.ThrowableHandler;

public class NewVersion {
  public static void main(String[] args) {
    try {
      if (args.length == 4 && args[0].equals("uos")) {
        NewVersion newVersion = new NewVersion(args[2], args[3]);
        int randomize = Integer.parseInt(args[1]);
        newVersion.taskManager.updateTasksSignature(newVersion.connection, "private_key.txt", randomize, "zeta.tasks");   // ToDo: not fix
        newVersion.updateOverallSingatures("private_key.txt", randomize);
      } else if (args.length == 6 && args[0].equals("uec")) {
        NewVersion newVersion = new NewVersion(args[4], args[5]);
        newVersion.updateEncryptionClass(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), "private_key.txt");  // ToDo: must be a different private key for keyClassname
        newVersion.close();
      } else if (args.length >= 6 && args.length <= 11 && args[0].length() == 1) {
        char c = args[0].charAt(0);
        String user = args[args.length-2];
        String password = args[args.length-1];
        String os = "";
        String arch = "";
        int taskId = 0;
        int randomize = Integer.parseInt(args[args.length-3]);
        String programFromUser = args[args.length-4];
        int numberOfProcessors = Integer.parseInt(args[args.length-5]);
        int i = 1;
        if (args.length >= 10) {
          os = args[args.length-7];
          arch = args[args.length-6];
        }
        if (args.length == 9 || args.length == 11) {
          taskId = Integer.parseInt(args[1]);
          i = 2;
        }
        NewVersion newVersion = new NewVersion(user, password);
        if (c == 'i') {
          String privateKeyFileName = "private_key.txt";    // ToDo: must be a different private key for keyClassname
          newVersion.insertNewVersion(taskId, args[i], args[i+1], os, arch, numberOfProcessors, randomize, null, programFromUser, privateKeyFileName);    // ToDo: configure key class name
        } else if (c == 'u') {
          String privateKeyFileName = "private_key.txt";    // ToDo: must be a different private key for keyClassname
          newVersion.updateNewVersion(taskId, args[i], args[i+1], os, arch, numberOfProcessors, randomize, null, programFromUser, privateKeyFileName);
        } else if (c == 'd') {
          String privateKeyFileName = "private_key.txt";    // ToDo: must be a different private key for keyClassname
          newVersion.deleteVersion(taskId, args[i], args[i+1], os, arch, numberOfProcessors, randomize, privateKeyFileName);
        } else if (c == 's') {
          newVersion.simpleUpdate(taskId, args[i], args[i+1], os, arch, numberOfProcessors, (randomize != 0));
        }
        newVersion.close();
      } else {
        System.err.println("USAGE: java zeta.server.tool.NewVersion <{i,u,d,s}> [<taskId>] <version> <program> [<os> <arch>] <number of processors> <from user> <randomize> <user> <password>\n"
                         + "       java zeta.server.tool.NewVersion uec <taskId> <encryption class filename> <randomize> <user> <password>\n"
                         + "       java zeta.server.tool.NewVersion uos <randomize> <user> <password>\n"
                         + "       i   - insert a new program and update the signatures\n"
                         + "       u   - update the program and update the signatures\n"
                         + "       d   - delete the program and update the signatures\n"
                         + "       s   - update the program\n"
                         + "       uec - update the encryption class\n"
                         + "       uos - update the overall signatures");
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  private NewVersion(String user, String password) throws Exception {
    connection = Database.getConnection(user, password);
    taskManager = TaskManager.getInstance(connection);
  }

  private void close() {
    DatabaseUtils.close(connection);
  }

  private static String getFilename(String program, String osName, String arch, int numberOfProcessors) {
    if (osName.length() == 0) {
      return program;
    }
    String name = osName + '/' + arch + '/' + numberOfProcessors + '/' + program;
    File file = new File(name);
    if (file.exists()) {
      return name;
    }
    name = osName + '/' + arch + '/' + program;
    file = new File(name);
    return (file.exists())? name : (osName + '/' + program);
  }

  /**
   *  Returns signature
  **/
  private String getSignature(String program, String osName, String arch, int numberOfProcessors, int randomize, String keyClassname, String privateKeyFileName) throws Exception {
    Signature signature = new Signature(KeyManager.getKey(keyClassname));
    InputStream in = null;
    InputStream privateKeyFile  = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream(16*1024);
      in = new FileInputStream(getFilename(program, osName, arch, numberOfProcessors));
      privateKeyFile = new FileInputStream(privateKeyFileName);
      BigInteger privateKey = IONumber.read(privateKeyFile);
      if (privateKey == null) {
        throw new IOException("Missing private key file");
      }
      signature.generate(randomize, privateKey, in, out);
      return out.toString("UTF-8");
    } finally {
      StreamUtils.close(in);
      StreamUtils.close(privateKeyFile);
    }
  }

  private void updateOverallSingatures(String privateKeyFileName, int randomize) {
    System.out.println("update overall signatures");
    Statement stmt = null;
    PreparedStatement pStmt = null;
    try {
      connection.setAutoCommit(false);
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT DISTINCT task_id,os_name,os_arch,processors FROM zeta.program");
      while (rs.next()) {
        int taskId = rs.getInt(1);
        String os = rs.getString(2);
        String arch = rs.getString(3);
        int numberOfProcessors = rs.getInt(4);
        String overallSignature = getOverallSignature(taskId, os, arch, numberOfProcessors, privateKeyFileName, randomize);
        String s = "UPDATE zeta.program SET overall_signature=? WHERE task_id=" + taskId + " AND os_name='" + os + "' AND os_version='' AND os_arch='" + arch + "' AND processors=" + numberOfProcessors;
        System.out.println(s);
        pStmt = connection.prepareStatement(s);
        pStmt.setBytes(1, overallSignature.getBytes("UTF-8"));
        int result = pStmt.executeUpdate();
        if (result <= 0) {
          System.out.println("ERROR!");
        }
        pStmt.close();
        pStmt = null;
      }
      rs.close();
      connection.setAutoCommit(true);
      connection.commit();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(pStmt);
    }
  }

  private String getOverallSignature(int taskId, String osName, String osArch, int numberOfProcessors, String privateKeyFileName, int randomize) throws Exception {
    String shortOsName = osName.trim();
    int idx = shortOsName.indexOf(' ');
    if (idx > 0) {
      shortOsName = shortOsName.substring(0, idx);
    }
    StringBuffer buffer = new StringBuffer(20000);
    buffer.append("SELECT key_class_name,program_from_user,name,os_name,os_arch,signature,processors FROM zeta.program WHERE (task_id=0 OR task_id=");
    buffer.append(taskId);
    buffer.append(") AND (os_name='' OR os_name='");
    buffer.append(shortOsName);
    buffer.append("') AND (os_arch='' OR os_arch='");
    buffer.append(osArch);
    buffer.append("') AND (processors=");
    buffer.append(numberOfProcessors);
    buffer.append(" OR processors=0 AND name NOT IN (SELECT name FROM zeta.program WHERE (task_id=0 OR task_id=");
    buffer.append(taskId);
    buffer.append(") AND (os_name='' OR os_name='");
    buffer.append(shortOsName);
    buffer.append("') AND (os_arch='' OR os_arch='");
    buffer.append(osArch);
    buffer.append("') AND processors=");
    buffer.append(numberOfProcessors);
    buffer.append(")) ORDER BY task_id,processors DESC,name");
    String keyClassname = null;
    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(buffer.toString());
      buffer.delete(0, buffer.length());
      buffer.append(taskManager.encode());
      buffer.append('\n');
      while (rs.next()) {
        //int processors = rs.getInt(7);
        buffer.append(rs.getString(1)); // key class name
        buffer.append('\n');
        buffer.append(rs.getString(2)); // program from user
        buffer.append('\n');
        buffer.append(rs.getString(3)); // program name
        String os = rs.getString(4); // OS name
        String arch = rs.getString(5);
        if (os.length() > 0 && arch.length() > 0) {
          buffer.append(',');
          buffer.append(os);
          buffer.append(',');
          buffer.append(arch);
        }
        buffer.append('\n');
        buffer.append(rs.getString(6)); // signature
        buffer.append('\n');
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
    }
    Signature sig = new Signature(KeyManager.getKey(null));
    InputStream in = null;
    InputStream privateKeyFile  = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream(16*1024);
      in = new ByteArrayInputStream(buffer.toString().getBytes("UTF-8"));
      privateKeyFile = new FileInputStream(privateKeyFileName);
      BigInteger privateKey = IONumber.read(privateKeyFile);
      if (privateKey == null) {
        throw new IOException("Missing private key file");
      }
      sig.generate(randomize, privateKey, in, out);
      return out.toString("UTF-8");
    } finally {
      StreamUtils.close(in);
      StreamUtils.close(privateKeyFile);
    }
  }

  private void insertNewVersion(int taskId, String version, String program, String os, String arch, int numberOfProcessors, int randomize, String keyClassname, String programFromUser, String privateKeyFileName) throws Exception {
    if (programFromUser == null) {
      throw new IllegalArgumentException("The user who build the program must be specified.");
    }
    if (numberOfProcessors < 0) {
      numberOfProcessors = 0;
    }
    if (keyClassname == null) {
      keyClassname = "zeta.crypto.DefaultKey";
    }
    PreparedStatement stmt = null;
    try {
      connection.setAutoCommit(false);
      taskManager.updateTasksSignature(connection, privateKeyFileName, randomize, "zeta.tasks");
      String signature = getSignature(program, os, arch, numberOfProcessors, randomize, keyClassname, privateKeyFileName);
      String s = "INSERT INTO zeta.program (task_id,name,os_name,os_version,os_arch,processors,version,key_class_name,program_from_user,compressed_YN,program,last_update,signature,overall_signature) VALUES ("
                 + taskId + ",'" + program + "','" + os + "','','" + arch + "'," + numberOfProcessors + ",'" + version + "','" + keyClassname + "','" + programFromUser + "','Y',?,CURRENT TIMESTAMP,?,'')";
      System.out.println(s);
      stmt = connection.prepareStatement(s);
      stmt.setBytes(1, StreamUtils.getFile(getFilename(program, os, arch, numberOfProcessors), true, true));
      stmt.setBytes(2, signature.getBytes("UTF-8"));
      System.out.println("exec");
      stmt.executeUpdate();
      System.out.println("ready");
      System.out.println(signature);
    } finally {
      DatabaseUtils.close(stmt);
      updateOverallSingatures(privateKeyFileName, randomize);
    }
  }

  private void updateNewVersion(int taskId, String version, String program, String os, String arch, int numberOfProcessors, int randomize, String keyClassname, String programFromUser, String privateKeyFileName) throws Exception {
    if (programFromUser == null) {
      throw new IllegalArgumentException("The user who build the program must be specified.");
    }
    if (numberOfProcessors < 0) {
      numberOfProcessors = 0;
    }
    if (keyClassname == null) {
      keyClassname = "zeta.crypto.DefaultKey";
    }
    PreparedStatement stmt = null;
    try {
      String signature = getSignature(program, os, arch, numberOfProcessors, randomize, keyClassname, privateKeyFileName);
      String s = "UPDATE zeta.program SET (version,key_class_name,program_from_user,compressed_YN,program,last_update,signature,overall_signature)=(?,?,?,'Y',?,CURRENT TIMESTAMP,?,'') WHERE name=? AND task_id="
                 + taskId + " AND os_name='" + os + "' AND os_version='' AND os_arch='" + arch + "' AND processors=" + numberOfProcessors;
      System.out.println(s);
      stmt = connection.prepareStatement(s);
      stmt.setString(1, version);
      stmt.setString(2, keyClassname);
      stmt.setString(3, programFromUser);
      stmt.setBytes(4, StreamUtils.getFile(getFilename(program, os, arch, numberOfProcessors), true, true));
      stmt.setBytes(5, signature.getBytes("UTF-8"));
      stmt.setString(6, program);
      System.out.println("exec");
      int result = stmt.executeUpdate();
      System.out.println("result=" + result);
      stmt.close();
      stmt = null;
      if (result != 1) {
        System.out.println("ERROR!");
      } else {
        System.out.println("ready");
        System.out.println(signature);
      }
      if (taskId > 0) {
        System.out.println("update overall signature");
        String overallSignature = getOverallSignature(taskId, os, arch, numberOfProcessors, privateKeyFileName, randomize);
        s = "UPDATE zeta.program SET overall_signature=? WHERE task_id=" + taskId + " AND os_name='" + os + "' AND os_version='' AND os_arch='" + arch + "' AND processors=" + numberOfProcessors;
        stmt = connection.prepareStatement(s);
        stmt.setBytes(1, overallSignature.getBytes("UTF-8"));
        if (result != 1) {
          System.out.println("ERROR!");
        } else {
          System.out.println("ready");
        }
      }
    } finally {
      DatabaseUtils.close(stmt);
      if (taskId == 0) {
        updateOverallSingatures(privateKeyFileName, randomize);
      }
    }
  }

  private void deleteVersion(int taskId, String version, String program, String os, String arch, int numberOfProcessors, int randomize, String privateKeyFileName) throws Exception {
    PreparedStatement stmt = null;
    try {
      connection.setAutoCommit(false);
      taskManager.updateTasksSignature(connection, privateKeyFileName, randomize, "zeta.tasks");
      String s = "DELETE FROM zeta.program WHERE name=? AND task_id=" + taskId + " AND os_name='" + os + "' AND os_version='' AND os_arch='" + arch
               + "' AND processors=" + numberOfProcessors;
      System.out.println(s);
      stmt = connection.prepareStatement(s);
      stmt.setString(1, program);
      System.out.println("exec");
      int result = stmt.executeUpdate();
      System.out.println("result=" + result);
      if (result != 1) {
        System.out.println("Warning: program is not defined!");
      }
    } finally {
      DatabaseUtils.close(stmt);
      updateOverallSingatures(privateKeyFileName, randomize);
    }
  }

  private void simpleUpdate(int taskId, String version, String program, String os, String arch, int numberOfProcessors, boolean compress) throws Exception {
    PreparedStatement stmt = null;
    try {
      String s = "UPDATE zeta.program SET (version,compressed_YN,program)=(?,'" + ((compress)? 'Y' : 'N')
                 + "',?) WHERE name=? AND task_id=" + taskId
                 + " AND os_name='" + os + "' AND os_version='' AND os_arch='" + arch + "' AND processors=" + numberOfProcessors;
      System.out.println(s);
      stmt = connection.prepareStatement(s);
      stmt.setString(1, version);
      stmt.setBytes(2, StreamUtils.getFile(getFilename(program, os, arch, numberOfProcessors), compress, true));
      stmt.setString(3, program);
      System.out.println("exec");
      int result = stmt.executeUpdate();
      System.out.println("result=" + result);
      if (result != 1) {
        System.out.println("ERROR!");
      }
    } finally {
      DatabaseUtils.close(stmt);
    }
  }

  private void updateEncryptionClass(int taskId, String classFileName, int randomize, String privateKeyFileName) throws Exception {
    InputStream in = null;
    InputStream privateKeyFile = null;
    PreparedStatement stmt = null;
    Signature signature = new Signature(KeyManager.getKey(null));
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream(4*1024);
      in = new FileInputStream(classFileName);
      StreamUtils.writeData(in, out, true, true);
      in = null;
      byte[] encryptionClass = out.toByteArray();
      privateKeyFile = new FileInputStream(privateKeyFileName);
      BigInteger privateKey = IONumber.read(privateKeyFile);
      if (privateKey == null) {
        throw new IOException("Missing private key file");
      }
      out.reset();
      signature.generate(randomize, privateKey, new ByteArrayInputStream(encryptionClass), out);
      String s = "UPDATE zeta.task SET (encryption_class,encryption_signature)=(?,?) WHERE id=" + taskId;
      System.out.println(s);
      stmt = connection.prepareStatement(s);
      stmt.setString(1, Base64.encode(encryptionClass));
      stmt.setString(2, out.toString("UTF-8"));
      System.out.println("exec");
      int result = stmt.executeUpdate();
      if (result != 1) {
        System.out.println("ERROR! result=" + result);
      }
    } finally {
      DatabaseUtils.close(stmt);
      StreamUtils.close(in);
    }
  }

  private Connection connection = null;
  private TaskManager taskManager = null;
}
