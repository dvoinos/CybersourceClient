<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Cybersource response</title>
</head>
<body>

<fieldset id="confirmation">
    <legend>Response From Cybersource</legend>
    <div>
        <c:forEach var="item" items="${responseMap}">
            <div>
                <span class="fieldName">${item.key}:</span><span class="fieldValue">${item.value}</span>
            </div>
        </c:forEach>
    </div>
</fieldset>

<form id="capture_form" action="processcapture" method="post">
    <input type="submit" id="capture" name="action1" value="CAPTURE"/>
</form>
<form id="auth_revarsal" action="cancel_auth" method="post">
    <input type="submit" id="cancel" name="action2" value="CANCEL"/>
</form>
</body>
</html>
