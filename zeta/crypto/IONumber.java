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

package zeta.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 *  @version 1.8.5, April 17, 2003
**/
public class IONumber {
  public static void main(String[] args) {
    try {
      BigInteger b = new BigInteger(args[0]);
      java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
      write(b, out, true);
      out.close();
      BigInteger a = read(new java.io.ByteArrayInputStream(out.toByteArray()));
      System.out.println(new String(out.toByteArray()));
      if (b.equals(a)) {
        System.out.println("ok");
      } else {
        System.out.println("a=" + a);
        System.out.println("b=" + b);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static BigInteger read(InputStream in) throws IOException {
    int i = in.read();
    if (i == -1) {
      return null;
    }
    if ((char)i != '8' || (char)in.read() != '*' || (char)in.read() != '4' || (char)in.read() != '*') {
      throw new IOException("wrong format");
    }
    int size = 0;
    boolean binary = false;
    while (true) {
      char c = (char)in.read();
      if (!Character.isDigit(c)) {
        binary = (c == 'B');
        if (!binary && c != '(') {
          throw new IOException("wrong format");
        }
        break;
      }
      size = size*10 + Character.digit(c, 10);
    }
    boolean ok = false;
    byte[] buffer = null;
    if (binary) {
      buffer = new byte[4*size];
      int pos = 0;
      while (pos < buffer.length) {
        int n = in.read(buffer, pos, buffer.length-pos);
        if (n <= 0) {
          break;
        }
        pos += n;
      }
      ok = (pos == buffer.length && (char)in.read() == ')');
      inverse(buffer);
    } else {
      size = 4*size+1;
      buffer = new byte[size];
      buffer[0] = 0;
      while (size > 4) {
        size -= 4;
        int digit = 0;
        while (true) {
          char c = (char)in.read();
          if (!Character.isDigit(c)) {
            if (c != ',') {
              if (size == 1 && c == ')') {
                ok = true;
              } else {
                throw new IOException("wrong format");
              }
            }
            break;
          }
          digit = digit*10 + Character.digit(c, 10);
        }
        buffer[size] = (byte)((digit >> 24) & 255);
        buffer[size+1] = (byte)((digit >> 16) & 255);
        buffer[size+2] = (byte)((digit >> 8) & 255);
        buffer[size+3] = (byte)(digit & 255);
      }
    }
    return (ok)? new BigInteger(buffer) : null;
  }

  public static void write(BigInteger a, OutputStream out, boolean binary) throws IOException {
    StringBuffer body = new StringBuffer(a.bitLength()/3);
    body.append("8*4*");
    byte[] buffer = a.toByteArray();
    body.append((buffer.length+3)/4);
    if (binary) {
      body.append('B');
      out.write(body.toString().getBytes());
      int size = 4*((buffer.length+3)/4)-buffer.length;
      if (size > 0) {
        byte[] tmp = new byte[size+buffer.length];
        Arrays.fill(tmp, 0, size, (byte)0);
        System.arraycopy(buffer, 0, tmp, size, buffer.length);
        buffer = tmp;
      }
      inverse(buffer);
      out.write(buffer);
      body.delete(0, body.length());
    } else {
      body.append('(');
      int size = buffer.length;
      if (size == 1) {
        body.append((((long)buffer[size-1]) & 255));
      } else if (size == 2) {
        body.append((((long)buffer[size-1]) & 255) | ((((long)buffer[size-2]) & 255) << 8));
      } else if (size == 3) {
        body.append((((long)buffer[size-1]) & 255) | ((((long)buffer[size-2]) & 255) << 8) | ((((long)buffer[size-3]) & 255) << 16));
      } else if (size >= 4) {
        body.append(((long)buffer[size-1] & 255) | ((((long)buffer[size-2]) & 255) << 8) | ((((long)buffer[size-3]) & 255) << 16) | ((((long)buffer[size-4]) & 255) << 24));
        for (size -= 4; size >= 4; size -= 4) {
          body.append(',');
          body.append(((long)buffer[size-1] & 255) | ((((long)buffer[size-2]) & 255) << 8) | ((((long)buffer[size-3]) & 255) << 16) | ((((long)buffer[size-4]) & 255) << 24));
        }
        switch (size&3) {
          case 1: body.append(',');
                  body.append((((long)buffer[size-1]) & 255));
                  break;
          case 2: body.append(',');
                  body.append((((long)buffer[size-1]) & 255) | ((((long)buffer[size-2]) & 255) << 8));
                  break;
          case 3: body.append(',');
                  body.append((((long)buffer[size-1]) & 255) | ((((long)buffer[size-2]) & 255) << 8) | ((((long)buffer[size-3]) & 255) << 16));
                  break;
          default:
          case 0: break;
        }
      }
    }
    body.append(')');
    out.write(body.toString().getBytes());
  }

  private static void inverse(byte[] array) {
    for (int i = 0, j = array.length; i < --j; ++i) {
      byte b = array[i];
      array[i] = array[j];
      array[j] = b;
    }
  }
}
