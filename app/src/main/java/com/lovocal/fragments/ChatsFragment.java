package com.lovocal.fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.activities.ChatScreenActivity;
import com.lovocal.activities.HomeActivity;
import com.lovocal.adapters.ChatsAdapter;
import com.lovocal.chat.ChatService;
import com.lovocal.chat.ChatService.ChatServiceBinder;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.SQLiteLoader;
import com.lovocal.data.TableChatMessages;
import com.lovocal.data.TableChats;
import com.lovocal.data.ViewChatsWithMessagesAndUsers;
import com.lovocal.fragments.dialogs.SingleChoiceDialogFragment;
import com.lovocal.http.HttpConstants;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.FragmentTags;
import com.lovocal.utils.AppConstants.Keys;
import com.lovocal.utils.AppConstants.Loaders;
import com.lovocal.utils.Logger;
import com.lovocal.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by anshul1235 on 15/07/14.
 */
public class ChatsFragment extends AbstractLavocalFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ServiceConnection,
        DBInterface.AsyncDbQueryCallback {

    private static final String TAG = "ChatsFragment";

    private ChatsAdapter mChatsAdapter;

    private ListView mChatsListView;

    private ChatService mChatService;

    private boolean mBoundToChatService;

    private String mDeleteChatId, mDeleteUserId;

    private final String mChatSelectionForDelete = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;

    /**
     * Whether a chat message should be loaded immediately on opening
     */
    private boolean mShouldLoadChat;

    /**
     * Id of the user to load immediately on opening
     */
    private String mUserIdToLoad;


    private final Handler mHandler = new Handler();

    /**
     * cursor maintains the reference to all the values of the list which we will use in
     * ChatPagerFragment
     */
    private Cursor mCursor;


    /**
     * Reference to the Dialog Fragment for selecting the chat options
     */
    private SingleChoiceDialogFragment mChatDialogFragment;


    /**
     * This list will hold the UserId arguments for each chat!
     */
    private ArrayList<String> mUserId = new ArrayList<String>();

    /**
     * This list will hold the ChatId arguments for each chat!
     */
    private ArrayList<String> mChatId = new ArrayList<String>();

    /**
     * This list will hold the Title arguments for each chat!
     */
    private ArrayList<String> mChatTitles = new ArrayList<String>();

    private ArrayList<String> mServiceIds = new ArrayList<String>();

    private ArrayList<String> mSenderType = new ArrayList<String>();

    private ArrayList<String> mCategoryIds = new ArrayList<String>();

    private String mCategoryId;

    private boolean mOpenedFromNotification=false;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View view = inflater
                .inflate(R.layout.fragment_chats, container, false);
        mChatsListView = (ListView) view.findViewById(R.id.list_chats);
        mChatsAdapter = new ChatsAdapter(getActivity(), null);
        mChatsListView.setAdapter(mChatsAdapter);
        mChatsListView.setOnItemClickListener(this);
        mChatsListView.setOnItemLongClickListener(this);


        final Bundle args = getArguments();

        if (args != null) {

            mCategoryId = args.getString(Keys.CATEGORY_ID);
            mOpenedFromNotification = args.getBoolean(Keys.FROM_NOTIFICATIONS);

        }
        getLoaderManager().restartLoader(Loaders.ALL_CHATS, null, this);
        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mBoundToChatService) {
            mChatService.setChatScreenVisible(true);
            getActivity().unbindService(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    }


    @Override
    public boolean onBackPressed() {
        getActivity().finish();

        if(mOpenedFromNotification){
            final Intent homeActivity = new Intent(getActivity(),
                    HomeActivity.class);

            startActivity(homeActivity);
            return true;
        }else{
            return super.onBackPressed();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        if (id == Loaders.ALL_CHATS) {

            if (mCategoryId == null) {
                return new SQLiteLoader(getActivity(), false, ViewChatsWithMessagesAndUsers.NAME, null,
                        null, null, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                        + SQLConstants.DESCENDING, null
                );
            } else {

                return new SQLiteLoader(getActivity(), false, ViewChatsWithMessagesAndUsers.NAME, null,
                        DatabaseColumns.CATEGORY_ID
                                + SQLConstants.EQUALS_ARG, new String[]{
                        mCategoryId}, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                        + SQLConstants.DESCENDING, null
                );
            }

        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (loader.getId() == Loaders.ALL_CHATS) {

            mCursor = cursor;
            Logger.d(TAG, "cursor loaded with " + cursor.getCount());
            mChatsAdapter.swapCursor(cursor);
        }

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        if (loader.getId() == Loaders.ALL_CHATS) {
            mChatsAdapter.swapCursor(null);
        }
    }

    public void onItemClick(final AdapterView<?> parent, final View view,
                            final int position, final long id) {

        if (parent.getId() == R.id.list_chats) {

            String senderType = "";
            mCursor.moveToFirst();
            mUserId.clear();
            mChatId.clear();
            mChatTitles.clear();
            mServiceIds.clear();
            mCategoryIds.clear();
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mServiceIds.add(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.RECEIVER_ID)));
                mUserId.add(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.ID)));
                mCategoryIds.add(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.CATEGORY_ID)));
                if (mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.CHAT_TYPE)).equals(AppConstants.ChatType.SERVICE)) {

                    Logger.d(TAG, "SERVICE");
                    senderType = AppConstants.SERVICE;
                    mSenderType.add(senderType);
                    mChatId.add(Utils.generateChatId(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.ID)), mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.RECEIVER_ID))));

                } else {
                    Logger.d(TAG, "USER");
                    senderType = AppConstants.USER;
                    mSenderType.add(senderType);
                    mChatId.add(Utils.generateChatId(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.ID)), AppConstants.UserInfo.INSTANCE.getId()));

                }
                mChatTitles.add(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.NAME)));
            }


            if (AppConstants.DeviceInfo.INSTANCE.getLatestLocation().getLatitude() != 0.0) {
                loadChat(mUserId, mChatId, mChatTitles, mServiceIds, mSenderType, mCategoryIds, position);
            } else {
                Toast.makeText(getActivity(), "Please Turn On Your Location", Toast.LENGTH_LONG).show();
            }

        }
    }


    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     *
     * @param userIds    The user Ids of the chat to load
     * @param chatIds    The IDs of the chat
     * @param chatTitles The titles of the chat
     * @param position   Which chat you want to open (pager position)
     */
    private void loadChat
    (ArrayList<String> userIds, ArrayList<String> chatIds, ArrayList<String> chatTitles
            , ArrayList<String> serviceIds, ArrayList<String> senderTypes, ArrayList<String> categoryId,
     int position) {


        final Intent chatScreenActivity = new Intent(getActivity(),
                ChatScreenActivity.class);

        chatScreenActivity.putStringArrayListExtra(Keys.USER_ID_ARRAY, userIds);
        chatScreenActivity.putStringArrayListExtra(Keys.CHAT_ID_ARRAY, chatIds);
        chatScreenActivity.putStringArrayListExtra(Keys.CHAT_TITLES, chatTitles);
        chatScreenActivity.putStringArrayListExtra(Keys.SERVICE_ID_ARRAY, serviceIds);
        chatScreenActivity.putStringArrayListExtra(Keys.CATEGORY_ID_ARRAY, categoryId);
        chatScreenActivity.putExtra(Keys.PAGER_POSITION, position);
        chatScreenActivity.putStringArrayListExtra(Keys.SENDER_TYPE, senderTypes);
        chatScreenActivity.putExtra(Keys.MY_ID, AppConstants.UserInfo.INSTANCE.getId());

        startActivity(chatScreenActivity);

    }

    @Override
    public void onServiceConnected(final ComponentName name,
                                   final IBinder service) {

        mBoundToChatService = true;
        mChatService = ((ChatServiceBinder) service).getService();
        mChatService.clearChatNotifications();
        mChatService.setChatScreenVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();

                if(mOpenedFromNotification){
                    final Intent homeActivity = new Intent(getActivity(),
                            HomeActivity.class);

                    startActivity(homeActivity);
                }

                return true;
            }


            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                   long id) {

        final Cursor cursor = (Cursor) mChatsAdapter.getItem(position);


        mDeleteChatId = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.CHAT_ID));

        mDeleteUserId = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.ID));

        showChatOptions();

        return true;
    }


    /**
     * Show dialog for chat options
     */
    private void showChatOptions() {

        mChatDialogFragment = new SingleChoiceDialogFragment();
        mChatDialogFragment
                .show(AlertDialog.THEME_HOLO_LIGHT, R.array.chat_longclick_choices, 0,
                        R.string.chat_longclick_dialog_head, getFragmentManager(), true,
                        FragmentTags.DIALOG_CHAT_LONGCLICK);

    }

    @Override
    public boolean willHandleDialog(final DialogInterface dialog) {

        if ((mChatDialogFragment != null)
                && mChatDialogFragment.getDialog().equals(dialog)) {
            return true;
        }
        return super.willHandleDialog(dialog);
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mChatDialogFragment != null)
                && mChatDialogFragment.getDialog().equals(dialog)) {

            if (which == 0) {


                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());

                // set title
                alertDialogBuilder.setTitle("Confirm");

                // set dialog message
                alertDialogBuilder
                        .setMessage(getResources().getString(R.string.delete_chat_alert_message))
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {

                                callDeleteApi(mDeleteChatId, mDeleteUserId);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                final AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();


            }
        } else {
            super.onDialogClick(dialog, which);
        }
    }

    private void callDeleteApi(String chatId,String userId){

        final Map<String, String> params = new HashMap<String, String>(1);
        params.put(HttpConstants.CHAT_SERVICE_ID, userId);
        mApiService.blockUser(params,this);

    }

    @Override
    public void success(Object o, Response response) {

        deleteChat(mDeleteChatId);
    }

    @Override
    public void failure(RetrofitError error) {
    }

    private void deleteChat(String chatId) {


        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_CHATS, getTaskTag(), null, TableChats.NAME,
                mChatSelectionForDelete, new String[]{
                        chatId
                }, true, this
        );
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_CHAT_MESSAGES, getTaskTag(), null,
                TableChatMessages.NAME, mChatSelectionForDelete, new String[]{
                        chatId
                }, true, this
        );

    }

    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }


    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // TODO Auto-generated method stub

    }

    public static ChatsFragment newInstance() {
        ChatsFragment f = new ChatsFragment();
        //f.setArguments(args);
        return f;
    }
}
