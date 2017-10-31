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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import zeta.server.tool.ConstantProperties;
import zeta.util.StreamUtils;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ZetaSearch {
  public static void main(String[] args) {
    if (args.length == 1 && args[0].equals("s")) {
      split();
    } else if (args.length == 2) {
      if (args[0].equals("t")) {
        searchText(args[1]);
      } else {
        search(args[0], Long.parseLong(args[1]));
      }
    } else {
      System.err.println("USAGE: <Rosser block type> <begin>\n"
                       + "       s\n"
                       + "       t <text>");
    }
  }

  static void split() {
    File file = new File("summary/tmp");
    File[] list = file.listFiles();
    if (list != null) {
      long lastMax = 9999999999999L;
      long lastMin = 0;
      for (int count = 0;; ++count) {
        File dir = new File("summary/tmp/" + count);
        if (dir.exists()) {
          continue;
        }
        dir.mkdir();
        for (int i = 0; i < 1000; ++i) {
          int idx = -1;
          for (int j = 0; j < list.length; ++j) {
            String name = list[j].getName();
            if (name.startsWith("zeta_zeros_0_") && name.endsWith(".tmp")) {
              long size = Long.parseLong(name.substring(13, name.length()-4));
              if (size > lastMin && size < lastMax) {
                lastMax = size;
                idx = j;
              }
            }
          }
          if (idx == -1) {
            return;
          } else {
            list[idx].renameTo(new File("summary/tmp/" + count + '/' + list[idx].getName()));
            lastMin = lastMax;
            lastMax = 9999999999999L;
          }
        }
      }
    }
  }

  static void search(String type, long begin) {
    type = ' ' + type + ',';
    File file = new File("summary/tmp");
    File[] list = file.listFiles();
    if (list != null) {
      System.out.println("start");
      int size = 0;
      for (int i = 0; i < list.length; ++i) {
        String s = list[i].getName();
        if (s.startsWith("zeta_zeros_") && s.endsWith(".tmp") && s.startsWith("zeta_zeros_0_")) {
          ++size;
        }
      }
      long[] start = new long[size];
      int pos = 0;
      for (int i = 0; i < list.length; ++i) {
        String s = list[i].getName();
        if (s.startsWith("zeta_zeros_") && s.endsWith(".tmp") && s.startsWith("zeta_zeros_0_")) {
          start[pos++] = Long.parseLong(s.substring(13, s.length()-4));
        }
      }
      System.out.println("size=" + size);
      Arrays.sort(start);
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader("summary/tmp/zeta_zeros_0_" + start[size-1] + ".tmp"));
        long maxAnzahl = getAnzahl(reader, type);
        reader.close();
        int a = 0;
        while (a < size-1 && start[a] < begin) {
          ++a;
        }
        for (long anzahl = 0; anzahl < maxAnzahl; ++anzahl) {
          int b = size-1;
          while (a < b) {
          int c = (a+b)/2;
            reader = new BufferedReader(new FileReader("summary/tmp/zeta_zeros_0_" + start[c] + ".tmp"));
            long anz = getAnzahl(reader, type);
            if (b-a == 1) {
              System.out.println(String.valueOf(anzahl) + ". found at " + "summary/tmp/zeta_zeros_0_" + start[b] + ".tmp");
              break;
            }
            if (anz <= anzahl) {
              a = c;
            } else {
              b = c;
            }
          }
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      } finally {
        StreamUtils.close(reader);
      }
    }
  }

  private static long getAnzahl(BufferedReader reader, String type) throws IOException {
    long anz = 0;
    String s = reader.readLine();
    if (s != null) {
      int idx = s.indexOf(type, s.indexOf(' ')+1);
      if (idx >= 0) {
        idx = s.indexOf(type, idx+1) + type.length();
        int j = idx-1;
        int k = s.length();
        while (++j < k && Character.isDigit(s.charAt(j)));
        anz = Long.parseLong(s.substring(idx, j));
      }
    }
    return anz;
  }

  static void searchText(String text) {
    byte[] textBytes = text.getBytes();
    int[] next = initIndexOf(textBytes);
    File file = new File(ConstantProperties.FINAL_DIR + "/1");
    File[] list = file.listFiles();
    if (list != null) {
      byte[] buffer = new byte[50000];
      for (int i = 0; i < list.length; ++i) {
        final String name = list[i].getName();
        if (name.startsWith("zeta_zeros_") && name.endsWith(".zip")) {
          ZipInputStream zip = null;
          try {
            zip = new ZipInputStream(new FileInputStream(list[i]));
            for (int k = 0; k < 2; ++k) {
              ZipEntry zEntry = zip.getNextEntry();
              if (zEntry.getName().endsWith(".log")) {
                while (true) {
                  int n = zip.read(buffer);
                  if (n <= 0) break;
                  if (indexOf(buffer, 0, n, textBytes, next) >= 0) {
                    System.out.println("found at " + ConstantProperties.FINAL_DIR + "/1/" + name);
                    break;
                  }
                }
              }
            }
          } catch (IOException ioe) {
            ioe.printStackTrace();
          } finally {
            StreamUtils.close(zip);
          }
        }
      }
    }
  }

  private static int[] initIndexOf(byte[] string) {
    final int l = string.length-1;
    int[] next = new int[string.length];
    int j = -1;
    next[0] = -1;
    for (int i = 0; i < l; ) {
      if (j == -1 || string[i] == string[j]) next[++i] = ++j;
      else j = next[j];
    }
    return next;
  }

  /**
   * finds the first occurrence of string2 in string1
  **/
  private static int indexOf(byte[] string1, int pos, int n, byte[] string2, int[] next) {
    if (string2.length == 0) return 0;
    final int l = n-string2.length;
    int j = 0;
    for (int i = pos; i < l;) {
      if (j == -1 || string1[i] == string2[j]) {
        ++i;
        if (++j == string2.length) return i-string2.length;
      } else j = next[j];
    }
    /*for (int i = pos; i < l; ++i) {
      int j = 0;
      while (j < string2.length && string1[i+j] == string2[j]) ++j;
      if (j == string2.length) return i;
    }*/
    return -1;
  }
}
