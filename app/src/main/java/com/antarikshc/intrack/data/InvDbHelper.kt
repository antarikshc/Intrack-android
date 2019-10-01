package com.antarikshc.intrack.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Name and version of database
 */
const val DATABASE_NAME = "inventory.db"
const val DATABASE_VERSION = 1

class InvDbHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * This is called when database is first created
     */
    override fun onCreate(db: SQLiteDatabase) {

        //String that contains the SQL statement to create the invent table
        val SQL_CREATE_INVENT_TABLE = ("CREATE TABLE " + InvContract.InvEntry.TABLE_NAME + " ("
                + InvContract.InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InvContract.InvEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InvContract.InvEntry.COLUMN_ITEM_STOCK + " INTEGER NOT NULL DEFAULT 0, "
                + InvContract.InvEntry.COLUMN_ITEM_CAPACITY + " INTEGER, "
                + InvContract.InvEntry.COLUMN_ITEM_ICON + " BLOB, "
                + InvContract.InvEntry.COLUMN_ITEM_SUP_PHONE + " TEXT, " //TEXT because we will save country code too

                + InvContract.InvEntry.COLUMN_ITEM_SUP_EMAIL + " TEXT);")

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENT_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //Delete the current database when upgrading
    }

}