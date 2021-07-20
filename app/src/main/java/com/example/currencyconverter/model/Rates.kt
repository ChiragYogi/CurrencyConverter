package com.example.currencyconverter.model


import com.google.gson.annotations.SerializedName

data class Rates(

    @SerializedName("USD")
    val uSD: Double,
    @SerializedName("EUR")
    val eUR: Double,
    @SerializedName("INR")
    val iNR: Double,
    @SerializedName("CAD")
    val cAD: Double,

)