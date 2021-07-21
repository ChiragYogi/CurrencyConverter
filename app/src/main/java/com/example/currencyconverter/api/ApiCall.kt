package com.example.currencyconverter.api

import com.example.currencyconverter.model.CurrencyResponce
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiCall {

    @GET("/latest")
    suspend fun getAmountForBaseCurrency(@Query("base")base: String):Response<CurrencyResponce>
}
