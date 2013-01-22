<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="perso.logement.core.Annonce" %>
<%@ page import="perso.logement.PMF" %>

<html>
  <head>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
  </head>
  <body>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null && "cyrilmontagnon@gmail.com".equals(user.getEmail())) {
%>
<p>Hello, <%= user.getNickname() %>! (You can
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>

<table id="table-3">
	<thead>
		<th><b>Date</b></th>
		<th><b>Arrondissement</b></th>
		<th><b>Quartier</b></th>
		<th><b>Prix</b></th>
		<th><b>Superficie</b></th>
		<th><b>Annonce</b></th>
		<th><b>Reference</b></th>
	</thead>
	<tbody>
<%
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String query = "select from " + Annonce.class.getName() + " order by date";
    List<Annonce> annonces = (List<Annonce>) pm.newQuery(query).execute();
    for (Annonce annonce : annonces) {
%>    
<tr>
<td><%= annonce.getDate()%></td>
<td><%= annonce.getArrondissement()%></td>
<td><%= annonce.getQuartier()%></td>
<td><%= annonce.getSuperficie()%></td>
<td><%= annonce.getText()%></td>
<td><%= annonce.getReference()%></td>
</tr>
<%
	
    }
    pm.close();
%>
	</tbody>
</table>

<%
    } else {
%>
<p>Hello!
<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
to include your name with greetings you post.</p>
<%
    }
%>
  </body>
</html>