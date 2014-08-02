package com.lovocal.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Window;

import com.lovocal.LavocalApplication;
import com.lovocal.R;
import com.lovocal.fragments.AbstractLavocalFragment;
import com.lovocal.fragments.FragmentTransition;
import com.lovocal.http.Api;


import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.lovocal.utils.AppConstants.UserInfo;
import com.lovocal.widgets.TypefaceCache;
import com.lovocal.widgets.TypefacedSpan;
import com.squareup.otto.Bus;

/**
 * Created by anshul1235 on 14/07/14.
 */
public  abstract class AbstractLavocalActivity extends ActionBarActivity implements Callback,DialogInterface.OnClickListener{

    private static final String TAG = "BaseLavocalActivity";

    private static final int ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
            | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO | ActionBar
            .DISPLAY_SHOW_HOME;

    private   ActivityTransition mActivityTransition;
    /**
     * this holds the reference for the restadapter which we declared in LavocalApplication
     */
    protected RestAdapter mRestAdapter;

    /**
     * this holds the reference for the Otto Bus which we declared in LavocalApplication
     */
    protected Bus         mBus;

    /**
     * this holds the reference for the api service which we declared in LavocalApplication
     */
    protected Api         mApiService;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        /* Here, getClass() might show an Ambiguous method call bug. It's a bug in IntelliJ IDEA 13
        * http://youtrack.jetbrains.com/issue/IDEA-72835 */
        mActivityTransition = getClass()
                .getAnnotation(ActivityTransition.class);

        mRestAdapter=((LavocalApplication) getApplication())
                .getRestAdapter();
        mBus=((LavocalApplication)getApplication()).getBus();
        mBus.register(this);
        mApiService=((LavocalApplication)getApplication()).getService(mRestAdapter);

        if (savedInstanceState == null) {
            if (mActivityTransition != null) {
                overridePendingTransition(mActivityTransition.createEnterAnimation(),
                        mActivityTransition
                                .createExitAnimation()
                );
            }

        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
        }


    }

    /**
     * Sets the Action bar title, using the desired {@link android.graphics.Typeface} loaded from {@link
     * TypefaceCache}
     *
     * @param titleResId The title string resource Id to set for the Action Bar
     */
    public final void setActionBarTitle(final int titleResId) {
        setActionBarTitle(getString(titleResId));
    }

    /**
     * Sets the Action bar title, using the desired {@link android.graphics.Typeface} loaded from {@link
     * TypefaceCache}
     *
     * @param title The title to set for the Action Bar
     */

    public final void setActionBarTitle(final String title) {

        final SpannableString s = new SpannableString(title);
        s.setSpan(new TypefacedSpan(this, TypefaceCache.BOLD), 0, s.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }


    public RestAdapter getRestAdapter()
    {
        return mRestAdapter;
    }

    public Bus getBus()
    {
        return mBus;
    }
    public Api getApiService()
    {
        return mApiService;
    }

    /**
     * A Tag to add to all async requests. This must be unique for all Activity types
     *
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getTaskTag();

    @Override
    protected void onStop() {
        super.onStop();
        //TODO  Cancel all pending requests because they shouldn't be delivered

        setProgressBarIndeterminateVisibility(false);
    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(displayOptions, ACTION_BAR_DISPLAY_MASK);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {


        //Fetch the current primary fragment. If that will handle the Menu click,
        // pass it to that one
        final AbstractLavocalFragment currentMainFragment = (AbstractLavocalFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.frame_content);

        boolean handled = false;
        if (currentMainFragment != null) {
            handled = currentMainFragment.onOptionsItemSelected(item);
        }

        if (!handled) {
            // To provide Up navigation
            if (item.getItemId() == android.R.id.home) {

                doUpNavigation();
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }

        }

        return handled;


    }

    /**
     * Moves up in the hierarchy using the Support meta data specified in manifest
     */
    private void doUpNavigation() {
        final Intent upIntent = NavUtils.getParentActivityIntent(this);

        if (upIntent == null) {

            NavUtils.navigateUpFromSameTask(this);

        } else {
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a
                // new
                // task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                                // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
        }

    }
    /**
     * Helper method to load fragments into layout
     *
     * @param containerResId The container resource Id in the content view into which to load the
     *                       fragment
     * @param fragment       The fragment to load
     * @param tag            The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the backstack
     * @param backStackTag   The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                             final AbstractLavocalFragment fragment, final String tag,
                             final boolean addToBackStack, final String backStackTag) {

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager
                .beginTransaction();
        final FragmentTransition fragmentTransition = fragment.getClass()
                .getAnnotation(
                        FragmentTransition.class);
        if (fragmentTransition != null) {

            transaction.setCustomAnimations(fragmentTransition.enterAnimation(), fragmentTransition
                    .exitAnimation(), fragmentTransition
                    .popEnterAnimation(), fragmentTransition
                    .popExitAnimation());

        }

        transaction.replace(containerResId, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(backStackTag);
        }
        transaction.commit();
    }

    /**
     * Returns the current master fragment. In single pane layout, this is the fragment in the main
     * content. In a multi-pane layout, returns the fragment in the master container, which is the
     * one responsible for coordination
     *
     * @return <code>null</code> If no fragment is loaded,the {@link AbstractLavocalFragment}
     * implementation which is the current master fragment otherwise
     */
    public AbstractLavocalFragment getCurrentMasterFragment() {

        return (AbstractLavocalFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_content);

    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getFirstName());
    }


    @Override
    public void success(Object o, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {

        final AbstractLavocalFragment fragment = getCurrentMasterFragment();

        if ((fragment != null) && fragment.isVisible()) {
            if (fragment.willHandleDialog(dialog)) {
                fragment.onDialogClick(dialog, which);
            }
        }
    }


    @Override
    public void onBackPressed() {

        /* Get the reference to the current master fragment and check if that will handle
        onBackPressed. If yes, do nothing. Else, let the Activity handle it. */
        final AbstractLavocalFragment masterFragment = getCurrentMasterFragment();

        boolean handled = false;
        if (masterFragment != null && masterFragment.isResumed()) {
            handled = masterFragment.onBackPressed();
        }

        if (!handled) {
            super.onBackPressed();
        }
    }
}
