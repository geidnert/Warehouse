package com.solidparts.warehouse.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.NetworkOnMainThreadException;
import android.view.Gravity;
import android.widget.Toast;

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
    Context context;

    public ItemService(Context context){
        onlineItemDAO = new OnlineItemDAO(context);
        offlineItemDAO = new OfflineItemDAO(context);
        this.context = context;
    }

    @Override
    public List<ItemDTO> getItems(String searchTerm, int searchType) throws Exception {
        List<ItemDTO> items = null;

        try {
            items = onlineItemDAO.getItems(searchTerm, searchType);
            //items = offlineItemDAO.getItems(searchTerm, searchType);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                items = offlineItemDAO.getItems(searchTerm, searchType);
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

    @Override
    public void addItem(ItemDTO itemDTO) {
        try {
            onlineItemDAO.addItem(itemDTO, 0);
            //itemDTO = offlineItemDAO.addItem(itemDTO, 0);
        } catch (NetworkOnMainThreadException e) {
            // No network, use offline mode
            /*try {
                offlineItemDAO.addItem(itemDTO, 0);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateItem(ItemDTO itemDTO) throws Exception {
        try {
            onlineItemDAO.updateItem(itemDTO, 1);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                offlineItemDAO.updateItem(itemDTO, 0);
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
    public void removeItem(long onlineId)  {
        boolean success = false;

        try {
            onlineItemDAO.removeItem(onlineId);
        } catch (IOException e) {
            // No network, use offline mode
            try {
                offlineItemDAO.removeItem(onlineId);
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
    public void syncToOnlineDB() {
        // get all items that are not synced from local db
        List<ItemDTO> notSyncedItems = null;
        try {
            notSyncedItems = offlineItemDAO.getNotSyncedItems();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        // Sync to online db if available
        if(notSyncedItems != null && notSyncedItems.size() > 0) {

            for(ItemDTO itemDTO : notSyncedItems) {
                try {
                    onlineItemDAO.addItem(itemDTO, 1); // add not synced item to online db
                    offlineItemDAO.updateItem(itemDTO, 1); // mark item as synced in local db
                } catch (IOException e) {
                    // No network, can not sync
                    e.printStackTrace();
                } catch (JSONException e) {
                    // No network, can not sync
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void syncFromOnlineDB() {
        if(!isNetworkAvaliable(context)){
            return;
        }

        // get all items that are not synced from local db
        List<ItemDTO> onlineItems = null;
        List<ItemDTO> localItems = null;
        try {
            onlineItems = onlineItemDAO.getItems("all", OfflineItemDAO.DEFAULT);
            localItems = offlineItemDAO.getItems("all", OfflineItemDAO.ALL);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        int addedItems = 0;
        // Sync to offline db
        if(onlineItems != null && onlineItems.size() > 0) {
            for(ItemDTO onlineItemDto : onlineItems) {
                    boolean foundItem = false;
                try {
                    // check if online db item allready is defined in local db
                    for(ItemDTO localItemDTO : localItems) {
                        if (localItemDTO.equals(onlineItemDto)){
                            foundItem = true;
                        }
                    }

                    // Did not find the item in the local storage, add it
                    if(!foundItem){
                        offlineItemDAO.addItem(onlineItemDto, 1); // mark item as synced in local db
                    }
                } catch (IOException e) {
                    // No network, can not sync
                    e.printStackTrace();
                } catch (JSONException e) {
                    // No network, can not sync
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isNetworkAvaliable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }

    private void showMessage(String message, boolean goBack) {
        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();
    }


}
