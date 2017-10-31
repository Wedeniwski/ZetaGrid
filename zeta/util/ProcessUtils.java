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

package zeta.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *  Provides process utilities.
 *
 *  @version 1.8.6, July 4, 2003
**/
public class ProcessUtils {
  /**
   *  Executes the specified string command in a separate process and
   *  causes the current thread to wait, if necessary, until the process represented by this Process object has terminated.
   *  @param command a specified system command
   *  @return the exit value of the process. By convention, 0 indicates normal termination and -1 indicates an exception
  **/
  public static int exec(String command) {
    return exec(command, null, null, false, 0);
  }

  /**
   *  Executes the specified string command in a separate process and
   *  causes the current thread to wait, if necessary, until the process represented by this Process object has terminated.
   *  @param command a specified system command
   *  @param out the standard output of the running process will be transferred to the specified writer
   *  @param autoFlush if <code>true</code>, the output buffer will be flushed whenever a byte array is written, one of the
   *                   println methods is invoked, or a newline character or byte ('\n') is written
   *  @return the exit value of the process. By convention, 0 indicates normal termination and -1 indicates an exception
  **/
  public static int exec(String command, OutputStream out, boolean autoFlush) {
    return exec(command, out, null, autoFlush, 0);
  }

  /**
   *  Executes the specified string command in a separate process and
   *  causes the current thread to wait, if necessary, until the process represented by this Process object has terminated.
   *  @param command a specified system command
   *  @param out the standard output of the running process will be transferred to the specified writer
   *  @param autoFlush if <code>true</code>, the output buffer will be flushed whenever a byte array is written, one of the
   *                   println methods is invoked, or a newline character or byte ('\n') is written
   *  @param timeout the process will be destroyed if the process runs longer than the specified time (in milliseconds) 
   *  @return the exit value of the process. By convention, 0 indicates normal termination and -1 indicates an exception
  **/
  public static int exec(String command, OutputStream out, boolean autoFlush, int timeout) {
    return exec(command, out, null, autoFlush, timeout);
  }

  /**
   *  Executes the specified string command in a separate process and
   *  causes the current thread to wait, if necessary, until the process represented by this Process object has terminated.
   *  @param command a specified system command
   *  @param out the standard output of the running process will be transferred to the specified writer
   *  @param error the error output of the running process will be transferred to the specified writer
   *  @param autoFlush if <code>true</code>, the output buffer will be flushed whenever a byte array is written, one of the
   *                   println methods is invoked, or a newline character or byte ('\n') is written
   *  @param timeout the process will be destroyed if the process runs longer than the specified time (in milliseconds) 
   *  @return the exit value of the process. By convention, 0 indicates normal termination and -1 indicates an exception
  **/
  public static int exec(String command, OutputStream out, final OutputStream error, final boolean autoFlush, final int timeout) {
    int result = -1;
    BufferedReader reader = null;
    try {
      final Process process = Runtime.getRuntime().exec(command);
      if (timeout > 0) {
        Thread destroyProcess = new Thread() {
          public void run() {
            try {
              sleep(timeout);
            } catch (InterruptedException ie) {
            } finally {
              process.destroy();
            }
          }
        };
        destroyProcess.start();
      }
      if (error != null) {
        Thread t = new Thread() {
          public void run() {
            BufferedReader errorReader = null;
            try {
              errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
              PrintStream pout = (error == null)? null : new PrintStream(error, autoFlush);
              while (true) {
                String line = errorReader.readLine();
                if (line == null) {
                  break;
                }
                if (pout != null) {
                  pout.println(line);
                }
              }
            } catch (IOException ioe) {
            } finally {
              StreamUtils.close(errorReader);
            }
          }
        };
        t.start();
      }
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      PrintStream pout = (out == null)? null : new PrintStream(out, autoFlush);
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        if (pout != null) {
          pout.println(line);
        }
      }
      result = process.waitFor();
    } catch (InterruptedException ie) {
    } catch (IOException ioe) {
    } finally {
      StreamUtils.close(reader);
    }
    return result;
  }
}
