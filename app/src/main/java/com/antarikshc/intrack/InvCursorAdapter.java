package com.antarikshc.intrack;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.antarikshc.intrack.data.InvContract;
import com.antarikshc.intrack.data.InvContract.InvEntry;

public class InvCursorAdapter extends CursorAdapter {

    InvCursorAdapter(Context context, Cursor c) {
        super(context, c, /* flags */ 0);
    }

    private TextSwitcher itemStockAmount;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.custom_list, parent, false);

    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView itemName = view.findViewById(R.id.item_name);

        ImageView itemIcon = view.findViewById(R.id.item_icon);

        itemStockAmount = view.findViewById(R.id.stock_number);
        TextView itemStockCapacity = view.findViewById(R.id.stock_capacity);

        TextView saleButton = view.findViewById(R.id.sale_button);
        RelativeLayout orderButton = view.findViewById(R.id.order_button_layout);
        RelativeLayout stockButton = view.findViewById(R.id.stock_button_layout);
        RelativeLayout editButton = view.findViewById(R.id.edit_button_layout);

        // Cursor sometimes return columns in unordered fashion
        // Get the indices manually ... bruhh
        final int id = cursor.getColumnIndex(InvEntry._ID);
        final int nameIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_NAME);
        int stockAmountIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_STOCK);
        int stockCapacityIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_CAPACITY);
        int iconColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_ICON);
        final int supPhoneIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_PHONE);
        final int supEmailIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_EMAIL);

        // Content URI for current item
        final Uri contentUri = ContentUris.withAppendedId(InvContract.CONTENT_URI, cursor.getInt(id));

        // Set the Item Name
        itemName.setText(cursor.getString(nameIndex));

        if (!cursor.isNull(iconColumnIndex)) {
            // Retrieve blob from cursor and convert to Bitmap
            byte[] imgByte = cursor.getBlob(iconColumnIndex);
            Bitmap iconOfItem = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            // Set the bitmap to ImageView
            itemIcon.setImageBitmap(iconOfItem);
        }


        final String stock = String.valueOf(cursor.getInt(stockAmountIndex));

        // Set current stock amount to one of the TextView from TextSwitcher
        itemStockAmount.setCurrentText(stock);

        // If present, set the capacity of the Stock
        String placeHolderStockCap = cursor.getString(stockCapacityIndex);
        if (cursor.isNull(stockCapacityIndex) || placeHolderStockCap.isEmpty()) {

            // Remove the view
            itemStockCapacity.setVisibility(View.GONE);

        } else {

            itemStockCapacity.setText(" / " + placeHolderStockCap);

            // Set the stock color depending on capacity
            int stockColor = colorizeStock(context,
                    Integer.parseInt(stock),
                    Integer.parseInt(placeHolderStockCap));

            TextView stockTextView = (TextView) itemStockAmount.getCurrentView();
            stockTextView.setTextColor(stockColor);
            itemStockCapacity.setTextColor(stockColor);

        }

        // Bounce animation for buttons
        final Animation buttonClick = AnimationUtils.loadAnimation(context, R.anim.bounce);
        BounceInterpolator interpolator = new BounceInterpolator(0.2, 6);
        buttonClick.setInterpolator(interpolator);

        // Animation for Stock amount TextSwitcher
        Animation inAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        inAnim.setDuration(200);
        Animation outAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
        outAnim.setDuration(300);

        // Set the slide In and slide Out animation to TextSwitcher
        itemStockAmount.setInAnimation(inAnim);
        itemStockAmount.setOutAnimation(outAnim);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initiate animation of the button
                v.startAnimation(buttonClick);

                // CursorAdapter has finished providing ListView the items
                // Our views are currently bind to the latest item of the list
                // bind views again for the current item
                itemStockAmount = view.findViewById(R.id.stock_number);

                // get the TextView of current TextSwitcher
                TextView myText = (TextView) itemStockAmount.getCurrentView();

                Integer currentStock = Integer.parseInt(myText.getText().toString());

                // Update both the TextViews.
                itemStockAmount.setText(String.valueOf(currentStock - 1));

                // Update Database with decrement in stock
                ContentValues values = new ContentValues();
                values.put(InvEntry.COLUMN_ITEM_STOCK, currentStock - 1);

                context.getContentResolver().update(contentUri, values, null, null);

            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initiate animation of the button
                v.startAnimation(buttonClick);

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.order_layout, null);
                final AlertDialog dialog = new AlertDialog.Builder(context).create();

                dialog.setView(dialogView);

                // we don't want title
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.order_layout);

                TextView callButton = dialogView.findViewById(R.id.call_button);
                TextView phoneNum = dialogView.findViewById(R.id.phone_num_subtext);
                TextView emailButton = dialogView.findViewById(R.id.mail_button);
                TextView emailId = dialogView.findViewById(R.id.email_num_subtext);
                TextView noInfo = dialogView.findViewById(R.id.no_sup_info);

                String phone = null;
                String email = null;

                // Store values to the strings if present
                if (!cursor.isNull(supPhoneIndex)) {
                    phone = cursor.getString(supPhoneIndex);
                    phoneNum.setText("(" + phone + ")");
                }
                if (!cursor.isNull(supEmailIndex)) {
                    email = cursor.getString(supEmailIndex);
                    emailId.setText("(" + email + ")");
                }

                // Null checks to hide/show views
                if (phone == null) {
                    callButton.setVisibility(View.GONE);
                    phoneNum.setVisibility(View.GONE);
                }
                if (email == null) {
                    emailButton.setVisibility(View.GONE);
                    emailId.setVisibility(View.GONE);
                }
                if (phone == null && email == null) {
                    noInfo.setVisibility(View.VISIBLE);
                }

                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + cursor.getString(supPhoneIndex)));
                        context.startActivity(callIntent);

                        dialog.dismiss();
                    }

                });

                emailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, cursor.getString(supEmailIndex));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order for " + cursor.getString(nameIndex));
                        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(emailIntent);
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        stockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initiate animation of the button
                v.startAnimation(buttonClick);

                LayoutInflater inflater = LayoutInflater.from(context);
                final View dialogView = inflater.inflate(R.layout.stock_layout, null);
                final AlertDialog dialog = new AlertDialog.Builder(context).create();

                dialog.setView(dialogView);

                // we don't want title
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.stock_layout);

                final EditText editStockAmount = dialogView.findViewById(R.id.edit_stock_amount);
                ImageView keyDown = dialogView.findViewById(R.id.key_arrow_down);
                ImageView keyUp = dialogView.findViewById(R.id.key_arrow_up);
                TextView saveStock = dialogView.findViewById(R.id.save_stock_button);

                // Yep. Find the view again and update the EditText for Stock amount
                itemStockAmount = view.findViewById(R.id.stock_number);
                String currentStock = (String) ((TextView) itemStockAmount.getCurrentView()).getText();
                editStockAmount.setText(currentStock);

                // Decrement value on key down
                keyDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer currentAmount = Integer.parseInt((editStockAmount.getText().toString()));
                        editStockAmount.setText(String.valueOf(currentAmount - 1));
                    }
                });

                // Increment value on key up
                keyUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer currentAmount = Integer.parseInt(editStockAmount.getText().toString());
                        editStockAmount.setText(String.valueOf(currentAmount + 1));
                    }
                });

                saveStock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Our views are currently bind to the latest item of the list
                        // bind views again for the current item
                        itemStockAmount = view.findViewById(R.id.stock_number);
                        String updatedStock = editStockAmount.getText().toString();
                        itemStockAmount.setCurrentText(updatedStock);

                        // Update Database with new stock amount
                        ContentValues values = new ContentValues();
                        values.put(InvEntry.COLUMN_ITEM_STOCK, Integer.parseInt(updatedStock));
                        context.getContentResolver().update(contentUri, values, null, null);

                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initiate animation of the button
                v.startAnimation(buttonClick);

                // Create intent for EditorActivity
                Intent intent = new Intent(context, EditorActivity.class);

                // Pass URI for current item from the list
                Uri currentItemUri = ContentUris.withAppendedId(InvContract.CONTENT_URI, cursor.getInt(id));
                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);

                context.startActivity(intent);
            }
        });

    }

    // We will indicate if user is running low on stock
    private int colorizeStock(Context context, Integer stock, Integer capacity) {

        float ratio = stock.floatValue() / capacity.floatValue();

        if (ratio < 0.2) {
            return ContextCompat.getColor(context, R.color.stock_red_text);
        } else if (ratio > 0.8) {
            return ContextCompat.getColor(context, R.color.stock_green_text);
        } else {
            return ContextCompat.getColor(context, android.R.color.tab_indicator_text);
        }

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
