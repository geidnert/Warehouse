package com.solidparts.warehouse.dao;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
public class ItemDAO implements IItemDAO {

    private final NetworkDAO networkDAO;

    public ItemDAO(){
        networkDAO = new NetworkDAO();
    }

    @Override
    public List<ItemDTO> getItems(String searchTerm) throws IOException, JSONException {
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
            String image = jsonItem.getString("image");
            String qrCode = jsonItem.getString("qrCode");

            ItemDTO item = new ItemDTO();
            item.setGuid(guid);
            item.setName(name);
            item.setDescription(description);
            item.setImage(image);
            item.setQrCode(qrCode);

            allItems.add(item);
        }
        return allItems;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
