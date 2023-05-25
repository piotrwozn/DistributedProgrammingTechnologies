<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Books</title>
</head>
<body>
<h1>Books</h1>
<ul>
    <c:forEach var="book" items="${books}">
        <li>
            <h2>${book.title}</h2>
            <p>${book.author}</p>
            <p>${book.publication_year}</p>
            <p>${book.pages}</p>
            <p>${book.publisher}</p>
        </li>
    </c:forEach>
</ul>
</body>
</html>
