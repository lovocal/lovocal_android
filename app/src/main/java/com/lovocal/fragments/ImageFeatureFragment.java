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
public class ImageFeatureFragment extends AbstractLavocalFragment{

    private ImageSwipeAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
    private CirclePageIndicator mCircularIndicator;
    private ArrayList<String> mImages=new ArrayList<String>();




    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_image_feature, container, false);

        mPager = (ViewPager)contentView.findViewById(R.id.pager);
        mCircularIndicator = (CirclePageIndicator)contentView.findViewById(R.id.indicator);

        getBannersFromServer();

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
            return ImageViewFragment.newInstance(position,mImages);
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

    private void getBannersFromServer(){
        mApiService.getBanners(this);
    }

    @Override
    public void success(Object o, Response response) {
        if(o.getClass().equals(BannerResponseModel.class))
        {
            BannerResponseModel banners=((BannerResponseModel)o);

            if(banners.image_url!=null) {
                mImages.addAll(banners.image_url);


                mAdapter = new ImageSwipeAdapter(getChildFragmentManager());


                mPager.setAdapter(mAdapter);

                mIndicator = mCircularIndicator;
                mCircularIndicator.setViewPager(mPager);
                mCircularIndicator.setSnap(true);
            }


            }

        }

    @Override
    public void failure(RetrofitError error) {
    }
}
