package com.example.labexam03

import android.os.Handler

object TimerManager {
    var elapsedTime: Long = 0 // Time in seconds
    private var isTimerRunning = false
    private val handler = Handler()

    fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (isTimerRunning) {
                        elapsedTime++
                        handler.postDelayed(this, 1000)
                    }
                }
            }, 1000)
        }
    }

    fun stopTimer() {
        isTimerRunning = false
    }

    fun resetTimer() {
        elapsedTime = 0
    }
}
