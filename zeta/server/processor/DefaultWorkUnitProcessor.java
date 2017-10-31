/*--
  This file is a part of ZetaGrid, a simple and secure Grid Computing
  kernel.

  Copyright (c) 2001-2005 Sebastian Wedeniwski.  All rights reserved.

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
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;

import zeta.WorkUnit;
import zeta.server.ServerTask;
import zeta.util.StreamUtils;

/**
 *  The default processor for work units which are received through the request and the result handler.
 *
 *  @version 2.0, August 6, 2005
**/
public class DefaultWorkUnitProcessor implements TaskRequestWorkUnitProcessor, TaskResultProcessor {
  /**
   *  Checks work units received through the result handler
   *  @param workUnit work unit which should be checked
   *  @param result buffer with the zipped result
  **/
  public void checkResult(WorkUnit workUnit, byte[] result) throws Exception {
    ZipInputStream zip = null;
    try {
      zip = new ZipInputStream(new ByteArrayInputStream(result));
      if (zip.getNextEntry() == null) {
        throw new IOException("empty");
      } else {
        while (zip.getNextEntry() != null);
      }
    } finally {
      StreamUtils.close(zip);
    }
  }

  /**
   *  Processes work units received through the result handler
   *  @param stmt statement object's database
   *  @param workUnit work unit which should be processed
   *  @param result buffer with the zipped result
   *  @return <code>true</code> if the ResultHandler shall save the result into the database.
   *  @exception  IOException  if an I/O error occurs.
  **/
  public boolean processResult(Statement stmt, WorkUnit workUnit, byte[] result) throws ServletException, SQLException, IOException {
    return true;
  }

  /**
   *  Returns the parameters which are associated with the specified task and work unit; are separated by the character ','
   *  @param workUnit work unit
   *  @return parameters which are associated with the specified task and work unit; are separated by the character ','
  **/  
  public String getParameters(ServerTask serverTask, WorkUnit workUnit) {
    String parameters = workUnit.getParameters();
    return (parameters != null)? parameters : serverTask.getTaskParameters();
  }

  /**
   *  Activates the specified work unit for the requested client. This work unit does not contain parameters.
   *  @param stmt statement object's database
   *  @param workUnit work unit
   *  @return less than 0 if an error occurs, 0 if the specified work unit is activated but no further work unit can be activated,
   *          and greater 0 if the specified work unit is activated and further work units can be activated.
   *  @exception  SQLException  if a database access error occurs.
  **/
  public int activateWorkUnit(Statement stmt, WorkUnit workUnit) throws ServletException, SQLException {
    return 1;
  }
}
