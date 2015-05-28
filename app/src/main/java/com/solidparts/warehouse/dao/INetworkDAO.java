package com.solidparts.warehouse.dao;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

/**
 * Created by geidnert on 28/05/15.
 */
public interface INetworkDAO {

    public String request(String uri) throws IOException, ClientProtocolException;

}
