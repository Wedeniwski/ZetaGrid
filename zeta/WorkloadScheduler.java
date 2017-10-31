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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *  Controls the computation in specified timeframes.
 *
 *  @version 1.9.3, May 29, 2004
**/
public class WorkloadScheduler {

  /**
   *  Constructs the environment for the scheduler.
   *  The timeframes can be defined by the following format:
   *  {<Day of Week><start hour>:<start minute>-<stop hour>:<stop minute>}*[,<CPU usage>]}*
   *  where
   * <Day of Week>: the name of the day, i.e. (Mo, Tu, We, Th, Fr, Sa, Su)
   * <start hour>: controls what hour the computation will start, and is specified
   *               in the 24 hour clock, values must be between 0 and 23
   *               (0 is midnight)
   * <start minute>: controls what minute of the hour the computation will start,
   *                 value must be between 0 and 59
   * <stop hour>: controls what hour the computation will start, and is specified
   *              in the 24 hour clock, values must be between 0 and 23
   *              (0 is midnight)
   * <stop minute>: controls what minute of the hour the computation will stop,
   *                value must be between 0 and 59
   * <CPU usage>: usage of the processor power during the timeframe
   * Example:
   * Mo08:00-12:00,80Mo13:00-20:00Tu00:00-00:00We00:00-00:00Th00:00-00:00Fr00:00-00:00
   * @param activeAt the timeframes when the client can activate the computation
   * @param defaultCPUUsage the default value for all timeframes
  **/
  public WorkloadScheduler(String activeAt, int defaultCPUUsage) {
    if (defaultCPUUsage > 100) {
      defaultCPUUsage = 100;
    }
    if (defaultCPUUsage < 1) {
      defaultCPUUsage = 1;
    }
    this.defaultCPUUsage = defaultCPUUsage;
    if (activeAt != null && activeAt.length() > 0) {
      final String[] dayOfWeek = { "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su" };
      final int[] calendarDayOfWeek = { Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY };
      for (int i = 0; i < 7; ++i) {
        List timeframes = new ArrayList(8);
        int pos = 0;
        while (true) {
          int[] res = timeframe(activeAt, pos, dayOfWeek[i]);
          pos = res[0];
          if (pos == 0) {
            break;
          }
          if (checkTimeframe(res[1], res[2], res[3], res[4])) {
            addTimeframe(timeframes, res[1], res[2], res[3], res[4], res[5]);
          }
        }
        int l = timeframes.size();
        if (l > 0) {
          int k = calendarDayOfWeek[i];
          this.activeAt[k] = new int[3*l];
          for (int j = 0; j < l; ++j) {
            int[] tf = (int[])timeframes.get(j);
            this.activeAt[k][3*j] = (tf[0] << 6) | tf[1];
            this.activeAt[k][3*j+1] = (tf[2] << 6) | tf[3];
            this.activeAt[k][3*j+2] = tf[4];
          }
          alwaysActive = false;
        }
      }
    }
  }

  /**
   *  Checks if the computation can be always active.
   *  @return <code>true</code> if the computation can be always active.
  **/
  public boolean isAlwaysActive() {
    return alwaysActive;
  }

  /**
   *  Checks if the computation should be active right now.
   *  @return <code>true</code> if the computation should be active right now.
  **/
  public boolean isActive() {
    if (alwaysActive) {
      return true;
    }
    Calendar rightNow = Calendar.getInstance();
    int dayOfWeek = rightNow.get(Calendar.DAY_OF_WEEK);
    if (activeAt[dayOfWeek] == null) {
      return false;
    }
    int time = (rightNow.get(Calendar.HOUR_OF_DAY) << 6) | rightNow.get(Calendar.MINUTE);
    int l = activeAt[dayOfWeek].length;
    for (int i = 2; i < l; i += 3) {
      if (time >= activeAt[dayOfWeek][i-2]) {
        if (time < activeAt[dayOfWeek][i-1]) {
          return true;
        }
      } else {
        return false;
      }
    }
    return false;
  }

