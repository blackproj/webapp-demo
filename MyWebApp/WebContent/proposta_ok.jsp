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
<title>Proposta ok</title>
</style>
</head>
<body>
	<div align="center">
		<h1>La proposta progettuale è stata caricata correttamente!</h1>
		<h2>${username}</h2>
		<br>
		<form action="index.jsp">
 		<fieldset>
  		<legend>Proposal ok</legend>
		<p><a href="/MyWebApp">Torna alla homepage!</a>
		<br>
		<br><a href="GetUserLogout">Esci dal sistema ed esegui il logout!</a>
		</fieldset>	
		</form>
		</div>
</body>
</html>