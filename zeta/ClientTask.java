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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import zeta.util.StreamUtils;

/**
 *  The client side task interface for the ZetaGrid framework.
 *
 *  @version 2.0, August 6, 2005
**/
public abstract class ClientTask extends Task {
  /**
   *  Returns the version of the task.
   *  The version should not contain more than 4 characters.
   *  @return the version of the task.
  **/
  public abstract String getVersion();

  /**
   *  Sets the available resources for this task.
   *  @param resources the available resources for this task.
  **/
  public abstract void setResources(String resources);

  /**
   *  Enables the standard output stream for the computation.
   *  @param enableStandardOutput <code>true</code> to enable the standard output stream for the computation.
  **/
  public abstract void setEnableStandardOutput(boolean enableStandardOutput);

  /**
   *  Starts the computation of the specified work unit.
   *  The computation should not consume more than the specified CPU usage.
   *  @param workUnit the work unit to compute
   *  @param cpuUsage available CPU for this computation
   *  @return 0 if and only if the computation was successfully completed.
  **/
  public abstract int start(WorkUnit workUnit, int cpuUsage);

  /**
   *  Can also be called from another thread or process to stop the computation.
   *  @return in milliseconds how long the client should before it terminate all processes
  **/
  public abstract int stop();

  /**
   *  Returns a list of valid work units which are defined by the specified file names.
   *  @param fileNames names of files  in the working root directory
   *  @return a list of valid work units which are defined by the specified file names.
   *  @see zeta.WorkUnit#encodeParameters(String)
  **/
  public abstract List createWorkUnits(String[] fileNames);

  public ClientTask(int id, String name, Class workUnitClass) {
    super(id, name, workUnitClass);
  }

  /**
   *  Returns the work unit manager for this task.
   *  @return the work unit manager for this task.
  **/
  public WorkUnitManager getWorkUnitManager() {
    if (workUnitManager == null) {
      workUnitManager = new WorkUnitManager(this);
    }
    return workUnitManager;
  }

  /**
   *  Returns a list of work units which are defined by the specified parameters.
   *  These parameters come from the server must be decoded. The parameters are encoded by the method
   *  encodeParameters(String) of the class zeta.WorkUnit.
   *  Only valid work units are included in the returned list.
   *  In the default implementation a new logfile will be created for each work unit of the returned list.
   *  The work unit specific parameters are stored in the first line of the logfile,
   *  beginning with "parameters=" and ending with a new line; these parameters are separated by the character ','.
   *  @param parameters of work units
   *  @return a list of work units which are defined by the specified parameters.
   *  @see zeta.WorkUnit#encodeParameters(String)
   *  @see zeta.WorkUnit#isValid()
  **/
  public List createWorkUnits(String parameters) {
    List workUnits = new ArrayList(10);
    int taskId = 0;
    long workUnitId = -1;
    int size = -1;
    String params = null;
    boolean recompute = false;
    BufferedReader reader = null;
    FileWriter writer = null;
    try {
      reader = new BufferedReader(new StringReader(parameters));
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        if (line.trim().length() == 0) {
          taskId = 0;
          workUnitId = -1;
          size = -1;
          params = null;
          recompute = false;
        } else {
          if (line.startsWith("parameters")) {
            params = line.substring(line.indexOf('=') + 1).trim();
          } else if (line.startsWith("task_id")) {
            taskId = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
          } else if (line.startsWith("work_unit_id")) {
            workUnitId = Long.parseLong(line.substring(line.indexOf('=') + 1).trim());
          } else if (line.startsWith("size")) {
            size = Integer.parseInt(line.substring(line.indexOf('=') + 1).trim());
            WorkUnit workUnit = createWorkUnit(workUnitId, size, params, recompute);
            if (workUnit.isValid()) {
              if (params == null) {
                File file = new File(workUnit.getLogFileName());
                file.createNewFile();
              } else {
                writer = new FileWriter(workUnit.getLogFileName());
                writer.write("parameters=" + params + '\n');
                StreamUtils.close(writer);
                writer = null;
              }
              workUnits.add(workUnit);
            }
            taskId = 0;
            workUnitId = -1;
            size = -1;
            params = null;
            recompute = false;
          } else if (line.startsWith("recompute")) {
            recompute = true;
          }
        }
      }
    } catch (IOException ioe) {
      ZetaInfo.handle(ioe);
    } finally {
      StreamUtils.close(reader);
      StreamUtils.close(writer);
    }
    return workUnits;
  }

  /**
   *  Returns <code>null</code> or a valid work unit which are defined by the specified file names. A work unit is only created by a logfile name.
   *  @param fileName name of local files in the working root directory
   *  @return a list of work units which are defined by the local file names.
   *  @see zeta.WorkUnit#getLogFileName()
  **/
  public WorkUnit createWorkUnit(String fileName) {
    WorkUnit dummyWorkUnit = createWorkUnit(1, 1, null, false);
    WorkUnit workUnit = null;
    if (fileName != null && dummyWorkUnit.isPartOfAnyWorkUnit(fileName)) {
      String parameters = null;
      BufferedReader reader = null; // ToDo: cannot be transferred to ServerTask
      try {
        reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        if (line.startsWith("parameters=")) {
          parameters = line.substring(11);
        }
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      } finally {
        StreamUtils.close(reader);
      }
      // ToDo: not fix and transfer to Task (be aware that the ServerTask as well as the ClientTask uses the same implementation if the default is overwritten), check if ".zip" is required!
      int i = dummyWorkUnit.getFileNamePrefix().length();
      int j = fileName.indexOf('_', i);
      if (j > i) {
        try {
          int l = fileName.length()
          long workUnitId = Long.parseLong(fileName.substring(i, j));
          for (i = ++j; j < l && Character.isDigit(fileName.charAt(j)); ++j);
          if (i < j) {
            int size = Integer.parseInt(fileName.substring(i, j));
            dummyWorkUnit = createWorkUnit(workUnitId, size, parameters, false);
            if (dummyWorkUnit.isValid()) {
              workUnit = dummyWorkUnit;
            }
          }
        } catch (NumberFormatException e) {
        }
      }
    }
    return workUnit;
  }
 
  /**
   *  Returns a list of valid work units which are defined by the specified file names. Work units are only created by logfiles names. The returned list can also be empty.
   *  @param fileNames names of local files in the working root directory
   *  @return a list of work units which are defined by the local file names.
   *  @see zeta.WorkUnit#getLogFileName()
  **/
  public List createWorkUnits(String[] fileNames) {
    List workUnits = new ArrayList(fileNames.length);
    for (int i = 0; i < fileNames.length; ++i) {
      WorkUnit workUnit = createWorkUnit(fileNames[i]);
      if (workUnit != null && !workUnits.contains(workUnit)) {
        workUnits.add(workUnit);
      }
    }
    return workUnits;
  }
 
  private WorkUnitManager workUnitManager = null;
}
