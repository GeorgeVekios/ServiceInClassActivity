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

class MainActivity : AppCompatActivity() {
    var timerBinder: TimerService.TimerBinder? = null
    var isConnected = false

    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            isConnected = true
        }
        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
        }
    }

    val timerHandler = Handler(Looper.getMainLooper()) {

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService(
            Intent(this, TimerService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )

        findViewById<Button>(R.id.startButton).setOnClickListener {
            val binder = timerBinder
            if(binder != null) {
                if (!binder.paused && !binder.isRunning)
                    binder.start(10)
                else
                    binder.pause()
            }
        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isConnected)
                timerBinder?.stop()
        }
    }
    override fun onDestroy() {
        if (isConnected) {
            unbindService(connection)
        }
        super.onDestroy()
    }
}