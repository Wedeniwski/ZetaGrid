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

package zeta.server.handler.statistic;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import zeta.server.DispatcherServlet;
import zeta.server.ServerTask;
import zeta.server.ZetaConstant;
import zeta.server.handler.GetHandler;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.server.util.PageConverter;
import zeta.util.StreamUtils;

/**
 *  Base handler to response a GET request for a specific statistic.
 *  Statistic pages will not be viewed when the created timestamp is older than 36 hours.
 *
 *  @version 2.0, August 6, 2005
**/
public abstract class AbstractHandler implements GetHandler {
  /**
   *  Creates HTML page with the content of a specified statistic.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of a specified statistic.
  **/
  abstract public String createPage(int taskId, Connection con) throws SQLException, ServletException;

  /**
   *  Creates HTML page with the content of a specified statistic.
   *  @param taskId task identifier
   *  @param  req  GET request
   *  @param  con  connection to the back-end database
   *  @return HTML page with the content of a specified statistic.
  **/
  protected String createPage(int taskId, HttpServletRequest req, Connection con) throws SQLException, ServletException {
    InnerPageBuffer innerPageBuffer = null;
    ByteArrayOutputStream out = null;
    try {
      innerPageBuffer = getInnerPageBuffer(taskId);
      if (innerPageBuffer == null) {
        return "<tr><td>This statistic is not available at moment! Please try again later.</td></tr>";
      } else {
        out = new ByteArrayOutputStream(10*1024);
        StreamUtils.writeData(innerPageBuffer.buffer, out, false, true);
        return out.toString("ISO-8859-1");
      }
    } catch (IOException ioe) {
      throw new ServletException(ioe);
    } finally {
      if (innerPageBuffer != null) {
        innerPageBuffer.close();
      }
    }
  }

  /**
   *  Creates PNG image for a specified name which is defined in the HTML page.
   *  Default is null.
   *  @param taskId task identifier
   *  @param  con  connection to the back-end database
   *  @param  imageName name of the image
   *  @return PNG image for a specified name which is defined in the HTML page.
  **/
  public BufferedImage createImage(int taskId, Connection con, String imageName) throws SQLException, ServletException {
    return null;
  }

