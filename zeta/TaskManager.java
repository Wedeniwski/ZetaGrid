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

package zeta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import zeta.crypto.KeyManager;
import zeta.crypto.Signature;
import zeta.util.StreamUtils;

/**
 *  @version 1.9.4, August 27, 2004
**/
public class TaskManager {

  public static synchronized TaskManager getInstance(String filename) {
    if (taskManager == null) {
      taskManager = new TaskManager(filename);
    }
    return taskManager;
  }

  public ClientTask getClientTask(String taskName) {
    return (ClientTask)clientTasks.get(taskName);
  }

  private TaskManager(String filename) {
    boolean error = true;
    boolean notFoundError = false;
    try {
      String content = new String(StreamUtils.getFile(filename, false, false), "UTF-8");
      if (content.startsWith("Tasks:")) {
        int i = content.indexOf('\n');
        if (i >= 0 && i+1 < content.length()) {
          ZetaInfo.write("Check digital signature of \'" + filename + '\'');
          Signature sig = new Signature(KeyManager.getKey(null));
          if (sig.verify(content.substring(i+1), content.substring(0, i).getBytes("UTF-8"))) {
            error = false;
            StringTokenizer st = new StringTokenizer(content.substring(6, i), ";");
            while (st.hasMoreTokens()) {
              String s = st.nextToken();
              i = s.indexOf(',');
              int j = s.indexOf(',', i+1);
              int k = s.indexOf(',', j+1);
              if (i >= 0 && j > i && k > j) {
                try {
                  Integer taskId = new Integer(s.substring(0, i));
                  String taskName = s.substring(i+1, j);
                  Class clientTaskClass = Class.forName(s.substring(j+1, k));
                  Class workUnitClass = Class.forName(s.substring(k+1));
                  Constructor c = clientTaskClass.getDeclaredConstructor(new Class[] { int.class, String.class, Class.class });
                  clientTasks.put(taskName, c.newInstance(new Object[] { taskId, taskName, workUnitClass }));
                } catch (Exception e) {
                  ZetaInfo.handle(e);
                }
              }
            }
          }
        }
      }
    } catch (FileNotFoundException ioe) {
      notFoundError = true;
      ZetaInfo.write("There is no local task definition.");
    } catch (IOException ioe) {
      ZetaInfo.handle(ioe);
    }
    if (error || clientTasks.size() == 0) {
      if (!notFoundError) {
        ZetaInfo.write("The local task definition is invalid.");
      }
      // ToDo: remove code
      try {
        Integer taskId = new Integer(1);
        String taskName = "zeta-zeros";
        Class clientTaskClass = Class.forName("zeta.ZetaTask");
        Class workUnitClass = Class.forName("zeta.example.ZetaWorkUnit");
        Constructor c = clientTaskClass.getDeclaredConstructor(new Class[] { int.class, String.class, Class.class });
        clientTasks.put(taskName, c.newInstance(new Object[] { taskId, taskName, workUnitClass }));
      } catch (Exception e) {
      }
    }
  }

  private Map clientTasks = new HashMap();
  private static TaskManager taskManager = null;
}
