package com.gor.mqtt.sensors.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.gor.mqtt.sensors.interfaces.SensorCommunication
import java.util.*

class Sensor constructor(
    context: Context,
    private val sensorCommunication: SensorCommunication,
    private val intentAction: String,
) : BroadcastReceiver() {


    private var delay: Long = 1000L
    private var isRegistered = false

    init {
        val intentFilter = IntentFilter(intentAction)
        context.registerReceiver(this, intentFilter)
        isRegistered = true
        updateDelay(delay)
    }

    fun unregisterReceiver(context: Context) {
        if (isRegistered) {
            isRegistered = false
            context.unregisterReceiver(this)
        }
    }

    fun registerReceiver(context: Context) {
        if (!isRegistered) {
            isRegistered = true
            context.registerReceiver(this, IntentFilter(intentAction))
        }
    }


    private var incomingDataFloatArray: FloatArray? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == intentAction) {
            val tmpArray = intent.getFloatArrayExtra("value")
            if (tmpArray != null) {
                incomingDataFloatArray = tmpArray
            }
        }
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    fun updateDelay(value: Long) {

        if (timer != null) {
            timer?.cancel()
            timer = null
        }

        if (value > 0) {
            this.delay = value
            timer = Timer()

            timerTask = object : TimerTask() {
                override fun run() {
                    if (incomingDataFloatArray != null) {
                        sensorCommunication.notifyDataChange(incomingDataFloatArray!!)
                    }
                }
            }

            timer?.scheduleAtFixedRate(timerTask, 0, delay)
        }
    }
}