package com.solidparts.warehouse.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.widget.Toast;

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
        String uri = "http://" + hostname +"/warehouse/get.php?searchterm=" + searchTerm;
        String request = networkDAO.request(uri);

        List<ItemDTO> allItems = new ArrayList<ItemDTO>();
        JSONObject root = new JSONObject(request);
        JSONArray items = root.getJSONArray("items");

        for (int i=0; i < items.length(); i++){
            JSONObject jsonItem = items.getJSONObject(i).getJSONObject("item");

            int guid = jsonItem.getInt("guid");
            int count = jsonItem.getInt("count");
            String name = jsonItem.getString("name");
            String description = jsonItem.getString("description");
            String location = jsonItem.getString("location");
            byte[] image = jsonItem.get("image").toString().getBytes("utf-8");
            byte[]  qrCode = jsonItem.get("qrcode").toString().getBytes("utf-8");

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
    public ItemDTO addItem(ItemDTO itemDTO, int sync) throws IOException, JSONException {

        String uri = "http://" + hostname +"/warehouse/add.php?name=" + itemDTO.getName() + "&guid=" + itemDTO.getGuid() + "&description=" + itemDTO.getDescription() +"&count=" + itemDTO.getCount()
                + "&image=" + itemDTO.getImage() + "&qrcode=" + itemDTO.getQrCode() + "&location=" + itemDTO.getLocation()
                + "&cacheid=" + itemDTO.getCacheID();
        String request = networkDAO.request(uri);

        // Also save to local database
        offlineItemDAO.addItem(itemDTO, 1);
        return null;
    }

    @Override
    public ItemDTO updateItem(ItemDTO itemDTO, int sync) throws IOException, JSONException {
        return null;
    }

    @Override
    public void removeItem(long cacheId) throws IOException, JSONException {

    }

    @Override
    public List<ItemDTO> getNotSyncedItems() throws IOException, JSONException {
        return null;
    }
}
