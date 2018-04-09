package com.example.milymozz.orderfood.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by milymozz on 2018. 3. 8..
 */

public class IGeoRetrofit {
    private static Retrofit retrofit = null;

    public static Retrofit getGoogleClient(String baseURL) {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create()) // String 등 처리시
                    .build();
        }

        return retrofit;
    }
}
