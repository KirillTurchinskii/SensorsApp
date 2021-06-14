package com.gor.mqtt.sensors.sensors

import android.content.Context
import com.gor.mqtt.sensors.InstanceIsNull
import com.gor.mqtt.sensors.interfaces.*
import com.gor.mqtt.sensors.utils.JsonTools

import com.gor.mqtt.sensors.sensors.Sensor as GSensor

// в данном вакете лежат классы моделей сенсоров (частные)
class AccelerometerSensor private constructor(
    context: Context,
    private val listenersList: ArrayList<NotifyView> = ArrayList(),
    private val handleIncomingJSONsList: ArrayList<HandelIncomingJSONs> = ArrayList(),
    private val dataListenersList: ArrayList<NotifyViewByData> = ArrayList(),
) : SensorCommunication, SensorActions {

    // класс (общая модель) сенсора, которая и получает данные от сервиса,
    // хранит в себе теймер для передачи инфомарции
    private var sensor: GSensor? = null

    init {
        sensor = GSensor(context, this, "android.sensor.accelerometer")
    }

    companion object {
        @Volatile
        private var instance: AccelerometerSensor? = null

        @Synchronized
        fun getInstance(context: Context): AccelerometerSensor {
            if (instance == null) {
                instance = AccelerometerSensor(context)
            }
            return instance as AccelerometerSensor
        }

        // есть возможность получить instance не передавая параметры, но нужно быть уверенным, что instance существует,
        // иначе будет исключение InstanceIsNull
        @Synchronized
        fun getInstance(): AccelerometerSensor {
            return if (instance != null) instance as AccelerometerSensor
            else throw InstanceIsNull("You should call at first getInstance(context: Context)")
        }
    }

    // метод получает данные от GSensor с заданным периодом
    @Synchronized
    override fun notifyDataChange(data: FloatArray) {
        val tmp = "Accelerometer \n Gx ${data[0]} \n Gy ${data[1]} \n Gz ${data[2]} \n m/s^2"
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

    @Synchronized
    override fun passActionFromServer(json: JsonTools) {
        handleIncomingJSONsList.forEach { it.handleIncomingJSON(json) }
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

// data classes to parse objects to json
data class AccelerometerSensorJSON(val name: String, val data: AccelerometerSensorDataJSON) :
    JsonTools()

data class AccelerometerSensorDataJSON(val gx: Float, val gy: Float, val gz: Float)

// data classes to parse json to objects
data class RequestFromServerToAccelerometerSensor(
    val name: String,
    val data: SomeInnerDataClassAccelerometerSensor
) : JsonTools()

data class SomeInnerDataClassAccelerometerSensor(val r: Float, val g: Float, val b: Float)