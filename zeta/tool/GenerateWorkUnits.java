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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import zeta.server.Server;
import zeta.server.tool.Database;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.util.ThrowableHandler;

/**
 *
 *  @version 2.0, August 6, 2005
**/
public class GenerateWorkUnits {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("USAGE: <task ID> <start work unit ID>");
    } else {
      generate(Integer.parseInt(args[0]), Long.parseLong(args[1]));
    }
  }

  public static void generate(int taskId, long startWorkUnitId) {
    Connection connection = null;
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      connection = Database.getConnection();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      int serverId = Server.getInstance(connection).getId();
      long workUnitIdComplete = Parameter.getValue(stmt, "work_unit_id_complete", taskId, 0, 3600000);
      long workUnitIdOverlap = Parameter.getValue(stmt, "work_unit_id_overlap", taskId, 0, 3600000);
      ResultSet rs = stmt.executeQuery("SELECT MIN(work_unit_id) FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id>=" + startWorkUnitId);
      if (rs.next()) {
        long maxWorkUnitId = rs.getLong(1);
        rs.close();
        rs = stmt.executeQuery("SELECT MAX(work_unit_id) FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id<=" + startWorkUnitId);
        long minWorkUnitId = (rs.next())? rs.getLong(1) : 0;
        rs.close();
        rs = stmt.executeQuery("SELECT size FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id=" + minWorkUnitId);
        minWorkUnitId += (rs.next())? rs.getInt(1) : 0;
        rs.close();
        rs = stmt.executeQuery("SELECT work_unit_id,size FROM zeta.server_size WHERE server_id=" + serverId + " AND task_id=" + taskId
                             + " AND work_unit_id<=" + startWorkUnitId + " AND work_unit_id+size>" + startWorkUnitId);
        if (!rs.next()) {
          throw new SQLException("Work unit " + startWorkUnitId + " is not inside a defined size for the server " + serverId);
        }
        long maxWorkUnitId2 = rs.getLong(1)+rs.getLong(2);
        rs.close();
        if (minWorkUnitId-workUnitIdOverlap > startWorkUnitId) {
          rs = stmt.executeQuery("SELECT MAX(work_unit_id) FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id>=" + minWorkUnitId + " AND work_unit_id<" + (maxWorkUnitId2-workUnitIdOverlap));
          minWorkUnitId = (rs.next())? rs.getLong(1) : 0;
          rs.close();
          rs = stmt.executeQuery("SELECT size FROM zeta.computation WHERE task_id=" + taskId + " AND work_unit_id=" + minWorkUnitId);
          minWorkUnitId += (rs.next())? rs.getInt(1) : 0;
          rs.close();
          minWorkUnitId -= workUnitIdOverlap;
          throw new SQLException("Work unit " + startWorkUnitId + " is still defined; a possible work unit may be " + minWorkUnitId);
        }
        maxWorkUnitId = Math.min(maxWorkUnitId, maxWorkUnitId2);
        System.out.println("start: " + startWorkUnitId + ", end: " + maxWorkUnitId);
        int numberOfNewWorkUnits = 0;
        long sizeAssigned = 0;
        rs = stmt.executeQuery("SELECT user_id,workstation_id,size,version,"
                             + "COUNT(DISTINCT DAYS(start)) AS start_days,"
                             + "COUNT(DISTINCT DAYS(stop)),"
                             + "MAX(TIMESTAMPDIFF(8,CAST((stop-start) AS CHAR(22)))) AS a"
                             //+ "MIN(TIMESTAMPDIFF(8,CAST((stop-start) AS CHAR(22))))"
                             + " FROM zeta.computation c, zeta.result r"
                             + " WHERE c.work_unit_id=r.work_unit_id"
                             + " AND c.task_id=r.task_id"
                             + " AND c.work_unit_id>" + workUnitIdComplete
                             + " AND c.task_id=" + taskId
                             + " AND c.server_id=" + serverId
                             + " AND DAYS(r.stop)>=DAYS(CURRENT DATE)-5"
                             + " GROUP BY workstation_id,user_id,size,version"
                             + " ORDER BY start_days DESC,a FETCH FIRST 1000 ROWS ONLY");
        while (rs.next() && startWorkUnitId < maxWorkUnitId) {
          if (rs.getInt(5) >= 5 && rs.getInt(6) >= 5 && rs.getInt(7) <= 24) {
            int userId = rs.getInt(1);
            int workstationId = rs.getInt(2);
            int size = rs.getInt(3);
            String version = rs.getString(4);
            if (startWorkUnitId+size+workUnitIdOverlap >= maxWorkUnitId) {
              size = (int)(maxWorkUnitId-startWorkUnitId+workUnitIdOverlap);
            }
            DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                              "INSERT INTO zeta.computation (task_id,work_unit_id,size,server_id,workstation_id,user_id,version,start) VALUES ("
                                              + taskId + ',' + startWorkUnitId + ',' + size + ',' + serverId + ',' + workstationId + ',' + userId
                                              + ",'" + version + "',CURRENT TIMESTAMP)");
            System.out.println(String.valueOf(startWorkUnitId) + " (" + size + ") -> " + workstationId + " (" + version + ')');
            ++numberOfNewWorkUnits;
            sizeAssigned += size;
            startWorkUnitId += size-workUnitIdOverlap;
          }
        }
        System.out.println("Number of new work units: " + numberOfNewWorkUnits);
        System.out.println("Size assigned: " + sizeAssigned);
        System.out.println("Next work unit: " + startWorkUnitId);
      }
      rs.close();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
      DatabaseUtils.close(connection);
    }
  }
}