  /**
   *  @param servlet  servlet which owns this handler.
  **/
  public AbstractHandler(DispatcherServlet servlet) throws SQLException, ServletException {
    this.servlet = servlet;
    isAllLocalAvailable = false;
    Connection con = null;
    Statement stmt = null;
    try {
      con = servlet.getConnection();
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT task_id,type,format,data_path FROM zeta.data WHERE class_name='" + getClass().getName() + '\'');
      if (rs.next()) {
        isAllLocalAvailable = true;
        do {
          String path = rs.getString(4);
          if (path == null) {
            isAllLocalAvailable = false;
          } else {
            if (path.length() > 0 && path.charAt(path.length()-1) != '/' && path.charAt(path.length()-1) != '\\') {
              path = ZetaConstant.ROOT_PATH + path + '/';
            } else {
              path = ZetaConstant.ROOT_PATH + path;
            }
            Integer taskId = new Integer(rs.getInt(1));
            Map data = (Map)dataFileNames.get(taskId);
            if (data == null) {
              data = new HashMap();
              dataFileNames.put(taskId, data);
            }
            String type = rs.getString(2);
            data.put(type, path + getClass().getName() + '.' + taskId + '.' + type + '.' + rs.getString(3));
          }
        } while (rs.next());
        if (isAllLocalAvailable) {
          Iterator i = servlet.getTaskManager().getServerTasks();
          while (i.hasNext()) {
            if (dataFileNames.get(new Integer(((ServerTask)i.next()).getId())) == null) {
              isAllLocalAvailable = false;
              break;
            }
          }
        }
      }
      rs.close();
    } finally {
      DatabaseUtils.close(stmt);
      DatabaseUtils.close(con);
    }
  }

  /**
   *  Returns the buffer containing the main inner HTML page (table).
   *  The inner page buffer must be closed if it not used.
   *  @param taskId task identifier
   *  @return inner page buffer
  **/
  public InnerPageBuffer getInnerPageBuffer(int taskId) throws ServletException {
    InnerPageBuffer innerPageBuffer = new InnerPageBuffer();
    boolean exists = false;
    Map data = (Map)dataFileNames.get(new Integer(taskId));
    if (data != null) {
      String fileName = (String)data.get("page");
      if (fileName != null) {
        exists = true;
        ZipInputStream zip = null;
        try {
          File fileTmp = new File(fileName + ".$$$");
          File file = new File(fileName);
          if ((file.exists() || fileTmp.exists()) && System.currentTimeMillis()-file.lastModified() < 129600000) {  // 36 hours
            // check if a temporary file with extension ".$$$" exists and wait up to 10 seconds
            for (int i = 0; i < 10 && fileTmp.exists(); ++i) {
              try {
                Thread.sleep(1000);  // wait 1 second
              } catch (InterruptedException ex) {
              }
            }
            zip = new ZipInputStream(new FileInputStream(file));
            if (zip.getNextEntry() != null) {
              timestampOfPage = file.lastModified();
              innerPageBuffer.buffer = new BufferedInputStream(zip);
              innerPageBuffer.resource = new Object[1];
              innerPageBuffer.resource[0] = zip;
              zip = null;
              return innerPageBuffer;
            }
          }
        } catch (Throwable e) {
          innerPageBuffer.close();
        } finally {
          StreamUtils.close(zip);
        }
      }
    }
    if (!exists) {
      Connection con = null;
      Statement stmt = null;
      try {
        con = servlet.getConnection();
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT data,last_update,update_interval FROM zeta.data WHERE task_id=" + taskId
                                       + " AND class_name='" + getClass().getName() + "' AND type='page'");
        if (rs.next()) {
          InputStream in = rs.getBinaryStream(1);
          Timestamp t = rs.getTimestamp(2);
          /*if (in == null && t == null) { // create first time only, i.e. data or last_update is null
            rs.close();
            synchronized (this) {
              rs = stmt.executeQuery("SELECT last_update FROM zeta.data WHERE task_id=" + taskId
                                   + " AND class_name='" + getClass().getName() + "' AND type='page' AND last_update IS NULL");
              boolean createPage = (rs.next());
              rs.close();
              if (createPage) {
                timestampOfPage = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(timestampOfPage);
                String page = createPage(taskId, con);
                byte[] buffer = page.getBytes("ISO-8859-1");
                innerPageBuffer.buffer = new BufferedInputStream(new ByteArrayInputStream(buffer));
                pStmt = con.prepareStatement("UPDATE zeta.data SET (last_update,data)=('" + timestamp.toString()
                                           + "',?) WHERE task_id=" + taskId + " AND class_name='" + getClass().getName() + "' AND type=?");
                pStmt.setBytes(1, buffer);
                pStmt.setString(2, "page");
                pStmt.execute();
                rs = stmt.executeQuery("SELECT last_update FROM zeta.data WHERE task_id=" + taskId
                                     + " AND class_name='" + getClass().getName() + "' AND type='xml'");
                createPage = (rs.next());
                rs.close();
                if (createPage) {
                  pStmt.setBytes(1, PageConverter.convertHTMLTableToXML(page, timestamp).getBytes("ISO-8859-1"));
                  pStmt.setString(2, "xml");
                  pStmt.execute();
                }
                return innerPageBuffer;
              }
            }
          } else*/ if (in != null && t != null && System.currentTimeMillis()-t.getTime() < 4*rs.getInt(3)+129600000) { // 4 times of update interval plus 36 hours
            ZipInputStream zip = new ZipInputStream(in);
            if (zip.getNextEntry() != null) {
              timestampOfPage = (t == null)? System.currentTimeMillis() : t.getTime();
              innerPageBuffer.buffer = new BufferedInputStream(zip);
              innerPageBuffer.resource = new Object[3];
              innerPageBuffer.resource[0] = rs;
              innerPageBuffer.resource[1] = stmt;
              innerPageBuffer.resource[2] = con;
              stmt = null;
              con = null;
              return innerPageBuffer;
            }
          } else {
            rs.close();
          }
        } else {
          rs.close();
        }
      } catch (Throwable e) {
        innerPageBuffer.close();
      } finally {
        DatabaseUtils.close(stmt);
        DatabaseUtils.close(con);
      }
    }   
    return null;
  }

  /**
   *  Handles a GET request <code>resp</code> for the specified statistic.
   *  The response <code>resp</code> contains either the HTML page or an image.
  **/
  final public void doGet(ServerTask task, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String imageName = req.getParameter("image");
    if (imageName != null) {
      if (task == null) {
        throw new ServletException("Task undefined!");
      }
      // send image
      ByteArrayOutputStream stream = new ByteArrayOutputStream(32*1024);
      boolean localFileDefined = false;
      Map data = (Map)dataFileNames.get(new Integer(task.getId()));
      if (data != null) {
        String fileName = (String)data.get(imageName);
        if (fileName != null) {
          localFileDefined = true;
          FileInputStream in = null;
          try {
            File fileTmp = new File(fileName + ".$$$");
            File file = new File(fileName);
            if ((fileTmp.exists() || file.exists()) && System.currentTimeMillis()-file.lastModified() < 129600000) {  // 36 hours
              // check if a temporary file with extension ".$$$" exists and wait up to 10 seconds
              for (int i = 0; i < 10 && fileTmp.exists(); ++i) {
                try {
                  Thread.sleep(1000);  // wait 1 second
                } catch (InterruptedException ex) {
                }
              }
              in = new FileInputStream(file);
              stream.reset();
              StreamUtils.writeData(in, stream, false, true);
            }
          } catch (Exception e) {
          } finally {
            StreamUtils.close(in);
          }
        }
      }
      if (!localFileDefined) {
        Connection con = null;
        Statement stmt = null;
        try {
          con = servlet.getConnection();
          stmt = con.createStatement();
          ResultSet rs = stmt.executeQuery("SELECT data,last_update,update_interval FROM zeta.data WHERE task_id=" + task.getId()
                                         + " AND class_name='" + getClass().getName() + "' AND type='" + imageName + '\'');
          if (rs.next()) {
            InputStream in = rs.getBinaryStream(1);
            Timestamp t = rs.getTimestamp(2);
            /*if (in == null && t == null) { // create first time only, i.e. data or last_update is null
              rs.close();
              synchronized (this) {
                rs = stmt.executeQuery("SELECT last_update FROM zeta.data WHERE task_id=" + task.getId()
                                     + " AND class_name='" + getClass().getName() + "' AND type='" + imageName + "' AND last_update IS NULL");
                boolean createPage = (rs.next());
                rs.close();
                if (createPage) {
                  BufferedImage image = createImage(task.getId(), con, imageName);
                  if (image == null) {
                    throw new ServletException("No image available!");
                  }
                  stream.reset();
                  // ToDo: only PNG format is supported for images
                  com.sun.jimi.core.encoder.png.PNGEncoder encoder = new com.sun.jimi.core.encoder.png.PNGEncoder();
                  encoder.encodeImage(com.sun.jimi.core.Jimi.createRasterImage(image.getSource()), stream);
                  stream.close();
                  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                  pStmt = con.prepareStatement("UPDATE zeta.data SET (last_update,data)=('" + timestamp.toString()
                                             + "',?) WHERE task_id=" + task.getId()
                                             + " AND class_name='" + getClass().getName() + "' AND type='" + imageName + '\'');
                  pStmt.setBytes(1, stream.toByteArray());
                  pStmt.execute();
                }
              }
            } else*/ if (in != null && t != null && System.currentTimeMillis()-t.getTime() < 4*rs.getInt(3)+129600000) { // 4 times of update interval plus 36 hours
              stream.reset();
              StreamUtils.writeData(in, stream, false, true);
              rs.close();
            }
          }
        } catch (SQLException e) {
          throw new ServletException(e);
        } finally {
          DatabaseUtils.close(stmt);
          DatabaseUtils.close(con);
        }
      }
      resp.setContentType("image/png");
      resp.setContentLength(stream.size());
      resp.setHeader("Cache-Control", "max-age=3600"); //1 hour
      stream.writeTo(resp.getOutputStream());
      //resp.getWriter().print(stream.toString(resp.getCharacterEncoding()));
    } else /*if (req.getParameter("page") != null)*/ {
      // send page
      try {
        StringBuffer buffer = new StringBuffer(100*1024);
        buffer.append("<tr><td><br><form type=\"GET\" action=\"");
        String servletPath = servlet.getRootPath() + servlet.getHandlerPath(getClass());
        buffer.append(servletPath);
        buffer.append("\">");
        Map parameters = req.getParameterMap();
        Iterator iter = parameters.keySet().iterator();
        while (iter.hasNext()) {
          String param = (String)iter.next();
          if (!param.equals("task")) {
            buffer.append("<input name=\"");
            buffer.append(param);
            buffer.append("\" type=\"hidden\" value=\"");
            buffer.append(parameters.get(param));
            buffer.append("\">");
          }
        }

        buffer.append("Task name: <select name=\"task\">");
        iter = servlet.getTaskManager().getServerTasks();
        while (iter.hasNext()) {
          ServerTask serverTask = (ServerTask)iter.next();
          if (task == null) {
            task = serverTask;
          }
          buffer.append("<option value=\"");
          buffer.append(serverTask.getName());
          buffer.append((serverTask.getId() == task.getId())? "\" selected>" : "\">");
          buffer.append(serverTask.getName());
        }
        buffer.append("</select>&nbsp;&nbsp;<input type=\"submit\" value=\"Select\"></form></td></tr><tr><td><small>&nbsp;</small></td></tr>");
        if (isAllLocalAvailable) {
          buffer.append(createPage((task == null)? 0 : task.getId(), req, null));
        } else {
          Connection con = null;
          String dbError = "The back-end database server is down.";
          try {
            con = servlet.getConnection();
            dbError = null;
            buffer.append(createPage((task == null)? 0 : task.getId(), req, con));
          } catch (SQLException se) {
            dbError = se.getMessage();
            servlet.log(se);
          } catch (ServletException se) {
            dbError = se.getMessage();
            servlet.log(se);
          } finally {
            DatabaseUtils.close(con);
          }
          if (dbError != null) {
            buffer.append("<tr><td>This statistic is not available at moment (");
            buffer.append(dbError);
            buffer.append(")!<p>Please try again later.</p></td></tr>");
          }
        }
        resp.setContentType("text/html");
        resp.setContentLength(buffer.length());
        resp.setHeader("Cache-Control", "max-age=600"); //10 minutes
        resp.getWriter().print(buffer.toString());
      } catch (ServletException e) {
        throw e;
      } catch (Exception e) {
servlet.log(e);
        throw new ServletException(e);
      }
    }
  }

  /**
   *  Timestamp in millis of the last update of the data of the page.
  **/
  public long getTimestampOfPage() {
    return timestampOfPage;
  }

  /**
   *  Servlet which owns this handler.
  **/
  protected DispatcherServlet servlet;

  /**
   *  Local file name (incl. path) of the page and the images if they are not stored in the database.
   *  The keys are the task identifiers as Integer. The values are maps of the data
   *  where the keys are the data type as String and the values are the data file name also as String.
  **/
  private Map dataFileNames = new HashMap();

  /**
   *  Timestamp when the inner page was generated.
  **/
  private long timestampOfPage = 0;

  /**
   *  Is <code>true</code> if the path of the page and the images are defined,
   *  i.e. the page and the images are locally available and must not be retrieved from the database.
  **/
  private boolean isAllLocalAvailable;

  class InnerPageBuffer {
    /**
     *  Returns <code>true</code> if and only if the stream contains the specified string <code>search</code>.
     *  Sets the position of the stream where the stream contains the specified string <code>search</code>.
     *  End of the stream will be set if the stream does not contain the specified string <code>search</code>.
     *  @param search string which is searched
     *  @param ignoreCase if <code>true</code>, ignore case when comparing characters.
     *  @return <code>true</code> if and only if the stream contains the specified string <code>search</code>.
    **/
    public boolean skip(String search, boolean ignoreCase) {
      try {
        return StreamUtils.skip(buffer, search, ignoreCase);
      } catch (IOException ioe) {
        return false;
      }
    }

    /**
     *  Returns lines from the input stream where all lines contains the specified string <code>search</code>.
     *  All these lines must be continuously inside the stream.
     *  @param in   input stream
     *  @param search starting string which is searched
     *  @param ignoreCase if <code>true</code>, ignore case when comparing characters.
     *  @return lines from the input stream where all lines contains the specified string <code>search</code>
     *          or <code>null</code> if the specified string is not defined in the stream.
     *  @exception  IOException  if an I/O error occurs.
    **/
    public String[] getLines(String search, boolean ignoreCase) {
      try {
        return StreamUtils.getLines(buffer, search, ignoreCase);
      } catch (IOException ioe) {
        return null;
      }
    }

    /**
     *  Returns the string of the stream which is between the specified strings <code>left</code> and <code>right</code>.
     *  The resulting string does not include the specified strings <code>left</code> and <code>right</code>.
     *  The position of the stream is after the specified ending string <code>right</code>.
     *  End of the stream will be set if the stream does not contain one of the specified strings.
     *  @param search starting string which is searched
     *  @param search ending string which is searched
     *  @param ignoreCase if <code>true</code>, ignore case when comparing characters.
     *  @return the string of the stream which is between the specified strings <code>left</code> and <code>right</code>
     *          or <code>null</code> if the specified strings are not defined in the stream.
    **/
    public String between(String left, String right, boolean ignoreCase) {
      try {
        return StreamUtils.between(buffer, left, right, ignoreCase);
      } catch (IOException ioe) {
        return null;
      }
    }

    /**
     *  Closes all resources.
    **/
    void close() throws ServletException {
      if (buffer != null) {
        StreamUtils.close(buffer);
        buffer = null;
      }
      if (resource != null) {
        for (int i = 0; i < resource.length; ++i) {
          if (resource[i] instanceof InputStream) {
            StreamUtils.close((InputStream)resource[i]);
          } else if (resource[i] instanceof ResultSet) {
            try {
              ((ResultSet)resource[i]).close();
            } catch (SQLException e) {
            }
          } else if (resource[i] instanceof Statement) {
            DatabaseUtils.close((Statement)resource[i]);
          } else if (resource[i] instanceof Connection) {
            DatabaseUtils.close((Connection)resource[i]);
          } else {
            throw new ServletException("Unknow resource!");
          }
        }
        resource = null;
      }
    }

    BufferedInputStream buffer = null;
    Object[] resource = null;
  }
}
