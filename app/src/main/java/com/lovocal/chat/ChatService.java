
package com.lovocal.chat;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.widget.Toast;

import com.lovocal.LavocalApplication;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lovocal.bus.BindExchangeUpdate;
import com.lovocal.chat.ChatRabbitMQConnector.OnReceiveMessageHandler;
import com.lovocal.chat.AbstractRabbitMQConnector.OnDisconnectCallback;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DBInterface.AsyncDbQueryCallback;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.TableMyServices;
import com.lovocal.http.Api;
import com.lovocal.http.HttpConstants;
import com.lovocal.retromodels.request.SendBroadcastChatRequestModel;
import com.lovocal.retromodels.request.SendChatRequestModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.DateFormatter;
import com.lovocal.utils.AppConstants.DeviceInfo;
import com.lovocal.utils.AppConstants.UserInfo;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.lovocal.utils.Logger;
import com.lovocal.utils.SharedPreferenceHelper;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Bound service to send and receive chat messages. The service will receive chat messages and
 * update them in the chats database. <br/> <br/> This service needs to be triggered in two cases -
 * <ol> <li>On application launch - This is done in {@link LavocalApplication#onCreate()}</li>
 * <li>On network connectivity resumed(if it was lost) - This is done in {@link
 * com.lovocal.http.NetworkChangeReceiver#onReceive(android.content.Context, android.content.Intent)}</li> </ol> <br/> This will take care of keeping
 * it tied to the chat server and listening for messages. <br/> <br/> For publishing messages,
 * however, you need to bind to this service, check if chat is connected and then publish the
 * message
 *
 */
public class ChatService extends Service implements OnReceiveMessageHandler,
        Callback, OnDisconnectCallback, AsyncDbQueryCallback {
    private static final String TAG                      = "ChatService";
    private static final String QUEUE_NAME_FORMAT        = "%squeue";
    private static final String VIRTUAL_HOST             = "/";
    private static final String EXCHANGE_NAME_FORMAT     = "%sexchange";

    //currently we are using the same barterli server
    private static final String USERNAME                 = "barterli";
    private static final String PASSWORD                 = "barter";
    /**
     * Minimum time interval(in seconds) to wait between subsequent connect attempts
     */
    private static final int    CONNECT_BACKOFF_INTERVAL = 5;

    /**
     * Maximum multiplier for the connect interval
     */
    private static final int MAX_CONNECT_MULTIPLIER = 180;

    private static final String MESSAGE_SELECT_BY_ID = BaseColumns._ID
            + SQLConstants.EQUALS_ARG;

    private final IBinder mChatServiceBinder = new ChatServiceBinder();

    /** {@link ChatRabbitMQConnector} instance for listening to messages */
    private ChatRabbitMQConnector mMessageConsumer;
    private ChatRabbitMQConnector[] mServiceConsumers;

    private DateFormatter mChatDateFormatter;

    private DateFormatter mMessageDateFormatter;

    private String mQueueName;

    /**
     * Current multiplier for connecting to chat. Can vary between 0 to {@link
     * #MAX_CONNECT_MULTIPLIER}
     */
    private int                    mCurrentConnectMultiplier;
    /**
     * Task to connect to Rabbit MQ Chat server
     */
    private ConnectToChatAsyncTask mConnectTask;

    private Handler mHandler;

    private Runnable mConnectRunnable;

    private ChatProcessTask.Builder mChatProcessTaskBuilder;

    private RestAdapter mRestAdapter;

    private Api         mApiService;

    /**
     * Single thread executor to process incoming chat messages in a queue
     */
    private ExecutorService mChatProcessor;

    /**
     * Holds the reference to the otto bus from LavocalApplication
     */

    private Bus mbus;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferenceHelper.registerSharedPreferencesChangedListener(
                ChatNotificationHelper.getInstance(this).getOnSharedPreferenceChangeListener());
        mChatDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                                               AppConstants.CHAT_TIME_FORMAT);
        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                                                  AppConstants.MESSAGE_TIME_FORMAT);
        mRestAdapter = ((LavocalApplication) getApplication()).getRestAdapter();
        mApiService = ((LavocalApplication)getApplication()).getService(mRestAdapter);

        mCurrentConnectMultiplier = 0;
        mHandler = new Handler();
        mChatProcessor = Executors.newSingleThreadExecutor();
        mChatProcessTaskBuilder = new ChatProcessTask.Builder(this);

        ((LavocalApplication) getApplication().getApplicationContext()).getBus().register(this);

    }

    /**
     * Sets the id of the user the current chat is being done with. Set this to the user id when the
     * chat detail screen opens, and clear it when the screen is paused. It is used to hide
     * notifications when the chat message received is from the user currently being chatted with
     *
     * @param currentChattingUserId The id of the current user being chatted with
     */
    public void setCurrentChattingUserId(final String currentChattingUserId) {
        ChatNotificationHelper.getInstance(this)
                              .setCurrentChattingUserId(currentChattingUserId);
    }

    @Override
    public void success(Object o, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

    }

    /**
     * Binder to connect to the Chat Service
     *
     */
    public class ChatServiceBinder extends Binder {

        public ChatService getService() {
            return ChatService.this;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mChatServiceBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {

        final String action = intent != null ? intent.getAction() : null;

        if ((action != null)
                && action.equals(AppConstants.ACTION_DISCONNECT_CHAT)) {

            if (isConnectedToChat()) {

                mMessageConsumer.dispose(true);
                mMessageConsumer = null;
                for(int i=0;i<mServiceConsumers.length;i++) {
                    mServiceConsumers[i].dispose(true);
                    mServiceConsumers[i]=null;
                }

            }
        }

        else {
            mCurrentConnectMultiplier = 0;
            initMessageConsumer();
            String[] columns={DatabaseColumns.ID};

            DBInterface.queryAsync(AppConstants.QueryTokens.GET_SERVICE_IDS,getTaskTag(),
                    null,true,TableMyServices.NAME,columns,null,null,null,null,null,null,ChatService.this);

            connectChatService();
        }

        return START_STICKY;
    }

    /**
     * Connects to the Chat Service
     */
    private void connectChatService() {

        //If there already is a pending connect task, remove it since we have a newer one
        if (mConnectRunnable != null) {
            mHandler.removeCallbacks(mConnectRunnable);
        }

        if(mMessageConsumer==null){
            Logger.e(TAG,"should not happen");
        }
        else {

            if (isLoggedIn() && !mMessageConsumer.isRunning()) {

                mConnectRunnable = new Runnable() {

                    @Override
                    public void run() {

                        if (!isLoggedIn()
                                || !DeviceInfo.INSTANCE
                                .isNetworkConnected()) {

                            //If there is no internet connection or we are not logged in, we need not attempt to connect
                            mConnectRunnable = null;
                            return;
                        }

                        mQueueName = generateQueueNameFromUserId(UserInfo.INSTANCE.getId());

                        if (mConnectTask == null) {
                            mConnectTask = new ConnectToChatAsyncTask();
                            mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                    .getId());
                        } else {
                            final Status connectingStatus = mConnectTask
                                    .getStatus();

                            if (connectingStatus != Status.RUNNING) {

                                // We are not already attempting to connect, let's try connecting
                                if (connectingStatus == Status.PENDING) {
                                    //Cancel a pending task
                                    mConnectTask.cancel(false);
                                }

                                mConnectTask = new ConnectToChatAsyncTask();
                                mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                        .getId());
                            }
                        }
                        mConnectRunnable = null;

                    }

                };

                mHandler.postDelayed(mConnectRunnable, mCurrentConnectMultiplier
                        * CONNECT_BACKOFF_INTERVAL * 1000);
                mCurrentConnectMultiplier = (++mCurrentConnectMultiplier > MAX_CONNECT_MULTIPLIER) ? MAX_CONNECT_MULTIPLIER
                        : mCurrentConnectMultiplier;
            }
        }

    }

    /**
     * Check if user is logged in or not
     */
    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }

    @Override
    public void onDestroy() {
        SharedPreferenceHelper.unregisterSharedPreferencesChangedListener(
                ChatNotificationHelper.getInstance(this).getOnSharedPreferenceChangeListener());
        if (isConnectedToChat()) {
            mMessageConsumer.dispose(true);
            mMessageConsumer = null;
            for(int i=0;i<mServiceConsumers.length;i++) {
                mServiceConsumers[i].dispose(true);
                mServiceConsumers[i]=null;
            }
        }
        mChatProcessor.shutdownNow();
        super.onDestroy();
    }


    /**
     * Is the chat service connected or not
     */
    public boolean isConnectedToChat() {

        return (mMessageConsumer != null) && mMessageConsumer.isRunning();
    }

    /**
     * Send a message to a user
     *
     * @param toUserId The user Id to send the message to
     * @param message  The message to send
     */
    public void sendMessageToUser(final String toUserId,final String fromUserId,String senderType, final String message,
                                  final String timeSentAt,final String categoryId,final String chatId) {

        if (!isLoggedIn()) {
            return;
        }


            //TODO get values for the left over fields
            SendChatRequestModel chatRequestModel=new SendChatRequestModel();
            String receiverType;
            if(senderType.equals(AppConstants.SERVICE)){
                receiverType=AppConstants.USER;
            }
            else
            {
                receiverType=AppConstants.SERVICE;
            }

            chatRequestModel.chat.setReceiver_id(toUserId);
            chatRequestModel.chat.setSent_time(timeSentAt);
            chatRequestModel.chat.setChat_id(chatId);
            chatRequestModel.chat.setMessage(message);
            chatRequestModel.chat.setSender_type(senderType);
            chatRequestModel.chat.setReceiver_type(receiverType);
            chatRequestModel.chat.setListing_category(categoryId);
            chatRequestModel.chat.setSender_id(fromUserId);

            final ChatProcessTask chatProcessTask = mChatProcessTaskBuilder
                    .setProcessType(ChatProcessTask.PROCESS_SEND)
                    .setMessageModel(chatRequestModel)
                    .setMessageDateFormatter(mMessageDateFormatter)
                    .setChatDateFormatter(mChatDateFormatter)
                    .setSendChatCallback(new ChatProcessTask.SendChatCallback() {

                        @Override
                        public void sendChat(SendChatRequestModel messageModel,

                                             long dbRowId) {

                        final SendChatRequestModel model=messageModel;
                            //Post on main thread
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    mApiService.sendChat(model,ChatService.this);
                                }
                            });
                        }
                    }).build();

            mChatProcessTaskBuilder.reset();
            mChatProcessor.submit(chatProcessTask);
    }



    /**
     * Cancels any notifications being displayed. Call this if the relevant screen is opened within
     * the app
     */
    public void clearChatNotifications() {

        ChatNotificationHelper.getInstance(this).clearChatNotifications();
    }

    /**
     * Set Chat screen currently visible to the user
     *
     * @param visible <code>true</code> to set chat screen visible to the user, <code>false</code>
     *                to disable them
     */
    public void setChatScreenVisible(final boolean visible) {
        ChatNotificationHelper.getInstance(this)
                              .setChatScreenVisible(visible);
    }

    /**
     * Uses the portion of the user's email before the "@" to generate the queue name
     *
     * @param userId  The user Id
     * @return The queue name for the user email
     */
    private String generateQueueNameFromUserId(
            final String userId) {

        return String.format(Locale.US, QUEUE_NAME_FORMAT, userId);

    }

    @Override
    public void onReceiveMessage(final byte[] message) {

        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);
            Logger.d(TAG, "Received:" + text);
            //TODO
