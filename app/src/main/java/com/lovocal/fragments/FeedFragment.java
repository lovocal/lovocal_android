package com.lovocal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lovocal.R;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class FeedFragment extends AbstractLavocalFragment{


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_feed, container, false);


        return contentView;

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }
    public static FeedFragment newInstance() {
        FeedFragment f = new FeedFragment();
        return f;
    }
}
