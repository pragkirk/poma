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

<h3>BillPay Application - Customer Search</h3>
<html:form styleId="searchform" action="/customerSearch">
Enter ID: <html:text name="customerSearchForm" property="customerId" size="2" maxlength="2"/>
<html:submit/>
</html:form>
</body>
</html:html>
