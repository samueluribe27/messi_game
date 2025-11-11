package com.example.messi_game

import android.content.Context
import android.content.SharedPreferences

class PlayerPreferences(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Get current player name
    fun getPlayerName(): String {
        return prefs.getString(KEY_PLAYER_NAME, DEFAULT_PLAYER_NAME) ?: DEFAULT_PLAYER_NAME
    }
    
    // Set player name
    fun setPlayerName(name: String) {
        prefs.edit().putString(KEY_PLAYER_NAME, name).apply()
    }
    
    // Get record for current player (highest score across all difficulties)
    fun getPlayerRecord(): Int {
        val playerName = getPlayerName()
        val easy = getScoreForDifficulty("easy")
        val medium = getScoreForDifficulty("medium")
        val hard = getScoreForDifficulty("hard")
        return maxOf(easy, medium, hard)
    }
    
    // Get score for specific difficulty
    fun getScoreForDifficulty(difficulty: String): Int {
        val playerName = getPlayerName()
        return prefs.getInt("${KEY_SCORE_PREFIX}_${playerName}_$difficulty", 0)
    }
    
    // Update score for specific difficulty if new score is higher
    fun updateScoreForDifficulty(difficulty: String, newScore: Int) {
        val playerName = getPlayerName()
        val currentScore = getScoreForDifficulty(difficulty)
        if (newScore > currentScore) {
            prefs.edit().putInt("${KEY_SCORE_PREFIX}_${playerName}_$difficulty", newScore).apply()
        }
    }
    
    // Get all player names
    fun getAllPlayerNames(): List<String> {
        val allKeys = prefs.all.keys
        return allKeys
            .filter { it.startsWith(KEY_SCORE_PREFIX) }
            .map { it.removePrefix("${KEY_SCORE_PREFIX}_").substringBefore("_") }
            .distinct()
            .sorted()
    }
    
    companion object {
        private const val PREFS_NAME = "messi_game_prefs"
        private const val KEY_PLAYER_NAME = "current_player_name"
        private const val KEY_SCORE_PREFIX = "score"
        private const val DEFAULT_PLAYER_NAME = "Invitado"
    }
}

