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

class SimulationActivity : AppCompatActivity() {
    private lateinit var chart: LineChart
    private lateinit var initialPriceInput: EditText
    private lateinit var daysInput: EditText
    private lateinit var simulateButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulation)

        try {
            // Inisialisasi views
            chart = findViewById(R.id.simulationChart)
            initialPriceInput = findViewById(R.id.initialPriceInput)
            daysInput = findViewById(R.id.daysInput)
            simulateButton = findViewById(R.id.simulateButton)
            backButton = findViewById(R.id.backButton)

            // Setup chart
            setupChart()

            // Set click listener untuk tombol simulasi
            simulateButton.setOnClickListener {
                simulatePrice()
            }

            // Set click listener untuk tombol kembali
            backButton.setOnClickListener {
                onBackPressed()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saat inisialisasi: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        try {
            super.onBackPressed()
        } catch (e: Exception) {
            finish()
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
                    axisLineColor = Color.BLACK
                    textColor = Color.BLACK
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

            // Generate data simulasi
            val entries = mutableListOf<Entry>()
            var currentPrice = initialPrice
            val random = Random()

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

        } catch (e: Exception) {
            Toast.makeText(this, "Error saat simulasi: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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