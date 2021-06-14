package com.gor.mqtt.sensors.sensors

import android.content.Context
import com.gor.mqtt.sensors.InstanceIsNull
import com.gor.mqtt.sensors.interfaces.*
import com.gor.mqtt.sensors.utils.JsonTools
import com.gor.mqtt.sensors.sensors.Sensor as GSensor


class MagneticFieldSensor(
    context: Context,
    private val listenersList: ArrayList<NotifyView> = ArrayList(),
    private val dataListenersList: ArrayList<NotifyViewByData> = ArrayList(),
    private val handleIncomingJSONsList: ArrayList<HandelIncomingJSONs> = ArrayList()

) : SensorCommunication, SensorActions {


    private var sensor: GSensor? = null


    init {
        sensor = GSensor(context, this, "android.sensor.magnetic_field")
    }

    companion object {
        @Volatile
        private var instance: MagneticFieldSensor? = null

        @Synchronized
        fun getInstance(context: Context): MagneticFieldSensor {
            if (instance == null) {
                instance = MagneticFieldSensor(context)
            }
            return instance as MagneticFieldSensor
        }

        @Synchronized
        fun getInstance(): MagneticFieldSensor {
            return if (instance != null) instance as MagneticFieldSensor
            else throw InstanceIsNull("You should call at first getInstance(context: Context)")
        }
    }
    @Synchronized
    override fun notifyDataChange(data: FloatArray) {
        val tmp = "Magnetic Field \n x ${data[0]} \n y ${data[1]} \n z ${data[2]} \n uT"

        listenersList.forEach { it.updateView(tmp) }
        dataListenersList.forEach { it.updateView(data) }

      /*  val tmpString = JsonTools.getJSON(
            MagneticFieldSensorJSON(
                "magnetic_field",
                MagneticFieldSensorDataJSON(data[0], data[1], data[2])
            )
        )*/
       // println(tmpString)
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


data class MagneticFieldSensorJSON(val name: String, val data: MagneticFieldSensorDataJSON) :
    JsonTools()

data class MagneticFieldSensorDataJSON(val x: Float, val y: Float, val z: Float)

//
data class RequestFromServerToMagneticFieldSensorJSON(
    val name: String,
    val data: SomeInnerDataClassMagneticFieldSensor
) : JsonTools()

data class SomeInnerDataClassMagneticFieldSensor(val x: Float, val y: Float, val z: Float)