package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false
    private var paused = false
    private var currentValue = 0
    private var timerHandler: Handler? = null
    private lateinit var t: TimerThread

    inner class TimerBinder : Binder() {
        val isRunning: Boolean get() = this@TimerService.isRunning
        val paused: Boolean    get() = this@TimerService.paused

        fun start(startValue: Int) {

            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val saved = prefs.getInt(KEY_REMAINING, 0)

            val toStart = if (saved > 0) {
                saved
            } else {
                startValue
            }
            prefs.edit().remove(KEY_REMAINING).apply()

            if (isRunning) {
                togglePause()
            } else {
                if (::t.isInitialized) t.interrupt()
                startTimer(toStart)
            }
        }

        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        fun stop() {
            if (::t.isInitialized) t.interrupt()
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit().remove(KEY_REMAINING).apply()
        }

        fun pause() {
            this@TimerService.togglePause()
        }
    }

    override fun onBind(intent: Intent): IBinder = TimerBinder()

    private fun startTimer(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    private fun togglePause() {
        if (!::t.isInitialized) return

        paused = !paused
        isRunning = !paused

        if (paused) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(KEY_REMAINING, currentValue)
                .apply()
            Log.d("TimerService", "Paused at $currentValue s")
        } else {
            Log.d("TimerService", "Resuming from $currentValue s")
        }
    }

    inner class TimerThread(startValue: Int) : Thread() {
        private var value = startValue

        override fun run() {
            isRunning = true
            paused = false
            try {
                for (i in value downTo 1) {
                    currentValue = i
                    Log.d("Countdown", "$i")

                    timerHandler?.sendEmptyMessage(i)

                    while (paused) { Thread.yield() }
                    sleep(1000)
                }
                // finished naturally
                isRunning = false
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit().remove(KEY_REMAINING).apply()
                Log.d("TimerService", "Finished!")
            } catch (e: InterruptedException) {
                Log.d("TimerService", "Interrupted, stopping")
                isRunning = false
                paused = false
            }
        }
    }

    companion object {
        const val PREFS_NAME     = "countdown_prefs"
        const val KEY_REMAINING  = "remaining_time"
    }
}