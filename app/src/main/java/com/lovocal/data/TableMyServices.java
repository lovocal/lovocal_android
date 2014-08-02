

package com.lovocal.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lovocal.utils.Logger;

import java.util.Locale;


/**
 * @author Anshul Kamboj Table representing all my services
 */
public class TableMyServices {

    private static final String TAG  = "TableMyServices";

    public static final String  NAME = "MY_SERVICES";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.NAME, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SERVICES_IMAGE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.MOBILE_NUMBER, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LATITUDE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LONGITUDE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.COUNTRY, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CITY, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.STATE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ZIP_CODE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.DESCRIPTION, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CUSTOMERCARE_NUMBER, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LANDLINE_NUMBER, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ADDRESS, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.WEBSITE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TWITTER_LINK, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.FACEBOOK_LINK, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LINKEDIN_LINK, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CATEGORIES, "")

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
