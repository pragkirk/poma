<%@ page info="an OSGi JSP page with taglibs" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" href="styles/springsource.css" type="text/css" />
	<title>Hello OSGi World</title>
</head>

<body>
<div id="main_wrapper">
<h1>JSP taglibs page</h1>

<p>This JSP page uses the standard JSTL <a href="http://java.sun.com/jsp/jstl/core">core</a> taglib to display 
the information about the user request. Since the WAR is deployed as an OSGi bundle, the taglib does not have to 
be nested inside the WAR and can be deployed, as a stand alone bundle which is then imported.

<h2>Browser Information</h2>
<c:out value="${header['User-Agent']}"/>

<h2>Header info:</h2>

 <table>
    <tr>
    	<th>Name</th>
    	<th>Value</th>
    </tr>
	<c:forEach var="head" items="${headerValues}">
	  <tr>
	  <td><c:out value="${head.key}"/></td>
	  <c:forEach var="val" items="${head.value}">
	  <td>
	     <c:out value="${val}"/>
	  </td>
	  </c:forEach>
	  </tr>
	</c:forEach>
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