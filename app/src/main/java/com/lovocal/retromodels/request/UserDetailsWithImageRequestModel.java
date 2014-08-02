package com.lovocal.retromodels.request;

import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 16/07/14.
 */
public  class UserDetailsWithImageRequestModel {



    public  User user=new User();



    public class User {
        public String mobile_number;
        public String first_name;
        public String last_name;
        public String email;
        public String description;
        public TypedFile image;

        public User() {}

        public void setMobile_number(String mobile_number) {
            this.mobile_number = mobile_number;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }


        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setImage(TypedFile image) {
            this.image = image;
        }



    }

}

