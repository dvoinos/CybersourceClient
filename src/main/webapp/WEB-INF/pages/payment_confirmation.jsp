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

    <fieldset id="confirmation">
        <legend>Signed Data Fields</legend>
        These fields have been signed on your server, and a signature has been generated. This will <br> detect
        tampering with these values as they pass through the consumers browser to the SASOP endpoint.<BR>
        <div>---------------------------------------------------------------------------------------</div>
        <div>


            <c:forEach var="item" items="${contextUser}">
                <div>
                    <span class="fieldName">${item.key}:</span><span class="fieldValue">${item.value}</span>
                </div>
            </c:forEach>
        </div>
    </fieldset>

    <c:forEach  var="item" items="${content}" >
        <input type="hidden" name="${item.key}" value="${item.value}"/></c:forEach>

    <legend>Your Card Information</legend>

    <div>---------------------------------------------------------------------------------------</div>
    <span>card_type:</span><input type="text" name="card_type"><br/> <span>card_number:</span><input
        type="text" name="card_number"><br/> <span>card_expiry_date:</span><input
        type="text" name="card_expiry_date"><br/> <span>card_cvn:</span><input
        type="password" name="card_cvn"><br/>
    <input type="submit" id="submit" value="Confirm "/>
    <script type="text/javascript" src="../../js/jquery-1.7.min.js"></script>
    <script type="text/javascript" src="../../js/payment_form.js"></script>

</form>
</body>
</html>
