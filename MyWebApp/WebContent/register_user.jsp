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
<meta http-equiv="Content-Type" content="text/html">
<title>Registrazione utente</title>
</style>
</head>
<body>
<div align="center">
<form method="post" action="RegisterUser" enctype = "multipart/form-data">
<h1> Registrazione nuovo utente</h1>
<br>
<p><a href="login.jsp">Sei già registrato? Accedi</a></p>
<fieldset>
<legend>Signup</legend>
<table>
	<tbody>
		<tr>
			<td>e-Mail*&nbsp;&nbsp;&nbsp;</td>
			<td><input id="username" name="username" type="email" title="Inserire un e-Mail nome@dominio.com" placeholder="Inserire la e-Mail.." required></td>
		</tr>
		<tr>
			<td>Password*&nbsp;&nbsp;&nbsp;</td>
			<td><input id="password" name="password" type="password" maxlength="30" size="30" placeholder="Inserire la password.." required></td>
		</tr>
		<tr>
			<td>Conferma password*&nbsp;&nbsp;&nbsp;</td>
			<td><input id="password_check" name="password_check" type="password" maxlength="30" size="30" placeholder="Inserire ancora la password.." required></td>
		</tr>		
		<tr>
			<td>Nome&nbsp;&nbsp;&nbsp;</td>
			<td><input id="nome" name="nome" type="text" pattern="[A-Za-z']{1,25}" title="Non sono ammessi caratteri speciali" placeholder="Inserire il nome.." ></td>
		</tr>
		<tr>
			<td>Cognome&nbsp;&nbsp;&nbsp;</td>
			<td><input id="cognome" name="cognome" type="text" pattern="[A-Za-z']{1,25}" title="Non sono ammessi caratteri speciali" placeholder="Inserire il cognome.."></td>
		</tr>
		<tr>
			<td>Foto profilo* (PNG, JPEG)&nbsp;&nbsp;&nbsp;</td>
			<td><input id="immagine" name="photo" type="file" accept="image/png, image/jpeg" required></td>
		</tr>
		<tr>
		<td><b><small>Max 5MB</small></b></td>
		<td><b><small>I campi contrassegnati da * sono obbligatori</small></b></td>
		</tr>
		<tr>
		</tr>
		<tr>
		</tr>
		<tr>
		</tr>
		<tr>
		</tr>
		<tr>
		</tr>
		<tr>
		</tr>
		<tr>
		<td></td>
			<td><input type="submit" value="Registrati"></td>
		</tr>
	</tbody>
	<tr>
</table>
</fieldset>
</form>
</div>
</body>
</html>