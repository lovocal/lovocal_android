package com.lovocal.retromodels.response;

import java.util.List;

/**
 * Created by anshul1235 on 05/08/14.
 */
public class BannerResponseModel {
    public List<ImageUrl> banners;

    public class ImageUrl{
       public String image_url;
    }

}
