package com.example.goldforecast

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.animation.Easing
import android.graphics.Color
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlin.random.Random
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var chart: LineChart
    private lateinit var initialPriceInput: EditText
    private lateinit var daysInput: EditText
    private lateinit var simulateButton: Button
    private lateinit var initialPriceText: TextView
    private lateinit var finalPriceText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private var historicalData: List<Pair<Date, Double>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupChart()
        setupListeners()
        setupBottomNavigation()
        loadHistoricalData()
    }

    private fun initializeViews() {
        chart = findViewById(R.id.simulationChart)
        initialPriceInput = findViewById(R.id.initialPriceInput)
        daysInput = findViewById(R.id.daysInput)
        simulateButton = findViewById(R.id.simulateButton)
        initialPriceText = findViewById(R.id.initialPriceText)
        finalPriceText = findViewById(R.id.finalPriceText)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                gridLineWidth = 0.5f
                enableGridDashedLine(10f, 10f, 0f)
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = true
                textSize = 12f
                form = Legend.LegendForm.LINE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            // Animasi chart
            animateX(1000, Easing.EaseInOutQuad)
        }
    }

    private fun setupListeners() {
        simulateButton.setOnClickListener {
            val initialPrice = initialPriceInput.text.toString().toDoubleOrNull()
            val days = daysInput.text.toString().toIntOrNull()

            if (initialPrice == null || days == null) {
                Toast.makeText(this, "Masukkan nilai yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (initialPrice <= 0 || days <= 0) {
                Toast.makeText(this, "Nilai harus lebih besar dari 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            simulatePrice(initialPrice, days)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    false
                }
                R.id.navigation_prediction -> {
                    // Sudah di halaman prediksi
                    true
                }
                R.id.navigation_visualization -> {
                    startActivity(Intent(this, SimulationActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun loadHistoricalData() {
        try {
            val inputStream = assets.open("gld_price_data.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            // Skip header
            reader.readLine()
            
            historicalData = reader.lineSequence()
                .map { line ->
                    val values = line.split(",")
                    val date = dateFormat.parse(values[0])
                    val price = values[1].toDoubleOrNull() ?: 0.0
                    date to price
                }
                .toList()
            
            reader.close()
            
            // Set nilai awal dari data historis
            if (historicalData.isNotEmpty()) {
                val lastPrice = historicalData.last().second
                initialPriceInput.setText(lastPrice.toString())
                // Animasi fade in untuk input
                initialPriceInput.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

                // Tampilkan data historis di chart
                showHistoricalData()
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading historical data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHistoricalData() {
        val entries = historicalData.mapIndexed { index, (_, price) ->
            Entry(index.toFloat(), price.toFloat())
        }

        val dataSet = LineDataSet(entries, "Data Historis").apply {
            color = Color.rgb(0, 128, 255) // Warna biru
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = Color.rgb(0, 128, 255)
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.data = LineData(dataSet)
        chart.invalidate()
        chart.animateX(1000)
    }

    private fun simulatePrice(initialPrice: Double, days: Int) {
        val entries = mutableListOf<Entry>()
        var currentPrice = initialPrice
        entries.add(Entry(0f, initialPrice.toFloat()))

        // Simulasi perubahan harga menggunakan random walk
        for (i in 1..days) {
            // Simulasi perubahan harga dengan random walk
            val change = Random.Default.nextDouble(-0.02, 0.02) // Perubahan -2% sampai +2%
            currentPrice *= (1 + change)
            entries.add(Entry(i.toFloat(), currentPrice.toFloat()))
        }

        // Update tampilan harga awal dan akhir
        initialPriceText.text = String.format("Rp %.2f", initialPrice)
        finalPriceText.text = String.format("Rp %.2f", currentPrice)

        // Buat dataset dan tampilkan di chart
        val dataSet = LineDataSet(entries, "Simulasi Harga Emas").apply {
            color = Color.rgb(255, 165, 0) // Warna oranye
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = Color.rgb(255, 165, 0)
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Gabungkan data historis dan simulasi
        val historicalEntries = historicalData.mapIndexed { index, (_, price) ->
            Entry(index.toFloat(), price.toFloat())
        }
        val historicalDataSet = LineDataSet(historicalEntries, "Data Historis").apply {
            color = Color.rgb(0, 128, 255) // Warna biru
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = Color.rgb(0, 128, 255)
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.data = LineData(historicalDataSet, dataSet)
        chart.invalidate()
        chart.animateX(1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        chart.clear()
        chart.invalidate()
    }
} 