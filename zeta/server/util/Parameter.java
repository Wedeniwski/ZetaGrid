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
     W. Westje
--*/

package zeta.server.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.tool.ServerSynchronization;

/**
 *  Gets global parameters from the database.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class Parameter {
  public static final int GLOBAL_PARAMETER = 0;

  /**
   *  Returns the value of the specified global parameter.
   *  @param stmt statement object's database
   *  @param parameter global parameter
   *  @param defaultValue default value
   *  @return the value of the specified global parameter.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static String getValue(Statement stmt, String parameter, String defaultValue) throws SQLException {
    return getValue(stmt, parameter, GLOBAL_PARAMETER, defaultValue, 0);
  }

  /**
   *  Returns the value of the specified task parameter.
   *  @param stmt statement object's database
   *  @param parameter global parameter
   *  @param taskId ID of the task
   *  @param defaultValue default value
   *  @return the value of the specified global parameter.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static String getValue(Statement stmt, String parameter, int taskId, String defaultValue) throws SQLException {
    return getValue(stmt, parameter, taskId, defaultValue, 0);
  }

  /**
   *  Returns the value of the specified task parameter.
   *  @param stmt statement object's database
   *  @param parameter global parameter
   *  @param taskId ID of the task
   *  @param defaultValue default value
   *  @param cachingTime caching the value for milli seconds
   *  @return the value of the specified global parameter.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static long getValue(Statement stmt, String parameter, int taskId, long defaultValue, long cachingTime) throws SQLException {
    long result = defaultValue;
    try {
      result = Long.parseLong(Parameter.getValue(stmt, parameter, taskId, Long.toString(defaultValue), cachingTime));
    } catch (NumberFormatException nfe) {
    }
    return result;
  }

  /**
   *  Returns the value of the specified task parameter.
   *  @param stmt statement object's database
   *  @param parameter task parameter
   *  @param taskId ID of the task
   *  @param defaultValue default value
   *  @param cachingTime caching the value for milli seconds
   *  @return the value of the specified global parameter.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static String getValue(Statement stmt, String parameter, int taskId, String defaultValue, long cachingTime) throws SQLException {
    String value = defaultValue;
    synchronized (mapValue) {
      long currentTimeMillis = System.currentTimeMillis();
      List taskParameterKey = Arrays.asList(new Object[] { new Integer(taskId), parameter });
      Object[] obj = (Object[])mapValue.get(taskParameterKey);
      if (obj == null || currentTimeMillis-((Long)obj[0]).longValue() >= cachingTime) {
        if (stmt != null) {
          ResultSet rs = stmt.executeQuery("SELECT value FROM zeta.parameter WHERE parameter='" + parameter + "' AND task_id=" + taskId);
          if (rs.next()) {
            value = rs.getString(1);
          }
          rs.close();
          if (cachingTime > 0) {
            obj = new Object[] { new Long(currentTimeMillis), value };
            mapValue.put(taskParameterKey, obj);
          }
        }
      } else {
        value = (String)obj[1];
      }
    }
    return value;
  }

  /**
   *  Set the value of the specified global parameter. This parameter will not be synchronized with other servers.
   *  @param stmt statement object's database
   *  @param parameter global parameter
   *  @param value value of the parameter
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static void setValue(Statement stmt, String parameter, String value) throws SQLException {
    setValue(0, stmt, GLOBAL_PARAMETER, parameter, value, false);
  }

  /**
   *  Set the value of the specified task parameter. This parameter will not be synchronized with other servers.
   *  @param stmt statement object's database
   *  @param taskId ID of the task
   *  @param parameter task parameter
   *  @param value value of the parameter
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static void setValue(Statement stmt, int taskId, String parameter, String value) throws SQLException {
    setValue(0, stmt, taskId, parameter, value, false);
  }

  /**
   *  Set the value of the specified global parameter.
   *  @param serverId ID of the server
   *  @param taskId ID of the task
   *  @param stmt statement object's database
   *  @param parameter global parameter
   *  @param value value of the parameter
   *  @param synchronizeWithOtherServers the parameter will be synchronized with other servers.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static void setValue(int serverId, Statement stmt, int taskId, String parameter, String value, boolean synchronizeWithOtherServers) throws SQLException {
    String sqlStatement = "UPDATE zeta.parameter SET value='" + value + "' WHERE parameter='" + parameter + "' AND task_id=" + taskId;
    if (synchronizeWithOtherServers) {
      DatabaseUtils.executeAndLogUpdate(serverId, stmt, sqlStatement);
    } else {
      stmt.executeUpdate(sqlStatement);
    }
  }

  /**
   *  Returns the maximal possible work unit ID (<code>result[0]+result[1] = work_unit_id+size</code>) for the specified server.
   *  @param servlet  servlet which has a connection to the server database.
   *  @param stmt statement object's database
   *  @param serverId ID of the server
   *  @param taskId ID of the task
   *  @return the maximal possible work unit ID (<code>result[0]+result[1] = work_unit_id+size</code>) for the specified server.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public static long[] getServerMaxWorkUnitId(DispatcherServlet servlet, Statement stmt, int serverId, int taskId) throws SQLException, ServletException {
    synchronized (updateServerMaxWorkUnitId) {
      while (updateServerMaxWorkUnitId.booleanValue()) {
        long workUnitIdComplete = Parameter.getValue(stmt, "work_unit_id_complete", taskId, 0, 3600000);
        ResultSet rs = stmt.executeQuery("SELECT work_unit_id,size FROM zeta.server_size WHERE server_id="
                                         + serverId + " AND task_id=" + taskId + " AND work_unit_id>" + workUnitIdComplete
                                         + " ORDER BY work_unit_id DESC FETCH FIRST 2 ROWS ONLY");
        if (rs.next() && rs.next()) {
          serverMaxWorkUnitId[0] = rs.getLong(1);
          serverMaxWorkUnitId[1] = rs.getLong(2);
          rs.close();
          updateServerMaxWorkUnitId = Boolean.FALSE;
        } else {
          rs.close();
          newServerMaxWorkUnitId(servlet, stmt, serverId, taskId);
        }
      }
    }
    return serverMaxWorkUnitId;
  }

  /**
   *  Inserts a new reservation of work units in the database for the specified server.
   *  @param servlet  servlet which has a connection to the server database.
   *  @param stmt statement object's database
   *  @param serverId ID of the server
   *  @param taskId ID of the task
   *  @exception  SQLException  if a database access error occurs or the table 'zeta.server' is not defined.
  **/
  public static long newServerMaxWorkUnitId(DispatcherServlet servlet, Statement stmt, int serverId, int taskId) throws SQLException, ServletException {
    synchronized (updateServerMaxWorkUnitId) {
      long workUnitIdComplete = Parameter.getValue(stmt, "work_unit_id_complete", taskId, 0, 3600000);
      ResultSet rs = stmt.executeQuery("SELECT MIN(work_unit_id),MAX(work_unit_id) FROM zeta.server_size WHERE server_id=" + serverId
                                     + " AND task_id=" + taskId + " AND work_unit_id>(SELECT MAX(work_unit_id) FROM zeta.result where task_id=" + taskId
                                     + " AND work_unit_id IN (SELECT work_unit_id FROM zeta.computation WHERE task_id=" + taskId
                                     + " AND work_unit_id>" + workUnitIdComplete + " AND server_id=" + serverId + "))");
      if (rs.next()) {
        long workUnitId = rs.getLong(1);
        if (workUnitId < rs.getLong(2)) {
          rs.close();
          return workUnitId;
        }
      }
      rs.close();
      rs = stmt.executeQuery("SELECT size FROM zeta.server WHERE server_id=" + serverId);
      if (rs.next()) {
        long size = rs.getLong(1);
        rs.close();
        ServerSynchronization sync = new ServerSynchronization(stmt.getConnection());
        sync.synchronization(new Integer(serverId));
        // ToDo: it is a critical situation, this method must be locked at all servers up to the sync.synchronization(null) below
        long workUnitIdOverlap = Parameter.getValue(stmt, "work_unit_id_overlap", taskId, 0, 3600000);
        rs = stmt.executeQuery("SELECT MAX(work_unit_id+size)-" + workUnitIdOverlap
                               + " FROM zeta.server_size WHERE task_id=" + taskId + " AND work_unit_id>" + workUnitIdComplete);
        serverMaxWorkUnitId[0] = (rs.next())? rs.getLong(1) : 0;
        serverMaxWorkUnitId[1] = size;
        rs.close();
        DatabaseUtils.executeAndLogUpdate(servlet,
                                          "INSERT INTO zeta.server_size (server_id,task_id,work_unit_id,size,start) VALUES ("
                                          + serverId + ',' + taskId + ',' + serverMaxWorkUnitId[0] + ',' + serverMaxWorkUnitId[1] + ",CURRENT TIMESTAMP)");
        sync.synchronization(null);
        updateServerMaxWorkUnitId = Boolean.TRUE;
      } else {
        throw new SQLException("Table 'zeta.server' is not defined.");
      }
      return 0;
    }
  }

  /**
   *  Cache for parameters.
  **/
  private static Map mapValue = new HashMap();

  /**
   *  <code>true</code> if the attribute <code>serverMaxWorkUnitId</code> has to retrieve from the database.
  **/
  private static Boolean updateServerMaxWorkUnitId = Boolean.TRUE;

  /**
   *  Values which are returned from the method <code>getServerMaxWorkUnitId</code>.
  **/
  private static long[] serverMaxWorkUnitId = new long[2];
}
