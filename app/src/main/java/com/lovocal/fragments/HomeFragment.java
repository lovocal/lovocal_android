package com.lovocal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lovocal.R;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class HomeFragment extends AbstractLavocalFragment{


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_home, container, false);


        return contentView;

    }
    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static HomeFragment newInstance() {
        HomeFragment f = new HomeFragment();
        return f;
    }


}
