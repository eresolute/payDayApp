package com.fh.payday.datasource.remote.message

import com.fh.payday.datasource.models.message.MessageBody
import com.fh.payday.datasource.models.message.MessageResponse
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface MessageApiInterface{
    @POST("$apiVersion/customers/{id}/support")
    @FormUrlEncoded
    fun sendMessage(@Path("id") id: Long,
                    @Field("subject") subject: String,
                    @Field("issue") issue: String,
                    @Field("body") body: String
    ): Call<ApiResult<MessageResponse>>
    @GET("$apiVersion/customers/{id}/support")
    fun getMessages(@Path("id") id: Long
    ): Call<ApiResult<List<MessageBody>>>
    @GET("$apiVersion/customers/{id}/issueAreas")
    fun getIssueAreas(@Path("id") id: Long): Call<ApiResult<List<String>>>

    @GET("$apiVersion/customers/{id}/issues")
    fun getIssues(@Path("id") id: Long,
                  @Query("issueArea") issueArea: String): Call<ApiResult<List<String>>>
}