package com.solidparts.warehouse.service;

import android.content.Context;

import com.solidparts.warehouse.ApplicationContextProvider;
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
    public List<ItemDTO> getItem(String searchString) {

        List<ItemDTO> items = null;
        try {
            //items = onlineIItemDAO.getItems(searchString);
            items = offlineItemDAO.getItems(searchString);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                items = offlineItemDAO.getItems(searchString);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }
}
