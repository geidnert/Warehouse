package com.solidparts.warehouse.dao;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.util.ArrayList;

public interface INetworkDAO {
    public String request(String action, ArrayList<NameValuePair> nameValuePairs) throws IOException;
}
