package com.izharmalik.taskapp.kotlin.api

import retrofit2.Call
import retrofit2.http.GET

interface ImagesAPI {
    @GET("/photos")
    fun getImages(): Call<List<Photos>>
}