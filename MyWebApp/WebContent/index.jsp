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
<title>Homepage SNA 21-22</title>
</style>
</head>
<body>
	<div align="center">
	<form method="post" action="login.jsp">
		<h1>Sicurezza nelle applicazioni 2021-2022</h1>
		<h2>Studente: GABRIELE PATTA #756410</h2>
		<br>
		<br>
		<br>
		<fieldset>
  		<legend>Entry point</legend>
		<h3>Benvenuto in questa applicazione</h3>
		<br>
		<p><b>Sei un utente registrato?</b></p>
		<input type="submit" value="Accedi">
		</fieldset>
	</form>
		<form method="post" action="register_user.jsp">
		<fieldset>
		<p><b>Se non possiedi un account registrati subito!</b></p>
		<input type="submit" value="Registrati">
			<br>
			<br>
			<br>
		</fieldset>
		</form>
	</div>
</body>
</html>