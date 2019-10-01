package com.antarikshc.intrack.ui

import android.app.Activity
import android.content.*
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.text.TextUtils
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.antarikshc.intrack.R
import com.antarikshc.intrack.data.InvContract
import com.antarikshc.intrack.data.InvDbHelper
import java.io.ByteArrayOutputStream

class EditorActivity : AppCompatActivity(), android.app.LoaderManager.LoaderCallbacks<Cursor> {

    lateinit var itemIcon: ImageView

    lateinit var editImage: TextView

    lateinit var itemName: TextView
    lateinit var editItemName: AppCompatEditText

    lateinit var stockAmount: TextView
    lateinit var editStockAmount: AppCompatEditText

    lateinit var stockCapacity: TextView
    lateinit var editStockCapacity: AppCompatEditText

    lateinit var supplierInfo: TextView
    lateinit var editSupplierPhone: AppCompatEditText
    lateinit var supPhoneIcon: ImageView
    lateinit var editSupplierEmail: AppCompatEditText
    lateinit var supEmailIcon: ImageView

    lateinit var saveUpdateButton: TextView

    /**
     * Button Click animation as we are using TextView as buttons
     */
    lateinit var buttonClick: Animation

    // Request codes for Image intents
    val REQUEST_IMAGE_CAPTURE = 1
    val PICK_IMAGE = 2

    lateinit var pictureIntent: Intent

    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private var mCurrentUri: Uri? = null

    /**
     * to create PetDbHelper instance
     */
    lateinit var mDbHelper: InvDbHelper

    /**
     * Identifier for the inventory data loader
     */
    private val EXISTING_INVENT_LOADER = 0

    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private var mItemHasChanged = false

