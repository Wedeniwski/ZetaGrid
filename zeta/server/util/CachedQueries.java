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

package zeta.server.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import zeta.util.Table;

/**
 *  Cache for some frequently used queries.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class CachedQueries {
  /**
   *  Maps all defined user names in lower case to the first typed user name.
   *  The map will be cached for about 20 minutes.
   *  @param  con  connection to the back-end database
   *  @return map which maps all defined user names in lower case to the first typed user name.
  **/
  public static Map getUserNames(Connection con) throws SQLException {
    boolean refresh = false;
    synchronized (userNames) {
      long time = System.currentTimeMillis();
      if (time-lastUserNamesRefresh >= 1200000) {
        lastUserNamesRefresh = time;
        refresh = true;
      }
    }
    if (refresh) {
      Map names = new HashMap(userNames.size());
      Statement stmt = null;
      try {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM zeta.user WHERE id>0 ORDER BY id,server_id");
        while (rs.next()) {
          String name = rs.getString(1);
          String lowerName = name.toLowerCase();
          if (!names.containsKey(lowerName)) {
            names.put(lowerName, name);
          }
        }
        rs.close();
        userNames = names;
      } finally {
        DatabaseUtils.close(stmt);
      }
    }
    return userNames;
  }

  /**
   *  Returns the starting timestamp in DAYS of the specified user name.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @param  userName user name
   *  @return the starting timestamp in DAYS of the specified user name.
  **/
  public static Long getUserMinStartDays(int taskId, Connection con, String userName) throws SQLException {
    Integer task = new Integer(taskId);
    Map map = (Map)userMinStartDays.get(task);
    if (map == null) {
      map = new HashMap();
      userMinStartDays.put(task, map);
    }
    Long days = (Long)map.get(userName);
    if (days == null) {
      Statement stmt = null;
      try {
        StringBuffer buffer = new StringBuffer(500);
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT server_id,id FROM zeta.user WHERE id>0 AND LOWER(name)='" + userName + "' ORDER BY id");
        if (rs.next()) {
          buffer.append("SELECT DAYS(MIN(start)) FROM zeta.computation WHERE task_id=");
          buffer.append(taskId);
          buffer.append(" AND (server_id=");
          buffer.append(rs.getInt(1));
          buffer.append(" AND user_id=");
          buffer.append(rs.getInt(2));
          while (rs.next()) {
            buffer.append(" OR server_id=");
            buffer.append(rs.getInt(1));
            buffer.append(" AND user_id=");
            buffer.append(rs.getInt(2));
          }
          buffer.append(')');
        }
        rs.close();
        if (buffer.length() > 0) {
          rs = stmt.executeQuery(buffer.toString());
          if (rs.next()) {
            days = new Long(rs.getLong(1));
            map.put(userName, days);
          }
          rs.close();
        }
      } finally {
        DatabaseUtils.close(stmt);
      }
    }
    return days;
  }

  /**
   *  Maps all defined team names in lower case to the first typed team name.
   *  The map will be cached for about 10 minutes.
   *  @param  con  connection to the back-end database
   *  @return map which maps all defined team names in lower case to the first typed team name.
  **/
  public static Map getTeamNames(Connection con) throws SQLException {
    boolean refresh = false;
    synchronized (teamNames) {
      long time = System.currentTimeMillis();
      if (time-lastTeamNamesRefresh >= 600000) {
        lastTeamNamesRefresh = time;
        refresh = true;
      }
    }
    if (refresh) {
      Map names = new HashMap(teamNames.size());
      Statement stmt = null;
      try {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT team_name FROM zeta.user WHERE id>0 AND NOT team_name IS NULL AND LENGTH(RTRIM(team_name))>0 ORDER BY join_in_team");
        while (rs.next()) {
          String name = rs.getString(1).trim();
          String lowerName = name.toLowerCase();
          if (!names.containsKey(lowerName)) {
            names.put(lowerName, name);
          }
        }
        rs.close();
        teamNames = names;
      } finally {
        DatabaseUtils.close(stmt);
      }
    }
    return teamNames;
  }

  /**
   *  Returns the latest days of the result table.
   *  Value will be cached for one day.
   *  @param taskId task identifier
   *  @param stmt statement object's database
   *  @return the latest days of the result table.
  **/
  public static long getMaxDays(int taskId, Statement stmt) throws SQLException {
    Integer task = new Integer(taskId);
    String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    Long maxDays = (Long)lastMaxDays.get(task);
    if (maxDays == null) {
      maxDays = new Long(0);
    }
    String last = (String)lastMaxDaysUpdate.get(task);
    if (!time.equals(last)) {
      ResultSet rs = stmt.executeQuery("SELECT MAX(DAYS(stop)) FROM zeta.computation a,zeta.result b WHERE a.task_id=b.task_id AND a.task_id=" + taskId
                                     + " AND a.work_unit_id=b.work_unit_id");
      if (rs.next()) {
        long days = rs.getLong(1);
        if (days != maxDays.longValue()) {
          maxDays = new Long(days);
          lastMaxDays.put(task, maxDays);
          lastMaxDaysUpdate.put(task, time);
        }
      }
      rs.close();
    }
    return maxDays.longValue();
  }

  /**
   *  Returns a set of user ID for the specified server ID that can be used for recomputation of work units.
   *  The set contains not more than 1000 user ID's.
   *  Value will be cached one time per day.
   *  @param stmt statement object's database
   *  @param serverId server ID
   *  @return a set of user ID for the specified server ID that can be used for recomputation of work units.
  **/
  public static Set getUsersForRecomputation(Statement stmt, int serverId) throws SQLException {
    String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    if (!time.equals(lastUsersForRecomputationUpdate) || lastUsersForRecomputationServerId != serverId) {
      Set u = new TreeSet();
      ResultSet rs = stmt.executeQuery("SELECT id FROM zeta.user WHERE server_id=" + serverId + " AND recomputation_YN='Y' FETCH FIRST 1000 ROWS ONLY");
      while (rs.next()) {
        u.add(new Integer(rs.getInt(1)));
      }
      rs.close();
      synchronized (usersForRecomputation) {
        usersForRecomputation = u;
        lastUsersForRecomputationUpdate = time;
        lastUsersForRecomputationServerId = serverId;
      }
    }
    return usersForRecomputation;
  }

  /**
   *  Returns the data about the top producers.
   *  The columns of the table are 'user', 'email', 'number of redistributions',
   *  'last redistributed work unit', 'last redistributed timestamp',
   *  'team', 'messages', 'properties', 'server_id', 'id'.
   *  where the last two columns are declared to be hidden.
   *  This table will be cached for about 10 minutes.
   *  @param  con  connection to the back-end database
   *  @return the data about the top producers.
  **/
  public static Table getUserData(Connection con) throws SQLException {
    boolean refresh = false;
    synchronized (userDataTable) {
      long time = System.currentTimeMillis();
      if (time-lastUserDataTableRefresh >= 600000) {
        lastUserDataTableRefresh = time;
        refresh = true;
      }
    }
    if (refresh) {
      Table table = new Query("SELECT name AS \"user\","
                            + " email AS \"email\","
                            + " number_of_redistributions AS \"number of redistributions\","
                            + " last_redistributed_work_unit AS \"last redistributed work unit\","
                            + " last_redistributed_timestamp AS \"last redistributed timestamp\","
                            + " team_name AS \"team\","
                            + " email_valid_YN AS \"messages\","
                            + " properties AS \"properties\","
                            + " server_id,id"
                            + " FROM zeta.user"
                            + " WHERE id>0 ORDER BY server_id,id", con, null).getResult();
      table.setHiddenColumnCount(2);
      userDataTable = table;
    }
    return userDataTable;
  }

  /**
   *  Set the data about the workstations.
   *  The columns of the table are 'user,email,hostname', 'age (days)', 'work units', 'zeros', 'number of active work units',
   *  'last work unit was requested at', 'number of redistributed work units',
   *  'last redistributed work unit ID', 'last redistributed work unit at', 'min start', 'max stop', 'team'
   *  where the last three columns are declared to be hidden.
   *  @param taskId task identifier
   *  @param table the data about the workstations.
  **/
  public static void setWorkstationTable(int taskId, Table table) {
    workstationTables.put(new Integer(taskId), table);
  }

  /**
   *  Returns the data about the workstations.
   *  The columns of the table are 'user,email,hostname', 'age (days)', 'work units', 'zeros', 'number of active work units',
   *  'last work unit was requested at', 'number of redistributed work units',
   *  'last redistributed work unit ID', 'last redistributed work unit at', 'min start', 'max stop', 'team'
   *  where the last three columns are declared to be hidden.
   *  @param taskId task identifier
   *  @return the data about the workstations.
  **/
  public static Table getWorkstationTable(int taskId) {
    return (Table)workstationTables.get(new Integer(taskId));
  }

  /**
   *  Returns an array of objects:
   *  1. Integer: the maximum number of computers which are used by the specified user name simultaneously.
   *  2. Integer: number of computed work units
   *  3. Long: number of computed zeros
   *  @param taskId task identifier
   *  @param name user name in lower case
   *  @param teamname only users that are members of the specified team name (trim and in lower case) are considered if teamname is not <code>null</code>
   *  @return an array of objects.
  **/
  public static Object[] getUserData(int taskId, String name, String teamname) {
    int computersUsed = 0;
    int numberOfWorkUnits = 0;
    long zeros = 0;
    Table table = getWorkstationTable(taskId);
    if (table != null) {
      List computers = new ArrayList(10);
      int n = name.length();
      for (int i = 0, l = table.getRowCount(); i < l; ++i) {
        String s = (String)table.getValue(i, 0);
        if (s.length() > n && s.charAt(n) == ',' && s.regionMatches(true, 0, name, 0, n) && (teamname == null || teamname.equals(table.getValue(i, 11)))) {
          computers.add(new Timestamp[] { (Timestamp)table.getValue(i, 9), (Timestamp)table.getValue(i, 10) });
          numberOfWorkUnits += ((Integer)table.getValue(i, 2)).intValue();
          zeros += ((Long)table.getValue(i, 3)).longValue();
        }
      }
      for (int i = 0, l = computers.size(); i < l; ++i) {
        int c = 1;
        Timestamp[] t1 = (Timestamp[])computers.get(i);
        if (t1[0] != null && t1[1] != null) {
          for (int j = i+1; j < l; ++j) {
            Timestamp[] t2 = (Timestamp[])computers.get(j);
            if (t2[0] != null && t2[1] != null && !t1[1].before(t2[0]) && !t1[0].after(t2[1])) {  // check time overlap
              ++c;
            }
          }
        }
        if (c > computersUsed) {
          computersUsed = c;
        }
      }
    }
    return new Object[] { new Integer(computersUsed), new Integer(numberOfWorkUnits), new Long(zeros) };
  }

  /**
   *  Returns the maximum number of computers which are used by the specified user name simultaneously.
   *  @param taskId task identifier
   *  @param name user name
   *  @return the maximum number of computers which are used by the specified user name simultaneously.
  **/
  public static int getMaxComputersUsed(int taskId, String name) {
    return ((Integer)getUserData(taskId, name, null)[0]).intValue();
  }

  /**
   *  Defines the current number of computers.
   *  @param numberOfComputers number of computers
  **/
  public static void setNumberOfComputers(int numberOfComputers) {
    CachedQueries.numberOfComputers = numberOfComputers;
  }

  /**
   *  Returns the current number of computers.
   *  @return the current number of computers.
  **/
  public static int getNumberOfComputers() {
    return numberOfComputers;
  }


  private static Map workstationTables = new HashMap();

  /**
   *  Contains the data about the top producers.
  **/
  private static Table userDataTable = new Table(8);
  private static long lastUserDataTableRefresh = 0;

  private static long lastUserNamesRefresh = 0;
  private static Map userNames = new HashMap();
  private static Map userMinStartDays = new HashMap();
  private static long lastTeamNamesRefresh = 0;
  private static Map teamNames = new HashMap();
  private static Map lastMaxDays = new HashMap();
  private static Map lastMaxDaysUpdate = new HashMap();

  private static Set usersForRecomputation = new TreeSet();
  private static String lastUsersForRecomputationUpdate = "";
  private static int lastUsersForRecomputationServerId = 0;
  private static int numberOfComputers = 0;
}
