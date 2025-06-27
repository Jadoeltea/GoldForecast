package com.example.goldforecast

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.material.appbar.MaterialToolbar

class ModelArchitectureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_architecture)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup model architecture text
        // val modelText = findViewById<TextView>(R.id.modelArchitectureText)
        // modelText.text = """
        //     Arsitektur Model LSTM untuk Prediksi Harga Emas
        //     ... (narasi dihapus karena sudah di layout XML)
        // """.trimIndent()
    }
} 