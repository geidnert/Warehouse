package com.solidparts.warehouse.dao;

import android.database.sqlite.SQLiteDatabase;

import com.solidparts.warehouse.dto.ItemDTO;

import java.util.List;

public interface IItemDAO {
    public static final int DEFAULT = 1;
    public static final int ALL = 2;
    public static final String hostname = "solidparts.se";

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public List<ItemDTO> getItems(String searchTerm, int searchType) throws Exception;

    public void addItem(ItemDTO itemDTO, int sync) throws Exception;

    public void updateItem(ItemDTO itemDTO, int sync) throws Exception;

    public void removeItemByOnlineId(int onlineId) throws Exception;

    public void removeItemByCacheId(long cacheId) throws Exception;

    public List<ItemDTO> getNotSyncedAddedItems() throws Exception;

    public List<ItemDTO> getNotSyncedRemovedItems() throws Exception;

}
