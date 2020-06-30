package com.example.ubfac.panicbutton.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by vincent on 10/31/18.
 */
public class RetrofitInstance {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://www.inecnigeria.org";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getGeoRetrofitInstance() {
        if (retrofit == null) {
            Retrofit.Builder builder = new Retrofit.Builder();
            builder.addConverterFactory(GsonConverterFactory.create());
            builder.build();
        }
        return retrofit;
    }
}
