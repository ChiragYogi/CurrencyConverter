package com.example.currencyconverter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.currencyconverter.api.ApiClient
import com.example.currencyconverter.databinding.ActivityMainBinding
import com.example.currencyconverter.model.Rates
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var baseCurrency: String? = null
    private var toConvertCurrency: String? = null
    private lateinit var finalAmountToDisplay: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val amountInEt = binding.amountToConvertEt.text

        finalAmountToDisplay = binding.finalAMountTxt


        selectSpinner()



        binding.convertButton.setOnClickListener {


            hideSoftKeyboard()

            if (baseCurrency == toConvertCurrency) {
                Toast.makeText(
                    this@MainActivity, "Please Select Different Currency",
                    Toast.LENGTH_SHORT
                ).show()


            } else {

                convert(
                    amountInEt.toString(),
                    baseCurrency.toString(),
                    toConvertCurrency.toString()
                )

                amountInEt?.clear()
            }
        }


    }


    private fun selectSpinner() {
        val baseSpinner = binding.fromSpinner

        ArrayAdapter.createFromResource(
            this, R.array.from_currency, android.R.layout.simple_spinner_item).also { baseAdapter ->

            baseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

            baseSpinner.adapter = baseAdapter
        }

        baseSpinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {


            }
        })

        val currencyToConvertSpinner = binding.toSpinner

        ArrayAdapter.createFromResource(
            this, R.array.to_currency, android.R.layout.simple_spinner_item
        ).also { toAdapter ->

            toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

            currencyToConvertSpinner.adapter = toAdapter

        }

        currencyToConvertSpinner.onItemSelectedListener =
            (object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    toConvertCurrency = parent?.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            })


    }



    private fun convert(amount: String, fromCurrecy: String, toCurrecy: String) {


        val fromAmount = amount.toFloatOrNull()

        if (fromAmount == null) {
            Toast.makeText(
                this@MainActivity, "Please Enter The Amount",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // if device has internet connection then the we request for api if there is no internet
        // connection then we show a SnackBar which have action to go to Setting Screen
        if (haseInterNetConnection()){

        GlobalScope.launch(Dispatchers.Main) {

            val responce = withContext(Dispatchers.IO)
            { ApiClient.api.getAmountForBaseCurrency(fromCurrecy) }

            Log.d("debug log", "$responce")

                try {

                    if (responce.isSuccessful) {
                        responce.body().let {

                            val fromBaseCurrencyRates = it!!.rates


                            // getting the rates of currency based on base currency
                            val convertToSelectedCurrencyRate =
                                getRateForCurrency(toCurrecy, fromBaseCurrencyRates)

                            // converting the currency using math round fun in kotlin
                            val convertedCurrency =
                                round(fromAmount * convertToSelectedCurrencyRate.toString()
                                        .toFloat() * 100) / 100



                            finalAmountToDisplay.text = convertedCurrency.toString()




                            Log.d("debug log", "$convertedCurrency")

                        }

                    } else {

                        Toast.makeText(this@MainActivity, "Something Went Wrong",
                            Toast.LENGTH_SHORT) .show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(applicationContext,"$e",Toast.LENGTH_SHORT).show()


                }


            } }else{

                Snackbar.make(findViewById(R.id.currency),"Please Check Your Internet Connection",
                    Snackbar.LENGTH_SHORT).setAction("OPEN") {
                    startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
                } .show()



            }
        }


    //fun to get Rates based on currency code
    private fun getRateForCurrency(currency: String, rates: Rates) = when (currency) {

        "CAD" -> rates.cAD
        "EUR" -> rates.eUR
        "INR" -> rates.iNR
        "USD" -> rates.uSD
        else -> null
    }

    // checking for Internet connection
    private fun haseInterNetConnection(): Boolean {

        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val activeNetwork = cm.activeNetwork ?: return false
            val capebiletey = cm.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capebiletey.hasTransport(TRANSPORT_WIFI) -> true
                capebiletey.hasTransport(TRANSPORT_CELLULAR) -> true
                capebiletey.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }


        } else {

            cm.activeNetworkInfo?.run {
                return when (type) {

                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false

    }


    // saving state of finalAmount
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("Converted_Amount", finalAmountToDisplay.text.toString())
    }

    // restoring state of finalAmount
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        finalAmountToDisplay.text = savedInstanceState.getString("Converted_Amount")



    }

    //hiding the keyboard
    private fun hideSoftKeyboard(){

        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken,0)

    }
}




























