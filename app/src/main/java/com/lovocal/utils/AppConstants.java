package com.lovocal.utils;

import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.lovocal.http.HttpConstants;

import java.util.Locale;

/**
 * Created by anshul1235 on 14/07/14.
 */
public class AppConstants {

    public static final int PROFILE_PAGE = 0;
    public static final int CATEGORY_PAGE = 1;
    public static final int CHAT_PAGE = 2;
    public static final int DEFAULT_SERVICE_PAGER_NUMBER = 1;
    public static final String AVATOR_PROFILE_NAME = "lavocal_avator_profile_name";
    public static final String SERVICE_IMAGE = "service_image";

    public static final String CATEGORY_SEPERATOR = ",";

    //page(default page number) and per(default count to be loaded per page)
    // as per listing for categories as well as services
    public static final String PER_VALUE = "50";

    public static final String DISTANCE = "50"; //in kms

    /*
    * heartbeat interval for rabbitmq chat
    */
    public static final int HEART_BEAT_INTERVAL = 20;


    public static final String ACTION_SHOW_ALL_CHATS = "com.lovocal.ACTION_SHOW_ALL_CHATS";
    public static final String ACTION_SHOW_CHAT_DETAIL = "com.lovocal.ACTION_SHOW_CHAT_DETAIL";
    public static final String ACTION_DISCONNECT_CHAT = "com.lovocal.ACTION_DISCONNECT_CHAT";
    public static final String ACTION_CHAT_BUTTON_CLICKED = "com.lovocal.ACTION_CHAT_BUTTON_CLICKED";
    public static final String ACTION_RECONNECT_CHAT = "com.lovocal.ACTION_RECONNECT_CHAT";

    public static final String CHAT_ID_FORMAT = "%s#%s";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String CHAT_TIME_FORMAT = "dd MMM, h:mm a";
    public static final String MESSAGE_TIME_FORMAT = "h:mm a";

    public static final String SERVICE = "service";
    public static final String USER = "user";


    /**
     * Constant Interface, DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface Keys {
        public static final String UP_NAVIGATION_TAG = "up_navigation_tag";
        public static final String ONWARD_INTENT = "onward_intent";
        public static final String PAGER_POSITION = "pager_position";
        public static final String ID = "id";
        public static final String CATEGORY_ID = "category_id";
        public static final String CATEGORY_NAME = "category_name";
        public static final String USER_INFO = "user_info";
        public static final String CHAT_ID = "chat_id";
        public static final String USER_ID = "user_id";
        public static final String SENDER_TYPE = "sender_type";
        public static final String CHAT_ID_ARRAY = "chat_id_array";
        public static final String USER_ID_ARRAY = "user_id_array";
        public static final String CHAT_TITLES = "chat_titles";
        public static final String MY_ID = "my_id";
        public static final String LOAD_CHAT = "load_chat";
        public static final String FROM_NOTIFICATIONS = "from_notifications";
        public static final String FINISH_ON_BACK = "finish_on_back";
        public static final String IMAGEFEATURE_POSITION = "imagefeature_position";
        public static final String CHAT_INDEX = "chat_index";
        public static final String LAST_FETCHED_LOCATION = "last_fetched_location";
        public static final String HAS_LOADED_ALL_ITEMS = "has_loaded_all_items";
        public static final String PANEL_OPEN = "panel_open";
        public static final String SERVICE_ID_ARRAY = "service_id_array";
        public static final String CATEGORY_ID_ARRAY = "category_id_array";
        public static final String SERVICE_IMAGE = "service_image";


    }

    /**
     * Constant interface, DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface FragmentTags {
        public static final String HOME_SCREEN = "home_screen";
        public static final String NAV_DRAWER = "nav_drawer";
        public static final String LOGIN = "login";
        public static final String DIALOG_SEND_QUERY = "dialog_send_query";
        public static final String EDIT_PROFILE = "edit_profile";
        public static final String CREATE_SERVICE = "create_service";
        public static final String SEARCH_SERVICE = "search_service";
        public static final String CHATS = "chats";
        public static final String CHAT_DETAILS = "chat_details";
        public static final String CHAT_DETAILS_PAGER = "chat_details_pager";
        public static final String BROADCAST_QUERY = "broadcast_query";
        public static final String ABOUT_ME = "about_me";
        public static final String MY_SERVICES = "my_services";

        //dialogs
        public static final String DIALOG_TAKE_PICTURE = "dialog_take_picture";
        public static final String DIALOG_CHAT_LONGCLICK = "dialog_chat_longclick";

    }


    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface ChatStatus {
        //Different types of chat status. Linked to the chat_sending_status of database
        public static final int SENDING = 0;
        public static final int SENT = 1;
        public static final int FAILED = -1;
        public static final int RECEIVED = 2;
    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface ChatType {

        public static final String PERSONAL = "personal";
        public static final String SERVICE = "service";
        public static final String GROUP = "group";
    }


    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface QueryTokens {

        // 1-100 for load queries
        public static final int GET_SERVICE_IDS = 1;

        // 101-200 for insert queries
        public static final int INSERT_CATEGORIES = 101;
        public static final int INSERT_SERVICES = 102;
        public static final int INSERT_MY_SERVICES = 103;


        // 201-300 for update queries

        //301-400 for delete queries
        public static final int DELETE_CATEGORIES = 301;
        public static final int DELETE_SERVICES = 302;
        public static final int DELETE_CHAT_MESSAGE = 303;
        public static final int DELETE_MY_SERVICES = 304;
        public static final int DELETE_CHATS = 305;
        public static final int DELETE_CHAT_MESSAGES = 306;

    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface Loaders {

        public static final int LOAD_CATEGORIES = 201;
        public static final int LOAD_CATEGORIES_IN_SERVICES = 202;
        public static final int LOAD_SERVICES = 203;
        public static final int ALL_CHATS = 204;
        public static final int CHAT_DETAILS = 205;
        public static final int USER_DETAILS_CHAT_DETAILS = 206;
        public static final int LOAD_MY_SERVICES = 207;

    }

    /**
     * All the request codes used in the application will be placed here
     *
     * @author Anshul Kamboj
     */
    public static interface RequestCodes {


