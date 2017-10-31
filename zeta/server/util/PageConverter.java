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

package zeta.server.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import zeta.util.StringUtils;

/**
 *  Converts the data of an HTML table to XML.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class PageConverter {
  /**
   *  Converts an HTML table to a XML format.
   *  @param pageHTML table in HTML format
   *  @return converted data in XML format
  **/
  public static String convertHTMLTableToXML(String pageHTML, Timestamp lastUpdate) {
    StringBuffer buffer = new StringBuffer(10*pageHTML.length());
    buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
    buffer.append("<statistic created=\"");
    buffer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastUpdate));
    buffer.append("\">\n");
    int i = pageHTML.indexOf("<table", pageHTML.indexOf("</style>"));
    if (i >= 0) {
      int pl = pageHTML.indexOf("</table>", i);
      List columns = new ArrayList(10);
      while (true) {
        int j = pageHTML.indexOf("<th>", i);
        if (j < 0 || j > pl) {
          break;
        }
        i = j;
        j = pageHTML.indexOf("</th>", i+4);
        if (j <= i+4) {
          break;
        }
        String s = pageHTML.substring(i+4, j);
        int k = s.indexOf('(');
        if (k > 0) {
          s = s.substring(0, (s.charAt(k-1) == ' ')? k-1 : k);
        }
        columns.add(s.replace(' ', '_'));
        i = j;
      }
      final int l = columns.size();
      if (l > 0) {
        for (int row = 1; i >= 0 && i < pl; ++row) {
          i = pageHTML.indexOf("<tr", i);
          if (i < 0) {
            break;
          }
          boolean endData = false;
          for (int k = 0; k < l; ++k) {
            i = pageHTML.indexOf("<td", i);
            if (i < 0) {
              break;
            }
            for (i += 3; i < pl && pageHTML.charAt(i) != '>'; ++i);
            if (++i >= pl) {
              break;
            }
            int j = pageHTML.indexOf("</td>", i);
            if (j <= i || pageHTML.startsWith("<span ", i) || pageHTML.startsWith("&sum;", i)) {
              break;
            }
            if (k == 0) {
              buffer.append("<data row=\"");
              buffer.append(row);
              buffer.append("\">\n");
              endData = true;
            }
            buffer.append(" <");
            buffer.append(columns.get(k));
            buffer.append('>');
            buffer.append(formatValue(pageHTML.substring(i, j)));
            buffer.append("</");
            buffer.append(columns.get(k));
            buffer.append(">\n");
            i = j;
          }
          if (endData) {
            buffer.append("</data>\n");
          }
        }
      }
    }
    buffer.append("</statistic>");
    return buffer.toString();
  }

  private static String formatValue(String value) {
    if (value.startsWith("<a href=")) {
      int i = value.indexOf("\">", 8);
      int j = value.indexOf("</a>", i+2);
      if (j > i && i > 0) {
        return encodeXML(value.substring(i+2, j));
      }
    }
    return encodeXML(StringUtils.replace(value, "&nbsp;", ""));
  }

  private static String encodeXML(String xml) {
    int l = xml.length();
    StringBuffer buffer = new StringBuffer(l+20);
    for (int i = 0 ; i < l; ++i) {
      char c = xml.charAt(i);
      if (c == '&') {
        buffer.append("&amp;");
      } else if (c == '<') {
        buffer.append("&lt;");
      } else if (c == '>') {
        buffer.append("&gt;");
      } else if (c == '"') {
        buffer.append("&quot;");
      } else if (((int)c) < 32) {
        buffer.append("&#");
        buffer.append((int)c);
        buffer.append(';');
      } else {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }
}
