package com.lovocal.fragments;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.activities.ChatScreenActivity;
import com.lovocal.activities.CreateServiceActivity;
import com.lovocal.adapters.ServicesListGridAdapter;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.SQLiteLoader;
import com.lovocal.data.TableMyServices;
import com.lovocal.data.TableServices;
import com.lovocal.fragments.dialogs.BroadcastMessageDialogFragment;
import com.lovocal.http.HttpConstants;
import com.lovocal.retromodels.response.MyServicesResponseModel;
import com.lovocal.retromodels.response.SearchServiceResponseModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.Loaders;
import com.lovocal.utils.Logger;
import com.lovocal.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class MyServicesFragment extends AbstractLavocalFragment implements DBInterface.AsyncDbQueryCallback,
        LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "MyServicesFragment";

    /**
     * GridView into which the book content will be placed
     */
    private GridView mServicesListGridView;

    /**
     * {@link com.lovocal.adapters.CategoryListGridAdapter} instance for the categories
     */
    private ServicesListGridAdapter mServicesAdapter;

    /**
     * Flag to indigate pull to refresh so as to disable mEmptyView set for the list
     */
    private boolean mFromPullToRefresh;

    /**
     * {@link uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout} reference
     */
    private PullToRefreshLayout mPullToRefreshLayout;



    /**
     * this is the flag to check if the services are already fetched or not
     */
    private boolean mFetchedFlag;


    private Button mCreateService;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_my_services, container, false);

        mPullToRefreshLayout = (PullToRefreshLayout) contentView
                .findViewById(R.id.ptr_layout);

        mServicesListGridView = (GridView) contentView
                .findViewById(R.id.grid_services);

        mCreateService = (Button) contentView.findViewById(R.id.button_create_service);

        mCreateService.setOnClickListener(this);

        mServicesAdapter = new ServicesListGridAdapter(getActivity());
        mServicesListGridView.setAdapter(mServicesAdapter);
        mServicesListGridView.setOnItemClickListener(this);
        mServicesListGridView.setVerticalScrollBarEnabled(false);

        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                .listener(this).setup(mPullToRefreshLayout);


        refreshMyServices();



        return contentView;

    }

    @Override
    public void onResume() {
        super.onResume();

    }


    private void loadServices() {

        getLoaderManager().restartLoader(Loaders.LOAD_MY_SERVICES, null, this);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public void success(Object o, Response response) {

        if (o.getClass().equals(MyServicesResponseModel.class)) {
            mFetchedFlag = true;
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
                    if (mResponse.service_images.size() > 0) {
                        values.put(DatabaseColumns.IMAGE, mResponse.service_images.get(0).getImage());
                    }

                    DBInterface
                            .insertAsync(AppConstants.QueryTokens.INSERT_MY_SERVICES, getTaskTag(), null, TableMyServices.NAME, null, values, true, this);

                }
                if(isAttached()) {
                    loadServices();
                }

            }

        }

    }

    @Override
    public void failure(RetrofitError error) {
        //Log.d("failure", error.getMessage());
//        Toast.makeText(getActivity(),error.getMessage()+"",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

        if (taskId == AppConstants.QueryTokens.INSERT_SERVICES) {

        }
    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if (taskId == AppConstants.QueryTokens.DELETE_MY_SERVICES) {
            //
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
        if (loaderId == Loaders.LOAD_MY_SERVICES) {

            return new SQLiteLoader(getActivity(), false, TableMyServices.NAME, null,
                    null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == Loaders.LOAD_MY_SERVICES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (mFetchedFlag == false) {
                if (cursor.getCount() == 0) {
                    mFetchedFlag = true;

                    mApiService.getMyServices(this);
                }

            }
            {
                mServicesAdapter.swapCursor(cursor);
                mPullToRefreshLayout.setRefreshComplete();
            }

        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Loaders.LOAD_MY_SERVICES) {
            mServicesAdapter.swapCursor(null);
        }
    }

    @Override
    public void onRefreshStarted(View view) {


        if (view.getId() == R.id.grid_services) {
            mFetchedFlag = false;
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_MY_SERVICES, getTaskTag(), null, TableMyServices.NAME, null, null, false, this);

        }
    }

    private void refreshMyServices(){
        mFetchedFlag = false;
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_MY_SERVICES, getTaskTag(), null, TableMyServices.NAME, null, null, false, this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_create_service) {
            final Intent editProfileIntent = new Intent(getActivity(),
                    CreateServiceActivity.class);
            startActivityForResult(editProfileIntent, AppConstants.RequestCodes.CREATE_SERVICE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
