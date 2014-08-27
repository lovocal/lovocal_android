package com.lovocal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.lovocal.LavocalApplication;
import com.lovocal.R;
import com.lovocal.bus.BindExchangeUpdate;
import com.lovocal.chat.ChatService;
import com.lovocal.data.DBInterface;
import com.lovocal.data.DatabaseColumns;
import com.lovocal.data.SQLiteLoader;
import com.lovocal.data.TableCategories;
import com.lovocal.data.TableMyServices;
import com.lovocal.data.TableServices;
import com.lovocal.fragments.dialogs.SingleChoiceDialogFragment;
import com.lovocal.http.Api;
import com.lovocal.http.HttpConstants;
import com.lovocal.retromodels.request.AddServiceImagesRequestModel;
import com.lovocal.retromodels.request.CreateServiceRequestModel;
import com.lovocal.retromodels.response.CreateServiceResponseModel;
import com.lovocal.retromodels.response.GoogleGeocodeResponse;
import com.lovocal.retromodels.response.ServiceImageResponse;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.Logger;
import com.lovocal.utils.PhotoUtils;
import com.lovocal.utils.SharedPreferenceHelper;
import com.lovocal.widgets.CircleImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 18/07/14.
 */
public class CreateServiceFragment extends AbstractLavocalFragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,AdapterView.OnItemSelectedListener,DBInterface.AsyncDbQueryCallback{


    private static final String TAG = "CategoryFragment";

    private CircleImageView     mServiceImage;
    private EditText            mEditServiceName,mEditDesciption,mEditAddress,mEditCity,mEditCountry;
    private Spinner             mCategorySpinner;

    private Bitmap mCompressedPhoto;
    private Uri mCameraImageCaptureUri;
    private Uri    mGalleryImageCaptureUri;

    /**
     * This holds the main service image
     */
    private File mServiceImageFile;


    private List<String> mCategories=new ArrayList<String>();
    private List<String> mCategoriesIds=new ArrayList<String>();

    private List<String> mSelectedCategories=new ArrayList<String>();

    private boolean mWasProfileImageChanged = false;


    private Uri mPhotoUri;


    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE   = 3;
    /**
     * Reference to the Dialog Fragment for selecting the picture type
     */
    private SingleChoiceDialogFragment mChoosePictureDialogFragment;

    /**
     * Reference to the latest location
     */
    private Location mLocation;

    private final String mAvatarServiceName = AppConstants.SERVICE_IMAGE+".jpg";


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View contentView = inflater
                .inflate(R.layout.fragment_create_service, container, false);




        mServiceImageFile = new File(Environment.getExternalStorageDirectory(), mAvatarServiceName);


        mServiceImage=(CircleImageView)contentView.findViewById(R.id.image_service_pic);
        mCategorySpinner=(Spinner)contentView.findViewById(R.id.categoryspinner);

        mCategorySpinner.setOnItemSelectedListener(this);
        mServiceImage.setOnClickListener(this);

        mEditServiceName=(EditText)contentView.findViewById(R.id.text_service_name);
        mEditDesciption=(EditText)contentView.findViewById(R.id.text_description);
        mEditAddress=(EditText)contentView.findViewById(R.id.text_address);
        mEditCity=(EditText)contentView.findViewById(R.id.text_city);
        mEditCountry =(EditText)contentView.findViewById(R.id.text_country);

        Picasso.with(getActivity())
                .load(R.drawable.ic_launcher)
                .resizeDimen(R.dimen.edit_profile_img_size,
                        R.dimen.edit_profile_img_size)
                .centerCrop()
                .into(mServiceImage.getTarget());

        mCameraImageCaptureUri = Uri.fromFile(new File(android.os.Environment
                .getExternalStorageDirectory(),
                AppConstants.SERVICE_IMAGE+".jpg"));

            getCategories();

            fillAddressDetails();

