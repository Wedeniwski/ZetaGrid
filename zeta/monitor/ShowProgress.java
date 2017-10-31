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

package zeta.monitor;

import java.io.FileReader;
import java.io.IOException;

/**
 *  @version 1.9.3, May 29, 2004
**/
public class ShowProgress {
  public static void main(String[] args) {
    Thread t = new Thread() {
      public void run() {
        boolean first = true;
        while (true) {
          if (!first) {
            System.out.print("\b\b\b\b\b");
          }
          FileReader reader = null;
          try {
            int i = 0;
            double[] progress = new double[3];
            reader = new FileReader("zeta_zeros.tmp");
            while (true) {
              int c = reader.read();
              if (c == -1) {
                break;
              } else if (Character.isDigit((char)c)) {
                progress[i] = 10*progress[i] + Character.digit((char)c, 10);
              } else if (++i >= 3) {
                break;
              }
            }
            if (progress[0] > progress[1] && progress[2] > 0) {
              int p = (int)(((progress[0]-progress[1])/progress[2])*1000);
              if (p < 100) {
                System.out.print(' ');
              }
              if (p < 10) {
                System.out.print('0');
              }
              System.out.print(p/10);
              System.out.print('.');
              System.out.print(p%10);
              System.out.print('%');
              System.out.flush();
              first = false;
            }
          } catch (IOException ioe) {
          } finally {
            if (reader != null) {
              try {
                reader.close();
              } catch (IOException ioe) {
              }
            }
          }
          try {
            synchronized (this) {
              wait(10000);
            }
          } catch (InterruptedException ie) {
          }
        }
      }
    };
    t.start();
  }
}
