package com.solidparts.warehouse.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solidparts.warehouse.dto.ItemDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by geidnert on 28/05/15.
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
    public List<ItemDTO> getItems(String searchTerm) throws IOException, JSONException {
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createItems = "CREATE TABLE" + ITEM + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GUID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + COUNT + " INTEGER, " + IMAGE +
                " BLOB, " + QRCODE + " TEXT " + " );";

        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void add(ItemDTO itemDTO){

        ContentValues cv = new ContentValues();

        cv.put(GUID, itemDTO.getGuid());
        cv.put(NAME, itemDTO.getName());
        cv.put(DESCRIPTION, itemDTO.getDescription());
        cv.put(COUNT, itemDTO.getCount());
        cv.put(IMAGE, itemDTO.getImage());
        cv.put(QRCODE, itemDTO.getQrCode());

        long cachceId = getWritableDatabase().insert(ITEM, NAME, cv);

        itemDTO.setCacheID(cachceId);

    }
}
