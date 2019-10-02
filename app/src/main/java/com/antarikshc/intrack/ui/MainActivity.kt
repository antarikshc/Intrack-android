package com.antarikshc.intrack.ui

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.ContentValues
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.antarikshc.intrack.R
import com.antarikshc.intrack.data.InvContract
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private val INVENT_LOADER = 0

    /**
     * Adapter and the ListView
     */
    lateinit var itemListView: ListView
    lateinit var cursorAdapter: InvCursorAdapter

    // FAB Button
    lateinit var fabAdd: FloatingActionButton

    // Spinning Loader
    lateinit var loadSpin: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the ListView which will be populated with the inventory data
        itemListView = findViewById(R.id.item_list)

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        val emptyView = findViewById<RelativeLayout>(R.id.empty_view)
        itemListView.emptyView = emptyView

        loadSpin = findViewById(R.id.loadSpin)

        fabAdd = findViewById(R.id.fab_add_button)

        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        cursorAdapter = InvCursorAdapter(this, null)
        itemListView.adapter = cursorAdapter

        // Initiate the loader
        loaderManager.initLoader<Cursor>(INVENT_LOADER, null, this)

        // FAB Button to add new Item
        fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, EditorActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // Define a projection that specifies the columns from the table we care about.
        val projection = arrayOf(InvContract.InvEntry._ID, InvContract.InvEntry.COLUMN_ITEM_NAME, InvContract.InvEntry.COLUMN_ITEM_ICON, InvContract.InvEntry.COLUMN_ITEM_STOCK, InvContract.InvEntry.COLUMN_ITEM_CAPACITY, InvContract.InvEntry.COLUMN_ITEM_SUP_PHONE, InvContract.InvEntry.COLUMN_ITEM_SUP_EMAIL)

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(this, // Parent activity context
                InvContract.CONTENT_URI, // Provider content URI to query
                projection, // No selection arguments
                null, null, null)// Columns to include in the resulting Cursor
        // No selection clause
        // Default sort order
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        loadSpin.visibility = View.GONE

        if (data != null) {
            cursorAdapter.swapCursor(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        cursorAdapter.swapCursor(null)
    }

    /**
     * Insert dummy data method
     */
    private fun insertDummyItem() {

        // Get a the default Bitmap for demo
        val icon = getBitmapFromVectorDrawable(this, R.drawable.ic_default_image)

        // Converting Bitmap to ByteArray
        val img = getBitmapAsByteArray(icon)

        val values = ContentValues()
        values.put(InvContract.InvEntry.COLUMN_ITEM_NAME, "Micro-USB Cable")
        values.put(InvContract.InvEntry.COLUMN_ITEM_STOCK, 53)
        values.put(InvContract.InvEntry.COLUMN_ITEM_ICON, img)
        values.put(InvContract.InvEntry.COLUMN_ITEM_CAPACITY, 100)
        values.put(InvContract.InvEntry.COLUMN_ITEM_SUP_PHONE, "+9100000000")
        values.put(InvContract.InvEntry.COLUMN_ITEM_SUP_EMAIL, "order@flipkart.com")

        contentResolver.insert(InvContract.CONTENT_URI, values)
    }

    /**
     * Delete all the data from table
     */
    private fun deleteAll() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_all_dialog_msg)
        builder.setPositiveButton(R.string.action_delete) { dialog, id ->
            // User clicked the "Delete" button, so delete the item.
            contentResolver.delete(InvContract.CONTENT_URI, null, null)
        }
        builder.setNegativeButton(R.string.action_cancel) { dialog, id ->
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the item.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {

            // Respond to a click on the "Insert dummy data" menu option
            R.id.action_insert_dummy_data -> {
                insertDummyItem()
                return true
            }

            // Respond to a click on the "Delete all entries" menu option
            R.id.action_delete_all_entries -> {
                deleteAll()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    // To get Bitmap from a Vector Drawable
    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    // Convert the obtained bitmap into ByteArray to store in DB
    fun getBitmapAsByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
        return outputStream.toByteArray()
    }

}