package com.lovocal.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lovocal.R;
import com.lovocal.activities.ChatScreenActivity;
import com.lovocal.adapters.ChatsAdapter;
import com.lovocal.adapters.ChatsBusinessAdapter;
import com.lovocal.chat.ChatService;
import com.lovocal.chat.ChatService.ChatServiceBinder;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.SQLiteLoader;
import com.lovocal.data.ViewChatsWithMessagesAndUsers;
import com.lovocal.data.ViewChatsWithServiceToUserMessages;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.Keys;
import com.lovocal.utils.AppConstants.Loaders;
import com.lovocal.utils.Logger;
import com.lovocal.utils.Utils;

import java.util.ArrayList;


/**
 * Created by anshul1235 on 15/07/14.
 */
public class ChatsBusinessFragment extends AbstractLavocalFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ServiceConnection,
        DBInterface.AsyncDbQueryCallback {

    private static final String TAG = "ChatsBussinessFragment";

    private ChatsBusinessAdapter mChatsAdapter;

    private ListView mChatsListView;

    private ChatService mChatService;

    private boolean mBoundToChatService;

    private String mDeleteChatId, mBlockUserId;

    private final String mChatSelectionForDelete = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;

    /** Whether a chat message should be loaded immediately on opening */
    private boolean mShouldLoadChat;

    /** Id of the user to load immediately on opening */
    private String mUserIdToLoad;


    private final Handler mHandler = new Handler();

    /**
     * cursor maintains the reference to all the values of the list which we will use in
     * ChatPagerFragment
     */
    private  Cursor mCursor;

    /**
     *This list will hold the UserId arguments for each chat!
     */
    private ArrayList<String> mUserId=new ArrayList<String>();

    /**
     *This list will hold the ChatId arguments for each chat!
     */
    private ArrayList<String> mChatId=new ArrayList<String>();

    /**
     *This list will hold the Title arguments for each chat!
     */
    private ArrayList<String> mChatTitles=new ArrayList<String>();

    private ArrayList<String> mServiceIds=new ArrayList<String>();


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View view = inflater
                .inflate(R.layout.fragment_chats, container, false);
        mChatsListView = (ListView) view.findViewById(R.id.list_chats);
        mChatsAdapter = new ChatsBusinessAdapter(getActivity(), null);
        mChatsListView.setAdapter(mChatsAdapter);
        mChatsListView.setOnItemClickListener(this);
        mChatsListView.setOnItemLongClickListener(this);

        if (savedInstanceState == null) {

            final Bundle args = getArguments();

            if (args != null) {
                mShouldLoadChat = args.getBoolean(Keys.LOAD_CHAT);

                if (mShouldLoadChat) {
                    mUserIdToLoad = args.getString(Keys.USER_ID);
                }

                if (TextUtils.isEmpty(mUserIdToLoad)) {
                    mShouldLoadChat = false;
                }
            }
        }
        else
        {
            mShouldLoadChat=savedInstanceState.getBoolean(Keys.LOAD_CHAT);
            mUserIdToLoad=savedInstanceState.getString(Keys.USER_ID);
        }
        getLoaderManager().restartLoader(Loaders.ALL_CHATS, null, this);
        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.USER_ID,mUserIdToLoad);
        outState.putBoolean(Keys.LOAD_CHAT,mShouldLoadChat);
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
            return new SQLiteLoader(getActivity(), false, ViewChatsWithServiceToUserMessages.NAME, null,
                    null, null, null, null,  DatabaseColumns.TIMESTAMP_EPOCH
                    + SQLConstants.DESCENDING, null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (loader.getId() == Loaders.ALL_CHATS) {

            mCursor=cursor;
            Logger.d(TAG,"cursor loaded with " + cursor.getCount());
            mChatsAdapter.swapCursor(cursor);
//            if (mShouldLoadChat) {
//
//                if (isAttached()) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            loadChat(mUserIdToLoad,0);
//                            //We only need to load on very launch of activity
//                            mShouldLoadChat = false;
//                        }
//                    });
//                }
//
//            }
        }

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        if (loader.getId() == Loaders.ALL_CHATS) {
            mChatsAdapter.swapCursor(null);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                            final int position, final long id) {

        if (parent.getId() == R.id.list_chats) {

            mCursor.moveToFirst();
            mUserId.clear();
            mChatId.clear();
            mChatTitles.clear();
            for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                mUserId.add(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.ID)));
                mChatId.add(Utils.generateChatId( mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.ID)),mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.RECEIVER_ID))));
                mChatTitles.add(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.NAME)));
                mServiceIds.add(mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.RECEIVER_ID)));
            }



            loadChat(mUserId, mChatId,mChatTitles,mServiceIds,position);
        }
    }

//    /**
//     * Loads a chat directly. This is used in the case where the user directly taps on a chat button
//     * on another user's profile page
//     */
//    private void loadChat(String userId,int position) {
//
//
//        final String chatId = Utils
//                .generateChatId(userId, AppConstants.UserInfo.INSTANCE.getId());
//
//        loadChat(userId, chatId,position);
//    }

    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     *
     * @param userId The user Id of the chat to load
     * @param chatId The ID of the chat
     */
    private void loadChat(ArrayList<String> userIds, ArrayList<String> chatIds,ArrayList<String> chatTitles,ArrayList<String> serviceIds
            ,int position) {



        final Intent chatScreenActivity = new Intent(getActivity(),
                ChatScreenActivity.class);

        chatScreenActivity.putStringArrayListExtra(Keys.USER_ID_ARRAY,userIds);
        chatScreenActivity.putStringArrayListExtra(Keys.CHAT_ID_ARRAY,chatIds);
        chatScreenActivity.putStringArrayListExtra(Keys.CHAT_TITLES,chatTitles);
        chatScreenActivity.putExtra(Keys.PAGER_POSITION, position);
        chatScreenActivity.putExtra(Keys.SENDER_TYPE, AppConstants.SERVICE);
        chatScreenActivity.putStringArrayListExtra(Keys.SERVICE_ID_ARRAY,serviceIds);
        chatScreenActivity.putExtra(Keys.MY_ID,AppConstants.UserInfo.INSTANCE.getId());

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
    public void onServiceDisconnected(final ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                   long id) {

        final Cursor cursor = (Cursor) mChatsAdapter.getItem(position);


        mDeleteChatId = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.CHAT_ID));
//        mBlockUserId = cursor.getString(cursor
//                .getColumnIndex(DatabaseColumns.USER_ID));
        return true;
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

    public static ChatsBusinessFragment newInstance() {
        ChatsBusinessFragment f = new ChatsBusinessFragment();
       // f.setArguments(categoryDetails);
        return f;
    }
}
