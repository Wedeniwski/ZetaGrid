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

package zeta.server.tool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;

import zeta.server.DispatcherServlet;
import zeta.server.handler.statistic.AbstractHandler;
import zeta.server.tool.Daemon;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.PageConverter;
import zeta.util.Properties;
import zeta.util.StreamUtils;
import zeta.util.ThrowableHandler;

/**
 *  Creates the specified statistic pages and data, and stores them in the database asynchronous.
 *  Then the application server does not create the statistics and can view them by retrieving
 *  the pages,data and images from the database. The format is HTML for the pages (compressed as ZIP),
 *  XML for the data, and should be PNG for the images.
 *  Additionally, the statistic pages, data and images can also be transferred to a server (e.g. a shared file server
 *  in the same zone as the application servers) via an FTP server if the network bandwidth between the database
 *  and the application server is too small or should not be overloaded with statistics. This approach has
 *  also the advantage that the statistic pages are available without a database.
 *  The statistic pages and data are stored in the table zeta.page and the optional images in zeta.image.
 *  The statistic pages and images will only be transferred via FTP if the path where them should be placed
 *  is defined by the property "ftp.statistic.path" .
 *  The FTP connection must be specified by the properties "ftp.host", "ftp.user", and "ftp.password" .
 *  The statistic data will only be transferred via FTP if the path where them should be placed
 *  is defined by the property "ftp.data.path" .
 *
 *  Pages, data, and images can only be created by statistic classes.
 *  A statistic class must extend the abstract class <code>zeta.server.handler.statistic.AbstractHandler</code>.
 *  A statistic class can generate one page with many images.
 *
 *  @see zeta.server.util.PageConverter
 *  @see zeta.server.handler.statistic.AbstractHandler
 *  @version 1.9.4, August 27, 2004
**/
public class CreateStatistics extends Thread {
  /**
   *  Starts the process to create statistic pages, data and images if no arguments are specified.
   *  All statistic pages, data and images will be created if no parameters are specified.
   *  Use just one parameter ? to get help.
   *  The first optional parameter specifies the task identifier,
   *  the second optional parameter specifies a class name to create a statistic page and data,
   *  the third is also optional and specifies additionally the image name of the statistic page.
   *  @param args command line arguments.
  **/
  public static void main(String[] args) {
    if (args.length == 0) {
      CreateStatistics csi = new CreateStatistics();
      csi.start();
      return;
    } else if (args.length == 2) {
      CreateStatistics csi = new CreateStatistics();
      csi.getPage(Integer.parseInt(args[0]), args[1]);
      return;
    } else if (args.length == 3) {
      CreateStatistics csi = new CreateStatistics();
      csi.getImage(Integer.parseInt(args[0]), args[1], args[2]);
      return;
    }
    System.out.println("Usage: java " + CreateStatistics.class.getName() + " [<task ID> <class name> [<image name>]]");
  }

  /**
   *  Stores the specified page as ZIP compressed file on disk. The file name is the class name with the extension '.zip'
   *  @param taskId task identifier
   *  @param className class name of the statistic class which has generated the page
  **/
  public void getPage(int taskId, String className) {
    Connection con = null;
    Statement stmt = null;
    try {
      con = Database.getConnection();
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT data,format FROM zeta.data WHERE task_id=" + taskId + " AND class_name='" + className + "' AND type='page'");
      if (rs.next()) {
        StreamUtils.writeData(rs.getBinaryStream(1), new FileOutputStream(className + '.' + taskId + ".page." + rs.getString(2)), false, true);
      } else {
        System.err.println("task ID=" + taskId + " and class name='" + className + "' of type 'page' not found!");
      }
      rs.close();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(con);
    }
  }

