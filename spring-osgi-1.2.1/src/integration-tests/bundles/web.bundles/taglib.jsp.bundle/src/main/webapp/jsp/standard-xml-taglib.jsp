<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL Support for XML</title>
</head>
<body bgcolor="#FFFFCC">
<h3>Portfolio</h3>

<c:import url="stocks.xml" var="xmldoc"/>
<x:parse xml="${xmldoc}" var="output"/>

<p>

<table border="2" width="50%">
 <tr>
 <th>Stock Symbol</th>
 <th>Company Name</th>
 <th>Price</th>
 </tr>
 <tr>

<x:forEach select="$output/portfolio/stock" var="item">
  <td><x:out select="symbol"/></td>
  <td><x:out select="name"/></td>
  <td><x:out select="price"/></td></tr>
</x:forEach>
</table>

</body>
</html>