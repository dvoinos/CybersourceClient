package com.wadoz.cybersource;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;


public interface CybersourceUtils {

    Map<String, String> collectDataAndSign(final String returnURL, final Properties props, final Map<String, String> map) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException;

    String getSessionId();

}
