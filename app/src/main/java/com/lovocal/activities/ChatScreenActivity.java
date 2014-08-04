package com.lovocal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.lovocal.R;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.fragments.ChatDetailsFragment;
import com.lovocal.fragments.ChatPagerFragment;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.FragmentTags;
import com.lovocal.utils.Logger;

import java.util.ArrayList;

public class ChatScreenActivity extends AbstractLavocalActivity {

    public static final String TAG = "ChatScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        //initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);

        Intent intent = getIntent();


        //We are using array list so that we can convert ChatDetailsFragment into titlepager view
        //anytime
        ArrayList<String> chatIds = intent.getStringArrayListExtra(AppConstants.Keys.CHAT_ID_ARRAY);
        ArrayList<String> userIds = intent.getStringArrayListExtra(AppConstants.Keys.USER_ID_ARRAY);
        ArrayList<String> chatTitles = intent.getStringArrayListExtra(AppConstants.Keys.CHAT_TITLES);
        ArrayList<String> categoryIds = intent.getStringArrayListExtra(AppConstants.Keys.CATEGORY_ID_ARRAY);
        ArrayList<String> senderIds = new ArrayList<String>();

        ArrayList<String> senderTypes = intent.getStringArrayListExtra(AppConstants.Keys.SENDER_TYPE);



        String myId = intent.getStringExtra(AppConstants.Keys.MY_ID);
//        String senderType = intent.getStringExtra(AppConstants.Keys.SENDER_TYPE);
        if (intent.hasExtra(AppConstants.Keys.SERVICE_ID_ARRAY)) {
            senderIds = intent.getStringArrayListExtra(AppConstants.Keys.SERVICE_ID_ARRAY);

        }
        int pagerPosition = intent.getIntExtra(AppConstants.Keys.PAGER_POSITION, 0);

        setActionBarTitle(chatTitles.get(pagerPosition));

        loadChatScreen(chatIds, userIds, chatTitles, myId, pagerPosition, senderTypes, senderIds,categoryIds);

    }


    /**
     * Loads the {@link com.lovocal.fragments.ChatPagerFragment} into the fragment container
     */
    public void loadChatScreen(ArrayList<String> chatIds, ArrayList<String> userIds,
                               ArrayList<String> chatTitles, String myId, int pagerPosition, ArrayList<String> senderTypes, ArrayList<String> senderIds,ArrayList<String> categoryIds) {

        final Bundle args = new Bundle(4);


        args.putString(AppConstants.Keys.CHAT_ID, chatIds.get(pagerPosition));
        args.putString(AppConstants.Keys.USER_ID, userIds.get(pagerPosition));
        args.putString(AppConstants.Keys.CATEGORY_ID,categoryIds.get(pagerPosition));

        if (senderTypes.get(pagerPosition).equals(AppConstants.SERVICE)) {
            args.putString(AppConstants.Keys.SENDER_TYPE, senderTypes.get(pagerPosition));
            Logger.d(TAG, "sender type is service");
            args.putString(AppConstants.Keys.MY_ID, senderIds.get(pagerPosition));
        } else {
            args.putString(AppConstants.Keys.SENDER_TYPE, AppConstants.USER);
            Logger.d(TAG, "sender type is user");
            args.putString(AppConstants.Keys.MY_ID, myId);

        }
//        args.putStringArrayList(AppConstants.Keys.CHAT_ID_ARRAY, chatIds);
//        args.putStringArrayList(AppConstants.Keys.USER_ID_ARRAY, userIds);
//        args.putStringArrayList(AppConstants.Keys.CHAT_TITLES,chatTitles);
//        args.putString(AppConstants.Keys.MY_ID, myId);
//        if(senderType.equals(AppConstants.SERVICE)) {
//            args.putString(AppConstants.Keys.USER_ID,userIds.get(pagerPosition));
//
//            args.putString(AppConstants.Keys.SENDER_TYPE, senderType);
//            args.putStringArrayList(AppConstants.Keys.SERVICE_ID_ARRAY,senderIds);
//        }
//        else{
//            args.putString(AppConstants.Keys.SENDER_TYPE, AppConstants.USER);
//        }
//        args.putInt(AppConstants.Keys.PAGER_POSITION,pagerPosition);

//        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
//                        .instantiate(this, ChatPagerFragment.class
//                                .getName(), args), FragmentTags.CHAT_DETAILS_PAGER, false,
//                null
//        );

        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, ChatDetailsFragment.class
                                .getName(), args), FragmentTags.CHAT_DETAILS, false,
                null
        );

    }

    @Override
    protected Object getTaskTag() {
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected boolean isDrawerActionBarToggleEnabled() {
//        return false;
//    }

}
