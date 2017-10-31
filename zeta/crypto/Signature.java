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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 *  @version 1.9.0, February 8, 2004
**/
import zeta.util.StreamUtils;

public class Signature {
  /**
   *  @param key key for the signature
  **/
  public Signature(Key key) {
    this.key = key;
  }

  /**
   *  Verifies the digital signature of a specified file.
   *  @param signature  the digital signature that can be read by <code>IONumber.read</code>
   *  @param file       data of the file
  **/
  public boolean verify(String signature, byte[] file) throws IOException {
    InputStream in = null;
    InputStream inSignature = null;
    try {
      in = new ByteArrayInputStream(file);
      inSignature = new ByteArrayInputStream(signature.getBytes("UTF-8"));
      BigInteger r = IONumber.read(inSignature);
      BigInteger s = IONumber.read(inSignature);
      if (r == null || s == null || r.compareTo(BigInteger.ZERO) == 0 || r.compareTo(key.getModulo()) >= 0) {
        return false;
      }
      return (key != null && key.getBase().modPow(r, key.getModulo()).multiply(r.modPow(s, key.getModulo())).mod(key.getModulo()).compareTo(key.getGenerator().modPow(hashCode(in, key.getModulo()), key.getModulo())) == 0);
    } finally {
      StreamUtils.close(inSignature);
      StreamUtils.close(in);
    }
  }

  public void generate(int randomize, BigInteger privateKey, InputStream in, OutputStream out) throws IOException {
    SecureRandom random = new SecureRandom(privateKey.modPow(BigInteger.valueOf(System.currentTimeMillis()*randomize), key.getModulo()).toByteArray());
    byte[] buffer = new byte[randomize/8];
    random.nextBytes(buffer);
    BigInteger q = key.getModulo().subtract(BigInteger.ONE);
    buffer = new byte[128];
    BigInteger k;
    do {
      random.nextBytes(buffer);
      buffer[0] &= 63;
      k = new BigInteger(buffer);
    } while (k.gcd(q).compareTo(BigInteger.ONE) > 0);
    BigInteger r = key.getGenerator().modPow(k, key.getModulo());
    IONumber.write(r, out, false);
    BigInteger h = hashCode(in, key.getModulo());
    r = r.multiply(privateKey).mod(q);
    if (h.compareTo(r) < 0) {
      h = h.add(q);
    }
    h = h.subtract(r).multiply(k.modInverse(q)).mod(q);
    IONumber.write(h, out, false);
  }

  private BigInteger hashCode(InputStream in, BigInteger p) throws IOException {
    BigInteger hash = BigInteger.ZERO;
    byte[] buffer = new byte[4*((p.bitLength()+31)/32-1)+1];
    buffer[0] = 0;
    boolean eof = false;
    while (!eof) {
      int pos = 1;
      while (pos < buffer.length) {
        int n = in.read(buffer, pos, buffer.length-pos);
        if (n <= 0) {
          eof = (n == -1);
          break;
        }
        pos += n;
      }
      if (pos == 1) {
        break;
      }
      if (pos < buffer.length) {
        int sz = 4*((pos+2)/4)+1;
        byte[] tmp = new byte[sz];
        System.arraycopy(buffer, 0, tmp, 0, pos);
        while (pos < sz) {
          tmp[pos++] = 0;
        }
        buffer = tmp;
      }
      for (int i = 1; i < pos; i += 4) {
        byte b = buffer[i];
        buffer[i] = buffer[i+3];
        buffer[i+3] = b;
        b = buffer[i+1];
        buffer[i+1] = buffer[i+2];
        buffer[i+2] = b;
      }
      hash = hash.add(new BigInteger(buffer));
      if (hash.compareTo(p) >= 0) {
        hash = hash.subtract(p);
      }
    }
    return hash;
  }

  private Key key = null;
}
