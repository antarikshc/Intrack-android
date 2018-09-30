package com.antarikshc.intrack.ui;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.antarikshc.intrack.R;
import com.antarikshc.intrack.data.InvContract;
import com.antarikshc.intrack.data.InvContract.InvEntry;

import java.io.ByteArrayOutputStream;

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

    // FAB Button
    FloatingActionButton fabAdd;

    // Spinning Loader
    ProgressBar loadSpin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ListView which will be populated with the inventory data
        itemListView = findViewById(R.id.item_list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        loadSpin = findViewById(R.id.loadSpin);

        fabAdd = findViewById(R.id.fab_add_button);

        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        cursorAdapter = new InvCursorAdapter(this, null);
        itemListView.setAdapter(cursorAdapter);

        // Initiate the loader
        getLoaderManager().initLoader(INVENT_LOADER, null, this);

        // FAB Button to add new Item
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_ITEM_NAME,
                InvEntry.COLUMN_ITEM_ICON,
                InvEntry.COLUMN_ITEM_STOCK,
                InvEntry.COLUMN_ITEM_CAPACITY,
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
        loadSpin.setVisibility(View.GONE);

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

        // Get a the default Bitmap for demo
        Bitmap icon = getBitmapFromVectorDrawable(this, R.drawable.ic_default_image);

        // Converting Bitmap to ByteArray
        byte[] img = getBitmapAsByteArray(icon);

        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_ITEM_NAME, "Micro-USB Cable");
        values.put(InvEntry.COLUMN_ITEM_STOCK, 53);
        values.put(InvEntry.COLUMN_ITEM_ICON, img);
        values.put(InvEntry.COLUMN_ITEM_CAPACITY, 100);
        values.put(InvEntry.COLUMN_ITEM_SUP_PHONE, "+9100000000");
        values.put(InvEntry.COLUMN_ITEM_SUP_EMAIL, "order@flipkart.com");

        getContentResolver().insert(InvContract.CONTENT_URI, values);
    }

    /**
     * Delete all the data from table
     **/
    private void deleteAll() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                getContentResolver().delete(InvContract.CONTENT_URI, null, null);
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyItem();
                return true;

            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // To get Bitmap from a Vector Drawable
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    // Convert the obtained bitmap into ByteArray to store in DB
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }
}
