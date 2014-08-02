package com.lovocal.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lovocal.R;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.SharedPreferenceHelper;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by anshul1235 on 14/07/14.
 */
public class ChatPagerFragment extends AbstractLavocalFragment implements ViewPager.OnPageChangeListener{


    private   TitlePageIndicator mIndicator;
    private ArrayList<String> mTitles;
    private FragmentStatePagerAdapter mPagerdapter;
    private int mPagerPosition=AppConstants.CATEGORY_PAGE;
    private ViewPager            mPager;

    /**
     * This will hold all the arguments sent by the ChatScreenActivity
     */
    private ArrayList<String> mUserIds,mChatIds;
    private ArrayList<String> mServiceIds=new ArrayList<String>();

    private  Bundle mExtras;

    private String mMId;

    private String mSenderType;



    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_chat_pager, container, false);


        Bundle extras=getArguments();


        if(extras.containsKey(AppConstants.Keys.SERVICE_ID_ARRAY)){
            mServiceIds=extras.getStringArrayList(AppConstants.Keys.SERVICE_ID_ARRAY);
        }
        if (extras != null &&savedInstanceState==null) {
            mPagerPosition=extras.getInt(AppConstants.Keys.PAGER_POSITION);
            mUserIds=extras.getStringArrayList(AppConstants.Keys.USER_ID_ARRAY);
            mChatIds=extras.getStringArrayList(AppConstants.Keys.CHAT_ID_ARRAY);
            mTitles=extras.getStringArrayList(AppConstants.Keys.CHAT_TITLES);
            mMId=extras.getString(AppConstants.Keys.MY_ID);
            mSenderType=extras.getString(AppConstants.Keys.SENDER_TYPE);

        }


        if(savedInstanceState!=null){

            mPagerPosition=savedInstanceState.getInt(AppConstants.Keys.PAGER_POSITION);
            mUserIds=savedInstanceState.getStringArrayList(AppConstants.Keys.USER_ID_ARRAY);
            mChatIds=savedInstanceState.getStringArrayList(AppConstants.Keys.CHAT_ID_ARRAY);
            mTitles=savedInstanceState.getStringArrayList(AppConstants.Keys.CHAT_TITLES);
            mMId=savedInstanceState.getString(AppConstants.Keys.MY_ID);
            mSenderType=savedInstanceState.getString(AppConstants.Keys.SENDER_TYPE);

        }


        mPagerdapter = new TestAdapter(getFragmentManager());

        mPager = (ViewPager)contentView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerdapter);

        mIndicator = (TitlePageIndicator)contentView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mPager.setCurrentItem(mPagerPosition);
        mIndicator.setOnPageChangeListener(this);

    return contentView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AppConstants.Keys.PAGER_POSITION,mPagerPosition);
        outState.putStringArrayList(AppConstants.Keys.USER_ID_ARRAY,mUserIds);
        outState.putStringArrayList(AppConstants.Keys.CHAT_ID_ARRAY,mChatIds);
        outState.putStringArrayList(AppConstants.Keys.CHAT_TITLES,mTitles);
        outState.putString(AppConstants.Keys.MY_ID,mMId);
        outState.putString(AppConstants.Keys.SENDER_TYPE, mSenderType);


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

    public class TestAdapter extends FragmentStatePagerAdapter{

        private FragmentManager mFragmentManager;
        private Fragment   mLoginFragment;
        public TestAdapter(FragmentManager fm) {

            super(fm);
            mFragmentManager=fm;
        }



        @Override
        public int getItemPosition(Object object) {
            if (mLoginFragment.equals(object)) {
                return POSITION_UNCHANGED;
            }
            return POSITION_NONE;
        }


        @Override
        public Fragment getItem(int position) {

            if(mServiceIds.size()==0) { //this means the user id is constant i.e opened from the ChatsFragment
                return ChatDetailsFragment.newInstance(mUserIds.get(position), mChatIds.get(position), mSenderType, mMId);
            }
            else
            {
                return ChatDetailsFragment.newInstance(mUserIds.get(position), mChatIds.get(position), mSenderType, mServiceIds.get(position));

            }


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

    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();
                return true;
            }


            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


}
