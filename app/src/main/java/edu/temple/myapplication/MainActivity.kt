package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private var timerBinder: TimerService.TimerBinder? = null
    private lateinit var timerTextView: TextView

    private val handler = Handler(Looper.getMainLooper()) {
        timerTextView.text = it.what.toString()
        true
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            timerBinder = binder as TimerService.TimerBinder
            timerBinder!!.setHandler(handler)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            timerBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.textView)
        bindService(Intent(this, TimerService::class.java),
            serviceConnection, Context.BIND_AUTO_CREATE)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            timerBinder?.let { b ->
                if (!b.isRunning && !b.paused) {
                    val prefs = getSharedPreferences(
                        TimerService.PREFS_NAME, MODE_PRIVATE
                    )
                    val saved = prefs.getInt(TimerService.KEY_REMAINING, 0)
                    val toStart = if (saved > 0) saved else 20
                    b.start(toStart)
                } else {
                    b.pause()
                }
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            timerBinder?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
