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

<h3>Bill Detail</h3>
Bill: <bean:write name="bill" property="name"/><BR>
Amount: <bean:write name="bill" property="amount"/><BR>
Status: <bean:write name="bill" property="status"/><BR>
<logic:present name="bill" property="auditedAmount">
	Audited Amount: <bean:write name="bill" property="auditedAmount"/><BR>
</logic:present>
<logic:present name="bill" property="paidAmount">
	Paid Amount: <bean:write name="bill" property="paidAmount"/><BR>
</logic:present>
<BR><BR>
<html:link page="/audit.do" paramId="billId" paramName="bill" paramProperty="billId">Audit</html:link><BR>
<html:link page="/pay.do" paramId="billId" paramName="bill" paramProperty="billId">Pay</html:link>
</body>
</html:html>
