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

package zeta.server.handler.approve;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.DatabaseUtils;

/**
 *  Approve a request to change a team name.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class ApproveTeamName extends ApproveBase {
  /**
   *  @param servlet servlet which owns the approve handler.
   *  @param userId id of the user where the data will be changed.
   *  @param user name of the user.
   *  @param eMail e-mail address of the user.
  **/
  public ApproveTeamName(DispatcherServlet servlet, int userId, String user, String eMail) {
    super(servlet, userId, user, eMail);
  }

  /**
   *  Returns the key name of the change request.
   *  A key name should contains only alpha characters.
   *  @return the key name of the change request.
  **/
  public String getKey() {
    return "team name";
  }

  /**
   *  Returns a row name which will be need as an additional information in the approve process.
   *  @param stmt statement object's database
   *  @param value which will be changed.
   *  @param timeMillis timestamp of the request.
   *  @return HTML text as information of the change.
  **/
  public String approve(Statement stmt, String value, long timeMillis) throws SQLException, ServletException {
    if (value == null || value.equals("null") || value.length() == 0) {
      DatabaseUtils.executeAndLogUpdate(servlet, "UPDATE zeta.user SET (team_name,join_in_team)=(NULL,'" + (new Timestamp(timeMillis)) + "') WHERE server_id=" + servlet.getServer().getId() + " AND id=" + userId);
      String result = "<html><body>The user '" + user + "' is successfully removed from the team.</body></html>";
      ResultSet rs = stmt.executeQuery("SELECT team_name FROM zeta.user WHERE server_id=" + servlet.getServer().getId() + " AND id=" + userId);
      if (rs.next()) {
        result = "<html><body>The user '" + user + "' is successfully removed from the team '" + rs.getString(1) + "'.</body></html>";
      }
      rs.close();
      return result;
    } else {
      if (servlet == null) {
        System.out.println("UPDATE zeta.user SET (team_name,join_in_team)=('" + value + "','" + (new Timestamp(timeMillis)) + "') WHERE server_id= AND id=" + userId);
      } else {
        DatabaseUtils.executeAndLogUpdate(servlet, "UPDATE zeta.user SET (team_name,join_in_team)=('" + value + "','" + (new Timestamp(timeMillis)) + "') WHERE server_id=" + servlet.getServer().getId() + " AND id=" + userId);
      }
      return "<html><body>The user '" + user + "' is successfully added to the team '" + value + "'.</body></html>";
    }
  }
}
