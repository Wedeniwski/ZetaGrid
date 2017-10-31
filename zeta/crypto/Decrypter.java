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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import zeta.util.Base64;
import zeta.util.StreamUtils;

/**
 *  Decrypts every encrypted communication from the client using
 *  a key establishment protocol (half-certified Diffie-Hellman)
 *  with keys which have a length of 1024 Bit.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class Decrypter {
  /**
   *  @param key key for the decryption algorithm
  **/
  public Decrypter(Key key) {
    this.key = key;
  }

  /**
   *  Decrypts a specified input stream.
   *  Every transfer of a result uses a key establishment protocol (half-certified Diffie-Hellman)
   *  with keys which have a length of 1024 Bit. 
   *  @param privateKey decryption number for the half-certified Diffie-Hellman protocol
   *  @param inFilename name of the file which should be decrypted
   *  @param outFilename name of the decrypted file
   *  @exception  IOException  if an I/O error occurs.
  **/
  public void decrypt(BigInteger privateKey, String inFilename, String outFilename) throws IOException {
    String outUnZip = outFilename + ".bz2";
    File file = new File(outUnZip);
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      in = new FileInputStream(inFilename);
      out = new FileOutputStream(outFilename);
      if (!decrypt(in, out, privateKey)) {
        System.err.println("file " + outFilename + " not encrypted!");
        StreamUtils.close(out);
        out = null;
        StreamUtils.close(in);
        in = null;
        file.delete();
        new File(inFilename).renameTo(file);
        new File(outFilename).delete();
      } else {
        StreamUtils.close(out);
        out = null;
        file.delete();
        new File(outFilename).renameTo(file);
      }
      bzip2UncompressData(outUnZip, outFilename);
      if (!(new File(outFilename).exists())) {
        System.err.println("unzip error!");
        file.renameTo(new File(outFilename));
      }
    } finally {
      StreamUtils.close(in);
      StreamUtils.close(out);
      file.delete();
    }
  }

  /**
   *  Decrypts a specified input stream.
   *  Every transfer of a result uses a key establishment protocol (half-certified Diffie-Hellman)
   *  with keys which have a length of 1024 Bit. 
   *  @param in input stream which should be decrypted
   *  @param out output stream for the decrypted data
   *  @param D decryption number for the half-certified Diffie-Hellman protocol
   *  @exception  IOException  if an I/O error occurs.
  **/
  private boolean decrypt(InputStream in, OutputStream out, BigInteger D) throws IOException {
    final int sz = (key.getModulo().bitLength()+31)/32 - 1;
    final int l = 4*sz;
    int size = 0;
    while (true) {
      char c = (char)in.read();
      if (!Character.isDigit(c)) {
        if (c != ';') {
          return false;
        }
        break;
      }
      size = size*10 + Character.digit(c, 10);
    }
    BigInteger x = IONumber.read(in);
    if (x == null) {
      return false;
    }
    x = x.modPow(D, key.getModulo()).modInverse(key.getModulo());
    int j = 0;
    while (true) {
      BigInteger n = IONumber.read(in);
      if (n == null) {
        break;
      }
      n = n.multiply(x).mod(key.getModulo());
      byte[] result = n.toByteArray();
      if (result.length < l) {
        byte[] tmp = new byte[l];
        System.arraycopy(result, 0, tmp, l-result.length, result.length);   // ToDo: Performance
        Arrays.fill(tmp, 0, l-result.length, (byte)0);
        result = tmp;
      }
      j += l;
      for (int i = result.length-l; i < l; i += 4) {
        byte b = result[i];
        result[i] = result[i+3];
        result[i+3] = b;
        b = result[i+1];
        result[i+1] = result[i+2];
        result[i+2] = b;
      }
      if (j >= size) {
        j -= l; size -= j;
        if ((size&3) == 0) {
          out.write(result, result.length-size, size);
        } else {
          out.write(result, result.length-size-4+(size&3), size);
        }
      } else {
        out.write(result, result.length-l, l);
      }      
    }
    return true;
  }

  /**
   *  Decrypts the file attribute of a URL where the parameters are packed in a parameter 'param'.
   *  The default decryption algorithmus will be used.
   *  @param urlFile encrypted file attribute of a URL
   *  @param D decryption number for the half-certified Diffie-Hellman protocol
   *  @return decrypted file attribute of a URL, or <code>null</code> if the encrypted file attribute cannot be decrypted
   *  @exception  IOException  if an I/O error occurs.
  **/
  public String decryptURLFile(String urlFile, BigInteger D) throws IOException {
    int idx = urlFile.indexOf("param=");
    byte[] data = Base64.decode((idx >= 0)? urlFile.substring(idx+6) : urlFile);
    ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
    if (decrypt(new ByteArrayInputStream(data), out, D)) {
      ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
      ZipEntry entry = zip.getNextEntry();
      if (entry != null && "param".equals(entry.getName())) {
        out.reset();
        StreamUtils.writeData(zip, out, true, true);
        return out.toString("UTF-8");
      }
    }
    return null;
  }

  /**
   *  Uncompresses a specified file.
   *  @param inFilename name of the file which should be uncompressed
   *  @param outFilename name of the uncompressed file
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static void bzip2UncompressData(String inFilename, String outFilename) throws IOException {
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    //org.apache.tools.bzip2.CBZip2InputStream bzin = null;
    Bzip2InputStream bzin = null;
    try {
      in = new BufferedInputStream(new FileInputStream(inFilename), 4*1024);
      if (in.read() != 'B' || in.read() != 'Z') {
        return;
      }
      out = new BufferedOutputStream(new FileOutputStream(outFilename), 4*1024);
      if (new File(inFilename).length() > 2) { // Bug if file size equal to 0
        //bzin = new org.apache.tools.bzip2.CBZip2InputStream(in);
        bzin = new Bzip2InputStream(in);
        for (int ch = bzin.read(); ch != -1; ch = bzin.read()) {
          out.write(ch);
        }
        out.flush();
      }
    } finally {
      StreamUtils.close(bzin);
      StreamUtils.close(in);
      StreamUtils.close(out);
    }
  }

  private Key key;
}
