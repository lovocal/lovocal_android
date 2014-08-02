
package com.lovocal.http;

/**
 * @author Anshul Kamboj Interface that holds all constants related to Http
 *         Requests
 */
public class HttpConstants {


    /**
     * The API version in use by the app
     */
    private static final int API_VERSION = 1;
    private static Server SERVER = Server.DEV;

    public static String getApiBaseUrl() {
        return SERVER.mUrl;
    }

    public static String getChatUrl() {
        return SERVER.mChatUrl;
    }

    public static String getGoogleGeocodeApiBaseUrl() {
        return GOOGLE_GEOCODE_API_BASEURL;
    }


    public static String getChangedChatUrl() {
        return "http://" + SERVER.mChatUrl + SERVER.mChatLink;
    }

    public static int getChatPort() {
        return SERVER.mChatPort;
    }

    /**
     * Enum to switch between servers
     */
    private enum Server {

        LOCAL(
                "http://192.168.1.138:3000/api/v",
                API_VERSION,
                "192.168.1.138",
                5672),

        DEV(
                "http://162.243.198.171:3000/api/v",
                API_VERSION,
                "162.243.198.171",
                5672);


        public final String mUrl;
        public final String mChatUrl;
        public final int mChatPort;
        public final String mChatLink = ":3000/api/v1";

        Server(final String url, final int apiVersion, final String chatUrl, final int chatPort) {
            mUrl = url + apiVersion;
            mChatUrl = chatUrl;
            mChatPort = chatPort;
        }
    }


    //constants
    public static final String HEADER_AUTHORIZATION_FORMAT = "Token token=\"%s\", phone_id=\"%s\"";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String GOOGLE_GEOCODE_API_BASEURL = "https://maps.googleapis.com/maps/api/";

    //search params
    public static final String SEARCH_LATITUDE = "search[latitude]";
    public static final String SEARCH_LONGITUDE = "search[longitude]";
    public static final String SEARCH_SERVICE_NAME = "search[listing_services]";
    public static final String SEARCH_CATEGORY_NAME = "search[listing_category]";
    public static final String SEARCH_CATEGORY_ID = "search[list_cat_id]";
    public static final String SEARCH_PER = "search[per]";
    public static final String SEARCH_PAGE = "search[page]";
    public static final String SEARCH_DISTANCE = "search[distance]";

    //edit profile params
    public static final String MOBILE_NUMBER = "user[mobile_number]";
    public static final String FIRST_NAME = "user[first_name]";
    public static final String LAST_NAME = "user[last_name]";
    public static final String EMAIL = "user[email]";
    public static final String DESCRIPTION = "user[description]";

    //geo code params (for Google API)
    public static final String LATLNG = "latlng";
    public static final String RESULT_TYPE = "result_type";
    public static final String KEY = "key";
    public static final String STREET_ADDRESS = "street_address";


    //chats
    public static final String SENDER_ID = "sender_id";
    public static final String RECEIVER_ID = "receiver_id";
    public static final String RECEIVED = "received";
    public static final String LIST_CAT_ID = "list_cat_id";
    public static final String CHAT_ID = "chat_id";
    public static final String REPLY_ID = "reply_id";
    public static final String SENT_AT = "sent_at";
    public static final String MESSAGE = "message";
    public static final String TIME = "time";
    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static final String ID_USER = "id_user";
    public static final String SENT_TIME = "sent_time";
    public static final String SERVER_SENT_TIME = "server_sent_time";
    public static final String NAME = "name";
    public static final String SENDER_TYPE = "sender_type";
    public static final String CHAT_QUERY_ID = "chat_query_id";
    public static final String CHAT_QUERY_MESSAGE = "chat_query_message";
    public static final String SENDER_NAME = "sender_name";
    public static final String SENDER_IMAGE = "sender_image";
    public static final String RECEIVER_IMAGE = "receiver_image";
    public static final String RECEIVER_NAME = "receiver_name";
    public static final String CHAT_SERVICE_ID = "chat[service_id]";


}