//
            final ChatProcessTask chatProcessTask = mChatProcessTaskBuilder
                    .setProcessType(ChatProcessTask.PROCESS_RECEIVE)
                    .setMessage(text)
                    .setChatDateFormatter(mChatDateFormatter)
                    .setMessageDateFormatter(mMessageDateFormatter)
                    .build();
            mChatProcessTaskBuilder.reset();
            mChatProcessor.submit(chatProcessTask);

        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            //Shouldn't be happening
        }

    }

    /**
     * Asynchronously connect to Chat Server TODO: Move the connect async task to the Rabbit MQ
     * Connector The execute() call requires 4 string params - The username, password, queue name in
     * the same order. All parameters should be passed. Send an EMPTY STRING if not required
     *
     */
    private class ConnectToChatAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {

            //Validation
            assert (params != null);
            assert (params.length == 3);
            assert (params[0] != null);
            assert (params[1] != null);
            assert (params[2] != null);
            Logger.v(TAG, "Username %s, Password %s, Queue %s", params[0], params[1], params[2]);
            mMessageConsumer
                    .connectToRabbitMQ(params[0], params[1], params[2], true, false, false, null);

            for(int i=0;i<mServiceConsumers.length;i++){
                mServiceConsumers[i].connectToRabbitMQ(params[0], params[1], params[2], true, false, false, null);
            }

            return null;
        }


        @Override
        protected void onPostExecute(final Void result) {
            if (!isConnectedToChat()) {
                /* If it's not connected, try connecting again */
                connectChatService();
            } else {

                mCurrentConnectMultiplier = 0;
            }
        }
    }

