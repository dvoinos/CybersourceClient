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

    <form id="payment_form" action="processForm" method="post">
    <input type="submit" id="submit" name="action1" value="Action1"/>

    </form>
</body>
</html>
