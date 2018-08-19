package com.openworldsimulator.tools;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

public class JSONTools {
    public static String readJson(String url) throws IOException {

        String responseBody = null;

        HttpClient client = new HttpClient();
        GetMethod method;

        method = new GetMethod(url);
        method.addRequestHeader("Accept", "application/json");

        try {
            client.executeMethod(method);

            int statusCode = method.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                responseBody = method.getResponseBodyAsString();
            } else {
                throw new HttpException("Received HTTP CODE : " + statusCode + " at URL: " + url);
            }

        } finally {
            method.releaseConnection();
        }

        return responseBody;
    }
}
