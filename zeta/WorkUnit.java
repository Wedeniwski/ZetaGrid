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

package zeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 *  The work unit interface for the ZetaGrid framework.
 *  Do not use the extension '.$$$' for the work unit files. This extension is reserved for the temporary compression.
 *
 *  @version 2.0, August 6, 2005
**/
public abstract class WorkUnit {
  /**
   *  Checks if the work unit is completed.
   *  @return <code>true</code> if the work unit is completed.
  **/
  public abstract boolean isCompleted();
  
  /**
   *  Constructs a new <code>WorkUnit</code>.
  **/
  public WorkUnit(int taskId, long workUnitId, int size, String parameters, boolean recompute) {
    this.taskId = taskId;
    this.workUnitId = workUnitId;
    this.size = size;
    this.parameters = parameters;
    this.recompute = recompute;
  }

  /**
   *  Returns the identifier of the task of this work unit.
   *  @return the identifier of the task of this work unit.
  **/
  public int getTaskId() {
    return taskId;
  }

  /**
   *  Returns the identifier of this work unit.
   *  @return the identifier of this work unit.
  **/
  public long getWorkUnitId() {
    return workUnitId;
  }

  /**
   *  Returns the size of this work unit.
   *  @return the size of this work unit.
  **/
  public int getSize() {
    return size;
  }

  /**
   *  Returns work unit and task specific parameters.
   *  @return work unit and task specific parameters.
  **/
  public String getParameters() {
    return parameters;
  }

  /**
   *  Returns if this work unit will be recomputed.
   *  @return <code>true</code>, if this work unit will be recomputed.
  **/
  public boolean isRecompute() {
    return recompute;
  }

  /**
   *  Checks if this work unit is well defined.
   *  @return <code>true</code>, if this work unit is well defined.
  **/
  public boolean isValid() {
    return (taskId > 0 && workUnitId >= 0 && size > 0);
  }

  /**
   *  Encodes the parameters of the work unit at the server-side.
   *  The parameters must be encoded in one string must be unique concatenated with other work unit parameters.
   *  The parameters should also contains the information: work unit identifier, size, recompute, and identifier of the task.
   *  This default implementation ends with an empty new line to identify the end of the parameter definition of a work unit.
   *  @param taskParameters the task specific parameters; are separated by the character ','
   *  @return the parameters of the work unit which are encoded in one string.
   *  @see zeta.ClientTask#createWorkUnits(String)
  **/
  public String encodeParameters(String taskParameters) {
    int l = (taskParameters == null)? -12 : taskParameters.length;
    StringBuffer buffer = new StringBuffer(12+l+8+9+13+19+6+9+6+9+2);
    if (l > 0) {
      buffer.append("parameters=");
      buffer.append(taskParameters);
      buffer.append('\n');
    }
    buffer.append("task_id=");
    buffer.append(taskId);
    buffer.append("\nwork_unit_id=");
    buffer.append(workUnitId);
    if (recompute) {
      buffer.append("\nrecompute");
    }
    buffer.append("\nsize=");
    buffer.append(size);
    buffer.append("\n\n");
    return buffer.toString();
  }

  /**
   *  Returns the prefix of all file names that associated with the work unit. This prefix is used in the default implementation of the file names.
   *  In the default implementation the task identifier plus an underscore is returned.
   *  @return the prefix of all file names that associated with the work unit.
  **/
  public String getFileNamePrefix() {
    return Integer.toString(getTaskId()) + '_';
  }

  /**
   *  Returns an array with the result file names of the work unit. The default implementation returns just the logfile name.
   *  Do not use the extension '.$$$' for a result file.
   *  @return an array with the result file names of the work unit.
  **/
  public String[] containsFileNames() {
    return new String[] { getLogFileName() };
  }

  /**
   *  Returns the logfile name of the work unit. The default implementation returns the logfile name
   *  <code>getFileNamePrefix() + getWorkUnitId() + '_' + getSize() + ".log"</code>.
   *  Do not use the file extension '.$$$'. At least the work unit identifier must be included in the logfile name
   *  which must be separated (e.g., by an underscore) from other numerical values in the logfile name.
   *  It is recommended to place the work unit identifier as first numerical value after the prefix in the logfile name.
   *  @return the logfile name of the work unit.
  **/
  public String getLogFileName() {
    return getFileNamePrefix() + getWorkUnitId() + '_' + getSize() + ".log";
  }

  /**
   *  Checks if the specified file name is a part of ANY work unit.
   *  It must be part of ANY work unit not only of this work unit object.
   *  In the default implementation the specified argument (file name) is verified if starts with the file name prefix
   *  and ends with the file extension ".log".
   *  @param fileName file name
   *  @return <code>true</code>, if the specified file name is a part of ANY work unit.
   *  @see getFileNamePrefix()
   *  @see containsFileNames()
  **/
  public boolean isPartOfAnyWorkUnit(String fileName) {
    return (fileName.startsWith(getFileNamePrefix()) && fileName.endsWith(".log"));
  }

  /**
   *  Returns the name of the completed work unit. The extension of the file name should be ".zip" .
   *  The default file name for the work unit is the logfile name without the extension ".log" plus the extension ".zip".
   *  The work unit file is a ZIP file which contains all result files of the work unit.
   *  @return the name of the completed work unit.
   *  @see containsFileNames()
  **/
  public String getWorkUnitFileName() {
    String logFileName = getLogFileName();
    if (logFileName.endsWith(".log")) {
      logFileName = logFileName.substring(0, logFileName.length()-4);
    }
    return logFileName + ".zip";
  }

  /**
   *  Compares this work unit to the specified object.
   *  The result is <code>true</code> if and only if the argument is a work unit object that has the same
   *  identifier, size, and task identifier.
   *  @param workUnit the work unit to compare this <code>WorkUnit</code> against.
   *  @return <true> if the <code>WorkUnit</code> are equal; <code>false</code> otherwise.
  **/
  public boolean equals(Object workUnit) {
    if (workUnit instanceof WorkUnit) {
      WorkUnit wu = (WorkUnit)workUnit;
      if (taskId == wu.taskId && workUnitId == wu.workUnitId && size == wu.size) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Returns a hash code for this work unit. The hash code for a work unit object
   *  is computed as
   *  <blockquote><pre>
   *    w/2^32 + w mod 2^32
   *  </pre></blockquote>
   *  using <code>int</code> arithmetic, where <code>w</code> is the work unit identifier
   *  <code>^</code> indicates exponentiation.
  **/
  public int hashCode() {
    return (int)(((workUnitId >>> 32)&0xffffffff) + (workUnitId&0xffffffff));
  }

  /**
   *  Returns the work unit information.
   *  @return the work unit information.
  **/
  public String toString() {
    return "taskId=" + taskId + ", workUnitId=" + workUnitId + ", size=" + size;
  }

  /**
   *  Identifier of the task of this work unit.
  **/
  protected int taskId;

  /**
   *  Identifier of this work unit.
  **/
  protected long workUnitId;

  /**
   *  Size of this work unit.
  **/
  protected int size;

  /**
   *  Work unit and task specific parameters.
  **/
  protected String parameters;

  /**
   *  If this work unit will be recomputed.
  **/
  protected boolean recompute;
}
