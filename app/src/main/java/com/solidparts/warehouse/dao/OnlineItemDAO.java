package com.solidparts.warehouse.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.solidparts.warehouse.dto.ItemDTO;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public class OnlineItemDAO implements IItemDAO {

    private final NetworkDAO networkDAO;
    private final OfflineItemDAO offlineItemDAO;
    private final Context context;

    public OnlineItemDAO(Context context){
        networkDAO = new NetworkDAO();
        offlineItemDAO = new OfflineItemDAO(context);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public List<ItemDTO> getItems(String searchTerm, int searchType) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("searchterm", searchTerm));

        String request = networkDAO.request(NetworkDAO.SEARCH, nameValuePairs);

        List<ItemDTO> allItems = new ArrayList<ItemDTO>();
        JSONObject root = new JSONObject(request);
        JSONArray items = root.getJSONArray("items");

        for (int i=0; i < items.length(); i++){
            JSONObject jsonItem = items.getJSONObject(i).getJSONObject("item");

            int id = jsonItem.getInt("id");
            int count = jsonItem.getInt("count");
            String name = jsonItem.getString("name");
            String description = jsonItem.getString("description");
            String location = jsonItem.getString("location");

            byte[] image = Base64.decode(jsonItem.get("image").toString(), Base64.DEFAULT);
            byte[] qrCode = Base64.decode(jsonItem.get("qrcode").toString(), Base64.DEFAULT);

            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setOnlineid(id);
            itemDTO.setCount(count);
            itemDTO.setName(name);
            itemDTO.setDescription(description);
            itemDTO.setLocation(location);
            itemDTO.setImage(image);
            itemDTO.setQrCode(qrCode);

            allItems.add(itemDTO);
        }

        return allItems;
    }

    @Override
    public void addItem(ItemDTO itemDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("name", itemDTO.getName()));
        nameValuePairs.add(new BasicNameValuePair("description", itemDTO.getDescription()));
        nameValuePairs.add(new BasicNameValuePair("count", itemDTO.getCount() + ""));
        nameValuePairs.add(new BasicNameValuePair("location", itemDTO.getLocation()));
        nameValuePairs.add(new BasicNameValuePair("image", Base64.encodeToString(itemDTO.getImage(), Base64.DEFAULT)));
        nameValuePairs.add(new BasicNameValuePair("qrcode", Base64.encodeToString(itemDTO.getQrCode(), Base64.DEFAULT)));

        String request = networkDAO.request(NetworkDAO.ADD, nameValuePairs);

        // TODO -- update with online db primary key on local item
        itemDTO.setOnlineid(Integer.parseInt(request.trim()));
        offlineItemDAO.updateItem(itemDTO, sync);

        // Also save to local database if its not a sync operation
        if(sync == 0) {
            offlineItemDAO.addItem(itemDTO, 1);
        }

    }

    @Override
    public void updateItem(ItemDTO itemDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("onlineid", itemDTO.getOnlineid()+""));
        nameValuePairs.add(new BasicNameValuePair("name", itemDTO.getName()));
        nameValuePairs.add(new BasicNameValuePair("description", itemDTO.getDescription()));
        nameValuePairs.add(new BasicNameValuePair("count", itemDTO.getCount() + ""));
        nameValuePairs.add(new BasicNameValuePair("location", itemDTO.getName()));
        nameValuePairs.add(new BasicNameValuePair("image", Base64.encodeToString(itemDTO.getImage(), Base64.DEFAULT)));
        nameValuePairs.add(new BasicNameValuePair("qrcode", Base64.encodeToString(itemDTO.getQrCode(), Base64.DEFAULT)));

        networkDAO.request(NetworkDAO.UPDATE, nameValuePairs);
    }

    @Override
    public void removeItem(long onlineId) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("onlineid", onlineId + ""));
        networkDAO.request(NetworkDAO.REMOVE, nameValuePairs);
    }

    @Override
    public List<ItemDTO> getNotSyncedItems() throws IOException, JSONException {
        return null;
    }
}
