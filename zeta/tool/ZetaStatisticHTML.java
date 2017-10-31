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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zeta.server.Server;
import zeta.server.tool.ConstantProperties;
import zeta.server.tool.Database;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.util.ThrowableHandler;

/**
 *  @version 2.0, August 6, 2005
**/
public class ZetaStatisticHTML {

  public static void main(String[] args) throws Exception {
    generate();
  }

  public static void generate() throws Exception {
    long nStart = getLastGramBlock();
    System.out.println("Statistic: " + nStart);
    if (nStart > 0) {
      if (start == null) {
        Connection connection = null;
        Statement stmt = null;
        try {
          connection = Database.getConnection();
          stmt = connection.createStatement();
          start = Parameter.getValue(stmt, "work_unit_id_complete", null);
        } catch (SQLException se) {
          ThrowableHandler.handle(se);
        } finally {
          DatabaseUtils.close(stmt);
          DatabaseUtils.close(connection);
        }
      }
      if (start == null || !start.equals(String.valueOf(nStart))) {
        start = String.valueOf(nStart);
        StringBuffer buffer = new StringBuffer(50000);
        FileReader reader = new FileReader("statistic.log");
        String lastZip = prevGenerate(Long.parseLong(start), readFile(new File("rosser_blocks.txt")), new BufferedReader(reader), buffer);
        if (lastZip != null) {
          System.err.println(lastZip);
        } else {
          reader.close();
          File f1 = new File("statistic.txt");
          File f2 = new File("statistic.bak");
          if (f1.exists()) {
            f2.delete();
            f1.renameTo(f2);
          }
          FileWriter writer = new FileWriter(f1);
          writer.write(buffer.toString(), 0, buffer.length());
          writer.close();
          reader = new FileReader(f1);
          buffer.delete(0, buffer.length());
          generate(new BufferedReader(reader), buffer);
          writer = new FileWriter("html/statistic.html");
          writer.write(buffer.toString(), 0, buffer.length());
          writer.close();

          Connection connection = null;
          Statement stmt = null;
          try {
            connection = Database.getConnection();
            int serverId = Server.getInstance(connection).getId();
            stmt = connection.createStatement();
            Parameter.setValue(serverId, stmt, 1, "work_unit_id_complete", start, true);
          } catch (SQLException se) {
            System.out.println("UPDATE zeta.parameter SET value='" + start + "' WHERE parameter='work_unit_id_complete'");
            System.out.println("Please update 'index.html' manually!");
          } finally {
            DatabaseUtils.close(stmt);
            DatabaseUtils.close(connection);
          }
        }
        reader.close();
      }
    }
  }

  private static String prevGenerate(long pos, String gramBlocks, BufferedReader reader, StringBuffer buffer) throws IOException {
    String s;
    String lastZip = "";
    do {
      while (true) {
        s = reader.readLine();
        if (s == null) {
          System.err.println("Zip-files are smaller than " + pos + '.');
          return lastZip;
        }
        if (s.startsWith("zeta_zeros_") && s.endsWith(".zip")) {
          int i = s.indexOf('_', 11);
          if (i > 11) {
            long value = Long.parseLong(s.substring(11, i)) + Integer.parseInt(s.substring(i+1, s.length()-4));
            if (value+20 >= pos) break;
          }
          lastZip = s;
        }
      }
      s = reader.readLine();
      if (s == null || !s.equals("Number of Rosser blocks of given length")) {
        System.err.println("Read line '" + s + "' instead 'Number of Rosser blocks of given length'.");
        return lastZip;
      }
      s = reader.readLine();
      if (s == null) {
        System.err.println("Missing line 'Number of Rosser blocks of given length'.");
        return lastZip;
      }
    } while (s.length() > 0);
    buffer.append("Number of Rosser blocks of given length");
    buffer.append(newLine);
    buffer.append(gramBlocks);
    buffer.append(newLine);
    while (true) {
      s = reader.readLine();
      if (s == null) {
        System.err.println("Missing 'typesCount='.");
        return lastZip;
      }
      if (s.startsWith("typesCount=")) break;
      buffer.append(s);
      buffer.append(newLine);
    }
    return null;
  }

