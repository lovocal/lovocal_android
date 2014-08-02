package com.lovocal.retromodels.request;

/**
 * Created by anshul1235 on 18/07/14.
 */
public class VerifyUserRequestModel {

    public  User user=new User();



    public class User {
        public String mobile_number;
        public String sms_serial_key;
        public String phone_id;

        public User() {}

        public void setMobile_number(String mobile_number) {
            this.mobile_number = mobile_number;
        }

        public void setPhone_id(String phone_id) {
            this.phone_id = phone_id;
        }

        public void setSms_serial_key(String sms_serial_key) {
            this.sms_serial_key = sms_serial_key;
        }
    }
}
