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
     H. Haddorp
     S. Wedeniwski
--*/

package zeta.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import javax.sql.DataSource;

import zeta.crypto.Decrypter;
import zeta.crypto.KeyManager;
import zeta.server.handler.GetHandler;
import zeta.server.handler.PostHandler;
import zeta.server.handler.database.ConnectionDataSource;
import zeta.server.handler.statistic.AbstractHandler;
import zeta.server.processor.TaskRequestWorkUnitProcessor;
import zeta.server.processor.TaskResultProcessor;
import zeta.server.util.DatabaseUtils;
import zeta.server.util.Parameter;
import zeta.util.Properties;
import zeta.util.StreamUtils;

/**
 *  Creates an HTTP servlet that dispatches HTTP GET and HTTP POST requests on the ZetaGrid site
 *  to specified handlers.
 *
 *  @version 2.0, August 6, 2005
**/
public class DispatcherServlet extends HttpServlet {

  /**
   *  Initialize lists to handle HTTP GET and HTTP POST requests.
  **/
  public DispatcherServlet() {
  }

  private Hashtable parameters = new Hashtable(100);
  public String getInitParameter(String name) {
    String result = null;
    try {
      result = super.getInitParameter(name);
    } catch (NullPointerException e) {
    }
    if (result == null) {
      synchronized (parameters) {
        File file = new File(ZetaConstant.INIT_PARAMETER_PATH + name);
        if (file.exists()) {
          Long lastModified = new Long(file.lastModified());
          Object[] obj = (Object[])parameters.get(name);
          if (obj == null) {
            obj = new Object[2];
            parameters.put(name, obj);
          }
          if (!lastModified.equals(obj[0])) {
            obj[0] = lastModified;
            try {
              obj[1] = new String(StreamUtils.getFile(file.getAbsolutePath(), false, false));
            } catch (IOException ioe) {
              obj[1] = null;
            }
          }
          result = (String)obj[1];
        }
        if (result == null) {
          try {
            Properties properties = new Properties(Properties.ZETA_TOOLS_CFG);
            result = properties.get(name);
            Object[] obj = new Object[2];
            obj[0] = new Long(System.currentTimeMillis());
            obj[1] = result;
            parameters.put(name, obj);
          } catch (Exception e) {
          }
        }
      }
    }
    return result;
  }

  public Enumeration getInitParameterNames() {
    if (hasSeparateFiles()) {
      File file = new File(ZetaConstant.INIT_PARAMETER_PATH + '.');
      String[] list = file.list();
      if (list != null) {
        for (int i = 0; i < list.length; ++i) {
          parameters.put(list[i], new Object[2]);
        }
      }
      return parameters.keys();
    } else {
      return super.getInitParameterNames();
    }
  }

