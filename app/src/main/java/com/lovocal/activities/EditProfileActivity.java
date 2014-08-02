package com.lovocal.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.lovocal.R;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.fragments.EditProfileFragment;
import com.lovocal.utils.AppConstants;

public class EditProfileActivity extends AbstractDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);


        if (savedInstanceState == null) {
            loadEditProfileFragment();
        }
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

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    protected boolean isDrawerActionBarToggleEnabled() {
        return false;
    }
}
