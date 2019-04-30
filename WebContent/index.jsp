<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>index</title>
<link rel="stylesheet" href="./themes/basique/style.css">
</head>
<body>
<h1>ENI - Ench�res</h1>
	<hr id="header">
	<aside class="menu">
	<%if (){}
		
		%>
		<p><a href="<%=request.getContextPath()%>/deconnexion">D�connexion</a></p><br>
	</aside>

	<h2>Liste des ench�res</h2>
	<p>Filtres :</p>
	<form class="recherche" action="filtre" method="post">
		<div class="nomArticleContient">
			<label for="contient">Le nom de l'article contient</label>
			<input class="champtexte" type="text" id="contient" name="contient" value="" />
		</div>
		<div class ="categorie">
		<label for="categorie">Cat�gorie</label>
		<select name="categorie">
			<option></option>
			<option>Informatique</option>
			<option>Ameublement</option>
			<option>V�tement</option>
			<option>Sport&Loisirs</option>
		</select>
		</div>
		<input type="submit" id="rechercher" value="rechercher" />
	</form>
</body>
</html>