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
<title>Caricamento file</title>
</style>
</head>
<body>
<div align="center">
<h1> File upload proposta progettuale</h1>
	<strong>${username}</strong>
	<br>
	<br>
	<form method="post" action="FileUpload" enctype = "multipart/form-data">
	<fieldset>
  	<legend>Choose file</legend>
	  	<br>
		<br>Scegli il file (Formato file accettato SOLO *txt):
		<br><input type = "file" name = "user_file" accept=".txt" required/>
   		<br><b><small>Max 7MB</small></b>
		<br>
		<br>
		<br>
		<br>
		<input type = "submit" value = "Carica file" />
		<br>
		<br>
		<br>
		<br>
		<br>
		<br><a href="GetUserLogout">Esci dal sistema ed esegui il logout!</a>
	 </fieldset>
	</form>
<br>
</div>
<br>
<br>
<br>
<br>
<br>
</body>
</html>