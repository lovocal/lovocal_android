package com.lovocal.retromodels.request;

/**
 * Created by anshul1235 on 22/07/14.
 */
public class SendBroadcastChatRequestModel {

    public Chat chat=new Chat();

    public class Chat{
        public String latitude;
        public String longitude;
        public String user_id;
        public String message;
        public String list_cat_id;
        public String sent_time;


        public void setLatitude(double latitude)
        {
            String lati=String.valueOf(latitude);

            this.latitude=lati;
        }

        public void setLongitude(double longitude)
        {
            String longi=String.valueOf(longitude);
            this.longitude=longi;
        }

        public void setUser_id(String user_id){
            this.user_id=user_id;
        }

        public void setMessage(String message)
        {
            this.message=message;
        }

        public void setList_cat_id(String list_cat_id){
            this.list_cat_id=list_cat_id;
        }

        public void setSent_time(String sent_time){
            this.sent_time=sent_time;
        }


    }
}
