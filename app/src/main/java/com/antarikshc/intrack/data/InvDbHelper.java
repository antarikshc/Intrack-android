package com.antarikshc.intrack.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.antarikshc.intrack.data.InvContract.InvEntry;

public class InvDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InvDbHelper.class.getSimpleName();

    /**
     * Name and version of database
     **/
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InvDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when database is first created
     **/
    @Override
    public void onCreate(SQLiteDatabase db) {

        //String that contains the SQL statement to create the invent table
        String SQL_CREATE_INVENT_TABLE = "CREATE TABLE " + InvEntry.TABLE_NAME + " ("
                + InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InvEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InvEntry.COLUMN_ITEM_STOCK + " INTEGER NOT NULL DEFAULT 0, "
                + InvEntry.COLUMN_ITEM_CAPACITY + " INTEGER, "
                + InvEntry.COLUMN_ITEM_ICON + " BLOB, "
                + InvEntry.COLUMN_ITEM_SUP_PHONE + " INTEGER, "
                + InvEntry.COLUMN_ITEM_SUP_EMAIL + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENT_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //Delete the current database when upgrading

    }
}
