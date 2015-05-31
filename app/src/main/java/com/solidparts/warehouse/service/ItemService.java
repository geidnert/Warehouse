package com.solidparts.warehouse.service;

import android.content.Context;

import com.solidparts.warehouse.dao.IItemDAO;
import com.solidparts.warehouse.dao.OfflineItemDAO;
import com.solidparts.warehouse.dao.OnlineItemDAO;
import com.solidparts.warehouse.dto.ItemDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public class ItemService implements IItemService {

    IItemDAO onlineItemDAO;
    IItemDAO offlineItemDAO;

    public ItemService(Context context){
        onlineItemDAO = new OnlineItemDAO();
        offlineItemDAO = new OfflineItemDAO(context);
    }

    @Override
    public ItemDTO getItem(String itemName)  {
        ItemDTO item = null;

        try {
            //items = onlineIItemDAO.getItem(searchString);
            item = offlineItemDAO.getItem(itemName);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                item = offlineItemDAO.getItem(itemName);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public void addItem(ItemDTO itemDTO) {
        try {
            offlineItemDAO.addItem(itemDTO);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                offlineItemDAO.addItem(itemDTO);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeItem(String itemName)  {
        boolean success = false;

        try {
            //items = onlineIItemDAO.getItem(searchString);
            success = offlineItemDAO.removeItem(itemName);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                success = offlineItemDAO.removeItem(itemName);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return success;
    }
}
