package com.solidparts.warehouse.dao;

import android.database.sqlite.SQLiteDatabase;

import com.solidparts.warehouse.dto.ItemDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public interface IItemDAO {
    public static final int DEFAULT = 1;
    public static final int ALL = 2;
    public static final String hostname = "solidparts.se";

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public List<ItemDTO> getItems(String searchTerm, int searchType) throws IOException, JSONException;

    public void addItem(ItemDTO itemDTO, int sync) throws IOException, JSONException;

    public void updateItem(ItemDTO itemDTO, int sync) throws IOException, JSONException;

    public void removeItem(long cacheId) throws IOException, JSONException;

    public List<ItemDTO> getNotSyncedItems() throws IOException, JSONException;

}
