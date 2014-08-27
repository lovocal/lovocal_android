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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.activities.ChatScreenActivity;
import com.lovocal.adapters.ServicesListGridAdapter;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.SQLiteLoader;
import com.lovocal.data.TableServices;
import com.lovocal.fragments.dialogs.BroadcastMessageDialogFragment;
import com.lovocal.retromodels.response.SearchServiceResponseModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.Loaders;
import com.lovocal.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import com.lovocal.http.HttpConstants;
import com.lovocal.utils.SharedPreferenceHelper;
import com.lovocal.utils.Utils;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class ServicesListFragment extends AbstractLavocalFragment implements DBInterface.AsyncDbQueryCallback,
        LoaderManager.LoaderCallbacks<Cursor>,OnRefreshListener,AdapterView.OnItemClickListener,View.OnClickListener {

    private static final String TAG = "ServicesListFragment";

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
     * cursor to load the categories so as to get ids of each in onclick
     */
    private Cursor mCursor;

    private BroadcastMessageDialogFragment mBroadcastMessageDialogFragment;

    private boolean mCategoryRefresh=true;

    private String  mCategoryName;
    private String  mCategoryId;

    /**
     * this will hold the latest location
     */
    private Location mLatestLocation;

    /**
     * this is the flag to check if the services are already fetched or not
     */
    private boolean mFetchedFlag;


    private int         mPageNumber=0;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_services, container, false);

        final Bundle extras = getArguments();

        if (extras != null && extras.containsKey(AppConstants.Keys.CATEGORY_NAME)) {
            mCategoryName=extras.getString(AppConstants.Keys.CATEGORY_NAME);
            mCategoryId=extras.getString(AppConstants.Keys.CATEGORY_ID);
        }
        else
        {
            //Should not happen
        }

        mPullToRefreshLayout = (PullToRefreshLayout) contentView
                .findViewById(R.id.ptr_layout);

        mServicesListGridView = (GridView) contentView
                .findViewById(R.id.grid_services);

        mServicesAdapter = new ServicesListGridAdapter(getActivity());
        mServicesListGridView.setAdapter(mServicesAdapter);
        mServicesListGridView.setOnItemClickListener(this);
        mServicesListGridView.setVerticalScrollBarEnabled(false);

        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                .listener(this).setup(mPullToRefreshLayout);


        loadServices();


        return contentView;

    }

    @Override
    public void onResume() {
        super.onResume();
        mLatestLocation = AppConstants.DeviceInfo.INSTANCE
                .getLatestLocation();

    }

    private void fetchServices(String mCategoryName,Location location)
    {


        final Map<String, String> params = new HashMap<String, String>(6);
        params.put(HttpConstants.SEARCH_LATITUDE, String.valueOf(location.getLatitude()));
        params.put(HttpConstants.SEARCH_LONGITUDE, String.valueOf(location.getLongitude()));
        params.put(HttpConstants.SEARCH_CATEGORY_NAME, mCategoryName);
        //params.put(HttpConstants.SEARCH_CATEGORY_ID, categoryId);
        //params.put(HttpConstants.SEARCH_DISTANCE, AppConstants.DISTANCE);
        params.put(HttpConstants.SEARCH_PAGE, mPageNumber+"");
        params.put(HttpConstants.SEARCH_PER,AppConstants.PER_VALUE);

        mApiService.getServices(params,this);
    }

    /**
     * Loads a chat directly. This is used in the case where the user directly taps on a chat button
     * on another user's profile page
     */
    private void loadChat(String userId,String chatName,String categoryId) {

        final String chatId = Utils
                .generateChatId(userId, AppConstants.UserInfo.INSTANCE.getId());

        loadChat(userId, chatId, chatName, categoryId);
    }

    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     *
     * @param userId The user Id of the chat to load
     * @param chatId The ID of the chat
     */
    private void loadChat(String userId, String chatId,String chatName,String categoryId) {


        ArrayList<String> userid=new ArrayList<String>();
        ArrayList<String> chatid=new ArrayList<String>();
        ArrayList<String> chatTitle=new ArrayList<String>();
        ArrayList<String> categoryIds=new ArrayList<String>();
        userid.add(userId);
        chatid.add(chatId);
        chatTitle.add(chatName);
        categoryIds.add(categoryId);

        final Intent chatScreenActivity = new Intent(getActivity(),
                ChatScreenActivity.class);
        chatScreenActivity.putStringArrayListExtra(AppConstants.Keys.USER_ID_ARRAY, userid);
        chatScreenActivity.putStringArrayListExtra(AppConstants.Keys.CHAT_ID_ARRAY, chatid);
        chatScreenActivity.putStringArrayListExtra(AppConstants.Keys.CHAT_TITLES, chatTitle);
        chatScreenActivity.putStringArrayListExtra(AppConstants.Keys.CATEGORY_ID_ARRAY, categoryIds);

        ArrayList<String> senderId=new ArrayList<String>();
        //we are passing an arraylist so that we can use the previous chat view any time
        //just by changing the fragment to be passed
        senderId.add(AppConstants.USER);
        chatScreenActivity.putExtra(AppConstants.Keys.SENDER_TYPE, senderId);
        chatScreenActivity.putExtra(AppConstants.Keys.MY_ID, AppConstants.UserInfo.INSTANCE.getId());
        startActivity(chatScreenActivity);
    }


    private void loadServices()
    {

        getLoaderManager().restartLoader(Loaders.LOAD_SERVICES, null, this);

    }


    public static ServicesListFragment newInstance(final Bundle categoryDetails) {

        ServicesListFragment f = new ServicesListFragment();
        f.setArguments(categoryDetails);
        return f;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public void success(Object o, Response response) {
        if(o.getClass().equals(SearchServiceResponseModel.class))
        {
            SearchServiceResponseModel mServicesModel=((SearchServiceResponseModel)o);
            //Toast.makeText(getActivity(), response.getBody().toString(), Toast.LENGTH_SHORT).show();
            mFetchedFlag=true;
            if(mServicesModel.search!=null) {
                //TODO make it a seperate function
                for (int i = 0; i < mServicesModel.search.size(); i++) {

                    ContentValues values = new ContentValues();
                    SearchServiceResponseModel.Search mResponse= mServicesModel.search.get(i);
                    values.put(DatabaseColumns.ID,mResponse.id);
                    values.put(DatabaseColumns.NAME, mResponse.business_name);
                    values.put(DatabaseColumns.MOBILE_NUMBER,mResponse.mobile_number);
                    values.put(DatabaseColumns.LATITUDE,mResponse.latitude);
                    values.put(DatabaseColumns.LONGITUDE,mResponse.longitude);
                    values.put(DatabaseColumns.COUNTRY,mResponse.country);
                    values.put(DatabaseColumns.CITY,mResponse.city);
                    values.put(DatabaseColumns.STATE,mResponse.state);
                    values.put(DatabaseColumns.ZIP_CODE,mResponse.zip_code);
                    values.put(DatabaseColumns.DESCRIPTION,mResponse.description);
                    values.put(DatabaseColumns.CUSTOMERCARE_NUMBER,mResponse.customer_care_number);
                    values.put(DatabaseColumns.LANDLINE_NUMBER,mResponse.landline_number);
                    values.put(DatabaseColumns.ADDRESS,mResponse.address);
                    values.put(DatabaseColumns.WEBSITE,mResponse.website);
                    values.put(DatabaseColumns.TWITTER_LINK,mResponse.twitter_link);
                    values.put(DatabaseColumns.FACEBOOK_LINK,mResponse.facebook_link);
                    values.put(DatabaseColumns.LINKEDIN_LINK,mResponse.linkedin_link);
                    if(mResponse.service_images.size()>0) {
                        values.put(DatabaseColumns.IMAGE, mResponse.service_images.get(0).getImage());
                    }
                    if (mResponse.list_cat_ids.size() > 0) {
                        final String[] tags = new String[mResponse.list_cat_ids.size()];
                        for(int j = 0;j<mResponse.list_cat_ids.size();j++)
                        {
                            tags[j]=mResponse.list_cat_ids.get(j);
                        }

                        values.put(DatabaseColumns.CATEGORIES, TextUtils
                                .join(AppConstants.CATEGORY_SEPERATOR, tags));
                    }


                    DBInterface
                            .insertAsync(AppConstants.QueryTokens.INSERT_SERVICES, getTaskTag(), null, TableServices.NAME, null, values, true, this);
                }
            }

        }

    }

    @Override
    public void failure(RetrofitError error) {
        //Log.d("failure", error.getMessage());
        Toast.makeText(getActivity(),error.getMessage()+"",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

        if(taskId==AppConstants.QueryTokens.INSERT_SERVICES)
        {

        }
    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if(taskId== AppConstants.QueryTokens.DELETE_SERVICES)
        {
            fetchServices(mCategoryName,mLatestLocation);
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
        if (loaderId == Loaders.LOAD_SERVICES) {

            String selection= DatabaseColumns.CATEGORIES
                    + SQLConstants.EQUALS_ARG;

            Logger.d(TAG,mCategoryId);
            return new SQLiteLoader(getActivity(), false, TableServices.NAME,null,
                    selection,  new String[] {mCategoryId}, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == Loaders.LOAD_SERVICES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if(mFetchedFlag==false)
            {
                if(cursor.getCount()==0) {
                    mFetchedFlag=true;
                    fetchServices(mCategoryName, mLatestLocation);
                }

            }
            {
                mServicesAdapter.swapCursor(cursor);
                mCursor=cursor;
                mPullToRefreshLayout.setRefreshComplete();
            }

        }
    }

    /**
     * Show the dialog for the user to enter his email address
     */
    private void showBroadcastMessageDialog() {

        mBroadcastMessageDialogFragment = new BroadcastMessageDialogFragment();
        mBroadcastMessageDialogFragment
                .show(AlertDialog.THEME_HOLO_LIGHT, 0, R.string.query, R.string.broadcast,
                        R.string.cancel, 0, R.string.query_message_hint, getFragmentManager(), true,
                        AppConstants.FragmentTags.DIALOG_SEND_QUERY);

    }

    @Override
    public boolean willHandleDialog(final DialogInterface dialog) {

        if ((mBroadcastMessageDialogFragment != null)
                && mBroadcastMessageDialogFragment.getDialog()
                .equals(dialog)) {
            return true;
        }
        return false;
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mBroadcastMessageDialogFragment != null)
                && mBroadcastMessageDialogFragment.getDialog()
                .equals(dialog)) {

            if (which == DialogInterface.BUTTON_POSITIVE) {
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Loaders.LOAD_SERVICES) {
            mServicesAdapter.swapCursor(null);
        }
    }

    @Override
    public void onRefreshStarted(View view) {



        if (view.getId() == R.id.grid_services) {
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_SERVICES,getTaskTag(),null,TableServices.NAME,null,null,false,this);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCursor.moveToPosition(position);


        loadChat(mCursor.getString(mCursor
                .getColumnIndex(
                        DatabaseColumns
                                .ID
                )),mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.NAME)),mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.CATEGORIES)));
       // showBroadcastMessageDialog();

//        final Intent searchService = new Intent(getActivity(),
//                SearchServiceActivity.class);
//        startActivity(searchService);
    }

    @Override
    public void onClick(View v) {

    }
}