  public int getInitParameter(String name, int defaultValue) {
    String value = getInitParameter(name);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException nfe) {
      }
    }
    return defaultValue;
  }

  public boolean hasSeparateFiles() {
    return (parameters.size() > 0);
  }

  /**
   *  Initialize the resources that are held for the life of the servlet.
   *  Setup the database connection driver and the configured handlers for HTTP GET and HTTP POST requests.
  **/
  public void init() throws ServletException {
    try {
      // init DB
      if (getInitParameter("database.connection.driver") != null) {
        try {
          dataSource = new ConnectionDataSource(getInitParameter("database.connection.driver"),
                                                getInitParameter("database.connection.url"),
                                                getInitParameter("database.connection.username"),
                                                getInitParameter("database.connection.password"),
                                                getInitParameter("database.connection.total.connections", 50),
                                                getInitParameter("database.connection.timeout", 1000),
                                                getInitParameter("database.connection.idle.timeout", 2000),
                                                getInitParameter("database.connection.aged.timeout", 12*3600));
        } catch (Throwable t) {
          throw new ServletException(t);
        }
      } else {
        try {
          InitialContext ctx = new InitialContext();
          dataSource = (DataSource)ctx.lookup("java:comp/env/jdbc/ZetaGridDB");
        } catch (NamingException ne) {
          throw new ServletException(ne);
        }
      }
      // generate logs
      Connection con = null;
      try {
        con = getConnection();
        server = Server.getInstance(con);
        initLogFile();
        taskManager = TaskManager.getInstance(con);
      } catch (Exception e) {
        throw new ServletException(e);
      } finally {
        DatabaseUtils.close(con);
      }
      servletPath = getInitParameter("servlet.path");
      rootPath = getInitParameter("root.path");
      // init configured handlers
      Runtime runtime = Runtime.getRuntime();
      log("\n\n\n===========\nInit config, free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      if (rootPath != null && servletPath != null) {
        log("Init parameters root.path=" + rootPath + " and servlet.path=" + servletPath);
      } else {
        log("The init parameters root.path and servlet.path are undefined.");
        rootPath = null;
      }
      requestTypeGetHandlers.clear();
      requestTypePostHandlers.clear();
      con  = null;
      Statement stmt = null;
      try {
        con = getConnection();
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT class_name,type,display_name,servlet_name FROM zeta.handler ORDER BY view_order");
        while (rs.next()) {
          String servletName = rs.getString(4);
          addHandler(rs.getString(2), rs.getString(1), rs.getString(3), servletName);
          log("Add handler " + servletName + ", free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
        }
        rs.close();
        log("" + requestTypeGetHandlers.size() + " handlers were initialized, free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      } catch (Throwable t) {
        throw new ServletException(t);
      }
    } catch (ServletException se) {
      log(se);
      throw se;
    }
  }

  /**
   *  Destroy the resources that are held for the life of the servlet.
  **/
  public void destroy() {
    super.destroy();
    requestTypeGetHandlers.clear();
    requestTypePostHandlers.clear();
  }

  /**
   *  Returns a connection to the back-end database. This connection must be closed.
   *  @return a connection to the back-end database.
  **/
  public Connection getConnection() throws SQLException {
    if (dataSource instanceof ConnectionDataSource) {
      String url = getInitParameter("database.connection.url");
      synchronized (DispatcherServlet.class) {
        if (url != null && !url.equals(((ConnectionDataSource)dataSource).getURL())) {
          ((ConnectionDataSource)dataSource).setURL(url);
        }
      }
    }
    return dataSource.getConnection();
  }

  /**
   *  Returns the active server.
   *  @return the active server.
  **/
  public Server getServer() {
    return server;
  }

  /**
   *  Returns the server task manager.
   *  @return the server task manager.
  **/
  public TaskManager getTaskManager() throws ServletException {
    if (taskManager == null) {
      Connection con = null;
      try {
        con = getConnection();
        taskManager = TaskManager.getInstance(con);
      } catch (Exception e) {
        throw new ServletException(e);
      } finally {
        DatabaseUtils.close(con);
      }
    }
    return taskManager;
  }

  /**
   *  Returns the context and servlet path of all dispatched servlets.
   *  @return the context and servlet path of all dispatched servlets.
  **/
  public static String getRootPath() {
    return rootPath;
  }

  /**
   *  Returns the servlet path of all dispatched servlets.
   *  @return the servlet path of all dispatched servlets.
  **/
  public static String getServletPath() {
    return servletPath;
  }

  /**
   *  Returns the number of defined handlers with a defined display name.
   *  @return the number of defined handlers with a defined display name.
  **/
  public static int getNumberOfHandlers() {
    int i = 0;
    for (int j = 0, l = requestTypeGetHandlers.size(); j < l; ++j) {
      Object[] obj = (Object[])requestTypeGetHandlers.get(j);
      if (obj[1] != null) {
        ++i;
      }
    }
    return i;
  }

  /**
   *  Returns the i-th defined handler with a defined display name.
   *  @param i  index
   *  @return the i-th defined handler with a defined display name.
  **/
  public static Class getHandlerClass(int i) {
    int idx = -1;
    for (int j = 0, l = requestTypeGetHandlers.size(); j < l; ++j) {
      Object[] obj = (Object[])requestTypeGetHandlers.get(j);
      if (obj[1] != null && ++idx == i) {
        return obj[2].getClass();
      }
    }
    return null;
  }

  /**
   *  Searches the handler class by the specified name.
   *  @param  className  name of a handler
   *  @return class of the handler; null if no handler is found.
  **/
  public static Class getHandlerClass(String className) {
    if (className != null) {
      Iterator iter = requestTypeGetHandlers.iterator();
      while (iter.hasNext()) {
        Object[] obj = (Object[])iter.next();
        if (className.equals(obj[2].getClass().getName())) {
          return obj[2].getClass();
        }
      }
    }
    return null;
  }

  /**
   *  Searches the display name of the specified handler class.
   *  @param  handler  class of a handler
   *  @return name of the handler; null if no handler is found.
  **/
  public static String getHandlerDisplayName(Class handler) {
    Iterator iter = requestTypeGetHandlers.iterator();
    while (iter.hasNext()) {
      Object[] obj = (Object[])iter.next();
      if (handler == obj[2].getClass()) {
        return (String)obj[1];
      }
    }
    return null;
  }

  /**
   *  Searches the handler object by the specified handler class.
   *  @param  handler  class of a handler
   *  @return object of the specified handler; <code>null</code> if no handler is found.
  **/
  public static Object getHandlerInstance(Class handler) {
    Iterator iter = requestTypeGetHandlers.iterator();
    while (iter.hasNext()) {
      Object[] obj = (Object[])iter.next();
      if (handler == obj[2].getClass()) {
        return obj[2];
      }
    }
    return null;
  }

  /**
   *  Searches the handler name by the specified class of this GET handler.
   *  @param  handler  class of a statistic handler
   *  @return name of the handler; null if no handler is found.
  **/
  public static String getHandlerPath(Class handler) {
    Iterator iter = requestTypeGetHandlers.iterator();
    while (iter.hasNext()) {
      Object[] obj = (Object[])iter.next();
      if (handler == obj[2].getClass()) {
        return (String)obj[0];
      }
    }
    return null;
  }

  /**
   *  Called by the server to allow this servlet to handle a GET request.
   *  The request will be forwarded to the index JSP if the handler has a display name and the parameter 'page' is unspecified.
   *  @param  req  contains the request the client has made of the servlet.
   *  @param  resp contains the response the servlet sends to the client.
   *  @exception  IOException  if an I/O error occurs when the servlet handles the GET request.
   *  @exception  javax.servlet.ServletException if the request for the GET could not be handled.
   *  @see javax.servlet.http.HttpServlet#doGet
  **/
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String page = req.getPathInfo();
    if (rootPath == null || rootPath.length() == 0) {
      if (page == null) {
        return;
      }
      servletPath = req.getServletPath();
      rootPath = req.getContextPath() + servletPath;
      if (page.equals("/init")) {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
        if (dispatcher != null) {
          try {
            dispatcher.forward(req, resp);
            return;
          } catch (IOException ioe) {
            log(ioe);
          }
        }
      }
    }
    if (page == null) {
      page = req.getParameter("page");
      if (page == null) {
        return;
      }
    }
    Runtime runtime = Runtime.getRuntime();
    int localId = ++id;
    try {
      GetHandler handler = (GetHandler)findHandler(requestTypeGetHandlers, page);
      if (handler != null) {
        boolean forwarded = false;
        log("Start " + localId + ':' + req.getPathInfo() + ' ' + req.getRemoteHost() + " (" + req.getRemoteAddr() + ") free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
        ServerTask task = null;
        Exception taskException = null;
        try {
          task = getServerTask(req.getParameterMap());
        } catch (IllegalArgumentException e) {
          taskException = e;
        }
        String displayName = (String)getHandlerDisplayName(handler.getClass());
        if (displayName != null && req.getParameter("page") == null && req.getParameter("image") == null) {
          // send page with layout
          if (task == null) { // default task
            Iterator i = getTaskManager().getServerTasks();
            if (i.hasNext()) {
              task = new ServerTask((ServerTask)i.next(), req.getParameterMap());
            }
          }
          String title = displayName;
          RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp?class_name=" + handler.getClass().getName()
                                         + "&timestamp_of_page=" + handler.getTimestampOfPage() + "&title=" + title
                                         + ((task == null)? "" : "&task=" + task.getName()));
          if (dispatcher != null) {
            try {
              dispatcher.forward(req, resp);
              forwarded = true;
            } catch (IOException ioe) {
              log(ioe);
            }
          }
        }
        if (!forwarded) {
          if (taskException != null && task == null) {
            log("Exception name: " + handler.getClass().getName() + " query: " + req.getQueryString());
            log(taskException);
          }
          handler.doGet(task, req, resp);
        }
        log("Stop " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      }
    } catch (SocketException se) {
      log("Exception " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      throw se;
    } catch (IOException ioe) {
      log("Exception " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      log(ioe);
      throw ioe;
    } catch (ServletException se) {
      log("Exception " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      log(se);
      throw se;
    } catch (Throwable t) {
      log("Exception " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      log(t);
      throw new ServletException(t);
    }
  }

  /**
   *  Called by the server to allow this servlet to handle a POST request.
   *  @param  req  contains the request the client has made of the servlet.
   *  @param  resp contains the response the servlet sends to the client.
   *  @exception  IOException  if an I/O error occurs when the servlet handles the POST request.
   *  @exception  javax.servlet.ServletException if the request for the POST could not be handled.
   *  @see javax.servlet.http.HttpServlet#doPost
  **/
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String page = req.getPathInfo();
    if (rootPath == null || rootPath.length() == 0) {
      if (page == null) {
        return;
      }
      servletPath = req.getServletPath();
      rootPath = req.getContextPath() + servletPath;
    }
    if (page == null) {
      return;
    }
    int localId = ++id;
    Runtime runtime = Runtime.getRuntime();
    try {
      PostHandler handler = (PostHandler)findHandler(requestTypePostHandlers, page);
      if (handler != null) {
        log("Start " + localId + ':' + req.getPathInfo() + ' ' + req.getRemoteHost() + " (" + req.getRemoteAddr() + ") free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
        ServerTask task = null;
        try {
          String paramString = req.getHeader("Param-String"); // not a standard header !!
          task = getServerTask((paramString == null || paramString.length() == 0)? null : HttpUtils.parseQueryString(paramString));
        } catch (IllegalArgumentException iae) {
          log("Param-String is invalid: " + req.getHeader("Param-String"));
        }
        handler.doPost(task, req, resp);
        log("Stop " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      }
    } catch (IOException ioe) {
      log("Exception " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      log(ioe);
      throw ioe;
    } catch (ServletException se) {
      log("Exception " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      log(se);
      throw se;
    } catch (Throwable t) {
      log("Exception " + localId + " free:" + runtime.freeMemory() + " total=" + runtime.totalMemory() + " max=" + runtime.maxMemory());
      log(t);
      throw new ServletException(t);
    }
  }

  /**
   *  Log the specified message text.
   *  @parm msg text which have to be logged
  **/
  public void log(String msg) {
    if (normalLogging) {
      try {
        super.log(msg);
      } catch (NullPointerException npe) {
        System.out.println(msg);
      }
    } else {
      synchronized (stdLogFileName) {
        initLogFile();
        if (stdLog != null) {
          try {
            stdLog.write(logFormat.format(new Date()) + ' ' + msg + '\n');
          } catch (Exception e) {
            super.log(msg);
          }
        }
      }
    }
  }

  /**
   *  Log the specified throwable.
   *  @parm t throwable which have to be logged
  **/
  public void log(Throwable t) {
    if (t instanceof ServletException) {
      Throwable rootCause = ((ServletException)t).getRootCause();
      if (rootCause != null) {
        t = rootCause;
      }
    }
    if (normalLogging) {
      try {
        super.log(t.getMessage(), t);
      } catch (NullPointerException npe) {
        t.printStackTrace();
      }
    } else {
      synchronized (stdLogFileName) {
        initLogFile();
        if (errorLog != null) {
          try {
            errorLog.write(logFormat.format(new Date())+'\n');
            t.printStackTrace(new PrintWriter(errorLog));
            errorLog.write("\n");
            errorLog.flush();
          } catch (Exception e) {
          }
        }
      }
    }
  }

  private void initLogFile() {
    normalLogging = false;
    long maxLogFileSize = 5*1024*1024;
    if (server != null) {
      maxLogFileSize = server.getMaxLogFileSize();
    }
    if (stdLogFileName != null && new File(stdLogFileName).length() >= maxLogFileSize) {
      StreamUtils.close(stdLog);
      stdLog = null;
      StreamUtils.close(errorLog);
      errorLog = null;
    }
    FileWriter fout = null;
    try {
      if (errorLogFileName == null || stdLogFileName == null || errorLogFileName.length() == 0 || stdLogFileName.length() == 0) {
        String loggingPath = (server == null)? null : server.getLoggingPath();
        if (loggingPath != null && loggingPath.length() > 0) {
          stdLogFileName = loggingPath + "servlet.log";
          errorLogFileName = loggingPath + "error_servlet.log";
        } else {
          stdLogFileName = "servlet.log";
          errorLogFileName = "error_servlet.log";
        }
        fout = new FileWriter(loggingPath + "init.log", true);
        fout.write(logFormat.format(new Date())+'\n');
      }
      if (server != null && new File(stdLogFileName).length() >= maxLogFileSize) {
        server.fileRollOver(errorLogFileName);
        server.fileRollOver(stdLogFileName);
      }
      if (errorLog == null && errorLogFileName != null && errorLogFileName.length() > 0) {
        errorLog = new FileWriter(errorLogFileName, true);
      }
      if (stdLog == null && stdLogFileName != null && stdLogFileName.length() > 0) {
        stdLog = new FileWriter(stdLogFileName, true);
      }
    } catch (IOException e) {
      normalLogging = true;
    } finally {
      StreamUtils.close(fout);
    }
  }

  private void addHandler(String requestType, String className, String displayName, String servletName) throws Exception {
    servletName = "/" + servletName;
    if (requestType.equalsIgnoreCase("GET")) {
      Class clazz = Class.forName(className);
      String handlerPath = getHandlerPath(clazz);
      if (handlerPath != null && !handlerPath.equals(servletName)) {
        log("Error: The handler " + className + " is already defined for the path " + handlerPath + " and cannot be defined also for the path " + servletPath);
      } else {
        requestTypeGetHandlers.add(new Object[] { servletName, displayName, getInstance(clazz) });
      }
    } else if (requestType.equalsIgnoreCase("POST")) {
      Class clazz = Class.forName(className);
      String handlerPath = getHandlerPath(clazz);
      if (handlerPath != null && !handlerPath.equals(servletName)) {
        log("Error: The handler " + className + " is already defined for the path " + handlerPath + " and cannot be defined also for the path " + servletPath);
      } else {
        requestTypePostHandlers.add(new Object[] { servletName, displayName, getInstance(clazz) });
      }
    } else {
      log("The request type '" + requestType + "' is unknown!");
    }
  }

  /**
   *  Searches the handler by the specified name.
   *  @param  handlers  container with the handler
   *  @param  name  name of the handler which is searched
   *  @return instance of the handler which name starts with the specified name; null if no handler is found.
  **/
  private static Object findHandler(List handlers, String name) {
    if (name != null) {
      Iterator iter = handlers.iterator();
      while (iter.hasNext()) {
        Object[] obj = (Object[])iter.next();
        if (name.equals((String)obj[0])) {
          return obj[2];
        }
      }
    }
    return null;
  }

  /**
   *  Builds a new instance of the specified handler class 
   *  @param  handlerClass  class of the handler
  **/
  private Object getInstance(Class handlerClass) throws Exception, Error {
    try {
      Constructor c = handlerClass.getDeclaredConstructor(new Class[] { DispatcherServlet.class });
      return c.newInstance(new Object[] { this });
    } catch (NoSuchMethodException e) {
      return handlerClass.newInstance();
    }
  }

  /**
   *  Returns the task object of a specified task name.
   *  @param servlet surrounding servlet
   *  @param parameters parameters of the HttpRequest; the parameters must contains the task name at the key 'task'
   *  @return the task object of a specified task ID if it is defined, otherwise <code>null</code>.
  **/
  private ServerTask getServerTask(Map parameter) throws ServletException {
    if (parameter == null) {
      throw new IllegalArgumentException("The parameters of the HttpRequest must be specified!");
    }
    // check if URL is encrypted
    String[] values = (String[])parameter.get("param");
    if (values != null && values.length > 0 && values[0] != null && values[0].length() > 0) {
      try {
        Decrypter decrypter = new Decrypter(KeyManager.getEncryptorKey(null));
        String s = decrypter.decryptURLFile(values[0], ServerTask.getDefaultDecryptionNumber(this));
        if (s != null) {
          parameter = HttpUtils.parseQueryString(s);
        }
      } catch (IOException ioe) {
      } catch (SQLException se) {
      }
    }
    values = (String[])parameter.get("task");
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("No task is defined in the HttpRequest");
    }
    String taskName = values[0];
    if (taskName == null || taskName.length() == 0) {
      throw new IllegalArgumentException("No valid task is defined in the HttpRequest");
    }
    ServerTask task = getTaskManager().getServerTask(taskName);
    return (task == null)? null : new ServerTask(task, parameter);
  }

  /**
   *  Map to handle HTTP GET requests.
  **/
  private static List requestTypeGetHandlers = new ArrayList(20);

  /**
   *  Map to handle HTTP POST requests.
  **/
  private static List requestTypePostHandlers = new ArrayList(5);

  /**
   *  Servlet path of all dispatched servlets.
  **/
  private static String servletPath = "";

  /**
   *  Context and servlet path of all dispatched servlets.
  **/
  private static String rootPath = "";

  /**
   *  The active server.
  **/
  private Server server = null;

  /**
   *  ID for logging.
  **/
  private int id = 0;

  private DataSource dataSource = null;

  private TaskManager taskManager = null;

  private String stdLogFileName = "";
  private Writer stdLog = null;
  private String errorLogFileName = "";
  private Writer errorLog = null;
  private SimpleDateFormat logFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.GERMANY);
  private boolean normalLogging = true;
}