  private static void generate(BufferedReader reader, StringBuffer buffer) throws IOException {
    appendHeader(buffer);
    List table = null;
    int idx = 0;
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        appendTable(idx, table, buffer);
        break;
      }
      if (line.length() > 0) {
        if (line.charAt(0) != ' ') {
          if (table != null) appendTable(idx, table, buffer);
          table = new ArrayList(100);
          ++idx;
        } else {
          List list = new ArrayList(100);
          final int l = line.length();
          for (int i = 0; i < l; ++i) {
            StringBuffer value = new StringBuffer(10);
            while (i < l && !Character.isDigit(line.charAt(i)) && line.charAt(i) != '-') ++i;
            if (i < l) {
              while (true) {
                char c = line.charAt(i);
                if (!Character.isDigit(c) && c != '-') {
                  list.add(value.toString());
                  break;
                }
                value.append(c);
                if (++i == l) {
                  list.add(value.toString());
                  break;
                }
              }
            }
          }
          table.add(list);
        }
      }
    }
    appendFooter(buffer);
  }

  private static void appendHeader(StringBuffer buffer) throws IOException {
    DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getInstance(Locale.US);
    decimalFormat.setGroupingSize(3);
    buffer.append("<html><head><title>Riemann Hypothesis - Statistics</title></head>\n<body background=\"zeta.gif\">");
    buffer.append("<center><b><font size=+2>Statistics of the Computational Results</font></b></center><hr width=\"100%\">\n");
    buffer.append("<font size=-2>Last update: ");
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    buffer.append(formatter.format(new Date()));
    buffer.append("</font><p>\n");
    buffer.append("Here we present some statistics concerning Rosser blocks (similar to [4]) in the interval [g<sub>-1</sub>, g<sub>");
    decimalFormat.format(Long.parseLong(start), buffer, new FieldPosition(0));
    buffer.append("</sub>[ where g<sub>m</sub> is the <i>m</i>th Gram point.");
    buffer.append("This report presents specific results which were obtained during the numerical verification of the Riemann Hypothesis. Therefore, we recommend the reader to study the four papers by");
    buffer.append("<a href=\"http://web.comlab.ox.ac.uk/oucl/work/richard.brent/pub/pub047.html\">Brent [1]</a>,");
    buffer.append("<a href=\"http://web.comlab.ox.ac.uk/oucl/work/richard.brent/pub/pub070.html\">Brent et al. [2]</a>, and van de Lune et al. [2], [3] for an easy understanding of this report and the notation.");
    buffer.append("<br>A previous version of my program was organized in such a way that in case the value of Z(t), ");
    buffer.append("obtained with method A, was too small for a rigorous sign determination, a few small shifts of the arguments ");
    buffer.append("were tried before method B was involved. Therefore, my program uses for j < ");
    decimalFormat.format(Double.parseDouble(getLastT()), buffer, new FieldPosition(0));
    buffer.append(", in relatively few cases, an approximation ");
    buffer.append("to the Gram point g<sub>j</sub> instead of g<sub>j</sub> itself. Consequently, the statistics ");
    buffer.append("presented in this section cannot be accumulated to the statistics found by [1].");

    buffer.append("<p>More details about the distribution of the verified zeros in form of statistical data");
    buffer.append("which contains interesting zero-patterns and at moment <a href=\"/zeta/ZetaGrid-Conference_in_honour_of_Hugh_Williams_2003.pdf\">unproved heuristics</a>");
    buffer.append("can be found in the draft <a href=\"/zeta/math/zeta.result.100billion.zeros.html\">Results connected with the first 100 billion zeros of the Riemann zeta function</a>.");
  }

  private static void appendFooter(StringBuffer buffer) {
    buffer.append("<br>&nbsp;<br>&nbsp;");
    buffer.append("<p><b>Bibliography.</b>");

    buffer.append("<p>[1] R. P. Brent, <i>On the Zeros of the Riemann Zeta Function in the Critical Strip</i>, Mathematics of Computation <b>33</b> (1979), 1361-1372.\n");
    buffer.append("<p>[2] R. P. Brent, J. van de Lune, H. J. J. te Riele, D. T. Winter, <i>On the Zeros of the Riemann Zeta Function in the Critical Strip II</i>, Mathematics of Computation <b>39</b> (1982), 681-688.\n");
    buffer.append("<p>[3] J. van de Lune, H. J. J. te Riele, <i>On the Zeros of the Riemann Zeta Function in the Critical Strip III</i>, Mathematics of Computation <b>41</b> (1983), 759-767\n");
    buffer.append("<p>[4] J. van de Lune, H. J. J. te Riele, D. T. Winter,<i>On the Zeros of the Riemann Zeta Function in the Critical Strip IV</i>,Mathematics of Computation <b>46</b> (1986), 667-681.</body></html>\n");
    buffer.append("</body></html>");
  }

  private static void appendTitle(int idx, StringBuffer buffer) {
    switch (idx) {
      case 1: buffer.append("Number of Rosser blocks of given length"); break;
      case 2: buffer.append("<p>Number of Gram intervals containing excatly <i>m</i> zeros"); break;
      case 3: buffer.append("<p>Zero-patterns of Rosser blocks"); break;
    }
    buffer.append("<p>");
  }

  private static void appendTableHeader(int idx, int column, StringBuffer buffer) {
    if (idx < 3 && column == 0) buffer.append('n');
    else {
      if (idx == 1) {
        buffer.append("J(");
        buffer.append(column);
        buffer.append(", n)");
      } else if (idx == 2) {
        buffer.append("m = ");
        buffer.append(column-1);
      } else if (idx == 3) {
        if (column == 0) buffer.append("Zero-pattern");
        else if (column == 1) buffer.append("First occurrences at Gram point");
        else if (column == 2) buffer.append("Number of Rosser blocks");
      } else buffer.append(column);
    }
  }

  private static void appendTable(int idx, List table, StringBuffer buffer) {
    appendTitle(idx, buffer);
    int rows = table.size();
    int columns = ((List)table.get(rows-1)).size();
    buffer.append("<style type=text/css>\n.c { text-align: center }\n.r { text-align: right }\n</style>\n<p><center><table border=\"1\" cellspacing=\"0\" width=\"90%\">\n<colgroup>\n");
    for (int i = 0; i < columns; ++i) {
      buffer.append("<col width=\"");
      buffer.append(100 / columns);
      buffer.append("%\"/>");
    }
    buffer.append("</colgroup>\n<tr>");
    for (int i = 0; i < columns; ++i) {
      buffer.append(" <th><b>");
      appendTableHeader(idx, i, buffer);
      buffer.append("</b></th>");
    }
    buffer.append("</tr>\n");
    for (int i = 0; i < rows; ++i) {
      buffer.append("<tr>");
      List row = (List)table.get(i);
      int l = row.size();
      for (int col = 0; col < columns; ++col) {
        buffer.append("<td class=\"r\">");
        if (col < l) buffer.append(row.get(col));
        else buffer.append("&nbsp;");
        buffer.append("</td>");
      }
      buffer.append("</tr>\n");
    }
    buffer.append("</table></center>\n");
  }

  private static long getLastGramBlock() {
    long maxSize = 0;
    File file = new File(ConstantProperties.FINAL_DIR + "/1");
    File[] list = file.listFiles();
    if (list != null) {
      for (int i = 0; i < list.length; ++i) {
        String s = list[i].getName();
        int l = s.length();
        if (s.startsWith("zeta_zeros_0_") && s.endsWith(".tmp") && l > 13 && Character.isDigit(s.charAt(13))) {
          int idx = 13;
          while (++idx < l && Character.isDigit(s.charAt(idx)));
          long r = Long.parseLong(s.substring(13, idx));
          if (r > maxSize) maxSize = r;
        }
      }
    }
    return maxSize;
  }

  private static String getLastT() throws IOException {
    String s = readFile(new File(ConstantProperties.FINAL_DIR + "/1/zeta_zeros_0_" + start + ".tmp"));
    int i = s.indexOf('.');
    return s.substring(i+1, s.indexOf(';', i+1));
  }

  private static String readFile(File file) throws IOException {
    char[] cBuffer = new char[(int)file.length()];
    FileReader reader = new FileReader(file);
    int idx = 0;
    while (idx < cBuffer.length) {
      int n = reader.read(cBuffer, idx, cBuffer.length-idx);
      if (n <= 0) break;
      idx += n;
    }
    reader.close();
    return new String(cBuffer);
  }

  private static String start = null;
  private static String newLine = System.getProperty("line.separator");
}
