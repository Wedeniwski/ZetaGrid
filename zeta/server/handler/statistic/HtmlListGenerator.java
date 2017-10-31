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

import java.text.Format;

import zeta.util.Table;


/**
 *  Generates a list of HTTP tables.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class HtmlListGenerator {

  public HtmlListGenerator() {
  }

  public String generate(Table table) {
    if (table == null) return "";
    final int rows = table.getRowCount();
    int columns = table.getColumnCount();
    if (rows == 0 || columns == 0) return "";
    StringBuffer sb = new StringBuffer(rows*columns*30);
    sb.append("\n<style type=\"text/css\">\n.c { text-align:center }\n.r { text-align:right }\n</style>");
    for (int row = 0; row < rows; ++row) {
      sb.append("\n<table border=\"1\" cellspacing=\"0\" width=\"90%\"><colgroup><col width=\"50\"/><col width=\"50\"/></colgroup>");
      for (int col = 0; col < columns; ++col) {
        Object value = table.getValue(row, col);
        if (value != null) {
          sb.append("\n<tr><td bgcolor=\"#dddddd\"><b>");
          sb.append(table.getColumnName(col));
          sb.append("</b></td><td");
          generateBackgound(table, row, col, sb);
          switch (table.getAlignment(col)) {
            case Table.LEFT:
              break;
            default:
            case Table.CENTER:
              sb.append(" class=\"c\"");
              break;
            case Table.RIGHT:
              sb.append(" class=\"r\"");
              break;
          }
          sb.append('>');
          generateCell(row, col, table, sb);
          sb.append("</td></tr>");
        }
      }
      sb.append("\n</table>");
    }
    return sb.toString();
  }

  protected void generateBackgound(Table table, int row, int col, StringBuffer sb) {
    if ((col&1) == 1) {
      sb.append(" bgcolor=\"#eeeeee\"");
    }
  }

  protected void generateCell(int row, int col, Table table, StringBuffer sb) {
    Object value = table.getValue(row, col);
    if (value != null) {
      Format format = table.getFormat(col);
      sb.append((format == null)? value.toString() : format.format(value));
    } else {
      sb.append("&nbsp;");
    }
  }
}
