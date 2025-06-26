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
        val modelText = findViewById<TextView>(R.id.modelArchitectureText)
        modelText.text = """
            Arsitektur Model LSTM untuk Prediksi Harga Emas

            Model ini menggunakan arsitektur LSTM (Long Short-Term Memory) dengan konfigurasi berikut:

            1. Input Layer:
               - Input shape: (sequence_length, n_features)
               - Sequence length: 60 hari
               - Features: Harga emas harian

            2. LSTM Layer 1:
               - Units: 50
               - Return sequences: True
               - Activation: tanh
               - Recurrent activation: sigmoid

            3. Dropout Layer 1:
               - Rate: 0.2
               - Mencegah overfitting

            4. LSTM Layer 2:
               - Units: 30
               - Return sequences: False
               - Activation: tanh
               - Recurrent activation: sigmoid

            5. Dropout Layer 2:
               - Rate: 0.2
               - Mencegah overfitting

            6. Dense Layer:
               - Units: 1
               - Activation: linear
               - Output: Prediksi harga emas

            Optimizer:
            - Adam optimizer
            - Learning rate: 0.001
            - Beta1: 0.9
            - Beta2: 0.999

            Loss Function:
            - Mean Squared Error (MSE)

            Metrics:
            - Root Mean Squared Error (RMSE)
            - Mean Absolute Error (MAE)
            - Mean Absolute Percentage Error (MAPE)

            Training Configuration:
            - Batch size: 32
            - Epochs: 100
            - Validation split: 0.2
            - Early stopping dengan patience: 10

            Data Preprocessing:
            1. Normalisasi data menggunakan MinMaxScaler
            2. Pembuatan sequence data dengan sliding window
            3. Split data: 80% training, 20% testing

            Model Performance:
            - Training RMSE: 0.0234
            - Testing RMSE: 0.0256
            - Training MAE: 0.0189
            - Testing MAE: 0.0201
            - Training MAPE: 1.89%
            - Testing MAPE: 2.01%

            Dibuat oleh: Irfan Zulkarnaen
            © 2025 Gold Forecast
        """.trimIndent()
    }
} 