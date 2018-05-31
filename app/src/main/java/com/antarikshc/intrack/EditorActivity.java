package com.antarikshc.intrack;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.antarikshc.intrack.data.InvContract;
import com.antarikshc.intrack.data.InvContract.InvEntry;
import com.antarikshc.intrack.data.InvDbHelper;

import java.io.ByteArrayOutputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ImageView itemIcon;

    TextView editImage;

    TextView itemName;
    AppCompatEditText editItemName;

    TextView stockAmount;
    AppCompatEditText editStockAmount;

    TextView stockCapacity;
    AppCompatEditText editStockCapacity;

    TextView supplierInfo;
    AppCompatEditText editSupplierPhone;
    ImageView supPhoneIcon;
    AppCompatEditText editSupplierEmail;
    ImageView supEmailIcon;

    TextView saveUpdateButton;

    /**
     * Button Click animation as we are using TextView as buttons
     */
    Animation buttonClick;

    // Request codes for Image intents
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int PICK_IMAGE = 2;

    Intent pictureIntent;

    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri mCurrentUri;

    /**
     * to create PetDbHelper instance
     */
    InvDbHelper mDbHelper;

    /**
     * Identifier for the inventory data loader
     */
    private static final int EXISTING_INVENT_LOADER = 0;

    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * Boolean flag that keeps track of whether the Icon has changed or not
     */
    private boolean mItemIconHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new inventory item or editing an existing one.
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        // Bind views
        itemIcon = findViewById(R.id.item_icon);
        editImage = findViewById(R.id.edit_image);
        itemName = findViewById(R.id.item_name_text);
        editItemName = findViewById(R.id.item_name_edit);
        stockAmount = findViewById(R.id.stock_amount_text);
        editStockAmount = findViewById(R.id.stock_amount_edit);
        stockCapacity = findViewById(R.id.stock_capacity_text);
        editStockCapacity = findViewById(R.id.stock_capacity_edit);
        supplierInfo = findViewById(R.id.supplier_info);
        editSupplierPhone = findViewById(R.id.supplier_phone);
        supPhoneIcon = findViewById(R.id.supplier_phone_icon);
        editSupplierEmail = findViewById(R.id.supplier_email);
        supEmailIcon = findViewById(R.id.supplier_email_icon);
        saveUpdateButton = findViewById(R.id.save_update_button);

        // IDK, Maybe it looks good to have a back button in Toolbar
        // Not that anyone will use it xDDD
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // We are primarily working with ImageView and Bitmap.
        // Convert our ic_default_image to Bitmap and set that to ImageView
        Bitmap icon = getBitmapFromVectorDrawable(this, R.drawable.ic_default_image);
        itemIcon.setImageBitmap(icon);

        // If the intent DOES NOT contain a item content URI, then we know that we are
        // creating a new inventory item.
        if (mCurrentUri == null) {
            // This is a new item, so change the app bar to say "Add a Item"
            setTitle(getString(R.string.editor_activity_new_title));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.editor_activity_edit_title));

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENT_LOADER, null, this);

            // We are updating the pet so rename button
            saveUpdateButton.setText(R.string.update_button);
        }

        // Bounce animation for buttons
        buttonClick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        BounceInterpolator interpolator = new BounceInterpolator(0.2, 6);
        buttonClick.setInterpolator(interpolator);

        /**
         * All the Focus change listeners for highlights
         **/
        // Get the dark_blue color for EditText
        final ColorStateList darkPurpleColor = ColorStateList.valueOf(getResources().getColor(R.color.dark_purple_edit_text));

        // Get the light_blue color for TextView
        final ColorStateList lightPurpleColor = ColorStateList.valueOf(getResources().getColor(R.color.light_purple_text_view));

        // Default color for EditText and TextView
        final ColorStateList defaultColor = ColorStateList.valueOf(getResources().getColor(android.R.color.tab_indicator_text));

        editItemName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    itemName.setTextColor(lightPurpleColor);
                    ViewCompat.setBackgroundTintList(v, darkPurpleColor);
                } else {
                    itemName.setTextColor(defaultColor);
                    ViewCompat.setBackgroundTintList(v, defaultColor);
                }
            }
        });

        editStockAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    stockAmount.setTextColor(lightPurpleColor);
                    ViewCompat.setBackgroundTintList(v, darkPurpleColor);
                } else {
                    stockAmount.setTextColor(defaultColor);
                    ViewCompat.setBackgroundTintList(v, defaultColor);
                }
            }
        });

        editStockCapacity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    stockCapacity.setTextColor(lightPurpleColor);
                    ViewCompat.setBackgroundTintList(v, darkPurpleColor);
                } else {
                    stockCapacity.setTextColor(defaultColor);
                    ViewCompat.setBackgroundTintList(v, defaultColor);
                }
            }
        });

        editSupplierPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    supplierInfo.setTextColor(lightPurpleColor);
                    supPhoneIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_purple));
                    ViewCompat.setBackgroundTintList(v, darkPurpleColor);
                } else {
                    supplierInfo.setTextColor(defaultColor);
                    supPhoneIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone));
                    ViewCompat.setBackgroundTintList(v, defaultColor);
                }
            }
        });

        editSupplierEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    supplierInfo.setTextColor(lightPurpleColor);
                    supEmailIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_email_purple));
                    ViewCompat.setBackgroundTintList(v, darkPurpleColor);
                } else {
                    supplierInfo.setTextColor(defaultColor);
                    supEmailIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_email));
                    ViewCompat.setBackgroundTintList(v, defaultColor);
                }
            }
        });

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        editItemName.setOnTouchListener(mTouchListener);
        editStockAmount.setOnTouchListener(mTouchListener);
        editStockCapacity.setOnTouchListener(mTouchListener);
        editSupplierPhone.setOnTouchListener(mTouchListener);
        editSupplierEmail.setOnTouchListener(mTouchListener);

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new InvDbHelper(this);

    }

    public void editImageDialog(View view) {
        // Initiate animation of the button
        view.startAnimation(buttonClick);

        mItemHasChanged = true;

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View dialogView = inflater.inflate(R.layout.edit_image_layout, null);
        final AlertDialog dialog = new AlertDialog.Builder(EditorActivity.this).create();

        dialog.setView(dialogView);

        // we don't want title
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.edit_image_layout);

        // Bind views from dialog layout
        TextView clickPhoto = dialogView.findViewById(R.id.click_photo);
        TextView choosePhoto = dialogView.findViewById(R.id.choose_photo);

        clickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
                }

                dialog.dismiss();
            }
        });

        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pictureIntent.setType("image/*");
                startActivityForResult(pictureIntent, PICK_IMAGE);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Input item from user to store in DB
     **/
    public void saveUpdateItem(View view) {

        // Get the info from EditTexts
        String nameOfItem = editItemName.getText().toString().trim();
        String stockOfItem = editStockAmount.getText().toString().trim();
        String capacityOfItem = editStockCapacity.getText().toString().trim();
        String supPhoneOfItem = editSupplierPhone.getText().toString().trim();
        String supEmailOfItem = editSupplierEmail.getText().toString().trim();

        if (nameOfItem.isEmpty()) {
            Toast.makeText(this, "Provide name for the Item", Toast.LENGTH_SHORT).show();
            return;
        }

        if (stockOfItem.isEmpty()) {
            Toast.makeText(this, "Provide initial value for amount of Stock", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (mCurrentUri == null &&
                TextUtils.isEmpty(nameOfItem) && TextUtils.isEmpty(stockOfItem) &&
                TextUtils.isEmpty(capacityOfItem) && TextUtils.isEmpty(supPhoneOfItem) &&
                TextUtils.isEmpty(supEmailOfItem)) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_ITEM_NAME, nameOfItem);
        values.put(InvEntry.COLUMN_ITEM_STOCK, stockOfItem);
        values.put(InvEntry.COLUMN_ITEM_CAPACITY, capacityOfItem);
        values.put(InvEntry.COLUMN_ITEM_SUP_PHONE, supPhoneOfItem);
        values.put(InvEntry.COLUMN_ITEM_SUP_EMAIL, supEmailOfItem);

        // Only process the Bitmap if user has selected new image
        if (mItemIconHasChanged) {
            Bitmap iconOfItem = ((BitmapDrawable) itemIcon.getDrawable()).getBitmap();
            // Converting Bitmap to ByteArray
            byte[] img = getBitmapAsByteArray(iconOfItem);

            values.put(InvEntry.COLUMN_ITEM_ICON, img);
        }


        // Determine if this is a new or existing item by checking if mCurrentUri is null or not
        if (mCurrentUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InvContract.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
            // Otherwise this is an EXISTING item, so update the pet with content URI: mCurrentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }

    }

    /**
     * Perform the deletion of the item in the db.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing item.
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Bitmap imageBitmap;
        Bitmap croppedImage;

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            try {
                croppedImage = cropImage(imageBitmap);
                itemIcon.setImageBitmap(croppedImage);

                mItemIconHasChanged = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                croppedImage = cropImage(imageBitmap);
                itemIcon.setImageBitmap(croppedImage);

                mItemIconHasChanged = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_ITEM_NAME,
                InvEntry.COLUMN_ITEM_STOCK,
                InvEntry.COLUMN_ITEM_CAPACITY,
                InvEntry.COLUMN_ITEM_ICON,
                InvEntry.COLUMN_ITEM_SUP_PHONE,
                InvEntry.COLUMN_ITEM_SUP_EMAIL
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentUri,         // Query the content URI
                projection,             // Columns to include in the resulting Cursor
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {

            // Cursor sometimes return columns in unordered fashion
            // Get the indices manually ... bruhh
            int id = data.getColumnIndex(InvEntry._ID);
            int nameIndex = data.getColumnIndex(InvEntry.COLUMN_ITEM_NAME);
            int stockAmountIndex = data.getColumnIndex(InvEntry.COLUMN_ITEM_STOCK);
            int stockCapacityIndex = data.getColumnIndex(InvEntry.COLUMN_ITEM_CAPACITY);
            int iconColumnIndex = data.getColumnIndex(InvEntry.COLUMN_ITEM_ICON);
            int supPhoneIndex = data.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_PHONE);
            int supEmailIndex = data.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_EMAIL);

            // Set all the values retrieved from database
            editItemName.setText(data.getString(nameIndex));
            editStockAmount.setText(data.getString(stockAmountIndex));

            if (!data.isNull(stockCapacityIndex)) {
                editStockCapacity.setText(data.getString(stockCapacityIndex));
            }
            if (!data.isNull(supPhoneIndex)) {
                editSupplierPhone.setText(data.getString(supPhoneIndex));
            }
            if (!data.isNull(supEmailIndex)) {
                editSupplierEmail.setText(data.getString(supEmailIndex));
            }

            if (!data.isNull(iconColumnIndex)) {
                // Retrieve blob from cursor and convert to Bitmap
                byte[] imgByte = data.getBlob(iconColumnIndex);
                Bitmap iconOfItem = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
                // Set the bitmap to ImageView
                itemIcon.setImageBitmap(iconOfItem);
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is invalidated, clear out all the data from the input fields.
        editItemName.setText("");
        itemIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_default_image));
        editStockAmount.setText("");
        editStockCapacity.setText("");
        editSupplierPhone.setText("");
        editSupplierEmail.setText("");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private Bitmap cropImage(Bitmap ogBitmap) {

        Bitmap croppedImage;

        int width = ogBitmap.getWidth();
        int height = ogBitmap.getHeight();

        // Get the 4:3 aspect ratio to compare with Bitmap ratio
        float aspectRatio = 4f / 3f;
        float bitmapRatio;

        if (width >= height) {
            // this means bitmap is horizontally oriented
            bitmapRatio = (float) width / (float) height;

            if (bitmapRatio == aspectRatio) {
                // If the Bitmap is 4:3 don't crop
                croppedImage = ogBitmap;
            } else {
                croppedImage = Bitmap.createBitmap(ogBitmap, (width / 2 - height - 2), 0, height, height);
            }

        } else {
            // this means bitmap is vertically oriented
            bitmapRatio = (float) height / (float) width;
            if (bitmapRatio == aspectRatio) {
                croppedImage = ogBitmap;
            } else {
                croppedImage = Bitmap.createBitmap(ogBitmap, 0, (height / 2 - width / 2), width, width);
            }
        }

        return croppedImage;

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

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.action_delete, new DialogInterface.OnClickListener() {
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

    class BounceInterpolator implements android.view.animation.Interpolator {
        // Using custom BounceInterpolator so we can adjust the amp and freq
        private double mAmplitude;
        private double mFrequency;

        BounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
        }
    }
}