        return contentView;

    }

    private void fillAddressDetails(){

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(HttpConstants.getGoogleGeocodeApiBaseUrl())
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError cause) {
                        Response r = cause.getResponse();
                        if (r != null && r.getStatus() == 401) {
                            return new Exception(cause);
                        }
                        return cause;
                    }
                })
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (!AppConstants.UserInfo.INSTANCE.getAuthToken().equals("")) {
                            request.addHeader(HttpConstants.HEADER_AUTHORIZATION, AppConstants.UserInfo.INSTANCE.getAuthHeader());
                            Logger.d(TAG,"Updated auth header");
                        }
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        final Map<String, String> params = new HashMap<String, String>(6);
        params.put(HttpConstants.LATLNG, AppConstants.DeviceInfo.INSTANCE.getLatestLocation().getLatitude()
                +","+AppConstants.DeviceInfo.INSTANCE.getLatestLocation().getLongitude());
        params.put(HttpConstants.KEY,getResources().getString(R.string.google_api_key));
        params.put(HttpConstants.RESULT_TYPE,HttpConstants.STREET_ADDRESS);
        Api service = restAdapter.create(Api.class);
        service.getMyAddress(params,this);

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_service_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();
                return true;
            }

            case R.id.action_service_save: {


                    CreateServiceRequestModel mCreateService = new CreateServiceRequestModel();
                    mCreateService.service.setMobile_number(AppConstants.UserInfo.INSTANCE.getMobileNumber());
                    mCreateService.service.setBusiness_name(mEditServiceName.getText().toString());
                    mCreateService.service.setDescription(mEditDesciption.getText().toString());
                    mCreateService.service.setAddress(mEditAddress.getText().toString());
                    mCreateService.service.setCity(mEditCity.getText().toString());
                    mCreateService.service.setCountry(mEditCountry.getText().toString());
                    mCreateService.service.setLatitude(mLocation.getLatitude());
                    mCreateService.service.setLongitude(mLocation.getLongitude());
                    mCreateService.service.setListing_categories(mSelectedCategories);
                    getActivity().setProgressBarIndeterminateVisibility(true);

                    mApiService.createService(mCreateService,this);


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


            case R.id.image_service_pic: {
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
                    mServiceImage.setImageBitmap(mCompressedPhoto);
                }
                PhotoUtils.saveImage(mCompressedPhoto,AppConstants.SERVICE_IMAGE);
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
            mServiceImage.setImageBitmap(mCompressedPhoto);
            PhotoUtils.saveImage(mCompressedPhoto, mAvatarServiceName);
        }
        mWasProfileImageChanged = true;

    }

    @Override
    public void onResume() {
        super.onResume();

        final Location latestLocation = AppConstants.DeviceInfo.INSTANCE
                .getLatestLocation();
        if ((latestLocation.getLatitude() != 0.0)
                && (latestLocation.getLongitude() != 0.0)) {
            updateLocation(latestLocation);
        }

    }

    public void updateLocation(final Location location) {

        if ((location.getLatitude() == 0.0) && (location.getLongitude() == 0.0)) {
            return;
        }
        mLocation=location;
        fetchDetails(mLocation);

    }

    private void  fetchDetails(Location location)
    {
        //TODO fetch from google geolocation api
    }

    @Override
    public void success(Object o, Response response) {
        if(o.getClass().equals(CreateServiceResponseModel.class))
        {
            CreateServiceResponseModel createServiceResponseModel=((CreateServiceResponseModel)o);

            ContentValues values = new ContentValues();

            values.put(DatabaseColumns.ID,  createServiceResponseModel.service.id);
            values.put(DatabaseColumns.NAME,  createServiceResponseModel.service.business_name);
            values.put(DatabaseColumns.MOBILE_NUMBER,  createServiceResponseModel.service.mobile_number);
            values.put(DatabaseColumns.LATITUDE,  createServiceResponseModel.service.latitude);
            values.put(DatabaseColumns.LONGITUDE,  createServiceResponseModel.service.longitude);
            values.put(DatabaseColumns.COUNTRY,  createServiceResponseModel.service.country);
            values.put(DatabaseColumns.CITY,  createServiceResponseModel.service.city);
            values.put(DatabaseColumns.STATE,  createServiceResponseModel.service.state);
            values.put(DatabaseColumns.ZIP_CODE,  createServiceResponseModel.service.zip_code);
            values.put(DatabaseColumns.DESCRIPTION,  createServiceResponseModel.service.description);
            values.put(DatabaseColumns.CUSTOMERCARE_NUMBER,  createServiceResponseModel.service.customer_care_number);
            values.put(DatabaseColumns.LANDLINE_NUMBER,  createServiceResponseModel.service.landline_number);
            values.put(DatabaseColumns.ADDRESS,  createServiceResponseModel.service.address);
            values.put(DatabaseColumns.WEBSITE,  createServiceResponseModel.service.website);
            values.put(DatabaseColumns.TWITTER_LINK,  createServiceResponseModel.service.twitter_link);
            values.put(DatabaseColumns.FACEBOOK_LINK,  createServiceResponseModel.service.facebook_link);
            values.put(DatabaseColumns.LINKEDIN_LINK,  createServiceResponseModel.service.linkedin_link);

            if ( createServiceResponseModel.service.listing_categories.size() > 0) {
                final String[] tags = new String[ createServiceResponseModel.service.listing_categories.size()];
                for (int j = 0; j <  createServiceResponseModel.service.listing_categories.size(); j++) {
                    tags[j] =  createServiceResponseModel.service.listing_categories.get(j);
                }

                values.put(DatabaseColumns.CATEGORIES, TextUtils
                        .join(AppConstants.CATEGORY_SEPERATOR, tags));
            }



            DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_MY_SERVICES,getTaskTag(),null, TableMyServices.NAME,null
            ,values,false,this);


            if(mWasProfileImageChanged) {
                List<TypedFile> images=new ArrayList<TypedFile>();
                TypedFile typedFile;
                File photo;
                photo = new File(mServiceImageFile.getPath());
                typedFile = new TypedFile("application/octet-stream", photo);
                images.add(typedFile);

                AddServiceImagesRequestModel addServiceImagesRequestModel=new AddServiceImagesRequestModel();
                addServiceImagesRequestModel.setService_image(images);
                mApiService.addServiceImages( createServiceResponseModel.service.id,typedFile,this);

            }
            else if(!mWasProfileImageChanged){
                getActivity().setProgressBarIndeterminateVisibility(false);
                getActivity().finish();
                SharedPreferenceHelper.set(R.string.pref_force_user_refetch, true);


            }



            mBus.post(new BindExchangeUpdate(true));


        }
        else if(o.getClass().equals(ServiceImageResponse.class)) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            ServiceImageResponse imageResponse = ((ServiceImageResponse) o);
            getActivity().finish();
            SharedPreferenceHelper.set(R.string.pref_force_user_refetch, true);
        }

        else  if(o.getClass().equals(GoogleGeocodeResponse.class)) {
            GoogleGeocodeResponse googleGeocodeResponse = ((GoogleGeocodeResponse) o);
            String[] address=googleGeocodeResponse.results.get(0).getAddress();

            mEditCity.setText(address[0]);
            mEditCountry.setText(address[2]);
        }


        }

    @Override
    public void failure(RetrofitError error) {

        getActivity().setProgressBarIndeterminateVisibility(false);

        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_LONG).show();
    }

    private void getCategories()
    {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CATEGORIES_IN_SERVICES, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_CATEGORIES_IN_SERVICES) {
            return new SQLiteLoader(getActivity(), false, TableCategories.NAME, null,
                    null, null, null, null, null, null);
        } else {
            return null;
        }    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_CATEGORIES_IN_SERVICES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            cursor.moveToFirst();
            mCategories.add("select category");
            for(int i=0;i<cursor.getCount();i++){
                mCategories.add(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.CATEGORY_NAME)));

                mCategoriesIds.add(cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID)));
                cursor.moveToNext();
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, mCategories);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCategorySpinner.setAdapter(dataAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_CATEGORIES_IN_SERVICES) {
            mCategories.clear();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position!=0) {
            mSelectedCategories.add(mCategoriesIds.get(position-1));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }
}
