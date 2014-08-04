package com.lovocal.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.fragments.SearchServiceFragment;
import com.lovocal.utils.AppConstants;

public class SearchActivity extends AbstractDrawerActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);

            loadHomeScreen();

    }

    /**
     * Loads the {@link com.lovocal.fragments.HomeScreenFragment} into the fragment container
     */
    public void loadHomeScreen() {

//        String categoryName = getIntent().getExtras().getString(AppConstants.Keys.CATEGORY_NAME);
//        String categoryId   = getIntent().getExtras().getString(AppConstants.Keys.CATEGORY_ID);
//
//        setActionBarTitle(categoryName);
//
//        Bundle args= new Bundle(2);
//        args.putString(AppConstants.Keys.CATEGORY_NAME,categoryName);
//        args.putString(AppConstants.Keys.CATEGORY_ID,categoryId);
//
//        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
//                        .instantiate(this, SearchServiceFragment.class
//                                .getName(), args), AppConstants.FragmentTags.SEARCH_SERVICE, false,
//                null
//        );

    }

    @Override
    protected Object getTaskTag() {
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    protected boolean isDrawerActionBarToggleEnabled() {
        return false;
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
}
