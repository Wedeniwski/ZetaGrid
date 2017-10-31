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
 *  Serializes a table.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class ListGenerator {

  public ListGenerator() {
  }

  public String generate(Table table) {
    if (table == null) {
      return "";
    }
    final int rows = table.getRowCount();
    final int columns = table.getColumnCount();
    if (rows == 0 || columns == 0) {
      return "";
    }
    StringBuffer sb = new StringBuffer(rows*columns*30);
    for (int row = 0; row < rows; ++row) {
      for (int col = 0; col < columns; ++col) {
        Object value = table.getValue(row, col);
        if (value != null) {
          sb.append(table.getColumnName(col));
          Format format = table.getFormat(col);
          sb.append((format == null)? value.toString() : format.format(value));
          sb.append('\n');
        }
      }
    }
    return sb.toString();
  }

  public String generateHTML(String content, CodedList list) {
    StringBuffer sb = new StringBuffer(2*content.length());
    sb.append("\n<table border=\"1\" cellspacing=\"0\" width=\"90%\"><colgroup><col width=\"50\"/><col width=\"50\"/></colgroup>");
    final int lc = content.length();
    for (int i = 0, col = 0; i < lc; ++i, ++col) {
      int j = content.indexOf('\n', i);
      sb.append("\n<tr><td bgcolor=\"#dddddd\"><b>");
      sb.append(list.decode(content, i));
      sb.append("</b></td><td");
      generateBackgound(0, col, sb);
      switch (list.getAlignment(content, i)) {
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
      sb.append(list.decodeValue(content, i, j));
      sb.append("</td></tr>");
      if (j == -1) {
        break;
      }
      i = j;
    }
    sb.append("\n</table>");
    return sb.toString();
  }

  protected void generateBackgound(int row, int col, StringBuffer sb) {
    if ((col&1) == 1) {
      sb.append(" bgcolor=\"#eeeeee\"");
    }
  }
}
