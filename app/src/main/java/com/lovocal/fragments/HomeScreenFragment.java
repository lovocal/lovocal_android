package com.lovocal.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
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
public class HomeScreenFragment extends AbstractLavocalFragment implements ViewPager.OnPageChangeListener {


    private TitlePageIndicator mIndicator;
    private ArrayList<String> mTitles;
    private FragmentStatePagerAdapter mPagerdapter;
    private int mPagerPosition = AppConstants.CATEGORY_PAGE;
    private ViewPager mPager;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_home_screen, container, false);


        final Bundle extras = getArguments();

        if (extras != null && extras.containsKey(AppConstants.Keys.PAGER_POSITION)) {
            mPagerPosition = extras.getInt(AppConstants.Keys.PAGER_POSITION);
        }


        mTitles = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.home_screen_titles)));


        mPagerdapter = new TestAdapter(getFragmentManager());

        mPager = (ViewPager) contentView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerdapter);

        mIndicator = (TitlePageIndicator) contentView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mPager.setCurrentItem(mPagerPosition);
        mIndicator.setOnPageChangeListener(this);

        return contentView;

    }


    @Override
    public void onResume() {
        super.onResume();
        if (SharedPreferenceHelper.getBoolean(R.string.pref_force_user_refetch)) {
            userRefresh(false);

            Bundle args = new Bundle(1);


            args.putInt(AppConstants.Keys.PAGER_POSITION, AppConstants.PROFILE_PAGE);

            loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                    .instantiate(getActivity(), HomeScreenFragment.class
                            .getName(), args), AppConstants.FragmentTags.HOME_SCREEN, false, null);


        }

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

    public class TestAdapter extends FragmentStatePagerAdapter {

        private FragmentManager mFragmentManager;
        private Fragment mLoginFragment;

        public TestAdapter(FragmentManager fm) {

            super(fm);
            mFragmentManager = fm;
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

            switch (position) {
                case 0:
                    if (isLoggedIn()) {
                        return MyProfileFragment.newInstance();
                    } else {
                        mLoginFragment = AskLoginFragment.newInstance();
                        return mLoginFragment;
                    }

                case 1:
                    return CategoryFragment.newInstance();

                case 2:
                    return ChatsFragment.newInstance();

                case 3:
                    return ChatsBusinessFragment.newInstance();


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


}
