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


package zeta.tool;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  Searches for small and large gaps between the zeros of the Riemann zeta function in the results of the computed work units.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class ZetaSearchGaps {
  public static void main(String[] args) {


    if (args.length == 0) {
      System.err.println("USAGE: <path name>\n"
                       + "       g <filename of summary>\n"
                       + "       s <filename of summary> [<beginning work unit id>]");
      return;
    }
    if (args.length == 2 && args[0].equals("g")) {
      sumMissingLogs(args[1]);
      return;
    }
    if (args.length == 2 && args[0].equals("s")) {
      search(args[1], 0);
      return;
    }
    if (args.length == 3 && args[0].equals("s")) {
      search(args[1], Long.parseLong(args[2]));
      return;
    }
    if (args.length == 1 && args[0].length() > 0 && args[0].charAt(args[0].length()-1) == ':') {
      try {
        while (true) {
          search(args);
          Toolkit.getDefaultToolkit().beep();
          File file = new File(args[0]);
          while (file.exists()) {
            Runtime.getRuntime().exec("eject " + args[0]).waitFor();
            Thread.sleep(250);
          }
          int count = 0;
          while (!file.exists()) {
            if (++count == 120) break;
            Thread.sleep(500);
          }
          count = 0;
          while (!file.exists()) {
            if (++count == 120) System.exit(1);
            Thread.sleep(3000);
          }
        }
      } catch (IOException ioe) {
        ThrowableHandler.handle(ioe);
      } catch (InterruptedException ie) {
        ThrowableHandler.handle(ie);
      }
    } else {
      search(args);
      Toolkit.getDefaultToolkit().beep();
      System.exit(1);
    }
  }

  private static void sumMissingLogs(String summaryFilename) {
    long size = 0;
    BufferedReader fin = null;
    try {
      fin = new BufferedReader(new FileReader(summaryFilename));
      while (true) {
        String line = fin.readLine();
        if (line == null) {
          break;
        }
        if (line.startsWith("checked without log: ")) {
          size += Integer.parseInt(line.substring(line.lastIndexOf('_')+1, line.length()-4));
        }
      }
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    } finally {
      StreamUtils.close(fin);
    }
    System.out.println("size of missing logs: " + size);
  }

  private static void search(String summaryFilename, long beginningWorkUnitId) {
    BufferedReader fin = null;
    try {
      boolean active = (beginningWorkUnitId == 0);
      fin = new BufferedReader(new FileReader(summaryFilename));
      while (true) {
        String line = fin.readLine();
        if (line == null) {
          break;
        }
        if (line.startsWith("just small value ")) {
          int idx1 = line.indexOf(':');
          int idx2 = line.indexOf(',', idx1+1);
          if (idx2 > idx1 && idx1 > 0) {
            if (line.charAt(idx2+1) != ' ') {
              line = line.substring(0, idx2) + '.' + line.substring(idx2+1);
              idx2 += 2;
              while (line.charAt(idx2) != ',') {
                ++idx2;
              }
            }
            int idx3 = line.indexOf('=', idx2);
            int idx4 = line.indexOf(',', idx3);
            if (idx4 > idx3 && idx3 > 0 && Math.abs(Double.parseDouble(line.substring(idx3+1, idx4))) < 0.6e-6) {
              long n = ZetaStatistic.getStartN(Double.parseDouble(line.substring(idx1+2, idx2)))-5;
              if (n >= beginningWorkUnitId) {
                active = true;
              }
              if (active) {
                ZetaStatistic.zetaZeros(n, 15, 0);
                String filename = "zeta_zeros_" + n + "_15.log";
                final String[] search = { "... Close pair of zeros between" };
                if (StreamUtils.search(search, new FileInputStream(filename), true)) {
                  FileWriter writer = new FileWriter("close_zeros.sql", true);
                  writer.write("close zeros found: " + filename + '\n');
                  writer.write("java zeta.tool.ZetaSynchronization s \"INSERT INTO zeta.found (task_id,work_unit_id,type,timestamp,found) VALUES (1,,'close zeros',CURRENT TIMESTAMP,)");
                  writer.close();
                } else {
                  new File(filename).delete();
                  new File("zeta_zeros_" + n + "_15.txt").delete();
                }
              }
            }
          }
        } else if (line.startsWith("Close pair of zeros between ")) {
          int idx1 = "Close pair of zeros between ".length();
          int idx2 = line.indexOf(' ', idx1+1);
          if (idx2 > idx1 && idx1 > 0) {
            long n = ZetaStatistic.getStartN(Double.parseDouble(line.substring(idx1, idx2)))-5;
            if (n >= beginningWorkUnitId) {
              active = true;
            }
            if (active) {
              ZetaStatistic.zetaZeros(n, 15, 0);
              String filename = "zeta_zeros_" + n + "_15.log";
              final String[] search = { "... Close pair of zeros between" };
              if (StreamUtils.search(search, new FileInputStream(filename), true)) {
                FileWriter writer = new FileWriter("close_zeros.sql", true);
                writer.write("close zeros found: " + filename + '\n');
                writer.write("java zeta.tool.ZetaSynchronization s \"INSERT INTO zeta.found (task_id,work_unit_id,type,timestamp,found) VALUES (1,,'close zeros',CURRENT TIMESTAMP,)");
                writer.close();
              } else {
                new File(filename).delete();
                new File("zeta_zeros_" + n + "_15.txt").delete();
              }
            }
          }
        }
      }
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    } finally {
      StreamUtils.close(fin);
    }
  }

  private static void search(String[] pathes) {
    new File("summary").mkdir();
    new File("summary/bz2").mkdir();
    new File("summary/log").mkdir();
    new File("summary/tmp").mkdir();
    StringBuffer buffer = new StringBuffer(10000000);
    byte[] byteBuffer = new byte[500000];
    for (int p = 0; p < pathes.length; ++p) {
      File file = new File(pathes[p]);
      File[] list = file.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; ++i) {
          final String name = list[i].getName();
          if (name.startsWith("zeta_zeros_") && name.endsWith(".zip")) {
            ZipInputStream zip = null;
            try {
              zip = new ZipInputStream(new FileInputStream(list[i]));
              boolean checkedLog = false;
              for (int k = 0; k < 2; ++k) {
                ZipEntry zEntry = zip.getNextEntry();
                buffer.delete(0, buffer.length());
                while (true) {
                  int n = zip.read(byteBuffer);
                  if (n <= 0) break;
                  buffer.append(new String(byteBuffer, 0, n));
                }
                if (zEntry.getName().endsWith(".txt")) {
                  String lastLine = "";
                  int lastZeros = 0;
                  int lastZeros2 = 0;
                  int simpleZeros = 0;
                  int simpleZeros2 = 0;
                  for (int idx = (buffer.charAt(0) != '.')? 0 : indexOf(buffer, '\n'); idx >= 0;) {
                    int startPos = indexOf(buffer, '.', idx+1);
                    if (startPos <= 0) break;
                    int endPos = indexOf(buffer, '\n', startPos);
                    if (endPos <= 0) break;
                    String t = buffer.substring(startPos+1, endPos);
                    /*if (t.equals(lastLine)) {
                      if (buffer.charAt(0) == '.') {
                        int j = indexOf(buffer, '\n');
                        if (buffer.charAt(j-1) == '\r') --j;
                        System.out.println("equal lines (" + name + "): " + buffer.substring(1, j) + t);
                      } else System.out.println("equal lines (" + name + "): " + t);
                    }*/
                    int numberOfZeros = 0;
                    int idx2 = idx;
                    while (++idx < startPos) {
                      if (buffer.charAt(idx) == '0' && buffer.charAt(++idx) == '0') numberOfZeros += 2;
                    }
                    if (numberOfZeros == 0) {
                      if (lastZeros+lastZeros2 >= 4) {
                        simpleZeros2 = simpleZeros;
                        simpleZeros = (buffer.charAt(idx2+1) == '0')? 1 : 0;
                      } else if (lastZeros+lastZeros2 == 0) {
                        simpleZeros2 = simpleZeros;
                        simpleZeros = (buffer.charAt(startPos-1) == '0')? 1 : 0;
                      }
                    } else simpleZeros2 = 0;
                    if (numberOfZeros+lastZeros+lastZeros2 > 4 || numberOfZeros+lastZeros+lastZeros2 == 4 && simpleZeros+simpleZeros2 > 1) {
                      if (buffer.charAt(0) == '.') {
                        int j = indexOf(buffer, '\n');
                        if (buffer.charAt(j-1) == '\r') --j;
                        System.out.println("large gap " + (numberOfZeros+lastZeros+lastZeros2+simpleZeros+simpleZeros2) + " (" + name + "): " + buffer.substring(1, j) + t);
                      } else System.out.println("large gap " + (numberOfZeros+lastZeros+lastZeros2+simpleZeros+simpleZeros2) + " (" + name + "): " + t);
                    }
                    lastLine = t; lastZeros2 = lastZeros; lastZeros = numberOfZeros;
                    idx = endPos;
                  }
                } else if (zEntry.getName().endsWith(".log")) {
                  final double SMALL_VALUE = 0.1e-5;
                  double lastValue = 0.0;
                  double lastN = 0.0;
                  boolean lastWasRealSmall = false;
                  int lastIdx = 0;
                  for (int idx = indexOf(buffer, "Call sumMZ at "); idx >= 0; idx = indexOf(buffer, "Call sumMZ at ", idx+1)) {
                    int endPos = indexOf(buffer, '\n', idx+14);
                    if (endPos <= 0) break;
                    if (buffer.charAt(endPos-1) == '\r') --endPos;
                    int startPos = endPos;
                    while (--startPos >= 0) {
                      char c = buffer.charAt(startPos);
                      if (c != '-' && c != 'e' && c != '.' && !Character.isDigit(c)) break;
                    }
                    checkedLog = true;
                    try {
                      double value = Double.parseDouble(buffer.substring(startPos+1, endPos));
                      if (Math.abs(value) < SMALL_VALUE) {
                        double n = Double.parseDouble(buffer.substring(lastIdx+14, indexOf(buffer, ',', lastIdx+14)));
                        if (Math.abs(n-lastN) < 1.0) {
                          if (lastValue*value < 0 && !lastWasRealSmall) {
                            System.out.println("real small value (" + name + "): " + buffer.substring(idx+14, endPos));
                            lastWasRealSmall = true;
                          }
                        } else {
                          System.out.println("just small value (" + name + "): " + buffer.substring(idx+14, endPos));
                          lastWasRealSmall = false;
                        }
                        lastN = n;
                      }
                      lastValue = value;
                    } catch (NumberFormatException nfe) {
                      System.out.println("NumberFormatException: name=" + name + ", value=" + buffer.substring(startPos+1, endPos));
                    }
                    lastIdx = idx; idx = endPos;
                  }
                  if (checkedLog && (buffer.length() == 0 || buffer.charAt(buffer.length()-1) != '@')) checkedLog = false;
                  for (int idx = indexOf(buffer, "Close pair of zeros "); idx >= 0; idx = indexOf(buffer, "Close pair of zeros ", idx+1)) {
                    int endPos = indexOf(buffer, '\n', idx+20);
                    if (endPos <= 0) break;
                    if (buffer.charAt(endPos-1) == '\r') --endPos;
                    System.out.println(buffer.substring(idx, endPos));
                  }
                  /*FileWriter writer = null;
                  try {
                    writer = new FileWriter("summary/log/" + zEntry.getName());
                    writer.write(buffer.toString());
                  } catch (IOException ioe) {
                    ThrowableHandler.handle(ioe);
                  } finally {
                    StreamUtils.close(writer);
                  }*/
                } else throw new IOException("Internal error: " + name);
              }
              if (checkedLog) System.out.println("checked complete: " + name);
              else System.out.println("checked without log: " + name);
            } catch (IOException ioe) {
              ThrowableHandler.handle(ioe);
            } finally {
              StreamUtils.close(zip);
            }
          } else if (name.endsWith(".bz2") || name.startsWith("zeta_zeros_") && name.endsWith(".tmp")) {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
              in = new FileInputStream(list[i]);
              out = new FileOutputStream(((name.endsWith(".bz2"))? "summary/bz2/" : "summary/tmp/") + name);
              StreamUtils.writeData(in, out, false, false);
            } catch (IOException ioe) {
              ThrowableHandler.handle(ioe);
            } finally {
              StreamUtils.close(in);
              StreamUtils.close(out);
            }
          }
        }
      }
    }
  }

  private static int indexOf(StringBuffer buffer, int ch) {
    return indexOf(buffer, ch, 0);
  }

  private static int indexOf(StringBuffer buffer, int ch, int fromIndex) {
    final int l = buffer.length();
    if (fromIndex < 0) fromIndex = 0;
    while (fromIndex < l) {
      if (buffer.charAt(fromIndex) == ch) return fromIndex;
      ++fromIndex;
    }
    return -1;
  }

  private static int indexOf(StringBuffer buffer, String str) {
    return indexOf(buffer, str, 0);
  }

  private static int indexOf(StringBuffer buffer, String str, int fromIndex) {
    final int l1 = buffer.length();
    final int l2 = str.length();
    if (l2 == 0) return fromIndex;
    char ch = str.charAt(0);
    if (fromIndex < 0) fromIndex = 0;
    while (fromIndex < l1) {
      if (buffer.charAt(fromIndex) == ch) {
        int i = 0;
        while (++i < l2 && str.charAt(i) == buffer.charAt(fromIndex+i));
        if (i == l2) return fromIndex;
      }
      ++fromIndex;
    }
    return -1;
  }
}
