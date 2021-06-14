package com.gor.mqtt.sensors.sensors

import android.content.Context
import com.gor.mqtt.sensors.interfaces.NotifyView
import com.gor.mqtt.sensors.interfaces.SensorCommunication
import com.gor.mqtt.sensors.sensors.Sensor as GSensor

@Deprecated("not implemented yet")
class TemperatureSensor (
    context: Context,
    private val notifyView: NotifyView
) : SensorCommunication {


    private var sensor: GSensor?= null

    init {
        sensor = GSensor(context, this, "android.sensor.temperature")
    }

    override fun notifyDataChange(data: FloatArray) {
        val tmp = "Temperature \n ${data[0]}"
        notifyView.updateView(tmp)
    }
}

