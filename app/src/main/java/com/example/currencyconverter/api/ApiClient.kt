package com.example.currencyconverter.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory







object ApiClient {


        private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.exchangerate.host")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    val api = retrofit.create(ApiCall::class.java)



}