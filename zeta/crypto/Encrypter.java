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
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import zeta.util.Base64;
import zeta.util.StreamUtils;

/**
 *  Encrypts every communication between the client and the server using
 *  a key establishment protocol (half-certified Diffie-Hellman)
 *  with keys which have a length of 1024 Bit.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class Encrypter {
  /**
   *  @param key key for the encryption algorithm
  **/
  public Encrypter(Key key) {
    this.key = key;
  }

  /**
   *  Encrypts a specified file.
   *  Every transfer of a result uses a key establishment protocol (half-certified Diffie-Hellman)
   *  with keys which have a length of 1024 Bit. 
   *  @param randomize random number for the key establishment protocol
   *  @param inFilename name of the file which should be encrypted
   *  @param outFilename name of the encrypted file
   *  @exception  IOException  if an I/O error occurs.
  **/
  public void encrypt(int randomize, String inFilename, String outFilename) throws IOException {
    String inZip = inFilename + ".bz2";
    new File(inZip).delete();
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      bzip2CompressData(inFilename, inZip);
      in = new FileInputStream(inZip);
      out = new FileOutputStream(outFilename);
      encrypt(randomize, new File(inZip).length(), in, out);
    } finally {
      StreamUtils.close(in);
      StreamUtils.close(out);
      new File(inZip).delete();
    }
  }

  /**
   *  Encrypts a specified input stream.
   *  Every transfer of a result uses a key establishment protocol (half-certified Diffie-Hellman)
   *  with keys which have a length of 1024 Bit. 
   *  @param randomize random number for the key establishment protocol
   *  @param inLength inLength length of the input stream which should be encrypted
   *  @param in input stream which should be encrypted
   *  @param out output stream for the encrypted data
   *  @exception  IOException  if an I/O error occurs.
  **/
  public void encrypt(int randomize, long inLength, InputStream in, OutputStream out) throws IOException {
    SecureRandom random = new SecureRandom(key.getGenerator().modPow(BigInteger.valueOf(System.currentTimeMillis()*randomize), key.getModulo()).toByteArray());
    String s = String.valueOf(inLength) + ';';
    out.write(s.getBytes());
    BigInteger x;
    final BigInteger TWO = new BigInteger("2");
    do {
      byte[] buffer = new byte[KEY_LENGTH/8];
      random.nextBytes(buffer);
      buffer[0] &= 63;
      x = new BigInteger(buffer);
      x = x.mod(key.getModulo()).subtract(TWO);
    } while ((x.bitLength()+31)/32 < 3);
    IONumber.write(key.getGenerator().modPow(x, key.getModulo()), out, true);
    x = key.getBase().modPow(x, key.getModulo());

    byte[] buffer = new byte[4*((key.getModulo().bitLength()+31)/32-1)+1];
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
        System.arraycopy(buffer, 0, tmp, 0, pos); // ToDo: Performance
        Arrays.fill(tmp, pos, sz, (byte)0);
        pos = sz;
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
      BigInteger n = new BigInteger(buffer);
      IONumber.write(n.multiply(x).mod(key.getModulo()), out, true);
    }
  }

  /**
   *  Encrypts the file attribute of a URL. The default encryption algorithmus will be used.
   *  @param urlFile file attribute of a URL
   *  @return encrypted file attribute of a URL where the parameters are packed in a new parameter 'param'
   *  @exception  IOException  if an I/O error occurs.
  **/
  public String encryptURLFile(String urlFile) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
    ZipOutputStream zip = new ZipOutputStream(out);
    zip.setLevel(Deflater.BEST_COMPRESSION);
    zip.putNextEntry(new ZipEntry("param"));
    int idx = urlFile.indexOf('?');
    StreamUtils.writeData(new ByteArrayInputStream(urlFile.substring(idx+1).getBytes("UTF-8")), zip, true, true);
    byte[] data = out.toByteArray();
    ByteArrayOutputStream out2 = new ByteArrayOutputStream(2048);
    encrypt(new SecureRandom().nextInt(1024), data.length, new ByteArrayInputStream(data), out2);
    data = out2.toByteArray();
    StringBuffer buffer = new StringBuffer(2*data.length);
    if (idx >= 0) {
      buffer.append(urlFile.substring(0, idx+1));
    }
    buffer.append("param=");
    buffer.append(Base64.encode(data));
    return buffer.toString();
  }

  /**
   *  Compresses a specified file.
   *  @param inFilename name of the file which should be compressed
   *  @param outFilename name of the compressed file
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static void bzip2CompressData(String inFilename, String outFilename) throws IOException {
/*
Bzip2OutputStream:
E:\wedeniws\zeta\zeros>java -cp classes zeta.crypto.Encrypter
time=131939
time=392755
bzip2:
time=194931
org.apache.tools.bzip2.CBZip2OutputStream:
time=148763
time=429107
*/
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    Bzip2OutputStream bzout = null;
    //org.apache.tools.bzip2.CBZip2OutputStream bzout = null;
    try {
      in = new BufferedInputStream(new FileInputStream(inFilename), 4*1024);
      out = new BufferedOutputStream(new FileOutputStream(outFilename), 4*1024);
      out.write('B');
      out.write('Z');
      int ch = in.read();
      if (ch != -1) { // Bug if file size equal to 0
        bzout = new Bzip2OutputStream(out);
        //bzout = new org.apache.tools.bzip2.CBZip2OutputStream(out);
        do {
          bzout.write(ch);
          ch = in.read();
        } while (ch != -1);
        bzout.flush();
      }
    } finally {
      StreamUtils.close(in);
      StreamUtils.close(bzout);
      StreamUtils.close(out);
    }
  }

  /**
   *  Simple test program.
  **/
  public static void main(String[] args) {
    try {
      File[] list = new File(".").listFiles();
      if (list != null) {
        zeta.util.Properties properties = new zeta.util.Properties(zeta.util.Properties.ZETA_TOOLS_CFG);
        BigInteger key = new BigInteger(properties.get("key", ""), 32);
        for (int i = 0; i < list.length; ++i) {
          String s = list[i].getName();
          if (!s.equals("e.$$$") && !s.equals("d.$$$") && !list[i].isDirectory() && list[i].length() < 1024*1024) {
            System.out.print(s + ": ");
            System.out.flush();
            Encrypter e = new Encrypter(new DefaultKeyEncrypt());
            Decrypter d = new Decrypter(new DefaultKeyEncrypt());
            e.encrypt(111, s, "e.$$$");
            d.decrypt(key, "e.$$$", "d.$$$");
            FileInputStream fin1 = new FileInputStream(s);
            FileInputStream fin2 = new FileInputStream("d.$$$");
            if (StreamUtils.compare(fin1, fin2)) {
              System.out.println("ok");
            } else {
              System.out.println("NOT ok!");
              return;
            }
            fin1.close();
            fin2.close();
          }
          System.gc();
          System.out.println("Memory: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
        }
      }
    } catch (Exception e) {
      zeta.util.ThrowableHandler.handle(e);
    }
  }

  private Key key;
  private final int KEY_LENGTH = 1024;
}
