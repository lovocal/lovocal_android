
package com.lovocal.data;

/**
 * Constant interface to hold table columns. DO NOT IMPLEMENT.
 *
 * @author Anshul Kamboj
 */
public interface DatabaseColumns {

    public static final String ID                   = "id";
    public static final String NAME                 = "name";
    public static final String TYPE                 = "type";
    public static final String IMAGE_URL            = "image_url";
    public static final String MAIN_IMAGE           = "main_image";
    public static final String CATEGORY_ID          = "category_id";
    public static final String CATEGORY_NAME        = "category_name";
    public static final String CATEGORY_IMAGE       = "category_image";
    public static final String DESCRIPTION          = "description";
    public static final String MOBILE_NUMBER        = "mobile_number";
    public static final String LATITUDE             = "latitude";
    public static final String LONGITUDE            = "longitude";
    public static final String COUNTRY              = "country";
    public static final String CITY                 = "city";
    public static final String STATE                = "state";
    public static final String ZIP_CODE             = "zip_code";
    public static final String CUSTOMERCARE_NUMBER  = "customercare_number";
    public static final String LANDLINE_NUMBER      = "landline_number";
    public static final String ADDRESS              = "address";
    public static final String WEBSITE              = "website";
    public static final String TWITTER_LINK         = "twitter_link";
    public static final String FACEBOOK_LINK        = "facebook_link";
    public static final String LINKEDIN_LINK        = "linkedin_link";
    public static final String CATEGORIES           = "categories";
    public static final String CHAT_ID              = "chat_id";
    public static final String SERVER_CHAT_ID       = "server_chat_id";
    public static final String TIMESTAMP            = "timestamp";
    public static final String MESSAGE              = "message";
    public static final String TIMESTAMP_HUMAN      = "timestamp_human";
    public static final String TIMESTAMP_EPOCH      = "timestamp_epoch";
    public static final String SENT_AT              = "sent_at";
    public static final String CHAT_TYPE            = "chat_type";
    public static final String LAST_MESSAGE_ID      = "last_message_id";
    public static final String SENDER_ID            = "sender_id";
    public static final String RECEIVER_ID          = "receiver_id";
    public static final String UNREAD_COUNT         = "unread_count";
    public static final String CHAT_QUERY_ID        = "chat_query_id";
    public static final String SENDER_NAME          = "sender_name";
    public static final String SENDER_IMAGE         = "sender_image";
    public static final String RECEIVER_NAME        = "receiver_name";
    public static final String RECEIVER_IMAGE       = "receiver_image";
    public static final String IMAGE                = "image";

    /**
     * Indicates the status of a chat message
     * <ul>
     * <li>0 - Sending</li>
     * <li>1 - Sent</li>
     * <li>2 - Received</li>
     * <li>-1 - Failed</li>
     * </ul>
     */
    public static final String CHAT_STATUS          = "chat_status";

    /** @deprecated Not used as of DB Version 4 */
    public static final String CHAT_ACK             = "sending_ack";


}
