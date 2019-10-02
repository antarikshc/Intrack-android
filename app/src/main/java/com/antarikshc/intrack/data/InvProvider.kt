package com.antarikshc.intrack.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log

class InvProvider : ContentProvider() {

    val LOG_TAG = InvProvider::class.java.simpleName

    /**
     * Initialize the provider and the database helper object.
     */
    lateinit var mDbHelper: InvDbHelper

    companion object {
        /**
         * UriMatcher object to match a content URI to a corresponding code.
         * The input passed into the constructor represents the code to return for the root URI.
         * It's common to use NO_MATCH as the input for this case.
         */
        val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        /**
         * URI matcher code for the content URI for the invents table
         */
        val INVENTS = 100

        /**
         * URI matcher code for the content URI for a single item in the invents table
         */
        val ITEM_ID = 101

        // Static initializer. This is run the first time anything is called from this class.
        init {
            // The content URI of the form "content://com.antarikshc.intrack/invent" will map to the
            // integer code {@link #INVENTS}. This URI is used to provide access to MULTIPLE rows
            // of the invents table.
            sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INVENT, INVENTS)

            // The content URI of the form "content://com.antarikshc.intrack/invent/#" will map to the
            // integer code {@link #ITEM_ID}. This URI is used to provide access to ONE single row
            // of the invents table.
            sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INVENT + "/#", ITEM_ID)
        }
    }

    override fun onCreate(): Boolean {
        mDbHelper = InvDbHelper(context)
        return true
    }


    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var selection = selection
        var selectionArgs = selectionArgs

        // Get readable database
        val database = mDbHelper.getReadableDatabase()

        // This cursor will hold the result of the query
        val cursor: Cursor

        // Figure out if the URI matcher can match the URI to a specific code
        val match = sUriMatcher.match(uri)
        when (match) {

            INVENTS ->
                // For the INVENTS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(InvContract.InvEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)

            ITEM_ID -> {
                // For the ITEM_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.antarikshc.intrack/invent/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InvContract.InvEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InvContract.InvEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            }

            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(context!!.contentResolver, uri)

        return cursor
    }


    /**
     * Insert new data into the provider with the given ContentValues.
     */
    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val match = sUriMatcher.match(uri)
        when (match) {
            INVENTS -> return insertItem(uri, contentValues!!)
            else -> throw IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private fun insertItem(uri: Uri, values: ContentValues): Uri? {

        // Check that the name is not null
        val name = values.getAsString(InvContract.InvEntry.COLUMN_ITEM_NAME)
                ?: throw IllegalArgumentException("Inventory item requires a name.")

        // If the stock amount is provided, check that it's greater than or equal to 0
        val stock = values.getAsInteger(InvContract.InvEntry.COLUMN_ITEM_STOCK)
        require(!(stock != null && stock < 0)) { "Inventory item requires valid stock amount." }

        // Get writable database
        val database = mDbHelper.getWritableDatabase()

        // Insert the new inventory item with the given values
        val id = database.insert(InvContract.InvEntry.TABLE_NAME, null, values)
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id.equals(-1)) {
            Log.e(LOG_TAG, "Failed to insert row for $uri")
            return null
        }

        // Notify all listeners that the data has changed for the pet content URI
        context!!.contentResolver.notifyChange(uri, null)

        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id)
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var selection = selection
        var selectionArgs = selectionArgs

        // Get writable database
        val database = mDbHelper.getWritableDatabase()

        // Track the number of rows that were deleted
        val rowsDeleted: Int

        val match = sUriMatcher.match(uri)
        when (match) {
            INVENTS ->
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InvContract.InvEntry.TABLE_NAME, selection, selectionArgs)
            ITEM_ID -> {
                // Delete a single row given by the ID in the URI
                selection = InvContract.InvEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                rowsDeleted = database.delete(InvContract.InvEntry.TABLE_NAME, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Deletion is not supported for $uri")
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }

        // Return the number of rows deleted
        return rowsDeleted

    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        var selection = selection
        var selectionArgs = selectionArgs

        val match = sUriMatcher.match(uri)
        when (match) {
            INVENTS -> return updatePet(uri, contentValues!!, selection, selectionArgs)
            ITEM_ID -> {
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InvContract.InvEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                return updatePet(uri, contentValues!!, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Update is not supported for $uri")
        }

    }

    /**
     * Update inventory items in the database with the given content values. Apply the changes to the
     * rows specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private fun updatePet(uri: Uri, values: ContentValues, selection: String?, selectionArgs: Array<String>?): Int {
        // If the {@link InvEntry#COLUMN_ITEM_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InvContract.InvEntry.COLUMN_ITEM_NAME)) {
            val name = values.getAsString(InvContract.InvEntry.COLUMN_ITEM_NAME)
                    ?: throw IllegalArgumentException("Inventory item requires a name.")
        }

        // If the {@link InvEntry#COLUMN_ITEM_STOCK} key is present,
        // check that the stock amount value is valid.
        if (values.containsKey(InvContract.InvEntry.COLUMN_ITEM_STOCK)) {
            val stock = values.getAsInteger(InvContract.InvEntry.COLUMN_ITEM_STOCK)
            require(!(stock != null && stock < 0)) { "Inventory item requires valid stock amount." }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0
        }

        // Otherwise, get writable database to update the data
        val database = mDbHelper.getWritableDatabase()

        // Perform the update on the database and get the number of rows affected
        val rowsUpdated = database.update(InvContract.InvEntry.TABLE_NAME, values, selection, selectionArgs)

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }

        // Return the number of rows updated
        return rowsUpdated

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    override fun getType(uri: Uri): String? {
        val match = sUriMatcher.match(uri)
        when (match) {
            INVENTS -> return InvContract.InvEntry.CONTENT_LIST_TYPE
            ITEM_ID -> return InvContract.InvEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalStateException("Unknown URI $uri with match $match")
        }
    }

}