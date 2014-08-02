package com.lovocal.retromodels.request;

/**
 * Created by anshul1235 on 16/07/14.
 */
public  class UserDetailsRequestModel {



    public  User user=new User();



    public class User {
        public String mobile_number;
        public String first_name;
        public String last_name;
        public String phone_id;

        public User() {}

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getMobile_number() {
            return mobile_number;
        }

        public void setMobile_number(String mobile_number) {
            this.mobile_number = mobile_number;
        }

        public String getPhone_id() {
            return phone_id;
        }

        public void setPhone_id(String phone_id) {
            this.phone_id = phone_id;
        }
    }

}

