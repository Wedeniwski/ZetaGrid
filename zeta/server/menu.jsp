<%@ page session="false"
         import="zeta.server.DispatcherServlet"
%>
<%
String rootPath = DispatcherServlet.getRootPath();
int idx = rootPath.indexOf(DispatcherServlet.getServletPath());
if (idx >= 0) {
  rootPath = rootPath.substring(0, idx);
}
String className = request.getParameter("class_name");
%>
<table border="0" cellspacing="0" cellpadding="0" width="165" bgcolor="#336699">
 <tr><td><img src="<%=rootPath%>/images/odot.gif" border="0" height="10" width="10"></td>
  <td><img src="<%=rootPath%>/images/odot.gif" border="0" height="10" width="10"></td>
  <td><img src="<%=rootPath%>/images/odot.gif" border="0" height="10" width="135"></td>
  <td><img src="<%=rootPath%>/images/odot.gif" border="0" height="10" width="10"></td></tr>
 <tr><td></td><td colspan="2" height="21"><span class="nav"><a href="<%=rootPath%>/index.jsp">ZetaGrid</a></span></td><td>&nbsp;</td></tr>
<%
String prevStartPath = "";
String selectedPath = DispatcherServlet.getHandlerPath(DispatcherServlet.getHandlerClass(className));
if (selectedPath != null) {
  int i = selectedPath.indexOf('/', 1);
  if (i > 0) {
    selectedPath = selectedPath.substring(0, i);
  }
}
for (int i = 0, l = DispatcherServlet.getNumberOfHandlers(); i < l; ++i) {
  Class c = DispatcherServlet.getHandlerClass(i);
  String startPath = DispatcherServlet.getHandlerPath(c);
  int j = startPath.indexOf('/', 1);
  if (j > 0) {
    startPath = Character.toUpperCase(startPath.charAt(0)) + startPath.substring(1, j);
  }
  String name = c.getName();
  if (name.equals(className)) {
    if (startPath.length() > 0 && startPath.equals(prevStartPath)) {
%>
 <tr><td colspan="2" bgcolor="#FFFFFF"></td><td height="21" bgcolor="#FFFFFF"><span class="nav">
<%
      if (selectedPath == null || !startPath.startsWith(selectedPath)) {
        while (i+1 < l && DispatcherServlet.getHandlerPath(DispatcherServlet.getHandlerClass(i+1)).startsWith(startPath)) {
          ++i;
        }
      }
    } else {
%>
 <tr><td bgcolor="#FFFFFF"></td><td colspan="2" height="21" bgcolor="#FFFFFF"><span class="nav">
<%
    }
    out.print(DispatcherServlet.getHandlerDisplayName(c));
%>
  </span></td><td bgcolor="#FFFFFF"></td></tr>
<%
  } else {
    if (startPath.length() == 0 || !startPath.equals(prevStartPath)) {
%>
 <tr><td><img src="<%=rootPath%>/images/odot.gif" border="0" height="1" width="1"></td>
  <td colspan="2" bgcolor="#CCCCFF"><img src="<%=rootPath%>/images/odot.gif" border="0" height="1" width="1"></td>
  <td><img src="<%=rootPath%>/images/odot.gif" border="0" height="1" width="1"></td></tr>
 <tr><td></td><td colspan="2" height="21">
<%
      if (selectedPath == null || !startPath.startsWith(selectedPath)) {
        while (i+1 < l && DispatcherServlet.getHandlerPath(DispatcherServlet.getHandlerClass(i+1)).startsWith(startPath)) {
          ++i;
        }
      }
    } else {
%>
 <tr><td colspan="2"><img src="<%=rootPath%>/images/odot.gif" border="0" height="1" width="1"></td>
  <td bgcolor="#CCCCFF"><img src="<%=rootPath%>/images/odot.gif" border="0" height="1" width="1"></td>
  <td><img src="<%=rootPath%>/images/odot.gif" border="0" height="1" width="1"></td></tr>
 <tr><td colspan="2"></td><td colspan="1" height="21">
<%
    }
%>
   <span class="nav"><a href="<%=DispatcherServlet.getRootPath()+DispatcherServlet.getHandlerPath(c)%>"><%=DispatcherServlet.getHandlerDisplayName(c)%></a></span></td>
  <td></td></tr>
<%
  }
  prevStartPath = startPath;
}
%>
 <tr><td colspan="4"><img src="<%=rootPath%>/images/odot.gif" width="1" height="5"></td></tr>
</table>
