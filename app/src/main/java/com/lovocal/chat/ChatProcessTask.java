

package com.lovocal.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.widget.Toast;

import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.TableChatMessages;
import com.lovocal.data.TableChats;
import com.lovocal.data.TableMyServices;
import com.lovocal.data.TableUsers;
import com.lovocal.http.HttpConstants;
import com.lovocal.http.JsonUtils;
import com.lovocal.retromodels.request.SendChatRequestModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.DateFormatter;
import com.lovocal.utils.Logger;
import com.lovocal.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import com.lovocal.utils.AppConstants.ChatStatus;
import com.lovocal.utils.AppConstants.ChatType;

/**
 * Runnable implementation to process chat messages
 * TODO this class needs workaround , some refactoring is required currently it is confusing
 */
class ChatProcessTask implements Runnable {

    private static final String TAG = "ChatProcessTask";

    private static final String CHAT_SELECTION = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;


    private static final String USER_SELECTION = DatabaseColumns.ID
            + SQLConstants.EQUALS_ARG;

    public static final int PROCESS_SEND = 1;
    public static final int PROCESS_RECEIVE = 2;

    /**
     * The process type of the task, either {@linkplain #PROCESS_SEND} or
     * {@linkplain #PROCESS_RECEIVE}
     */
    private int mProcessType;

    /**
     * Reference to the context to prepare notifications
     */
    private Context mContext;

    /**
     * The message Model
     */
    private SendChatRequestModel mMessageModel;

    /**
     * Date formatter for formatting chat timestamps
     */
    private DateFormatter mChatDateFormatter;

    /**
     * Date formatter for formatting timestamps for messages
     */
    private DateFormatter mMessageDateFormatter;

    /**
     * Callback for receiving when it is ready to send the chat message
     */
    private SendChatCallback mSendChatCallback;

    /**
     * received message in json string
     */
    private String mMessage;

    /**
     * Callback defined for when the local chat has been saved to the database
     * and the request can be sent
     */
    public static interface SendChatCallback {

        /**
         * Send the chat request
         *
         * @param text    The request body
         * @param dbRowId The row id of the inserted local chat message
         */
        public void sendChat(final SendChatRequestModel messageModel, final long dbRowId);
    }

    private ChatProcessTask() {
        //Private constructor
    }

    private ChatProcessTask(final Context context) {
        mContext = context;
    }

    public int getProcessType() {
        return mProcessType;
    }

    public SendChatRequestModel getMessage() {
        return mMessageModel;
    }

    public DateFormatter getChatDateFormatter() {
        return mChatDateFormatter;
    }

    public DateFormatter getMessageDateFormatter() {
        return mMessageDateFormatter;
    }

    public SendChatCallback getSendChatCallback() {
        return mSendChatCallback;
    }

    @Override
    public void run() {

        if (mProcessType == PROCESS_RECEIVE) {
            processReceivedMessage();
        } else if (mProcessType == PROCESS_SEND) {
            saveMessageAndCallback();
        }

    }

