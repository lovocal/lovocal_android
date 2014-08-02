package com.lovocal.retromodels.request;

/**
 * Created by anshul1235 on 22/07/14.
 */
public class SendChatRequestModel {

    public Chat chat = new Chat();

    public class Chat {
        public String sender_id;
        public String sender_type;
        public String receiver_id;
        public String receiver_type;
        public String message;
        public String chat_id;
        public String chat_query_id;
        public String sent_time;
        public String list_cat_id;

        public void setSender_id(String sender_id) {
            this.sender_id = sender_id;
        }

        public void setSender_type(String sender_type) {
            this.sender_type = sender_type;
        }

        public void setReceiver_id(String receiver_id) {
            this.receiver_id = receiver_id;
        }

        public void setReceiver_type(String receiver_type) {
            this.receiver_type = receiver_type;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setChat_id(String chat_id) {
            this.chat_id = chat_id;
        }

        public void setSent_time(String sent_time) {
            this.sent_time = sent_time;
        }


        public void setChat_query_id(String chat_query_id) {
            this.chat_query_id=chat_query_id;
        }

        public void setListing_category(String list_cat_id) {
            this.list_cat_id = list_cat_id;
        }

    }
}
