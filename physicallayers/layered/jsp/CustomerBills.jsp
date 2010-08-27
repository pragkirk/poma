<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<html:html locale="true">
<head>
<title>BillPay Application</title>
<html:base/>
</head>
<body bgcolor="white">

<h1>BillPay Application - Customer list</h1>

<h3><bean:write name="customerbills" property="name"/></h3><BR>
<bean:define id="thebills" name="customerbills" property="bills"/>
<logic:iterate id="billitem" name="thebills">
	<html:link page="/billDetail.do" paramId="billId" paramName="billitem" paramProperty="billId">
		<bean:write name="billitem" property="name"/><BR>
	</html:link>
</logic:iterate>

</body>
</html:html>
