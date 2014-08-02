package com.lovocal.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lovocal.R;
import com.lovocal.utils.AppConstants;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class ImageViewFragment extends AbstractLavocalFragment{
    private int mPosition;
    private ImageView mFeatureImage;


    public static ImageViewFragment newInstance(int position) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle args=new Bundle(1);
        args.putInt(AppConstants.Keys.IMAGEFEATURE_POSITION,position);
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
        }

        switch(mPosition) {
            case 0:
                mFeatureImage.setImageDrawable(getResources().getDrawable(R.drawable.a1));
                break;
            case 1:
                mFeatureImage.setImageDrawable(getResources().getDrawable(R.drawable.a2));
                break;
            case 2:
                mFeatureImage.setImageDrawable(getResources().getDrawable(R.drawable.a3));
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
