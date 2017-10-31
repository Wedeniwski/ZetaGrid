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

package zeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 *  Transfers every line from an input stream to an output of another stream.
 *
 *  @version 1.8.5, April 17, 2003
**/
public class PipeStream extends Thread {
    
  /**
   *  @param  in  input stream
   *  @param  out output stream
  **/
  public PipeStream(InputStream in, OutputStream out) {
    reader = new BufferedReader(new InputStreamReader(in));
    this.out = out;
  }

  /**
   *  Closes the input and output stream and releases any system resources associated with the stream.
  **/
  public void close() {
    stopped = true;
    try {
      reader.close();
    } catch (IOException ioe) {}
    try {
      out.close();
    } catch (IOException ioe) {}
  }

  /**
   *  Starts the transfer.
  **/
  public void run() {
    try {
      while (!stopped) {
        String line = reader.readLine();
        if (line == null) break;
        line += newLine;
        out.write(line.getBytes());
      }
      reader.close();
      out.close();
    } catch (IOException ioe) {
      if (!stopped) ioe.printStackTrace();
    }
  }

  /**
   *  true, if the transfer should stopped.
  **/
  private boolean stopped = false;

  /**
   *  Buffered reader of the input stream.
  **/
  private BufferedReader reader;

  /**
   *  The output stream.
  **/
  private OutputStream out;

  /**
   *  Line separator.
  **/
  private static String newLine = System.getProperty("line.separator");
}