package com.example.goldforecast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import android.content.Intent

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Inisialisasi views
        val titleText = findViewById<TextView>(R.id.aboutTitle)
        val descriptionText = findViewById<TextView>(R.id.aboutDescription)
        val backButton = findViewById<Button>(R.id.backButton)

        // Set teks
        titleText.text = "Tentang Aplikasi"
        descriptionText.text = """
            Aplikasi Prediksi Harga Emas adalah aplikasi yang menggunakan teknologi Machine Learning 
            untuk memprediksi pergerakan harga emas di masa depan. Aplikasi ini dirancang untuk membantu 
            investor dan pengguna dalam mengambil keputusan investasi yang lebih baik.

            Versi: 1.0.0
            Dibuat oleh: Irfan Zulkarnaen
            © 2025 Gold Forecast
        """.trimIndent()

        // Set click listener untuk tombol kembali
        backButton.setOnClickListener {
            finish()
        }
    }
} 