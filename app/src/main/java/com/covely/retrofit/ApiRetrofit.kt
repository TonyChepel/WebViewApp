package com.covely.retrofit

import com.covely.entity.InstallEntity
import com.covely.entity.PushTokenEntity
import com.covely.entity.RequestPushToken
import com.covely.entity.UserEntity
import retrofit2.http.*

interface ApiRetrofit {

    @PATCH("api/installs/{user_id}/push-token")
    @Headers(
        "Content-Type: application/json",
        "X-AUTH-TOKEN: 1fafe63df11cdb3a40a52b4f265b087f68fb6ebc05f35ef042"
    )
    suspend fun installUserIdPushToken(
        @Path("user_id") userId : String,
        @Body requestPushToken : RequestPushToken
    ): PushTokenEntity


    @POST("api/installs")
    @Headers(
        "Content-Type: application/json",
        "X-AUTH-TOKEN: 1fafe63df11cdb3a40a52b4f265b087f68fb6ebc05f35ef042"
    )
    suspend fun install(
        @Body installRequest: UserEntity
    ): InstallEntity

    @GET("api/installs/{user_id}")
    @Headers(
        "Content-Type: application/json",
        "X-AUTH-TOKEN: 1fafe63df11cdb3a40a52b4f265b087f68fb6ebc05f35ef042"
    )
    suspend fun installUser(
        @Path ("user_id") userid : String
    ) : InstallEntity
}