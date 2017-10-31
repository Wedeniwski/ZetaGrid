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

package zeta.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import zeta.WorkUnit;
import zeta.server.Server;
import zeta.server.WorkUnitVerifier;
import zeta.server.util.DatabaseUtils;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  @version 2.0, August 6, 2005
**/
public class ZetaWorkUnitVerifier implements WorkUnitVerifier {
  public boolean checkHeader(long workUnitId, int size, String filename) {
    if (filename.endsWith(".log")) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();
        if (line.startsWith("parameter=")) {
          line = reader.readLine();
        }
        if (!line.startsWith("This run (LASTN=" + workUnitId + ", NRANGE=" + size)) {
          if (line.startsWith("This run (LASTN=")) {
            int i = 16;
            int l = line.length();
            while (i < l && Character.isDigit(line.charAt(i))) {
              ++i;
            }
            long n = Long.parseLong(line.substring(16, i));
            if (n > workUnitId+size || n < workUnitId-size || !line.startsWith(", NRANGE=" + size, i)) {
              return false;
            }
          } else {
            return false;
          }
        }
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      } finally {
        StreamUtils.close(reader);
      }
    }
    return true;
  }

  public boolean verify(WorkUnit workUnit, String filename, Connection con) {
    if (filename.endsWith(".log")) {
      BufferedReader reader = null;
      Statement stmt = null;
      try {
        boolean wrongHeader = !checkHeader(workUnit.getWorkUnitId(), workUnit.getSize(), filename);
        if (wrongHeader) {
          System.out.println("Wrong header!");
        }
        int serverId = (con == null)? 1 : Server.getInstance(con).getId();
        stmt = (con == null)? null : con.createStatement();
        reader = new BufferedReader(new FileReader(filename));
        String largeLine = null;
        String line = reader.readLine();
        boolean footer = false;
        boolean wrongFooter = false;
        while (line != null) {
          line = reader.readLine();
          if (line == null) {
            break;
          }
          wrongFooter = footer;
          if (line.startsWith("... Close pair of zeros between ")) {
            String s = reader.readLine();
            if (s != null) {
              line += s;
            }
            if (con != null) {
              int taskId = 1;
              DatabaseUtils.executeAndLogUpdate(serverId, stmt,
                                                "INSERT INTO zeta.found (task_id,work_unit_id,type,timestamp,found) VALUES ("
                                                + workUnit.getTaskId() + ',' + workUnit.getWorkUnitId() + ",'close zeros',CURRENT TIMESTAMP,"
                                                + DatabaseUtils.encodeName(line) + ')');
            }
          } else if (line.startsWith("Extreme S(t) between ") && con != null) {
            DatabaseUtils.executeAndLogUpdate(serverId, stmt,
                                              "INSERT INTO zeta.found (task_id,work_unit_id,type,timestamp,found) VALUES ("
                                              + workUnit.getTaskId() + ',' + workUnit.getWorkUnitId() + ",'extreme S(t)',CURRENT TIMESTAMP,"
                                              + DatabaseUtils.encodeName(line) + ')');
          } else if (line.startsWith(".... Large value at ")) {
            if (largeLine != null) {
              int idx1 = line.indexOf(':');
              int idx2 = largeLine.indexOf(':');
              if (idx1 > 0 && idx2 > 0 && ++idx1 < line.length() && ++idx2 < line.length()) {
                try {
                  BigDecimal t1 = new BigDecimal(line.substring(idx1).trim());
                  BigDecimal t2 = new BigDecimal(largeLine.substring(idx2).trim());
                  if (t1.compareTo(t2) > 0) {
                    largeLine = line;
                  }
                } catch (NumberFormatException nfe) {
                  ThrowableHandler.handle(nfe);
                }
              }
            } else {
              largeLine = line;
            }
          } else if (line.startsWith("LASTN (input for next run) ")) {
            int idx = line.indexOf('=');
            if (idx > 0 && idx+2 < line.length()) {
              try {
                long l = 0;
                for (idx += 2; idx < line.length(); ++idx) {
                  char c = line.charAt(idx);
                  if (Character.isDigit(c)) {
                    l = l*10 + Character.digit(c, 10);
                  } else if (c == ' ') {
                    break;
                  }
                }
                if (l+10 < workUnit.getWorkUnitId()+workUnit.getSize()) {
                  System.out.println("ERROR: " + line);
                }
              } catch (NumberFormatException nfe) {
                ThrowableHandler.handle(nfe);
              }
            } else {
              System.out.println("ERROR: " + line);
            }
          } else if (line.equals("@")) {
            footer = true;
          }
        }
        if (largeLine != null && con != null) {
          int taskId = 1;
          DatabaseUtils.executeAndLogUpdate(serverId, stmt,
                                            "INSERT INTO zeta.found (task_id,work_unit_id,type,timestamp,found) VALUES ("
                                            + workUnit.getTaskId() + ',' + workUnit.getWorkUnitId() + ",'large value',CURRENT TIMESTAMP," + DatabaseUtils.encodeName(largeLine) + ')');
        }
        if (!footer || wrongFooter) {
          System.out.println("Wrong footer!");
          if (wrongHeader) {
            throw new RuntimeException("Wrong header and footer!");
          }
        }
      } catch (SQLException se) {
        ThrowableHandler.handle(se);
        System.exit(1);
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      } finally {
        DatabaseUtils.close(stmt);
        StreamUtils.close(reader);
      }
    }
    return true;
  }
}
