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
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import android.content.res.AssetFileDescriptor
import android.widget.Spinner
import android.widget.DatePicker
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private lateinit var chart: LineChart
    private lateinit var initialPriceInput: EditText
    private lateinit var daysInput: EditText
    private lateinit var simulateButton: Button
    private lateinit var initialPriceText: TextView
    private lateinit var finalPriceText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private var historicalDataMulti: List<Pair<Date, List<Double>>> = emptyList()
    private var tflite: Interpreter? = null
    private var selectedRangeStart: Int = 0
    private val minSPX = 700.0f
    private val maxSPX = 2900.0f
    private val minGLD = 70.0f
    private val maxGLD = 184.589996f
    private val minUSO = 8.0f
    private val maxUSO = 120.0f
    private val minSLV = 8.0f
    private val maxSLV = 48.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        bottomNavigation.selectedItemId = R.id.navigation_prediction
        setupChart()
        setupListeners()
        setupBottomNavigation()

        // Pindahkan proses berat ke background agar tidak ANR
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                loadHistoricalDataSafe()
                loadTFLiteModelSafe()
            }
            // Setelah selesai, update UI jika perlu
            if (historicalDataMulti.isNotEmpty()) {
                val lastPrice = historicalDataMulti.last().second[1] // GLD
                initialPriceInput.setText(lastPrice.toString())
                initialPriceInput.startAnimation(AnimationUtils.loadAnimation(this@MainActivity, android.R.anim.fade_in))
                showHistoricalData()
            }
        }
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
            setDoubleTapToZoomEnabled(true)
            isHighlightPerDragEnabled = true
            isHighlightPerTapEnabled = true
            viewPortHandler.setMaximumScaleX(10f)
            viewPortHandler.setMaximumScaleY(10f)
            
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
            val days = daysInput.text.toString().toIntOrNull()
            if (days == null || days <= 0) {
                Toast.makeText(this, "Masukkan jumlah hari yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Harga awal diambil dari data historis sesuai range
            val start = selectedRangeStart
            val end = (start + 60).coerceAtMost(historicalDataMulti.size)
            val rangePrices = historicalDataMulti.subList(start, end)
            val initialPrice = if (rangePrices.isNotEmpty()) rangePrices.last().second[1] else 0.0
            // Prediksi harga emas hari berikutnya menggunakan model TFLite
            val predicted = predictWithTFLite()
            if (predicted != null) {
                finalPriceText.text = String.format("Prediksi Harga: Rp %.2f", predicted)
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

    private suspend fun loadHistoricalDataSafe() {
        try {
            val inputStream = assets.open("gld_price_data.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            // Skip header
            reader.readLine()
            historicalDataMulti = reader.lineSequence()
                .mapNotNull { line ->
                    val values = line.split(",")
                    if (values.size >= 5) {
                        val date = try { dateFormat.parse(values[0]) } catch (e: Exception) { null }
                        val spx = values[1].toDoubleOrNull()
                        val gld = values[2].toDoubleOrNull()
                        val uso = values[3].toDoubleOrNull()
                        val slv = values[4].toDoubleOrNull()
                        if (date != null && spx != null && gld != null && uso != null && slv != null)
                            date to listOf(spx, gld, uso, slv)
                        else null
                    } else null
                }
                .toList()
            reader.close()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error loading historical data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showHistoricalData() {
        val calStart = Calendar.getInstance()
        calStart.set(2008, Calendar.JANUARY, 1)
        val calEnd = Calendar.getInstance()
        calEnd.set(2018, Calendar.DECEMBER, 31)
        val entries = historicalDataMulti.mapIndexedNotNull { index, (date, values) ->
            val price = values[1] // GLD
            if (date != null && date.after(calStart.time) && date.before(calEnd.time)) {
                Entry(index.toFloat(), price.toFloat())
            } else null
        }
        val dataSet = LineDataSet(entries, "Data Historis GLD 2008-2018").apply {
            color = Color.rgb(0, 128, 255)
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
        val historicalEntries = historicalDataMulti.mapIndexed { index, (date, values) ->
            Entry(index.toFloat(), values[1].toFloat())
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

    private suspend fun loadTFLiteModelSafe() {
        try {
            val fileDescriptor: AssetFileDescriptor = assets.openFd("model.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            tflite = Interpreter(modelBuffer)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Gagal memuat model TFLite: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun predictWithTFLite(): Float? {
        return try {
            // Ambil 4 fitur terakhir dari data historis
            if (historicalDataMulti.isEmpty()) return null
            val last = historicalDataMulti.last().second
            val input = Array(1) { FloatArray(4) }
            input[0][0] = ((last[0] - minSPX) / (maxSPX - minSPX)).toFloat()
            input[0][1] = ((last[1] - minGLD) / (maxGLD - minGLD)).toFloat()
            input[0][2] = ((last[2] - minUSO) / (maxUSO - minUSO)).toFloat()
            input[0][3] = ((last[3] - minSLV) / (maxSLV - minSLV)).toFloat()

            val output = Array(1) { FloatArray(1) }
            tflite?.run(input, output)
            val predNorm = output[0][0]
            val predAsli = predNorm * (maxGLD - minGLD) + minGLD
            predAsli
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal prediksi dengan TFLite: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chart.clear()
        chart.invalidate()
    }
} 