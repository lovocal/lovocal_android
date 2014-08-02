
package com.lovocal.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lovocal.utils.Logger;

import java.util.Locale;

/**
 * View representation for chats, messages and users
 */
public class ViewChatsWithMessagesAndUsers {

    private static final String TAG = "ViewChatsWithMessagesAndUsers";

    //Aliases for the tables
    private static final String ALIAS_CHATS = "A";
    private static final String ALIAS_CHAT_MESSAGES = "B";
    private static final String ALIAS_SERVICES = "C";
    private static final String ALIAS_USERS = "D";

    public static final String NAME = "CHATS_WITH_CHAT_MESSAGES_AND_USERS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                .join(",", new String[]{
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, BaseColumns._ID),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.CHAT_ID),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.ID),
                        DatabaseColumns.CHAT_TYPE,
                        DatabaseColumns.RECEIVER_ID,
                        DatabaseColumns.CATEGORY_ID,
                        DatabaseColumns.SENDER_IMAGE,
                        DatabaseColumns.RECEIVER_IMAGE,
                        DatabaseColumns.NAME,
                        DatabaseColumns.UNREAD_COUNT,
                        DatabaseColumns.MESSAGE,
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.TIMESTAMP_HUMAN),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.TIMESTAMP_EPOCH),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.TIMESTAMP)
                });

        Logger.d(TAG, "View Column Def: %s", columnDef);

        final String fromDef = TextUtils
                .join(",", new String[]{
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableChats.NAME, ALIAS_CHATS),
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableChatMessages.NAME, ALIAS_CHAT_MESSAGES),
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableServices.NAME, ALIAS_SERVICES),

                });
        Logger.d(TAG, "From Def: %s", fromDef);

        final String fromDef2 = TextUtils
                .join(",", new String[]{
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableChats.NAME, ALIAS_CHATS),
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableChatMessages.NAME, ALIAS_CHAT_MESSAGES),
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableUsers.NAME, ALIAS_USERS)

                });


        final String whereDef = String
                .format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.LAST_MESSAGE_ID)
                + SQLConstants.EQUALS
                + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHAT_MESSAGES, BaseColumns._ID)
                + SQLConstants.AND
                + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.ID)
                + SQLConstants.EQUALS
                + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_SERVICES, DatabaseColumns.ID);

        final String whereDef2 = String
                .format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.LAST_MESSAGE_ID)
                + SQLConstants.EQUALS
                + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHAT_MESSAGES, BaseColumns._ID)
                + SQLConstants.AND
                + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.ID)
                + SQLConstants.EQUALS
                + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_USERS, DatabaseColumns.ID);

        Logger.d(TAG, "Where Def: %s", whereDef);

        final String selectDef = String
                .format(Locale.US, SQLConstants.SELECT_FROM_WHERE, columnDef, fromDef, whereDef);

        final String selectDef2=String
                .format(Locale.US, SQLConstants.SELECT_FROM_WHERE,columnDef,fromDef2,whereDef2);


        Logger.d(TAG, "Select Def: %s", selectDef);
        db.execSQL(String
                .format(Locale.US, SQLConstants.CREATE_VIEW, NAME, selectDef+" UNION "+selectDef2));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                               final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table

        if (oldVersion < 4) {

            /*
             * Drop & recreate the view if upgrading from DB version 1(alpha
             * version)
             */
            db.execSQL(String
                    .format(Locale.US, SQLConstants.DROP_VIEW_IF_EXISTS, NAME));
            create(db);

        }
    }
}
