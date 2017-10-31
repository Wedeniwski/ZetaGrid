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

package zeta.server.tool;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import zeta.server.Server;
import zeta.server.ServerTask;
import zeta.server.TaskManager;
import zeta.server.util.DatabaseUtils;
import zeta.tool.KeepAlive;
import zeta.tool.ZetaApprove;
import zeta.tool.ZetaCD;
import zeta.tool.ZetaIndexHTML;
import zeta.tool.ZetaStatistic;
import zeta.tool.ZetaStatisticHTML;
import zeta.util.Properties;
import zeta.util.ThrowableHandler;

/**
 *  @version 2.0, August 6, 2005
**/
public class Daemon {

  public static boolean allowDbRestart() {
    return allowDbRestart;
  }

  public static void setAllowDbRestart(boolean allowDbRestart) {
    Daemon.allowDbRestart = allowDbRestart;
    if (!allowDbRestart) {
      try {
        Thread.sleep(2000);   // wait 2 sec.
      } catch (InterruptedException ie) {
      }
    }
  }

  public static void main(String[] args) {
    if (args.length == 1 && args[0].length() == 1 && args[0].charAt(0) == 's') {
      statistic();
      return;
    }
    String user = null;
    String password = null;
    String restart = null;
    if (args.length == 3) {
      restart = args[0];
      user = args[1];
      password = args[2];
    } else if (args.length == 2) {
      user = args[0];
      password = args[1];
    }
    boolean doSynchronization = false;
    boolean doGetData = false;
    boolean doReorg = false;
    Integer serverId = null;
    Trust trust = null;
    Connection con = null;
    try {
      con = Database.getConnection();
      serverId = new Integer(Server.getInstance(con).getId());
      Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
      daemonWait = properties.get("daemon.wait", 30 * 60000);
      doSynchronization = properties.get("synchronization", "false").equals("true");
      doGetData = properties.get("get.data", "true").equals("true");
      trust = new Trust();
      new KeepAlive(restart);
      try {
        Thread.sleep(60*1000);  // wait 1 min.
      } catch (InterruptedException ie) {
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
      return;
    } finally {
      DatabaseUtils.close(con);
      con = null;
    }
    boolean createAllStatistics = true;
    CreateStatistics statistics = new CreateStatistics();
    ServerSynchronization sync = new ServerSynchronization(null);
    SimpleDateFormat sqlFormatter = new SimpleDateFormat("yyyy-MM-dd");
    String lastBackup = sqlFormatter.format(new Date());
    while (true) {
      try {
        Date d = new Date();
        System.out.println("Start at " + formatter.format(d) + '.');
        long time = d.getTime();
        if (doGetData) {
          setAllowDbRestart(false);
          if (GetData.get()) {
            //RemoveData.remove();
          }
          setAllowDbRestart(true);
        }
        if (doSynchronization) {
          for (int i = 0; i < 10; ++i) {
            System.out.println("Start server synchronization at " + formatter.format(new Date()) + '.');
            sync.synchronization(serverId);
            if (sync.size() < 100) {
              break;
            }
          }
        }
        if (doReorg) {
          doReorg = false;
          if (user != null && password != null) {
            setAllowDbRestart(false);
            // start redistribution of old work units
            DatabaseReorg reorg = new DatabaseReorg(user, password);
            con = null;
            try {
              con = Database.getConnection();
              Iterator i = TaskManager.getInstance(con).getServerTasks();
              while (i.hasNext()) {
                ServerTask serverTask = (ServerTask)i.next();
                reorg.hardReorg(serverTask, 0, 0, 0, false);
              }
            } catch (SQLException se) {
              ThrowableHandler.handle(se);
            } finally {
              DatabaseUtils.close(con);
              con = null;
            }
            // approve findings
            ZetaApprove approve = new ZetaApprove(user, password);
            approve.approve(false);
            approve.close();
            setAllowDbRestart(true);
          }
        }
        String s = sqlFormatter.format(d);
        if (!s.equals(lastBackup)) {
          setAllowDbRestart(false);
          ZetaIndexHTML.generate();
          trust.trust();
          // start backup
          System.out.println("Start backup at " + formatter.format(new Date()) + '.');
          DatabaseBackup.backup(s);
          lastBackup = s;
          doReorg = true;
          try {   // wait 5 minutes to finalize table and index reorg
            Thread.sleep(5*60*1000);
          } catch (InterruptedException ie) {
          }
          setAllowDbRestart(true);
        }
        System.out.println("Start statistics at " + formatter.format(new Date()) + '.');
        statistics.updateData(createAllStatistics);
        createAllStatistics = false;
        time = System.currentTimeMillis() - time;
        System.out.println("End at " + formatter.format(new Date()) + '.');
        if (time < daemonWait) {
          try {
            Thread.sleep(daemonWait-time);
          } catch (InterruptedException ie) {
          }
        }
      } catch (Throwable e) {
        ThrowableHandler.handle(e);
        setAllowDbRestart(true);
      }
    }
    //System.out.println("End at " + formatter.format(new Date()) + '.');
  }

  private static void statistic() {
    try {
      while (true) {
        Date d = new Date();
        System.out.println("Start at " + formatter.format(d) + '.');
        int[] previousNumberOfCreatedCDFiles = new int[2];
        previousNumberOfCreatedCDFiles[0] = -1;
        while (true) {
          ZetaStatistic.calc(ZetaCD.MAX_NUMBER_OF_FILES/2);
          int[] numberOfCreatedCDFiles = ZetaCD.create(ZetaCD.CD_SIZE_LARGE);
          if (numberOfCreatedCDFiles[0] == 0 && numberOfCreatedCDFiles[1] >= 0 && previousNumberOfCreatedCDFiles[0] == numberOfCreatedCDFiles[0] && previousNumberOfCreatedCDFiles[1] == numberOfCreatedCDFiles[1]) {
            break;
          }
          previousNumberOfCreatedCDFiles[0] = numberOfCreatedCDFiles[0];
          previousNumberOfCreatedCDFiles[1] = numberOfCreatedCDFiles[1];
        }
        ZetaStatisticHTML.generate();
        System.out.println("End at " + formatter.format(new Date()) + '.');
        try {
          Thread.sleep(24*daemonWait);
        } catch (InterruptedException ie) {
        }
      }
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    }
  }

  static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private static int daemonWait = 30 * 60 * 1000;
  private static boolean allowDbRestart = true;
}
