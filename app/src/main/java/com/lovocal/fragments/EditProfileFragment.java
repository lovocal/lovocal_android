package com.lovocal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.lovocal.R;
import com.lovocal.fragments.dialogs.SingleChoiceDialogFragment;
import com.lovocal.http.HttpConstants;
import com.lovocal.retromodels.request.UserDetailsWithImageRequestModel;
import com.lovocal.retromodels.request.UserDetailsWithoutImageRequestModel;
import com.lovocal.retromodels.response.CreateUserResponseModel;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.PhotoUtils;
import com.lovocal.utils.SharedPreferenceHelper;
import com.lovocal.widgets.CircleImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 18/07/14.
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
        popEnterAnimation = R.anim.zoom_in,
        popExitAnimation = R.anim.slide_out_to_right)
public class EditProfileFragment extends AbstractLavocalFragment implements View.OnClickListener{

    public static final String TAG="EditProfileFragment";

    private CircleImageView     mProfilePic;
    private EditText            mFirstName,mLastName,mDesciption;

    private Bitmap mCompressedPhoto;
    private File mAvatarfile;
    private Uri mCameraImageCaptureUri;
    private Uri    mGalleryImageCaptureUri;
    private boolean mWasProfileImageChanged = false;
    private TypedFile typefile;
    private final String mAvatarFileName = AppConstants.AVATOR_PROFILE_NAME+".jpg";



    private String mId;
    private Uri mPhotoUri;


    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE   = 3;
    /**
     * Reference to the Dialog Fragment for selecting the picture type
     */
    private SingleChoiceDialogFragment mChoosePictureDialogFragment;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View contentView = inflater
                .inflate(R.layout.fragment_profile_edit, container, false);

        final Bundle extras = getArguments();

        mAvatarfile = new File(Environment.getExternalStorageDirectory(), mAvatarFileName);


        if(extras!=null&&extras.containsKey(AppConstants.Keys.ID)&&savedInstanceState==null)
        {
            mId=extras.getString(AppConstants.Keys.ID);
        }

        if(savedInstanceState!=null){
            mId=savedInstanceState.getString(AppConstants.Keys.MY_ID);
        }



        mProfilePic=(CircleImageView)contentView.findViewById(R.id.image_profile_pic);

        mProfilePic.setOnClickListener(this);

        mFirstName=(EditText)contentView.findViewById(R.id.text_first_name);
        mLastName=(EditText)contentView.findViewById(R.id.text_last_name);
        mDesciption=(EditText)contentView.findViewById(R.id.text_about_me);


        mFirstName.setText(AppConstants.UserInfo.INSTANCE.getFirstName());
        mLastName.setText(AppConstants.UserInfo.INSTANCE.getLastName());

        if(AppConstants.UserInfo.INSTANCE.getProfilePicture().equals("")) {
            Picasso.with(getActivity())
                    .load(R.drawable.ic_launcher)
                    .resizeDimen(R.dimen.edit_profile_img_size,
                            R.dimen.edit_profile_img_size)
                    .centerCrop()
                    .into(mProfilePic.getTarget());
        }
        else
        {
            Picasso.with(getActivity())
                    .load(AppConstants.UserInfo.INSTANCE.getProfilePicture())
                    .resizeDimen(R.dimen.edit_profile_img_size,
                            R.dimen.edit_profile_img_size)
                    .centerCrop()
                    .into(mProfilePic.getTarget());
        }



        mCameraImageCaptureUri = Uri.fromFile(new File(android.os.Environment
                .getExternalStorageDirectory(),
               AppConstants.AVATOR_PROFILE_NAME+".jpg"));



