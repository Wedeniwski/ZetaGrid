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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import zeta.crypto.Encrypter;
import zeta.server.tool.ConstantProperties;
import zeta.util.ThrowableHandler;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ZetaCD {
  public final static int CD_SIZE_LARGE        = 734003200; // 700*1024*1024;
  public final static int CD_SIZE_SMALL        = 681574400; // 650*1024*1024;
  public final static int MAX_NUMBER_OF_FILES  = 1000;


  public static void main(String[] args) {
    create(CD_SIZE_LARGE);
  }

  public static int[] create(int cdSize) {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    int[] created = new int[2];
    created[0] = created[1] = 0;
    while (true) {
      int[] count = createCD(cdSize);
      if (count == null) {
        break;
      }
      created[0] += count[0];
      created[1] += count[1];
      if (count[0] == 0) {
        break;
      }
      System.out.println("Successful created at " + formatter.format(new Date()) + '.');
    }
    return created;
  }

  /**
   *  @return number of files
  **/
  private static int[] createCD(int cdSize) {
    File file = new File(ConstantProperties.ROOT_DIR);
    File[] list = file.listFiles();
    if (list != null) {
      File currentCD = null;
      int idx = 0;
      for (int i = 0; i < list.length; ++i) {
        if (list[i].isDirectory() && list[i].getName().startsWith("cd")) {
          int j = Integer.parseInt(list[i].getName().substring(2));
          if (j > idx) {
            idx = j;
            currentCD = list[i];
          }
        }
      }
      if (currentCD == null) {
        System.out.println("missing folder cd");
      } else {
        File calc = new File(ConstantProperties.FINAL_DIR + "/1");   // ToDo: not fix task_id
        list = calc.listFiles();
        if (list != null) {
          List files = new ArrayList(list.length);
          List order = new ArrayList(list.length);
          for (int i = 0; i < list.length; ++i) {
            String s = list[i].getName();
            int l = s.length();
            if (s.startsWith("zeta_zeros_") && l > 11 && Character.isDigit(s.charAt(11))) {
              Long value = null;
              if (s.endsWith(".zip")) {
                idx = 11;
                while (++idx < l && Character.isDigit(s.charAt(idx)));
                value = new Long(s.substring(11, idx));
              } else if (s.endsWith(".tmp")) {
                idx = s.length()-4;
                while (--idx > 0 && Character.isDigit(s.charAt(idx)));
                value = new Long(Long.parseLong(s.substring(idx+1, s.length()-4))-2000);
              }
              if (value != null) {
                l = order.size();
                int j = 0;
                while (j < l && value.compareTo(order.get(j)) > 0) ++j;
                order.add(j, value);
                files.add(j, list[i]);
              }
            }
          }
          File lastFile = null;
          File prevLastFile = null;
          File firstFile = null;
          File prevFile = null;
          Iterator i = files.iterator();
          while (i.hasNext()) {
            file = (File)i.next();
            if (file.getName().endsWith(".tmp")) {
              if (firstFile == null) {
                if (prevFile != null) {
                  String s = prevFile.getName();
                  int l = s.length();
                  if (s.endsWith(".zip") && s.startsWith("zeta_zeros_") && l > 11 && Character.isDigit(s.charAt(11))) {
                    idx = 11;
                    while (++idx < l && Character.isDigit(s.charAt(idx)));
                    long value = Long.parseLong(s.substring(11, idx));
                    int size = Integer.parseInt(s.substring(idx+1, l-4));
                    int sz = files.size();
                    for (int j = 0; j < sz; ++j) {
                      if (files.get(j) == file) {
                        long val = ((Long)order.get(j)).longValue();
                        if (value < val && value+size >= val) {
                          firstFile = prevFile;
                        }
                        break;
                      }
                    }
                  }
                }
                if (firstFile == null) {
                  firstFile = file;
                }
              }
              prevLastFile = lastFile;
              lastFile = file;
            }
            prevFile = file;
          }
          if (prevLastFile == null) {
            return null;
          }
          i = files.iterator();
          long dirSize = 0;
          int countFiles = 0;
          boolean first = true;
          while (i.hasNext()) {
            file = (File)i.next();
            if (first && file != firstFile) {
              File tmpDir = new File(ConstantProperties.ROOT_DIR + "/tmp");
              tmpDir.mkdir();
              file.renameTo(new File(ConstantProperties.ROOT_DIR + "/tmp/" + file.getName()));
            } else {
              first = false;
              dirSize += file.length();
              ++countFiles;
              if (file == prevLastFile || dirSize > cdSize) {
                break;
              }
            }
          }
          if (dirSize > cdSize) {
            currentCD = new File(ConstantProperties.ROOT_DIR + "/cd" + (Integer.parseInt(currentCD.getName().substring(2))+1));
            System.out.println("new CD " + currentCD.getName());
            if (currentCD.mkdir()) {
              // ToDo: create db overview
              i = files.iterator();
              countFiles = 0;
              dirSize = 0;
              first = true;
              while (i.hasNext()) {
                file = (File)i.next();
                if (first && file != firstFile) {
                  continue;
                }
                first = false;
                ++countFiles;
                dirSize += file.length();
                if (dirSize > cdSize) {
                  break;
                }
                if (!file.renameTo(new File(ConstantProperties.ROOT_DIR + '/' + currentCD.getName() + '/' + file.getName()))) {
                  return null;
                }
                System.out.println(file.getName());
                if (file == prevLastFile) {
                  break;
                }
              }
              file = new File("statistic.log");
              if (file.exists()) {
                try {
                  Encrypter.bzip2CompressData("statistic.log", "statistic.log.bz2");
                  file = new File("statistic.log.bz2");
                  if (file.renameTo(new File(ConstantProperties.ROOT_DIR + '/' + currentCD.getName() + '/' + file.getName()))) {
                    file = new File("statistic.log");
                    file.delete();
                  }
                } catch (IOException ioe) {
                  ThrowableHandler.handle(ioe);
                }
              }
              if (dirSize > cdSize) {
                return new int[] { countFiles, countFiles };
              }
            }
          } else {
            System.out.println("cd size=" + dirSize + ", count=" + countFiles);
            return new int[] { 0, countFiles };
          }
        }
      }
    }
    return null;
  }
}