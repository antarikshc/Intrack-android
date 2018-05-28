package com.antarikshc.intrack;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class EditorActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

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
