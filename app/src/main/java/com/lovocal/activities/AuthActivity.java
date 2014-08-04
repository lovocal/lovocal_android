package com.lovocal.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.lovocal.R;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.fragments.LoginFragment;
import com.lovocal.utils.AppConstants;

public class AuthActivity extends AbstractLavocalActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);


        if (savedInstanceState == null) {
            loadLoginFragment();
        }
    }

    /** Load the fragment for login */
    private void loadLoginFragment() {

        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, LoginFragment.class.getName(), getIntent().getExtras()),
                AppConstants.FragmentTags.LOGIN, false, null
        );
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


//    @Override
//    protected boolean isDrawerActionBarToggleEnabled() {
//        return false;
//    }


}
