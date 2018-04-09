package com.example.milymozz.orderfood.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by milymozz on 2018. 3. 8..
 */

public interface IGoogleService {
    @GET
    Call<String> getAddressName(@Url String url);
}
