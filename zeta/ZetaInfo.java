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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import zeta.util.Properties;
import zeta.util.StreamUtils;

/**
 *  The <code>ZetaInfo</code> class writes information in the specified log file 'info.log'
 *  and the event log file. There is also a usual roll over behaviour of log files.
 *
 *  @version 2.0, August 6, 2005
**/
public class ZetaInfo {

  /**
   *  Initialize the configuration.
   *  @param properties properties
  **/
  public static void init(Properties properties) {
    String infoOutput = properties.get("info.output");
    if (infoOutput != null && infoOutput.equals("false")) {
      setStandardOutput(false);
    }
    String exceptionOutput = properties.get("exception.output");
    if (exceptionOutput != null && exceptionOutput.equals("false")) {
      setExceptionOutput(false);
    }
    String appendsTimestamp = properties.get("info.timestamp");
    if (appendsTimestamp != null) {
      setAppendsTimestamp(appendsTimestamp);
    }
    filename = properties.get("info.filename", "info.tmp"); // ToDo: remove default "info.tmp"
    eventLogFilename = properties.get("info.log.filename", "");
    String eventLogTimestamp = properties.get("info.log.timestamp");
    if (eventLogTimestamp != null) {
      setEventLogTimestamp(eventLogTimestamp);
    }
    eventLogFileSize = toFileSize(properties.get("info.log.file_size"));
    eventLogMaxBackupIndex = properties.get("info.log.max_backup_index", 0);
  }

  /**
   *  Writes the specified information in the specified log file and the event log file.
   *  @param information information
  **/
  public static void write(String information) {
    write(information, System.out);
  }

  /**
   *  Writes the specified information in the specified log file and the event log file.
   *  @param information information
   *  @param output output stream
  **/
  public static void write(String information, PrintStream output) {
    Date rightNow = new Date();
    String informationTimestamp = information;
    if (appendsTimestamp != null) {
      StringBuffer buffer = new StringBuffer(information.length()+30);
      buffer.append(information);
      buffer.append(" (");
      buffer.append(appendsTimestamp.format(rightNow));
      buffer.append(')');
      informationTimestamp = buffer.toString();
    }
    if (standardOutput) {
      output.println(informationTimestamp);
    }
    if (filename != null && filename.length() > 0) {
      FileOutputStream fout = null;
      try {
        fout = new FileOutputStream(filename);
        fout.write(informationTimestamp.getBytes());
        fout.flush();
      } catch (IOException ioe) {
      } finally {
        StreamUtils.close(fout);
      }
    }
    if (eventLogFileSize > 0 && eventLogFilename.length() > 0 && information.trim().length() > 0) {
      synchronized (eventLogFilename) {
        FileOutputStream fout = null;
        try {
          boolean append = (eventLogFileSize > new File(eventLogFilename).length());
          if (!append) {
            rollOver();
          }
          fout = new FileOutputStream(eventLogFilename, append);
          StringBuffer buffer = null;
          if (eventLogTimestamp != null) {
            buffer = new StringBuffer(information.length()+newLine.length()+30);
            buffer.append('[');
            buffer.append(eventLogTimestamp.format(rightNow));
            buffer.append("] ");
          } else {
            buffer = new StringBuffer(information.length()+newLine.length());
          }
          buffer.append(information);
          buffer.append(newLine);
          fout.write(buffer.toString().getBytes());
          fout.flush();
        } catch (IOException ioe) {
        } finally {
          StreamUtils.close(fout);
        }
      }
    }
  }

  /**
   *  display exception data
  **/
  public static void handle(Throwable t) {
    if (exceptionOutput) {
      t.printStackTrace();
    }
    if (eventLogFileSize > 0 && eventLogFilename.length() > 0) {
      synchronized (eventLogFilename) {
        FileOutputStream fout = null;
        try {
          boolean append = (eventLogFileSize > new File(eventLogFilename).length());
          if (!append) {
            rollOver();
          }
          fout = new FileOutputStream(eventLogFilename, append);
          if (eventLogTimestamp != null) {
            StringBuffer buffer = new StringBuffer(30);
            buffer.append('[');
            buffer.append(eventLogTimestamp.format(new Date()));
            buffer.append("] ");
            fout.write(buffer.toString().getBytes());
          }
          t.printStackTrace(new PrintStream(fout));
          fout.flush();
        } catch (IOException ioe) {
        } finally {
          StreamUtils.close(fout);
        }
      }
    }
  }

  /**
   *  Appends timestamps after every information.
  **/
  public static void setAppendsTimestamp(String appendsTimestamp) {
    if (appendsTimestamp == null || appendsTimestamp.length() == 0) {
      ZetaInfo.appendsTimestamp = null;
    } else {
      ZetaInfo.appendsTimestamp = new SimpleDateFormat(appendsTimestamp);
    }
  }

  /**
   *  Timestamps at the beginning of every information in the event log file.
  **/
  public static void setEventLogTimestamp(String eventLogTimestamp) {
    if (eventLogTimestamp == null || eventLogTimestamp.length() == 0) {
      ZetaInfo.eventLogTimestamp = null;
    } else {
      ZetaInfo.eventLogTimestamp = new SimpleDateFormat(eventLogTimestamp);
    }
  }

  /**
   *  Puts every information also on the standard output stream.
  **/
  public static void setStandardOutput(boolean standardOutput) {
    ZetaInfo.standardOutput = standardOutput;
  }

  /**
   *  Puts every exception on the standard output stream.
  **/
  public static void setExceptionOutput(boolean exceptionOutput) {
    ZetaInfo.exceptionOutput = exceptionOutput;
  }

  /**
   *  Implements the usual roll over behaviour of log files.
  **/
  private static void rollOver() {  // its call is synchronized
    if (eventLogMaxBackupIndex > 0) {
      File file = new File(eventLogFilename + '.' + eventLogMaxBackupIndex);
      if (file.exists()) {
        file.delete();
      }
      for (int i = eventLogMaxBackupIndex-1; i >= 1; --i) {
        File f = new File(eventLogFilename + '.' + i);
        if (f.exists()) {
          f.renameTo(file);
        }
        file = f;
      }
      new File(eventLogFilename).renameTo(file);
    }
  }

  private static long toFileSize(String value) {
    if (value != null && value.length() > 0) {
      value = value.toLowerCase();
      try {
        int i = value.indexOf("kb");
        if (i >= 0) {
          return Long.valueOf(value.substring(0, i).trim()).longValue() * 1024;
        }
        i = value.indexOf("mb");
        if (i >= 0) {
          return Long.valueOf(value.substring(0, i).trim()).longValue() * 1024 * 1024;
        }
        i = value.indexOf("gb");
        if (i >= 0) {
          return Long.valueOf(value.substring(0, i).trim()).longValue() * 1024 * 1024 * 1024;
        }
        return Long.valueOf(value.trim()).longValue();
      } catch (NumberFormatException e) {
        handle(e);
      }
    }
    return 0;
  }


  private static SimpleDateFormat appendsTimestamp = null;

  private static boolean standardOutput = true;

  private static boolean exceptionOutput = true;

  /**
   *  Filename of the information.
  **/
  private static String filename = null;

  private static String eventLogFilename = "";
  private static SimpleDateFormat eventLogTimestamp = null;
  private static long eventLogFileSize = 0;
  private static int eventLogMaxBackupIndex = 0;

  private static String newLine = System.getProperty("line.separator");
}
