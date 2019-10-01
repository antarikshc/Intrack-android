package com.antarikshc.intrack.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.antarikshc.intrack.data.InvContract.InvEntry.Companion.COLUMN_ITEM_CAPACITY
import com.antarikshc.intrack.data.InvContract.InvEntry.Companion.COLUMN_ITEM_ICON
import com.antarikshc.intrack.data.InvContract.InvEntry.Companion.COLUMN_ITEM_NAME
import com.antarikshc.intrack.data.InvContract.InvEntry.Companion.COLUMN_ITEM_STOCK
import com.antarikshc.intrack.data.InvContract.InvEntry.Companion.COLUMN_ITEM_SUP_EMAIL
import com.antarikshc.intrack.data.InvContract.InvEntry.Companion.COLUMN_ITEM_SUP_PHONE
import com.antarikshc.intrack.data.InvContract.InvEntry.Companion.TABLE_NAME
import com.antarikshc.intrack.data.InvContract.InvEntry.Companion._ID

class InvDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * This is called when database is first created
     */
    override fun onCreate(db: SQLiteDatabase) {

        //String that contains the SQL statement to create the invent table
        val SQL_CREATE_INVENT_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + COLUMN_ITEM_STOCK + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_ITEM_CAPACITY + " INTEGER, "
                + COLUMN_ITEM_ICON + " BLOB, "
                + COLUMN_ITEM_SUP_PHONE + " TEXT, " //TEXT because we will save country code too

                + COLUMN_ITEM_SUP_EMAIL + " TEXT);")

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENT_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        //Delete the current database when upgrading

    }

    companion object {

        val LOG_TAG = InvDbHelper::class.java.simpleName

        /**
         * Name and version of database
         */
        private const val DATABASE_NAME = "inventory.db"
        private const val DATABASE_VERSION = 1
    }
}
