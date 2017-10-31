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

package zeta.server.processor;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;

import zeta.WorkUnit;
import zeta.server.util.Parameter;
import zeta.util.StreamUtils;

/**
 *  Processes work units as files received through the result handler.
 *
 *  @version 1.9.4, August 27, 2004
**/
public class SimpleFileProcessor extends DefaultWorkUnitProcessor {
  /**
   *  Processes work units received through the result handler.
   *  The work unit will be stored in a shared path which is defined by the task specific parameter 'path_data' .
   *  The filename is defined by the method zeta.WorkUnit.getWorkUnitFileName().
   *  @param stmt statement object's database
   *  @param workUnit work unit which should be processed
   *  @param result buffer with the zipped result
   *  @return <code>true</code> if the ResultHandler shall save the result into the database.
   *  @exception  IOException  if an I/O error occurs.
   *  @see zeta.WorkUnit#getWorkUnitFileName()
  **/
  public boolean processResult(Statement stmt, WorkUnit workUnit, byte[] result) throws ServletException, SQLException, IOException {
    String pathData = Parameter.getValue(stmt, "path_data", workUnit.getTaskId(), "", 3600000);
    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(pathData + workUnit.getWorkUnitFileName());
      fout.write(result);
    } finally {
      StreamUtils.close(fout);
    }
    return false;
  }
}
