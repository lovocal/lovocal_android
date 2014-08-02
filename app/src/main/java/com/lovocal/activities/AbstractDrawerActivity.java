
package com.lovocal.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.lovocal.R;
import com.lovocal.fragments.NavDrawerFragment;
import com.lovocal.utils.AppConstants;

/**
 * Base class for all Activities that need to use a Navigation drawer. The drawer must be present in
 * the layout
 * <p/>
 */

public abstract class AbstractDrawerActivity extends AbstractLavocalActivity implements NavDrawerFragment.INavDrawerActionCallback {


    /**
     * Drawer Layout that contains the Navigation Drawer
     */
    private DrawerLayout          mDrawerLayout;
    /**
     * Drawer toggle for Action Bar
     */
    private ActionBarDrawerToggle mDrawerToggle;
    /**
     * {@link android.widget.FrameLayout} that provides the navigation items
     */
    private FrameLayout           mNavFrameContent;

    /**
     * Whether the drawer has been initialized or not.
     */
    private boolean mDrawerInitialized;

    /**
     * Initializes the Navigation drawer. Call this in onCreate() of your Activity AFTER setting the
     * content view
     */
    protected void initDrawer(final int drawerLayoutResId, final int drawerContentResId) {

        mDrawerInitialized = true;
        mDrawerLayout = (DrawerLayout) findViewById(drawerLayoutResId);

        if (mDrawerLayout == null) {
            throw new IllegalArgumentException(
                    "Drawer Layout with id R.id.drawer_layout not found. Check your layout/resource id being sent");
        }
        mNavFrameContent = (FrameLayout) findViewById(drawerContentResId);

        if (mNavFrameContent == null) {
            throw new IllegalArgumentException(
                    "Drawer content with id R.id.frame_nav_drawer not found. Check the layout/resource id being sent");
        }

        if (isDrawerActionBarToggleEnabled()) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                                             R.drawable.ic_navigation_drawer,
                                                      R.string.drawer_open,
                                                      R.string.drawer_closed) {

                @Override
                public void onDrawerOpened(final View drawerView) {
                    super.onDrawerOpened(drawerView);
                    //  mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    invalidateOptionsMenu();

                }

                @Override
                public void onDrawerClosed(final View drawerView) {
                    super.onDrawerClosed(drawerView);
                    //   mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    invalidateOptionsMenu();

                }

            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mDrawerToggle.setDrawerIndicatorEnabled(true);

        }

        /* In the case that the activity was destroyed in background, the system will take care of reinitializing the fragment for us*/
        NavDrawerFragment fragment = (NavDrawerFragment) getSupportFragmentManager()
                .findFragmentByTag(AppConstants.FragmentTags.NAV_DRAWER);

        if (fragment == null) {
            fragment = (NavDrawerFragment) Fragment
                    .instantiate(this, NavDrawerFragment.class.getName());
            loadFragment(R.id.frame_nav_drawer, fragment, AppConstants.FragmentTags.NAV_DRAWER,
                         false, null);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mDrawerInitialized) {
            throw new RuntimeException(
                    "Drawer not initialized! Are you sure you are calling initDrawer() in your Activity's onCreate()?");
        }
    }

    /**
     * Used to control whether the Activity should attach the drawer layout to the Action Bar.
     *
     * @return <code>true</code> to attach the drawer layout to the action bar
     */
    protected abstract boolean isDrawerActionBarToggleEnabled();

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (isDrawerActionBarToggleEnabled()) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isDrawerActionBarToggleEnabled()) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (isDrawerActionBarToggleEnabled()) {
            setOptionsGroupHidden(menu, mDrawerLayout.isDrawerOpen(mNavFrameContent));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void setOptionsGroupHidden(final Menu menu, final boolean drawerOpen) {


        //TODO when u add the menu
        menu.setGroupEnabled(R.id.group_hide_on_drawer_open, !drawerOpen);
        menu.setGroupVisible(R.id.group_hide_on_drawer_open, !drawerOpen);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        /* Pass the event to ActionBarDrawerToggle, if it returns
         true, then it has handled the app icon touch event */
        if (isDrawerActionBarToggleEnabled() && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActionTaken() {
        mDrawerLayout.closeDrawer(mNavFrameContent);
    }
}
