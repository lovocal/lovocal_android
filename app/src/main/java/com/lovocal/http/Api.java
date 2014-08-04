package com.lovocal.http;

import com.lovocal.retromodels.request.AddServiceImagesRequestModel;
import com.lovocal.retromodels.request.CreateServiceRequestModel;
import com.lovocal.retromodels.request.SearchServicesFromCategoriesRequestModel;
import com.lovocal.retromodels.request.SendBroadcastChatRequestModel;
import com.lovocal.retromodels.request.SendChatRequestModel;
import com.lovocal.retromodels.request.UserDetailsRequestModel;
import com.lovocal.retromodels.request.UserDetailsWithImageRequestModel;
import com.lovocal.retromodels.request.UserDetailsWithoutImageRequestModel;
import com.lovocal.retromodels.request.VerifyUserRequestModel;
import com.lovocal.retromodels.response.CategoryListResponseModel;
import com.lovocal.retromodels.response.CreateServiceResponseModel;
import com.lovocal.retromodels.response.CreateUserResponseModel;
import com.lovocal.retromodels.response.GoogleGeocodeResponse;
import com.lovocal.retromodels.response.MyServicesResponseModel;
import com.lovocal.retromodels.response.SearchServiceResponseModel;
import com.lovocal.retromodels.response.ServiceImageResponse;
import com.lovocal.retromodels.response.VerifySmsResponseModel;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 17/07/14.
 */
public interface Api {


    // All the GET apis

    @GET("/listing_categories")
    void getCategories(Callback<CategoryListResponseModel> cb);

    @GET("/users")
    void getUserDetail(Callback<UserDetailsRequestModel> cb);



    @GET("/search")
    void getServices(@QueryMap Map<String,String> params,Callback<SearchServiceResponseModel> cb);

    @GET("/users/current_user_services")
    void getMyServices(Callback<MyServicesResponseModel> cb);

    //get location from the google geocode api
    @GET("/geocode/json")
    void getMyAddress( @QueryMap Map<String,String> params,Callback<GoogleGeocodeResponse> cb);



    // All the PUT apis
    @Multipart
    @PUT("/users/{id}")
    void updateUserMultipart(@Path("id") String id,
                             @Part("image") TypedFile image,
                             @QueryMap Map<String,String> params
                             ,Callback<CreateUserResponseModel> cb);

    @PUT("/users/{id}")
    void updateUserNoImage(@Path("id") String id,
                           @Body UserDetailsWithoutImageRequestModel user
            ,Callback<CreateUserResponseModel> cb);


    // All the POST apis
    @POST("/users")
    void createUser(@Body UserDetailsRequestModel user, Callback<CreateUserResponseModel> cb);

    @Multipart
    @POST("/services/{id}/service_images")
    void addServiceImages(@Path("id") String id,@Part("service_images")TypedFile images,Callback<ServiceImageResponse> cb);

    @POST("/verify_sms_key")
    void verifyUser(@Body VerifyUserRequestModel user, Callback<VerifySmsResponseModel> cb);

    @POST("/services")
    void createService(@Body CreateServiceRequestModel serviceRequestModel,Callback<CreateServiceResponseModel> cb);

    @POST("/chat")
    void sendChat(@Body SendChatRequestModel chat,Callback<String> cb);

    @POST("/multiple_chats")
    void sendBroadCastChat(@Body SendBroadcastChatRequestModel chatBroadcast,Callback<String> cb);

    @POST("/chat/user_chat_block")
    void blockUser(@QueryMap Map<String,String> params,Callback<String> cb);

    @POST("/referral")
    void postReferrer(@QueryMap Map<String,String> params,Callback<String> cb);


    //All the DELETE apis








}
