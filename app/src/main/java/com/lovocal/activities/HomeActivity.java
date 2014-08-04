package com.lovocal.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.lovocal.R;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.fragments.ChatDetailsFragment;
import com.lovocal.fragments.ChatsFragment;
import com.lovocal.fragments.CreateServiceFragment;
import com.lovocal.fragments.EditProfileFragment;
import com.lovocal.fragments.HomeScreenFragment;
import com.lovocal.fragments.LoginFragment;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.FragmentTags;
import com.lovocal.utils.GooglePlayClientWrapper;
import com.lovocal.utils.AppConstants.Keys;

public class HomeActivity extends AbstractLavocalActivity implements
        LocationListener {

    /**
     * Helper for connecting to Google Play Services
     */
    private GooglePlayClientWrapper mGooglePlayClientWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        //initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);

        mGooglePlayClientWrapper = new GooglePlayClientWrapper(this, this);

        setActionBarTitle(R.string.city_name);
        getActionBar().setDisplayHomeAsUpEnabled(false);

        if (savedInstanceState == null) {

            final String action = getIntent().getAction();

            if (action == null) {

                loadAppropriateScreen();


            } else if (action.equals(AppConstants.ACTION_SHOW_ALL_CHATS)) {
                loadChatsFragment();
            } else if (action.equals(AppConstants.ACTION_SHOW_CHAT_DETAIL)) {

                loadChatDetailFragment(getIntent().getStringExtra(Keys.CHAT_ID), getIntent()
                        .getStringExtra(Keys.USER_ID), getIntent().getStringExtra(Keys.MY_ID));
            }
            else{
                loadAppropriateScreen();

            }

        }

    }


    private void loadAppropriateScreen(){
        if(isVerified()) {
            loadHomeScreen();
        }


        else if(!isActivated()){
            //jump to activate screen
            loadLoginFragment();
        }


        else if(!isLoggedIn()){
            //jump to Edit Profile Screen
            loadEditProfileFragment();

        }

    }

    /**
     * Loads the {@link ChatsFragment} into the fragment container
     */
    private void loadChatsFragment() {

        final Bundle args = new Bundle(1);
        args.putBoolean(Keys.FROM_NOTIFICATIONS,true);
        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, ChatsFragment.class.getName(), args),
                FragmentTags.CHATS, false,
                null
        );

    }

    /** Load the fragment for editing the profile */
    private void loadEditProfileFragment() {

        Bundle args=new Bundle(1);

        args.putString(AppConstants.Keys.ID, AppConstants.UserInfo.INSTANCE.getId());

        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, EditProfileFragment.class.getName(),args),
                AppConstants.FragmentTags.EDIT_PROFILE, false, null
        );
    }

    /** Load the fragment for login */
    private void loadLoginFragment() {

        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, LoginFragment.class.getName(), getIntent().getExtras()),
                AppConstants.FragmentTags.LOGIN, false, null
        );
    }

    /**
     * Loads the {@link ChatDetailsFragment} into the fragment container
     *
     * @param chatId The chat detail to load
     * @param userId The user Id of the user with which the current user is chatting
     */
    private void loadChatDetailFragment(final String chatId, final String userId, final String myId) {

        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(userId)) {
            finish();
        }


        final Bundle args = new Bundle(2);
        args.putString(Keys.CHAT_ID, chatId);
        args.putString(Keys.USER_ID, userId);
        args.putString(Keys.MY_ID,myId);
        args.putBoolean(Keys.FROM_NOTIFICATIONS,true);
        args.putString(Keys.SENDER_TYPE,AppConstants.SERVICE);
        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, ChatDetailsFragment.class.getName(), args),
                FragmentTags.CHAT_DETAILS, false, null
        );

    }
    @Override
    protected void onStop() {
        mGooglePlayClientWrapper.onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGooglePlayClientWrapper.onStart();

    }

    /**
     * Loads the {@link com.lovocal.fragments.HomeScreenFragment} into the fragment container
     */
    public void loadHomeScreen() {

        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, HomeScreenFragment.class
                                .getName(), null), FragmentTags.HOME_SCREEN, false,
                null
        );

    }

    @Override
    protected Object getTaskTag() {
        return null;
    }



    @Override
    public void onLocationChanged(Location location) {

        if (location.getLatitude() != 0.0) {
            AppConstants.DeviceInfo.INSTANCE.setLatestLocation(location);
            // Toast.makeText(getApplicationContext(),location.getLatitude()+"",Toast.LENGTH_SHORT).show();
            final AbstractLavocalFragment fragment = getCurrentMasterFragment();

            if (fragment instanceof CreateServiceFragment) {
                ((CreateServiceFragment) fragment).updateLocation(location);
            }
        }
    }
}
