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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import zeta.server.Server;
import zeta.server.tool.Database;
import zeta.server.util.DatabaseUtils;
import zeta.util.ThrowableHandler;


/**
 *  @version 2.0, August 6, 2005
**/
public class Recomputation {
  public static void main(String[] args) {
    try {
      if (args.length == 7 || args.length == 8) {
        String parameters = (args.length == 8)? args[5] : null;
        Recomputation recomp = new Recomputation(args[args.length-2], args[args.length-1]);
        recomp.recomp(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Long.parseLong(args[3]), Integer.parseInt(args[4]), parameters);
        recomp.close();
      } else if (args.length == 5 || args.length == 6) {
        String parameters = (args.length == 6)? args[3] : null;
        Recomputation recomp = new Recomputation(args[args.length-2], args[args.length-1]);
        recomp.recomp(Integer.parseInt(args[0]), Long.parseLong(args[1]), Integer.parseInt(args[2]), parameters);
        recomp.close();
      } else {
        System.err.println("Usage: [<serverId> <workstationId>] <taskId> <workUnitId> <size> [<parameters>] <user> <password>");
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  private Recomputation(String user, String password) throws Exception {
    connection = Database.getConnection(user, password);
  }

  private void close() {
    DatabaseUtils.close(connection);
  }

  private void recomp(int taskId, long workUnitId, int size, String parameters) throws SQLException, IOException {
    parameters = (parameters == null)? "NULL" : "'" + parameters + '\'';
    int serverId = Server.getInstance(connection).getId();
    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      DatabaseUtils.executeAndLogUpdate(serverId, stmt,
                                        "INSERT INTO zeta.recomputation (task_id,work_unit_id,size,server_id,parameters) VALUES ("
                                        + taskId + ',' + workUnitId + ',' + size + ',' + serverId + ',' + parameters + ')');
    } finally {
      DatabaseUtils.close(stmt);
    }
  }

  private void recomp(int serverId, int workstationId, int taskId, long workUnitId, int size, String parameters) throws SQLException {
    parameters = (parameters == null)? "NULL" : "'" + parameters + '\'';
    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      DatabaseUtils.executeAndLogUpdate(serverId, stmt,
                                        "INSERT INTO zeta.recomputation (task_id,work_unit_id,size,server_id,workstation_id,parameters) VALUES ("
                                        + taskId + ',' + workUnitId + ',' + size + ',' + serverId + ',' + workstationId + ',' + parameters + ')');
    } finally {
      DatabaseUtils.close(stmt);
    }
  }


  private Connection connection = null;
}
