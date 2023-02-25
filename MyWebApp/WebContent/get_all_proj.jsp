<%@page import="it.uniba.cybersec.model.Project"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<style>
fieldset {
  background-color: #eeeeee;
}

legend {
  background-color: gray;
  color: white;
  padding: 5px 10px;
}

input {
  margin: 5px;
}
<meta charset="ISO-8859-1">
<title>Lista dei progetti</title>
</style>
</head>
<body>
<div align="center">
<h1> Progetti inseriti sul server - ${username}</h1>
<br>
	<%
		@SuppressWarnings("unchecked")
		List<Project> allProj = (List<Project>)session.getAttribute("allProj");
	%>
	<p><a href="/MyWebApp">Homepage</a>   <a href="upload_file.jsp">Aggiungi una nuova proposta</a></p>
	<br>
	<fieldset>
	<legend>Projects online</legend>
	<table>
		<thead>
		<tr>
			<td>ID Utente	</td><td>Descrizione	</td><td>Link</td>
		</tr>
		</thead>
			<tbody>					
			<%
				for(int i = 0; i < allProj.size(); ++i) {
					Project pj = allProj.get(i);
			%>
				<tr>
				<td> <%= pj.getId_usr() %> </td>
				<td> Proj. user no: <%= + pj.getId_usr() %></td>
				<td><a href="GetUserProj?id_usr=<%=pj.getId_usr()%>">Apri proposta</a></td>				
			<% 
				}
			%>
				</tr>
			</tbody>
	</table>
</fieldset>
</div>
</body>
</html>