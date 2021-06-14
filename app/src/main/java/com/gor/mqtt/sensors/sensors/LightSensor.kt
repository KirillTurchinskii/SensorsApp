package com.gor.mqtt.sensors.sensors

import android.content.Context
import com.gor.mqtt.sensors.InstanceIsNull
import com.gor.mqtt.sensors.interfaces.*
import com.gor.mqtt.sensors.utils.JsonTools
import com.gor.mqtt.sensors.sensors.Sensor as GSensor

class LightSensor(
    context: Context,
    private val listenersList: ArrayList<NotifyView> = ArrayList(),
    private val handleIncomingJSONsList: ArrayList<HandelIncomingJSONs> = ArrayList(),
    private val dataListenersList: ArrayList<NotifyViewByData> = ArrayList(),

    ) : SensorCommunication, SensorActions {


    private var sensor: GSensor? = null

    init {
        sensor = GSensor(context, this, "android.sensor.light")
    }


    companion object {
        @Volatile
        private var instance: LightSensor? = null

        @Synchronized
        fun getInstance(context: Context): LightSensor {
            if (instance == null) {
                instance = LightSensor(context)
            }
            return instance as LightSensor
        }

        @Synchronized
        fun getInstance(): LightSensor {
            return if (instance != null) instance as LightSensor
            else throw InstanceIsNull("You should call at first getInstance(context: Context)")
        }
    }

    @Synchronized
    override fun notifyDataChange(data: FloatArray) {
        val tmp = "Light \n  ${data[0]} \n lux"
        listenersList.forEach { it.updateView(tmp)}
        dataListenersList.forEach { it.updateView(data) }
    }

    @Synchronized
    override fun addDataListenerFromSensor(notifyView: NotifyView) {
        listenersList.add(notifyView)
    }
    @Synchronized
    override fun addRawDataListenerFromSensor(notifyView: NotifyViewByData) {
        dataListenersList.add(notifyView)
    }

    override fun registerReceiver(context: Context) {
        sensor?.registerReceiver(context)
    }

    override fun unregisterReceiver(context: Context) {
        sensor?.unregisterReceiver(context)
    }

    override fun updateDataReceivingDelay(value: Long) {
        sensor?.updateDelay(value)
    }

    override fun stopTimer() {
        sensor?.updateDelay(0L)
    }

    override fun startTimer() {
        sensor?.updateDelay(1000L)
    }

    @Synchronized
    override fun passActionFromServer(json: JsonTools) {
        handleIncomingJSONsList.forEach { it.handleIncomingJSON(json)}
    }
    @Synchronized
    override fun addIncomingDataServerHandler(handleIncomingJSONs: HandelIncomingJSONs) {
        this.handleIncomingJSONsList.add(handleIncomingJSONs)
    }

    @Synchronized
    override fun clearAllHandlersOnDestroy() {
        listenersList.clear()
        handleIncomingJSONsList.clear()
        dataListenersList.clear()
    }
}

data class LightSensorJSON(val name: String, val data: LightSensorDataJSON) : JsonTools()
data class LightSensorDataJSON(val lux: Float)

// на вход от сервера
data class RequestFromServerToLightSensor(val name: String, val data: SomeInnerDataClassLightSensor ): JsonTools()
data class SomeInnerDataClassLightSensor(val r: Float, val g: Float, val b: Float)