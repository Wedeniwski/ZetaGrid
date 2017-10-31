<%@ page session="false"
         import="java.io.IOException,
                 java.util.Date,
                 java.util.Locale,
                 java.text.DateFormat,
                 java.text.SimpleDateFormat,
                 javax.servlet.jsp.JspWriter,
                 zeta.server.DispatcherServlet"
%>
<%
if (DispatcherServlet.getNumberOfHandlers() == 0) { // dummy include to initialize the global parameters
%>
<jsp:forward page="/service/init">
</jsp:forward>
<%
}
String title = request.getParameter("title");
if (title == null) {
  title = "";
}
String rootPath = DispatcherServlet.getRootPath();
int idx = rootPath.indexOf(DispatcherServlet.getServletPath());
if (idx >= 0) {
  rootPath = rootPath.substring(0, idx);
}
String className = request.getParameter("class_name");
%>
<html>
<head>
 <title>ZetaGrid <%=(title.length() == 0)? "" : " - " + title%></title>
 <link rel="stylesheet" type="text/css" href="<%=rootPath%>/zetagrid.css">
</head>
<body bgcolor="#FFFFFF" marginheight="0" marginwidth="0" leftmargin="0" topmargin="0">
 <table border="0" cellspacing="0" cellpadding="0" width="100%">
  <tr><td bgcolor="#336699" width="14%"><img src="<%=rootPath%>/images/zetagrid_logo.jpg" border="0" height="37" width="150"></td>
   <td bgcolor="#336699" width="1%"><img src="<%=rootPath%>/images/odot.gif" border="0" height="37" width="15"></td>
   <td bgcolor="#336699" width="80%"><span class="site-title">&nbsp;&nbsp;<%=title%></span></td>
   <td width="5%" bgcolor="#336699">&nbsp;</td></tr>
  <tr valign="top">
   <td width="14%">
<jsp:include page="menu.jsp">
 <jsp:param name="class_name" value="<%=className%>"/>
</jsp:include>
   </td>
   <td width="1%"><img src="<%=rootPath%>/images/odot.gif" height="2" border="0" width="2"></td>
   <td width="80%">
    <table border="0" cellspacing="0" cellpadding="0" width="100%">
     <tr><td width="90%"><img src="<%=rootPath%>/images/odot.gif" height="2" border="0" width="2"></td>
      <td width="1%"><img src="<%=rootPath%>/images/odot.gif" height="2" border="0" width="15"></td>
     </tr>
<%
  try {
    long timestampOfPage = Long.parseLong(request.getParameter("timestamp_of_page"));
    if (timestampOfPage > 0) {
%>
     <tr valign="bottom"><td colspan="2"><small>Last update:
<%
      DateFormat timeFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.GERMANY);
      out.print(timeFormatter.format(new Date(timestampOfPage)));
%>
      </small><br><img src="<%=rootPath%>/images/odot.gif" height="2" border="0" width="1"></td></tr>
     <tr><td colspan="2" background="<%=rootPath%>/images/back_dots_66f.gif"><img src="<%=rootPath%>/images/odot.gif" height="2" border="0" width="2"></td></tr>
<%
    }
  } catch (Exception e) {
  }
  Class clazz = DispatcherServlet.getHandlerClass(className);
  String url = null;
  if (clazz != null) {
    String path = DispatcherServlet.getHandlerPath(clazz);
    url = DispatcherServlet.getServletPath() + path + "?page=" + path;
    String task = request.getParameter("task");
    if (task != null) {
      url += "&task=" + task;
    }
%>
<jsp:include page="<%=url%>">
</jsp:include>
<%
  } else {
%>
<tr><td><p><img src="<%=rootPath%>/images/zetagrid.jpg" width="500" height="91"></p></td></tr>
<tr><td height="30pt" class="second-head-gray">What is ZetaGrid?</td></tr>
<tr><td>
ZetaGrid is a platform independent grid system that uses idle CPU cycles from participating computers.
Grid computing can be used for any CPU intensive application which can be split into many separate steps and which would require very long computation times on a single computer.
ZetaGrid can be run as a low-priority background process on various platforms like Windows, Linux, AIX, Solaris, HP-UX, and Mac OS X.
On Windows systems it may also be run in screen saver mode.
</td></tr>
<%
  }
%>
    </table></td></tr>
  <tr><td><p>&nbsp;</p></td></tr>
 </table>
</body>
</html>
