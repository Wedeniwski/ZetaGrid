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

package zeta;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import zeta.util.Properties;

/**
 *  Manages the used resources for the computation.
 *
 *  @version 2.0, August 6, 2005
**/
public class ZetaCalc implements Runnable {

  /**
   *  Constructs the environment how to use the resources for the defined task.
  **/
  public ZetaCalc() {
    try {
      properties = new Properties(Properties.ZETA_CFG, Properties.DEFAULT_CFG);
    } catch (IOException e) {
      ZetaInfo.handle(e);
      return;
    }
    ZetaInfo.init(properties);
    exitFile = getExitFile();
    try {
      String filename = InetAddress.getLocalHost().getHostName().toLowerCase() + ".tasks";
      task = TaskManager.getInstance(filename).getClientTask(properties.get("task"));
    } catch (Exception e) {
      ZetaInfo.handle(e);
      return;
    }
    checkExit();
    workUnitManager = task.getWorkUnitManager();
    try {
      Runtime.getRuntime().addShutdownHook(new Thread() {   // prerequisite: JRE 1.3
        public void run() {
          int wait = task.stop();
          try {
            Thread.sleep(wait);
          } catch (InterruptedException ex) {
          }
        }
      });
    } catch (Throwable t) {
      ZetaInfo.handle(t);
    }
    String standardOutput = properties.get("standard.output");
    task.setEnableStandardOutput(standardOutput == null || !standardOutput.equals("false"));
    task.setResources(properties.get("resources"));
  }

  /**
   *  Starts one thread to calculate per defined processor.
   *  Every thread retrieves and sumbits work units for the defined task.
  **/
  public void run() {
    if (properties.get("exit.time", 0) > 0) {
      final long stopTimeMillis = System.currentTimeMillis() + properties.get("exit.time", 0)*1000;
      Thread thread = new Thread() {
        public void run() {
          while (stopTimeMillis > System.currentTimeMillis()) {
            try {
              Thread.sleep(stopTimeMillis-System.currentTimeMillis());
            } catch (InterruptedException ex) {
            }
          }
          System.exit(1);
        }
      };
      thread.start();
    }
    File file = new File("nohup.out");
    file.delete();
    String activeAt = properties.get("active.at");
    int defaultCPUUsage = properties.get("processors.usage", 100);
    if (properties.get("processors.usage") == null) { // ToDo: remove
      defaultCPUUsage = properties.get("sleep", 0);
      if (defaultCPUUsage <= 0 || defaultCPUUsage > 12000) {
        defaultCPUUsage = 100;
      } else {
        defaultCPUUsage = (int)((defaultCPUUsage*100)/12001.0);
      }
    }
    final WorkloadScheduler workloadScheduler = new WorkloadScheduler(activeAt, defaultCPUUsage);
    if (!workloadScheduler.isAlwaysActive()) {
      Thread thread = new Thread() {
        public void run() {
          while (true) {
            try {
              long millis = workloadScheduler.getTimeMillisToNextTimeframe();
              if (computationIsActive > 0) {
                if (workloadScheduler.isActive()) {
                  StringBuffer buffer = new StringBuffer(150);
                  buffer.append("Continue computing");
                  appendTimeInfo(millis, buffer);
                  ZetaInfo.write(buffer.toString());
                } else {
                  ZetaInfo.write("The workload scheduler stops the computation.");
                  task.stop();
                }
              }
              Thread.sleep(millis);
            } catch (InterruptedException ex) {
            } catch (Throwable t) { // internal error occur
              ZetaInfo.handle(t);
              try {
                Thread.sleep(60000);  // internal error, wait 1 minute
              } catch (InterruptedException ex) {
              }
            }
          }
        }
      };
      thread.start();
    }
    final int processors = properties.get("processors", 1);
    for (int i = 0; i < processors || i == 0; ++i) {
      Thread thread = new Thread() {
        public void run() {
          WorkUnit workUnit = null;
          while (true) {
            long millis = workloadScheduler.getTimeMillisToNextTimeframe();
            if (workloadScheduler.isActive()) {
              if (this == outputThread) {
                StringBuffer buffer = new StringBuffer(150);
                if (processors == 1) {
                  buffer.append("Computing on 1 processor");
                } else {
                  buffer.append("Computing on ");
                  buffer.append(processors);
                  buffer.append(" processors");
                }
                if (!workloadScheduler.isAlwaysActive()) {
                  appendTimeInfo(millis, buffer);
                }
                ZetaInfo.write(buffer.toString());
              }
              if (workUnit == null) {
                workUnit = workUnitManager.getWorkUnit();
              }
              ++computationIsActive;
              if (workUnit != null && task.start(workUnit, workloadScheduler.getCPUUsage()) == 0) {
                --computationIsActive;
                checkExit();
                workUnitManager.submit(workUnit, (processors == 0), true);
                workUnit = null;
              } else {
                if (workUnit != null) {
                  ZetaInfo.write("The computation was stopped.");
                }
                --computationIsActive;
                checkExit();
                workUnitManager.submit(null, (processors == 0), true); // safe for a bad case
                try {
                  Thread.sleep(60000);  // wait 1 minute
                } catch (InterruptedException ex) {
                }
                checkExit();
              }
            } else {
              long stopTimeMillis = System.currentTimeMillis() + millis;
              if (this == outputThread) {
                StringBuffer buffer = new StringBuffer(150);
                buffer.append("The computation paused");
                appendTimeInfo(millis, buffer);
                ZetaInfo.write(buffer.toString());
              }
              if (exitFile != null) {
                while (stopTimeMillis > System.currentTimeMillis()) {
                  try {
                    Thread.sleep(Math.min(60000, stopTimeMillis-System.currentTimeMillis()));
                  } catch (InterruptedException ex) {
                  }
                  checkExit();
                }
              } else {
                try {
                  Thread.sleep(millis);
                } catch (InterruptedException ex) {
                }
              }
            }
          }
        }
      };
      if (i == 0) {
        outputThread = thread;
      }
      thread.start();
    }
  }

  /**
   *  Appends time information.
  **/
  private void appendTimeInfo(long millis, StringBuffer buffer) {
    long t = millis/60000;
    if (t == 60) {
      buffer.append(" at least 1 hour.");
    } else if (t > 60) {
      if (t%60 == 0) {
        buffer.append(" at least ");
        buffer.append(t/60);
        buffer.append(" hours.");
      } else if (t < 120) {
        buffer.append(" at least 1 hour and ");
        buffer.append(t%60);
        buffer.append(" minutes.");
      } else {
        buffer.append(" at least ");
        buffer.append(t/60);
        buffer.append(" hours and ");
        buffer.append(t%60);
        buffer.append(" minutes.");
      }
    } else if (t > 1) {
      buffer.append(" at least ");
      buffer.append(t);
      buffer.append(" minutes.");
    } else {
      buffer.append(" about 1 minute.");
    }
  }

  /**
   *  Checks if the specifed 'exit.filename' exists and terminate the computation and the client.
  **/
  private void checkExit() {
    if (exitFile != null && exitFile.exists()) {
      exitFile.delete();
      task.stop();
      System.exit(1);
    }
  }

  private File getExitFile() {
    String s = properties.get("exit.filename");
    return (s == null || s.length() == 0)? null : new File(s);
  }

  /**
   *  Contains a persistent set of the ZetaGrid properties.
  **/
  private Properties properties;

  private ClientTask task;
  private WorkUnitManager workUnitManager;
  private Thread outputThread = null;
  private int computationIsActive = 0;
  private File exitFile;
}
