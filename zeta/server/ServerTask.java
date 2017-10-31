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
     W. Westje
--*/

package zeta.server;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;

import zeta.Task;
import zeta.server.processor.TaskRequestWorkUnitProcessor;
import zeta.server.processor.TaskResultProcessor;
import zeta.server.util.DatabaseUtils;
import zeta.util.ThrowableHandler;

/**
 *  Defines the task at the server side.
 *
 *  @version 2.0, August 6, 2005
**/
public class ServerTask extends Task {
  /**
   *  The method <code>getTask</code> must be used.
   *  @param parameters parameters of the HttpRequest
  **/
  ServerTask(int taskId, String taskname, String clientTaskClassName, String workUnitClassName, Map parameter, byte[] encryptionClass, String encryptionSignature, BigInteger decryptionNumber,
             TaskRequestWorkUnitProcessor requestProcessor, TaskResultProcessor resultProcessor, String parameters, String workUnitVerifierClassName, int redistributionConnected, int redistributionUnconnected) {
    super(taskId, taskname, loadClass(workUnitClassName));
    this.clientTaskClassName = clientTaskClassName;
    this.parameter = parameter;
    this.encryptionClass = encryptionClass;
    this.encryptionSignature = encryptionSignature;
    this.decryptionNumber = decryptionNumber;
    this.requestProcessor = requestProcessor;
    this.resultProcessor = resultProcessor;
    this.redistributionConnected = redistributionConnected;
    this.redistributionUnconnected = redistributionUnconnected;
    this.parameters = parameters;
    workUnitVerifier = null;
    if (workUnitVerifierClassName != null) {
      try {
        workUnitVerifier = (WorkUnitVerifier)Class.forName(workUnitVerifierClassName).newInstance();
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      }
    }
  }

  ServerTask(ServerTask serverTask, Map parameter) {
    super(serverTask.getId(), serverTask.getName(), serverTask.getWorkUnitClass());
    clientTaskClassName = serverTask.clientTaskClassName;
    encryptionClass = serverTask.encryptionClass;
    encryptionSignature = serverTask.encryptionSignature;
    decryptionNumber = serverTask.decryptionNumber;
    requestProcessor = serverTask.requestProcessor;
    resultProcessor = serverTask.resultProcessor;
    parameters = serverTask.parameters;
    workUnitVerifier = serverTask.workUnitVerifier;
    this.parameter = parameter;
  }

  public String getClientTaskClassName() {
    return clientTaskClassName;
  }

  public WorkUnitVerifier getWorkUnitVerifier() {
    return workUnitVerifier;
  }

  /**
   *  Specifies in minutes the deadline when an active work unit expires and a redistributes will be invoked.
   *  The active work unit is reserved for a connected resource, i.e. that the resource has connected the server with another request up to the deadline.
   *  @return deadline in minutes
  **/
  public int getRedistributionConnected() {
    return redistributionConnected;
  }

  /**
   *  Specifies in minutes the deadline when an active work unit expires and a redistributes will be invoked.
   *  The active work unit is reserved for an unconnected resource, i.e. that the resource has not connected the server with any request up to the deadline.
   *  @return deadline in minutes
  **/
  public int getRedistributionUnconnected() {
    return redistributionUnconnected;
  }

  /**
   *  Returns the bytes that make up the class data which contains the key to encrypt the results of this task.
   *  @return the bytes that make up the class data which contains the key to encrypt the results of this task.
  **/
  public byte[] getEncryptionClass() {
    return encryptionClass;
  }

  /**
   *  Returns the digital signature (big integer radix 32) of the class name which contains the key to encrypt the results of this task.
   *  @return the digital signature (big integer radix 32) of the class name which contains the key to encrypt the results of this task.
  **/
  public String getEncryptionSignature() {
    return encryptionSignature;
  }

