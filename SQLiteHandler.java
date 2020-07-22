package com.ikai.unitshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by shiv on 12/11/17.
 */

class SQLiteHandler extends SQLiteOpenHelper {
    private final static String TABLE_NAME  = "user_info";
    private final static String NAME  = "name";
    private final static String SHOP_NAME = "shop_name";
    private final static String SHOP_ADDRESS = "shop_address";
    private final static String PIN_CODE = "shop_pin_code";
    private final static String ID = "id";
    SQLiteHandler(Context context) {
        super(context,"userDetails", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +"(" + ID + " INTEGER PRIMARY KEY,"
                + NAME +" TEXT," + SHOP_NAME + " TEXT," + SHOP_ADDRESS +" TEXT,"
                + PIN_CODE +" TEXT" + ")";
        try {
            db.execSQL(query);
        } catch (SQLiteException exception) {
            Log.d("Database Error", "The error: " + exception.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void insertRecords(ContentValues rowRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, rowRecord);
        db.close();
    }

    ContentValues readFirstRow(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{ID, NAME, SHOP_NAME, SHOP_ADDRESS,
                PIN_CODE}, ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        // Variable to store the first row record.
        ContentValues values = null;

        if (cursor != null){
            cursor.moveToFirst();

            // Read all data and put it into values.
            values = new ContentValues();
            values.put(NAME, cursor.getString(1));
            values.put(SHOP_NAME, cursor.getString(2));
            values.put(SHOP_ADDRESS, cursor.getString(3));
            values.put(PIN_CODE, cursor.getString(4));
            // close the cursor
            cursor.close();
        }
        // Close the database
        db.close();

        // return the row.
        return values;
    }

    String getUserName(int id) {
        ContentValues contentValues = readFirstRow(id);
        return contentValues.get(NAME).toString();
    }

    int updateRecord (ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        int id = (int)values.get(ID);
        int returnValue = db.update(TABLE_NAME, values, ID + "=?",
                new String[]{String.valueOf(id)});
        // Close the database
        db.close();
        return returnValue;
    }
}
