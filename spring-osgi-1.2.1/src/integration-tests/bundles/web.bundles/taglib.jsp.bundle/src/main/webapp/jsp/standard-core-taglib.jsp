<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<title>Core Taglib Example</title>
</head>
<body>

<h2>c:out/c:set</h2>
<c:set var="browser" value="${header['User-Agent']}"/>
<c:out value="${browser}"/>
<br/>
OR simply just:
<c:out value="${header['User-Agent']}"/>

<h2>c:forEach</h2>
<h3>Header info:</h3>

<c:forEach var="head" items="${headerValues}">
  param: <c:out value="${head.key}"/><br>
  values:
   <c:forEach var="val" items="${head.value}">
     <c:out value="${val}"/>
   </c:forEach>
   <p>
</c:forEach>

</body>
</html>