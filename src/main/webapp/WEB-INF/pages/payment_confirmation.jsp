<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
    <title>Unsigned Data Fields</title>
    <link rel="stylesheet" type="text/css" href="../../css/payment.css"/>
</head>
<body>
<form id="payment_confirmation" action="${requestaddress}" method="post">


    <div class="demo">
        <h2>Your order is being processed</h2>
        <img src="../../79.gif" alt="processing" width="100" height="100"/>

    </div>


    <c:forEach var="item" items="${content}">
        <input type="hidden" name="${item.key}" value="${item.value}"/>
    </c:forEach>
    <script>
        document.getElementById("payment_confirmation").submit();
    </script>

    <input type="hidden" id="submit" value="Confirm "/>


</form>
</body>
</html>
