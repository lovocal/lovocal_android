package com.lovocal.retromodels.response;

import java.util.List;



/**
 * Created by anshul1235 on 17/07/14.
 */

public class CategoryListResponseModel {

        public List<CategoryList> listing_categories;

    public class CategoryList{

        public String id;
        public String name;
        public String image_url;
    }


}