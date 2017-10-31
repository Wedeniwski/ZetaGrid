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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *  Provides I/O stream utilities especially for the servlets.
 *
 *  @version 1.9.0, February 8, 2004
**/
public class StreamUtils {
  /**
   *  Closes an input stream and releases any system resources associated with this stream.
   *  No exception will be thrown if an I/O error occurs.
   *  @param in the input stream
  **/
  public static void close(InputStream in) {
    if (in != null) {
      try {
        in.close();
      } catch(IOException ioe) {
      }
    }
  }

  /**
   *  Closes an output stream and releases any system resources associated with this stream.
   *  No exception will be thrown if an I/O error occurs.
   *  @param out the output stream
  **/
  public static void close(OutputStream out) {
    if (out != null) {
      try {
        out.close();
      } catch(IOException ioe) {
      }
    }
  }

  /**
   *  Closes a character-input stream and releases any system resources associated with this stream.
   *  No exception will be thrown if an I/O error occurs.
   *  @param in a reader
  **/
  public static void close(Reader in) {
    if (in != null) {
      try {
        in.close();
      } catch(IOException ioe) {
      }
    }
  }

  /**
   *  Closes a character-output stream and releases any system resources associated with this stream.
   *  No exception will be thrown if an I/O error occurs.
   *  @param out a writer
  **/
  public static void close(Writer out) {
    if (out != null) {
      try {
        out.close();
      } catch(IOException ioe) {
      }
    }
  }

