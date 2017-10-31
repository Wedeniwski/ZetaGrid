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

package zeta.server.handler.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.TaskManager;
import zeta.server.handler.GetHandler;
import zeta.server.handler.statistic.HtmlTableGenerator;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Query;
import zeta.util.Table;

/**
 *  @version 2.0, August 6, 2005
**/
public class ActiveRecomputationReportHandler implements GetHandler {

  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public ActiveRecomputationReportHandler(DispatcherServlet servlet) {
    this.servlet = servlet;
  }

  /**
   *  Handles a GET request <code>resp</code> for the specified statistic.
   *  The response <code>resp</code> contains either the HTML page or an image.
  **/
  final public void doGet(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Connection con = null;
    try {
      con = servlet.getConnection();
      StringBuffer buffer = new StringBuffer(5000);
      buffer.append("<tr><td><br><form type=\"GET\" action=\"");
      String servletPath = servlet.getRootPath() + servlet.getHandlerPath(getClass());
      buffer.append(servletPath);
      buffer.append("\">Task name: <select name=\"task\">");
      TaskManager taskManager = TaskManager.getInstance(con);
      Iterator iter = taskManager.getServerTasks();
      while (iter.hasNext()) {
        ServerTask serverTask = (ServerTask)iter.next();
        if (task == null) {
          task = serverTask;
        }
        buffer.append("<option value=\"");
        buffer.append(serverTask.getName());
        buffer.append((serverTask.getId() == task.getId())? "\" selected>" : "\">");
        buffer.append(serverTask.getName());
      }
      buffer.append("</select>&nbsp;&nbsp;<input type=\"submit\" value=\"Select\"></form></td></tr>");
      buffer.append("<tr><td><br><center>");
      HtmlTableGenerator generator = new HtmlTableGenerator(servlet);
      int taskId = (task == null)? 0 : task.getId();
      Table table = new Query("SELECT DISTINCT work_unit_id AS \"work unit\","
                            + "version AS \"version\","
                            + "start AS \"start\","
                            + "server_id AS \"server\","
                            + "user_id AS \"user\","
                            + "workstation_id AS \"workstation\","
                            + "size AS \"size\""
                            + " FROM zeta.recomputation"
                            + " WHERE task_id=" + taskId
                            + " AND stop IS NULL"
                            + " ORDER BY start,work_unit_id", con, servlet).getResult();
      buffer.append(generator.generate(table));
      buffer.append("</center></td></tr>");
      resp.setContentType("text/html");
      resp.setContentLength(buffer.length());
      resp.getWriter().print(buffer.toString());
    } catch (SQLException se) {
      throw new ServletException(se);
    } finally {
      DatabaseUtils.close(con);
    }
  }

  /**
   *  Timestamp in millis of the last update of the data of the page.
   *  Returns always the current timestamp.
   *  @return always the current timestamp.
  **/
  public long getTimestampOfPage() {
    return System.currentTimeMillis();
  }

  /**
   *  Servlet which owns this handler.
  **/
  private DispatcherServlet servlet;
}