    /**
     * Save a local message in the database, and give a callback to make the
     * chat send request once it is saved
     */
    private void saveMessageAndCallback() {

        try {
            final String senderId = mMessageModel.chat.sender_id;
            final String sentAtTime = mMessageModel.chat.sent_time;

            final String receiverId = mMessageModel.chat.receiver_id;

            final String messageText = mMessageModel.chat.message;

            final String categoryId = mMessageModel.chat.list_cat_id;
            final String serverChatId = mMessageModel.chat.chat_id;

//            String[] columns={DatabaseColumns.ID};
//            String selection=DatabaseColumns.ID+SQLConstants.EQUALS;
//
//            DBInterface.query(true,TableMyServices.NAME,columns,selection,)


            final ContentValues chatValues = new ContentValues(10);

            final String chatId = Utils.generateChatId(receiverId, senderId);

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SERVER_CHAT_ID, serverChatId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, messageText);
            chatValues.put(DatabaseColumns.TIMESTAMP, sentAtTime);
            chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
            chatValues.put(DatabaseColumns.CATEGORY_ID, categoryId);
            chatValues.put(DatabaseColumns.CHAT_STATUS, ChatStatus.SENDING);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter
                    .getEpoch(sentAtTime));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter
                    .getOutputTimestamp(sentAtTime));

            final long insertRowId = DBInterface
                    .insert(TableChatMessages.NAME, null, chatValues, true);

            //services id(sender id for services) will be receiver id of the message in services case
            //we are using receiver id sender id for services when user clicks for service-user chat in chatsFragment
            //so if we update the user with his own message in the case of service-user chat than
            //services id becomes the receiver id and it will send message to own service
            // a little confusing but //TODO we need to change this structure
            if (insertRowId >= 0) {

                //Update or insert the chats table
                if (!checkIdServiceId(senderId)) {
                    final ContentValues values = new ContentValues(7);
                    values.put(DatabaseColumns.CHAT_ID, chatId);
                    values.put(DatabaseColumns.SERVER_CHAT_ID, serverChatId);
                    values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);
                    if (checkIdServiceId(senderId)) {
                        values.put(DatabaseColumns.CHAT_TYPE, ChatType.SERVICE);
                        Logger.d(TAG, "saving as type service");
                    } else {
                        values.put(DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL);
                        Logger.d(TAG, "saving as type user");
                    }
                    values.put(DatabaseColumns.TIMESTAMP, sentAtTime);
                    try {
                        values.put(DatabaseColumns.TIMESTAMP_HUMAN, mChatDateFormatter
                                .getOutputTimestamp(sentAtTime));
                        values.put(DatabaseColumns.TIMESTAMP_EPOCH, mChatDateFormatter
                                .getEpoch(sentAtTime));
                    } catch (ParseException e) {
                        //Shouldn't happen
                    }

                    values.put(DatabaseColumns.ID, receiverId);

                    Logger.v(TAG, "Updating chats for Id %s", chatId);
                    final int updateCount = DBInterface
                            .update(TableChats.NAME, values, CHAT_SELECTION, new String[]{
                                    chatId
                            }, true);

                    if (updateCount == 0) {
                        //Insert the chat message
                        DBInterface.insert(TableChats.NAME, null, values, true);
                    }
                }

                //After finishing the local chat insertion, give a callback to do the actual network call
                mSendChatCallback.sendChat(mMessageModel, insertRowId);
            }
        } catch (ParseException e) {
            Logger.e(TAG, e, "Invalid timestamp");
        }
    }

    /**
     * Processes a received message, stores it in the database
     */
    private void processReceivedMessage() {
        try {
            final JSONObject messageJson = new JSONObject(mMessage);
            //TODO we need some change in backend
            if (!mMessage.contains("Chatter not found")) {


                final JSONObject receiverObject = JsonUtils
                        .readJSONObject(messageJson, HttpConstants.RECEIVER, false, false);

                final JSONObject senderObject = JsonUtils
                        .readJSONObject(messageJson, HttpConstants.SENDER, false, false);

                final String messageText = JsonUtils
                        .readString(messageJson, HttpConstants.MESSAGE, false, false);

                final String serverChatId = JsonUtils
                        .readString(messageJson, HttpConstants.CHAT_ID, false, false);

                final String chatQueryId = JsonUtils
                        .readString(messageJson, HttpConstants.CHAT_QUERY_ID, false, false);

                //TODO use server_sent_time -some parse error

//                final String timestamp = JsonUtils
//                        .readString(messageJson, HttpConstants.SERVER_SENT_TIME, false, false);

                final String timestamp = JsonUtils
                        .readString(messageJson, HttpConstants.SENT_TIME, false, false);

                final String categoryId = JsonUtils
                        .readString(messageJson, HttpConstants.LIST_CAT_ID, false, false);

                final String chatQueryMessage = JsonUtils
                        .readString(messageJson, HttpConstants.CHAT_QUERY_MESSAGE, false, false);

                final String sentAtTime = JsonUtils
                        .readString(messageJson, HttpConstants.SENT_TIME, false, false);

                //sender values
                final String senderId = JsonUtils
                        .readString(senderObject, HttpConstants.SENDER_ID, false, false);
                final String senderType = JsonUtils
                        .readString(senderObject, HttpConstants.SENDER_TYPE, false, false);
                final String senderName = JsonUtils
                        .readString(senderObject, HttpConstants.SENDER_NAME, false, false);
                final String senderImage = JsonUtils
                        .readString(senderObject, HttpConstants.SENDER_IMAGE, false, false);

                //receiver values
                final String receiverId = JsonUtils
                        .readString(receiverObject, HttpConstants.RECEIVER_ID, false, false);
                final String receiverName = JsonUtils
                        .readString(receiverObject, HttpConstants.RECEIVER_NAME, false, false);
                final String receiverImage = JsonUtils
                        .readString(receiverObject, HttpConstants.RECEIVER_IMAGE, false, false);

                final String chatId = Utils.generateChatId(receiverId, senderId);

                final ContentValues chatValues = new ContentValues(7);

                boolean isSenderCurrentUser = senderId
                        .equals(AppConstants.UserInfo.INSTANCE.getId());


                boolean isSenderMyService = checkIdServiceId(senderId);


                chatValues.put(DatabaseColumns.CHAT_ID, chatId);
                chatValues.put(DatabaseColumns.SERVER_CHAT_ID, serverChatId);
                chatValues.put(DatabaseColumns.SENDER_ID, senderId);
                chatValues.put(DatabaseColumns.SENDER_NAME, senderName);
                chatValues.put(DatabaseColumns.SENDER_IMAGE, senderImage);
                chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
                chatValues.put(DatabaseColumns.RECEIVER_NAME, receiverName);
                chatValues.put(DatabaseColumns.RECEIVER_IMAGE, receiverImage);
                chatValues.put(DatabaseColumns.MESSAGE, messageText);
                chatValues.put(DatabaseColumns.TIMESTAMP, timestamp);
                chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
                chatValues.put(DatabaseColumns.CATEGORY_ID, categoryId);
                if (!isSenderMyService) {
                    chatValues.put(DatabaseColumns.CHAT_STATUS, isSenderCurrentUser ? ChatStatus.SENT
                            : ChatStatus.RECEIVED);
                } else {
                    chatValues.put(DatabaseColumns.CHAT_STATUS, isSenderMyService ? ChatStatus.SENT
                            : ChatStatus.RECEIVED);
                }
                    chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter
                            .getEpoch(timestamp));
                chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter
                        .getOutputTimestamp(timestamp));

                if (isSenderCurrentUser) {
                    //Update the locally saved message to mark it as sent

                    //Insert the chat message into DB
                    final String selection = DatabaseColumns.SENDER_ID
                            + SQLConstants.EQUALS_ARG + SQLConstants.AND
                            + DatabaseColumns.SENT_AT
                            + SQLConstants.EQUALS_ARG;

                    final String[] args = new String[]{
                            senderId, sentAtTime
                    };

//                final String selection = DatabaseColumns.SENDER_ID
//                        + SQLConstants.EQUALS_ARG ;
//
//                final String[] args = new String[] {
//                        senderId
//                };
                    Logger.d(TAG, "updated message in current user");

                    DBInterface.update(TableChatMessages.NAME, chatValues, selection, args, true);

                } else if (isSenderMyService) {
                    final String selection = DatabaseColumns.SENDER_ID
                            + SQLConstants.EQUALS_ARG + SQLConstants.AND
                            + DatabaseColumns.SENT_AT
                            + SQLConstants.EQUALS_ARG;

                    final String[] args = new String[]{
                            senderId, sentAtTime
                    };

//                final String selection = DatabaseColumns.SENDER_ID
//                        + SQLConstants.EQUALS_ARG ;
//
//                final String[] args = new String[] {
//                        senderId
//                };
                    Logger.d(TAG, "updated message in services");

                    DBInterface.update(TableChatMessages.NAME, chatValues, selection, args, true);
                } else {

                    Logger.d(TAG, "inserted message");
                    //Insert the message in the db
                    final long insertRowId = DBInterface
                            .insert(TableChatMessages.NAME, null, chatValues, true);

                /*
                 * Parse and store sender info. We will receive messages both
                 * when we send and receive, so we need to check the sender id
                 * if it is our own id first to detect who sent the message
                 */

                    //TODO get user details from the user id
                    // final String senderName = parseAndStoreChatUserInfo(senderId, senderObject);


                    ChatNotificationHelper
                            .getInstance(mContext)
                            .showChatReceivedNotification(mContext, chatId, senderId, receiverId, senderName, messageText);


                    final ContentValues values = new ContentValues(7);
                    values.put(DatabaseColumns.CHAT_ID, chatId);
                    values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);
                    values.put(DatabaseColumns.SERVER_CHAT_ID, serverChatId);
                    values.put(DatabaseColumns.CHAT_QUERY_ID, chatQueryId);
                    values.put(DatabaseColumns.CHAT_TYPE, checkIdServiceId(receiverId) ? ChatType.SERVICE : ChatType.PERSONAL);
                    values.put(DatabaseColumns.ID, isSenderCurrentUser ? receiverId
                            : senderId);
                    if (checkIdServiceId(receiverId)) {
                        Logger.d(TAG, "saving as type service");
                    } else {
                        Logger.d(TAG, "saving as type user");
                    }
                    values.put(DatabaseColumns.TIMESTAMP_HUMAN, mChatDateFormatter
                            .getOutputTimestamp(timestamp));
                    values.put(DatabaseColumns.TIMESTAMP, timestamp);
                    values.put(DatabaseColumns.TIMESTAMP_EPOCH, mChatDateFormatter
                            .getEpoch(timestamp));

                    final int updateCount = DBInterface
                            .update(TableChats.NAME, values, CHAT_SELECTION, new String[]{
                                    chatId
                            }, true);

                    Logger.v(TAG, "Updating chats for Id %s", chatId);


                    if (senderType.equals(AppConstants.USER)) {
                        final ContentValues senderValue = new ContentValues(2);
                        senderValue.put(DatabaseColumns.ID, senderId);
                        senderValue.put(DatabaseColumns.NAME, senderName);
                        final int updateUserCount = DBInterface
                                .update(TableUsers.NAME, senderValue, USER_SELECTION, new String[]{
                                        senderId
                                }, true);
                        if (updateUserCount == 0) {
                            DBInterface.insert(TableUsers.NAME, null, senderValue, true);
                            Logger.d(TAG, "inserted User Value(Sender) ");
                        } else {
                            Logger.d(TAG, "updated User Value(" + senderId + ") ");
                        }

                    }


                    if (updateCount == 0) {
                        DBInterface.insert(TableChats.NAME, null, values, true);

                    }
                }
            } else {
                Logger.d(TAG, "some error in message sending");
            }

        } catch (JSONException e) {
            Logger.e(TAG, e, "Invalid message json");
        } catch (ParseException e) {
            Logger.e(TAG, e, "Invalid timestamp");
        }

    }

    /**
     * Parses the user info of the user who sent the message and updates the
     * local users table
     *
     * @param senderId     The id for the user who sent the chat message
     * @param senderObject The Sender object received in the chat message
     * @return The name of the sender
     * @throws org.json.JSONException If the JSON is invalid
     */
    private String parseAndStoreChatUserInfo(final String senderId,
                                             final JSONObject senderObject) throws JSONException {

        final String senderFirstName = JsonUtils
                .readString(senderObject, HttpConstants.NAME, true, false);

        final ContentValues senderValues = new ContentValues(4);
        senderValues.put(DatabaseColumns.ID, senderId);
        senderValues.put(DatabaseColumns.NAME, senderFirstName);

        final int updateCount = DBInterface
                .update(TableUsers.NAME, senderValues, USER_SELECTION, new String[]{
                        senderId
                }, true);

        if (updateCount == 0) {
            DBInterface.insert(TableUsers.NAME, null, senderValues, true);
        }

        return senderFirstName;
    }

    /**
     * Builder for Chat Process tasks
     */
    public static class Builder {

        private Context mContext;

        private ChatProcessTask mChatProcessTask;

        public Builder(Context context) {
            mContext = context;
            mChatProcessTask = new ChatProcessTask(mContext);
        }

        public Builder setProcessType(int processType) {
            mChatProcessTask.mProcessType = processType;
            return this;
        }

        public Builder setMessage(String message) {
            mChatProcessTask.mMessage = message;
            return this;
        }

        public Builder setMessageModel(SendChatRequestModel messageModel) {
            mChatProcessTask.mMessageModel = messageModel;
            return this;
        }

        public Builder setChatDateFormatter(DateFormatter chatDateFormatter) {
            mChatProcessTask.mChatDateFormatter = chatDateFormatter;
            return this;
        }

        public Builder setMessageDateFormatter(
                DateFormatter messageDateFormatter) {
            mChatProcessTask.mMessageDateFormatter = messageDateFormatter;
            return this;
        }

        public Builder setSendChatCallback(SendChatCallback sendChatCallback) {
            mChatProcessTask.mSendChatCallback = sendChatCallback;
            return this;
        }

        /**
         * Builds the chat process task
         *
         * @return The complete chat process task
         * @throws IllegalStateException If the chat process task is invalid
         */
        public ChatProcessTask build() {

            if (mChatProcessTask.mProcessType != PROCESS_RECEIVE
                    && mChatProcessTask.mProcessType != PROCESS_SEND) {
                throw new IllegalStateException("Invalid process type");
            }


            if (mChatProcessTask.mMessageModel != null) {
                if (TextUtils.isEmpty(mChatProcessTask.mMessageModel.chat.message)) {
                    throw new IllegalStateException("Empty or null message");
                }
            } else {
                if (TextUtils.isEmpty(mChatProcessTask.mMessage)) {
                    throw new IllegalStateException("Empty or null message");
                }
            }

            if (mChatProcessTask.mChatDateFormatter == null) {
                throw new IllegalStateException("No chat date formatter set");
            }

            if (mChatProcessTask.mMessageDateFormatter == null) {
                throw new IllegalStateException("No message date formatter set");
            }

            if (mChatProcessTask.mProcessType == PROCESS_SEND
                    && mChatProcessTask.mSendChatCallback == null) {
                throw new IllegalStateException("No send chat callback set for a send message");
            }

            return mChatProcessTask;
        }

        /**
         * Resets the builder for preparing another chat process task
         */
        public Builder reset() {
            mChatProcessTask = new ChatProcessTask(mContext);
            return this;
        }
    }

    private boolean checkIdServiceId(String id) {
        Cursor cursor = DBInterface.query(true, TableMyServices.NAME, null, null, null, null, null, null, null);


        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (id.equals(cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID)))) {
                return true;
            }
            cursor.moveToNext();
        }
        return false;
    }

}
