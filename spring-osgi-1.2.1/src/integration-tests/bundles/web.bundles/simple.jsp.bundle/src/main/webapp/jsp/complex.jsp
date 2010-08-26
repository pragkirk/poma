<%@page contentType="text/html" %>

<html>

<body>

<%!
int cnt=0;
private int getCount(){
//increment cnt and return the value
cnt++;
return cnt;
}
%>

<p>Values of Cnt are:</p>

<p><%=getCount()%></p>

<p><%=getCount()%></p>

<p><%=getCount()%></p>

<p><%=getCount()%></p>

<p><%=getCount()%></p>

<p><%=getCount()%></p>

</body>

</html>