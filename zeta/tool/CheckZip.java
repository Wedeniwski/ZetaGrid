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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import zeta.util.StreamUtils;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class CheckZip {
  public static void main(String[] args) {
    if (args.length == 1) {
      check(0, args[0]);
    } else if (args.length == 2) {
      check(Long.parseLong(args[0]), args[1]);
    } else {
      System.err.println("USAGE: java CheckZip [<work unit id>] <path>");
    }
  }

  static void check(long workUnitId, String path) {
    File file = new File(path);
    File[] list = file.listFiles();
    if (list != null) {
      File tmpFile = null;
      for (int i = 0; i < list.length; ++i) {
        String s = list[i].getName();
        int l = s.length();
        if (s.startsWith("zeta_zeros_") && l > 11 && Character.isDigit(s.charAt(11)) && s.endsWith(".zip")) {
          int idx = 11;
          while (++idx < l && Character.isDigit(s.charAt(idx)));
          if (Long.parseLong(s.substring(11, idx)) > workUnitId && !checkFile(list[i])) {
            transferContent(list[i]);
          }
        }
      }
    }
  }

  private static boolean checkFile(File file) {
    ZipInputStream zip = null;
    try {
      String filename = file.getName();
      filename = filename.substring(0, filename.lastIndexOf('.'));
      zip = new ZipInputStream(new FileInputStream(file));
      for (int k = 0; k < 2; ++k) {
        ZipEntry zEntry = zip.getNextEntry();
        String entryName = zEntry.getName();
        if (!entryName.startsWith(filename)) {
          return false;
        }
      }
    } catch (IOException ioe) {
      System.err.println(file.getName() + ": ZIP Error!");
    } finally {
      StreamUtils.close(zip);
    }
    return true;
  }

  private static void transferContent(File file) {
    String filename = file.getName();
    System.out.println(filename);
    filename = filename.substring(0, filename.lastIndexOf('.'));
    ZipInputStream zipIn = null;
    ZipOutputStream zipOut = null;
    try {
      zipIn = new ZipInputStream(new FileInputStream(file));
      for (int k = 0; k < 2; ++k) {
        ZipEntry zEntry = zipIn.getNextEntry();
        String entryName = zEntry.getName();
        if (zipOut == null) {
          zipOut = new ZipOutputStream(new FileOutputStream(entryName.substring(0, entryName.lastIndexOf('.')) + ".zip"));
          zipOut.setLevel(Deflater.BEST_COMPRESSION);
        }
        zipOut.putNextEntry(new ZipEntry(entryName));
        StreamUtils.writeData(zipIn, zipOut, false, false);
        zipOut.flush();
      }
      StreamUtils.close(zipIn);
      zipIn = null;
      file.delete();
    } catch (IOException ioe) {
      System.err.println(file.getName() + ": ZIP Error!");
    } finally {
      StreamUtils.close(zipOut);
      StreamUtils.close(zipIn);
    }
  }
}
