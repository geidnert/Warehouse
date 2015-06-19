package com.solidparts.warehouse.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solidparts.warehouse.dto.ItemDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 * http://www.techotopia.com/index.php/An_Android_Studio_SQLite_Database_Tutorial
 */
public class OfflineItemDAO extends SQLiteOpenHelper implements IItemDAO {
    public static final String ITEM = "item";
    public static final String CACHE_ID = "cache_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String LOCATION = "location";
    public static final String ONLINEID = "onlineid";
    public static final String IMAGE = "image";
    public static final String COUNT = "count";
    public static final String QRCODE = "qrcode";
    public static final String SYNCED = "synced";

    public OfflineItemDAO(Context context) {
        super(context, "warehouse.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createItems = "CREATE TABLE " + ITEM + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ONLINEID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + COUNT + " INTEGER, " + IMAGE +
                " BLOB, " + QRCODE + " TEXT, " + LOCATION + " TEXT, " + SYNCED + " INTEGER );";

        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public List<ItemDTO> getItems(String searchTerm, int searchType) throws IOException, JSONException {

        String query = "Select * FROM " + ITEM + " WHERE " + NAME + " LIKE  \"%" + searchTerm + "%\"" + " OR " + LOCATION + " LIKE \"%" + searchTerm + "%\" AND " + SYNCED + " < 2";

        // Search all in a location
        if(searchType == ALL) {
            query = "Select * FROM " + ITEM;
        }
        List<ItemDTO> searchResultList = getItemDTOs(query);
        return searchResultList;
    }

    public List<ItemDTO> getNotSyncedAddedItems() throws Exception {
        String query = "Select * FROM " + ITEM + " WHERE " + SYNCED + " = 0";
        List<ItemDTO> searchResultList = getItemDTOs(query);
        return searchResultList;
    }

    public List<ItemDTO> getNotSyncedRemovedItems() throws Exception {
        String query = "Select * FROM " + ITEM + " WHERE " + SYNCED + " = 2";
        List<ItemDTO> searchResultList = getItemDTOs(query);
        return searchResultList;
    }

    private List<ItemDTO> getItemDTOs(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<ItemDTO> searchResultList = new ArrayList<>();
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            ItemDTO itemDto = new ItemDTO();

            itemDto.setCacheID(cursor.getInt(0));
            itemDto.setCount(cursor.getInt(4));
            itemDto.setDescription(cursor.getString(3));
            itemDto.setLocation(cursor.getString(7));
            itemDto.setName(cursor.getString(2));
            itemDto.setOnlineid(cursor.getInt(1));
            itemDto.setImage(cursor.getBlob(5));
            itemDto.setQrCode(cursor.getBlob(6));

            searchResultList.add(itemDto);
            cursor.moveToNext();
        }

        db.close();
        return searchResultList;
    }

    @Override
    public void addItem(ItemDTO itemDTO, int sync) throws IOException, JSONException {
        ContentValues cv = new ContentValues();

        cv.put(ONLINEID, itemDTO.getOnlineid());
        cv.put(NAME, itemDTO.getName());
        cv.put(DESCRIPTION, itemDTO.getDescription());
        cv.put(COUNT, itemDTO.getCount());
        cv.put(LOCATION, itemDTO.getLocation());
        cv.put(IMAGE, itemDTO.getImage());
        cv.put(QRCODE, itemDTO.getQrCode());
        cv.put(SYNCED, sync);

        long cachceId = getWritableDatabase().insert(ITEM, null, cv);
        itemDTO.setCacheID(cachceId);
    }

    @Override
    public void updateItem(ItemDTO itemDTO, int sync){
        System.out.println("values: " + itemDTO.toString());
        ContentValues cv = new ContentValues();

        cv.put(ONLINEID, itemDTO.getOnlineid());
        cv.put(NAME, itemDTO.getName());
        cv.put(DESCRIPTION, itemDTO.getDescription());
        cv.put(COUNT, itemDTO.getCount());
        cv.put(LOCATION, itemDTO.getLocation());
        cv.put(IMAGE, itemDTO.getImage());
        cv.put(QRCODE, itemDTO.getQrCode());
        cv.put(SYNCED, sync);

        String where = "onlineid=?";

        String[] whereArgs = {Long.toString(itemDTO.getOnlineid())};
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(ITEM, cv, where, whereArgs);

    }

    @Override
    public void removeItemByOnlineId(int onlineId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ITEM, ONLINEID + "=" + onlineId, null);
        db.close();
    }

    @Override
    public void removeItemByCacheId(long cacheId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ITEM, CACHE_ID + "=" + cacheId, null);
        db.close();
    }
}
