package com.antarikshc.intrack;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
    public void bindView(final View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView itemName = view.findViewById(R.id.item_name);
        itemStockAmount = view.findViewById(R.id.stock_number);

        TextView saleButton = view.findViewById(R.id.sale_button);
        RelativeLayout orderButton = view.findViewById(R.id.order_button_layout);
        RelativeLayout stockButton = view.findViewById(R.id.stock_button_layout);
        RelativeLayout editButton = view.findViewById(R.id.edit_button_layout);

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

        // Set current stock amount to one of the TextView from TextSwitcher
        itemStockAmount.setCurrentText(stock);

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

                v.startAnimation(buttonClick);

                // CursorAdapter has finished providing ListView the items
                // Our views are currently bind to the latest item of the list
                // bind views again for the current item
                itemStockAmount = view.findViewById(R.id.stock_number);

                // get the TextView of current TextSwitcher
                TextView myText = (TextView) itemStockAmount.getCurrentView();

                Integer currentStock = Integer.parseInt((String) myText.getText());

                // Update both the TextViews.
                itemStockAmount.setText(String.valueOf(currentStock - 1));

            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.order_layout, null);
                final AlertDialog dialog = new AlertDialog.Builder(context).create();

                dialog.setView(dialogView);

                // we don't want title
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.order_layout);

                TextView callButton = dialogView.findViewById(R.id.call_button);
                TextView emailButton = dialogView.findViewById(R.id.mail_button);

                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:0123456789"));
                        context.startActivity(callIntent);

                        dialog.dismiss();
                    }

                });

                emailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, "antarikshc@gmail.com");
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order for Item Name");
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

                String currentStock = (String) ((TextView) itemStockAmount.getCurrentView()).getText();
                editStockAmount.setText(currentStock);

                // Decrement value on key down
                keyDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer currentAmount = Integer.parseInt(String.valueOf(editStockAmount.getText()));
                        editStockAmount.setText(String.valueOf(currentAmount - 1));
                    }
                });

                // Increment value on key up
                keyUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer currentAmount = Integer.parseInt(String.valueOf(editStockAmount.getText()));
                        editStockAmount.setText(String.valueOf(currentAmount + 1));
                    }
                });

                saveStock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Our views are currently bind to the latest item of the list
                        // bind views again for the current item
                        itemStockAmount = view.findViewById(R.id.stock_number);
                        itemStockAmount.setCurrentText(String.valueOf(editStockAmount.getText()));
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent intent = new Intent(context, EditorActivity.class);
                context.startActivity(intent);
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
