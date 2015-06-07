package com.solidparts.warehouse.dao;

import android.database.sqlite.SQLiteDatabase;

import com.solidparts.warehouse.dto.ItemDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public class OnlineItemDAO implements IItemDAO {

    private final NetworkDAO networkDAO;

    public OnlineItemDAO(){
        networkDAO = new NetworkDAO();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public List<ItemDTO> getItems(String searchTerm, int searchType) throws IOException, JSONException {
        String uri = "http://warehouse.com/perl/mobile/viewItemsjson.pl?Combined_Name=" + searchTerm;
        String request = networkDAO.request(uri);

        List<ItemDTO> allItems = new ArrayList<ItemDTO>();
        JSONObject root = new JSONObject(request);
        JSONArray items = root.getJSONArray("items");

        for (int i=0; i < items.length(); i++){
            JSONObject jsonItem = items.getJSONObject(i);

            int guid = jsonItem.getInt("guid");
            int count = jsonItem.getInt("count");
            String name = jsonItem.getString("name");
            String description = jsonItem.getString("description");
            String location = jsonItem.getString("location");
            byte[] image = jsonItem.get("image").toString().getBytes("utf-8");
            byte[]  qrCode = jsonItem.get("qrCode").toString().getBytes("utf-8");

            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setGuid(guid);
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
    public ItemDTO addItem(ItemDTO itemDTO) throws IOException, JSONException {
        return null;
    }

    @Override
    public ItemDTO updateItem(ItemDTO itemDTO) throws IOException, JSONException {
        return null;
    }

    @Override
    public void removeItem(long cacheId) throws IOException, JSONException {

    }
}
