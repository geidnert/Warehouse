package com.solidparts.warehouse.service;

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

    IItemDAO onlineIItemDAO;
    IItemDAO offlineIItemDAO;

    public ItemService(){
        onlineIItemDAO = new OnlineItemDAO();
    }

    @Override
    public List<ItemDTO> getItem(ItemDTO itemDTO) {

        List<ItemDTO> items = null;
        try {
            items = onlineIItemDAO.getItems("motor");
        } catch (IOException e) {
            // No network, use offline mode
            try {
                items = offlineIItemDAO.getItems("motor");
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
