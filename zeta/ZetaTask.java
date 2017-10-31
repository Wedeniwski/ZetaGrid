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

package zeta;

import java.util.ArrayList;
import java.util.List;

import zeta.util.Properties;

/**
 *  @deprecated
 *  @version 2.0, August 6, 2005
 *  @see zeta.example.ZetaTask
**/
public class ZetaTask extends ClientTask {
  static {
    try {
      System.loadLibrary("zeta_zeros");
    } catch (Exception e) {
      ZetaInfo.handle(e);
    }
  }

  public ZetaTask(int id, String name, Class workUnitClass) {
    super(id, name, workUnitClass);
    try {
      properties = new Properties(Properties.ZETA_CFG, Properties.DEFAULT_CFG);
    } catch (IOException ioe) {
      ZetaInfo.handle(ioe);
    }
  }

  public String getVersion() {
    return version;
  }

  public void setEnableStandardOutput(boolean enableStandardOutput) {
    setCoutLog(enableStandardOutput);
  }

  public void setResources(String resources) {
    if (resources != null) {
      try {
        setZetaResources(Integer.parseInt(resources));
      } catch (Exception e) {
        ZetaInfo.handle(e);
      }
    } else {
      setZetaResources(0);
    }
  }

  public int start(WorkUnit workUnit, int cpuUsage) {
    if (cpuUsage == 100) {
      cpuUsage = 0;
    } else {
      cpuUsage = (int)((cpuUsage*12001.0)/100);
    }
    return zetaZeros(workUnit.getWorkUnitId(), workUnit.getSize(), cpuUsage);
  }

  public int stop() {
    try {
      zetaExit();
    } catch (Throwable t) {
      ZetaInfo.handle(t);
    }
    return 2000;
  }

  /**
   *  Contains a persistent set of the ZetaGrid properties.
  **/
  private Properties properties;

  private native static String getZetaVersion();
  private native static int zetaZeros(long workUnitId, int size, int sleepN);
  private native static void zetaExit();
  private native static void setCoutLog(boolean coutLog);
  private native static void setZetaResources(int resourceId);
  private static String version = getZetaVersion();
}