  /**
   *  Stores the specified image as PNG file on disk. The file name is the class name with the extension '.png'
   *  @param taskId task identifier
   *  @param className class name of the statistic class which has generated the image
   *  @param imageName image name
  **/
  public void getImage(int taskId, String className, String imageName) {
    Connection con = null;
    Statement stmt = null;
    try {
      con = Database.getConnection();
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT data,format FROM zeta.data WHERE task_id=" + taskId + " AND class_name='" + className + "' AND type='" + imageName + '\'');
      if (rs.next()) {
        StreamUtils.writeData(rs.getBinaryStream(1), new FileOutputStream(className + '.' + taskId + '.' + imageName + '.' + rs.getString(2)), false, true);
      } else {
        System.err.println("task ID=" + taskId + " and class name='" + className + "' of type='" + imageName + "' not found!");
      }
      rs.close();
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      DatabaseUtils.close(con);
    }
  }

  /**
   *  Create statistic pages, data and images in that separately executing thread.
  **/
  public void run() {
    boolean createAllStatistics = true;
    int wait = 60000;
    while (true) {
      try {
        int error = 0;
        do {
          try {
            wait = updateData(createAllStatistics)*60000;
            createAllStatistics = false;
          } catch (SQLException se) {
            if (++error == 2) {
              ThrowableHandler.handle(se);
            }
            sleep(30000);
          } catch (ServletException se) {
            ++error;
            ThrowableHandler.handle(se.getRootCause());
            sleep(30000);
          }
        } while (error == 1);
        System.out.println(new Timestamp(System.currentTimeMillis()).toString() + " next update will be in about " + (wait/60000) + " minute(s)");
        sleep(wait);
      } catch (InterruptedException ie) {
      } catch (Throwable e) {
        ThrowableHandler.handle(e);
        try {
          sleep(30000);
        } catch (InterruptedException ie) {
        }
      }
    }
  }

  /**
   *  Executes all specified statistic classes with types of the table zeta.data to update all pages, data, and images.
   *  @param createAllStatistics all statistics will be updated if <code>true</code> otherwise only the expired statistics
  **/
  public int updateData(boolean createAllStatistics) throws SQLException, ServletException {
    Connection con = null;
    Statement stmt = null;
    int wait = 0;
    try {
      Daemon.setAllowDbRestart(false);
      DispatcherServlet servlet = new DispatcherServlet();
      servlet.init();
      con = Database.getConnection();
      stmt = con.createStatement();
      con.setReadOnly(true);
      String page = null;
      String pageClassName = null;
      Timestamp pageLastUpdate = null;
      // Generate images
      ResultSet rs = stmt.executeQuery("SELECT task_id,class_name,type,format,update_interval,data_path FROM zeta.data"
                                     + ((createAllStatistics)? "" : " WHERE last_update IS NULL OR TIMESTAMPDIFF(4, CAST(CURRENT TIMESTAMP-last_update AS CHAR(22)))>update_interval")
                                     + " ORDER BY task_id,create_order,class_name,type");
      while (rs.next()) {
        int taskId = rs.getInt(1);
        String className = rs.getString(2);
        String type = rs.getString(3);
        String format = rs.getString(4);
        int updateInterval = rs.getInt(5);
        String dataPath = rs.getString(6);
        String fileName = className + '.' + taskId + '.' + type + '.' + format;
        try {
          Class statisticClass = servlet.getHandlerClass(className);
          AbstractHandler statistic = (AbstractHandler)servlet.getHandlerInstance(statisticClass);
          wait = (wait == 0)? updateInterval : gcd(wait, updateInterval);
          byte[] data = null;
          Timestamp lastUpdate = null;
          long startTime = System.currentTimeMillis();
          System.out.print(new Timestamp(startTime).toString() + ' ' + fileName);
          System.out.flush();
          if (type.equals("page")) {
            page = statistic.createPage(taskId, con);
            pageClassName = className;
            data = page.getBytes("ISO-8859-1");
          } else if (type.equals("xml")) {
            lastUpdate = pageLastUpdate;
            if (!className.equals(pageClassName)) {
              page = statistic.createPage(taskId, con);
              pageLastUpdate = null;
              lastUpdate = new Timestamp(startTime);
            }
            data = PageConverter.convertHTMLTableToXML(page, lastUpdate).getBytes("ISO-8859-1");
            page = null;
          } else {
            try {
              data = generateImage(taskId, statistic, con, type);
            } catch (Exception e) {
              ThrowableHandler.handle(e);
            }
          }
          long stopTime = System.currentTimeMillis();
          if (lastUpdate == null) {
            lastUpdate = new Timestamp(stopTime);
            if (page != null && className.equals(pageClassName)) {
              pageLastUpdate = lastUpdate;
            }
          }
          if (data != null) {
            con.setReadOnly(false);
            storeData(con, taskId, className, type, format, lastUpdate, dataPath, fileName, data);
            System.out.print(" (" + ((stopTime-startTime)/1000) + "s)");
          } else {
            System.out.print(" is EMPTY! (" + ((stopTime-startTime)/1000) + "s)");
          }
        } catch (Exception e) {
          ThrowableHandler.handle(e);
        } finally {
          System.out.println();
        }
      }
    } catch (SQLException se) {
      System.out.println(se.getMessage());
      throw se;
    } catch (ServletException se) {
      throw se;
    } catch (Exception e) {
      ThrowableHandler.handle(e);
    } finally {
      if (con != null) {
        try {
          con.setReadOnly(false);
        } catch (SQLException se) {
        }
      }
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(con);
      Daemon.setAllowDbRestart(true);
    }
    return Math.max(1, wait);
  }

  private void storeData(Connection con, int taskId, String className, String type, String format, Timestamp lastUpdate, String dataPath, String fileName, byte[] data) throws IOException, SQLException {
    PreparedStatement pStmt = null;
    try {
      InputStream in = null;
      if (format.equals("zip")) {
        in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
        ZipOutputStream zip = new ZipOutputStream(out);
        zip.setLevel(Deflater.BEST_COMPRESSION);
        zip.putNextEntry(new ZipEntry(className + '.' + taskId + '.' + type));
        StreamUtils.writeData(in, zip, true, true);
        data = out.toByteArray();
      }
      pStmt = con.prepareStatement("UPDATE zeta.data SET (last_update,data)=('" + lastUpdate.toString()
                                 + "',?) WHERE task_id=" + taskId + " AND class_name='" + className + "' AND type='" + type + '\'');
      pStmt.setBytes(1, data);
      pStmt.execute();
      DatabaseUtils.close(pStmt);
      pStmt = null;
      if (dataPath != null && dataPath.length() > 0) {
        FTP ftp = null;
        in = null;
        try {
          Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
          ftp = new FTP();
          ftp.connect(properties.get("ftp.host", ""));
          ftp.login(properties.get("ftp.user", ""), properties.get("ftp.password", ""));
          ftp.cd(dataPath);
          in = new ByteArrayInputStream(data);
          ftp.put(in, fileName + ".$$$", FTP.MODE_BINARY);
          try {
            ftp.deleteFile(fileName);
          } catch (Exception e) {
          }
          ftp.rename(fileName + ".$$$", fileName);
        } catch (Exception e) {
          ThrowableHandler.handle(e);
        } finally {
          StreamUtils.close(in);
          if (ftp != null) {
            ftp.disconnect();
          }
        }
      }
    } finally {
      DatabaseUtils.close(pStmt);
    }
  }

  private byte[] generateImage(int taskId, AbstractHandler statistic, Connection con, String imageName) throws IOException, SQLException, ServletException, com.sun.jimi.core.JimiException {
    BufferedImage image = null;
    try {
      image = statistic.createImage(taskId, con, imageName);
    } catch (NullPointerException npe) {
      throw new SQLException("Reset connection");
    }
    if (image == null) {
      return null;
    }
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    com.sun.jimi.core.encoder.png.PNGEncoder encoder = new com.sun.jimi.core.encoder.png.PNGEncoder();
    encoder.encodeImage(com.sun.jimi.core.Jimi.createRasterImage(image.getSource()), stream);
    stream.close();
    return stream.toByteArray();
  }

  /**
   *  Computes the gcd of the specified parameters.
   *  @return the gcd of the specified parameters.
  **/
  private static int gcd(int a, int b) {
    if (a == 0) return b;
    if (a < 0) a = -a;
    if (b == 0) return a;
    if (b < 0) b = -b;
  
    int i,j;
    for (i = 0; (a&1) == 0; ++i) a >>= 1;
    for (j = 0; (b&1) == 0; ++j) b >>= 1;
    while (a != b)
      if (a > b) {
        a -= b;
        do a >>= 1; while ((a&1) == 0);
      } else {
        b -= a;
        do b >>= 1; while ((b&1) == 0);
      }
    return a << ((i > j)? j : i);
  }
}
