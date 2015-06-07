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
    public List<ItemDTO> getItems(String searchTerm, int searchType) throws Exception {
        List<ItemDTO> item = null;

        try {
            //items = onlineIItemDAO.getItems(searchString);
            item = offlineItemDAO.getItems(searchTerm, searchType);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                item = offlineItemDAO.getItems(searchTerm, searchType);
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
    public ItemDTO addItem(ItemDTO itemDTO) {
        try {
            itemDTO = offlineItemDAO.addItem(itemDTO);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                itemDTO = offlineItemDAO.addItem(itemDTO);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return itemDTO;
    }

    @Override
    public ItemDTO updateItem(ItemDTO itemDTO) {
        try {
            itemDTO = offlineItemDAO.updateItem(itemDTO);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                itemDTO = offlineItemDAO.updateItem(itemDTO);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return itemDTO;
    }

    @Override
    public void removeItem(long cacheId)  {
        boolean success = false;

        try {
            //items = onlineIItemDAO.getItems(searchString);
            offlineItemDAO.removeItem(cacheId);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                offlineItemDAO.removeItem(cacheId);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