        return contentView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AppConstants.Keys.MY_ID,mId);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();
                return true;
            }

            case R.id.action_profile_save: {

                getActivity().setProgressBarIndeterminateVisibility(true);


                UserDetailsWithoutImageRequestModel userDetailsWithoutImageModel = new UserDetailsWithoutImageRequestModel();
                userDetailsWithoutImageModel.user.setMobile_number(AppConstants.UserInfo.INSTANCE.getMobileNumber());
                userDetailsWithoutImageModel.user.setFirst_name(mFirstName.getText().toString());
                userDetailsWithoutImageModel.user.setLast_name(mLastName.getText().toString());
                userDetailsWithoutImageModel.user.setEmail(AppConstants.UserInfo.INSTANCE.getEmail());
                userDetailsWithoutImageModel.user.setDescription(mDesciption.getText().toString());

                if(mWasProfileImageChanged) {
                    TypedFile typedFile;
                    File photo;
                    photo = new File(mAvatarfile.getAbsolutePath());
                    typedFile = new TypedFile("application/octet-stream", photo);
                    UserDetailsWithImageRequestModel userDetailsWithImageModel= new UserDetailsWithImageRequestModel();

//                    userDetailsWithImageModel.user.setMobile_number(AppConstants.UserInfo.INSTANCE.getMobileNumber());
//                    userDetailsWithImageModel.user.setFirst_name(mFirstName.getText().toString());
//                    userDetailsWithImageModel.user.setLast_name(mLastName.getText().toString());
//                    userDetailsWithImageModel.user.setEmail(AppConstants.UserInfo.INSTANCE.getEmail());
//                    userDetailsWithImageModel.user.setDescription(mDesciption.getText().toString());
//                    userDetailsWithImageModel.user.setImage(typedFile);

                    final Map<String, String> params = new HashMap<String, String>(6);
                    params.put(HttpConstants.MOBILE_NUMBER, AppConstants.UserInfo.INSTANCE.getMobileNumber());
                    params.put(HttpConstants.FIRST_NAME, mFirstName.getText().toString());
                    params.put(HttpConstants.LAST_NAME, mLastName.getText().toString());
                    params.put(HttpConstants.EMAIL, AppConstants.UserInfo.INSTANCE.getEmail());
                    params.put(HttpConstants.DESCRIPTION,mDesciption.getText().toString());
                    mApiService.updateUserMultipart(mId,typedFile, params, this);

                }
                else {


                    mApiService.updateUserNoImage(mId, userDetailsWithoutImageModel, this);
                }

                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public void onClick(final View v) {
        switch (v.getId()) {


            case R.id.image_profile_pic: {
                showChoosePictureSourceDialog();
                break;
            }
        }
    }

    /**
     * Method to handle click on profile image
     */
    private void showChoosePictureSourceDialog() {

        mChoosePictureDialogFragment = new SingleChoiceDialogFragment();
        mChoosePictureDialogFragment
                .show(AlertDialog.THEME_HOLO_LIGHT, R.array.take_photo_choices, 0,
                        R.string.take_picture, getFragmentManager(), true,
                        AppConstants.FragmentTags.DIALOG_TAKE_PICTURE);

    }

    @Override
    public boolean willHandleDialog(final DialogInterface dialog) {

        if ((mChoosePictureDialogFragment != null)
                && mChoosePictureDialogFragment.getDialog()
                .equals(dialog)) {
            return true;
        }
        return super.willHandleDialog(dialog);
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mChoosePictureDialogFragment != null)
                && mChoosePictureDialogFragment.getDialog()
                .equals(dialog)) {

            if (which == 0) { // Pick from camera
                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageCaptureUri);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, getString(R.string.complete_action_using)),
                            PICK_FROM_CAMERA);
                } catch (final ActivityNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (which == 1) { // pick from file
                final Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(intent, getString(R.string.complete_action_using)),
                        PICK_FROM_FILE);
            }
        } else {
            super.onDialogClick(dialog, which);
        }
    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_FROM_CAMERA:
                // doCrop(PICK_FROM_CAMERA);
                setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);
                break;

            case PICK_FROM_FILE:
                mGalleryImageCaptureUri = data.getData();

                setAndSaveImage(mGalleryImageCaptureUri, PICK_FROM_FILE);
                // doCrop(PICK_FROM_FILE);
                break;

            case CROP_FROM_CAMERA:
                final Bundle extras = data.getExtras();
                if (extras != null) {
                    mCompressedPhoto = extras.getParcelable("data");
                    mProfilePic.setImageBitmap(mCompressedPhoto);
                }
                PhotoUtils.saveImage(mCompressedPhoto,AppConstants.AVATOR_PROFILE_NAME);
                break;

            }


    } // End of onActivityResult
    /**
     * Set the Profile Image and Save it locally
     *
     * @param uri             URI of the image to be saved.
     * @param source_of_image If the image was from Gallery or Camera
     */

    private void setAndSaveImage(final Uri uri, final int source_of_image) {
        String source_string;
        if (source_of_image == PICK_FROM_FILE) {
            source_string = "Gallery";
        } else {
            source_string = "Camera";
        }

        mPhotoUri=uri;
        mCompressedPhoto = PhotoUtils
                .rotateBitmapIfNeededAndCompressIfTold(getActivity(), uri, source_string, true);

        if (mCompressedPhoto != null) {
            mProfilePic.setImageBitmap(mCompressedPhoto);
            PhotoUtils.saveImage(mCompressedPhoto, mAvatarFileName);
        }
        mWasProfileImageChanged = true;

    }

    @Override
    public void success(Object o, Response response) {
        if(o.getClass().equals(CreateUserResponseModel.class))
        {

            getActivity().setProgressBarIndeterminateVisibility(false);


            CreateUserResponseModel userResponseModel=((CreateUserResponseModel)o);
            AppConstants.UserInfo.INSTANCE.setEmail(userResponseModel.user.email);
            AppConstants.UserInfo.INSTANCE.setFirstName(userResponseModel.user.first_name);
            AppConstants.UserInfo.INSTANCE.setLastName(userResponseModel.user.last_name);
            AppConstants.UserInfo.INSTANCE.setId(userResponseModel.user.id);
            AppConstants.UserInfo.INSTANCE.setProfilePicture(userResponseModel.user.image_url);
            AppConstants.UserInfo.INSTANCE.setMobileNumber(userResponseModel.user.mobile_number);

            SharedPreferenceHelper.set(R.string.pref_first_name,userResponseModel.user.first_name);
            SharedPreferenceHelper.set(R.string.pref_last_name,userResponseModel.user.last_name);
            SharedPreferenceHelper.set(R.string.pref_profile_image,userResponseModel.user.image_url);
            SharedPreferenceHelper.set(R.string.pref_user_id,userResponseModel.user.id);
            SharedPreferenceHelper.set(R.string.pref_email,userResponseModel.user.email);
            SharedPreferenceHelper.set(R.string.pref_mobile_number,userResponseModel.user.mobile_number);

            userRefresh(true);
            getActivity().finish();



        }


    }

    @Override
    public void failure(RetrofitError error) {

        getActivity().setProgressBarIndeterminateVisibility(false);

        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_LONG).show();
    }
}
