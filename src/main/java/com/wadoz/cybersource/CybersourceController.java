package com.wadoz.cybersource;

import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/checkout")
public class CybersourceController {

    private final CybersourceUtils utils;
    private static String pathResources = "src/main/resources/";
    private static final String PROPERTIES = pathResources + "cybs.properties";
    private static Properties cybProperties;
    private static int cartidnumber = 12345677;

    @Autowired
    public CybersourceController(CybersourceUtils utils) {
        this.utils = utils;
    }

    Properties props = new Properties();
    HashMap<String, String> copy = new LinkedHashMap<>();
    HashMap signedData = new LinkedHashMap();


    @RequestMapping(value = "cybersource.do", method = RequestMethod.POST)
    public String signAndToken(ModelMap model, HttpServletRequest request) {
        HashMap signedData = new LinkedHashMap();
        try {
            props.load(CybersourceController.class.getClassLoader().getResourceAsStream("local.properties"));
            String merchantReferenceCode = String.valueOf(cartidnumber += 1);

            String profileID = props.getProperty("cybersource.profileId");

            String accessKey = props.getProperty("cybersource.accessKey");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            signedData.put("access_key", accessKey);
            signedData.put("profile_id", profileID);
            signedData.put("transaction_uuid", UUID.randomUUID());

            signedData.put("override_custom_receipt_page", props.getProperty("cybersource.returnurl"));
            signedData.put("signed_field_names", props.getProperty("cybersource.signed_field_names"));
            signedData.put("unsigned_field_names", props.getProperty("cybersource.unsigned_field_names"));

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
            model.addAttribute("requestaddress", props.getProperty("cybersource.requestaddress"));
            model.addAttribute("content", utils.collectDataAndSign(props.getProperty("cybersource.returnurl"), props, signedData));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return "payment_confirmation";
    }

    @RequestMapping(value = "cybersource.do", method = RequestMethod.GET)
    public String startProcess() {
        return "payment_form";
    }

    @RequestMapping(value = "return.do", method = RequestMethod.POST)
    public String returnProcess(HttpServletRequest request, Model model) {
        // JSONObject obj = new JSONObject();
        HashMap<String, String> params = new HashMap<>();
        Enumeration<String> paramsEnum = request.getParameterNames();

        while (paramsEnum.hasMoreElements()) {
            String paramName = paramsEnum.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
            //obj.put(paramName, paramValue);
        }

        model.addAttribute("responseMap", params);

        //Connect to DB
//        DBObject dbObject = (DBObject) JSON.parse(String.valueOf(obj));
//        Mongo mongo = new MongoClient("localhost", 27017);
//        DB db = mongo.getDB("CyberPayment");
//        DBCollection collection = db.getCollection("Token");
//
//        collection.insert((DBObject) dbObject);

        return "receipt";
    }
    @RequestMapping(value = "return.do", method = RequestMethod.GET)
    public String checkCapture(HttpServletRequest request, Model model) {
        // JSONObject obj = new JSONObject();
        HashMap<String, String> params = new HashMap<>();
        Enumeration<String> paramsEnum = request.getParameterNames();

        while (paramsEnum.hasMoreElements()) {
            String paramName = paramsEnum.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
            //obj.put(paramName, paramValue);
        }

        model.addAttribute("responseMap", params);
        model.addAttribute(runCapture(cybProperties));

        //Connect to DB
//        DBObject dbObject = (DBObject) JSON.parse(String.valueOf(obj));
//        Mongo mongo = new MongoClient("localhost", 27017);
//        DB db = mongo.getDB("CyberPayment");
//        DBCollection collection = db.getCollection("Token");
//
//        collection.insert((DBObject) dbObject);

        return "receipt";
    }


    @RequestMapping(value = "order.do", method = RequestMethod.GET)
    public String order(){

        return "order";
    }


    public String runCapture(Properties props) {

        Properties captureProps = readProperty(pathResources + "capture.properties");

        String authRequestID = captureProps.getProperty("ccCaptureService_authRequestID");

        HashMap<String, String> request = new HashMap<String, String>(
                (Map) captureProps);

        try {
            displayMap("FOLLOW-ON CAPTURE REQUEST:", request);
            // run transaction now
            Map reply = Client.runTransaction(request, props);

            displayMap("FOLLOW-ON CAPTURE REPLY:", reply);

            //   writeToMongoDb(captureProps);

        } catch (ClientException e) {
            System.out.println(e.getMessage());
            if (e.isCritical()) {
                handleCriticalException(e, request);
            }
        } catch (FaultException e) {
            e.printStackTrace();
        }
        return authRequestID;
    }

    private void handleCriticalException(ClientException e, HashMap<String, String> request) {

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

    private static Properties readProperty(String filename) {
        Properties props = new Properties();

        try {
            FileInputStream fis = new FileInputStream(filename);
            props.load(fis);
            fis.close();
            return (props);
        } catch (IOException ioe) {
            System.out.println("File not found");
            // do nothing. An empty Properties object will be returned.
        }

        return (props);
    }

}