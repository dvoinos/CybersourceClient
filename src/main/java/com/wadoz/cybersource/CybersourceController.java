package com.wadoz.cybersource;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/checkout")
public class CybersourceController {

    private final CybersourceUtils utils;

    private static int cartidnumber = 12345677;

    @Autowired
    public CybersourceController(CybersourceUtils utils) {
        this.utils = utils;
    }

    Properties props = new Properties();
    HashMap<String,String> copy = new LinkedHashMap<>();
    HashMap signedData = new LinkedHashMap();

//    @RequestMapping(value = "cybergoserver.do", method = RequestMethod.POST)
//    public String copyValue(Model model) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
//
//        model.addAttribute("content", utils.collectDataAndSign(props.getProperty("cybersource.returnurl"), props, signedData));
//        return "copy";
//
//    }

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
        JSONObject obj = new JSONObject();
        HashMap<String, String> params = new HashMap<>();
        Enumeration<String> paramsEnum = request.getParameterNames();

        while (paramsEnum.hasMoreElements()) {
            String paramName = paramsEnum.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
            obj.put(paramName, paramValue);
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


//    @RequestMapping(value = "start", method = RequestMethod.POST)
//    public String start(){
//     signedData.get()
//        return "payment_form2";
//    }



}