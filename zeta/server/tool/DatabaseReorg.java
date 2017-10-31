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

package zeta.server.tool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zeta.server.Server;
import zeta.server.ServerTask;
import zeta.server.TaskManager;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.util.ThrowableHandler;

/**
 *  @version 2.0, August 6, 2005
**/
public class DatabaseReorg {
  public static void main(String[] args) {
    try {
      if (args.length == 2 || args.length == 3 && args[0].equals("c")) {
        DatabaseReorg reorg = new DatabaseReorg(args[args.length-2], args[args.length-1]);
        Iterator i = TaskManager.getInstance(reorg.connection).getServerTasks();
        while (i.hasNext()) {
          reorg.reorg((ServerTask)i.next(), args.length == 3);
        }
        reorg.close();
        return;
      } else if (args.length == 6 && args[0].equals("h")) {
        DatabaseReorg reorg = new DatabaseReorg(args[4], args[5]);
        reorg.hardReorg(TaskManager.getInstance(reorg.connection).getServerTask(Integer.parseInt(args[1])), Long.parseLong(args[2]), Integer.parseInt(args[3]), 0, false);
        reorg.close();
        return;
      } else if (args.length == 7 && args[0].equals("h")) {
        DatabaseReorg reorg = new DatabaseReorg(args[5], args[6]);
        reorg.hardReorg(TaskManager.getInstance(reorg.connection).getServerTask(Integer.parseInt(args[1])), Long.parseLong(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), false);
        reorg.close();
        return;
      } else if (args.length == 6 && args[0].equals("i")) {
        DatabaseReorg reorg = new DatabaseReorg(args[4], args[5]);
        reorg.hardReorg(TaskManager.getInstance(reorg.connection).getServerTask(Integer.parseInt(args[1])), Long.parseLong(args[2]), Integer.parseInt(args[3]), 0, true);
        reorg.close();
        return;
      } else if (args.length == 7 && args[0].equals("i")) {
        DatabaseReorg reorg = new DatabaseReorg(args[5], args[6]);
        reorg.hardReorg(TaskManager.getInstance(reorg.connection).getServerTask(Integer.parseInt(args[1])), Long.parseLong(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), true);
        reorg.close();
        return;
      } else if (args.length == 6 && args[0].equals("s")) {
        DatabaseReorg reorg = new DatabaseReorg(args[4], args[5]);
        reorg.serverReorg(Integer.parseInt(args[1]), Long.parseLong(args[2]), Integer.parseInt(args[3]), 0);
        reorg.close();
        return;
      } else if (args.length == 7 && args[0].equals("s")) {
        DatabaseReorg reorg = new DatabaseReorg(args[5], args[6]);
        reorg.serverReorg(Integer.parseInt(args[1]), Long.parseLong(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        reorg.close();
        return;
      } else if (args.length == 4) {
        DatabaseReorg reorg = new DatabaseReorg(args[2], args[3]);
        reorg.reorg(Integer.parseInt(args[0]), Long.parseLong(args[1]));
        reorg.close();
        return;
      } else if (args.length == 6) {
        DatabaseReorg reorg = new DatabaseReorg(args[4], args[5]);
        reorg.reorg(Integer.parseInt(args[0]), Long.parseLong(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        reorg.close();
        return;
      }
      System.err.println("USAGE: [c] <user> <password>\n"
                       + "       <task_id> <work_unit_id> [<user_id> <workstation_id>] <user> <password>\n"
                       + "       h <task_id> <less than work_unit_id> <older than days> [<workstation_id>] <user> <password>\n"
                       + "       i <task_id> <less than work_unit_id> <older than days> [<workstation_id>] <user> <password>\n"
                       + "       s <task_id> <less than work_unit_id> <older than days> [<workstation_id>] <user> <password>");
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  DatabaseReorg(String user, String password) throws Exception {
    connection = Database.getConnection(user, password);
  }

  void close() {
    DatabaseUtils.close(connection);
  }

  // preferred method to redistribute work units
  void hardReorg(ServerTask serverTask, long maxWorkUnitId, int days, int workstationId, boolean ignoreSize) throws IOException, SQLException {
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      int taskId = serverTask.getId();
      int serverId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      long workUnitIdComplete = Parameter.getValue(stmt, "work_unit_id_complete", taskId, 0, 3600000) - 2*Parameter.getValue(stmt, "work_unit_id_overlap", taskId, 0, 3600000);
      if (days == 0) {
        days = serverTask.getRedistributionUnconnected();
      }
      long maxDays = CachedQueries.getMaxDays(taskId, stmt);
      if (maxWorkUnitId == 0) {
        ResultSet rs = stmt.executeQuery("SELECT MAX(work_unit_id) FROM zeta.result"
                                       + " WHERE DAYS(stop)<=" + (maxDays-serverTask.getRedistributionConnected())
                                       + " AND work_unit_id>" + workUnitIdComplete);
        if (rs.next()) {
          maxWorkUnitId = rs.getLong(1);
        }
        rs.close();
      }

      // redistribute all work units which are not recomputed up to the deadline
      DatabaseUtils.executeAndLogUpdate(serverId, stmt, "UPDATE zeta.recomputation SET (user_id,workstation_id,version,start)=(NULL,NULL,NULL,NULL) WHERE task_id=" + taskId
                                                      + " AND server_id=" + serverId
                                                      + " AND work_unit_id BETWEEN " + workUnitIdComplete + " AND " + maxWorkUnitId
                                                      + " AND stop IS NULL AND DAYS(start)<=" + (maxDays-days));
      // workstations which still work on a redistributed work unit
      Set activeWorkstations = new HashSet(100);
      ResultSet rs = stmt.executeQuery("SELECT DISTINCT workstation_id"
                                     + " FROM zeta.computation"
                                     + " WHERE task_id=" + taskId
                                     + " AND work_unit_id>" + workUnitIdComplete
                                     + " AND server_id=" + serverId
                                     + " AND redistributed_YN='Y'"
                                     + " AND NOT work_unit_id IN "
                                     + "  (SELECT work_unit_id FROM zeta.result WHERE task_id=" + taskId + " AND work_unit_id>" + workUnitIdComplete + ')');
      while (rs.next()) {
        activeWorkstations.add(new Integer(rs.getInt(1)));
      }
      rs.close();      
      List candidates = new ArrayList(200);
      rs = stmt.executeQuery("SELECT user_id,workstation_id,size,version,"
                           + "COUNT(DISTINCT DAYS(start)) AS start_days,"
                           + "COUNT(DISTINCT DAYS(stop)),"
                           + "MAX(TIMESTAMPDIFF(8,CAST((stop-start) AS CHAR(22)))) AS a"
                           + " FROM zeta.computation c, zeta.result r"
                           + " WHERE c.work_unit_id=r.work_unit_id"
                           + " AND c.task_id=r.task_id"
                           + " AND c.work_unit_id>" + workUnitIdComplete
                           + " AND c.task_id=" + taskId
                           + " AND c.server_id=" + serverId
                           + " AND DAYS(r.stop)>=" + (maxDays-5)
                           + ((workstationId != 0)? " AND NOT workstation_id=" + workstationId : "")
                           + " GROUP BY workstation_id,user_id,size,version"
                           + " ORDER BY start_days DESC,a FETCH FIRST 1000 ROWS ONLY");
      while (rs.next()) {
        Integer wsId = new Integer(rs.getInt(2));
        if (!activeWorkstations.contains(wsId) && rs.getInt(5) >= 5 && rs.getInt(6) >= 5 && rs.getInt(7) <= 24) {
          candidates.add(new Object[] { new Integer(rs.getInt(1)), wsId, new Integer(rs.getInt(3)), rs.getString(4) });
        }
      }
      rs.close();
      if (candidates.size() > 0) {
        Set sizes = new HashSet();
        boolean first = true;
        rs = stmt.executeQuery("SELECT user_id,workstation_id,start,work_unit_id,size FROM zeta.computation WHERE server_id=" + serverId
                             + " AND task_id=" + taskId + " AND work_unit_id>" + workUnitIdComplete
                             + " AND work_unit_id<" + maxWorkUnitId + " AND DAYS(start)<=" + (maxDays-days)
                             + ((workstationId != 0)? " AND workstation_id=" + workstationId : "")
                             + " AND NOT work_unit_id IN "
                             + "  (SELECT work_unit_id FROM zeta.result WHERE task_id=" + taskId + " AND work_unit_id>" + workUnitIdComplete
                             + "   AND work_unit_id<" + maxWorkUnitId + ')');
        while (rs.next()) {
          int userId = rs.getInt(1);
          workstationId = rs.getInt(2);
          long workUnitId = rs.getLong(4);
          int size = rs.getInt(5);
          int l = candidates.size();
          if (l == 0) {
            System.out.println("No further candidates available!");
            break;
          }
          int i = 0;
          while (i < l && ((Integer)((Object[])candidates.get(i))[2]).intValue() != size) {
            ++i;
          }
          boolean equalSize = true;
          if (i == l && (first || ignoreSize)) {
            i = 0;
            while (i < l) {
              int r = ((Integer)((Object[])candidates.get(i))[2]).intValue();
              if (ignoreSize || r > size/2 && r < 2*size) {
                break;
              }
              ++i;
            }
            equalSize = false;
          }
          if (i < l) {
            if (equalSize) {
              first = false;
            }
            Object[] obj = (Object[])candidates.get(i);
            System.out.println("work unit=" + workUnitId + ", workstation=" + workstationId + ", user=" + userId + ", start=" + rs.getTimestamp(3).toString().substring(0, 19));
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            DatabaseUtils.executeAndLogUpdate(serverId, stmt2, "UPDATE zeta.user SET (number_of_redistributions,last_redistributed_work_unit,last_redistributed_timestamp)=(number_of_redistributions+1," + workUnitId + ",'" + currentTimestamp + "') WHERE id=" + userId + " AND server_id=" + serverId);
            DatabaseUtils.executeAndLogUpdate(serverId, stmt2, "UPDATE zeta.workstation SET (number_of_redistributions,last_redistributed_work_unit,last_redistributed_timestamp)=(number_of_redistributions+1," + workUnitId + ",'" + currentTimestamp + "') WHERE id=" + workstationId + " AND server_id=" + serverId);
            int newUserId = ((Integer)obj[0]).intValue();
            int newWorkstationId = ((Integer)obj[1]).intValue();
            String version = (String)obj[3];
            DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                              "UPDATE zeta.computation SET (start,workstation_id,user_id,version,redistributed_YN)=(CURRENT TIMESTAMP," + newWorkstationId + ','
                                              + newUserId + ",'" + version + "','Y') WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
            System.out.println("> (" + size + ") -> " + newWorkstationId + " (" + version + ')');
            candidates.remove(i);
          } else if (!sizes.contains(new Integer(size))) {
            sizes.add(new Integer(size));
            System.out.println("No candidates for size " + size + " (work unit " + workUnitId + ") available!");
          }
        }
        rs.close();
      } else {
        System.out.println("No candidates available!");
      }
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
    }
  }

  /**
   *  It is not good to change the server. But the results can be computed faster.
   *  All server should be well synchronized!!
  **/
  private void serverReorg(int taskId, long maxWorkUnitId, int days, int workstationId) throws IOException, SQLException {
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      int activeServerId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      long workUnitIdComplete = Parameter.getValue(stmt, "work_unit_id_complete", taskId, 0, 3600000) - 2*Parameter.getValue(stmt, "work_unit_id_overlap", taskId, 0, 3600000);
      List candidates = new ArrayList(200);
      ResultSet rs = stmt.executeQuery("SELECT server_id,user_id,workstation_id,size,version,"
                                     + "COUNT(DISTINCT DAYS(start)) AS start_days,"
                                     + "COUNT(DISTINCT DAYS(stop)),"
                                     + "MAX(TIMESTAMPDIFF(8,CAST((stop-start) AS CHAR(22)))) AS a"
                                     + " FROM zeta.computation c, zeta.result r"
                                     + " WHERE c.work_unit_id=r.work_unit_id"
                                     + " AND c.task_id=r.task_id"
                                     + " AND c.work_unit_id>" + workUnitIdComplete
                                     + " AND c.task_id=" + taskId
                                     + " AND DAYS(r.stop)>=DAYS(CURRENT DATE)-5"
                                     + ((workstationId != 0)? " AND NOT workstation_id=" + workstationId : "")
                                     + " GROUP BY workstation_id,user_id,size,version"
                                     + " ORDER BY start_days DESC,a FETCH FIRST 1000 ROWS ONLY");
      while (rs.next()) {
        if (rs.getInt(6) >= 5 && rs.getInt(7) >= 5 && rs.getInt(8) <= 24) {
          candidates.add(new Object[] { new Integer(rs.getInt(1)), new Integer(rs.getInt(2)), new Integer(rs.getInt(3)), new Integer(rs.getInt(4)), rs.getString(5) });
        }
      }
      rs.close();
      if (candidates.size() > 0) {
        Set sizes = new HashSet();
        boolean first = true;
        rs = stmt.executeQuery("SELECT server_id,user_id,workstation_id,start,work_unit_id,size FROM zeta.computation WHERE task_id=" + taskId
                             + " AND work_unit_id>" + workUnitIdComplete
                             + " AND work_unit_id<" + maxWorkUnitId + " AND DAYS(CURRENT DATE)-DAYS(start)>" + days
                             + ((workstationId != 0)? " AND workstation_id=" + workstationId : "")
                             + " AND NOT work_unit_id IN "
                             + "  (SELECT work_unit_id FROM zeta.result WHERE task_id=" + taskId + " AND work_unit_id>" + workUnitIdComplete
                             + "   AND work_unit_id<" + maxWorkUnitId + ')');
        while (rs.next()) {
          int serverId = rs.getInt(1);
          int userId = rs.getInt(2);
          workstationId = rs.getInt(3);
          long workUnitId = rs.getLong(5);
          int size = rs.getInt(6);
          int l = candidates.size();
          if (l == 0) {
            System.out.println("No further candidates available!");
            break;
          }
          int i = 0;
          while (i < l) {
            Object[] obj = (Object[])candidates.get(i);
            if (((Integer)obj[3]).intValue() == size && ((Integer)obj[0]).intValue() == serverId) {
              break;
            }
            ++i;
          }
          if (i == l) { // change server
            i = 0;
            while (i < l) {
              Object[] obj = (Object[])candidates.get(i);
              if (((Integer)obj[3]).intValue() == size) {
                break;
              }
              ++i;
            }
          }
          boolean equalSize = true;
          if (first && i == l) {
            i = 0;
            while (i < l) {
              Object[] obj = (Object[])candidates.get(i);
              if (((Integer)obj[0]).intValue() == serverId) {
                int r = ((Integer)obj[3]).intValue();
                if (r > size/2 && r < 2*size) {
                  break;
                }
              }
              ++i;
            }
            if (i == l) { // change server
              i = 0;
              while (i < l) {
                Object[] obj = (Object[])candidates.get(i);
                int r = ((Integer)obj[3]).intValue();
                if (r > size/2 && r < 2*size) {
                  break;
                }
                ++i;
              }
            }
            equalSize = false;
          }
          if (i < l) {
            if (equalSize) {
              first = false;
            }
            Object[] obj = (Object[])candidates.get(i);
            System.out.println("work unit=" + workUnitId + ", workstation=" + workstationId + ", user=" + userId + ", start=" + rs.getTimestamp(4).toString().substring(0, 19));
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            DatabaseUtils.executeAndLogUpdate(serverId, stmt2, "UPDATE zeta.user SET (number_of_redistributions,last_redistributed_work_unit,last_redistributed_timestamp)=(number_of_redistributions+1," + workUnitId + ",'" + currentTimestamp + "') WHERE id=" + userId + " AND server_id=" + serverId);
            DatabaseUtils.executeAndLogUpdate(serverId, stmt2, "UPDATE zeta.workstation SET (number_of_redistributions,last_redistributed_work_unit,last_redistributed_timestamp)=(number_of_redistributions+1," + workUnitId + ",'" + currentTimestamp + "') WHERE id=" + workstationId + " AND server_id=" + serverId);
            serverId = ((Integer)obj[0]).intValue();
            userId = ((Integer)obj[1]).intValue();
            workstationId = ((Integer)obj[2]).intValue();
            String version = (String)obj[4];
            DatabaseUtils.executeAndLogUpdate(activeServerId, stmt2,
                                              "UPDATE zeta.computation SET (start,server_id,workstation_id,user_id,version,redistributed_YN)=(CURRENT TIMESTAMP," + serverId +
                                              + ',' + workstationId + ',' + userId + ",'" + version + "','Y') WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
            System.out.println("> (" + size + ") -> " + workstationId + " (" + version + ')');
            candidates.remove(i);
          } else if (!sizes.contains(new Integer(size))) {
            sizes.add(new Integer(size));
            System.out.println("No candidates for size " + size + " (work unit " + workUnitId + ") available!");
          }
        }
        rs.close();
      } else {
        System.out.println("No candidates available!");
      }
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
    }
  }

  private void reorg(int taskId, long workUnitId) throws IOException, SQLException {
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      int serverId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      updateUsersForRecomputation(stmt, taskId, serverId);
      ResultSet rs = stmt.executeQuery("SELECT workstation_id,user_id,start,size FROM zeta.computation WHERE server_id=" + serverId
                                       + " AND work_unit_id=" + workUnitId + " AND task_id=" + taskId
                                       + " AND work_unit_id NOT IN (SELECT work_unit_id FROM zeta.result WHERE work_unit_id=" + workUnitId + " AND task_id=" + taskId + ')');
      if (rs.next()) {
        int workstationId = rs.getInt(1);
        int userId = rs.getInt(2);
        int size = rs.getInt(4);
        String version = "";
        System.out.println("task_id=" + taskId + ", work_unit_id=" + workUnitId + ", workstation_id=" + workstationId + ", user_id=" + userId + ", start=" + rs.getTimestamp(3).toString().substring(0, 19) + ", last_update=" + getLastUpdate(stmt2, serverId, workstationId));
        Object[] obj = getBestWorkstation(taskId, serverId, workstationId, userId, size);
        if (obj != null) {
          userId = ((Integer)obj[0]).intValue();
          workstationId = ((Integer)obj[1]).intValue();
          version = (String)obj[2];
          System.out.println("> workstation_id=" + workstationId + ", user_id=" + userId + ", version=" + version);
          DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                            "UPDATE zeta.computation SET (start,workstation_id,user_id,version,redistributed_YN)=(CURRENT TIMESTAMP," + workstationId + ','
                                            + userId + ",'" + version + "','Y') WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
        } else {
          System.out.println("> no workstation available!");
        }
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
    }
  }

  private void reorg(int taskId, long workUnitId, int userId, int workstationId) throws IOException, SQLException {
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      int serverId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      stmt2 = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT workstation_id,user_id,start,version FROM zeta.computation WHERE server_id=" + serverId
                                       + " AND work_unit_id=" + workUnitId + " AND task_id=" + taskId
                                       + " AND work_unit_id NOT IN (SELECT work_unit_id FROM zeta.result WHERE work_unit_id=" + workUnitId + " AND task_id=" + taskId + ')');
      if (rs.next()) {
        int originWorkstationId = rs.getInt(1);
        int originUserId = rs.getInt(2);
        String version = rs.getString(4);
        System.out.println("task_id=" + taskId + ", work_unit_id=" + workUnitId + ", workstation_id=" + originWorkstationId + ", user_id=" + originUserId + ", start=" + rs.getTimestamp(3).toString().substring(0, 19) + ", last_update=" + getLastUpdate(stmt2, serverId, originWorkstationId));
        rs.close();
        rs = stmt2.executeQuery("SELECT MAX(version) FROM zeta.computation WHERE server_id=" + serverId + " AND user_id=" + userId + " AND workstation_id=" + workstationId);
        if (rs.next()) {
          version = rs.getString(1);
        }
        System.out.println("> workstation_id=" + workstationId + ", user_id=" + userId + ", version=" + version);
        DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                          "UPDATE zeta.computation SET (start,workstation_id,user_id,version,redistributed_YN)=(CURRENT TIMESTAMP," + workstationId + ','
                                          + userId + ",'" + version + "','Y') WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
    }
  }


  /*
    1. ermittle alle Pakete,
    1.1. die nicht abgeschlossen sind
    1.2. die vor mehr als 2*d Tagen reserviert wurden
    1.3. und der zugehörige Rechner hat sich nicht innerhalb der letzten d Tagen gemeldet
    2. oder alle Pakete,
    2.1. die vor mehr als d Tagen reserviert wurden
    2.2. und der zugehörige Rechner in den letzten d Tagen kein Paket abgeschlossen hat
    3. oder alle Pakete,
    3.1. die vor mehr als d Tagen reserviert wurden
    3.2. und nicht gleich der kleinsten Paketnummer ist, die vom zugehörigen Rechner reserviert wurden
  */
  private void reorg(ServerTask serverTask, boolean check) throws IOException, SQLException {
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    System.out.println("start: " + format.format(new Date()));
    int originTaskId = -1;
    int taskId = serverTask.getId();
    int failures = 0;
    Statement stmt = null;
    Statement stmt2 = null;
    try {
      int serverId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      String lastRedistribution = Parameter.getValue(stmt, "last_redistribution", null);
      if (lastRedistribution != null) {
        if (System.currentTimeMillis()-Long.parseLong(lastRedistribution) <= Long.parseLong(Parameter.getValue(stmt, "diff_to_last_redistribution", "0"))) {
          System.out.println("last redistribution at " + new Date(Long.parseLong(lastRedistribution)));
          return;
        }
      }
      stmt2 = connection.createStatement();
      workUnitIdComplete = Parameter.getValue(stmt, "work_unit_id_complete", 0, 0, 3600000) - 2*Parameter.getValue(stmt, "work_unit_id_overlap", 0, 0, 3600000);
      ResultSet rs = stmt.executeQuery("SELECT work_unit_id,workstation_id,user_id,start,size"
                                     + " FROM zeta.computation"
                                     + " WHERE task_id=" + taskId
                                     + " AND work_unit_id>" + workUnitIdComplete
                                     + " AND server_id=" + serverId
                                     + " AND work_unit_id NOT IN (SELECT res.work_unit_id FROM zeta.result res WHERE res.task_id=task_id AND res.work_unit_id>" + workUnitIdComplete + ')'
                                     + " AND (DAYS(CURRENT DATE)-DAYS(start)>" + serverTask.getRedistributionConnected()
                                     + "  AND workstation_id IN"
                                     + "   (SELECT id FROM zeta.workstation"
                                     + "    WHERE server_id=" + serverId
                                     + "    AND DAYS(CURRENT DATE)-DAYS(last_update)>" + serverTask.getRedistributionUnconnected() + ')'
                                     + "  OR DAYS(CURRENT DATE)-DAYS(start)>" + serverTask.getRedistributionUnconnected()
                                     + "  AND NOT (server_id,workstation_id,user_id) IN"
                                     + "   (SELECT DISTINCT comp.server_id,comp.workstation_id,comp.user_id FROM zeta.computation comp"
                                     + "    WHERE comp.task_id=task_id"
                                     + "    AND comp.work_unit_id>" + workUnitIdComplete
                                     + "    AND comp.work_unit_id IN"
                                     + "     (SELECT res.work_unit_id FROM zeta.result res"
                                     + "      WHERE res.task_id=task_id"
                                     + "      AND DAYS(CURRENT DATE)-DAYS(res.stop)<=" + serverTask.getRedistributionUnconnected()
                                     + "      AND res.work_unit_id>" + workUnitIdComplete + ')'
                                     + "    AND comp.server_id=" + serverId
                                     + "))");
      for (int cycle = 0; cycle < 3; ++cycle) {
        System.out.println("cycle=" + cycle);
        while (rs.next()) {
          long workUnitId = rs.getLong(1);
          int workstationId = rs.getInt(2);
          int userId = rs.getInt(3);
          int size = rs.getInt(5);
          int originUserId = userId;
          String version = "";
          System.out.println("task_id=" + taskId + ", work_unit_id=" + workUnitId + ", size=" + size + ", workstation_id=" + workstationId + ", user_id=" + userId + ", start=" + rs.getTimestamp(4).toString().substring(0, 19) + ", last_update=" + getLastUpdate(stmt2, serverId, workstationId));
          if (originTaskId != taskId) {
            updateUsersForRecomputation(stmt2, taskId, serverId);
            originTaskId = taskId;
          }
          Object[] obj = getBestWorkstation(taskId, serverId, workstationId, userId, size);
          if (obj != null) {
            if (userId != ((Integer)obj[0]).intValue() && !check) {
              Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
              DatabaseUtils.executeAndLogUpdate(serverId, stmt2, "UPDATE zeta.user SET (number_of_redistributions,last_redistributed_work_unit,last_redistributed_timestamp)=(number_of_redistributions+1," + workUnitId + ",'" + currentTimestamp + "') WHERE id=" + userId + " AND server_id=" + serverId);
              DatabaseUtils.executeAndLogUpdate(serverId, stmt2, "UPDATE zeta.workstation SET (number_of_redistributions,last_redistributed_work_unit,last_redistributed_timestamp)=(number_of_redistributions+1," + workUnitId + ",'" + currentTimestamp + "') WHERE id=" + workstationId + " AND server_id=" + serverId);
              // ToDo: improve vacation problem!
              // DatabaseUtils.executeAndLogUpdate(serverId, stmt2, "UPDATE zeta.user SET fail=fail+1 WHERE id=" + userId + " AND server_id=" + serverId);
            }
            userId = ((Integer)obj[0]).intValue();
            workstationId = ((Integer)obj[1]).intValue();
            version = (String)obj[2];
            System.out.println("> workstation_id=" + workstationId + ", user_id=" + userId + ", version=" + version);
            if (!check) {
              if (cycle <= 1 || userId != originUserId) {
                DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                                  "UPDATE zeta.computation SET (start,workstation_id,user_id,version,redistributed_YN)=(CURRENT TIMESTAMP," + workstationId + ','
                                                  + userId + ",'" + version + "','Y') WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
              }
              if (cycle > 1) {
                DatabaseUtils.executeAndLogUpdate(serverId, stmt2,
                                                  "UPDATE zeta.recomputation SET (start,workstation_id,user_id,version,redistributed_YN)=(CURRENT TIMESTAMP," + workstationId + ','
                                                  + userId + ",'" + version + "','Y') WHERE task_id=" + taskId + " AND work_unit_id=" + workUnitId);
              }
            }
          } else {
            System.out.println("> no workstation available!");
            if (++failures > 10*(cycle+1)) {
              System.out.println("Error: Too many failures: " + failures);
              break;
            }
          }
        }
        rs.close();
        if (cycle == 0) {
          rs = stmt.executeQuery("SELECT work_unit_id,workstation_id,user_id,start,size"
                               + " FROM zeta.computation"
                               + " WHERE server_id=" + serverId
                               + " AND task_id=" + taskId
                               + " AND work_unit_id>" + workUnitIdComplete
                               + " AND work_unit_id NOT IN (SELECT res.work_unit_id FROM zeta.result res WHERE res.task_id=task_id)"
                               + " AND DAYS(CURRENT DATE)-DAYS(start)>" + serverTask.getRedistributionUnconnected()
                               + " AND NOT (workstation_id,work_unit_id) IN"
                               + "  (SELECT workstation_id,min(work_unit_id)"
                               + "  FROM zeta.computation comp"
                               + "  WHERE comp.task_id=task_id"
                               + "  AND comp.work_unit_id NOT IN (SELECT res.work_unit_id FROM zeta.result res WHERE res.task_id=task_id)"
                               + "  AND server_id=" + serverId
                               + "  AND DAYS(CURRENT DATE)-DAYS(start)>" + serverTask.getRedistributionUnconnected()
                               + "  GROUP BY workstation_id)");
        } else if (cycle == 1) {
          rs = stmt.executeQuery("SELECT work_unit_id,workstation_id,user_id,start,size"
                               + " FROM zeta.recomputation"
                               + " WHERE work_unit_id>" + workUnitIdComplete
                               + " AND task_id=" + taskId
                               + " AND server_id=" + serverId
                               + " AND stop IS NULL"
                               + " AND (DAYS(CURRENT DATE)-DAYS(start)>" + serverTask.getRedistributionConnected()
                               + "  AND workstation_id IN"
                               + "   (SELECT id FROM zeta.workstation"
                               + "    WHERE server_id=" + serverId
                               + "    AND DAYS(CURRENT DATE)-DAYS(last_update)>" + serverTask.getRedistributionUnconnected() + ')'
                               + "  OR DAYS(CURRENT DATE)-DAYS(start)>" + serverTask.getRedistributionUnconnected()
                               + "  AND NOT (server_id,workstation_id,user_id) IN"
                               + "   (SELECT DISTINCT comp.server_id,comp.workstation_id,comp.user_id FROM zeta.computation comp"
                               + "    WHERE comp.task_id=task_id"
                               + "    AND comp.work_unit_id>" + workUnitIdComplete
                               + "    AND comp.work_unit_id IN"
                               + "     (SELECT res.work_unit_id FROM zeta.result res"
                               + "      WHERE res.task_id=task_id"
                               + "      AND DAYS(CURRENT DATE)-DAYS(res.stop)<=" + serverTask.getRedistributionUnconnected()
                               + "      AND res.work_unit_id>" + workUnitIdComplete + ')'
                               + "    AND comp.server_id=" + serverId
                               + "))");
        }
      }
      Parameter.setValue(stmt, "last_redistribution", String.valueOf(System.currentTimeMillis()));
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(stmt2);
      System.out.println("stop: " + format.format(new Date()));
      if (failures > 0) {
        System.out.println("an error ocurred (" + failures + ")!");
      }
    }
  }

  private Object[] getBestWorkstation(int taskId, int serverId, int workstationId, int userId, int size) throws SQLException {
    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      workUnitIdComplete = Parameter.getValue(stmt, "work_unit_id_complete", taskId, 0, 3600000) - 2*Parameter.getValue(stmt, "work_unit_id_overlap", taskId, 0, 3600000);
      List list = (List)bestWorkstationUser.get(new Integer(userId));
      if (list == null) {
        list = new LinkedList();
      }
      if (list.size() == 0) {
        ServerTask serverTask = TaskManager.getInstance(connection).getServerTask(taskId);
        ResultSet rs = stmt.executeQuery("SELECT comp.user_id,comp.workstation_id,MAX(version),MAX(DAYS(res.stop)),COUNT(*)"
                                         + " FROM zeta.computation comp,zeta.result res"
                                         + " WHERE comp.work_unit_id>" + workUnitIdComplete
                                         + " AND comp.server_id=" + serverId
                                         + " AND comp.task_id=" + taskId
                                         + " AND comp.task_id=res.task_id"
                                         + " AND comp.work_unit_id=res.work_unit_id"
                                         + " AND comp.size=" + size
                                         + " AND DAYS(CURRENT DATE)-DAYS(res.stop)<=" + serverTask.getRedistributionUnconnected()
                                         + " AND (comp.user_id=" + userId + " OR comp.workstation_id=" + workstationId + ')'
                                         + " GROUP BY comp.user_id,comp.workstation_id ORDER BY MAX(DAYS(res.stop)) DESC,MAX(version) DESC,COUNT(*) DESC");
        while (rs.next()) {
          Object[] obj = { new Integer(rs.getInt(1)), new Integer(rs.getInt(2)), rs.getString(3) };
          list.add(obj);
        }
        rs.close();
        if (list.size() > 0) {
          bestWorkstationUser.put(new Integer(userId), list);
        } else {
          list = null;
        }
      }
      if (list != null) {
        while (list.size() > 0) {
          Object[] obj = (Object[])list.get(0);
          list.remove(0);
          ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM zeta.computation"
                                         + " WHERE work_unit_id>" + workUnitIdComplete
                                         + " AND task_id=" + taskId
                                         + " AND server_id=" + serverId
                                         + " AND workstation_id=" + (Integer)obj[1]
                                         + " AND work_unit_id NOT IN"
                                         + "  (SELECT work_unit_id FROM zeta.result"
                                         + "   WHERE task_id=" + taskId
                                         + "   AND work_unit_id>" + workUnitIdComplete + ')');
          if (!rs.next() || rs.getInt(1) <= 5) {
            return obj;
          }
        }
      }
      if (bestWorkstation.size() == 0) {
        ResultSet rs = stmt.executeQuery("SELECT user_id,workstation_id,MAX(version),SUM(size),MAX(start)"
                                       + " FROM zeta.computation"
                                       + " WHERE server_id=" + serverId
                                       + " AND task_id=" + taskId
                                       + " AND user_id IN (SELECT id FROM zeta.user WHERE server_id=" + serverId + " AND recomputation_YN='Y')"
                                       + " AND work_unit_id IN"
                                       + "  (SELECT work_unit_id"
                                       + "  FROM zeta.result"
                                       + "  WHERE task_id=" + taskId
                                       + "  AND work_unit_id>" + workUnitIdComplete
                                       + "  AND DAYS(stop)>=DAYS(CURRENT DATE)-1)"
                                       + " AND size=" + size
                                       + " AND HOUR(CURRENT TIME)-HOUR(start)<5"
                                       + " GROUP BY user_id,workstation_id ORDER BY MAX(version) DESC,SUM(size),MAX(start)");
        while (rs.next()) {
          Object[] obj = { new Integer(rs.getInt(1)), new Integer(rs.getInt(2)), rs.getString(3) };
          bestWorkstation.add(obj);
        }
        rs.close();
        if (bestWorkstation.size() == 0) {
          return null;
          //throw new SQLException("no workstation");
        }
      }
      if (++bestWorkstationIdx == bestWorkstation.size()) {
        bestWorkstationIdx = 0;
      }
      return (Object[])bestWorkstation.get(bestWorkstationIdx);
    } finally {
      DatabaseUtils.close(stmt);
    }
  }

  private Timestamp getLastUpdate(Statement stmt, int serverId, int workstationId) throws SQLException {
    ResultSet rs = stmt.executeQuery("SELECT last_update FROM zeta.workstation WHERE server_id=" + serverId + " AND id=" + workstationId);
    Timestamp lastUpdate = (rs.next())? rs.getTimestamp(1) : null;
    rs.close();
    return lastUpdate;
  }

  private void updateUsersForRecomputation(Statement stmt, int taskId, int serverId) throws SQLException {
    stmt.executeUpdate("UPDATE zeta.user SET recomputation_YN='N' WHERE server_id=" + serverId);
    StringBuffer buffer = new StringBuffer(5000);
    buffer.append("UPDATE zeta.user SET recomputation_YN='Y' WHERE server_id=" + serverId + " AND id IN (");
    ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM zeta.computation WHERE server_id=" + serverId);
    if (rs.next()) {
      String version = rs.getString(1).trim();
      rs.close();
      rs = stmt.executeQuery("SELECT comp.user_id,COUNT(DISTINCT DAYS(res.stop)) AS \"count\""
                           + " FROM zeta.computation comp, zeta.result res"
                           + " WHERE comp.server_id=" + serverId
                           + " AND comp.task_id=res.task_id"
                           + " AND comp.work_unit_id=res.work_unit_id"
                           + " AND comp.work_unit_id>" + workUnitIdComplete
                           + " AND comp.task_id=" + taskId
                           + " AND comp.version='" + version + '\''
                           + " AND DAYS(res.stop)>=DAYS(CURRENT DATE)-5"
                           + " GROUP BY comp.user_id ORDER BY \"count\" DESC");
      boolean first = true;
      while (rs.next()) {
        if (rs.getInt(2) < 3) {
          break;
        }
        if (!first) {
          buffer.append(',');
        }
        buffer.append(rs.getInt(1));
        first = false;
      }
      rs.close();
      buffer.append(')');
      if (!first) {
        String sql = buffer.toString();
        System.out.println(sql);
        stmt.executeUpdate(sql);
      }
    } else {
      rs.close();
    }
  }

  private Connection connection = null;
  private long workUnitIdComplete = -1;
  private List bestWorkstation = new ArrayList(10000);
  private int bestWorkstationIdx = -1;
  private Map bestWorkstationUser = new HashMap();
}
