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
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class ZetaCheckVersions {
  public static void main(String[] args) {
    FileWriter fout = null;
    try {
      String props = "client.cmd.version=" + getZetaClientVersion() + "\ncomputation.version=" + getFileVersion("src/zeros/zeta_zeros.h");
      fout = new FileWriter("zeta_versions.cfg");
      fout.write(props);
    } catch (IOException ioe) {
      ThrowableHandler.handle(ioe);
    } finally {
      StreamUtils.close(fout);
    }
  }

  private static String getZetaClientVersion() throws IOException {
    String version = null;
    ZipInputStream zip = null;
    try {
      zip = new ZipInputStream(new FileInputStream("kernel.zip"));
      while (true) {
        ZipEntry z = zip.getNextEntry();
        if (z == null) {
          break;
        }
        String name = z.getName();
        if (!z.isDirectory() && name.endsWith(".java")) {
          String v = getFileVersion(name);
          if (version == null || version.compareTo(v) < 0) {
            version = v;
          }
        }
      }
    } finally {
      StreamUtils.close(zip);
    }
    return version;
  }

  private static String getFileVersion(String filename) throws IOException {
    BufferedReader in = null;
    try {
      String version1 = " *  @version ";
      String version2 = "#define VERSION \"";
      in = new BufferedReader(new FileReader(filename));
      while (true) {
        String line = in.readLine();
        if (line == null) {
          break;
        }
        if (line.startsWith(version1)) {
          int idx = line.indexOf(',', version1.length());
          if (idx > version1.length()) {
            return line.substring(version1.length(), idx);
          }
        }
        if (line.startsWith(version2)) {
          int idx = line.indexOf('\"', version2.length());
          if (idx > version2.length()) {
            return line.substring(version2.length(), idx);
          }
        }
      }
    } finally {
      StreamUtils.close(in);
    }
    throw new IOException("no version available in file '" + filename + '\'');
  }
}
