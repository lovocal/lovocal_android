package com.lovocal.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lovocal.R;
import com.lovocal.bus.SlidePanelUpdate;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.Logger;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

/**
 * Created by anshul1235 on 14/07/14.
 */
public class SearchServiceFragment extends AbstractLavocalFragment implements ViewPager.OnPageChangeListener,
        PanelSlideListener{


    private String                           TAG="SearchServiceFragment";
    private   TitlePageIndicator             mIndicator;
    private ArrayList<String>                mTitles;
    private FragmentStatePagerAdapter        mPagerdapter;
    private ViewPager                        mPager;
    private Bundle                           mArgs;
    private int                              mDefaultPage=AppConstants.DEFAULT_SERVICE_PAGER_NUMBER;

    /**
     * Used to provide a slide up UI companent to place the Query for particular category
     */
    private SlidingUpPanelLayout mSlidingLayout;

    /**
     * this will tell if the panel is open or not for the savedinstanceState
     */
    private boolean mPanelOpen;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_service_screen, container, false);


        final Bundle extras = getArguments();

        if (extras != null && extras.containsKey(AppConstants.Keys.CATEGORY_NAME)) {
            mArgs=extras;
        }
        else
        {
            //Should not happen
        }

        mTitles= new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.search_services_titles)));

        mSlidingLayout = (SlidingUpPanelLayout) contentView
                .findViewById(R.id.sliding_layout);
        mSlidingLayout.setPanelSlideListener(this);


        if (savedInstanceState == null) {
            final QueryServiceFragment fragment = new QueryServiceFragment();
            fragment.setArguments(mArgs);

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_user_profile, fragment, AppConstants.FragmentTags.BROADCAST_QUERY)
                    .commit();
        }

        else{
           mPanelOpen= savedInstanceState.getBoolean(AppConstants.Keys.PANEL_OPEN);
        }

        mPagerdapter = new TestAdapter(getFragmentManager());

        mPager = (ViewPager)contentView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerdapter);

        mIndicator = (TitlePageIndicator)contentView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setCurrentItem(mDefaultPage);
        mIndicator.setOnPageChangeListener(this);

        if(mPanelOpen){
            mSlidingLayout.expandPane();

        }

        return contentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
            outState.putBoolean(AppConstants.Keys.PANEL_OPEN,mPanelOpen);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onPanelSlide(View view, float v) {

    }

    @Override
    public void onPanelCollapsed(View view) {
        //panel close post to QueryServiceFragment
        mPanelOpen=false;
        mBus.post(new SlidePanelUpdate(false));
    }

    @Override
    public void onPanelExpanded(View view) {
        //panel open post to QueryServiceFragment
        mPanelOpen=true;
        if(isAttached()) {
            mBus.post(new SlidePanelUpdate(true));
        }
    }

    @Override
    public void onPanelAnchored(View view) {

    }


    public class TestAdapter extends FragmentStatePagerAdapter{

        public TestAdapter(FragmentManager fm) {

            super(fm);
        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:

                    return ChatsFragment.newInstance();


                case 1:
                    return ServicesListFragment.newInstance(mArgs);

                default:
                    break;
            }
            return null;



        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

        @Override
        public int getCount() {
            return mTitles.size();
        }


    }
    /**
     * @param view The drag handle to be set for the Sliding Pane Layout
     */
    public void setDragHandle(View view) {

        Logger.v(TAG, "Setting Drag View %s", view.toString());
        mSlidingLayout.setDragView(view);
        // mSlidingLayout.setEnableDragViewTouchEvents(false);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                if (mSlidingLayout.isExpanded()) {
                    mSlidingLayout.collapsePane();
                }
                else {
                    getActivity().finish();
                }
                return true;
            }


            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
    @Override
    public boolean onBackPressed() {

        if (mSlidingLayout.isExpanded()) {
            mSlidingLayout.collapsePane();
            return true;
        } else {
            return super.onBackPressed();
        }
    }


}
