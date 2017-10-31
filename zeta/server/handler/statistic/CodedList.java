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

package zeta.server.handler.statistic;

/**
 *  Interface to serialize a table in a compressed format.
 *
 *  @version 1.9.3, May 29, 2004
**/
public interface CodedList {
  /**
   *  Encodes a specified column name.
   *  @param column column name
   *  @return encoded column name.
  **/
  public String encode(String column);

  /**
   *  Decodes a specified column name.
   *  @param content text with column name
   *  @param pos encoded text starts at the specified position
   *  @return decoded column name.
  **/
  public String decode(String content, int pos);

  /**
   *  Decodes that value of the column.
   *  @param content text of the column
   *  @param pos encoded text starts at the specified position
   *  @return value of the decoded column.
  **/
  public String decodeValue(String content, int pos, int end);

  /**
   *  Returns the alignment of the value of the decoded column.
   *  @param content text of the column
   *  @param pos encoded text starts at the specified position
   *  @return the alignment of the value of the decoded column.
  **/
  public int getAlignment(String content, int pos);
}
