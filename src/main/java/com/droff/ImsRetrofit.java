package com.droff;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ImsRetrofit {

  @FormUrlEncoded
  @POST("/ims/exchange/jwt")
  Call<AccessToken> getAccessToken(
      @Field("client_id") String clientId,
      @Field("client_secret") String clientSecret,
      @Field("jwt_token") String jwtToken);


}