  /**
   *  Returns the CPU usage which should be active right now.
   *  @return the CPU usage which should be active right now.
  **/
  public int getCPUUsage() {
    Calendar rightNow = Calendar.getInstance();
    int dayOfWeek = rightNow.get(Calendar.DAY_OF_WEEK);
    if (activeAt[dayOfWeek] == null) {
      return defaultCPUUsage;
    }
    int time = (rightNow.get(Calendar.HOUR_OF_DAY) << 6) | rightNow.get(Calendar.MINUTE);
    int l = activeAt[dayOfWeek].length;
    for (int i = 2; i < l; i += 3) {
      if (time >= activeAt[dayOfWeek][i-2]) {
        if (time < activeAt[dayOfWeek][i-1]) {
          return activeAt[dayOfWeek][i];
        }
      } else {
        return defaultCPUUsage;
      }
    }
    return defaultCPUUsage;
  }

  /**
   *  Returns in milliseconds how long nothing will happen for the scheduler.
   *  @return in milliseconds how long nothing will happen for the scheduler.
  **/
  public long getTimeMillisToNextTimeframe() {
    if (alwaysActive) {
      return -1;
    }
    Calendar rightNow = Calendar.getInstance();
    int dayOfWeek = rightNow.get(Calendar.DAY_OF_WEEK);
    if (activeAt[dayOfWeek] != null) {
      int l = activeAt[dayOfWeek].length;
      if (l > 0) {
        int time = (rightNow.get(Calendar.HOUR_OF_DAY) << 6) | rightNow.get(Calendar.MINUTE);
        for (int i = 1; i < l; i += 3) {
          if (time >= activeAt[dayOfWeek][i-1]) {
            if (time < activeAt[dayOfWeek][i]) {
              long t = ((activeAt[dayOfWeek][i] >>> 6)-rightNow.get(Calendar.HOUR_OF_DAY))*60 + (activeAt[dayOfWeek][i]&63)-rightNow.get(Calendar.MINUTE);
              return (t*60-rightNow.get(Calendar.SECOND))*1000;
            }
          } else {
            long t = ((activeAt[dayOfWeek][i-1] >>> 6)-rightNow.get(Calendar.HOUR_OF_DAY))*60 + (activeAt[dayOfWeek][i-1]&63)-rightNow.get(Calendar.MINUTE);
            return (t*60-rightNow.get(Calendar.SECOND))*1000;
          }
        }
      }
    }
    long time = 60*(24-rightNow.get(Calendar.HOUR_OF_DAY))-rightNow.get(Calendar.MINUTE);
    return (time*60-rightNow.get(Calendar.SECOND))*1000;
  }

  private static boolean checkTimeframe(int startHour, int startMinute, int stopHour, int stopMinute) {
    // check correct values
    if (startHour < 0 || startHour > 23 || stopHour < 0 || stopHour > 23) {
      ZetaInfo.write("A timestamp will be ignored since hour is not between 0 and 23.");
      return false;
    }
    if (startMinute < 0 || startMinute > 59 || stopMinute < 0 || stopMinute > 59) {
      ZetaInfo.write("A timestamp will be ignored since minutes are not between 0 and 59.");
      return false;
    }
    // the end of the timeframe must be after the begin, except midnight
    if ((stopHour != 0 || stopMinute != 0) && (stopHour < startHour || stopHour == startHour && stopMinute <= startMinute)) {
      ZetaInfo.write("Timeframe " + startHour + ((startMinute >= 10)? ":" + startMinute : ":0" + startMinute)
                     + '-' + stopHour + ((stopMinute >= 10)? ":" + stopMinute : ":0" + stopMinute) + " is not correct defined and will be ignored.");
      return false;
    }
    return true;
  }

