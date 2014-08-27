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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.activities.SearchServiceActivity;
import com.lovocal.adapters.CategoryListGridAdapter;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLConstants;
import com.lovocal.data.SQLiteLoader;
import com.lovocal.data.TableCategories;
import com.lovocal.fragments.dialogs.BroadcastMessageDialogFragment;
import com.lovocal.retromodels.response.BannerResponseModel;
import com.lovocal.retromodels.response.CategoryListResponseModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.Loaders;
import com.lovocal.utils.Logger;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class CategoryFragment extends AbstractLavocalFragment implements DBInterface.AsyncDbQueryCallback,
        LoaderManager.LoaderCallbacks<Cursor>,OnRefreshListener,AdapterView.OnItemClickListener,View.OnClickListener {

    private static final String TAG = "CategoryFragment";

    /**
     * GridView into which the book content will be placed
     */
    private GridView mCategoryListGridView;

    /**
     * {@link CategoryListGridAdapter} instance for the categories
     */
    private CategoryListGridAdapter mCategoryAdapter;

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

    /**
     * my last fetched location
     */
    private Location mLastFetchedLocation;

    private boolean mCategoryRefresh=true;

    private boolean mHasLoadedAllItems;

    private ArrayList<String> mImages=new ArrayList<String>();


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_category, container, false);



        // load the ImageFeatureFragment
        if (savedInstanceState == null) {
            getBannersFromServer();
           //TODO add to outstate
        }
        else
        {
            mLastFetchedLocation = savedInstanceState
                    .getParcelable(AppConstants.Keys.LAST_FETCHED_LOCATION);
            mHasLoadedAllItems = savedInstanceState
                    .getBoolean(AppConstants.Keys.HAS_LOADED_ALL_ITEMS);
        }

        mPullToRefreshLayout = (PullToRefreshLayout) contentView
                .findViewById(R.id.ptr_layout);

        mCategoryListGridView = (GridView) contentView
                .findViewById(R.id.grid_categories);

        mCategoryAdapter = new CategoryListGridAdapter(getActivity());
        mCategoryListGridView.setAdapter(mCategoryAdapter);
        mCategoryListGridView.setOnItemClickListener(this);
        mCategoryListGridView.setVerticalScrollBarEnabled(false);

        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                .listener(this).setup(mPullToRefreshLayout);


            loadCategories();


        return contentView;

    }

    private void getBannersFromServer(){
        mApiService.getBanners(this);
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AppConstants.Keys.HAS_LOADED_ALL_ITEMS, mHasLoadedAllItems);

    }

    private void fetchCategories()
    {
        mApiService.getCategories(this);
    }

    private void loadCategories()
    {

        getLoaderManager().restartLoader(Loaders.LOAD_CATEGORIES, null, this);

    }


    public static CategoryFragment newInstance() {
        CategoryFragment f = new CategoryFragment();
        return f;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public void success(Object o, Response response) {
        if(o.getClass().equals(CategoryListResponseModel.class))
        {
            CategoryListResponseModel mCategoryModel=((CategoryListResponseModel)o);

            if(mCategoryModel.listing_categories!=null) {
                for (int i = 0; i < mCategoryModel.listing_categories.size(); i++) {

                    mHasLoadedAllItems=true;
                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, mCategoryModel.listing_categories.get(i).id);
                    values.put(DatabaseColumns.CATEGORY_NAME, mCategoryModel.listing_categories.get(i).name);
                    values.put(DatabaseColumns.CATEGORY_IMAGE, mCategoryModel.listing_categories.get(i).image_url);
                    final String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
                    final String[] args = new String[1];
                    args[0]=mCategoryModel.listing_categories.get(i).id;

                    DBInterface
                            .insertAsync(AppConstants.QueryTokens.INSERT_CATEGORIES, getTaskTag(), null, TableCategories.NAME, null, values, true, this);
                }
            }

        }
        else  if(o.getClass().equals(BannerResponseModel.class))
        {
            BannerResponseModel banners=((BannerResponseModel)o);

            for(int i=0;i<banners.banners.size();i++){
                mImages.add(banners.banners.get(i).image_url);
            }

            Logger.d(TAG, "image count = %s", banners.banners.get(0).image_url);
            final ImageFeatureFragment imageFeatureFragment = new ImageFeatureFragment();

            Bundle args=new Bundle(2);
            args.putStringArrayList(AppConstants.Keys.IMAGE_URLS,mImages);
            args.putInt(AppConstants.Keys.BANNER_COUNT,mImages.size());

            imageFeatureFragment.setArguments(args);

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_images, imageFeatureFragment, AppConstants.FragmentTags.CHAT_DETAILS)
                    .commit();


        }



    }

    @Override
    public void failure(RetrofitError error) {
        Log.e("failure", error.getMessage());
        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

        if(taskId==AppConstants.QueryTokens.INSERT_CATEGORIES)
        {

        }
    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if(taskId== AppConstants.QueryTokens.DELETE_CATEGORIES)
        {
            fetchCategories();
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
        if (loaderId == Loaders.LOAD_CATEGORIES) {
            return new SQLiteLoader(getActivity(), false, TableCategories.NAME, null,
                    null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == Loaders.LOAD_CATEGORIES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if(cursor.getCount()==0)
            {
                    fetchCategories();

            }
            {
                mCategoryAdapter.swapCursor(cursor);
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
        if (loader.getId() == Loaders.LOAD_CATEGORIES) {
            mCategoryAdapter.swapCursor(null);
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        if (view.getId() == R.id.grid_categories) {
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_CATEGORIES,getTaskTag(),null,TableCategories.NAME,null,null,false,this);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    Toast.makeText(getActivity(),mCursor.getString(mCursor
            .getColumnIndex(DatabaseColumns.CATEGORY_NAME)),Toast.LENGTH_SHORT).show();


        final Intent searchService = new Intent(getActivity(),
                SearchServiceActivity.class);
        searchService.putExtra(AppConstants.Keys.CATEGORY_NAME,mCursor.getString(mCursor
                .getColumnIndex(DatabaseColumns.CATEGORY_NAME)));

        searchService.putExtra(AppConstants.Keys.CATEGORY_ID,mCursor.getString(mCursor
                .getColumnIndex(DatabaseColumns.ID)));
        startActivity(searchService);
    }

    @Override
    public void onClick(View v) {

    }
}
