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

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.util.Query;
import zeta.server.util.QueryWithSum;
import zeta.util.Table;

/**
 *  Handles a GET request for the statistic 'operating systems'.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class OperatingSystemsHandler extends AbstractHandler {
  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public OperatingSystemsHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    super(servlet);
  }

  /**
   *  Creates HTML page with the content of the statistic 'operating systems'.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of the statistic 'operating systems'.
  **/
  public String createPage(int taskId, Connection con) throws SQLException, ServletException {
    StringBuffer buffer = new StringBuffer(70*1024);
    buffer.append("<tr><td height=\"30pt\" class=\"second-head-gray\"><center>Under which operating systems were the zeros verified:</center></td></tr>");
    buffer.append("<tr><td><br><center>");
    HtmlTableGenerator generator = new HtmlTableGeneratorWithSum(servlet);
    Table table = new QueryWithSum("WITH a (server_id,workstation_id,os_name,os_arch,ws_zeros) AS"
                                 + " (SELECT comp.server_id,comp.workstation_id,"
                                 + "  ws.os_name,ws.os_arch,SUM(CAST(comp.size AS DECIMAL(15, 0))) AS ws_zeros"
                                 + "   FROM zeta.computation comp, zeta.result res, zeta.workstation ws"
                                 + "   WHERE res.task_id=comp.task_id"
                                 + "   AND comp.task_id=" + taskId
                                 + "   AND res.work_unit_id=comp.work_unit_id"
                                 + "   AND comp.server_id=ws.server_id"
                                 + "   AND comp.workstation_id=ws.id"
                                 + "   AND ws.os_arch<>''"
                                 + "   GROUP BY comp.server_id,comp.workstation_id,ws.os_name,ws.os_arch)"
                                 + "SELECT os_name AS \"operating system\",os_arch AS \"processor\","
                                 + "COUNT(*) AS \"computers\",SUM(ws_zeros) AS \"zeros\""
                                 + " FROM a GROUP BY os_name,os_arch ORDER BY os_name,os_arch", con, servlet).getResult();
    Table table2 = new Query("WITH a (server_id,workstation_id,os_name,os_arch,ws_zeros) AS"
                           + " (SELECT comp.server_id,comp.workstation_id,"
                           + "  ws.os_name,ws.os_arch,SUM(CAST(comp.size AS DECIMAL(15, 0))) AS ws_zeros"
                           + "   FROM zeta.recomputation comp, zeta.workstation ws"
                           + "   WHERE comp.server_id=ws.server_id"
                           + "   AND comp.workstation_id=ws.id"
                           + "   AND comp.task_id=" + taskId
                           + "   AND ws.os_arch<>''"
                           + "   GROUP BY comp.server_id,comp.workstation_id,ws.os_name,ws.os_arch)"
                           + "SELECT os_name AS \"operating system\",os_arch AS \"processor\","
                           + "COUNT(*) AS \"computers\",SUM(ws_zeros) AS \"zeros\""
                           + " FROM a GROUP BY os_name,os_arch ORDER BY os_name,os_arch", con, servlet).getResult();
    table.addAndOrderByLastDesc(table2, new String[] { "operating system", "processor"}, null, false);
    extendPercent(table);
    buffer.append(generator.generate(table));
    buffer.append("</center></td></tr>");
    return buffer.toString();
  }

  private void extendPercent(Table table) {
    int l = table.getRowCount()-1;
    int columns = table.getColumnCount();
    long sum1 = 0;
    long sum2 = 0;
    for (int i = 0; i < l; ++i) {
      Number value = (Number)table.getValue(i, columns-2);
      if (value != null) {
        sum1 += value.intValue();
      }
      value = (Number)table.getValue(i, columns-1);
      if (value != null) {
        sum2 += value.longValue();
      }
    }
    table.insertColumn(columns-1);
    table.insertColumn(columns+1);
    table.setColumnName(columns-1, "percentage");
    table.setColumnName(columns+1, "percentage");
    table.setAlignment(columns-1, Table.RIGHT);
    table.setAlignment(columns+1, Table.RIGHT);
    table.setType(columns-1, Types.CHAR);
    table.setType(columns+1, Types.CHAR);
    DecimalFormat format = new DecimalFormat("#,##0.00%");
    table.setFormat(columns-1, format);
    table.setFormat(columns+1, format);
    for (int i = 0; i < l; ++i) {
      Number value = (Number)table.getValue(i, columns-2);
      if (value != null) {
        table.setValue(i, columns-1, new Double(value.doubleValue()/sum1));
      }
      value = (Number)table.getValue(i, columns);
      if (value != null) {
        table.setValue(i, columns+1, new Double(value.doubleValue()/sum2));
      }
    }
  }
}