  /**
   *  Deletes the file or the whole directory denoted by this abstract pathname.
   *  @return <code>true</code> if and only if the file or directory is successfully deleted; false otherwise
  **/
  public static boolean delete(String pathname) {
    File file = new File(pathname);
    if (file.isDirectory()) {
      boolean result = true;
      File[] list = file.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; ++i) {
          result &= delete(list[i].getAbsolutePath());
        }
      }
      result &= file.delete();
      return result;
    } else {
      return file.delete();
    }
  }

  /**
   *  Move a file or a whole directory from a source path to a destination path.
   *  @param source source filename or directory
   *  @param source destination filename or directory
   *  @return <code>true</code> if and only if the file or directory is successfully moved; false otherwise
  **/
  public static boolean move(String source, String destination) {
    File file = new File(source);
    if (file.isDirectory()) {
      boolean result = new File(destination).mkdir();
      File[] list = file.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; ++i) {
          result &= list[i].renameTo(new File(destination + '/' + list[i].getName()));
        }
      }
      result &= new File(source).delete();
      return result;
    } else {
      return file.renameTo(new File(destination));
    }
  }

  /**
   *  Copy a file or a whole directory from a source path to a destination path.
   *  @param source source filename or directory
   *  @param source destination filename or directory
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static void copy(String source, String destination) throws IOException {
    File file = new File(source);
    if (file.isDirectory()) {
      new File(destination).mkdir();
      File[] list = file.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; ++i) {
          copy(list[i].getAbsolutePath(), destination + '/' + list[i].getName());
        }
      }
    } else {
      writeData(new FileInputStream(file), new FileOutputStream(destination), true, true);
    }
  }

  /**
   *  Transfers the data from a specified input stream to an output stream.
   *  @param in   input stream
   *  @param out  output stream
   *  @param closeIn close input stream after the transfer if <code>true</code>.
   *  @param closeOut close output stream after the transfer if <code>true</code>.
   *  @return size of output stream.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static int writeData(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
    int size = 0;
    try {
      byte[] buffer = new byte[64 * 1024];
      while (true) {
        int n = in.read(buffer);
        if (n <= 0) {
          break;
        }
        if (out != null) {
          out.write(buffer, 0, n);
        }
        size += n;
      }
    } finally {
      if (closeIn) {
        in.close();
      }
      if (closeOut && out != null) {
        out.close();
      }
    }
    return size;
  }

  /**
   *  Returns bytes of data from the specified file.
   *  @param filename name of the file
   *  @param compress  <code>true</code>, if the returned data should be compressed
   *  @param verbose   switch on/off the verbose mode
   *  @return bytes of data from the specified file.
  **/
  public static byte[] getFile(String filename, boolean compress, boolean verbose) throws IOException {
    File file = new File(filename);
    if (verbose) {
      System.out.println("size=" + file.length());
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream((int)file.length());
    if (compress) {
      ZipOutputStream zip = new ZipOutputStream(out);
      zip.setLevel(Deflater.BEST_COMPRESSION);
      zip.putNextEntry(new ZipEntry(filename));
      StreamUtils.writeData(new FileInputStream(file), zip, true, true);
    } else {
      StreamUtils.writeData(new FileInputStream(file), out, true, false);
    }
    byte[] buffer = out.toByteArray();
    out.close();
    if (verbose) {
      System.out.println("compressed=" + buffer.length);
    }
    return buffer;
  }

  /**
   *  Compares the content of two streams.
   *  @param in1 first input stream
   *  @param in2 second input stream
   *  @return <code>true</code> if the content of the first stream is equal to the content of the second stream.
  **/
  public static boolean compare(InputStream in1, InputStream in2) throws IOException {
    BufferedInputStream bin1 = new BufferedInputStream(in1);
    BufferedInputStream bin2 = new BufferedInputStream(in2);
    while (true) {
      int i1 = bin1.read();
      int i2 = bin2.read();
      if (i1 != i2) {
        return false;
      }
      if (i1 == -1) {
        return true;
      }
    }
  }

  /**
   *  Copy temporary the specified input stream to the specified destination file to check the available disk space.
   *  @param in input stream
   *  @param in2 second input stream
   *  @return <code>true</code> if the temporary file can be successfully created.
  **/
  public static boolean checkAvailDiskSpace(InputStream in, File dest) throws IOException {
    try {
      return (StreamUtils.writeData(in, new FileOutputStream(dest), false, true) == dest.length());
    } finally {
      dest.delete();
    }
  }

  /**
   *  Returns a buffer which is aligned to the next 8 byte border.
   *  @param buffer buffer to align
   *  @return buffer which is aligned to the next 8 byte border, maybe the same buffer.
  **/
  public static byte[] align8(byte[] buffer) {
    int n = buffer.length & 7;
    if (n != 0) {
      byte[] newBuffer = new byte[(buffer.length & (~7)) + 8];
      System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
      for (int i = buffer.length; i < newBuffer.length; ++i) {
        newBuffer[i] = 0x20;
      }
      buffer = newBuffer;
    }
    return buffer;
  }

  /**
   *  Returns <code>true</code> if and only if the stream contains the specified string <code>search</code>.
   *  Sets the position of the stream where the stream contains the specified string <code>search</code>.
   *  End of the stream will be set if the stream does not contain the specified string <code>search</code>.
   *  The input stream must supports the <code>mark</code> and <code>reset</code> methods.
   *  @param in   input stream
   *  @param search string which is searched
   *  @param ignoreCase if <code>true</code>, ignore case when comparing characters.
   *  @return <code>true</code> if and only if the stream contains the specified string <code>search</code>.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static boolean skip(InputStream in, String search, boolean ignoreCase) throws IOException {
    if (!in.markSupported()) {
      throw new IllegalArgumentException("The input stream must supports the mark and reset methods.");
    }
    if (search != null && search.length() > 0) {
      char[] s = search.toCharArray();
      while (true) {
        in.mark(s.length);
        int c = in.read();
        if (c == -1) {
          break;
        }
        char ch = (char)c;
        if (!ignoreCase && ch == s[0] || ignoreCase && Character.toLowerCase(ch) == Character.toLowerCase(s[0]) && Character.toUpperCase(ch) == Character.toUpperCase(s[0])) {
          int j = 0;
          while (++j < s.length) {
            c = in.read();
            ch = (char)c;
            if (ignoreCase) {
              if (Character.toLowerCase(ch) != Character.toLowerCase(s[j]) || Character.toUpperCase(ch) != Character.toUpperCase(s[j])) {
                break;
              }
            } else if (ch != s[j]) {
              break;
            }
          }
          in.reset();
          if (j == s.length) {
          return true;
          }
          in.read();
      }
      }
      return false;
    }
    return true;
  }

  /**
   *  Returns the string of the stream which is between the specified strings <code>left</code> and <code>right</code>.
   *  The resulting string does not include the specified strings <code>left</code> and <code>right</code>.
   *  The position of the stream is after the specified ending string <code>right</code>.
   *  End of the stream will be set if the stream does not contain one of the specified strings.
   *  @param in   input stream
   *  @param search starting string which is searched
   *  @param search ending string which is searched
   *  @param ignoreCase if <code>true</code>, ignore case when comparing characters.
   *  @return the string of the stream which is between the specified strings <code>left</code> and <code>right</code>
   *          or <code>null</code> if the specified strings are not defined in the stream.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static String between(InputStream in, String left, String right, boolean ignoreCase) throws IOException {
    if (skip(in, left, ignoreCase)) {
      int l = left.length();
      for (int i = 0; i < l; ++i) {
        in.read();
      }
      char[] search = right.toCharArray();
      if (search.length == 0) {
        return "";
      }
      if (ignoreCase) {
        for (int i = 0; i < search.length; ++i) {
          search[i] = Character.toLowerCase(search[i]);
        }
      }
      char last = search[search.length-1];
      StringBuffer buffer = new StringBuffer(1024);
      l = 0;
      while (true) {
        int c = in.read();
        if (c == -1) {
          break;
        }
        char ch = (char)c;
        buffer.append(ch);
        if (++l >= search.length && (!ignoreCase && ch == last || ignoreCase && Character.toLowerCase(ch) == last)) {
          int idx = l-search.length;
          if (ignoreCase) {
            for (int i = 0; idx < l && Character.toLowerCase(buffer.charAt(idx)) == search[i] && Character.toUpperCase(buffer.charAt(idx)) == Character.toUpperCase(search[i]); ++i, ++idx);
          } else {
            for (int i = 0; idx < l && buffer.charAt(idx) == search[i]; ++i, ++idx);
          }
          if (idx == l) {
            return buffer.substring(0, l-search.length);
          }
        }
      }
    }
    return null;
  }

  /**
   *  Returns the specified number of lines from the input stream where the last line contains the specified string <code>search</code>.
   *  The last returned lines maybe <code>null</code> if the input stream contains less lines previous the specified string <code>search</code>.
   *  The position of the stream is after the last returned line.
   *  @param in   input stream
   *  @param numberOfLines number of lines which should be returned
   *  @param search starting string which is searched
   *  @param ignoreCase if <code>true</code>, ignore case when comparing characters.
   *  @return the specified number of strings where the last line contains the specified string <code>search</code>
   *          or <code>null</code> if the specified string is not defined in the stream.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static String[] getLines(InputStream in, int numberOfLines, String search, boolean ignoreCase) throws IOException {
    String[] lines = new String[numberOfLines];
    if (search == null || search.length() == 0 || numberOfLines == 0) {
      return lines;
    }
    StringBuffer buffer = new StringBuffer(1024);
    while (true) {
      int c = in.read();
      if (c == -1) {
        break;
      }
      char ch = (char)c;
      if (ch == '\n') {
        int i = numberOfLines-1;
        while (i >= 0 && lines[i] == null) {
          --i;
        }
        if (++i == numberOfLines) {
          for (i = 0; i+1 < numberOfLines; ++i) {
            lines[i] = lines[i+1];
          }
        }
        lines[i] = buffer.toString();
        buffer.delete(0, buffer.length());
        if (!ignoreCase && lines[i].indexOf(search) >= 0 || ignoreCase && StringUtils.indexOfIgnoreCase(lines[i], search, 0) >= 0) {
          return lines;
        }
      } else {
        buffer.append(ch);
      }
    }
    return null;
  }

  /**
   *  Returns lines from the input stream where all lines contains the specified string <code>search</code>.
   *  All these lines must be continuously inside the stream.
   *  @param in   input stream
   *  @param search starting string which is searched
   *  @param ignoreCase if <code>true</code>, ignore case when comparing characters.
   *  @return lines from the input stream where all lines contains the specified string <code>search</code>
   *          or <code>null</code> if the specified string is not defined in the stream.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static String[] getLines(InputStream in, String search, boolean ignoreCase) throws IOException {
    if (in != null) {
      if (search == null || search.length() == 0) {
        return new String[0];
      }
      List lines = new ArrayList(50);
      StringBuffer buffer = new StringBuffer(1024);
      boolean searchIsActive = false;
      while (true) {
        int c = in.read();
        if (c == -1) {
          break;
        }
        char ch = (char)c;
        if (ch == '\n') {
          String line = buffer.toString();
          buffer.delete(0, buffer.length());
          if (!ignoreCase && line.indexOf(search) >= 0 || ignoreCase && StringUtils.indexOfIgnoreCase(line, search, 0) >= 0) {
            lines.add(line);
            searchIsActive = true;
          } else if (searchIsActive) {
            String[] l = new String[lines.size()];
            for (int i = 0; i < l.length; ++i) {
              l[i] = (String)lines.get(i);
            }
            return l;
          }
        } else {
          buffer.append(ch);
        }
      }
    }
    return null;
  }

  /**
   *  Returns the specified number of lines from the input stream.
   *  The last returned lines maybe <code>null</code> if the input stream contains less lines.
   *  The position of the stream is after the last returned line.
   *  @param in   input stream
   *  @param numberOfLines number of lines which should be returned
   *  @return the specified number of lines from the input stream.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static String[] getLines(InputStream in, int numberOfLines) throws IOException {
    String[] lines = new String[numberOfLines];
    if (numberOfLines == 0) {
      return lines;
    }
    StringBuffer buffer = new StringBuffer(1024);
    while (true) {
      int c = in.read();
      if (c == -1) {
        break;
      }
      char ch = (char)c;
      if (ch == '\n') {
        int i = numberOfLines-1;
        while (i >= 0 && lines[i] == null) {
          --i;
        }
        lines[++i] = buffer.toString();
        buffer.delete(0, buffer.length());
        if (i == numberOfLines-1) {
          return lines;
        }
      } else {
        buffer.append(ch);
      }
    }
    return lines;
  }

  /**
   *  Searches for specified text in an input stream.
   *  @param text array of strings which are searched
   *  @param in   input stream
   *  @param closeIn close input stream after the search
   *  @return <code>true</code> if the specified text is in the input stream.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static boolean search(String[] text, InputStream in, boolean closeIn) throws IOException {
    try {
      if (text != null && text.length > 0) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(50000);
        writeData(in, out, closeIn, true);
        byte[] buffer = out.toByteArray();
        for (int idx = 0; idx < text.length; ++idx) {
          byte[] textBytes = text[idx].getBytes();
          if (indexOf(buffer, 0, buffer.length, textBytes, initIndexOf(textBytes)) == -1) {
            return false;
          }
        }
      }
      return true;
    } finally {
      if (closeIn) {
        StreamUtils.close(in);
      }
    }
  }

  /**
   *  Searches for specified text in a reader.
   *  @param text array of strings which are searched
   *  @param in   reader
   *  @param closeIn close input stream after the search if <code>true</code>.
   *  @return the lines which start with the specified text, <code>null</code> if the specified text is not in the reader.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public static String[] startsWith(String[] text, Reader in, boolean closeIn) throws IOException {
    String[] found = null;
    try {
      if (text != null && text.length > 0) {
        List lines = new ArrayList(text.length);
        BufferedReader buffer = new BufferedReader(in);
        while (true) {
          String line = buffer.readLine();
          if (line == null) {
            break;
          }
          for (int i = 0; i < text.length; ++i) {
            if (line.startsWith(text[i])) {
              lines.add(line);
              break;
            }
          }
        }
        final int l = lines.size();
        if (l > 0) {
          found = new String[l];
          for (int i = 0; i < l; ++i) {
            found[i] = (String)lines.get(i);
          }
        }
      }
    } finally {
      if (closeIn) {
        StreamUtils.close(in);
      }
    }
    return found;
  }

  /**
   * Internal function for search.
  **/
  private static int[] initIndexOf(byte[] string) {
    final int l = string.length-1;
    int[] next = new int[string.length];
    int j = -1;
    next[0] = -1;
    for (int i = 0; i < l; ) {
      if (j == -1 || string[i] == string[j]) next[++i] = ++j;
      else j = next[j];
    }
    return next;
  }

  /**
   * Internal function for search. Finds the first occurrence of string2 in string1.
  **/
  private static int indexOf(byte[] string1, int pos, int n, byte[] string2, int[] next) {
    if (string2.length == 0) return 0;
    final int l = n-string2.length;
    int j = 0;
    for (int i = pos; i < l;) {
      if (j == -1 || string1[i] == string2[j]) {
        ++i;
        if (++j == string2.length) return i-string2.length;
      } else j = next[j];
    }
    return -1;
  }
}
