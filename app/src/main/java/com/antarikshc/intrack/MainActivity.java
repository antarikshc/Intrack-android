package com.antarikshc.intrack;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.antarikshc.intrack.data.InvContract;
import com.antarikshc.intrack.data.InvContract.InvEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int INVENT_LOADER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insertDummyItem();

        // Initiate the loader
        getLoaderManager().initLoader(INVENT_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_ITEM_NAME,
                InvEntry.COLUMN_ITEM_STOCK,
                InvEntry.COLUMN_ITEM_SUP_PHONE,
                InvEntry.COLUMN_ITEM_SUP_EMAIL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InvContract.CONTENT_URI,        // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,              // No selection arguments
                null);                // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {

            String name = data.getString(data.getColumnIndex(InvEntry.COLUMN_ITEM_NAME));
            Integer stock = data.getInt(data.getColumnIndex(InvEntry.COLUMN_ITEM_STOCK));
            String phone = data.getString(data.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_PHONE));
            String email = data.getString(data.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_EMAIL));

            Log.i("Item Name", name);
            Log.i("Item Stock", String.valueOf(stock));
            Log.i("Supplier Phone", phone);
            Log.i("Supplier Email", email);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Insert dummy data method
     **/
    private void insertDummyItem() {

        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_ITEM_NAME, "USB Cable");
        values.put(InvEntry.COLUMN_ITEM_STOCK, 53);
        values.put(InvEntry.COLUMN_ITEM_SUP_PHONE, "+9100000000");
        values.put(InvEntry.COLUMN_ITEM_SUP_EMAIL, "order@flipkart.com");

        getContentResolver().insert(InvContract.CONTENT_URI, values);
    }
}