    /**
     * Boolean flag that keeps track of whether the Icon has changed or not
     */
    private var mItemIconHasChanged = false

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private val mTouchListener = View.OnTouchListener { view, motionEvent ->
        mItemHasChanged = true
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new inventory item or editing an existing one.
        val intent = intent
        mCurrentUri = intent.data

        // Bind views
        itemIcon = findViewById(R.id.item_icon)
        editImage = findViewById(R.id.edit_image)
        itemName = findViewById(R.id.item_name_text)
        editItemName = findViewById(R.id.item_name_edit)
        stockAmount = findViewById(R.id.stock_amount_text)
        editStockAmount = findViewById(R.id.stock_amount_edit)
        stockCapacity = findViewById(R.id.stock_capacity_text)
        editStockCapacity = findViewById(R.id.stock_capacity_edit)
        supplierInfo = findViewById(R.id.supplier_info)
        editSupplierPhone = findViewById(R.id.supplier_phone)
        supPhoneIcon = findViewById(R.id.supplier_phone_icon)
        editSupplierEmail = findViewById(R.id.supplier_email)
        supEmailIcon = findViewById(R.id.supplier_email_icon)
        saveUpdateButton = findViewById(R.id.save_update_button)

        // IDK, Maybe it looks good to have a back button in Toolbar
        // Not that anyone will use it xDDD
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // We are primarily working with ImageView and Bitmap.
        // Convert our ic_default_image to Bitmap and set that to ImageView
        val icon = getBitmapFromVectorDrawable(this, R.drawable.ic_default_image)
        itemIcon.setImageBitmap(icon)

        // If the intent DOES NOT contain a item content URI, then we know that we are
        // creating a new inventory item.
        if (mCurrentUri == null) {
            // This is a new item, so change the app bar to say "Add a Item"
            title = getString(R.string.editor_activity_new_title)

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu()
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            title = getString(R.string.editor_activity_edit_title)

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            loaderManager.initLoader(EXISTING_INVENT_LOADER, null, this)

            // We are updating the pet so rename button
            saveUpdateButton.setText(R.string.update_button)
        }

        // Bounce animation for buttons
        buttonClick = AnimationUtils.loadAnimation(applicationContext, R.anim.bounce)
        val interpolator = BounceInterpolator(0.2, 6.0)
        buttonClick.interpolator = interpolator

        /**
         * All the Focus change listeners for highlights
         */
        // Get the dark_blue color for EditText
        val darkPurpleColor = ColorStateList.valueOf(resources.getColor(R.color.dark_purple_edit_text))

        // Get the light_blue color for TextView
        val lightPurpleColor = ColorStateList.valueOf(resources.getColor(R.color.light_purple_text_view))

        // Default color for EditText and TextView
        val defaultColor = ColorStateList.valueOf(resources.getColor(android.R.color.tab_indicator_text))

        editItemName.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                itemName.setTextColor(lightPurpleColor)
                ViewCompat.setBackgroundTintList(v, darkPurpleColor)
            } else {
                itemName.setTextColor(defaultColor)
                ViewCompat.setBackgroundTintList(v, defaultColor)
            }
        }

        editStockAmount.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                stockAmount.setTextColor(lightPurpleColor)
                ViewCompat.setBackgroundTintList(v, darkPurpleColor)
            } else {
                stockAmount.setTextColor(defaultColor)
                ViewCompat.setBackgroundTintList(v, defaultColor)
            }
        }

        editStockCapacity.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                stockCapacity.setTextColor(lightPurpleColor)
                ViewCompat.setBackgroundTintList(v, darkPurpleColor)
            } else {
                stockCapacity.setTextColor(defaultColor)
                ViewCompat.setBackgroundTintList(v, defaultColor)
            }
        }

        editSupplierPhone.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                supplierInfo.setTextColor(lightPurpleColor)
                supPhoneIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_phone_purple))
                ViewCompat.setBackgroundTintList(v, darkPurpleColor)
            } else {
                supplierInfo.setTextColor(defaultColor)
                supPhoneIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_phone))
                ViewCompat.setBackgroundTintList(v, defaultColor)
            }
        }

        editSupplierEmail.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                supplierInfo.setTextColor(lightPurpleColor)
                supEmailIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_email_purple))
                ViewCompat.setBackgroundTintList(v, darkPurpleColor)
            } else {
                supplierInfo.setTextColor(defaultColor)
                supEmailIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_email))
                ViewCompat.setBackgroundTintList(v, defaultColor)
            }
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        editItemName.setOnTouchListener(mTouchListener)
        editStockAmount.setOnTouchListener(mTouchListener)
        editStockCapacity.setOnTouchListener(mTouchListener)
        editSupplierPhone.setOnTouchListener(mTouchListener)
        editSupplierEmail.setOnTouchListener(mTouchListener)

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = InvDbHelper(this)

    }

    fun editImageDialog(view: View) {
        // Initiate animation of the button
        view.startAnimation(buttonClick)

        mItemHasChanged = true

        val inflater = LayoutInflater.from(applicationContext)
        val dialogView = inflater.inflate(R.layout.edit_image_layout, null)
        val dialog = AlertDialog.Builder(this@EditorActivity).create()

        dialog.setView(dialogView)

        // we don't want title
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.setContentView(R.layout.edit_image_layout)

        // Bind views from dialog layout
        val clickPhoto = dialogView.findViewById<TextView>(R.id.click_photo)
        val choosePhoto = dialogView.findViewById<TextView>(R.id.choose_photo)

        clickPhoto.setOnClickListener(View.OnClickListener {
            pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (pictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE)
            }

            dialog.dismiss()
        })

        choosePhoto.setOnClickListener(View.OnClickListener {
            pictureIntent = Intent(Intent.ACTION_GET_CONTENT)
            pictureIntent.type = "image/*"
            startActivityForResult(pictureIntent, PICK_IMAGE)

            dialog.dismiss()
        })

        dialog.show()
    }

    /**
     * Input item from user to store in DB
     */
    fun saveUpdateItem(view: View) {

        // Get the info from EditTexts
        val nameOfItem = editItemName.text!!.toString().trim { it <= ' ' }
        val stockOfItem = editStockAmount.text!!.toString().trim { it <= ' ' }
        val capacityOfItem = editStockCapacity.text!!.toString().trim { it <= ' ' }
        val supPhoneOfItem = editSupplierPhone.text!!.toString().trim { it <= ' ' }
        val supEmailOfItem = editSupplierEmail.text!!.toString().trim { it <= ' ' }

        if (nameOfItem.isEmpty()) {
            Toast.makeText(this, "Provide name for the Item", Toast.LENGTH_SHORT).show()
            return
        }

        if (stockOfItem.isEmpty()) {
            Toast.makeText(this, "Provide initial value for amount of Stock", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (mCurrentUri == null &&
                TextUtils.isEmpty(nameOfItem) && TextUtils.isEmpty(stockOfItem) &&
                TextUtils.isEmpty(capacityOfItem) && TextUtils.isEmpty(supPhoneOfItem) &&
                TextUtils.isEmpty(supEmailOfItem)) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return
        }

        val values = ContentValues()
        values.put(InvContract.InvEntry.COLUMN_ITEM_NAME, nameOfItem)
        values.put(InvContract.InvEntry.COLUMN_ITEM_STOCK, stockOfItem)
        values.put(InvContract.InvEntry.COLUMN_ITEM_CAPACITY, capacityOfItem)
        values.put(InvContract.InvEntry.COLUMN_ITEM_SUP_PHONE, supPhoneOfItem)
        values.put(InvContract.InvEntry.COLUMN_ITEM_SUP_EMAIL, supEmailOfItem)

        // Only process the Bitmap if user has selected new image
        if (mItemIconHasChanged) {
            val iconOfItem = (itemIcon.drawable as BitmapDrawable).bitmap
            // Converting Bitmap to ByteArray
            val img = getBitmapAsByteArray(iconOfItem)

            values.put(InvContract.InvEntry.COLUMN_ITEM_ICON, img)
        }


        // Determine if this is a new or existing item by checking if mCurrentUri is null or not
        if (mCurrentUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            val newUri = contentResolver.insert(InvContract.CONTENT_URI, values)

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show()
            }
            finish()
        } else {
            // Otherwise this is an EXISTING item, so update the pet with content URI: mCurrentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentUri will already identify the correct row in the database that
            // we want to modify.
            val rowsAffected = contentResolver.update(mCurrentUri!!, values, null, null)

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show()
            }
            finish()
        }

    }

    /**
     * Perform the deletion of the item in the db.
     */
    private fun deletePet() {
        // Only perform the delete if this is an existing item.
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the item that we want.
            val rowsDeleted = contentResolver.delete(mCurrentUri!!, null, null)

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show()
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show()
            }
        }

        // Close the activity
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val imageBitmap: Bitmap
        val croppedImage: Bitmap

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            val extras = data!!.extras
            imageBitmap = extras!!.get("data") as Bitmap

            try {
                croppedImage = cropImage(imageBitmap)
                itemIcon.setImageBitmap(croppedImage)

                mItemIconHasChanged = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            val uri = data!!.data
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                croppedImage = cropImage(imageBitmap)
                itemIcon.setImageBitmap(croppedImage)

                mItemIconHasChanged = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        val projection = arrayOf(InvContract.InvEntry._ID, InvContract.InvEntry.COLUMN_ITEM_NAME, InvContract.InvEntry.COLUMN_ITEM_STOCK, InvContract.InvEntry.COLUMN_ITEM_CAPACITY, InvContract.InvEntry.COLUMN_ITEM_ICON, InvContract.InvEntry.COLUMN_ITEM_SUP_PHONE, InvContract.InvEntry.COLUMN_ITEM_SUP_EMAIL)

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(this, // Parent activity context
                mCurrentUri, // Query the content URI
                projection,
                null, null, null)// Columns to include in the resulting Cursor
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.count < 1) {
            return
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {

            // Cursor sometimes return columns in unordered fashion
            // Get the indices manually ... bruhh
            val id = data.getColumnIndex(InvContract.InvEntry._ID)
            val nameIndex = data.getColumnIndex(InvContract.InvEntry.COLUMN_ITEM_NAME)
            val stockAmountIndex = data.getColumnIndex(InvContract.InvEntry.COLUMN_ITEM_STOCK)
            val stockCapacityIndex = data.getColumnIndex(InvContract.InvEntry.COLUMN_ITEM_CAPACITY)
            val iconColumnIndex = data.getColumnIndex(InvContract.InvEntry.COLUMN_ITEM_ICON)
            val supPhoneIndex = data.getColumnIndex(InvContract.InvEntry.COLUMN_ITEM_SUP_PHONE)
            val supEmailIndex = data.getColumnIndex(InvContract.InvEntry.COLUMN_ITEM_SUP_EMAIL)

            // Set all the values retrieved from database
            editItemName.setText(data.getString(nameIndex))
            editStockAmount.setText(data.getString(stockAmountIndex))

            if (!data.isNull(stockCapacityIndex)) {
                editStockCapacity.setText(data.getString(stockCapacityIndex))
            }
            if (!data.isNull(supPhoneIndex)) {
                editSupplierPhone.setText(data.getString(supPhoneIndex))
            }
            if (!data.isNull(supEmailIndex)) {
                editSupplierEmail.setText(data.getString(supEmailIndex))
            }

            if (!data.isNull(iconColumnIndex)) {
                // Retrieve blob from cursor and convert to Bitmap
                val imgByte = data.getBlob(iconColumnIndex)
                val iconOfItem = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
                // Set the bitmap to ImageView
                itemIcon.setImageBitmap(iconOfItem)
            }

        }

    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

        // If the loader is invalidated, clear out all the data from the input fields.
        editItemName.setText("")
        itemIcon.setImageBitmap(BitmapFactory.decodeResource(resources,
                R.drawable.ic_default_image))
        editStockAmount.setText("")
        editStockCapacity.setText("")
        editSupplierPhone.setText("")
        editSupplierEmail.setText("")

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentUri == null) {
            val menuItem = menu.findItem(R.id.action_delete)
            menuItem.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            // Respond to a click on the "Delete" menu option
            R.id.action_delete -> {
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog()
                return true
            }

            // Respond to a click on the "Up" arrow button in the app bar
            android.R.id.home -> {
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                    return true
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                val discardButtonClickListener = DialogInterface.OnClickListener { dialogInterface, i ->
                    // User clicked "Discard" button, navigate to parent activity.
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                }

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed()
            return
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        val discardButtonClickListener = DialogInterface.OnClickListener { dialogInterface, i ->
            // User clicked "Discard" button, close the current activity.
            finish()
        }

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener)
    }

    private fun cropImage(ogBitmap: Bitmap): Bitmap {

        val croppedImage: Bitmap

        val width = ogBitmap.width
        val height = ogBitmap.height

        // Get the 4:3 aspect ratio to compare with Bitmap ratio
        val aspectRatio = 4f / 3f
        val bitmapRatio: Float

        if (width >= height) {
            // this means bitmap is horizontally oriented
            bitmapRatio = width.toFloat() / height.toFloat()

            if (bitmapRatio == aspectRatio) {
                // If the Bitmap is 4:3 don't crop
                croppedImage = ogBitmap
            } else {
                croppedImage = Bitmap.createBitmap(ogBitmap, width / 2 - height - 2, 0, height, height)
            }

        } else {
            // this means bitmap is vertically oriented
            bitmapRatio = height.toFloat() / width.toFloat()
            if (bitmapRatio == aspectRatio) {
                croppedImage = ogBitmap
            } else {
                croppedImage = Bitmap.createBitmap(ogBitmap, 0, height / 2 - width / 2, width, width)
            }
        }

        return croppedImage

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

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     */
    private fun showUnsavedChangesDialog(
            discardButtonClickListener: DialogInterface.OnClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
        builder.setPositiveButton(R.string.discard, discardButtonClickListener)
        builder.setNegativeButton(R.string.keep_editing) { dialog, id ->
            // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the item.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.action_delete) { dialog, id ->
            // User clicked the "Delete" button, so delete the item.
            deletePet()
        }
        builder.setNegativeButton(R.string.action_delete) { dialog, id ->
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the item.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    internal inner class BounceInterpolator(// Using custom BounceInterpolator so we can adjust the amp and freq
            private val mAmplitude: Double, private val mFrequency: Double) : android.view.animation.Interpolator {

        override fun getInterpolation(time: Float): Float {
            return (-1.0 * Math.pow(Math.E, -time / mAmplitude) *
                    Math.cos(mFrequency * time) + 1).toFloat()
        }
    }
}