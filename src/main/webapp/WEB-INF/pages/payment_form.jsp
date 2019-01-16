<%@ page contentType="text/html;charset=UTF-8" %>


<html>
<head>
    <title>Signed Data Fields</title>
    <link rel="stylesheet" type="text/css" href="../../css/payment.css"/>
    <script type="text/javascript" src="../../js/jquery-1.7.min.js"></script>
</head>
<body>
<form id="payment_form" action="cybersource.do" method="post">

    <fieldset>
        <legend>Signed Data Fields</legend>
        These fields will be POSTed to your server for signing using the
        Security file included in the <br> sample script. The name of
        each signed field should be included in the signed_field_names.<BR/>
        <div id="paymentDetailsSection" class="section">
            <span>reference_number:</span><input type="text"
                                                 name="reference_number" size="25"><br/>
            <span>transaction_type:</span><input
                type="text" name="transaction_type" size="25"><br/> <span>amount:</span><input
                type="text" name="amount" size="25"><br/> <span>currency:</span><input
                type="text" name="currency" size="25"><br/> <span>payment_method:</span><input
                type="text" name="payment_method"><br/> <span>bill_to_forename:</span><input
                type="text" name="bill_to_forename"><br/> <span>bill_to_surname:</span><input
                type="text" name="bill_to_surname"><br/> <span>bill_to_email:</span><input
                type="text" name="bill_to_email"><br/> <span>bill_to_phone:</span><input
                type="text" name="bill_to_phone"><br/> <span>bill_to_address_line1:</span><input
                type="text" name="bill_to_address_line1"><br/> <span>bill_to_address_city:</span><input
                type="text" name="bill_to_address_city"><br/> <span>bill_to_address_state:</span><input
                type="text" name="bill_to_address_state"><br/> <span>bill_to_address_country:</span><input
                type="text" name="bill_to_address_country"><br/> <span>bill_to_address_postal_code:</span><input
                type="text" name="bill_to_address_postal_code"><br/> <span>card_type:</span><input
                type="text" name="card_type"><br/> <span>card_number:</span><input
                type="text" name="card_number"><br/> <span>card_expiry_date:</span><input
                type="text" name="card_expiry_date"><br/> <span>card_cvn:</span><input
                type="password" name="card_cvn"><br/>
        </div>
    </fieldset>
    <input type="submit" id="submit" name="submit" value="Submit"/>
    <script type="text/javascript" src="../../js/payment_form.js"></script>
</form>
</body>
</html>
