<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>updateMembers Form</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>GUMS User Management</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Updates the list of members of each group by connecting to the different VO servers.
</p>

<form action="updateMembers.jsp" method="get">
  <table id="form" border="0" cellpadding="2" cellspacing="2">
    <tbody>
      <tr>
        <td>
        <div style="text-align: center;"><button type="submit">Update VO Members DataBase</button></div>
        </td>
      </tr>
    </tbody>
  </table>
</form>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
