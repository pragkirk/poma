<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>


  <h1>
  Spring DM OSGi Web Console
  </h1>

  <h2>Introduction</h2>
	This application demonstrates the use of Spring-MVC annotations 
	inside OSGi through Spring Dynamic Modules.
	The web application acts as an OSGi console which allows you to
	interact with the OSGi environment. 
 
  <h2>OSGi web console</h2>
  
  <p>Select one of the installed bundles (listed below ) to find out information about 
	 it (such as the relevant OSGi headers, used services, imported and exported packages).
	 Additionally, resources can be searched in one of the bundle <i>spaces</i>. 
  </p>
  
  <h3>Select Bundle</h3>
	  <form:form modelAttribute="selection">
       <table>
	      <tr>
		      <td>Available bundles:</td>
		      <td>
		      <form:select path="bundleId" items="${bundles}"/>
		      </td>
		      </tr>
			  <tr>
		      <td>Display bundles as:</td>
		      <td><form:select path="displayChoice" items="${displayOptions}"/></td>
		  	  </tr>
		  	  <tr>
	          <td colspan="2">
    			  <p class="submit"><input align="center" type="submit" value="Select Bundle"/></p>
              </td>
          </tr>
  	  </table>
  	  </form:form>
  <br/>
 
  <h3>Bundle Synopsis</h3>
  
  <h4>Bundle Info</h4>
  Headers
  <table id="properties">
    <tr><th>Name</th><th>Value</th></tr>
	<c:forEach var="prop" items="${bundleInfo.properties}">
	  <tr>
	    <td><c:out value="${prop.key}"/></td>
	    <td><c:out value="${prop.value}"/></td>
	  </tr>
	</c:forEach>
  </table>
  <h4>Status</h4>
  <table>
	<tr><th>State</th><td>${bundleInfo.state}</td></tr>
	<tr><th>LastModified</th><td><fmt:formatDate value="${bundleInfo.lastModified}" pattern="HH:mm:ss z 'on' yyyy.MM.dd"/></td></tr>
	<tr><th>Location</th><td>${bundleInfo.location}</td></tr>
  </table>
  
  <p/>
  
  <h4>Bundle Wiring</h4>
  <table>
  	<tr>
  		<td>Exported Packages</td>
  		<td>
  		  <c:forEach var="package" items="${bundleInfo.exportedPackages}">
  		  	<c:out value="${package}"/><br/>
  		  </c:forEach>
  		</td>
  	</tr>
  	<tr>
  		<td>Imported Packages<br/> 
  		(incl. the bound versions) </td>
  		<td>
  		  <c:forEach var="package" items="${bundleInfo.importedPackages}">
  		  	<c:out value="${package}"/><br/>
  		  </c:forEach>
  		</td>
  	</tr>
  </table>
  
  <h4>Services</h4>
  
  <h5>Services Registered</h5>
  <table>
  	<tr>
  	   <th>Properties</th><th>Using Bundles</th>
  	</tr>
    <c:forEach var="service" items="${bundleInfo.registeredServices}">
  	<tr>
  	  <td>
 	  <c:forEach var="prop" items="${service.properties}">
 	    <i><c:out value="${prop.key}"/></i> : 
	    <c:out value="${prop.value}"/><br/>
  	  </c:forEach>
  	  </td>
      <td>
  	  <c:forEach var="bundle" items="${service.usingBundles}">
  	  	<c:out value="${bundle}"/><br/>
  	  </c:forEach>
  	  </td>
  	</tr>
  	</c:forEach>
  </table>
  
  <h5>Services In Use</h5>
  <table>
   	<tr>
  	   <th>Properties</th><th>Owning Bundle</th>
  	</tr>
    <c:forEach var="service" items="${bundleInfo.servicesInUse}">
  	<tr>
  	  <td>
 	  <c:forEach var="prop" items="${service.properties}">
	    <i><c:out value="${prop.key}"/></i> : 
	    <c:out value="${prop.value}"/><br/>
  	  </c:forEach>
      </td>
      <td>
  	    <c:out value="${service.bundle}"/>
  	  </td>
  	</tr>
  	</c:forEach>
  </table>

  <a name="search"></a>
  <h3>Bundle Search</h3>

  <form:form modelAttribute="selection" action="#search">
    <table>
     <tr>
      <td width="80%">Search Space: </td>
      <td width="20%"><form:select path="searchChoice" items="${searchChoices}"/></td>
     </tr>
     <tr>
      <td>Pattern:<br/>
        <form:errors path="*" cssClass="errors"/>
      </td>
      <td><form:input path="searchPattern"/></td>
  	 </tr>
  	 <tr>
      <td colspan="2">
  		<p class="submit"><input align="center" type="submit" value="Search Bundle"/></p>
      </td>
     </tr>
	</table>
  </form:form>

  <c:set var="searchEntries" value="${fn:length(searchResult)}" />
  
  <h4>Search Results ( <c:out value="${searchEntries}"/>  
      <c:choose>
        <c:when test='${searchEntries == 1}'>
            entry
        </c:when>
        <c:otherwise>
            entries
       </c:otherwise>
      </c:choose>)
  </h4>
  <table>
    <tr><td>
    <c:forEach var="result" items="${searchResult}">
  	    <c:out value="${result}"/><br/>
  	</c:forEach>
	</td></tr>  	
  </table>
  
  <h2>Requirements</h2> 
  
  This sample requires:
  <ul>
    <li>an OSGi 4.0+ platform</li>
    <li>Spring DM 1.1 + dependencies</li>
    <li>Apache Tomcat 5.5.x+</li>
    <li>Apache Jasper 2 Engine </li>
  </ul>
  
  Note that all the dependencies are automatically downloaded when running the sample.
  
<%@ include file="/WEB-INF/jsp/footer.jsp" %>