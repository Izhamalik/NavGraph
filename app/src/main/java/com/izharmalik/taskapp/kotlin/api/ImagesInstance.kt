package com.izharmalik.taskapp.kotlin.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASEURL = "https://jsonplaceholder.typicode.com/"
    var retrofit: Retrofit? = null
        get() {
            if (field == null) {
                field = Retrofit.Builder()
                    .baseUrl(BASEURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return field
        }
}
