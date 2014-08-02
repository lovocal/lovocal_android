
package com.lovocal.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lovocal.utils.Logger;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

import com.lovocal.utils.AppConstants.ChatStatus;

/**
 * @author Anshul Kamboj Table representing a list of chat messages
 */
public class TableChatMessages {

    private static final String TAG = "TableChatMessages";

    public static final String NAME = "CHAT_MESSAGES";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                .join(SQLConstants.COMMA, new String[]{
                        String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SERVER_CHAT_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SENDER_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SENDER_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SENDER_IMAGE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.RECEIVER_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.RECEIVER_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.RECEIVER_IMAGE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CATEGORY_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SENT_AT, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.MESSAGE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_HUMAN, ""),
                        String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.TIMESTAMP_EPOCH, 0),
                        String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.CHAT_STATUS, ChatStatus.FAILED)
                });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    @SuppressLint("UseSparseArrays")
    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                               final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table

        if (oldVersion == 1) {

            /*
             * Drop & recreate the table if upgrading from DB version 1(alpha
             * version)
             */
            db.execSQL(String
                    .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
            create(db);

        }
    }
}
