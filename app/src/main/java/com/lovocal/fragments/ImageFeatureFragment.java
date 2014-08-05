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
import com.lovocal.retromodels.response.BannerResponseModel;
import com.lovocal.utils.AppConstants;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class ImageFeatureFragment extends AbstractLavocalFragment {

    private ImageSwipeAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
    private CirclePageIndicator mCircularIndicator;
    private ArrayList<String> mImages = new ArrayList<String>();
    private int mImageCount;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_image_feature, container, false);


        Bundle extras = getArguments();

        mImages = extras.getStringArrayList(AppConstants.Keys.IMAGE_URLS);
        mImageCount = extras.getInt(AppConstants.Keys.BANNER_COUNT);
        mPager = (ViewPager) contentView.findViewById(R.id.pager);
        mCircularIndicator = (CirclePageIndicator) contentView.findViewById(R.id.indicator);

        mAdapter = new ImageSwipeAdapter(getChildFragmentManager());
        mPager.setAdapter(mAdapter);

        mIndicator = mCircularIndicator;
        mCircularIndicator.setViewPager(mPager);
        mCircularIndicator.setSnap(true);


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


        private int mCount = mImageCount;

        public ImageSwipeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageViewFragment.newInstance(position, mImages);
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
