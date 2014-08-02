package com.lovocal.retromodels.response;

/**
 * Created by anshul1235 on 17/07/14.
 */
public class CreateUserResponseModel {

    public User user;

    public static class User
    {
        public String id;
        public String mobile_number;
        public String first_name;
        public String last_name;
        public String email;
        public String image_url;
        public String description;
    }

}
