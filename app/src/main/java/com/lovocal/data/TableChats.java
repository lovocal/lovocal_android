

package com.lovocal.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lovocal.utils.Logger;

import java.util.Locale;
import com.lovocal.utils.AppConstants.ChatType;


/**
 *  Table representing a list of chats
 */
public class TableChats {

    private static final String TAG  = "TableChats";

    public static final String  NAME = "CHATS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SERVER_CHAT_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_QUERY_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LAST_MESSAGE_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.UNREAD_COUNT, 0),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_HUMAN, ""),
                                String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.TIMESTAMP_EPOCH, 0),
                        });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

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
