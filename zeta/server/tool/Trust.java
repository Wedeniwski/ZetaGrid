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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import zeta.server.Server;
import zeta.server.util.DatabaseUtils;
import zeta.util.ThrowableHandler;


/**
 *  @version 2.0, August 6, 2005
**/
public class Trust {

  public static void main(String[] args) {
    try {
      Trust trust = new Trust();
      trust.trust();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  public Trust() throws Exception {
  }

  public void trust() {
    Statement stmt = null;
    Connection connection = null;
    try {
      connection = Database.getConnection();
      int serverId = Server.getInstance(connection).getId();
      stmt = connection.createStatement();
      int[] trust = null;
      ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM zeta.user WHERE server_id=" + serverId);
      if (rs.next()) {
        trust = new int[rs.getInt(1)+1];
      }
      rs.close();
      if (trust == null) {
        return;
      }
      StringBuffer trustedUsers = new StringBuffer(8*trust.length);
      trustedUsers.append('(');
      rs = stmt.executeQuery("SELECT id,trust FROM zeta.user WHERE server_id=" + serverId + " AND NOT (name='anonymous' AND email='')");
      while (rs.next()) {
        int userId = rs.getInt(1);
        int trustValue = rs.getInt(2);
        trust[userId] = trustValue;
        if (trustValue >= 2) {
          if (trustedUsers.length() > 1) {
            trustedUsers.append(',');
          }
          trustedUsers.append(userId);
        }
      }
      rs.close();
      trustedUsers.append(')');
      StringBuffer users1 = new StringBuffer(8*trust.length);
      users1.append('(');
      rs = stmt.executeQuery("SELECT a.user_id,SUM(CAST(a.size AS DECIMAL(15, 0))) FROM zeta.computation a WHERE a.server_id=" + serverId
                           + " AND a.work_unit_id IN (SELECT work_unit_id FROM zeta.result WHERE task_id=a.task_id AND work_unit_id=a.work_unit_id)"
                           + ((trustedUsers.length() > 2)? " AND a.user_id NOT IN " + trustedUsers.toString() : "")
                           + " GROUP BY a.user_id");
      trustedUsers.delete(1, trustedUsers.length());
      while (rs.next()) {
        int userId = rs.getInt(1);
        long size = rs.getLong(2);
        if (size >= 100000000) {
          if (trustedUsers.length() > 1) {
            trustedUsers.append(',');
          }
          trustedUsers.append(userId);
        } else if (size >= 10000000 && trust[userId] == 0) {
          if (users1.length() > 1) {
            users1.append(',');
          }
          users1.append(userId);
        }
      }
      rs.close();
      trustedUsers.append(')');
      users1.append(')');
      if (trustedUsers.length() > 2) {
        DatabaseUtils.executeAndLogUpdate(serverId, stmt, "UPDATE zeta.user SET trust=2 WHERE server_id=" + serverId + " AND id IN " + trustedUsers.toString());
      }
      if (users1.length() > 2) {
        DatabaseUtils.executeAndLogUpdate(serverId, stmt, "UPDATE zeta.user SET trust=1 WHERE server_id=" + serverId + " AND id IN " + users1.toString());
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(connection);
    }
  }
}
