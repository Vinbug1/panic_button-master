package com.example.ubfac.panicbutton.api;

import com.google.gson.JsonObject;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by vincent on 10/31/18.
 */
public interface ApiService {

    @GET("/wp-content/themes/inec/ndmPHP.php")
    Call<JsonObject> getAllPhotos(@Query("data") String data);

    @GET("https://maps.googleapis.com/maps/api/geocode/json")
    Call<JsonObject> getGeoCoderAddress(@Query("key") String apiKey,
                                        @Query("latlng") String latlng);
}
