package com.lovocal.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.lovocal.R;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.fragments.CreateServiceFragment;
import com.lovocal.utils.AppConstants;

public class CreateServiceActivity extends AbstractDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);


        if (savedInstanceState == null) {
            loadCreateServiceFragment();
        }
    }



    /** Load the fragment for creating the service */
    private void loadCreateServiceFragment() {


        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(this, CreateServiceFragment.class.getName(),null),
                AppConstants.FragmentTags.CREATE_SERVICE, false, null
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
