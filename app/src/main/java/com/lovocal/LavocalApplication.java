package com.lovocal;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.ViewConfiguration;

import com.lovocal.bus.RestAdapterUpdate;
import com.lovocal.chat.ChatService;
import com.lovocal.http.Api;
import com.lovocal.http.HttpConstants;
import com.lovocal.utils.AppConstants;
import com.lovocal.utils.AppConstants.UserInfo;
import com.lovocal.utils.Logger;
import com.lovocal.utils.SharedPreferenceHelper;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Field;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Custom Application class which holds some common functionality for the
 * Application
 *
 * @author Anshul Kamboj
 */
public class LavocalApplication extends Application {

    private static final String TAG = "LavocalApplication";

    /**
     * RestAdapter for retrofit, we declare the object in LavocalApplication
     */
    private RestAdapter mRestAdapter;

    /**
     * Reference to the bus (OTTO By Square)
     */
    private Bus         mBus;

    /**
     * this holds the reference to the auth token
     */
    private String      mAuthToken;

    /**
     * Maintains a reference to the application context so that it can be
     * referred anywhere wihout fear of leaking. It's a hack, but it works.
     */
    private static Context sStaticContext;


    /**
     * Gets a reference to the application context
     */
    public static Context getStaticContext() {
        if (sStaticContext != null) {
            return sStaticContext;
        }

        //Should NEVER hapen
       throw new RuntimeException("No static context instance");
    }

    @Override
    public void onCreate() {

        sStaticContext = getApplicationContext();
        mBus=new Bus();

        mBus.register(this);




        readUserInfoFromSharedPref();


        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(HttpConstants.getApiBaseUrl())
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
                        if (!UserInfo.INSTANCE.getAuthToken().equals("")) {
                            request.addHeader(HttpConstants.HEADER_AUTHORIZATION, UserInfo.INSTANCE.getAuthHeader());
                            Logger.d(TAG,"Updated auth header");
                        }
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        AppConstants.DeviceInfo.INSTANCE.setNetworkConnected(true);

        if (AppConstants.DeviceInfo.INSTANCE.isNetworkConnected()) {
            startChatService();

        }

    }

    /**
     * Start the chat service. The connection doesn't happen if the user isn't
     * logged in.
     */
    public static void startChatService() {


        final Intent intent = new Intent(sStaticContext, ChatService.class);
        sStaticContext.startService(intent);
    }

    /**
     * Start the chat service.
     */
    public static void stopChatService() {

        final Intent intent = new Intent(sStaticContext, ChatService.class);
        sStaticContext.stopService(intent);

    }

    /**
     * Reads the previously fetched auth token from Shared Preferencesand stores
     * it in the Singleton for in memory access
     */
    private void readUserInfoFromSharedPref() {

        UserInfo.INSTANCE.setAuthToken(SharedPreferenceHelper
                .getString(R.string.pref_auth_token));
        UserInfo.INSTANCE.setId(SharedPreferenceHelper
                .getString(R.string.pref_user_id));
        UserInfo.INSTANCE.setEmail(SharedPreferenceHelper
                .getString(R.string.pref_email));
        UserInfo.INSTANCE.setProfilePicture(SharedPreferenceHelper
                .getString(R.string.pref_profile_image));
        UserInfo.INSTANCE.setFirstName(SharedPreferenceHelper
                .getString(R.string.pref_first_name));
        UserInfo.INSTANCE.setLastName(SharedPreferenceHelper.getString(R.string.pref_last_name));
        UserInfo.INSTANCE.setMobileNumber(SharedPreferenceHelper.getString(R.string.pref_mobile_number));
        UserInfo.INSTANCE.setDeviceId(SharedPreferenceHelper.getString(R.string.pref_device_id));
        UserInfo.INSTANCE.setDescription(SharedPreferenceHelper.getString(R.string.pref_description));

    }

    public RestAdapter getRestAdapter() {
        return mRestAdapter;
    }

    public Bus getBus() {return mBus;}

    public Api getService(RestAdapter restAdapter)
    {
        Api service = restAdapter.create(Api.class);
        return service;
    }



    /**
     * Some device manufacturers are stuck in the past and stubbornly use H/W
     * menu buttons, which is deprecated since Android 3.0. This breaks the UX
     * on newer devices since the Action Bar overflow just doesn't show. This
     * little hack tricks the Android OS into thinking that the device doesn't
     * have a permanant menu button, and hence the Overflow button gets shown.
     * This doesn't disable the Menu button, however. It will continue to
     * function as normal, so the users who are already used to it will be able
     * to use it as before
     */
    private void overrideHardwareMenuButton() {
        try {
            final ViewConfiguration config = ViewConfiguration.get(this);
            final Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (final Exception ex) {
            // Ignore since we can't do anything
        }

    }

    //This method is for the response of the bus.post method which is called when we get the auth
    //token after verification of phone number
    @Subscribe public void updateRestAdapterEvent(RestAdapterUpdate restAdapterUpdate)
    {
        mAuthToken=restAdapterUpdate.authToken;
        //for setting up the device id encrypted
        UserInfo.INSTANCE.setAuthToken(mAuthToken);
        SharedPreferenceHelper.set(R.string.pref_auth_token,mAuthToken);
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(HttpConstants.getApiBaseUrl())
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
                        if (!UserInfo.INSTANCE.getAuthToken().equals("")) {
                            request.addHeader(HttpConstants.HEADER_AUTHORIZATION,UserInfo.INSTANCE.getAuthHeader());
                            Logger.d(TAG,"Updated restAdapter with authtoken");
                        }
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

    }

}
