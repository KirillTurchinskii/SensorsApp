package com.gor.mqtt.sensors.sensors

import android.content.Context
import com.gor.mqtt.sensors.InstanceIsNull
import com.gor.mqtt.sensors.interfaces.*
import com.gor.mqtt.sensors.utils.JsonTools
import com.gor.mqtt.sensors.sensors.Sensor as GSensor

class GyroscopeSensor(
    context: Context,
    private val listenersList: ArrayList<NotifyView> = ArrayList(),
    private val handleIncomingJSONsList: ArrayList<HandelIncomingJSONs> = ArrayList(),
    private val dataListenersList: ArrayList<NotifyViewByData> = ArrayList(),
) : SensorCommunication, SensorActions {


    private var sensor: GSensor? = null

    init {
        sensor = GSensor(context, this, "android.sensor.gyroscope")
    }

    companion object {
        @Volatile
        private var instance: GyroscopeSensor? = null

        @Synchronized
        fun getInstance(context: Context): GyroscopeSensor {
            if (instance == null) {
                instance = GyroscopeSensor(context)
            }
            return instance as GyroscopeSensor
        }

        @Synchronized
        fun getInstance(): GyroscopeSensor {
            return if (instance != null) instance as GyroscopeSensor
            else throw InstanceIsNull("You should call at first getInstance(context: Context)")
        }
    }

    @Synchronized
    override fun notifyDataChange(data: FloatArray) {
        val tmp = "Gyroscope \n x ${data[0]} \n y ${data[1]} \n z ${data[2]} \n Radians"
        listenersList.forEach { it.updateView(tmp) }
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

    //сервер вызывает метод, который в свою очередь вызывает
    // всех обработчиков передвая json
    @Synchronized
    override fun passActionFromServer(json: JsonTools) {
        handleIncomingJSONsList.forEach { it.handleIncomingJSON(json) }
    }

    // добавляем обработчиков входящего запроса с сервера
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

data class GyroscopeSensorJSON(val name: String, val data: GyroscopeSensorDataJSON) : JsonTools()
data class GyroscopeSensorDataJSON(val x: Float, val y: Float, val z: Float)

//
data class RequestFromServerToGyroscopeSensorJSON(
    val name: String,
    val data: SomeInnerDataClassGyroscopeSensor
) : JsonTools()

data class SomeInnerDataClassGyroscopeSensor(val x: Float, val y: Float, val z: Float)