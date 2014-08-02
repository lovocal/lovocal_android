package com.lovocal.retromodels.response;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by anshul1235 on 29/07/14.
 */
public class GoogleGeocodeResponse {

    public List<Results> results;

    public class Results{
        public String formatted_address;

        /**
         * Index
         * 0=city
         * 1=state
         * 2=country
         */

        public String[] getAddress(){
            String[] address= TextUtils.split(this.formatted_address,",");
            String[] addressSplit=new String[3];
            int length=address.length;
            //gives country
            addressSplit[2]=address[length-1];
            //gives state
            addressSplit[1]=address[length-2];
            //gives city
            addressSplit[0]=address[length-3];

            return addressSplit;
        }
    }

}
