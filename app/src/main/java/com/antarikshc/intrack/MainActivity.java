package com.antarikshc.intrack;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.antarikshc.intrack.data.InvContract;
import com.antarikshc.intrack.data.InvContract.InvEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int INVENT_LOADER = 0;

    /**
     * Adapter and the ListView
     */
    ListView itemListView;
    InvCursorAdapter cursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ListView which will be populated with the inventory data
        itemListView = findViewById(R.id.item_list);

        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        cursorAdapter = new InvCursorAdapter(this, null);
        itemListView.setAdapter(cursorAdapter);

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

        if (data != null) {
            cursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
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
