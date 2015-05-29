package com.solidparts.warehouse.dao;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by geidnert on 28/05/15.
 */
public class NetworkDAO implements INetworkDAO{
    @Override
    public String request(String uri) throws IOException {
        String returnString = "";

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        returnString = httpClient.execute(httpGet, responseHandler);


        return returnString;
    }
}
