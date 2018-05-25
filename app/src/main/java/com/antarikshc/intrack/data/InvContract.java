package com.antarikshc.intrack.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InvContract {

    private InvContract() {
    }

    //Inv, Invent, Inventory it's all same

    /**
     * Constants for ContentProvider
     **/
    public static final String CONTENT_AUTHORITY = "com.antarikshc.intrack";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public final static String PATH_INVENT = "invent";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENT);

    public static abstract class InvEntry implements BaseColumns {

        /**
         * Constants for invent table
         **/
        public final static String TABLE_NAME = "invent";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME = "name";
        public final static String COLUMN_ITEM_STOCK = "stock";
        public final static String COLUMN_ITEM_CAPACITY = "capacity";
        public final static String COLUMN_ITEM_ICON = "icon";
        public final static String COLUMN_ITEM_SUP_PHONE = "supplier_phone";
        public final static String COLUMN_ITEM_SUP_EMAIL = "supplier_email";


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENT;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a inventory item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENT;


    }

}
