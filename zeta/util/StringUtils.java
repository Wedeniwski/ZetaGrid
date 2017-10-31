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

/**
 *  Provides string utilities.
 *
 *  @version 1.9.1, February 17, 2004
**/
public class StringUtils {

  /**
   *  Returns a formatted string.
   *  @param left <code>true</code> means left alignment otherwise rigth alignment
   *  @param fill filling character if the specified text is smaller than the resulting size
   *  @param size size of the formatted string
   *  @param text string which should be formated
   *  @return formatted string
  **/
  public static String format(boolean left, char fill, int size, String text) {
    if (text == null) {
      text = "";
    }
    final int l = text.length();
    if (l >= size) {
      return (left)? text.substring(0, size) : text.substring(l-size);
    } else {
      StringBuffer buffer = new StringBuffer(size);
      format(left, fill, size, text, buffer);
      return buffer.toString();
    }
  }

  /**
   *  Formats a specified string and appends the formatted string to the buffer.
   *  @param left <code>true</code> means left alignment otherwise rigth alignment
   *  @param fill filling character if the specified text is smaller than the resulting size
   *  @param size size of the formatted string
   *  @param text string which should be formated
   *  @param buffer buffer where the formatted string will be appended
  **/
  public static void format(boolean left, char fill, int size, String text, StringBuffer buffer) {
    if (text == null) {
      text = "";
    }
    final int l = text.length();
    if (l >= size) {
      buffer.append((left)? text.substring(0, size) : text.substring(l-size));
    } else {
      if (left) {
        buffer.append(text);
      }
      size -= l;
      if (fill == ' ') {
        while (size > 31) {
          buffer.append("                                ");
          size -= 32;
        }
        if (size > 15) {
          buffer.append("                ");
          size -= 16;
        }
        if (size > 7) {
          buffer.append("        ");
          size -= 8;
        }
        switch (size) {
          case 7: buffer.append("       "); break;
          case 6: buffer.append("      "); break;
          case 5: buffer.append("     "); break;
          case 4: buffer.append("    "); break;
          case 3: buffer.append("   "); break;
          case 2: buffer.append("  "); break;
          case 1: buffer.append(' ');
        }
      } else if (fill == '0') {
        while (size > 31) {
          buffer.append("00000000000000000000000000000000");
          size -= 32;
        }
        if (size > 15) {
          buffer.append("0000000000000000");
          size -= 16;
        }
        if (size >  7) {
          buffer.append("00000000");
          size -= 8;
        }
        switch (size) {
          case 7: buffer.append("0000000"); break;
          case 6: buffer.append("000000"); break;
          case 5: buffer.append("00000"); break;
          case 4: buffer.append("0000"); break;
          case 3: buffer.append("000"); break;
          case 2: buffer.append("00"); break;
          case 1: buffer.append('0');
        }
      } else {
        while (size > 0) {
          buffer.append(fill);
          --size;
        }
      }
      if (!left) {
        buffer.append(text);
      }
    }
  }

  /**
   *  Replaces indicated characters with other characters.
   *  @param text string where characters should be replaced
   *  @param oldCharacters the character to be replaced by <code>newCharacters</code>.
   *  @param newCharacters the character replacing <code>oldCharacters</code>.
   *  @return replaced string.
  **/
  public static String replace(String text, String oldCharacters, String newCharacters) {
    final int l = oldCharacters.length();
    if (l > 0) {
      StringBuffer buffer = new StringBuffer(Math.max(10, text.length()+2*(newCharacters.length()-l)));
      int i = 0;
      for (int j = 0;; i = j+l) {
        j = text.indexOf(oldCharacters, i);
        if (j == -1) {
          break;
        }
        buffer.append(text.substring(i, j));
        buffer.append(newCharacters);
      }
      buffer.append(text.substring(i));
      text = buffer.toString();
    }
    return text;
  }

  /**
   *  Counts the number of digits in the specified text.
   *  @param text string
   *  @return number of digits in the specified text.
  **/
  public static int numberOfDigits(String text) {
    final int l = text.length();
    int count = 0;
    for (int i = 0; i < l; ++i) {
      if (Character.isDigit(text.charAt(i))) {
        ++count;
      }
    }
    return count;
  }

  /**
   *  Returns the index within the specified string of the first occurrence
   *  of the specified substring ignoring case considerations and
   *  starting at the specified index.
   *  The integer returned is the smallest value
   *  <i>k</i> such that:
   *  <blockquote><pre>
   *  string.startsWith(substring, <i>k</i>)
   *  </pre></blockquote>
   *  is <code>true</code>.
   *
   *  @param   string  any string.
   *  @param   substring   any string.
   *  @param   fromIndex   the index from which to start the search.
   *  @return  if the second string argument occurs as a substring within the
   *           first string argument, then the index of the first character of the first
   *           such substring is returned; if it does not occur as a
   *           substring, <code>-1</code> is returned.
   *  @exception java.lang.NullPointerException if <code>string</code> or <code>substring</code> is
   *           <code>null</code>.
  **/
  public static int indexOfIgnoreCase(String string, String substring, int fromIndex) {
    int l = string.length();
    if (fromIndex >= l) {
      return (substring.length() == 0)? l : -1;
    }
    char[] target = substring.toCharArray();
    if (fromIndex < 0) {
     fromIndex = 0;
   }
    if (target.length == 0) {
      return fromIndex;
    }
    char firstUpperCase = Character.toUpperCase(target[0]);
    char firstLowerCase = Character.toLowerCase(target[0]);
    l -= target.length;
    for (int i = fromIndex; i <= l; ++i) {
      char c = string.charAt(i);
      if (Character.toUpperCase(c) == firstUpperCase || Character.toLowerCase(c) == firstLowerCase) {
        int j = 0;
        while (++j < target.length) {
          char d = string.charAt(i+j);
          if (Character.toUpperCase(d) != Character.toUpperCase(target[j]) && Character.toLowerCase(d) != Character.toLowerCase(target[j])) {
            break;
          }
        }
        if (j == target.length) {
          return i;
        }
      }
    }
    return -1;
  }
}
