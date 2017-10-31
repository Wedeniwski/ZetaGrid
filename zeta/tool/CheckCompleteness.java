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

package zeta.tool;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import zeta.server.tool.ConstantProperties;
import zeta.server.tool.Database;
import zeta.server.util.DatabaseUtils;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class CheckCompleteness {

  public static void main(String[] args) {
    if (args.length == 1) {
      if (args[0].equals("?")) {
        System.out.println("USAGE: [s] [n]");
      } else if (args[0].equals("s")) {
        summary(0);
      } else {
        checkCompleteness(Long.parseLong(args[0]));
      }
    } else if (args.length == 2 && args[0].equals("s")) {
      summary(Long.parseLong(args[1]));
    } else {
      checkCompleteness(0);
    }
  }

  static void summary(long greaterThanWorkUnitId) {
    File file = new File(ConstantProperties.FINAL_DIR + "/1");
    String[] list = file.list();
    if (list != null) {
      long sizeAll = 0;
      int numberOfWorkUnits = 0;
      int workUnitIdOverlap = 100; // ToDo: not fix
      long minWorkUnitId = 0;
      long maxWorkUnitIdSize = 0;
      for (int i = 0; i < list.length; ++i) {
        if (list[i].endsWith(".zip")) {
          int idx = list[i].indexOf('_', 11);
          long workUnitId = Long.parseLong(list[i].substring(11, idx));
          if (greaterThanWorkUnitId == 0 || greaterThanWorkUnitId < workUnitId) {
            int size = Integer.parseInt(list[i].substring(idx+1, list[i].length()-4));
            if (minWorkUnitId == 0 || minWorkUnitId > workUnitId) {
              minWorkUnitId = workUnitId;
            }
            if (maxWorkUnitIdSize == 0 || maxWorkUnitIdSize < workUnitId+size) {
              maxWorkUnitIdSize = workUnitId+size-workUnitIdOverlap;
            }
            sizeAll += size-workUnitIdOverlap;
            ++numberOfWorkUnits;
          }
        }
      }
      System.out.println("Checked between " + minWorkUnitId + " and " + maxWorkUnitIdSize);
      System.out.println("Available: " + sizeAll + " (" + (sizeAll*100.0)/(maxWorkUnitIdSize-minWorkUnitId) + "%)");
      System.out.println("Missing: " + (maxWorkUnitIdSize-minWorkUnitId-sizeAll) + " (" + ((maxWorkUnitIdSize-minWorkUnitId-sizeAll)*100.0)/(maxWorkUnitIdSize-minWorkUnitId) + "%)");
    }
  }

  static void checkCompleteness(long greaterThanWorkUnitId) {
    int taskId = 1; // ToDo: not fix
    int workUnitIdOverlap = 100; // ToDo: not fix
    File file = new File(ConstantProperties.FINAL_DIR + '/' + taskId);
    String[] list = file.list();
    if (list != null) {
      Arrays.sort(list);
      Connection connection = null;
      Statement stmt = null;
      try {
        long lastWorkUnitId = 0;
        for (int i = 0; i < list.length; ++i) {
          if (list[i].endsWith(".zip")) {
            int idx = list[i].indexOf('_', 11);
            long workUnitId = Long.parseLong(list[i].substring(11, idx));
            if (greaterThanWorkUnitId == 0 || greaterThanWorkUnitId < workUnitId) {
              int size = Integer.parseInt(list[i].substring(idx+1, list[i].length()-4));
              if (lastWorkUnitId != 0 && lastWorkUnitId < workUnitId) {
                try {
                  if (connection == null) {
                    connection = Database.getConnection();
                    stmt = connection.createStatement();
                  }
                  ResultSet rs = stmt.executeQuery("SELECT size FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id=" + (lastWorkUnitId-workUnitIdOverlap));
                  if (rs.next()) {
                    size = rs.getInt(1);
                    rs.close();
                    System.out.println("MISSING: " + (lastWorkUnitId-workUnitIdOverlap) + ' ' + size);
                  } else {
                    rs.close();
                    long workUnitId2;
                    do {
                      workUnitId2 = workUnitId;
                      rs = stmt.executeQuery("SELECT MAX(work_unit_id) FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id<" + lastWorkUnitId);
                      if (rs.next()) {
                        workUnitId2 = rs.getLong(1);
                        rs.close();
                        rs = stmt.executeQuery("SELECT size FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId2);
                        if (rs.next()) {
                          size = rs.getInt(1);
                        }
                      } else {
                        size = (int)(workUnitId-lastWorkUnitId);
                        workUnitId2 = lastWorkUnitId;
                      }
                      rs.close();
                      System.out.println("MISSING: " + workUnitId2 + ' ' + size);
                      lastWorkUnitId = workUnitId2+size;
                    } while (lastWorkUnitId < workUnitId);
                    workUnitId = workUnitId2;
                  }
                } catch (Exception e) {
                  System.out.println("MISSING! " + lastWorkUnitId + ' ' + (workUnitId-lastWorkUnitId));
                }
                --i;
              } else {
                checkFileSize(taskId, list[i], size);
              }
              lastWorkUnitId = workUnitId+size;
            }
          }
        }
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(connection);
      }
    }
  }

  static void checkFileSize(int taskId, String filename, int size) {
    long fileSize = new File(ConstantProperties.FINAL_DIR + '/' + taskId + '/' + filename).length();
    double d = size/200000.0;
    if (fileSize < d*100000 || fileSize > d*880000) {
      int idx = filename.indexOf('_', 11);
      long workUnitId = Long.parseLong(filename.substring(11, idx));
      size = Integer.parseInt(filename.substring(idx+1, filename.length()-4));
      System.out.println("ERROR:   " + workUnitId + ' ' + size);
    }
  }
}
