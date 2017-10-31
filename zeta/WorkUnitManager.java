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
     H. Haddorp
     S. Wedeniwski
--*/

package zeta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import zeta.monitor.MonitoredOutputStream;
import zeta.monitor.MonitoredURLStreamHandler;
import zeta.util.Base64;
import zeta.util.Properties;
import zeta.util.StreamUtils;

/**
 *  Manages the local work units, i.e. requests work units from the server and submits completed work units to the server.
 *  Optionally, the communication to the server can be asynchronous to the computation.
 *  The file extension '.$$$' will be used for temporary files during encryption.
 *
 *  @version 2.0, August 6, 2005
**/
public class WorkUnitManager {

  public WorkUnitManager(ClientTask task) {
    this.task = task;
    try {
      possibleWorkUnits = new ArrayList(10);
      activeWorkUnits = new ArrayList(10);
      storeWorkUnits = new ArrayList(10);
      properties = new Properties(Properties.ZETA_CFG, Properties.DEFAULT_CFG);
    } catch (Exception e) {
      ZetaInfo.handle(e);
      throw new IllegalArgumentException(e.getMessage());
    }
    retrievePossibleWorkUnits();
    cleanWorkUnits();
    if (!ZetaClient.isUnknownHostExceptionOccur()) {
      submit(null, false, true);
    }
    completedLocalWorkUnits = numberOfCompletedLocalWorkUnits();
    exitAfterWorkUnits = properties.get("exit", 0);
    exitAfterWorkUnitsActive = (exitAfterWorkUnits > 0);
    int transferDetect = properties.get("transfer.detect", 0);
    if (transferDetect > 0 && properties.get("transfer.asynchronous", "false").equals("true")) {
      Thread t = new Thread() {
        public void run() {
          while (true) {
            int transferDetect = Math.max(properties.get("transfer.detect", 0), 300);
            if (!properties.get("transfer.asynchronous", "false").equals("true") || transferDetect == 0) {
              try {
                Thread.sleep(60000);  // wait 1 minute
              } catch (InterruptedException ex) {
              }
            } else {
              if (numberOfCompletedLocalWorkUnits() > 0) {
                boolean error = true;
                HttpURLConnection connection = null;
                try {
                  URL url = new URL("http", properties.get("host.name", "www.zetagrid.net"), properties.get("host.port", 80),
                                    properties.get("resultURL", "/servlet/service/result"));
                  connection = (HttpURLConnection)url.openConnection();
                  connection.setUseCaches(false);
                  connection.setRequestMethod("GET");
                  connection.setDoInput(true);
                  connection.connect();
                  error = false;
                } catch (Exception e) {
                  error = true;
                } finally {
                  if (connection != null) {
                    connection.disconnect();
                  }
                }
                if (!error) {
                  submit(null, false, false);
                }
              }
              try {
                Thread.sleep(transferDetect*1000);
              } catch (InterruptedException ex) {
              }
            }
          }
        }
      };
      t.start();
    }
  }

  /**
   *  Returns the next work unit for the computation.
   *  @return the next work unit for the computation.
  **/
  public WorkUnit getWorkUnit() {
    WorkUnit workUnit = null;
    synchronized (activeWorkUnits) {
      cleanWorkUnits();
      try {
        String[] filenames = new File(".").list();
        if (filenames != null) {
          List workUnitCandidates = task.createWorkUnits(filenames);
          boolean offline = possibleWorkUnits.isEmpty();
          for (int i = 0, l = workUnitCandidates.size(); i < l; ++i) {
            WorkUnit workUnitCandidate = (WorkUnit)workUnitCandidates.get(i);
            if (!workUnitCandidate.isCompleted()) {
              if ((workUnit == null || compare(workUnitCandidate, workUnit) < 0) && (offline || possibleWorkUnits.contains(workUnitCandidate)) && !activeWorkUnits.contains(workUnitCandidate)) {
                workUnit = workUnitCandidate;
              }
            }
          }
          if (workUnit != null) {
            activeWorkUnits.add(workUnit);
          }
        }
      } catch (Exception e) {
        ZetaInfo.handle(e);
        return null;
      }
    }
    return workUnit;
  }

