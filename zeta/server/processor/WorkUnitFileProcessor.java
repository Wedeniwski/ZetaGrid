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

package zeta.server.processor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;

import zeta.WorkUnit;
import zeta.server.util.Parameter;
import zeta.util.ProcessUtils;
import zeta.util.StreamUtils;

/**
 *  Processes work units as files received through the request and the result handler.
**/
public class WorkUnitFileProcessor extends DefaultWorkUnitProcessor {
  /**
   *  Processes work units received through the result handler
   *  @param stmt statement object's database
   *  @param workUnit work unit which should be processed
   *  @param result buffer with the zipped result
   *  @param recomputation if the result was recomputed
   *  @return <code>true</code> if the ResultHandler shall save the result into the database.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public boolean processResult(Statement stmt, WorkUnit workUnit, byte[] result, boolean recomputation) throws ServletException, SQLException, IOException {
    // Handle work unit files
    final String pathActive = Parameter.getValue(stmt, "path_active", workUnit.getTaskId(), "", 3600000);
    final String pathSplit = Parameter.getValue(stmt, "path_split", workUnit.getTaskId(), "", 3600000);
    final String pathOut = Parameter.getValue(stmt, "path_out", workUnit.getTaskId(), "", 3600000);
    if (pathActive.length() > 0 && pathSplit.length() > 0) {
      final File[] list = new File(pathActive).listFiles();
      if (list == null || list.length == 0) {
        throw new ServletException("no active work unit");
      }
      int i = 0;
      String s = String.valueOf(workUnit.getWorkUnitId()) + '_';
      while (i < list.length) {
        if (list[i].isDirectory() && list[i].getName().startsWith(s)) {
          final String name = list[i].getName().substring(s.length());
          if (!new File(pathSplit + '/' + name).exists()) {
            int j = 0;
            while (j < list.length) {
              if (j != i && list[j].isDirectory() && list[j].getName().endsWith(name)) {
                long workUnitId2 = 0;
                try {
                  workUnitId2 = Long.parseLong(list[j].getName().substring(0, list[j].getName().indexOf('_')));
                  if (workUnitId2 != workUnit.getWorkUnitId()) {
                    ResultSet rs = stmt.executeQuery("SELECT work_unit_id FROM zeta.result WHERE task_id=" + workUnit.getTaskId() + " AND work_unit_id=" + workUnitId2);
                    if (!rs.next()) {
                      rs.close();
                      break;
                    }
                    rs.close();
                  }
                } catch (NumberFormatException nfe) {
                }
              }
              ++j;
            }
            if (j == list.length) {
              final String command = Parameter.getValue(stmt, "command_combine", workUnit.getTaskId(), "", 3600000);
              if (command.length() > 0) {
                Thread thread = new Thread() {
                  public void run() {
                    ProcessUtils.exec(command + ' ' + pathActive + ' ' + name + ' ' + pathOut);
                    if (!(new File(pathOut + '/' + name + ".err").exists())) {
                      for (int j = 0; j < list.length; ++j) {
                        if (list[j].isDirectory() && list[j].getName().endsWith(name)) {
                          StreamUtils.delete(list[j].getPath());
                        }
                      }
                      /*String pathIn = Parameter.getValue(stmt, "path_in", workUnit.getTaskId(), "", 3600000);
                      if (pathIn != null && pathIn.length() > 0) {
                        int idx = name.indexOf('_');
                        File[] files = new File(pathIn).listFiles();
                        if (files != null) {
                          for (int j = 0; j < files.length; ++j) {
                            if (name.startsWith(files[j].getName(), idx+1)) {
                              files[j].delete();
                            }
                          }
                        }
                      }*/
                    }
                  }
                };
                thread.start();
              }
            }
          }
          break;
        }
        ++i;
      }
      if (i == list.length) {
        throw new ServletException("work unit " + workUnit.getWorkUnitId() + " is not active");
      }
    }
    return false;
  }

  /**
   *  Activates the specified work unit for the requested client. This work unit does not contain parameters.
   *  @param stmt statement object's database
   *  @param workUnit work unit
   *  @return less than 0 if an error occurs, 0 if the specified work unit is activated but no further work unit can be activated,
   *          and greater 0 if the specified work unit is activated and further work units can be activated.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public int activateWorkUnit(Statement stmt, WorkUnit workUnit) throws ServletException, SQLException {
    String pathSplit = Parameter.getValue(stmt, "path_split", workUnit.getTaskId(), "", 3600000);
    String pathActive = Parameter.getValue(stmt, "path_active", workUnit.getTaskId(), "", 3600000);
    if (pathSplit.length() > 0 && pathActive.length() > 0) {
      File[] list = new File(pathSplit).listFiles();
      if (list == null || list.length == 0) {   // no work unit available
        return -1;
      }
      // get subdirectory with lex. lowest name, that does not contain a lock file
      File dir = null;
      for (int i = 0; i < list.length; ++i) {
        if (list[i].isDirectory()) {
          File[] list2 = list[i].listFiles();
          if (list2 != null && list2.length > 0) {
            if (dir == null || list[i].getName().compareTo(dir.getName()) < 0) {
              int j = 0;
              while (j < list2.length) {
                if (list2[j].getName().equals("lock")) {
                  break;
                }
                ++j;
              }
              if (j == list2.length) {
                dir = list[i];
              }
            }
          }
        }
      }
      if (dir == null) {  // no work unit available
        return -1;
      }
      String pathName = pathActive + '/' + workUnit.getWorkUnitId() + '_' + dir.getName();
      new File(pathName).mkdir();
      list = dir.listFiles();
      // a work unit contains files with same name and different extension
      // note: one folder contains one or more work units
      if (!list[0].renameTo(new File(pathName + '/' + list[0].getName()))) {
        throw new ServletException("file error: " + workUnit.getWorkUnitId() + '/' + list[0].getName());
      }
      String s = list[0].getName().substring(0, list[0].getName().lastIndexOf('.'));
      for (int i = 1; i < list.length; ++i) {
        if (list[i].getName().startsWith(s) && !list[i].renameTo(new File(pathName + '/' + list[i].getName()))) {
          throw new ServletException("file error: " + workUnit.getWorkUnitId() + '/' + list[i].getName());
        }
      }
      list = dir.listFiles();
      if (list == null || list.length == 0) {
        dir.delete();
      }
      list = new File(pathSplit).listFiles();
      if (list == null || list.length == 0) {
        return 0;
      }
    }
    return 1;
  }
}
