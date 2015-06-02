package com.solidparts.warehouse.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

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

    public static final String ITEM = "ITEM";
    public static final String CACHE_ID = "CACHE_ID";
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
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
                " BLOB, " + QRCODE + " TEXT " + " );";

        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public ItemDTO getItem(String itemName) throws IOException, JSONException {
        //List<ItemDTO> itemList = new ArrayList<ItemDTO>();
        String query = "Select * FROM " + ITEM + " WHERE " + NAME + " =  \"" + itemName + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ItemDTO itemDto = new ItemDTO();

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            itemDto.setCacheID(cursor.getInt(0));
            itemDto.setCount(cursor.getInt(4));
            itemDto.setDescription(cursor.getString(3));
            itemDto.setName(cursor.getString(2));
            itemDto.setGuid(cursor.getInt(1));
            itemDto.setImage(cursor.getBlob(5));
            itemDto.setQrCode(cursor.getString(6));
            cursor.close();
        } else {
            itemDto = null;
        }

        db.close();
        return itemDto;
    }

    @Override
    public void addItem(ItemDTO itemDTO) throws IOException, JSONException {
        ContentValues cv = new ContentValues();

        cv.put(GUID, itemDTO.getGuid());
        cv.put(NAME, itemDTO.getName());
        cv.put(DESCRIPTION, itemDTO.getDescription());
        cv.put(COUNT, itemDTO.getCount());
        cv.put(IMAGE, itemDTO.getImage());
        cv.put(QRCODE, itemDTO.getQrCode());

        long cachceId = getWritableDatabase().insert(ITEM, null, cv);

        itemDTO.setCacheID(cachceId);
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
