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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import zeta.crypto.IONumber;
import zeta.crypto.KeyManager;
import zeta.crypto.Signature;
import zeta.server.processor.TaskRequestWorkUnitProcessor;
import zeta.server.processor.TaskResultProcessor;
import zeta.server.util.DatabaseUtils;
import zeta.util.Base64;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  @version 2.0, August 6, 2005
**/
public class TaskManager {

  public static synchronized TaskManager getInstance(Connection con) {
    if (taskManager == null) {
      taskManager = new TaskManager(con);
    }
    return taskManager;
  }

  public ServerTask getServerTask(String taskName) {
    Iterator i = serverTasks.values().iterator();
    while (i.hasNext()) {
      ServerTask task = (ServerTask)i.next();
      if (task.getName().equals(taskName)) {
        return task;
      }
    }
    return null;
  }

  public ServerTask getServerTask(int taskId) {
    return (ServerTask)serverTasks.get(new Integer(taskId));
  }

  /**
   *  Sorted by the task identifier.
  **/
  public Iterator getServerTasks() {
    return serverTasks.values().iterator();
  }

  public String encode() {
    StringBuffer buffer = new StringBuffer(60*serverTasks.size());
    buffer.append("Tasks:");
    Iterator i = serverTasks.values().iterator();
    while (i.hasNext()) {
      ServerTask task = (ServerTask)i.next();
      buffer.append(task.getId());
      buffer.append(',');
      buffer.append(task.getName());
      buffer.append(',');
      buffer.append(task.getClientTaskClassName());
      buffer.append(',');
      buffer.append(task.getWorkUnitClass().getName());
      buffer.append(';');
    }
    buffer.append('\n');
    buffer.append(signature);
    return buffer.toString();
  }

  private TaskManager(Connection con) {
    serverTasks = new TreeMap();
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT id,name,client_task_class_name,work_unit_class_name,encryption_class,encryption_signature,decryption_number,request_processor,result_processor,parameters,verifier_class_name,overall_signature,redistrib_connected,redistrib_unconnected FROM zeta.task");
      while (rs.next()) {
        if (signature == null) {
          signature = rs.getString(12);
        }
        int taskId = rs.getInt(1);
        if (taskId > 0) {
          String taskName = rs.getString(2);
          String clientTaskClassName = rs.getString(3);
          String workUnitClassName = rs.getString(4);
          byte[] encryptionClass = Base64.decode(rs.getString(5));
          String encryptionSignature = rs.getString(6);
          BigInteger decryptionNumber = new BigInteger(rs.getString(7), 32);
          String requestProcessorName = rs.getString(8);
          String resultProcessorName = rs.getString(9);
          String parameters = rs.getString(10);
          String workUnitVerifierClassName = rs.getString(11);
          int redistributionConnected = rs.getInt(13);
          int redistributionUnconnected = rs.getInt(14);
          TaskRequestWorkUnitProcessor requestProcessor = null;
          if (requestProcessorName != null) {
            try {
              Class requestProcessorClass = Class.forName(requestProcessorName);
              requestProcessor = (TaskRequestWorkUnitProcessor)requestProcessorClass.newInstance();
            } catch (Exception e) {
            }
          }
          TaskResultProcessor resultProcessor = null;
          if (resultProcessorName != null) {
            try {
              Class resultProcessorClass = Class.forName(resultProcessorName);
              resultProcessor = (TaskResultProcessor)resultProcessorClass.newInstance();
            } catch (Exception e) {
            }
          }
          serverTasks.put(new Integer(taskId), new ServerTask(taskId, taskName, clientTaskClassName, workUnitClassName, null, encryptionClass, encryptionSignature, decryptionNumber, requestProcessor, resultProcessor, parameters, workUnitVerifierClassName, redistributionConnected, redistributionUnconnected));
        }
      }
      rs.close();
    } catch (SQLException se) {
      ThrowableHandler.handle(se);
    } finally {
      DatabaseUtils.close(stmt);
    }
  }

  public void updateTasksSignature(Connection connection, String privateKeyFileName, int randomize, String outFilename) {
    InputStream in = null;
    InputStream privateKeyFile  = null;
    FileOutputStream fout = null;
    Statement stmt = null;
    try {
      Signature signature = new Signature(KeyManager.getKey(null));
      String s = taskManager.encode();
      s = s.substring(0, s.indexOf('\n'));
      ByteArrayOutputStream out = new ByteArrayOutputStream(16*1024);
      in = new ByteArrayInputStream(s.getBytes("UTF-8"));
      privateKeyFile = new FileInputStream(privateKeyFileName);
      BigInteger privateKey = IONumber.read(privateKeyFile);
      if (privateKey == null) {
        throw new IOException("Missing private key file");
      }
      signature.generate(randomize, privateKey, in, out);
      String overallSignature = out.toString("UTF-8");
      stmt = connection.createStatement();
      stmt.executeUpdate("UPDATE zeta.task SET overall_signature='" + overallSignature + "' WHERE id=0");
      this.signature = overallSignature;
      if (outFilename != null) {
        fout = new FileOutputStream(outFilename);
        fout.write((s+'\n'+overallSignature).getBytes("UTF-8"));
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      StreamUtils.close(fout);
      DatabaseUtils.close(stmt);
      StreamUtils.close(in);
      StreamUtils.close(privateKeyFile);
    }
  }

  private Map serverTasks = null;
  private String signature = null;
  private static TaskManager taskManager = null;
}
