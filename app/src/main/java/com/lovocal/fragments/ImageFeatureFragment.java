package com.lovocal.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lovocal.R;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class ImageFeatureFragment extends AbstractLavocalFragment{

    ImageSwipeAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;




    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_image_feature, container, false);
        mAdapter = new ImageSwipeAdapter(getChildFragmentManager());

        mPager = (ViewPager)contentView.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        CirclePageIndicator indicator = (CirclePageIndicator)contentView.findViewById(R.id.indicator);
        mIndicator = indicator;
        indicator.setViewPager(mPager);
        indicator.setSnap(true);

        return contentView;

    }
    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static ImageFeatureFragment newInstance() {
        ImageFeatureFragment f = new ImageFeatureFragment();
        return f;
    }

    class ImageSwipeAdapter extends FragmentPagerAdapter implements IconPagerAdapter {


        private int mCount = 3;

        public ImageSwipeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageViewFragment.newInstance(position);
        }

        @Override
        public int getIconResId(int index) {
            return 0;
        }

        @Override
        public int getCount() {
            return mCount;
        }


    }
}