  public void submit(final WorkUnit workUnit, final boolean exitAfterTransfer, final boolean outputExceptions) {
    synchronized (activeWorkUnits) {
      if (workUnit != null && !activeWorkUnits.contains(workUnit) || storeWorkUnits.contains(workUnit)) {
        return;
      }
      storeWorkUnits.add(workUnit);
    }
    Thread t = new Thread() {
      public void run() {
        try {
          if (workUnit == null) {
            int storedWorkUnits = storeCompletedWorkUnits(outputExceptions);
            if (storedWorkUnits > 0) {
              if (exitAfterTransfer) {
                System.exit(1);
              }
              if (exitAfterWorkUnitsActive) {
                exitAfterWorkUnits -= storedWorkUnits;
                if (exitAfterWorkUnits <= numberOfCompletedLocalWorkUnits()-completedLocalWorkUnits-getNumberOfActiveWorkUnits()) {
                  System.exit(1);
                }
              }
            }
            if ((outputExceptions || storedWorkUnits > 0) && !requestNewWorkUnits()) {
              ZetaInfo.write(" ");
              try {
                Thread.sleep(wait);
              } catch (InterruptedException ex) {
              }
              wait = (wait == 60000)? 900000 : 60000;
            } else {
              ZetaInfo.write(" ");
            }
          } else {
            wait = 60000;
            if (storeData(workUnit, outputExceptions)) {
              if (exitAfterTransfer || exitAfterWorkUnitsActive && --exitAfterWorkUnits <= numberOfCompletedLocalWorkUnits()-completedLocalWorkUnits-getNumberOfActiveWorkUnits()) {
                System.exit(1);
              }
              requestNewWorkUnits();
              ZetaInfo.write(" ");
            }
          }
        } finally {
          synchronized (activeWorkUnits) {
            if (workUnit != null) {
              activeWorkUnits.remove(workUnit);
            }
            storeWorkUnits.remove(workUnit);
          }
        }
      }
    };
    if (properties.get("transfer.asynchronous", "false").equals("true")) {
      t.start();
    } else {
      t.run();
    }
  }

  /**
   *  A work unit with a log file size greater than 0 will be preferred.
   *  Next the order in the local work unit file defines the priority.
   *  Last a smaller work unit ID will be preferred.
  **/
  private int compare(WorkUnit workUnit1, WorkUnit workUnit2) {
    if (workUnit1 == null && workUnit2 != null) {
      return -1;
    }
    if (workUnit1 != null && workUnit2 == null) {
      return 1;
    }
    if (workUnit1 == null && workUnit2 == null) {
      return 0;
    }
    long wl1 = new File(workUnit1.getLogFileName()).length();
    long wl2 = new File(workUnit2.getLogFileName()).length();
    if (wl1 > 0 && wl2 == 0) {
      return -1;
    }
    if (wl1 == 0 && wl2 > 0) {
      return 1;
    }
    int i1 = possibleWorkUnits.indexOf(workUnit1);
    int i2 = possibleWorkUnits.indexOf(workUnit2);
    if (i1 >= 0 && i2 >= 0) {
      if (i1 == i2) {
        return 0;
      }
      return (i1 < i2)? -1 : 1;
    }
    if (i1 >= 0) {
      return -1;
    }
    if (i2 >= 0) {
      return 1;
    }
    if (workUnit1.getWorkUnitId() < workUnit2.getWorkUnitId()) {
      return -1;
    }
    if (workUnit1.getWorkUnitId() > workUnit2.getWorkUnitId()) {
      return 1;
    }
    return 0;
  }

