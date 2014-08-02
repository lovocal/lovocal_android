
package com.lovocal.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import com.lovocal.utils.AppConstants.DeviceInfo;

/**
 * Utility methods for Lavocal
 */
public class Utils {

    private static final String TAG = "Utils";



    /**
     * Checks if the current thread is the main thread or not
     *
     * @return <code>true</code> if the current thread is the main/UI thread, <code>false</code>
     * otherwise
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * Makes an SHA1 Hash of the given string
     *
     * @param string The string to shash
     * @return The hashed string
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String sha1(final String string)
            throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        final byte[] data = digest.digest(string.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    /**
     * Reads the network info from service and sets up the singleton
     */
    public static void setupNetworkInfo(final Context context) {

        final ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            DeviceInfo.INSTANCE.setNetworkConnected(activeNetwork
                    .isConnectedOrConnecting());
            DeviceInfo.INSTANCE.setCurrentNetworkType(activeNetwork.getType());
        } else {
            DeviceInfo.INSTANCE.setNetworkConnected(false);
            DeviceInfo.INSTANCE
                    .setCurrentNetworkType(ConnectivityManager.TYPE_DUMMY);
        }

        Logger.d(TAG, "Network State Updated Connected: %b Type: %d", DeviceInfo.INSTANCE
                .isNetworkConnected(), DeviceInfo.INSTANCE
                .getCurrentNetworkType());
    }

    public static boolean copyFile(final File src, final File dst) {
        boolean returnValue = true;

        FileChannel inChannel = null, outChannel = null;

        try {

            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();

        } catch (final FileNotFoundException fnfe) {

            Logger.d(TAG, "inChannel/outChannel FileNotFoundException");
            fnfe.printStackTrace();
            return false;
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);

        } catch (final IllegalArgumentException iae) {

            Logger.d(TAG, "TransferTo IllegalArgumentException");
            iae.printStackTrace();
            returnValue = false;

        } catch (final NonReadableChannelException nrce) {

            Logger.d(TAG, "TransferTo NonReadableChannelException");
            nrce.printStackTrace();
            returnValue = false;

        } catch (final NonWritableChannelException nwce) {

            Logger.d(TAG, "TransferTo NonWritableChannelException");
            nwce.printStackTrace();
            returnValue = false;

        } catch (final ClosedByInterruptException cie) {

            Logger.d(TAG, "TransferTo ClosedByInterruptException");
            cie.printStackTrace();
            returnValue = false;

        } catch (final AsynchronousCloseException ace) {

            Logger.d(TAG, "TransferTo AsynchronousCloseException");
            ace.printStackTrace();
            returnValue = false;

        } catch (final ClosedChannelException cce) {

            Logger.d(TAG, "TransferTo ClosedChannelException");
            cce.printStackTrace();
            returnValue = false;

        } catch (final IOException ioe) {

            Logger.d(TAG, "TransferTo IOException");
            ioe.printStackTrace();
            returnValue = false;

        } finally {

            if (inChannel != null) {
                try {

                    inChannel.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return returnValue;
    }

    /**
     * Generate a user's name from the first name last name
     *
     * @param firstName
     * @param lastName
     * @return
     */
    public static String makeUserFullName(String firstName, String lastName) {

        if (TextUtils.isEmpty(firstName)) {
            return "";
        }

        final StringBuilder builder = new StringBuilder(firstName);

        if (!TextUtils.isEmpty(lastName)) {
            builder.append(" ").append(lastName);
        }
        return builder.toString();
    }


    /**
     * Generates as chat ID which will be unique for a given sender/receiver pair
     *
     * @param receiverId The receiver of the chat
     * @param senderId   The sender of the chat
     * @return The chat Id
     */
    public static String generateChatId(final String receiverId,
                                        final String senderId) {

        /*
         * Method of generating the chat ID is simple. First we compare the two
         * ids and combine them in ascending order separate by a '#'. Then we
         * SHA1 the result to make the chat id
         */

        String combined = null;
        if (receiverId.compareTo(senderId) < 0) {
            combined = String
                    .format(Locale.US, AppConstants.CHAT_ID_FORMAT, receiverId, senderId);
        } else {
            combined = String
                    .format(Locale.US, AppConstants.CHAT_ID_FORMAT, senderId, receiverId);
        }

        String hashed = null;

        try {
            hashed = Utils.sha1(combined);
        } catch (final NoSuchAlgorithmException e) {
            /*
             * Shouldn't happen sinch SHA-1 is standard, but in case it does use
             * the combined string directly since they are local chat IDs
             */
            hashed = combined;
        }

        return hashed;
    }

    /**
     * Gets the distance between two Locations(in metres)
     *
     * @param start The start location
     * @param end   The end location
     * @return The distance between two locations(in metres)
     */
    public static float distanceBetween(final Location start, final Location end) {

        final float[] results = new float[1];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(), end
                .getLatitude(), end.getLongitude(), results);
        return results[0];
    }

    /**
     * Gets the current epoch time. Is dependent on the device's H/W time.
     */
    public static long getCurrentEpochTime() {

        return System.currentTimeMillis() / 1000;
    }


    /**
     * Converts a cursor to a bundle. Field datatypes will be maintained. Floats will be stored in
     * the Bundle as Doubles, and Integers will be stored as Longs due to Cursor limitationcs
     *
     * @param cursor The cursor to convert to a Bundle. This must be positioned to the row to be
     *               read
     * @return The converted bundle
     */
    public static Bundle cursorToBundle(Cursor cursor) {

        final int columnCount = cursor.getColumnCount();
        final Bundle bundle = new Bundle(columnCount);

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {

            final String columnName = cursor.getColumnName(columnIndex);
            switch (cursor.getType(columnIndex)) {

                case Cursor.FIELD_TYPE_STRING: {
                    bundle.putString(columnName, cursor.getString(columnIndex));
                    break;
                }

                case Cursor.FIELD_TYPE_BLOB: {
                    bundle.putByteArray(columnName, cursor.getBlob(columnIndex));
                    break;
                }

                case Cursor.FIELD_TYPE_FLOAT: {
                    bundle.putDouble(columnName, cursor.getDouble(columnIndex));
                    break;
                }

                case Cursor.FIELD_TYPE_INTEGER: {
                    bundle.putLong(columnName, cursor.getLong(columnIndex));
                    break;
                }
            }
        }

        return bundle;
    }

}
