package com.lovocal.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lovocal.R;
import com.lovocal.retromodels.response.BannerResponseModel;
import com.lovocal.utils.AppConstants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class ImageViewFragment extends AbstractLavocalFragment{
    private int mPosition;
    private ImageView mFeatureImage;
    private ArrayList<String> mImageUrls;


    public static ImageViewFragment newInstance(int position,ArrayList<String> image_urls) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle args=new Bundle(1);
        args.putInt(AppConstants.Keys.IMAGEFEATURE_POSITION,position);
        args.putStringArrayList(AppConstants.Keys.IMAGE_URLS,image_urls);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_image, container, false);

        mFeatureImage=(ImageView)contentView.findViewById(R.id.feature_image);



        final Bundle extras = getArguments();



        if (extras != null && extras.containsKey(AppConstants.Keys.IMAGEFEATURE_POSITION)) {
            mPosition=extras.getInt(AppConstants.Keys.IMAGEFEATURE_POSITION);
            mImageUrls=extras.getStringArrayList(AppConstants.Keys.IMAGE_URLS);
        }

        switch(mPosition) {
            case 0:
                Picasso.with(getActivity())
                        .load(mImageUrls.get(mPosition))
                        .resizeDimen(R.dimen.services_image_width, R.dimen.services_image_height)
                        .centerCrop().into(mFeatureImage);
                break;
            case 1:
                Picasso.with(getActivity())
                        .load(mImageUrls.get(mPosition))
                        .resizeDimen(R.dimen.services_image_width, R.dimen.services_image_height)
                        .centerCrop().into(mFeatureImage);
                break;
            case 2:
                Picasso.with(getActivity())
                        .load(mImageUrls.get(mPosition))
                        .resizeDimen(R.dimen.services_image_width, R.dimen.services_image_height)
                        .centerCrop().into(mFeatureImage);
                break;
        }


        return contentView;

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static ImageViewFragment newInstance() {
        ImageViewFragment f = new ImageViewFragment();
        return f;
    }


}
