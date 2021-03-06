

package com.lovocal.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lovocal.utils.Logger;

import java.util.Locale;


/**
 * @author Anshul Kamboj Table representing all the categories
 */
public class TableCategories {

    private static final String TAG  = "TableCategories";

    public static final String  NAME = "CATEGORIES";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CATEGORY_NAME, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CATEGORY_IMAGE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.DESCRIPTION, "")
                        });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

      //Add any data migration code here. Default is to drop and rebuild the table

        if (oldVersion == 1) {
            
            /* Drop & recreate the table if upgrading from DB version 1(alpha version) */
            db.execSQL(String
                            .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
            create(db);

        }
    }
}
