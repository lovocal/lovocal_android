package com.lovocal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lovocal.LavocalApplication;
import com.lovocal.R;
import com.lovocal.bus.RestAdapterUpdate;
import com.lovocal.bus.SmsVerification;
import com.lovocal.retromodels.request.UserDetailsRequestModel;
import com.lovocal.retromodels.request.VerifyUserRequestModel;
import com.lovocal.retromodels.response.CreateUserResponseModel;
import com.lovocal.retromodels.response.VerifySmsResponseModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.UserInfo;
import com.lovocal.utils.Logger;
import com.lovocal.utils.SharedPreferenceHelper;
import com.lovocal.utils.Utils;
import com.lovocal.widgets.CircleImageView;
import com.squareup.otto.Subscribe;

import java.security.NoSuchAlgorithmException;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by anshul1235 on 15/07/14.
 */


public class LoginFragment extends AbstractLavocalFragment implements View.OnClickListener {

    private CircleImageView mProfileImage;
    private EditText mMobileNumber;
    private Button mActivateButton;
    private boolean mVerificationSent = false;
    private boolean mFragmentPaused = false;
    private String TAG = "LoginFragment";

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_login, container, false);

        if (savedInstanceState == null) {
            mBus.register(this);
        }


        mMobileNumber = (EditText) contentView.findViewById(R.id.edit_mobilenumber);
        mActivateButton = (Button) contentView.findViewById(R.id.button_activate);
        mActivateButton.setOnClickListener(this);


        return contentView;

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
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

    public static LoginFragment newInstance() {
        LoginFragment f = new LoginFragment();
        return f;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_activate) {
            if (AppConstants.DeviceInfo.INSTANCE.getLatestLocation().getLatitude() != 0.0) {

                try {
                    UserInfo.INSTANCE.setDeviceId(Settings.Secure.getString(getActivity()
                            .getContentResolver(), Settings.Secure.ANDROID_ID) + Utils.sha1(mMobileNumber.getText().toString()));

                    SharedPreferenceHelper.set(R.string.pref_device_id, UserInfo.INSTANCE.getDeviceId());

                } catch (NoSuchAlgorithmException e) {
                    //should not happen
                    Logger.e(TAG, e.getMessage());
                }

                if ((!TextUtils.isEmpty(mMobileNumber.getText().toString()))
                        && (TextUtils.getTrimmedLength(mMobileNumber.getText().toString()) == 10)) {

                    UserDetailsRequestModel userDetails = new UserDetailsRequestModel();
                    userDetails.user.setMobile_number("+91" + mMobileNumber.getText().toString());

                    mApiService.createUser(userDetails, this);

                    mActivateButton.setEnabled(false);

                    getActivity().setProgressBarIndeterminateVisibility(true);
                } else {
                    Toast.makeText(getActivity(), "Please Enter ten digit number", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "Please Turn On Your Location", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void success(Object o, Response response) {
        if (o.getClass().equals(CreateUserResponseModel.class)) {


            CreateUserResponseModel userResponseModel = ((CreateUserResponseModel) o);
            UserInfo.INSTANCE.setEmail(userResponseModel.user.email);
            UserInfo.INSTANCE.setFirstName(userResponseModel.user.first_name);
            UserInfo.INSTANCE.setLastName(userResponseModel.user.last_name);
            UserInfo.INSTANCE.setId(userResponseModel.user.id);
            UserInfo.INSTANCE.setProfilePicture(userResponseModel.user.image_url);
            UserInfo.INSTANCE.setMobileNumber(userResponseModel.user.mobile_number);
            UserInfo.INSTANCE.setDescription(userResponseModel.user.description);
            Logger.d(TAG, userResponseModel.user.first_name + "");

            SharedPreferenceHelper.set(R.string.pref_first_name, userResponseModel.user.first_name);
            SharedPreferenceHelper.set(R.string.pref_last_name, userResponseModel.user.last_name);
            SharedPreferenceHelper.set(R.string.pref_profile_image, userResponseModel.user.image_url);
            SharedPreferenceHelper.set(R.string.pref_user_id, userResponseModel.user.id);
            SharedPreferenceHelper.set(R.string.pref_email, userResponseModel.user.email);
            SharedPreferenceHelper.set(R.string.pref_mobile_number, userResponseModel.user.mobile_number);
            SharedPreferenceHelper.set(R.string.pref_description,userResponseModel.user.description);


            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
            mActivateButton.setText(getResources().getString(R.string.verifying_tag));


        } else if (o.getClass().equals(VerifySmsResponseModel.class)) {


            VerifySmsResponseModel userResponseModel = ((VerifySmsResponseModel) o);
            UserInfo.INSTANCE.setAuthToken(userResponseModel.user.auth_token);
            Logger.d(TAG, userResponseModel.user.auth_token + "");

            SharedPreferenceHelper.set(R.string.pref_auth_token, userResponseModel.user.auth_token);


            Bundle args = new Bundle(1);

            args.putString(AppConstants.Keys.ID, userResponseModel.user.id);
            if (mVerificationSent) {

                mBus.post(new RestAdapterUpdate(UserInfo.INSTANCE.getAuthToken()));

                LavocalApplication.startChatService();


                if(mFragmentPaused){

                }
                else {


                    loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                            .instantiate(getActivity(), EditProfileFragment.class
                                    .getName(), args), AppConstants.FragmentTags.LOGIN, true, AppConstants.FragmentTags.EDIT_PROFILE);

                    getActivity().setProgressBarIndeterminateVisibility(false);
                }
            }

            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();


        }


    }

    @Override
    public void onPause() {
        super.onPause();
        mFragmentPaused=true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFragmentPaused=false;
        if(isVerified()){
            loadHomeScreen();
        }
    }

    @Override
    public void failure(RetrofitError error) {

        getActivity().setProgressBarIndeterminateVisibility(false);
        mActivateButton.setText(getResources().getString(R.string.activate_tag));
        mActivateButton.setEnabled(true);
        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Loads the {@link com.lovocal.fragments.HomeScreenFragment} into the fragment container
     */
    public void loadHomeScreen() {

        loadFragment(R.id.frame_content, (AbstractLavocalFragment) Fragment
                        .instantiate(getActivity(), HomeScreenFragment.class
                                .getName(), null), AppConstants.FragmentTags.HOME_SCREEN, false,
                null
        );

    }


    @Subscribe
    public void onSmsReceivedEvent(SmsVerification event) {
        Toast.makeText(getActivity(), event.sms, Toast.LENGTH_SHORT).show();
        Logger.d("HOMESCREEN", event.sms + " received");

        VerifyUserRequestModel verifyUserRequest = new VerifyUserRequestModel();

        verifyUserRequest.user.setMobile_number(UserInfo.INSTANCE.getMobileNumber());
        verifyUserRequest.user.setPhone_id(UserInfo.INSTANCE.getDeviceId());
        verifyUserRequest.user.setSms_serial_key(event.sms);


        mApiService.verifyUser(verifyUserRequest, this);

        mVerificationSent = true;


    }


}
