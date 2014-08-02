package com.lovocal.retromodels.request;

import java.util.List;

import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 28/07/14.
 */
public class AddServiceImagesRequestModel {


    public List<TypedFile> image;
        public void setService_image(List<TypedFile> imagefile){
            this.image=imagefile;
        }


}
