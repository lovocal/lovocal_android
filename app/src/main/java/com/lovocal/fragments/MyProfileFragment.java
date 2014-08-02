package com.lovocal.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.activities.CreateServiceActivity;
import com.lovocal.activities.EditProfileActivity;
import com.lovocal.adapters.ProfileFragmentsAdapter;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.SQLiteLoader;
import com.lovocal.data.TableCategories;
import com.lovocal.data.TableMyServices;
import com.lovocal.data.TableServices;
import com.lovocal.retromodels.response.CategoryListResponseModel;
import com.lovocal.retromodels.response.MyServicesResponseModel;
import com.lovocal.retromodels.response.SearchServiceResponseModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.Logger;
import com.lovocal.utils.SharedPreferenceHelper;
import com.lovocal.widgets.CircleImageView;
import com.squareup.picasso.Picasso;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class MyProfileFragment extends AbstractLavocalFragment implements View.OnClickListener,
        DBInterface.AsyncDbQueryCallback,LoaderManager.LoaderCallbacks<Cursor>,TabHost.OnTabChangeListener,
        ViewPager.OnPageChangeListener {

    public static final String TAG="MyProfileFragment";
    private CircleImageView mProfileImageView;
    private TextView mName;

    private FragmentTabHost mTabHost;
    private ViewPager mViewPager;

    private ProfileFragmentsAdapter mProfileFragmentsAdapter;


    private boolean mHasLoadedAllItems=false;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_my_profile, container, false);

        mProfileImageView=(CircleImageView)contentView.findViewById(R.id.image_user);
        mName=(TextView)contentView.findViewById(R.id.text_user_name);

        mTabHost = (FragmentTabHost) contentView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec(AppConstants.FragmentTags.ABOUT_ME)
                        .setIndicator(getString(R.string.about_me)), DummyFragment.class,
                null);
        mTabHost.addTab(mTabHost.newTabSpec(AppConstants.FragmentTags.MY_SERVICES)
                        .setIndicator(getString(R.string.my_services)), DummyFragment.class,
                null);
        mTabHost.setOnTabChangedListener(this);

        mViewPager = (ViewPager) contentView.findViewById(R.id.pager_profile);
        mProfileFragmentsAdapter = new ProfileFragmentsAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mProfileFragmentsAdapter);
        mViewPager.setOnPageChangeListener(this);



        mName.setText(SharedPreferenceHelper.getString(R.string.pref_first_name)+" "+
                SharedPreferenceHelper.getString(R.string.pref_last_name));

        Logger.d(TAG,AppConstants.UserInfo.INSTANCE.getProfilePicture());
        Picasso.with(getActivity())
                .load(AppConstants.UserInfo.INSTANCE.getProfilePicture())
                .resizeDimen(R.dimen.user_image_size_profile,
                        R.dimen.user_image_size_profile)
                .centerCrop()
                .into(mProfileImageView.getTarget());

        if(savedInstanceState==null)
        {

            getMyServices();
        }

        mProfileFragmentsAdapter.getFragmentAtPosition(mViewPager
                .getCurrentItem());



        //loadMyServices();
        return contentView;

    }

    @Override
    public void onTabChanged(String tabId) {

        final int currentViewPagerItem = mViewPager.getCurrentItem();
        if (tabId.equals(AppConstants.FragmentTags.ABOUT_ME)) {

            if (currentViewPagerItem != 0) {
                mViewPager.setCurrentItem(0, true);
            }
        } else if (tabId.equals(AppConstants.FragmentTags.MY_SERVICES)) {

            if (currentViewPagerItem != 1) {
                mViewPager.setCurrentItem(1, true);
            }
        }
    }
    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }
    @Override
    public void onPageSelected(int position) {

        if (mTabHost.getCurrentTab() != position) {
            mTabHost.setCurrentTab(position);
        }

    }
    @Override
    public void onPageScrollStateChanged(int i) {

    }

    /**
     * Empty dummy fragment to provide for the TabHost
     *
     */
    public static class DummyFragment extends Fragment {

        public DummyFragment() {
        }

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    public static MyProfileFragment newInstance() {
        MyProfileFragment f = new MyProfileFragment();
        return f;
    }

    @Override
    public void onClick(View v) {

    }

    public void getMyServices(){

        mApiService.getMyServices(this);
        //DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_MY_SERVICES,getTaskTag(),null,TableMyServices.NAME,null,null,false,this);

    }

    @Override
    public void success(Object o, Response response) {
        if (o.getClass().equals(MyServicesResponseModel.class)) {
            MyServicesResponseModel mServicesModel = ((MyServicesResponseModel) o);
            if (mServicesModel.services != null) {
                //TODO make it a seperate function
                for (int i = 0; i < mServicesModel.services.size(); i++) {



                    ContentValues values = new ContentValues();
                    MyServicesResponseModel.Services mResponse = mServicesModel.services.get(i);

                    values.put(DatabaseColumns.ID, mResponse.id);
                    values.put(DatabaseColumns.NAME, mResponse.business_name);
                    values.put(DatabaseColumns.MOBILE_NUMBER, mResponse.mobile_number);
                    values.put(DatabaseColumns.LATITUDE, mResponse.latitude);
                    values.put(DatabaseColumns.LONGITUDE, mResponse.longitude);
                    values.put(DatabaseColumns.COUNTRY, mResponse.country);
                    values.put(DatabaseColumns.CITY, mResponse.city);
                    values.put(DatabaseColumns.STATE, mResponse.state);
                    values.put(DatabaseColumns.ZIP_CODE, mResponse.zip_code);
                    values.put(DatabaseColumns.DESCRIPTION, mResponse.description);
                    values.put(DatabaseColumns.CUSTOMERCARE_NUMBER, mResponse.customer_care_number);
                    values.put(DatabaseColumns.LANDLINE_NUMBER, mResponse.landline_number);
                    values.put(DatabaseColumns.ADDRESS, mResponse.address);
                    values.put(DatabaseColumns.WEBSITE, mResponse.website);
                    values.put(DatabaseColumns.TWITTER_LINK, mResponse.twitter_link);
                    values.put(DatabaseColumns.FACEBOOK_LINK, mResponse.facebook_link);
                    values.put(DatabaseColumns.LINKEDIN_LINK, mResponse.linkedin_link);

                    if (mResponse.listing_categories.size() > 0) {
                        final String[] tags = new String[mResponse.listing_categories.size()];
                        for (int j = 0; j < mResponse.listing_categories.size(); j++) {
                            tags[j] = mResponse.listing_categories.get(j);
                        }

                        values.put(DatabaseColumns.CATEGORIES, TextUtils
                                .join(AppConstants.CATEGORY_SEPERATOR, tags));
                    }


                    mHasLoadedAllItems=true;
                    DBInterface
                            .insertAsync(AppConstants.QueryTokens.INSERT_MY_SERVICES, getTaskTag(), null, TableMyServices.NAME, null, values, true, this);

                }

            }
            if(isAttached()) {
                loadMyServices();
            }
        }

    }
    @Override
    public void failure(RetrofitError error) {
        super.failure(error);
    }


    private void loadMyServices(){

            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_MY_SERVICES, null, this);

    }
    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {
        if(taskId== AppConstants.QueryTokens.DELETE_MY_SERVICES){
            mApiService.getMyServices(this);

        }

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_SERVICES) {
            return new SQLiteLoader(getActivity(), false, TableServices.NAME, null,
                    null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_SERVICES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_SERVICES) {
   //         mServicesAdapter.swapCursor(null);
        }
    }


}
