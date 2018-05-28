package com.antarikshc.intrack;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class EditorActivity extends AppCompatActivity {

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

    // ID for Image intents
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int PICK_IMAGE = 2;

    Intent pictureIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

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

        // Bounce animation for buttons
        final Animation buttonClick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        BounceInterpolator interpolator = new BounceInterpolator(0.2, 6);
        buttonClick.setInterpolator(interpolator);

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

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
        });

        /**
         * All the Focus change listeners for highlights
         **/
        // Get the dark_blue color for EditText
        final ColorStateList darkBlueColor = ColorStateList.valueOf(getResources().getColor(R.color.dark_blue_edit_text));

        // Get the light_blue color for TextView
        final ColorStateList lightBlueColor = ColorStateList.valueOf(getResources().getColor(R.color.light_blue_text_view));

        // Default color for EditText and TextView
        final ColorStateList defaultColor = ColorStateList.valueOf(getResources().getColor(android.R.color.tab_indicator_text));

        editItemName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    itemName.setTextColor(lightBlueColor);
                    ViewCompat.setBackgroundTintList(v, darkBlueColor);
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
                    stockAmount.setTextColor(lightBlueColor);
                    ViewCompat.setBackgroundTintList(v, darkBlueColor);
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
                    stockCapacity.setTextColor(lightBlueColor);
                    ViewCompat.setBackgroundTintList(v, darkBlueColor);
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
                    supplierInfo.setTextColor(lightBlueColor);
                    supPhoneIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_blue));
                    ViewCompat.setBackgroundTintList(v, darkBlueColor);
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
                    supplierInfo.setTextColor(lightBlueColor);
                    supEmailIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_email_blue));
                    ViewCompat.setBackgroundTintList(v, darkBlueColor);
                } else {
                    supplierInfo.setTextColor(defaultColor);
                    supEmailIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_email));
                    ViewCompat.setBackgroundTintList(v, defaultColor);
                }
            }
        });

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
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                croppedImage = cropImage(imageBitmap);
                itemIcon.setImageBitmap(croppedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
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
