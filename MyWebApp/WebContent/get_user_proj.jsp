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
<meta charset="ISO-8859-1">
<title>Proposta utente</title>
</head>
<body>
<div align="center">
<h1> La proposta caricata sul server</h1>
<br>
<br>
<fieldset>
<legend>Your proposal</legend>
<br>
${projUser}
<br>
<br>
<br>
<p><a href="/MyWebApp">Ritorna alla homepage</a>
</fieldset>
</div>
</body>
</html>