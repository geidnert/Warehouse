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

    public static final int ALL = 2;
    public static final String ITEM = "ITEM";
    public static final String CACHE_ID = "CACHE_ID";
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String LOCATION = "LOCATION";
    public static final String GUID = "GUID";
    public static final String IMAGE = "IMAGE";
    public static final String COUNT = "COUNT";
    public static final String QRCODE = "QRCODE";

    public OfflineItemDAO(Context context) {
        super(context, "warehouse.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createItems = "CREATE TABLE " + ITEM + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GUID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + COUNT + " INTEGER, " + IMAGE +
                " BLOB, " + QRCODE + " TEXT, " + LOCATION + " TEXT "  + " );";

        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public List<ItemDTO> getItems(String searchTerm, int searchType) throws IOException, JSONException {

        String query = "Select * FROM " + ITEM + " WHERE " + NAME + " LIKE  \"%" + searchTerm + "%\"" + " OR " + LOCATION + " LIKE \"%" + searchTerm + "%\"";

        // Search all in a location
        if(searchType == ALL) {
            query = "Select * FROM " + ITEM + " WHERE " + LOCATION + " LIKE  \"%" + searchTerm + "%\"";
        }

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
            itemDto.setGuid(cursor.getInt(1));
            itemDto.setImage(cursor.getBlob(5));
            itemDto.setQrCode(cursor.getBlob(6));

            searchResultList.add(itemDto);
            cursor.moveToNext();
        }

        db.close();
        return searchResultList;
    }

    @Override
    public ItemDTO additem(ItemDTO itemDTO) throws IOException, JSONException {
        ContentValues cv = new ContentValues();

        cv.put(GUID, itemDTO.getGuid());
        cv.put(NAME, itemDTO.getName());
        cv.put(DESCRIPTION, itemDTO.getDescription());
        cv.put(COUNT, itemDTO.getCount());
        cv.put(LOCATION, itemDTO.getLocation());
        cv.put(IMAGE, itemDTO.getImage());
        cv.put(QRCODE, itemDTO.getQrCode());

        long cachceId = getWritableDatabase().insert(ITEM, null, cv);
        itemDTO.setCacheID(cachceId);

        return itemDTO;
    }

    public boolean removeItem(String itemName) throws IOException, JSONException {
        boolean result = false;
        String query = "Select * FROM " + ITEM + " WHERE " + NAME + " =  \"" + itemName + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ItemDTO itemDTO= new ItemDTO();

        if (cursor.moveToFirst()) {
            itemDTO.setName(cursor.getString(2));
            db.delete(ITEM, NAME + " = ?",
                    new String[] { String.valueOf(itemDTO.getName()) });
            cursor.close();
            result = true;
        }

        db.close();
        return result;
    }
}
