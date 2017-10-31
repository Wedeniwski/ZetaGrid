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
     H. Haddorp
     S. Wedeniwski
--*/

package zeta.server.handler.statistic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.Format;

import zeta.server.DispatcherServlet;
import zeta.util.Table;

/**
 *  Generates an HTTP table.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class HtmlTableGenerator {

  public HtmlTableGenerator(DispatcherServlet servlet) {
    this(servlet, null, null);
  }

  public HtmlTableGenerator(DispatcherServlet servlet, String linkAddress, String linkColumnName) {
    this.servlet = servlet;
    this.linkAddress = linkAddress;
    this.linkColumnName = linkColumnName;
  }

  public String generate(Table table) {
    if (table == null) return "";
    final int rows = table.getRowCount();
    int columns = table.getColumnCount();
    StringBuffer sb = new StringBuffer(rows*columns*20);
    sb.append("\n<style type=\"text/css\">\n.c { text-align:center }\n.r { text-align:right }\n</style>");
    sb.append("\n<table border=\"1\" cellspacing=\"0\" width=\"90%\"><colgroup>");
    boolean trend = (columns > 0 && table.getColumnName(1) != null && table.getColumnName(1).equals("trend"));
    boolean place = (columns > 0 && table.getColumnName(0) != null && table.getColumnName(0).equals("place"));
    for (int i = 0; i < columns; ++i) {
      sb.append("<col width=\"");
      if (place && trend) {
        if (i == 2) sb.append(125/columns);
        else if (i == 0 || i == 1) sb.append(5);
        else if (i == columns-1) sb.append(100/columns);
        else sb.append(80/columns);
      } else if (place || trend) {
        if (i == 1) sb.append(100/columns);
        else if (i == 0) sb.append(5);
        else sb.append(80/columns);
      } else sb.append(100/columns);
      sb.append("%\"/>");
    }
    sb.append("</colgroup>\n<tr bgcolor=\"#dddddd\">");
    int linkColumnNameIdx = -1;
    for (int i = 0; i < columns; ++i) {
      sb.append("<th>");
      String s = table.getColumnName(i);
      if (s == null) {
        s = "";
      }
      if (s.equals(linkColumnName)) {
        linkColumnNameIdx = i;
      }
      sb.append(s);
      sb.append("</th>");
    }
    sb.append("</tr>\n");
    for (int row = 0; row < rows; ++row) {
      sb.append("<tr");
      generateBackgound(table, row, sb);
      sb.append('>');
      for (int col = 0; col < columns; ++col) {
        sb.append("<td");
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
        generateCell(row, col, (linkColumnNameIdx == col), table, sb);
        sb.append("</td>");
      }
      sb.append("</tr>\n");
    }
    sb.append("</table>\n");
    return sb.toString();
  }

  protected void generateBackgound(Table table, int row, StringBuffer sb) {
    if ((row&1) == 1) {
      sb.append(" bgcolor=\"#eeeeee\"");
    }
  }

  protected void generateCell(int row, int col, boolean link, Table table, StringBuffer sb) {
    Object value = table.getValue(row, col);
    if (value != null) {
      if (link && linkAddress != null && linkColumnName != null) {
        String s = value.toString();
        sb.append("<a href=\"");
        sb.append(linkAddress);
        sb.append('?');
        sb.append(linkColumnName);
        sb.append('=');
        try {
          sb.append(URLEncoder.encode(s, "ISO-8859-1"));
        } catch (UnsupportedEncodingException uee) {
        }
        sb.append("\">");
        sb.append(s);
        sb.append("</a>");
      } else {
        Format format = table.getFormat(col);
        if (format == null) {
          switch (table.getType(col)) {
            case Types.DECIMAL    :
            case Types.INTEGER    :
            case Types.NUMERIC    :
              format = new DecimalFormat("#,###");
              sb.append(format.format(value));
              break;
            default:
              sb.append(value);
              break;
          }
        } else {
          sb.append(format.format(value));
        }
      }
    } else {
      sb.append("&nbsp;");
    }
  }

  protected DispatcherServlet servlet;
  private String linkAddress;
  private String linkColumnName;
}
