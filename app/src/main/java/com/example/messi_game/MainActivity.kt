package com.example.messi_game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.messi_game.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playButton.setOnClickListener {
            // Aquí puedes lanzar otra Activity o iniciar el juego
        }
    }
}
