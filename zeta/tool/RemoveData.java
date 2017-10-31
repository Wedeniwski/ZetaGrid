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

  Version 1.9.3, May 29, 2004

  This program is based on the work of:
     S. Wedeniwski
--*/

package zeta.tool;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import zeta.server.tool.Database;
import zeta.server.util.DatabaseUtils;
import zeta.util.ThrowableHandler;


public class RemoveData {
  public static void main(String[] args) {
    remove();
  }

  // ToDo: not fix task_id!!
  static void remove() {
    File file = new File("calc/1");
    File[] list = file.listFiles();
    if (list == null) {
      return;
    }
    List nstart = new ArrayList(list.length);
    for (int i = 0; i < list.length; ++i) {
      if (System.currentTimeMillis()-list[i].lastModified() < 24*60*60*1000) {
        String s = list[i].getName();
        int l = s.length();
        if (s.startsWith("zeta_zeros_") && l > 11 && Character.isDigit(s.charAt(11))) {
          int idx = 11;
          while (idx < l && Character.isDigit(s.charAt(idx))) ++idx;
          nstart.add(s.substring(11, idx));
        }
      }
    }
    int l = nstart.size();
    if (l == 0) {
      return;
    }
    Connection connection = null;
    Statement stmt = null;
    try {
      connection = Database.getConnection();
      stmt = connection.createStatement();
      StringBuffer s = new StringBuffer(500);
      s.append("UPDATE zeta.result SET result=NULL WHERE task_id=1 AND NOT result IS NULL AND work_unit_id IN (");
      s.append((String)nstart.get(0));
      for (int i = 1; i < l; ++i) {
        s.append(',');
        s.append((String)nstart.get(i));
        if (s.length() > 450 && i+1 < l) {
          s.append(')');
          stmt.executeUpdate(s.toString());
          s.delete(0, s.length());
          s.append("UPDATE zeta.result SET result=NULL WHERE task_id=1 AND NOT result IS NULL AND work_unit_id IN (");
          s.append((String)nstart.get(++i));
        }
      }
      s.append(')');
      stmt.executeUpdate(s.toString());
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(connection);
    }
  }
}
