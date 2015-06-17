package com.solidparts.warehouse.dao;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by geidnert on 28/05/15.
 */
public interface INetworkDAO {

    //public String request(String uri) throws IOException;
    public String request(String action, ArrayList<NameValuePair> nameValuePairs) throws IOException;
}
