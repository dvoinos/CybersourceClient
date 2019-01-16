package com.wadoz.cybersource.controller;

import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.wadoz.cybersource.util.CybersourceUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/checkout")
public class CybersourceController {

    private static String pathResources = "src/main/resources/";

    private static int cartidnumber = 12345677;
    private static HashMap<String, String> params = new HashMap<>();
    private final CybersourceUtils utils;

    private static final Properties loadProp = new Properties();

    private static final InputStream caprureProp = CybersourceController.class
            .getClassLoader()
            .getResourceAsStream("capture.properties");

    private static final InputStream localProp = CybersourceController.class
            .getClassLoader()
            .getResourceAsStream("local.properties");

    private static final InputStream authProp = CybersourceController.class
            .getClassLoader()
            .getResourceAsStream(" auth_reversal.properties");

    private static final InputStream cybProp = CybersourceController.class
            .getClassLoader()
            .getResourceAsStream("cybs.properties");

    public CybersourceController(CybersourceUtils utils) {
        this.utils = utils;
    }

    private static void displayMap(String header, Map map) {
        System.out.println(header);

        StringBuffer dest = new StringBuffer();

        if (map != null && !map.isEmpty()) {
            Iterator iter = map.keySet().iterator();
            String key, val;
            while (iter.hasNext()) {
                key = (String) iter.next();
                val = (String) map.get(key);
                dest.append(key + "=" + val + "\n");
            }
        }

        System.out.println(dest.toString());
    }

    private static Properties writeProperty(String filename, String authRequestID, String merchantReferenceCode,
                                            String purchaseTotals_currency, String item_0_unitPrice) {
        Properties props = new Properties();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename);
            if (filename.equals("auth_reversal.properties")) {
                props.setProperty("ccAuthReversalService_authRequestID", authRequestID);
                props.setProperty("ccAuthReversalService_run", "true");
            } else {
                props.setProperty("ccCaptureService_run", "true");
                props.setProperty("ccCaptureService_authRequestID", authRequestID);
            }
            props.setProperty("merchantReferenceCode", merchantReferenceCode);
            props.setProperty("purchaseTotals_currency", purchaseTotals_currency);
            props.setProperty("item_0_unitPrice", item_0_unitPrice);

            props.store(fos, null);

            fos.close();
            return (props);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (props);
    }

    private void handleCriticalException(ClientException e, HashMap<String, String> request) {

    }

    //1-st step It's start page for payment
    @RequestMapping(value = "cybersource.do", method = RequestMethod.GET)
    public String startProcess() {
        return "payment_form";
    }

    //second step It's for generate signature and POST payment
    @RequestMapping(value = "cybersource.do", method = RequestMethod.POST)
    public String signAndToken(ModelMap model, HttpServletRequest request) {

        HashMap signedData = new LinkedHashMap();

        try {

            loadProp.load(localProp);

            String merchantReferenceCode = String.valueOf(cartidnumber += 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            signedData.put("access_key", loadProp.getProperty("cybersource.accessKey"));
            signedData.put("profile_id", loadProp.getProperty("cybersource.profileId"));
            signedData.put("transaction_uuid", UUID.randomUUID());

            signedData.put("override_custom_receipt_page", loadProp.getProperty("cybersource.returnurl"));
            signedData.put("signed_field_names", loadProp.getProperty("cybersource.signed_field_names"));
            signedData.put("unsigned_field_names", loadProp.getProperty("cybersource.unsigned_field_names"));

            signedData.put("signed_date_time", simpleDateFormat.format(new Date()));
            signedData.put("reference_number", merchantReferenceCode);
            signedData.put("device_fingerprint_id", utils.getSessionId());
            signedData.put("locale", "en");

            request.setCharacterEncoding("UTF-8");

            Enumeration<String> paramsEnum = request.getParameterNames();
            while (paramsEnum.hasMoreElements()) {
                String paramName = paramsEnum.nextElement();
                String paramValue = request.getParameter(paramName);
                if (!paramName.contains("submit")) {
                    signedData.put(paramName, paramValue);
                }
            }
            model.addAttribute("requestaddress", loadProp.getProperty("cybersource.requestaddress"));
            model.addAttribute("content", utils.collectDataAndSign(loadProp.getProperty("cybersource.returnurl"), loadProp, signedData));

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();

        }

        return "payment_confirmation";
    }

    //It's page purchase decision page and read via token information
    @RequestMapping(value = "return.do", method = RequestMethod.GET)
    public String checkCapture(HttpServletRequest request, Model model) {

        model.addAttribute("responseMap", params);

        return "receipt";
    }

    //4-r step It's page "order.jsp" and saving properties
    @RequestMapping(value = "order.do", method = RequestMethod.POST)
    public String order(HttpServletRequest request) {

        Enumeration<String> paramsEnum = request.getParameterNames();

        while (paramsEnum.hasMoreElements()) {
            String paramName = paramsEnum.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        //write to .properties
        writeProperty("capture.properties", params.get("transaction_id"), params.get("req_reference_number"),
                params.get("req_currency"), params.get("req_amount"));

        writeProperty("auth_reversal.properties", params.get("transaction_id"), params.get("req_reference_number"),
                params.get("req_currency"), params.get("req_amount"));

        return "order";
    }

    //5-t-2 step //It's button "Cancel"
    @RequestMapping(value = "cancel_auth", params = "action2", method = RequestMethod.POST)
    public String cancelAuth() {
        runAuthReversal();
        return "cancel_auth";
    }

    //5-t-1 step It's button "Capture"
    @RequestMapping(value = "process_capture", params = "action1", method = RequestMethod.POST)
    public String orderCapture() {
        runCapture();
        return "process_capture";
    }

    private void runCapture() {

        try {
            loadProp.load(caprureProp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Properties captureProps = readProperty(pathResources + "capture.properties");
        String authRequestID = loadProp.getProperty("ccCaptureService_authRequestID");
        HashMap<String, String> request = new HashMap<String, String>(
                (Map) loadProp);
        Properties properties = new Properties();
        try {
            properties.load(cybProp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            displayMap("FOLLOW-ON CAPTURE REQUEST:", request);
            // run transaction now
            Map reply = Client.runTransaction(request, properties);

            displayMap("FOLLOW-ON CAPTURE REPLY:", reply);
            //write to db
            writeToMongoDb(reply, "CAPTURE");

        } catch (ClientException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        } catch (FaultException e) {
            e.printStackTrace();
        }
    }

    private void runAuthReversal() {

        try {
            loadProp.load(authProp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, String> request = new HashMap<String, String>(
                (Map) loadProp);

        try {
            displayMap("REVERSAL REQUEST:", request);
            System.out.println("auth reversal");
            // run transaction now
            Map reply = Client.runTransaction(request, loadProp);
            displayMap("REVERSAL REPLY:", reply);

            //write to db
            writeToMongoDb(reply, "CANCEL_AUTHORIZATION");

        } catch (ClientException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        } catch (FaultException e) {
            e.printStackTrace();
        }

    }

    private static void writeToMongoDb(Map reply, String processing) {

        JSONObject obj = new JSONObject();
        obj.putAll(reply);
        Document doc = Document.parse(String.valueOf(obj));
        doc.put("processing", processing);
        //  Connect to DB
        MongoCollection mongo = new MongoClient("localhost", 27017)
                .getDatabase("CyberPayment")
                .getCollection("Customer");
        mongo.insertOne(doc);

    }


}