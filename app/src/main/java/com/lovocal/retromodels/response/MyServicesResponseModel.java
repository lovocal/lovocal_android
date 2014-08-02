package com.lovocal.retromodels.response;

import java.util.List;

/**
 * Created by anshul1235 on 23/07/14.
 */
public class MyServicesResponseModel {

    public List<Services> services;

    public class Services{
        public String id;
        public String business_name;
        public String mobile_number;
        public List<String> listing_categories;
        public String latitude;
        public String longitude;
        public String country;
        public String city;
        public String state;
        public String zip_code;
        public String description;
        public String customer_care_number;
        public String landline_number;
        public String address;
        public String website;
        public String twitter_link;
        public String facebook_link;
        public String linkedin_link;
        public List<ServiceImage> service_images;
        public String service_timing;

        public class ServiceImage{
            String id;
            String image_url;
            String is_main;

            public String getImage(){
                return this.image_url;
            }
        }

    }
}