        public static final int LOGIN = 100;
        public static final int EDIT_PROFILE = 101;
        public static final int CREATE_SERVICE = 102;
        public static final int GALLERY_INTENT_CALLED = 103;
        public static final int GALLERY_KITKAT_INTENT_CALLED = 104;

    }

    /**
     * Singleton to hold frequently accessed info in memory
     *
     * @author Anshul Kamboj
     */
    public enum UserInfo {

        INSTANCE;

        private String mAuthToken;
        private String mEmail;
        private String mMobileNumber;
        private String mId;
        private String mProfilePicture;
        private String mAuthHeader;
        private String mDeviceId;
        private String mFirstName;
        private String mLastName;
        private String mDescription;

        private UserInfo() {
            reset();
        }

        public void reset() {
            mAuthToken = "";
            mAuthHeader = "";
            mEmail = "";
            mId = "";
            mProfilePicture = "";
            mFirstName = "";
            mLastName = "";
            mMobileNumber = "";
            mDescription = "";
        }

        public String getMobileNumber() {
            return mMobileNumber;
        }

        public void setMobileNumber(final String mobileNumber) {
            if (mobileNumber == null) {
                mMobileNumber = "";
            } else {
                mMobileNumber = mobileNumber;
            }
        }

        public void setDescription(final String description) {
            if (description == null) {
                mDescription = "";
            } else {
                mDescription = description;
            }
        }

        public String getAuthToken() {
            return mAuthToken;
        }


        public void setAuthToken(final String authToken) {

            if (authToken == null) {
                mAuthToken = "";
            } else {
                mAuthToken = authToken;
            }
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(final String email) {

            if (email == null) {
                mEmail = "";
            } else {
                mEmail = email;
            }
        }

        public String getId() {
            return mId;
        }

        public void setId(final String id) {

            if (id == null) {
                mId = "";
            } else {
                mId = id;
            }
        }

        public String getProfilePicture() {
            return mProfilePicture;
        }

        public void setProfilePicture(final String profilePicture) {

            if (profilePicture == null) {
                mProfilePicture = "";
            } else {
                mProfilePicture = profilePicture;
            }

        }

        public String getDeviceId() {
            return mDeviceId;
        }

        public void setDeviceId(final String deviceId) {
            mDeviceId = deviceId;
        }

        public String getDescription(){
            return mDescription;
        }

        public String getAuthHeader() {

            if (TextUtils.isEmpty(mAuthHeader)
                    && !TextUtils.isEmpty(mAuthToken)
                    && !TextUtils.isEmpty(mDeviceId)) {
                mAuthHeader = String
                        .format(Locale.US, HttpConstants.HEADER_AUTHORIZATION_FORMAT, mAuthToken,
                                mDeviceId);
            }
            return mAuthHeader;
        }

        public String getFirstName() {
            return mFirstName;
        }

        public void setFirstName(final String firstName) {

            mFirstName = firstName;
        }

        public String getLastName() {
            return mLastName;
        }

        public void setLastName(final String lastName) {

            mLastName = lastName;
        }

    }

    /**
     * Singleton to hold the current network state. Broadcast receiver for network state will be
     * used to keep this updated
     *
     * @author Anshul Kamboj
     */
    public enum DeviceInfo {

        INSTANCE;

        private final Location defaultLocation = new Location(LocationManager.PASSIVE_PROVIDER);

        private boolean mIsNetworkConnected;
        private int mCurrentNetworkType;
        private Location mLatestLocation;

        private DeviceInfo() {
            reset();
        }

        public void reset() {

            mIsNetworkConnected = false;
            mCurrentNetworkType = ConnectivityManager.TYPE_DUMMY;
            mLatestLocation = defaultLocation;
        }

        public boolean isNetworkConnected() {
            return mIsNetworkConnected;
        }

        public void setNetworkConnected(final boolean isNetworkConnected) {
            mIsNetworkConnected = isNetworkConnected;
        }

        public int getCurrentNetworkType() {
            return mCurrentNetworkType;
        }

        public void setCurrentNetworkType(final int currentNetworkType) {
            mCurrentNetworkType = currentNetworkType;
        }

        public Location getLatestLocation() {
            return mLatestLocation;
        }

        public void setLatestLocation(final Location latestLocation) {
            if (latestLocation == null) {
                mLatestLocation = defaultLocation;
            }
            mLatestLocation = latestLocation;
        }

    }

}
