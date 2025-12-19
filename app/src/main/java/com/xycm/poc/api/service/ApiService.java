package com.xycm.poc.api.service;

import com.xycm.poc.api.req.LoginBody;
import com.xycm.poc.api.resp.LoginResponse;
import com.xycm.poc.api.resp.UserInfoResponse;
import com.xycm.poc.util.Result;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/system/login")
    Call<Result<LoginResponse>> login(@Body LoginBody loginBody);

    @GET("api/system/user/getInfo")
    Call<Result<UserInfoResponse>> getUserInfo();
}
