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

package zeta.monitor;

import java.io.IOException;
import java.io.OutputStream;

import zeta.ZetaInfo;

/**
 *  Monitor the output stream.
 *
 *  @version 1.8.5, April 17, 2003
**/
public class MonitoredOutputStream extends OutputStream {
  MonitoredOutputStream(OutputStream out) {
    this.out = out;
  }

  /**
   * Writes the specified byte to this output stream. The general 
   * contract for <code>write</code> is that one byte is written 
   * to the output stream. The byte to be written is the eight 
   * low-order bits of the argument <code>b</code>. The 24 
   * high-order bits of <code>b</code> are ignored.
   * <p>
   * Subclasses of <code>OutputStream</code> must provide an 
   * implementation for this method. 
   *
   * @param      b   the <code>byte</code>.
   * @exception  IOException  if an I/O error occurs. In particular, 
   *             an <code>IOException</code> may be thrown if the 
   *             output stream has been closed.
   */
  public void write(int b) throws IOException {
    out.write(b);
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this output stream. 
   * The general contract for <code>write(b, off, len)</code> is that 
   * some of the bytes in the array <code>b</code> are written to the 
   * output stream in order; element <code>b[off]</code> is the first 
   * byte written and <code>b[off+len-1]</code> is the last byte written 
   * by this operation.
   * <p>
   * The <code>write</code> method of <code>OutputStream</code> calls 
   * the write method of one argument on each of the bytes to be 
   * written out. Subclasses are encouraged to override this method and 
   * provide a more efficient implementation. 
   * <p>
   * If <code>b</code> is <code>null</code>, a 
   * <code>NullPointerException</code> is thrown.
   * <p>
   * If <code>off</code> is negative, or <code>len</code> is negative, or 
   * <code>off+len</code> is greater than the length of the array 
   * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs. In particular, 
   *             an <code>IOException</code> is thrown if the output 
   *             stream is closed.
   */
  public void write(byte b[], int off, int len) throws IOException {
    int pos = 0;
    int size = 16*1024;
    while (pos+size < len) {
      out.write(b, off+pos, size);
      out.flush();
      pos += size;
      int percentage = (pos*100)/len;
      if (percentage > 100) {
        percentage = 100;
      }
      ZetaInfo.write(String.valueOf(percentage) + "% of the work unit " + workUnitId + " is transferred");
    }
    out.write(b, off+pos, len-pos);
    out.flush();
    //out.write(b, off, len);
  }

  /**
   * Flushes this output stream and forces any buffered output bytes 
   * to be written out. The general contract of <code>flush</code> is 
   * that calling it is an indication that, if any bytes previously 
   * written have been buffered by the implementation of the output 
   * stream, such bytes should immediately be written to their 
   * intended destination.
   * <p>
   * The <code>flush</code> method of <code>OutputStream</code> does nothing.
   *
   * @exception  IOException  if an I/O error occurs.
   */
  public void flush() throws IOException {
    out.flush();
  }

  /**
   * Closes this output stream and releases any system resources 
   * associated with this stream. The general contract of <code>close</code> 
   * is that it closes the output stream. A closed stream cannot perform 
   * output operations and cannot be reopened.
   * <p>
   * The <code>close</code> method of <code>OutputStream</code> does nothing.
   *
   * @exception  IOException  if an I/O error occurs.
   */
  public void close() throws IOException {
    out.close();
  }

  public static void setWorkUnitId(long workUnitId) {
    MonitoredOutputStream.workUnitId = workUnitId;
  }

  private OutputStream out;
  private static long workUnitId;
}