//    @Override
//    public void onPreExecute(final IBlRequestContract request) {
//
//    }
//
//    @Override
//    public void onPostExecute(final IBlRequestContract request) {
//
//    }
//
//    @Override
//    public void onSuccess(final int requestId,
//                          final IBlRequestContract request,
//                          final ResponseInfo response) {
//
//        if (requestId == RequestId.AMPQ) {
//            Logger.v(TAG, "Chat sent");
//        }
//    }
//
//    @Override
//    public void onBadRequestError(final int requestId,
//                                  final IBlRequestContract request, final int errorCode,
//                                  final String errorMessage, final Bundle errorResponseBundle) {
//
//        if (requestId == RequestId.AMPQ) {
//
//            final long messageDbId = (Long) (request.getExtras().get(Keys.ID));
//
//            markChatAsFailed(messageDbId);
//        }
//    }
//
//    @Override
//    public void onAuthError(final int requestId,
//                            final IBlRequestContract request) {
//
//        if (requestId == RequestId.AMPQ) {
//
//            final long messageDbId = (Long) (request.getExtras().get(Keys.ID));
//
//            markChatAsFailed(messageDbId);
//        }
//    }
//
//    @Override
//    public void onOtherError(final int requestId,
//                             final IBlRequestContract request, final int errorCode) {
//
//        if (requestId == RequestId.AMPQ) {
//
//            final long messageDbId = (Long) (request.getExtras().get(Keys.ID));
//
//            markChatAsFailed(messageDbId);
//        }
//    }
//
//    /**
//     * @param messageDbId The database row of the locally inserted chat message
//     */
//    private void markChatAsFailed(long messageDbId) {
//        final ContentValues values = new ContentValues(1);
//        values.put(DatabaseColumns.CHAT_STATUS, ChatStatus.FAILED);
//
//        DBInterface.updateAsync(QueryTokens.UPDATE_MESSAGE_STATUS, hashCode(), null,
//                                TableChatMessages.NAME, values, MESSAGE_SELECT_BY_ID, new String[]{
//                        String.valueOf(messageDbId)
//                }, true, this
//        );
//    }

    @Override
    public void onDisconnect(final boolean manual) {
        if (!manual) {
            connectChatService();
        }
    }

    public Object getTaskTag() {
        return hashCode();
    }
    /**
     * Creates a new consumer
     */
    private void initMessageConsumer() {
        if ((mMessageConsumer == null) && isLoggedIn()) {
            mMessageConsumer = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants
                    .getChatPort(), VIRTUAL_HOST, String
                                                                 .format(Locale.US,
                                                                         EXCHANGE_NAME_FORMAT,
                                                                         UserInfo.INSTANCE
                                                                                 .getId()
                                                                 ),
                                                         AbstractRabbitMQConnector.ExchangeType.FANOUT
            );

            Logger.d(TAG,"consumer initialized");
            mMessageConsumer.setOnReceiveMessageHandler(ChatService.this);
            mMessageConsumer.setOnDisconnectCallback(ChatService.this);
        }

    }


    /**
     * Creates a new consumer for each service, making new exchanges
     */
    public void initMessageConsumer(String[] serviceIds) {
        mServiceConsumers=new ChatRabbitMQConnector[serviceIds.length];
        for(int i=0;i<serviceIds.length;i++) {
            mServiceConsumers[i] = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants
                        .getChatPort(), VIRTUAL_HOST, String
                        .format(Locale.US,
                                EXCHANGE_NAME_FORMAT,
                               serviceIds[i]
                        ),
                        AbstractRabbitMQConnector.ExchangeType.FANOUT
                );
            Logger.d(TAG,"service consumer initialized");
            mServiceConsumers[i].setOnReceiveMessageHandler(ChatService.this);
            mServiceConsumers[i].setOnDisconnectCallback(ChatService.this);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.lovocal.data.DBInterface.AsyncDbQueryCallback#onInsertComplete(int,
     * java.lang.Object, long)
     */
    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see
     * com.lovocal.data.DBInterface.AsyncDbQueryCallback#onDeleteComplete(int,
     * java.lang.Object, int)
     */
    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see
     * com.lovocal.data.DBInterface.AsyncDbQueryCallback#onUpdateComplete(int,
     * java.lang.Object, int)
     */
    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.lovocal.data.DBInterface.AsyncDbQueryCallback#onQueryComplete(int,
     * java.lang.Object, android.database.Cursor)
     */
    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(token== AppConstants.QueryTokens.GET_SERVICE_IDS){
            String[] serviceIds;
            cursor.moveToFirst();
            serviceIds=new String[cursor.getCount()];

            for(int i=0;i<cursor.getCount();i++){
                serviceIds[i]=cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));
                cursor.moveToNext();
                Logger.d(TAG,"Service Ids = "+serviceIds[i]);
            }
            initMessageConsumer(serviceIds);

        }
    }


@Subscribe public void bindExchange(BindExchangeUpdate bindExchangeUpdate){
    if(bindExchangeUpdate.haveToBind) {
        Logger.d(TAG, "binding new service to consumer");

        //TODO reconnecting the chat ~~
        if (isConnectedToChat()) {

            mMessageConsumer.dispose(true);
            mMessageConsumer = null;
            for (int i = 0; i < mServiceConsumers.length; i++) {
                mServiceConsumers[i].dispose(true);
                mServiceConsumers[i] = null;
            }

            mCurrentConnectMultiplier = 0;
            initMessageConsumer();
            String[] columns = {DatabaseColumns.ID};

            DBInterface.queryAsync(AppConstants.QueryTokens.GET_SERVICE_IDS, getTaskTag(),
                    null, true, TableMyServices.NAME, columns, null, null, null, null, null, null, ChatService.this);

            connectChatService();

        }


    }
  }
}
