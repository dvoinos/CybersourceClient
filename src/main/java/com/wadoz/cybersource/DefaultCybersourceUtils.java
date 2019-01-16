package com.wadoz.cybersource;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


@Component
public class DefaultCybersourceUtils implements CybersourceUtils {


    public String getSessionId() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();

        return session.getId();
    }

    private Object getSessionAttribute(String key) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();

        return session.getAttribute(key);
    }

    private boolean removeSessionAttribute(String key) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        session.removeAttribute(key);

        if (session.getAttribute(key) == null)
            return true;
        else return false;
    }

    @Override
    public Map<String, String> collectDataAndSign(final String returnURL, final Properties props, Map<String, String> signedData) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {

        signedData.put("signature", sign(buildDataToSign(signedData), props.getProperty("cybersource.sign.secretkey"), props.getProperty("cybersource.sign.algorithm")));

        return signedData;
    }

    private String sign(String data, String secretKey, String encodingAlgo) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), encodingAlgo);
        Mac mac = Mac.getInstance(encodingAlgo);
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));
        return DatatypeConverter.printBase64Binary(rawHmac).replace("\n", "");
    }

    private String buildDataToSign(Map<String, String> params) {
        String[] signedFieldNames = String.valueOf(params.get("signed_field_names")).split(",");
        ArrayList<String> dataToSign = new ArrayList<String>();
        for (String signedFieldName : signedFieldNames) {
            dataToSign.add(signedFieldName + "=" + String.valueOf(params.get(signedFieldName)));
        }
        return commaSeparate(dataToSign);
    }

    private String commaSeparate(ArrayList<String> dataToSign) {
        StringBuilder csv = new StringBuilder();
        for (Iterator<String> it = dataToSign.iterator(); it.hasNext(); ) {
            csv.append(it.next());
            if (it.hasNext()) {
                csv.append(",");
            }
        }
        return csv.toString();
    }

    /*@Override
    public String collectDataAndSign(final String returnURL, final String clientIp, final Properties props) {

        *//*if (cartModel == null) {
            throw new IllegalArgumentException("Cart Model is null");
        }*//*

        Map<String, String> signedData = getRequestMap();
        *//*if (MapUtils.isNotEmpty(this.request)) {
            signedData.putAll(request);
        }*//*

        //cartModel.getCode()
        String merchantReferenceCode = String.valueOf(cartidnumber+=1);
       *//* cartModel.setMerchantReferenceCode(merchantReferenceCode);
        modelService.save(cartModel);*//*

        // Secure Acceptance Profile ID (obtained from the CyberSource EBC)
        String profileID = props.getProperty("cybersource.profileId");
        // Secure Acceptance Access Key (obtained from the CyberSource EBC)
        String accessKey = props.getProperty("cybersource.accessKey");
        // Current time stamp
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String transactionUUID = profileID + "-" + merchantReferenceCode + "-"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // Add core data to map

       *//* Double totalPrice = 45.94;
        if (totalPrice != null) {
            signedData.put("amount", totalPrice.toString());
        }

        CurrencyModel currency = cartModel.getCurrency();
        if (currency != null) {
            signedData.put("currency", currency.getIsocode());
        }*//*

     *//*  DeliveryModeModel deliveryMode = cartModel.getDeliveryMode();
        if (deliveryMode != null && StringUtils.isNotEmpty(deliveryMode.getCode())) {
            signedData.put("merchant_defined_data8", deliveryMode.getCode());
        }*//*

        signedData.put("access_key", accessKey);
        signedData.put("profile_id", profileID);
        signedData.put("transaction_uuid", transactionUUID);
        signedData.put("signed_date_time", simpleDateFormat.format(new Date()));
        signedData.put("payment_method", "card");

        //from cybersource documentation the locale HAS to be "en"
        //signedData.put("locale", CybersourceConstants.TOKENIZATION_LOCALE);
        signedData.put("reference_number", merchantReferenceCode);
        signedData.put("device_fingerprint_id", getSessionId());

        if (StringUtils.isNotEmpty(clientIp)) {
            signedData.put("customer_ip_address", clientIp);
        }

       *//* UserModel userModel = userService.getCurrentUser();
        if (userModel != null) {

            String uid = userModel.getUid();
            if (StringUtils.isNotEmpty(uid)) {
                signedData.put("merchant_defined_data1", uid);
                signedData.put("bill_to_email", uid);
            }

            String differenceDate = getDifferenceDate(userModel.getCreationtime());
            if (StringUtils.isNotEmpty(differenceDate)) {
                signedData.put("merchant_defined_data2", differenceDate);
            }

            // Reply URL (Override)
        }*//*

        //signedData.put("merchant_defined_data22", CybersourceConstants.CYBERSOURCE_PAYMENT_METHOD);

        // Add cookies accepted info to map
        if (getSessionAttribute("customerCookiesAccepted") != null) {
            signedData.put("customer_cookies_accepted", (String)getSessionAttribute("customerCookiesAccepted"));
            removeSessionAttribute("customerCookiesAccepted");
        }

        int index = 0;
       *//* List<AbstractOrderEntryModel> orderEntryList = cartModel.getEntries();
        if (CollectionUtils.isNotEmpty(orderEntryList)) {
            for (AbstractOrderEntryModel orderEntry : orderEntryList) {

                if (orderEntry != null && orderEntry.getProduct() != null) {

                    signedData.put("item_" + index + "_name", orderEntry.getProduct().getName());
                    signedData.put("item_" + index + "_sku", orderEntry.getProduct().getCode());

                    if (orderEntry.getTotalPrice() != null && orderEntry.getTotalPrice() != null) {
                        signedData.put("item_" + index + "_unit_price", orderEntry.getTotalPrice().toString());
                    }

                    if (orderEntry.getQuantity() != null) {
                        signedData.put("item_" + index + "_quantity", orderEntry.getQuantity().toString());
                    }
                    ++index;
                }

            }
        }*//*

     *//*Integer itemCount = CollectionUtils.size(orderEntryList);

        if (cartModel.getDiscountedDeliveryCost() != null) {
            signedData.put("item_" + index + "_code", CybersourceConstants.CYBERSOURCE_SHIPPING_ITEM_CODE);
            signedData.put("item_" + index + "_name", CybersourceConstants.CYBERSOURCE_SHIPPING_ITEM_NAME);
            signedData.put("item_" + index + "_sku", String.valueOf(index));
            signedData.put("item_" + index + "_unit_price", cartModel.getDiscountedDeliveryCost().toString());
            signedData.put("item_" + index + "_quantity", BigInteger.ONE.toString());
            ++itemCount;
        }*//*

        //signedData.put("line_item_count", itemCount.toString());

        // Add bill_to data to map
        *//*AddressModel billingAddress = cartModel.getBillingAddress();
        if (billingAddress != null) {

            signedData.put("bill_to_forename", billingAddress.getFirstname());
            signedData.put("bill_to_surname", billingAddress.getLastname());
            signedData.put("bill_to_address_line1", billingAddress.getLine1());
            signedData.put("bill_to_address_city", billingAddress.getTown());
            signedData.put("bill_to_address_postal_code", billingAddress.getPostalcode());

            if (StringUtils.isNotEmpty(billingAddress.getLine2())) {
                signedData.put("bill_to_address_line2", billingAddress.getLine2());
            }

            if (billingAddress.getCountry() != null) {
                signedData.put("bill_to_address_country", billingAddress.getCountry().getIsocode());
            }
        }

        // Add ship_to data to map
        AddressModel deliveryAddress = cartModel.getDeliveryAddress();
        if (deliveryAddress != null) {
            signedData.put("ship_to_forename", deliveryAddress.getFirstname());
            signedData.put("ship_to_surname", deliveryAddress.getLastname());
            signedData.put("ship_to_address_line1", deliveryAddress.getLine1());
            signedData.put("ship_to_address_city", deliveryAddress.getTown());
            signedData.put("ship_to_address_postal_code", deliveryAddress.getPostalcode());

            if (StringUtils.isNotEmpty(deliveryAddress.getLine2())) {
                signedData.put("ship_to_address_line2", deliveryAddress.getLine2());
            }

            if (deliveryAddress.getCountry() != null) {
                signedData.put("ship_to_address_country", deliveryAddress.getCountry().getIsocode());
            }
        }*//*

        signedData.put("override_custom_receipt_page", props.getProperty("cybersource.returnurl"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("data to sign before filtering:" + signedData);
        }

        Iterator<String> valuesItor = signedData.values().iterator();

        while (valuesItor.hasNext()) {

            String value = valuesItor.next();
            if (value == null || "null".equalsIgnoreCase(value)) {
                valuesItor.remove();
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("data to sign after filtering:" + signedData);
        }

        //this will be the return of the method
        StringBuilder stringBuf = new StringBuilder();
        stringBuf.append("<!-- Secure Acceptance Signed Data Fields -->\n");
        Iterator<Map.Entry<String, String>> signedDataIterator = signedData.entrySet().iterator();
        String listOfSignedDataFields = StringUtils.EMPTY;
        while (signedDataIterator.hasNext()) {
            Map.Entry<String, String> signedDataFields = signedDataIterator.next();
            listOfSignedDataFields += signedDataFields.getKey() + ",";
            String value = signedDataFields.getValue();
            stringBuf.append("<input type=\"hidden\" id=\"" + signedDataFields.getKey() + "\" name=\"" + signedDataFields.getKey() + "\" value=\"" + StringUtils.trimToEmpty(value)
                    + "\"/>\n");
        }

        listOfSignedDataFields += "signed_field_names";
        signedData.put("signed_field_names", listOfSignedDataFields);

        String signature = null;
        try {
            signature = sign(signedData, props);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            LOG.error("Could not sign the form, cybersource will refuse this call: " + e, e);
        }

        stringBuf.append("<input type=\"hidden\" id=\"signed_field_names\" name=\"signed_field_names\" value=\"" + listOfSignedDataFields + "\"/>\n");
        stringBuf.append("\n<!-- Secure Acceptance Signature -->\n");
        stringBuf.append("<input type=\"hidden\" id=\"signature\" name=\"signature\" value=\"" + signature + "\"/>");

        return stringBuf.toString();
    }

    *//*public String sign(final Map<String, String> secureAcceptanceResponse,Properties props) throws InvalidKeyException, NoSuchAlgorithmException {
        return sign(buildDataToSign(secureAcceptanceResponse), props);
    }
    private static String buildDataToSign(Map<String, String> secureAcceptanceResponse) {
        String[] signedFieldNames = String.valueOf(secureAcceptanceResponse.get("signed_field_names")).split(",");
        List<String> dataToSign = new ArrayList<String>();
        for (String signedFieldName : signedFieldNames) {

            String fieldValue = secureAcceptanceResponse.get(signedFieldName);
            dataToSign.add(signedFieldName + "=" + StringUtils.trimToEmpty(fieldValue));
        }
        return StringUtils.join(dataToSign, ',');
    }
    public String sign(String data, Properties props) throws InvalidKeyException, NoSuchAlgorithmException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(props.getProperty("cybersource.sign.secretkey").getBytes(),
                props.getProperty("cybersource.sign.algorithm"));
        Mac mac = Mac.getInstance(props.getProperty("cybersource.sign.algorithm"));
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes());
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encodeBuffer(rawHmac).replace("\n", StringUtils.EMPTY);
    }*/

}
