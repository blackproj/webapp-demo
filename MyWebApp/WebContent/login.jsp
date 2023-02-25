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
<title>Login</title>
</head>
<body>
	<div align="center">
	<h1> Accedi al sistema della web-app</h1>
	<br>
	<fieldset>
	<legend>Login form</legend>
	<p>Sei un nuovo utente?</p><p><a href="register_user.jsp">Crea un nuovo account</a></p>
	<form method="post" action="GetUserLogin">
		<table>
			<tbody>
				<tr>
			<tr>
				<td></td>
				<td><input id="username" name="username" type="email" placeholder="e-Mail" required></td>
			</tr>
			<tr>
				<td></td>
			<td><input id="password" name="passphrase" type="password" placeholder="Password" required></td>
			</tr>
			<tr>
			</tr>
		</table>
		<input type="checkbox" name="rememberme" value="true"> Ricordami su questo computer
		<br>
		<br><input type="submit" value="Login">
	</form>
	</fieldset>
	</div>
</body>
</html>