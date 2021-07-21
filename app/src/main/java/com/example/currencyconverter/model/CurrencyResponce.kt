package com.example.currencyconverter.model


import com.google.gson.annotations.SerializedName

data class CurrencyResponce(
    @SerializedName("base")
    val base: String,
    @SerializedName("rates")
    val rates: Rates
)