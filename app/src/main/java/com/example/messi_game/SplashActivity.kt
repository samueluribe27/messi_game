package com.example.messi_game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    
    private val splashDuration = 2500L // 2.5 segundos
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Animación de fade in para el logo
        val logoView = findViewById<View>(R.id.splashLogo)
        val titleView = findViewById<View>(R.id.splashTitle)
        
        // Fade in del logo
        ObjectAnimator.ofFloat(logoView, "alpha", 0f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        // Fade in del título con delay
        ObjectAnimator.ofFloat(titleView, "alpha", 0f, 1f).apply {
            duration = 800
            startDelay = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        // Escala del logo
        ObjectAnimator.ofFloat(logoView, "scaleX", 0.8f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        ObjectAnimator.ofFloat(logoView, "scaleY", 0.8f, 1f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        // Navegar a MainActivity después del splash
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, splashDuration)
    }
}

