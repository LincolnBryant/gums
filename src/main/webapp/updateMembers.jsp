<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>GUMS</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Update VO Members</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<% 
	try {
		gums.updateGroups();
		out.println("VO Members Database has been successfully updated!");
	} catch(Exception e) {
		out.println("<div class=\"failure\">Error updating groups: " + e.getMessage() + "</div>");
	}
%>
</p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
