package com.antarikshc.intrack;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.antarikshc.intrack.data.InvContract.InvEntry;

public class InvCursorAdapter extends CursorAdapter {

    TextView itemName;
    TextSwitcher itemStockAmount;

    RelativeLayout orderButton;
    RelativeLayout stockButton;
    RelativeLayout editButton;

    TextView saleButton;

    TextView myText;

    InvCursorAdapter(Context context, Cursor c) {
        super(context, c, /* flags */ 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.custom_list, parent, false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        itemName = view.findViewById(R.id.item_name);
        itemStockAmount = view.findViewById(R.id.stock_number);

        saleButton = view.findViewById(R.id.sale_button);
        orderButton = view.findViewById(R.id.order_button_layout);
        stockButton = view.findViewById(R.id.stock_button_layout);
        editButton = view.findViewById(R.id.edit_button_layout);

        // Cursor sometimes return columns in unordered fashion
        // Get the indices manually ... bruhh
        int nameIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_NAME);
        int stockAmountIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_STOCK);
        int stockCapacityIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_CAPACITY);
        int iconColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_ICON);
        int supPhoneIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_PHONE);
        int supEmailIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_EMAIL);

        itemName.setText(cursor.getString(nameIndex));
        final String stock = String.valueOf(cursor.getInt(stockAmountIndex));
        itemStockAmount.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                myText = new TextView(context);
                myText.setText(stock);
                myText.setTextSize(16);
                return myText;
            }
        });

        // Bounce animation for buttons
        final Animation buttonClick = AnimationUtils.loadAnimation(context, R.anim.bounce);
        BounceInterpolator interpolator = new BounceInterpolator(0.2, 6);
        buttonClick.setInterpolator(interpolator);

        // Animation for Stock amount TextSwitcher
        Animation inAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        inAnim.setDuration(200);
        Animation outAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
        outAnim.setDuration(300);

        itemStockAmount.setInAnimation(inAnim);
        itemStockAmount.setOutAnimation(outAnim);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.startAnimation(buttonClick);
                Integer currentStock = Integer.parseInt((String) myText.getText());
                itemStockAmount.setText(String.valueOf(currentStock - 1));
                myText.setText(String.valueOf(currentStock - 1));

            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Inventory Item", "Order Placed.");
                v.startAnimation(buttonClick);
            }
        });

        stockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Inventory Item", "Stocked up.");
                v.startAnimation(buttonClick);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Inventory Item", "Edit this shit up.");
                v.startAnimation(buttonClick);
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
