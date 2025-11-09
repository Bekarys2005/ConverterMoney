package com.example.converter_money

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var amountEditText: EditText
    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var resultTextView: TextView

    private val currencies = listOf("USD", "EUR", "KZT", "RUB")

    private val API_KEY = "MYYonp1sXltqST02ozOiFkJqPtaAmdFv"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountEditText = findViewById(R.id.amountEditText)
        fromSpinner = findViewById(R.id.fromCurrencySpinner)
        toSpinner = findViewById(R.id.toCurrencySpinner)
        resultTextView = findViewById(R.id.resultTextView)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter

        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { convertCurrency() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) { convertCurrency() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) { convertCurrency() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun convertCurrency() {
        val amountText = amountEditText.text.toString()
        if (amountText.isEmpty()) {
            resultTextView.text = "Result"
            return
        }

        val amount = amountText.toDoubleOrNull() ?: return
        val from = fromSpinner.selectedItem.toString()
        val to = toSpinner.selectedItem.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.exchangerate.host/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(ExchangeRateApi::class.java)
                val response = service.getRate(API_KEY, from, to)

                val rate = response.rates[to] ?: 0.0
                val result = amount * rate

                withContext(Dispatchers.Main) {
                    val formatter = DecimalFormat("#,###.##")
                    val formattedResult = formatter.format(result)
                    resultTextView.text = "$formattedResult $to"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resultTextView.text = "Connection error"
                }
            }
        }
    }
}

interface ExchangeRateApi {
    @GET("latest")
    suspend fun getRate(
        @Header("apikey") apiKey: String,
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val rates: Map<String, Double>
)
