package com.solidparts.warehouse.dao;

import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public interface IItemDAO {

    public List<ItemDAO> getItems(String searchTerm) throws IOException, JSONException;

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

}