  private boolean storeData(WorkUnit workUnit, boolean outputExceptions) {
    synchronized (WorkUnitManager.class) {
      if (!workUnit.isCompleted()) {
        return false;
      }
      boolean error = true;
      for (int tries = 0; error && tries < 2; ++tries) {
        error = false;
        SecureRandom rnd = new SecureRandom();
        rnd.setSeed(System.currentTimeMillis());
        String[] filenames = workUnit.containsFileNames();
        File largestFile = null;
        for (int i = 0; i < filenames.length; ++i) {
          File file = new File(filenames[i]);
          if (largestFile == null || largestFile.length() < file.length()) {
            largestFile = file;
          }
        }
        HttpURLConnection connection = null;
        OutputStream out = null;
        try {
          String hostname = properties.get("host.name", "www.zetagrid.net");
          InetAddress localHost = InetAddress.getLocalHost();
          String paramString = "task=" + URLEncoder.encode(task.getName())
                             + "&work_unit_id=" + workUnit.getWorkUnitId()
                             + "&user=" + URLEncoder.encode(properties.get("name"))
                             + "&hostname=" + URLEncoder.encode(localHost.getHostName().toLowerCase())
                             + "&hostaddr=" + URLEncoder.encode(localHost.getHostAddress())
                             + "&key=" + URLEncoder.encode(ZetaClient.getKey());
          paramString = ZetaClient.encryptURLFile(paramString);
          ZetaInfo.write("Checking connection to server " + hostname);
          URL url = new URL("http", hostname, properties.get("host.port", 80), properties.get("resultURL", "/servlet/service/result"));
          connection = (HttpURLConnection)url.openConnection();
          connection.setUseCaches(false);
          connection.setRequestProperty("Content-Length", "2");
          connection.setRequestProperty("Content-Type", "application/octet-stream");
          connection.setRequestProperty("Param-String",  paramString); // not a standard property !!
          connection.setDoOutput(true);
          connection.setRequestMethod("POST");
          connection.connect();
          out = connection.getOutputStream();
          ZetaInfo.write("Checking availability of the services at " + hostname);
          out.write(new byte[] { 1, 2 });
          out.flush();
          int code = connection.getResponseCode();
          if (code == HttpURLConnection.HTTP_UNAVAILABLE) {
            error = true;
            continue;
          }
          if (code != HttpURLConnection.HTTP_ACCEPTED && code != HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
            ZetaInfo.write("The services at " + hostname + " are not available!");
            return false;
          }
          byte[] keyEncryptorClass = null;
          String keySignature = null;
          // ToDo: generate tmp file using zeta.WorkUnit.getWorkUnitFileName() if the files are too large; make it configurable by threshold
          ByteArrayOutputStream resultOut = new ByteArrayOutputStream(4 * 1024 * 1024);
          if (code == HttpURLConnection.HTTP_ACCEPTED) {
            try {
              // retrieve the encryptor with its digital signature
              StreamUtils.writeData(connection.getInputStream(), resultOut, false, true);
              ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(Base64.decode(resultOut.toString("UTF-8"))));
              while (true) {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null) {
                  break;
                }
                String name = entry.getName();
                if (name.equals("className")) {
                  resultOut.reset();
                  StreamUtils.writeData(zip, resultOut, false, true);
                  keyEncryptorClass = resultOut.toByteArray();
                } else if (name.equals("signature")) {
                  resultOut.reset();
                  StreamUtils.writeData(zip, resultOut, false, true);
                  keySignature = resultOut.toString("UTF-8");
                } else {
                  throw new IOException("The signature contains a not valid value '" + name + '\'');
                }
              }
            } catch (IOException ioe) {
              ZetaInfo.handle(ioe);
              keyEncryptorClass = null;
              keySignature = null;
            }
          }
          connection.disconnect();
          connection = null;
          if (keyEncryptorClass == null || keyEncryptorClass.length == 0) {
            ZetaInfo.write("No encryption key is available at " + hostname);
            return false;
          }
          if (keySignature == null || keySignature.length() == 0) {
            ZetaInfo.write("No digital signature is available for the encryption key at " + hostname);
            return false;
          }
          // check signature of the encryption class
          ZetaInfo.write("Check digital signature of the encryption class");
          FileInputStream fin = null;
          FileOutputStream fileOut = null;
          try {
            if (!ZetaClient.verify(null, keySignature, keyEncryptorClass)) {
              throw new IOException("Wrong signature for the encryption class");
            }
            fin = new FileInputStream(largestFile);
            if (!StreamUtils.checkAvailDiskSpace(fin, new File(largestFile.getName() + ".$$$"))) {
              throw new IOException("Hard disk is full!");
            }
            resultOut.reset();
            ZetaInfo.write("Encrypting work unit " + workUnit.getWorkUnitId());
            ZipOutputStream zip = new ZipOutputStream(resultOut);
            zip.setLevel(Deflater.NO_COMPRESSION);
            for (int i = 0; i < filenames.length; ++i) {    // ToDo: status info
              String name = filenames[i] + ".$$$";
              ZetaClient.encrypt(rnd.nextInt(1024)+5, keyEncryptorClass, filenames[i], name);
              zip.putNextEntry(new ZipEntry(name));
              StreamUtils.writeData(new FileInputStream(name), zip, true, false);
              new File(name).delete();
            }
            zip.close();
          } finally {
            resultOut.close();
            StreamUtils.close(fin);
          }
          // Connecting server
          ZetaInfo.write("Connecting server " + hostname);
          MonitoredOutputStream.setWorkUnitId(workUnit.getWorkUnitId());
          url = new URL("http", hostname, properties.get("host.port", 80), properties.get("resultURL", "/servlet/service/result"), new MonitoredURLStreamHandler());
          connection = (HttpURLConnection)url.openConnection();
          connection.setUseCaches(false);
          byte[] resultBuffer = resultOut.toByteArray();
          connection.setRequestProperty("Content-Length", Long.toString(resultBuffer.length));
          connection.setRequestProperty("Content-Type", "application/octet-stream");
          connection.setRequestProperty("Param-String",  paramString); // not a standard property !!
          connection.setDoOutput(true);
          connection.setRequestMethod("POST");
          connection.connect();
          out = connection.getOutputStream();
          ZetaInfo.write("Server " + hostname + " connected");
          out.write(resultBuffer);
          out.flush();
          ZetaInfo.write("Transfering work unit " + workUnit.getWorkUnitId());
          resultBuffer = null;
          code = connection.getResponseCode();
          if (code != HttpURLConnection.HTTP_OK) {
            ZetaInfo.write("Could not store result: return code " + code);
            System.err.println("Could not store result: return code " + code);
            error = true;
            if (code == HttpURLConnection.HTTP_INTERNAL_ERROR || code == HttpURLConnection.HTTP_UNAVAILABLE) {
              continue;
            }
            return false;
          }
          ZetaInfo.write("Work unit " + workUnit.getWorkUnitId() + " successfully transferred");
        } catch (UnknownHostException uhe) {
          error = true;
          tries = 1;
          if (outputExceptions) {
            ZetaInfo.write("Unknown host: " + uhe.getMessage());
          }
        } catch (IOException e) {
          error = true;
          try {
            if (connection == null || connection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
              tries = 1;
            }
          } catch (IOException ioe) {
          }
          if (tries == 1 && outputExceptions) {
            ZetaInfo.handle(e);
          }
          try {
            Thread.sleep(4000);
          } catch (InterruptedException ex) {
          }
        } finally {
          StreamUtils.close(out);
          if (connection != null) {
            connection.disconnect();
            connection = null;
          }
          for (int i = 0; i < filenames.length; ++i) {
            if (!error) {
              new File(filenames[i]).delete();
            }
            new File(filenames[i] + ".$$$").delete();
          }
          cleanWorkUnits();
          System.gc();
        }
      }
      return !error;
    }
  }

  /**
   *  @return number of completed work units which are stored successfully at the server, -1 if an error occurs and no work unit is stored
  **/
  private int storeCompletedWorkUnits(boolean outputExceptions) {
    synchronized (WorkUnitManager.class) {
      int storedWorkUnits = 0;
      try {
        String[] filenames = new File(".").list();
        if (filenames != null) {
          List workUnits = task.createWorkUnits(filenames);
          int l = workUnits.size();
          for (int i = 0; i < l; ++i) {
            WorkUnit workUnit = (WorkUnit)workUnits.get(i);
            if (workUnit.isValid() && workUnit.isCompleted()) {
              String[] workUnitFilenames = workUnit.containsFileNames();
              int j = 0;
              while (j < workUnitFilenames.length && new File(workUnitFilenames[j]).exists()) {
                ++j;
              }
              if (j == workUnitFilenames.length && storeData(workUnit, outputExceptions) && ++storedWorkUnits == 0) {
                storedWorkUnits = 1;
              }
            }
          }
        }
      } catch (Exception e) {
        if (outputExceptions) {
          ZetaInfo.handle(e);
        }
        if (storedWorkUnits == 0) {
          storedWorkUnits = -1;
        }
      }
      return storedWorkUnits;
    }
  }

  private boolean requestNewWorkUnits() {
    synchronized (WorkUnitManager.class) {
      if (properties.get("processors", 1) <= 0) {
        return false;
      }
      int workUnits = properties.get("work_units", 1);
      // Attention: Work units can be redistributed.
      if (workUnits <= numberOfLocalWorkUnits(2*24*60*60*1000)) {  // do not request new work units if there are enough local work units
        retrievePossibleWorkUnits();
        return true;
      }
      String workUnitSize = properties.get("work_unit_size", "m");
      String eMail = properties.get("eMail");
      if (eMail == null) {
        eMail = "";
      }
      String messages = properties.get("messages", null);
      if (messages == null) {
        messages = "";
      } else {
        messages = (messages.equals("true"))? "&messages=true" : "&messages=false";
      }
      String team = properties.get("team");
      if (team == null) {
        team = "";
      }
      String version = task.getVersion();
      for (int tries = 0; tries < 2; ++tries) {
        HttpURLConnection connection = null;
        try {
          InetAddress localHost = InetAddress.getLocalHost();
          ZetaInfo.write("Requesting new work units");
          String urlFile = properties.get("requestURL", "/servlet/service/requestWorkUnit")
                            + "?user=" + URLEncoder.encode(properties.get("name"))
                            + "&email=" + URLEncoder.encode(eMail)
                            + messages
                            + "&team=" + URLEncoder.encode(team)
                            + "&task=" + URLEncoder.encode(task.getName())
                            + "&size=" + URLEncoder.encode(workUnitSize)
                            + "&hostname=" + URLEncoder.encode(localHost.getHostName().toLowerCase())
                            + "&hostaddr=" + URLEncoder.encode(localHost.getHostAddress())
                            + "&key=" + URLEncoder.encode(ZetaClient.getKey())
                            + "&version=" + URLEncoder.encode(version)
                            + "&work_units=" + workUnits
                            + "&os_name=" + URLEncoder.encode(System.getProperty("os.name", "?"))
                            + "&os_version=" + URLEncoder.encode(System.getProperty("os.version", "?"))
                            + "&os_arch=" + URLEncoder.encode(System.getProperty("os.arch", "?"))
                            + "&processors=" + properties.get("processors", 1);
          urlFile = ZetaClient.encryptURLFile(urlFile);
          URL url = new URL("http", properties.get("host.name", "www.zetagrid.net"), properties.get("host.port", 80), urlFile);
          connection = (HttpURLConnection)url.openConnection();
          connection.setUseCaches(false);
          connection.setRequestMethod("GET");
          connection.setDoInput(true);
          connection.connect();
          ByteArrayOutputStream parameters = new ByteArrayOutputStream();
          StreamUtils.writeData(connection.getInputStream(), parameters, false, true);
          storePossibleWorkUnits(parameters.toString("UTF-8"));
          return true;
        } catch (UnknownHostException uhe) {
          tries = 1;
          ZetaInfo.write("Unknown host: " + uhe.getMessage());
        } catch (MalformedURLException e) {
          try {
            if (connection == null || connection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
              tries = 1;
            }
          } catch (IOException ioe) {
          }
          if (tries == 1) {
            ZetaInfo.handle(e);
          }
          try {
            Thread.sleep(4000);
          } catch (InterruptedException ex) {
          }
        } catch (IOException e) {
          try {
            if (connection == null || connection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
              tries = 1;
            }
          } catch (IOException ioe) {
          }
          if (tries == 1) {
            ZetaInfo.handle(e);
          }
          try {
            Thread.sleep(4000);
          } catch (InterruptedException ex) {
          }
        } catch (Exception e) {
          ZetaInfo.handle(e);
          return false;
        } finally {
          if (connection != null) {
            connection.disconnect();
            connection = null;
          }
        }
      }
      return false;
    }
  }

  /**
   *  Returns the number of work units which are currently computed.
   *  @return the number of work units which are currently computed.
  **/
  private int getNumberOfActiveWorkUnits() {
    synchronized (activeWorkUnits) {
      return activeWorkUnits.size();
    }
  }

  /**
   *  Returns the number of local work units which are not older than the specified time.
   *  @return 0 if all local work units are older than the specified time.
  **/
  private int numberOfLocalWorkUnits(long notOlderThanMillis) {
    int count = 0;
    synchronized (activeWorkUnits) {
      try {
        String[] filenames = new File(".").list();
        if (filenames != null) {
          List workUnits = task.createWorkUnits(filenames);
          long currentTime = System.currentTimeMillis();
          boolean offline = possibleWorkUnits.isEmpty();
          for (int i = 0, l = workUnits.size(); i < l; ++i) {
            WorkUnit workUnit = (WorkUnit)workUnits.get(i);
            if (offline || possibleWorkUnits.contains(workUnit)) {
              File file = new File(workUnit.getLogFileName());
              if (currentTime-file.lastModified() < notOlderThanMillis) {
                ++count;
              }
            }
          }
        }
      } catch (Exception e) {
        ZetaInfo.handle(e);
        count = 0;
      }
    }
    return count;
  }

  /**
   *  Returns the number of local work units which are completed.
   *  @return the number of local work units which are completed.
  **/
  private int numberOfCompletedLocalWorkUnits() {
    int count = 0;
    synchronized (activeWorkUnits) {
      try {
        String[] filenames = new File(".").list();
        if (filenames != null) {
          List workUnits = task.createWorkUnits(filenames);
          boolean offline = possibleWorkUnits.isEmpty();
          for (int i = 0, l = workUnits.size(); i < l; ++i) {
            WorkUnit workUnit = (WorkUnit)workUnits.get(i);
            if (workUnit.isCompleted() && (offline || possibleWorkUnits.contains(workUnit))) {
              ++count;
            }
          }
        }
      } catch (Exception e) {
        ZetaInfo.handle(e);
        count = 0;
      }
    }
    return count;
  }

  /**
   *  Deletes local work unit files.
   *  A log file will be deleted if it is not inside the local work unit list
   *  or if it is completed but at least one result file is lost.
   *  Every work unit must have a log file otherwise all its result files will be deleted.
  **/
  private void cleanWorkUnits() {
    try {
      String[] filenames = new File(".").list();
      if (filenames != null) {
        WorkUnit workUnit = null;
        List workUnits = task.createWorkUnits(filenames);
        boolean offline = possibleWorkUnits.isEmpty();
        for (int i = 0, l = workUnits.size(); i < l; ++i) {
          workUnit = (WorkUnit)workUnits.get(i);
          if (!offline && !possibleWorkUnits.contains(workUnit)) { // if it is not inside the local work unit list
            String[] f = workUnit.containsFileNames();
            for (int j = 0; j < f.length; ++j) {
              new File(f[j]).delete();
            }
          } else if (workUnit.isCompleted()) {  // if it is completed but at least one result file is lost
            String[] f = workUnit.containsFileNames();
            for (int j = 0; j < f.length; ++j) {
              File file = new File(f[j]);
              if (!file.exists()) { // error occur (work unit is not complete)
                for (j = 0; j < f.length; ++j) {
                  new File(f[j]).delete();
                }
              }
            }
          }
        }
        if (workUnit != null) {  // Every work unit must have a log file otherwise all its result files will be deleted.
          for (int i = 0; i < filenames.length; ++i) {
            if (workUnit.isPartOfAnyWorkUnit(filenames[i])) {
              int j = 0;
              int l = workUnits.size();
              for (; j < l; ++j) {
                workUnit = (WorkUnit)workUnits.get(j);
                String[] f = workUnit.containsFileNames();
                int k = f.length;
                while (--k >= 0 && !filenames[i].equals(f[k]));
                if (k >= 0) {
                  break;
                }
              }
              if (j == l) {  // delete files which are part of an invalid work unit
                new File(filenames[i]).delete();
              }
            }
          }
        }
      }
    } catch (Exception e) {
      ZetaInfo.handle(e);
    }
  }

  /**
   *  Reads the local work unit list.
  **/
  private void retrievePossibleWorkUnits() {
    synchronized (activeWorkUnits) {
      possibleWorkUnits.clear();
      BufferedReader reader = null;
      try {
        List filenameList = new ArrayList(10);
        reader = new BufferedReader(new FileReader(InetAddress.getLocalHost().getHostName() + ".tmp"));
        while (true) {
          String line = reader.readLine();
          if (line == null) {
            break;
          }
          filenameList.add(line);
        }
        String[] filenames = new String[filenameList.size()];
        for (int i = filenames.length-1; i >= 0; --i) {
          filenames[i] = (String)filenameList.get(i);
        }
        possibleWorkUnits = task.createWorkUnits(filenames);
      } catch (FileNotFoundException e) {
      } catch (Exception e) {
        ZetaInfo.handle(e);
      } finally {
        StreamUtils.close(reader);
      }
    }
  }

  private void storePossibleWorkUnits(String workUnitsParameters) {
    synchronized (activeWorkUnits) {
      BufferedWriter writer = null;
      try {
        possibleWorkUnits = task.createWorkUnits(workUnitsParameters);
        writer = new BufferedWriter(new FileWriter(InetAddress.getLocalHost().getHostName() + ".tmp"));
        Iterator i = possibleWorkUnits.iterator();
        while (i.hasNext()) {
          writer.write(((WorkUnit)i.next()).getLogFileName());
          writer.newLine();
        }
      } catch (Exception ioe) {
        ZetaInfo.handle(ioe);
      } finally {
        StreamUtils.close(writer);
        // delete all other work units
        cleanWorkUnits();
      }
    }
  }

  /**
   *  Contains a persistent set of the ZetaGrid properties.
  **/
  private Properties properties;

  private ClientTask task;

  /**
   *  The work unit list.
  **/
  private List possibleWorkUnits;

  /**
   *  Contains the work units which are currently computed.
   *  The size of this container is less or equal to the number of specified processors.
  **/
  private List activeWorkUnits;

  /**
   *  Contains the work units which are in the submission process.
  **/
  private List storeWorkUnits;
  private int completedLocalWorkUnits;
  private int exitAfterWorkUnits = 0;
  private boolean exitAfterWorkUnitsActive = false;
  private int wait = 60000;
}
