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

package zeta.server.handler.statistic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.CachedQueries;
import zeta.server.util.DatabaseUtils;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'workstations'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class WorkstationsHandler extends AbstractHandler implements CodedList {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public WorkstationsHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'workstations'
   *  where the user is not defined.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'workstations'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    List workstations = new ArrayList(100000);
    final int workstationsRecordSize = 7;
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      final Integer NULL = new Integer(0);
      ResultSet rs = stmt.executeQuery("SELECT server_id,id,hostname,number_of_redistributions,last_redistributed_work_unit,last_redistributed_timestamp FROM zeta.workstation WHERE id>0 ORDER BY server_id,id");
      while (rs.next()) {
        short serverId = rs.getShort(1);
        workstations.add(new Short(serverId));
        workstations.add(new Integer(rs.getInt(2)));
        workstations.add(rs.getString(3).toLowerCase() + " (" + (serverId-1) + ')');
        workstations.add(new Integer(rs.getInt(4)));
        workstations.add(new Long(rs.getLong(5)));
        workstations.add(rs.getTimestamp(6));
        workstations.add(NULL);
      }
      rs.close();
      final int l = workstations.size()-workstationsRecordSize+1;
      rs = stmt.executeQuery("SELECT server_id,workstation_id,COUNT(*) FROM zeta.computation WHERE task_id=" + taskId + " AND workstation_id>0 GROUP BY server_id,workstation_id");
      while (rs.next()) {
        Short serverId = new Short(rs.getShort(1));
        Integer wsId = new Integer(rs.getInt(2));
        int j = search(workstations, workstationsRecordSize, serverId, wsId);
        if (j < 0) {
          System.out.println("key not found: " + serverId + ", " + wsId);
        } else if (j < l) {
          workstations.set(j+6, new Integer(rs.getInt(3)));
        }
      }
      rs.close();
      DecimalFormat decimalFormatter = new DecimalFormat("#,###");
      SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyy/MM/dd");
      rs = stmt.executeQuery("SELECT server_id,workstation_id,COUNT(*) FROM zeta.computation comp, zeta.result res WHERE comp.task_id=res.task_id AND comp.task_id=" + taskId
                           + " AND comp.work_unit_id=res.work_unit_id AND comp.workstation_id>0 GROUP BY server_id,workstation_id");
      while (rs.next()) {
        Short serverId = new Short(rs.getShort(1));
        Integer wsId = new Integer(rs.getInt(2));
        int j = search(workstations, workstationsRecordSize, serverId, wsId);
        if (j < 0) {
          System.out.println("key not found: " + serverId + ", " + wsId);
        } else if (j < l) {
          int n = ((Integer)workstations.get(j+6)).intValue() - rs.getInt(3);
          workstations.set(j+6, (n <= 0)? NULL : new Integer(n));
        }
      }
      rs.close();

      /*rs = stmt.executeQuery("SELECT LOWER(b.name),b.email,c.server_id,c.id,DAYS(CURRENT TIMESTAMP)-DAYS(MIN(start)),COUNT(*),SUM(CAST(a.size AS DECIMAL(15, 0))),MAX(start)"
                           + " FROM zeta.computation a,zeta.user b,zeta.workstation c"
                           + " WHERE a.server_id=b.server_id"
                           + " AND a.task_id" + taskId
                           + " AND a.user_id=b.id"
                           + " AND a.server_id=c.server_id"
                           + " AND a.workstation_id=c.id"
                           + " GROUP BY LOWER(b.name),b.email,c.server_id,c.id ORDER BY LOWER(b.name),b.email DESC,MAX(start) DESC,c.id");*/
      Table table = new Table(12);
      table.setColumnName(0, encode("user"));
      table.setType(0, Types.VARCHAR);
      table.setAlignment(0, Table.LEFT);
      table.setColumnName(1, encode("age (days)"));
      table.setType(1, Types.INTEGER);
      table.setAlignment(1, Table.RIGHT);
      table.setFormat(1, decimalFormatter);
      table.setColumnName(2, encode("work units"));
      table.setType(2, Types.INTEGER);
      table.setAlignment(2, Table.RIGHT);
      table.setFormat(2, decimalFormatter);
      table.setColumnName(3, encode("zeros"));
      table.setType(3, Types.NUMERIC);
      table.setAlignment(3, Table.RIGHT);
      table.setFormat(3, decimalFormatter);
      table.setColumnName(4, encode("number of active work units"));
      table.setType(4, Types.INTEGER);
      table.setAlignment(4, Table.RIGHT);
      table.setFormat(4, decimalFormatter);
      table.setColumnName(5, encode("last work unit was requested at"));
      table.setType(5, Types.TIMESTAMP);
      table.setAlignment(5, Table.RIGHT);
      table.setFormat(5, simpleDateFormatter);
      table.setColumnName(6, encode("number of redistributed work units"));
      table.setType(6, Types.INTEGER);
      table.setAlignment(6, Table.RIGHT);
      table.setFormat(6, decimalFormatter);
      table.setColumnName(7, encode("last redistributed work unit ID"));
      table.setType(7, Types.NUMERIC);
      table.setAlignment(7, Table.RIGHT);
      table.setFormat(7, decimalFormatter);
      table.setColumnName(8, encode("last redistributed work unit at"));
      table.setType(8, Types.TIMESTAMP);
      table.setAlignment(8, Table.RIGHT);
      table.setFormat(8, simpleDateFormatter);
      table.setColumnName(9, "min start");
      table.setType(9, Types.TIMESTAMP);
      table.setAlignment(9, Table.RIGHT);
      table.setColumnName(10, "max stop");
      table.setType(10, Types.TIMESTAMP);
      table.setAlignment(10, Table.RIGHT);
      table.setColumnName(11, "team");
      table.setType(11, Types.VARCHAR);
      table.setAlignment(11, Table.LEFT);
      table.setHiddenColumnCount(3);

      Map names = CachedQueries.getUserNames(con);
      Table userDataTable = CachedQueries.getUserData(con);
      rs = stmt.executeQuery("SELECT a.server_id,a.workstation_id,a.user_id,"
                           + " DAYS(CURRENT TIMESTAMP)-DAYS(MIN(start)),COUNT(*),SUM(CAST(a.size AS DECIMAL(15, 0))),MAX(start),MIN(start)"
                           + " FROM zeta.computation a,zeta.result b,zeta.workstation c"
                           + " WHERE a.task_id=b.task_id"
                           + " AND a.task_id=" + taskId
                           + " AND a.work_unit_id=b.work_unit_id"
                           + " AND a.server_id=c.server_id"
                           + " AND a.workstation_id=c.id AND a.workstation_id>0"
                           + " GROUP BY a.server_id,a.workstation_id,a.user_id");
      while (rs.next()) {
        Short serverId = new Short(rs.getShort(1));
        Integer wsId = new Integer(rs.getInt(2));
        int j = search(workstations, workstationsRecordSize, serverId, wsId);
        if (j < 0) {
          System.out.println("key not found: " + serverId + ", " + wsId);
        } else if (j < l) {
          Integer userId = new Integer(rs.getInt(3));
          int i = -1;
          do {
            i = userDataTable.indexOfRow(userId, 9, ++i);
          } while (i >= 0 && !serverId.equals(userDataTable.getValue(i, 8)));
          if (i >= 0) {
            String name = ((String)userDataTable.getValue(i, 0)).toLowerCase();
            String email = (String)userDataTable.getValue(i, 1);
            String teamname = (String)userDataTable.getValue(i, 5);
            if (teamname != null) {
              teamname = teamname.trim().toLowerCase();
            }
            String name2 = (String)names.get(name);
            name = ((name2 == null)? name.trim() : name2.trim()) + ',' + email + ',' + workstations.get(j+2);
            int row = table.indexOfRow(name, 0);
            if (row >= 0) {
              table.setValue(row, 1, new Integer(Math.max(((Integer)table.getValue(row, 1)).intValue(), rs.getInt(4))));
              table.setValue(row, 2, new Integer(((Integer)table.getValue(row, 2)).intValue() + rs.getInt(5)));
              table.setValue(row, 3, new Long(((Long)table.getValue(row, 3)).longValue() + rs.getLong(6)));
              Timestamp t = rs.getTimestamp(8);
              Timestamp t2 = (Timestamp)table.getValue(row, 9);
              table.setValue(row, 9, (t.before(t2))? t : t2);
              t = rs.getTimestamp(7);
              if (t.after((Timestamp)table.getValue(row, 10))) {
                table.setValue(row, 10, t);
              }
            } else {
              row = table.getRowCount();
              table.addRow();
              table.setValue(row, 0, name);
              table.setValue(row, 1, new Integer(rs.getInt(4)));
              table.setValue(row, 2, new Integer(rs.getInt(5)));
              table.setValue(row, 3, new Long(rs.getLong(6)));
              table.setValue(row, 9, rs.getTimestamp(8));
              table.setValue(row, 10, rs.getTimestamp(7));
              table.setValue(row, 11, teamname);
            }
            Integer num = (Integer)workstations.get(j+6); // number of work units
            if (num != null && num.intValue() > 0) {
              table.setValue(row, 4, num);
              Timestamp t = (Timestamp)table.getValue(row, 5);
              if (t != null) {
                Timestamp t2 = rs.getTimestamp(7);
                table.setValue(row, 5, (t.before(t2))? t2 : t);
              } else {
                table.setValue(row, 5, rs.getTimestamp(7));
              }
              num = (Integer)workstations.get(j+3);
              if (num != null && num.intValue() > 0) {
                table.setValue(row, 6, num);
                table.setValue(row, 7, workstations.get(j+4));
                table.setValue(row, 8, workstations.get(j+5));
              }
            }
          }
        }
      }
      CachedQueries.setWorkstationTable(taskId, table);
      StringBuffer buffer = new StringBuffer(200*table.getRowCount());
      ListGenerator generator = new ListGenerator();
      buffer.append('\n');  // for easy search in TopProducersHandler
      buffer.append(generator.generate(table));
      return buffer.toString();
    } finally {
      DatabaseUtils.close(stmt);
    }
  }

  boolean isActive(String content) {
    return (content.indexOf("\n" + encode("last work unit was requested at")) >= 0);
  }

  public String encode(String column) {
    if (column.equals("user")) {
      return "Ul";
    } else if (column.equals("age (days)")) {
      return "Ar";
    } else if (column.equals("work units")) {
      return "Wr";
    } else if (column.equals("zeros")) {
      return "Zr";
    } else if (column.equals("number of active work units")) {
      return "Nr";
    } else if (column.equals("last work unit was requested at")) {
      return "Lr";
    } else if (column.equals("number of redistributed work units")) {
      return "Rr";
    } else if (column.equals("last redistributed work unit ID")) {
      return "Ir";
    } else if (column.equals("last redistributed work unit at")) {
      return "Tr";
    }
    return "";
  }

  public String decode(String content, int pos) {
    char c = content.charAt(pos);
    if (c == 'U') {
      return "user";
    } else if (c == 'A') {
      return "age (days)";
    } else if (c == 'W') {
      return "work units";
    } else if (c == 'Z') {
      return "zeros";
    } else if (c == 'N') {
      return "number of active work units";
    } else if (c == 'L') {
      return "last work unit was requested at";
    } else if (c == 'R') {
      return "number of redistributed work units";
    } else if (c == 'I') {
      return "last redistributed work unit ID";
    } else if (c == 'T') {
      return "last redistributed work unit at";
    }
    return "";
  }

  public String decodeValue(String content, int pos, int end) {
    return (end < 0)? content.substring(pos+2) : content.substring(pos+2, end);
  }

  public int getAlignment(String content, int pos) {
    char c = content.charAt(pos+1);
    if (c == 'l') {
      return Table.LEFT;
    } else if (c == 'r') {
      return Table.RIGHT;
    } else if (c == 'c') {
      return Table.CENTER;
    }
    return -1;
  }

  private int search(List workstations, int workstationsRecordSize, Short serverId, Integer wsId) {
  	int low = 0;
    int high = workstations.size()-workstationsRecordSize;
    while (low <= high) {
      int mid = (low+high) >> 1;
      mid -= mid%workstationsRecordSize;
      int cmp = serverId.compareTo((Short)workstations.get(mid));
      if (cmp == 0) {
        cmp = wsId.compareTo((Integer)workstations.get(mid+1));
      }
      if (cmp < 0) {
        high = mid-workstationsRecordSize;
      } else if (cmp > 0) {
        low = mid+workstationsRecordSize;
      } else {
        return mid; // key found
      }
    }
    return -low-workstationsRecordSize; // key not found
    /*int j = 0;
    while (j < l && (!wsId.equals(workstations.get(j+1)) || !serverId.equals(workstations.get(j)))) {
      j += workstationsRecordSize;
    }
    return j;*/
  }
}
