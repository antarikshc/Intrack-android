package com.antarikshc.intrack.ui

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import com.antarikshc.intrack.R
import com.antarikshc.intrack.data.InvContract
import com.antarikshc.intrack.data.InvContract.InvEntry
import kotlin.math.cos
import kotlin.math.pow

class InvCursorAdapter internal constructor(context: Context, c: Cursor?)/* flags */ : CursorAdapter(context, c, 0) {

    private var itemStockAmount: TextSwitcher? = null

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {

        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.custom_list, parent, false)

    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {

        // Find individual views that we want to modify in the list item layout
        val itemName = view.findViewById<TextView>(R.id.item_name)

        val itemIcon = view.findViewById<ImageView>(R.id.item_icon)

        itemStockAmount = view.findViewById(R.id.stock_number)
        val itemStockCapacity = view.findViewById<TextView>(R.id.stock_capacity)

        val saleButton = view.findViewById<TextView>(R.id.sale_button)
        val orderButton = view.findViewById<RelativeLayout>(R.id.order_button_layout)
        val stockButton = view.findViewById<RelativeLayout>(R.id.stock_button_layout)
        val editButton = view.findViewById<RelativeLayout>(R.id.edit_button_layout)

        // Set a tag with the current position of cursor
        // To be referenced later in button's click listeners
        val pos = cursor.position
        editButton.tag = pos

        // Cursor sometimes return columns in unordered fashion
        // Get the indices manually ... bruhh
        val id = cursor.getColumnIndex(InvEntry._ID)
        val nameIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_NAME)
        val stockAmountIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_STOCK)
        val stockCapacityIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_CAPACITY)
        val iconColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_ICON)
        val supPhoneIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_PHONE)
        val supEmailIndex = cursor.getColumnIndex(InvEntry.COLUMN_ITEM_SUP_EMAIL)

        // Content URI for current item
        val contentUri = ContentUris.withAppendedId(InvContract.CONTENT_URI, cursor.getInt(id).toLong())

        // Set the Item Name
        itemName.text = cursor.getString(nameIndex)

        if (!cursor.isNull(iconColumnIndex)) {
            // Retrieve blob from cursor and convert to Bitmap
            val imgByte = cursor.getBlob(iconColumnIndex)
            val iconOfItem = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
            // Set the bitmap to ImageView
            itemIcon.setImageBitmap(iconOfItem)
        }


        val stock = cursor.getInt(stockAmountIndex).toString()

        // Set current stock amount to one of the TextView from TextSwitcher
        itemStockAmount!!.setCurrentText(stock)

        // If present, set the capacity of the Stock
        val placeHolderStockCap = cursor.getString(stockCapacityIndex)
        if (cursor.isNull(stockCapacityIndex) || placeHolderStockCap.isEmpty()) {

            // Remove the view
            itemStockCapacity.visibility = View.GONE

        } else {

            itemStockCapacity.text = " / $placeHolderStockCap"

            // Set the stock color depending on capacity
            val stockColor = colorizeStock(context,
                    Integer.parseInt(stock),
                    Integer.parseInt(placeHolderStockCap))

            val stockTextView = itemStockAmount!!.currentView as TextView
            stockTextView.setTextColor(stockColor)
            itemStockCapacity.setTextColor(stockColor)

        }

        // Bounce animation for buttons
        val buttonClick = AnimationUtils.loadAnimation(context, R.anim.bounce)
        val interpolator = BounceInterpolator(0.2, 6.0)
        buttonClick.interpolator = interpolator

        // Animation for Stock amount TextSwitcher
        val inAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        inAnim.duration = 200
        val outAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
        outAnim.duration = 300

        // Set the slide In and slide Out animation to TextSwitcher
        itemStockAmount!!.inAnimation = inAnim
        itemStockAmount!!.outAnimation = outAnim

        saleButton.setOnClickListener { v ->
            // Initiate animation of the button
            v.startAnimation(buttonClick)

            // CursorAdapter has finished providing ListView the items
            // Our views are currently bind to the latest item of the list
            // bind views again for the current item
            itemStockAmount = view.findViewById(R.id.stock_number)

            // get the TextView of current TextSwitcher
            val myText = itemStockAmount!!.currentView as TextView

            val currentStock = Integer.parseInt(myText.text.toString())

            // Update both the TextViews.
            itemStockAmount!!.setText((currentStock - 1).toString())

            // Update Database with decrement in stock
            val values = ContentValues()
            values.put(InvEntry.COLUMN_ITEM_STOCK, currentStock - 1)

            context.contentResolver.update(contentUri, values, null, null)
        }

        orderButton.setOnClickListener { v ->
            // Initiate animation of the button
            v.startAnimation(buttonClick)

            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.order_layout, null)
            val dialog = AlertDialog.Builder(context).create()

            dialog.setView(dialogView)

            // we don't want title
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setContentView(R.layout.order_layout)

            val callButton = dialogView.findViewById<TextView>(R.id.call_button)
            val phoneNum = dialogView.findViewById<TextView>(R.id.phone_num_subtext)
            val emailButton = dialogView.findViewById<TextView>(R.id.mail_button)
            val emailId = dialogView.findViewById<TextView>(R.id.email_num_subtext)
            val noInfo = dialogView.findViewById<TextView>(R.id.no_sup_info)

            var phone: String? = null
            var email: String? = null

            // Store values to the strings if present
            if (!cursor.isNull(supPhoneIndex)) {
                phone = cursor.getString(supPhoneIndex)
                phoneNum.text = "($phone)"
            }
            if (!cursor.isNull(supEmailIndex)) {
                email = cursor.getString(supEmailIndex)
                emailId.text = "($email)"
            }

            // Null checks to hide/show views
            if (phone == null) {
                callButton.visibility = View.GONE
                phoneNum.visibility = View.GONE
            }
            if (email == null) {
                emailButton.visibility = View.GONE
                emailId.visibility = View.GONE
            }
            if (phone == null && email == null) {
                noInfo.visibility = View.VISIBLE
            }

            callButton.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + cursor.getString(supPhoneIndex))
                context.startActivity(callIntent)

                dialog.dismiss()
            }

            emailButton.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto:") // only email apps should handle this
                emailIntent.putExtra(Intent.EXTRA_EMAIL, cursor.getString(supEmailIndex))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order for " + cursor.getString(nameIndex))
                if (emailIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(emailIntent)
                }

                dialog.dismiss()
            }

            dialog.show()
        }

        stockButton.setOnClickListener { v ->
            // Initiate animation of the button
            v.startAnimation(buttonClick)

            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.stock_layout, null)
            val dialog = AlertDialog.Builder(context).create()

            dialog.setView(dialogView)

            // we don't want title
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setContentView(R.layout.stock_layout)

            val editStockAmount = dialogView.findViewById<AppCompatEditText>(R.id.edit_stock_amount)
            val keyDown = dialogView.findViewById<ImageView>(R.id.key_arrow_down)
            val keyUp = dialogView.findViewById<ImageView>(R.id.key_arrow_up)
            val saveStock = dialogView.findViewById<TextView>(R.id.save_stock_button)

            // Yep. Find the view again and update the EditText for Stock amount
            itemStockAmount = view.findViewById(R.id.stock_number)
            val currentStock = (itemStockAmount!!.currentView as TextView).text as String
            editStockAmount.setText(currentStock)

            // Decrement value on key down
            keyDown.setOnClickListener {
                val currentAmount = Integer.parseInt(editStockAmount.text.toString())
                editStockAmount.setText((currentAmount - 1).toString())
            }

            // Increment value on key up
            keyUp.setOnClickListener {
                val currentAmount = Integer.parseInt(editStockAmount.text.toString())
                editStockAmount.setText((currentAmount + 1).toString())
            }

            saveStock.setOnClickListener {
                // Our views are currently bind to the latest item of the list
                // bind views again for the current item
                itemStockAmount = view.findViewById(R.id.stock_number)
                val updatedStock = editStockAmount.text.toString()
                itemStockAmount!!.setCurrentText(updatedStock)

                // Update Database with new stock amount
                val values = ContentValues()
                values.put(InvEntry.COLUMN_ITEM_STOCK, Integer.parseInt(updatedStock))
                context.contentResolver.update(contentUri, values, null, null)

                dialog.dismiss()
            }

            dialog.show()
        }

        editButton.setOnClickListener { v ->
            // Initiate animation of the button
            v.startAnimation(buttonClick)

            // Set the cursor to position obtained from getTag
            cursor.moveToPosition(Integer.parseInt(v.tag.toString()))

            // Create intent for EditorActivity
            val intent = Intent(context, EditorActivity::class.java)

            // Pass URI for current item from the list
            val currentItemUri = ContentUris.withAppendedId(InvContract.CONTENT_URI, cursor.getInt(id).toLong())
            // Set the URI on the data field of the intent
            intent.data = currentItemUri

            context.startActivity(intent)
        }

    }

    // We will indicate if user is running low on stock
    private fun colorizeStock(context: Context, stock: Int, capacity: Int): Int {

        val ratio = stock.toFloat() / capacity.toFloat()

        return when {
            ratio < 0.2 -> ContextCompat.getColor(context, R.color.stock_red_text)
            ratio > 0.8 -> ContextCompat.getColor(context, R.color.stock_green_text)
            else -> ContextCompat.getColor(context, android.R.color.tab_indicator_text)
        }

    }

    internal inner class BounceInterpolator(// Using custom BounceInterpolator so we can adjust the amp and freq
            private val mAmplitude: Double, private val mFrequency: Double) : android.view.animation.Interpolator {

        override fun getInterpolation(time: Float): Float {
            return (-1.0 * Math.E.pow(-time / mAmplitude) *
                    cos(mFrequency * time) + 1).toFloat()
        }
    }

}
