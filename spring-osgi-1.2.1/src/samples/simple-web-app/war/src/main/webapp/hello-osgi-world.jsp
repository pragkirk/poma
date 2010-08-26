<%@ page info="a hello world example" %>
<%@ page import="java.util.*,java.text.*"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" href="styles/springsource.css" type="text/css" />
	<title>Hello OSGi World</title>
</head>

<body>
<div id="main_wrapper">
<h1>Hello <%=request.getRemoteAddr()%>, from OSGi World!</h1>
<p>This JSP page (if properly compiled), should display various information about the current HTTP session.

  <h2>HTTP Session info</h2>
  
  <% 
  	Format formatter = DateFormat.getDateTimeInstance();
  %>
  
  <table>
    <tr><td>Id: </td><td><%=session.getId()%></td></tr>
    <tr><td>Creation Date:</td><td><%=formatter.format(new Date(session.getCreationTime()))%></td></tr>
    <tr><td>MaxInactiveInterval:</td><td><%=session.getMaxInactiveInterval()%> sec </td></tr>
  </table>
  <h3>Session Attributes</h3>
  <table>
      <tr><th>Name</th><th>Value</th></tr>
      
      <%
         for (Enumeration attributeNames = session.getAttributeNames(); attributeNames.hasMoreElements();) {
			  String attributeName = (String) attributeNames.nextElement();
			  Object attributeValue = session.getAttribute(attributeName);
      %>
      <tr><td><%=attributeName%></td><td><%=attributeValue%></td></tr>
      
      <%
         }
      %>
  </table>
  
  <table class="footer">
    <tr>
      <td><a href="./">Home</a></td>
      <td align="right"><img src="./images/springsource-logo.png"/></td>
	</tr>
  </table>

</div>
</body>
</html>