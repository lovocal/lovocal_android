package com.lovocal.retromodels.request;

import java.util.List;

/**
 * Created by anshul1235 on 20/07/14.
 */


public class CreateServiceRequestModel {

    public Service service=new Service();

    public class Service{
        public String business_name;
        public String mobile_number;
        public String description;
        public String city;
        public String country;
        public String address;
        public String latitude;
        public String longitude;
        public List<String> list_cat_ids;

        public void setBusiness_name(String business_name)
        {
            this.business_name=business_name;
        }

        public void setMobile_number(String mobile_number)
        {
            this.mobile_number=mobile_number;
        }

        public void setDescription(String description)  {
            this.description=description;
        }

        public void setCity(String city)
        {
            this.city=city;
        }

        public void setCountry(String country)
        {
            this.country=country;
        }

        public void setAddress(String address)
        {
            this.address=address;
        }

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

        public void setListing_categories(List<String> list_cat_ids)
        {
            this.list_cat_ids=list_cat_ids;
        }
    }
}
