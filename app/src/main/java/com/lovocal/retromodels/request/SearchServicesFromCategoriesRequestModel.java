package com.lovocal.retromodels.request;

/**
 * Created by anshul1235 on 20/07/14.
 */

public class SearchServicesFromCategoriesRequestModel {

    public Search search=new Search();

    public class Search{
        public String latitude;
        public String longitude;
        public String listing_category;
        public String distance;
        public String page;
        public String per;


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

        public void setListing_category(String listing_category)
        {
           this.listing_category=listing_category;
        }

        public void setDistance(String distance)
        {
            this.distance=distance;
        }

        public void setPage(String page)
        {
            this.page=page;
        }
        public void setPer(String per)
        {
            this.per=per;
        }

    }
}
