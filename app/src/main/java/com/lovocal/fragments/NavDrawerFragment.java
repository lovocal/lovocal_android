
package com.lovocal.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lovocal.R;
import com.lovocal.activities.AbstractLavocalActivity;
import com.lovocal.activities.AuthActivity;
import com.lovocal.activities.EditProfileActivity;
import com.lovocal.activities.HomeActivity;
import com.lovocal.adapters.NavDrawerAdapter;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.SharedPreferenceHelper;


/**
 * Fragment to load in the Navigation Drawer Created by vinaysshenoy on 29/6/14.
 */
public class NavDrawerFragment extends AbstractLavocalFragment implements AdapterView
        .OnItemClickListener {

    private static final String TAG = "NavDrawerFragment";



    /**
     * ListView to provide Nav drawer content
     */
    private ListView                 mListView;
    /**
     * Drawer Adapter to provide the list view options
     */
    private NavDrawerAdapter         mDrawerAdapter;
    /**
     * Callback will be triggered whenever the Nav drawer takes an action.  Use to close the drawer
     * layout
     */
    private INavDrawerActionCallback mNavDrawerActionCallback;
    /**
     * Callback for delaying the running of nav drawer actions. This is so that the drawer can be
     * closed without jank
     */
    private Handler                  mHandler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mHandler = new Handler();

        mListView = (ListView) inflater.inflate(R.layout.fragment_nav_drawer, container, false);
        mDrawerAdapter = new NavDrawerAdapter(getActivity(), R.array.nav_drawer_primary,
                                              R.array.nav_drawer_secondary);



        mListView.setAdapter(mDrawerAdapter);

        mListView.setOnItemClickListener(this);
        return mListView;
    }







    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent == mListView) {
            final Runnable launchRunnable = makeRunnableForNavDrawerClick(position);
            if (launchRunnable != null) {
                //Give time for drawer to close before performing the action
                mHandler.postDelayed(launchRunnable, 250);
            }
            if (mNavDrawerActionCallback != null) {
                mNavDrawerActionCallback.onActionTaken();
            }
        }
    }

    /**
     * Creates a {@link Runnable} for positing to the Handler for launching the Navigation Drawer
     * click
     *
     * @param position The nav drawer item that was clicked
     * @return a {@link Runnable} to be posted to the Handler thread
     */
    private Runnable makeRunnableForNavDrawerClick(final int position) {

        Runnable runnable = null;
        final AbstractLavocalFragment masterFragment = ((AbstractLavocalActivity) getActivity())
                .getCurrentMasterFragment();
        final Activity activity = getActivity();

        switch (position) {

            //Log In
            case 0: {
                final Intent loginIntent = new Intent(getActivity(),
                        AuthActivity.class);
                startActivityForResult(loginIntent, AppConstants.RequestCodes.LOGIN);

                break;

            }

            //logout
            case 1: {

                AppConstants.UserInfo.INSTANCE.reset();

                SharedPreferenceHelper.set(R.string.pref_auth_token,"");
                SharedPreferenceHelper.set(R.string.pref_first_name,"");
                SharedPreferenceHelper.set(R.string.pref_last_name,"");
                SharedPreferenceHelper.set(R.string.pref_profile_image,"");
                SharedPreferenceHelper.set(R.string.pref_user_id,"");
                SharedPreferenceHelper.set(R.string.pref_email,"");
                SharedPreferenceHelper.set(R.string.pref_mobile_number,"");

                Bundle args=new Bundle(1);

                if(getActivity().getClass()== HomeActivity.class)
                {
                    args.putInt(AppConstants.Keys.PAGER_POSITION,AppConstants.PROFILE_PAGE);
                    loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                            .instantiate(getActivity(), HomeScreenFragment.class
                                    .getName(), args), AppConstants.FragmentTags.LOGIN, false, null);  }
                else
                {
                    userRefresh(true);
                    getActivity().finish();
                }





                break;
            }

            //editprofile
            case 2: {
                final Intent editProfileIntent = new Intent(getActivity(),
                        EditProfileActivity.class);
                startActivityForResult(editProfileIntent, AppConstants.RequestCodes.EDIT_PROFILE);

                break;
            }

            default: {
                runnable = null;
            }
        }

        return runnable;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof INavDrawerActionCallback)) {
            throw new IllegalArgumentException(
                    "Activity " + activity.toString() + " must implement INavDrawerActionCallback");
        }

        mNavDrawerActionCallback = (INavDrawerActionCallback) activity;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    /**
     * Interface that is called when the Navigation Drawer performs an Action
     */
    public static interface INavDrawerActionCallback {
        public void onActionTaken();
    }



}