  private static void addTimeframe(List timeframes, int startHour, int startMinute, int stopHour, int stopMinute, int cpuUsage) {
    if (stopHour == 0 && stopMinute == 0) {
      stopHour = 24;
    }
    if (cpuUsage > 100) {
      cpuUsage = 100;
    }
    if (cpuUsage < 0) {
      cpuUsage = 0;
    }
    // Timeframes which overlaps a previous timeframe will be ignored
    int i = 0;
    for (int l = timeframes.size(); i < l; ++i) {
      int[] timeframe = (int[])timeframes.get(i);
      if (startHour < timeframe[0] || startHour == timeframe[0] && startMinute < timeframe[1]) {
        if (stopHour < timeframe[0] || stopHour == timeframe[0] && stopMinute < timeframe[1]) {
          break;
        } else {
          ZetaInfo.write("Timeframe " + startHour + ((startMinute >= 10)? ":" + startMinute : ":0" + startMinute)
                         + '-' + stopHour + ((stopMinute >= 10)? ":" + stopMinute : ":0" + stopMinute) + " will be ignored since it overlaps a previous timeframe.");
          return;
        }
      } else if (startHour < timeframe[2] || startHour == timeframe[2] && startMinute <= timeframe[3]) {
        ZetaInfo.write("Timeframe " + startHour + ((startMinute >= 10)? ":" + startMinute : ":0" + startMinute)
                       + '-' + stopHour + ((stopMinute >= 10)? ":" + stopMinute : ":0" + stopMinute) + " will be ignored since it overlaps a previous timeframe.");
        return;
      }
    }
    timeframes.add(i, new int[] { startHour, startMinute, stopHour, stopMinute, cpuUsage });
  }

  private int[] timeframe(String activeAt, int pos, String dayOfWeek) {
    int startHour = 0;
    int startMin = 0;
    int stopHour = 0;
    int stopMin = 0;
    int cpuUsage = defaultCPUUsage;
    int i = activeAt.indexOf(dayOfWeek, pos);
    pos = 0;
    if (i >= 0) {
      int l = activeAt.length();
      int j = i+dayOfWeek.length();
      if (j < l) {
        while (j < l && Character.isDigit(activeAt.charAt(j))) {
          startHour = 10*startHour + Character.digit(activeAt.charAt(j), 10);
          ++j;
        }
        if (++j < l && activeAt.charAt(j-1) == ':') {
          while (j < l && Character.isDigit(activeAt.charAt(j))) {
            startMin = 10*startMin + Character.digit(activeAt.charAt(j), 10);
            ++j;
          }
          if (++j < l && activeAt.charAt(j-1) == '-') {
            while (j < l && Character.isDigit(activeAt.charAt(j))) {
              stopHour = 10*stopHour + Character.digit(activeAt.charAt(j), 10);
              ++j;
            }
            if (j < l && activeAt.charAt(j) == ':') {
              while (++j < l && Character.isDigit(activeAt.charAt(j))) {
                stopMin = 10*stopMin + Character.digit(activeAt.charAt(j), 10);
              }
              if (j < l && activeAt.charAt(j) == ',') {
                cpuUsage = 0;
                while (++j < l && Character.isDigit(activeAt.charAt(j))) {
                  cpuUsage = 10*cpuUsage + Character.digit(activeAt.charAt(j), 10);
                }
              }
              pos = j;
            } else {
              startHour = startMin = stopHour = 0;
            }
          } else {
            startHour = startMin = 0;
          }
        } else {
          startHour = 0;
        }
      }
    }
    return new int[] { pos, startHour, startMin, stopHour, stopMin, cpuUsage };
  }

  private boolean alwaysActive = true;
  private int defaultCPUUsage;
  private final int MAX_DAY = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Calendar.SUNDAY, Calendar.MONDAY), Calendar.TUESDAY), Calendar.WEDNESDAY), Calendar.THURSDAY), Calendar.FRIDAY), Calendar.SATURDAY)+1;
  private int[][] activeAt = new int[MAX_DAY][MAX_DAY];
}
