package com.example.goldforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.Legend
import android.graphics.Color
import java.util.*
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import android.content.res.AssetFileDescriptor
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InputStream
import android.util.Log
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class SimulationActivity : AppCompatActivity() {
    private lateinit var chart: LineChart
    private lateinit var initialPriceInput: EditText
    private lateinit var daysInput: EditText
    private lateinit var simulateButton: Button
    private lateinit var finalPriceTextView: TextView
    private var historicalData: List<Pair<Date, Double>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulation)

        try {
            // Inisialisasi views
            chart = findViewById(R.id.simulationChart)
            initialPriceInput = findViewById(R.id.initialPriceInput)
            daysInput = findViewById(R.id.daysInput)
            simulateButton = findViewById(R.id.simulateButton)
            finalPriceTextView = findViewById(R.id.finalPriceTextView)

            // Setup BottomNavigationView
            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigation.selectedItemId = R.id.navigation_visualization
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        startActivity(Intent(this, HomeActivity::class.java))
                        false
                    }
                    R.id.navigation_prediction -> {
                        startActivity(Intent(this, MainActivity::class.java))
                        false
                    }
                    R.id.navigation_visualization -> {
                        // Sudah di halaman visualisasi
                        true
                    }
                    else -> false
                }
            }

            // Setup chart
            setupChart()
            loadHistoricalData()

            // Set click listener untuk tombol simulasi
            simulateButton.setOnClickListener {
                simulatePrice()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saat inisialisasi: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupChart() {
        try {
            chart.apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDoubleTapToZoomEnabled(true)
                isHighlightPerDragEnabled = true
                isHighlightPerTapEnabled = true
                setScaleXEnabled(true)
                setScaleYEnabled(true)
                viewPortHandler.setMaximumScaleX(10f)
                viewPortHandler.setMaximumScaleY(10f)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = -45f
                    textColor = Color.BLACK
                    textSize = 12f
                    valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()
                        }
                    }
                    axisLineColor = Color.BLACK
                    axisLineWidth = 1f
                    setLabelCount(6, true)
                    this@apply.axisMinimum = 0f
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    gridLineWidth = 0.5f
                    axisLineColor = Color.BLACK
                    textColor = Color.BLACK
                    textSize = 12f
                    axisMinimum = 0f
                }
                
                axisRight.isEnabled = false
                
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    form = Legend.LegendForm.LINE
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }
                this.extraBottomOffset = 20f
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saat setup chart: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun simulatePrice() {
        try {
            val initialPrice = initialPriceInput.text.toString().toDoubleOrNull()
            val days = daysInput.text.toString().toIntOrNull()

            if (days == null || initialPrice == null) {
                Toast.makeText(this, "Masukkan nilai yang valid", Toast.LENGTH_SHORT).show()
                return
            }

            if (days <= 0 || initialPrice <= 0) {
                Toast.makeText(this, "Nilai harus lebih besar dari 0", Toast.LENGTH_SHORT).show()
                return
            }

            // Gunakan seed berdasarkan input agar hasil simulasi konsisten untuk setiap kombinasi input
            val seed = (initialPrice * 1000).toLong() + days
            val random = Random(seed)

            // Generate data simulasi
            val entries = mutableListOf<Entry>()
            var currentPrice = initialPrice

            for (i in 0 until days) {
                // Simulasi pergerakan harga dengan random walk
                val change = (random.nextDouble() - 0.5) * 10 // Perubahan antara -5 dan +5
                currentPrice += change
                entries.add(Entry(i.toFloat(), currentPrice.toFloat()))
            }

            // Update chart
            val dataSet = LineDataSet(entries, "Simulasi Harga").apply {
                color = Color.rgb(255, 165, 0) // Warna oranye
                setDrawCircles(false)
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = Color.rgb(255, 165, 0)
                fillAlpha = 30
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
            chart.animateX(1000)

            // Tampilkan harga akhir
            finalPriceTextView.text = "Harga Akhir: %.2f".format(currentPrice)

        } catch (e: Exception) {
            Toast.makeText(this, "Error saat simulasi: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun loadHistoricalData() {
        try {
            val inputStream = assets.open("gld_price_data.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val dateFormat = java.text.SimpleDateFormat("M/d/yyyy", java.util.Locale.getDefault())
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
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading historical data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        try {
            chart.clear()
            chart.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
} 