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

package zeta.tool;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import zeta.server.Server;
import zeta.server.tool.Database;
import zeta.server.util.DatabaseUtils;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  @version 2.0, August 6, 2005
**/
public class ZetaApprove {

  public static void main(String[] args) {
    try {
      if (args.length == 2) {
        ZetaApprove approve = new ZetaApprove(args[0], args[1]);
        approve.approve(true);
        approve.close();
        return;
      }
      System.err.println("USAGE: java zeta.tool.ZetaApprove <user> <password>");
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  public ZetaApprove(String user, String password) throws Exception {
    connection = Database.getConnection(user, password);
  }

  public void close() {
    DatabaseUtils.close(connection);
  }

  public void approve(boolean outputNotApproved) throws SQLException, IOException {
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      int serverId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT task_id,work_unit_id,found FROM zeta.found WHERE approved_YN='N' AND type='close zeros'");
      while (rs.next()) {
        boolean approved = false;
        int count = 0;
        int taskId = rs.getInt(1);
        long workUnitId = rs.getLong(2);
        String found = rs.getString(3);
        int idx = found.indexOf("This happened at ");
        String pos = (idx >= 0)? found.substring(idx+17) : "";
        ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM zeta.found WHERE approved_YN='N' AND type='close zeros' AND task_id=" + taskId
                                         + " AND work_unit_id=" + workUnitId
                                         + " AND found LIKE '%This happened at %'"
                                         + " AND NOT found LIKE '%This happened at " + pos + "%'");
        if (rs2.next()) {
          count = rs2.getInt(1);
          rs2.close();
          rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM zeta.found WHERE approved_YN='N' AND type='close zeros' AND task_id=" + taskId + " AND work_unit_id=" + workUnitId);
          int countSame = (rs2.next())? rs2.getInt(1) : 1;
          rs2.close();
          rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM zeta.found WHERE approved_YN='N' AND type='close zeros' AND task_id=" + taskId + " AND found LIKE '%This happened at " + pos + "%' AND work_unit_id=" + workUnitId);
          int countUnique = (rs2.next())? rs2.getInt(1) : 1;
          rs2.close();
          String search = "... Close pair of zeros between ";
          if (found.startsWith(search)) {
            if (idx > 0) {
              int idx2 = found.indexOf(' ', search.length());
              if (idx2 > 0) {
                long n = ZetaStatistic.getStartN(Double.parseDouble(found.substring(search.length(), idx2)))-5;
                //ZetaCalc.setCoutLog(false);
                ZetaStatistic.zetaZeros(n, 15, 0);
                String filename = "zeta_zeros_" + n + "_15.log";
                String[] closeZerosVerified = StreamUtils.startsWith(new String[] { "... Close pair of zeros between ", "This happened at " }, new FileReader(filename), true);
                //new File(filename).delete();
                //new File("zeta_zeros_" + n + "_15.txt").delete();
                //System.out.println("n="+n+", countSame="+countSame+", countUnique="+countUnique+", closeZerosVerified.length="+((closeZerosVerified == null)? -1 : closeZerosVerified.length));
                if (closeZerosVerified == null || closeZerosVerified.length <= 1) {
                  if (outputNotApproved) {
                    System.out.println("String not found: " + found);
                  }
                } else if (count > 0 || countUnique > 1 || closeZerosVerified.length > 2) {
                  if (outputNotApproved) {
                    System.out.println("String not unique: " + found);
                  }
                } else {
                  if (countUnique == 1 && countSame > 1) {
                    DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                                      "DELETE FROM zeta.found WHERE approved_YN='N' AND type='close zeros' AND task_id=" + taskId + " AND NOT found LIKE '" + found + "' AND work_unit_id=" + workUnitId);
                  }
                  if (!closeZerosVerified[0].equals(found.substring(0, idx)) || !closeZerosVerified[1].equals(pos)) {
                    DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                                      "UPDATE zeta.found SET (approved_YN,found)=('Y','" + closeZerosVerified[0] + closeZerosVerified[1]
                                                      + "') WHERE approved_YN='N' AND type='close zeros' AND task_id=" + taskId + " AND work_unit_id=" + workUnitId);
                    System.out.println("Work unit " + workUnitId + " approved but CHANGED (" + getDistance(found) + ").");
                  } else {
                    DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                                      "UPDATE zeta.found SET approved_YN='Y' WHERE approved_YN='N' AND type='close zeros' AND task_id=" + taskId + " AND work_unit_id=" + workUnitId);
                    System.out.println("Work unit " + workUnitId + " approved (" + getDistance(found) + ").");
                  }
                  approved = true;
                }
              }
            }
          }
        } else {
          rs2.close();
        }
        if (!approved && outputNotApproved) {
          System.out.println("Work unit " + workUnitId + " cannot be approved (" + count + "): " + pos);
        }
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
    }
  }

  private String getDistance(String found) {
    final int l = found.length();
    int i = 0;
    while (i < l && !Character.isDigit(found.charAt(i))) {
      ++i;
    }
    int j = found.indexOf(' ', i);
    if (i > 0 && j > i) {
      BigDecimal t1 = new BigDecimal(found.substring(i, j));
      for (i = j; i < l && !Character.isDigit(found.charAt(i)); ++i);
      j = found.indexOf(' ', i);
      if (i > 0 && j > i) {
        BigDecimal t2 = new BigDecimal(found.substring(i, j));
        i = found.indexOf("This happened at ", j);
        if (i > 0) {
          return new DecimalFormat("0.00000000000").format(t2.subtract(t1));
        }
      }
    }
    return "";
  }

  private Connection connection = null;
}
