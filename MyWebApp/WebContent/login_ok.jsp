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
<title>Login ok</title>
</head>
	<body>
	<div align="center">
		<h1>Accesso effettuato correttamente!</h1>
		<p>Bentornato/a, ${username}
		<br>
		<fieldset>
		<legend>Signin complete</legend>
		<br><img src="data:image/jpg;base64,${fetchImage}" width="250" height="250" />
		<br>
		<br>
		<br>
		<br>
		<br>
		<br><b>Queste sono le opzioni a tua disposizione:</b>
		&nbsp;&nbsp;&nbsp;<p><a href="GetAllProjects">Mostra tutti i progetti presenti</a>
		<p><a href="upload_file.jsp">Inserisci un nuovo progetto</a>
		<p>
		<br><a href="GetUserLogout">Esci dal sistema ed esegui il logout!</a>
		</p>
		</fieldset>
		</div>
	</body>
</html>