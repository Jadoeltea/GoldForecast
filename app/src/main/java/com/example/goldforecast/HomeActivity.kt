package com.example.goldforecast

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var goldImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_home)

        initializeViews()
        setupAnimations()
        setupBottomNavigation()
        setupDrawerNavigation()
    }

    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        goldImage = findViewById(R.id.goldImage)

        setSupportActionBar(toolbar)
    }

    private fun setupAnimations() {
        // Animasi rotate untuk gold image
        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 2000
        rotate.repeatCount = Animation.INFINITE
        goldImage.startAnimation(rotate)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Sudah di halaman home
                    true
                }
                R.id.navigation_prediction -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_visualization -> {
                    startActivity(Intent(this, SimulationActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDrawerNavigation() {
        toolbar.setNavigationOnClickListener {
            drawerLayout.open()
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.close()
                    true
                }
                R.id.nav_prediction -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_visualization -> {
                    startActivity(Intent(this, SimulationActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_features -> {
                    startActivity(Intent(this, FeaturesActivity::class.java))
                    drawerLayout.close()
                    true
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    drawerLayout.close()
                    true
                }
                R.id.nav_model -> {
                    startActivity(Intent(this, ModelArchitectureActivity::class.java))
                    drawerLayout.close()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh animasi saat kembali ke halaman ini
        setupAnimations()
    }
} 