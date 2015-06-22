package com.solidparts.warehouse.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.Toast;

import com.solidparts.warehouse.dao.IItemDAO;
import com.solidparts.warehouse.dao.OfflineItemDAO;
import com.solidparts.warehouse.dao.OnlineItemDAO;
import com.solidparts.warehouse.dto.ItemDTO;

import java.util.List;

public class ItemService implements IItemService {

    IItemDAO onlineItemDAO;
    IItemDAO offlineItemDAO;
    Context context;

    public ItemService(Context context) {
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
        } catch (Exception e) {
            // No network, use offline mode
            try {
                items = offlineItemDAO.getItems(searchTerm, searchType);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return items;
    }

    @Override
    public void addItem(ItemDTO itemDTO) {
        try {
            onlineItemDAO.addItem(itemDTO, 0);
            //offlineItemDAO.addItem(itemDTO, 0);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                offlineItemDAO.addItem(itemDTO, 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void updateItem(ItemDTO itemDTO) throws Exception {
        try {
            onlineItemDAO.updateItem(itemDTO, 1);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                offlineItemDAO.updateItem(itemDTO, 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void removeItem(ItemDTO itemDTO) {
        boolean success = false;
        boolean foundNotSyncedIem = false;

        try {
            onlineItemDAO.removeItemByOnlineId(itemDTO.getOnlineid());
        } catch (Exception e) {
            // No network, use offline mode
            try {
                //offlineItemDAO.removeItemByOnlineId(onlineId);
                List<ItemDTO> notSyncedAddedItems = offlineItemDAO.getNotSyncedAddedItems();

                for (ItemDTO notSynceditem : notSyncedAddedItems) {
                    if (notSynceditem.getCacheID() == itemDTO.getCacheID()) {
                        offlineItemDAO.removeItemByCacheId(itemDTO.getCacheID());
                        foundNotSyncedIem = true;
                    }
                }

                // mark for deletion
                if (!foundNotSyncedIem) {
                    offlineItemDAO.updateItem(itemDTO, 2);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    public int syncToOnlineDB() {
        // get all items that are not synced from local db
        List<ItemDTO> notSyncedAddedItems = null;
        List<ItemDTO> notSyncedRemovedItems = null;
        try {
            notSyncedAddedItems = offlineItemDAO.getNotSyncedAddedItems();
            notSyncedRemovedItems = offlineItemDAO.getNotSyncedRemovedItems();
        } catch (Exception e1) {
            e1.printStackTrace();
            return -1;
        }

        // Sync added items to online db if available
        if (notSyncedAddedItems != null && notSyncedAddedItems.size() > 0) {

            for (ItemDTO itemDTO : notSyncedAddedItems) {
                try {
                    onlineItemDAO.addItem(itemDTO, 1); // add not synced item to online db
                    offlineItemDAO.updateItem(itemDTO, 1); // mark item as synced in local db
                } catch (Exception e) {
                    // No network, can not sync
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        // Sync removed items to online db if available
        if (notSyncedRemovedItems != null && notSyncedRemovedItems.size() > 0) {

            for (ItemDTO itemDTO : notSyncedRemovedItems) {
                try {
                    onlineItemDAO.removeItemByOnlineId(itemDTO.getOnlineid()); // add not synced item to online db
                    offlineItemDAO.removeItemByOnlineId(itemDTO.getOnlineid()); // mark item as synced in local db
                } catch (Exception e) {
                    // No network, can not sync
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        return 2;
    }

    @Override
    public int syncFromOnlineDB() {
        if (!isNetworkAvaliable(context)) {
            return -1;
        }

        // get all items that are not synced from local db
        List<ItemDTO> onlineItems = null;
        List<ItemDTO> localItems = null;
        try {
            onlineItems = onlineItemDAO.getItems("all", OfflineItemDAO.DEFAULT);
            localItems = offlineItemDAO.getItems("all", OfflineItemDAO.ALL);
        } catch (Exception e1) {
            e1.printStackTrace();
            return -1;
        }

        // Sync to offline db
        if (onlineItems != null && onlineItems.size() > 0) {
            for (ItemDTO onlineItemDto : onlineItems) {
                boolean foundItem = false;
                try {
                    // check if online db item allready is defined in local db
                    for (ItemDTO localItemDTO : localItems) {
                        if (localItemDTO.equals(onlineItemDto)) {
                            foundItem = true;
                        }
                    }

                    // Did not find the item in the local storage, add it
                    if (!foundItem) {
                        offlineItemDAO.addItem(onlineItemDto, 1); // mark item as synced in local db
                    }
                } catch (Exception e) {
                    // No network, can not sync
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        return 1;
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
