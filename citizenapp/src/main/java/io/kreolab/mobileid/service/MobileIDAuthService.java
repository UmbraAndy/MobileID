/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.service;


import io.kreolab.mobileid.service.model.AuthTokenResponse;
import io.kreolab.mobileid.service.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MobileIDAuthService {
    String BASE_URL = "https://us-central1-mytestwebapp-7e4b6.cloudfunctions.net/";

    @POST("createToken")
    Call<AuthTokenResponse>  createToken(@Body User user);
}
