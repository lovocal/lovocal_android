package com.lovocal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lovocal.R;
import com.lovocal.activities.EditProfileActivity;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.Logger;
import com.lovocal.utils.SharedPreferenceHelper;
import com.lovocal.widgets.CircleImageView;
import com.squareup.picasso.Picasso;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class AboutMeFragment extends AbstractLavocalFragment implements View.OnClickListener {

    public static final String TAG = "AboutMeFragment";
    private TextView mMobileNumber, mDescription;

    private Button mButtonEditProfile;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_aboutme, container, false);

        mMobileNumber = (TextView) contentView.findViewById(R.id.text_mobile_number);
        mDescription = (TextView) contentView.findViewById(R.id.description);


        mButtonEditProfile = (Button) contentView.findViewById(R.id.button_edit_profile);
        mButtonEditProfile.setOnClickListener(this);


        mMobileNumber.setText(SharedPreferenceHelper.getString(R.string.pref_mobile_number));
        mDescription.setText(SharedPreferenceHelper.getString(R.string.pref_description));

        Logger.d(TAG, AppConstants.UserInfo.INSTANCE.getProfilePicture());


        return contentView;

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static AboutMeFragment newInstance() {
        AboutMeFragment f = new AboutMeFragment();
        return f;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_edit_profile) {
            final Intent editProfileIntent = new Intent(getActivity(),
                    EditProfileActivity.class);
            startActivityForResult(editProfileIntent, AppConstants.RequestCodes.EDIT_PROFILE);
        }
    }
}
