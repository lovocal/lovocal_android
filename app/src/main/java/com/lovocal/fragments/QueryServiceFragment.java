package com.lovocal.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.activities.AbstractLavocalActivity;
import com.lovocal.bus.SlidePanelUpdate;
import com.lovocal.chat.ChatService;
import com.lovocal.retromodels.request.SendBroadcastChatRequestModel;
import com.lovocal.utils.AppConstants;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class QueryServiceFragment extends AbstractLavocalFragment implements View.OnClickListener,ServiceConnection
{

    private  EditText                   mEditQuery;
    private  Button                     mBroadcastButton;

    private ChatService mChatService;

    private boolean mBoundToChatService;

    private SimpleDateFormat mFormatter;

    private TextView        mPanelHeader;

    /**
     * holds the category id
     */
    private String      mCategoryId;




    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_query, container, false);


        final Bundle extras = getArguments();

        if(savedInstanceState==null) {
            mBus.register(this);
        }

        if (extras != null && extras.containsKey(AppConstants.Keys.CATEGORY_ID)) {
            mCategoryId=extras.getString(AppConstants.Keys.CATEGORY_ID);
        }
        else
        {
            //Should not happen
        }

        //this sets the drag handle for slide layout
        final AbstractLavocalFragment fragment = ((AbstractLavocalActivity) getActivity())
                .getCurrentMasterFragment();

        ((SearchServiceFragment) fragment).setDragHandle(contentView
                .findViewById(
                        R.id.container_profile_info));

        setHasOptionsMenu(false);

        // load the ChatsFragment
            final ChatsFragment chatFragment = new ChatsFragment();

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_chat_details, chatFragment, AppConstants.FragmentTags.CHAT_DETAILS)
                    .commit();


        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());

        mPanelHeader=(TextView)contentView.findViewById(R.id.panel_header);

        mEditQuery=(EditText)contentView.findViewById(R.id.edit_query);
        mBroadcastButton=(Button)contentView.findViewById(R.id.button_broadcast);
        mBroadcastButton.setOnClickListener(this);


        return contentView;

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBoundToChatService) {
            mChatService.setChatScreenVisible(true);
            getActivity().unbindService(this);
        }
    }

    @Subscribe
    public void updatePanelOpen(SlidePanelUpdate update)
    {
        if(update.panelFlag) {
            mPanelHeader.setText("Send A Broadcast Message");
        }
        else{
            mPanelHeader.setText("Press me to write Query");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    }


    public static QueryServiceFragment newInstance(Bundle categoryDetails) {
        QueryServiceFragment f = new QueryServiceFragment();
        f.setArguments(categoryDetails);
        return f;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_broadcast) {
            if (sendChatMessage(mEditQuery.getText().toString())) {
                mEditQuery.setText(null);
            } else {
            }
        }
    }

    /**
     * Send a chat message to the user the current user is chatting with
     *
     * @param message The message to send.
     * @return <code>true</code> If the message was sent, <code>false</code> otherwise
     */
    private boolean sendChatMessage(String message) {

        boolean sent = true;
        if (!TextUtils.isEmpty(message)) {
            if (mBoundToChatService) {
                final String sentAt = mFormatter.format(new Date());

                sendBroadcastMessageToCategory(mCategoryId, message, sentAt, AppConstants.DeviceInfo.INSTANCE.getLatestLocation());
            } else {
                sent = false;
            }
        }
        return sent;
    }

    /**
     * Send a message to a user
     *
     * @param toCategoryId The Category Id to send the message to
     * @param message  The message to send
     */
    public void sendBroadcastMessageToCategory(final String toCategoryId, final String message,
                                               final String timeSentAt,final Location location) {

        if (!isLoggedIn()) {
            return;
        }



        SendBroadcastChatRequestModel broadcastChatRequestModel=new SendBroadcastChatRequestModel();


        broadcastChatRequestModel.chat.setLatitude(location.getLatitude());
        broadcastChatRequestModel.chat.setLongitude(location.getLongitude());
        broadcastChatRequestModel.chat.setList_cat_id(toCategoryId);
        broadcastChatRequestModel.chat.setSent_time(timeSentAt);
        broadcastChatRequestModel.chat.setMessage(message);
        broadcastChatRequestModel.chat.setUser_id(AppConstants.UserInfo.INSTANCE.getId());

        getActivity().setProgressBarIndeterminateVisibility(true);
        mApiService.sendBroadCastChat(broadcastChatRequestModel,this);

    }

    @Override
    public void success(Object o, Response response) {
        getActivity().setProgressBarIndeterminateVisibility(false);
        Toast.makeText(getActivity(),"Broadcast sent successfully",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void failure(RetrofitError error) {
        getActivity().setProgressBarIndeterminateVisibility(false);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBoundToChatService = true;
        mChatService = ((ChatService.ChatServiceBinder) service).getService();
        mChatService.setCurrentChattingUserId(AppConstants.UserInfo.INSTANCE.getId());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBoundToChatService = false;
    }


}
