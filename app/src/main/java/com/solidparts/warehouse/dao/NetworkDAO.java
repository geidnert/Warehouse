package com.solidparts.warehouse.dao;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by geidnert on 28/05/15.
 */
public class NetworkDAO implements INetworkDAO{
    public static final String ADD = "add.php";
    public static final String UPDATE = "update.php";
    public static final String REMOVE = "remove.php";
    public static final String SEARCH = "get.php";

    private static final String URL = "http://solidparts.se/warehouse/";

    @Override
    public String request(String action, ArrayList<NameValuePair> nameValuePairs) throws IOException {
        String returnString = "";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(URL + action);
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        returnString = httpClient.execute(httpPost, responseHandler);

        return returnString;
    }
    /*public String request(String uri) throws IOException {
        String returnString = "";

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        returnString = httpClient.execute(httpGet, responseHandler);

        return returnString;
    }*/
}