  /**
   *  Returns the decryption number for the Decryptor (half-certified Diffie-Hellman protocol).
   *  @return the decryption number for the Decryptor (half-certified Diffie-Hellman protocol).
  **/
  public BigInteger getDecryptionNumber() {
    return decryptionNumber;
  }

  /**
   *  Returns an object which processes the request for work units of the task.
   *  @return an object which processes the request for work units of the task or <code>null</null> if it is not defined.
  **/
  public TaskRequestWorkUnitProcessor getRequestWorkUnitProcessor() {
    return requestProcessor;
  }

  /**
   *  Returns an object which processes the task results.
   *  @return an object which processes the task results or <code>null</null> if it is not defined.
  **/
  public TaskResultProcessor getResultProcessor() {
    return resultProcessor;
  }

  /**
   *  Returns the task specific parameters.
   *  @return the task specific parameters.
  **/
  public String getTaskParameters() {
    return parameters;
  }

  /**
   *  Returns the decrypted parameters of the HttpRequest.
   *  @return the decrypted parameters of the HttpRequest.
  **/
  public Map getParameter() {
    return parameter;
  }

  /**
   *  Returns a value for a key from the specified map with parameters.
   *  @param key key
   *  @return value for a key from the specified map with parameters; <code>null</code> if no value was found.
  **/
  public String getParameter(String key) {
    String[] values = (parameter == null)? null : (String[])parameter.get(key);
    return (values == null || values.length == 0)? null : values[0];
  }

  /**
   *  Returns the default decryption number for the half-certified Diffie-Hellman protocol.
   *  @param servlet surrounding servlet
   *  @return the default decryption number for the half-certified Diffie-Hellman protocol.
   *  @exception  SQLException  if a database access error occurs.
  **/
  static BigInteger getDefaultDecryptionNumber(DispatcherServlet servlet) throws ServletException, SQLException {
    if (defaultDecryptionNumber == null) {
      Connection con = null;
      Statement stmt = null;
      try {
        con  = servlet.getConnection();
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT decryption_number FROM zeta.task WHERE id=0");
        if (rs.next()) {
          defaultDecryptionNumber = new BigInteger(rs.getString(1), 32);
        }
        rs.close();
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(con);
      }
    }
    return defaultDecryptionNumber;
  }

  private static Class loadClass(String className) {
    Class clazz = null;
    if (className != null) {
      try {
        clazz = Class.forName(className);
      } catch (Exception e) {
        ThrowableHandler.handle(e);
      }
    }
    return clazz;
  }

  /**
   *  The bytes that make up the class data which contains the key to encrypt the results of this task.
  **/
  private byte[] encryptionClass;

  /**
   *  Digital signature of the class name which contains the key to encrypt the results of this task.
   *  A big integer radix 32.
  **/
  private String encryptionSignature;

  /**
   *  Decryption number for the Decryptor (half-certified Diffie-Hellman protocol).
  **/
  private BigInteger decryptionNumber;

  /**
   *  An object which processes the request for work units of the task.
  **/
  private TaskRequestWorkUnitProcessor requestProcessor;

  /**
   *  An object for the specified class name which processes the task results.
  **/
  private TaskResultProcessor resultProcessor;

  private String clientTaskClassName;

  private String parameters;

  private WorkUnitVerifier workUnitVerifier;

  /**
   *  Specifies in minutes the deadline when an active work unit expires and a redistributes will be invoked.
   *  The active work unit is reserved for a connected resource, i.e. that the resource has connected the server with another request up to the deadline.
  **/
  private int redistributionConnected;

  /**
   *  Specifies in minutes the deadline when an active work unit expires and a redistributes will be invoked.
   *  The active work unit is reserved for an unconnected resource, i.e. that the resource has not connected the server with any request up to the deadline.
  **/
  private int redistributionUnconnected;

  /**
   *  Parameters of the HttpRequest.
  **/
  private Map parameter;

  /**
   *  The default decryption number for the half-certified Diffie-Hellman protocol.
  **/
  private static BigInteger defaultDecryptionNumber = null;
}
