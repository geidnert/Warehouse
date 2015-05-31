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

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public ItemDTO getItem(String searchTerm) throws IOException, JSONException;

    public void addItem(ItemDTO itemDTO) throws IOException, JSONException;

    public boolean removeItem(String itemName) throws IOException, JSONException;

}
