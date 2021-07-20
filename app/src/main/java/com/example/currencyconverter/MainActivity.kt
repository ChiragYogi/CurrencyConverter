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
                    this@MainActivity, "Plese Select Different Currency",
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

      /*  finalAmountToDisplay = binding.finalAMountTxt*/
        val fromAmount = amount.toFloatOrNull()
        if (fromAmount == null) {
            Toast.makeText(
                this@MainActivity, "Please Enter The Amount",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        GlobalScope.launch(Dispatchers.Main) {

            if (haseInterNetConnection()) {


            val responce = withContext(Dispatchers.IO)
            { ApiClient.api.getAmountForBaseCurrency(fromCurrecy) }

            Log.d("debug log", "$responce")

                try {

                    if (responce.isSuccessful) {
                        responce.body().let {

                            val fromBaseCurrencyRates = it!!.rates

                            val convertToSelectedCurrencyRate =
                                getRateForCurrency(toCurrecy, fromBaseCurrencyRates)

                            val convertedCurrency =
                                round(
                                    fromAmount * convertToSelectedCurrencyRate.toString()
                                        .toFloat() * 100
                                ) / 100
                            /*    ((fromAmount.times(
                                convertToSelectedCurrencyRate!!.toString().toFloat()
                            )) * 100).roundToLong() / 100*/

                            finalAmountToDisplay.text = convertedCurrency.toString()

                            Log.d("debug log", "$convertedCurrency")

                        }

                    } else {

                        Toast.makeText(this@MainActivity, "Somthinge Went Wrong",
                            Toast.LENGTH_SHORT) .show()
                    }

                } catch (e: Exception) {
                    when(e){
                        is NumberFormatException  -> {
                            finalAmountToDisplay.text = R.string.error.toString()
                        }
                    }



                }


            }else{
                Snackbar.make(findViewById(R.id.currency),"Please Check Your Internet Connection",
                    Snackbar.LENGTH_SHORT).setAction("OPEN") {
                    startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
                } .show()



            }
        }
    }


    private fun getRateForCurrency(currency: String, rates: Rates) = when (currency) {

        "CAD" -> rates.cAD

        "INR" -> rates.iNR
        "USD" -> rates.uSD
        else -> null
    }


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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("Converted_Amount", finalAmountToDisplay.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        finalAmountToDisplay.text = savedInstanceState.getString("Converted_Amount")



    }

    private fun hideSoftKeyboard(){

        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken,0)

    }
}




























