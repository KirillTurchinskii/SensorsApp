package com.gor.mqtt.sensors.activities.sensors

import android.app.Activity
import android.os.Bundle
import com.gor.mqtt.sensors.R
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionStatus
import com.gor.mqtt.sensors.interfaces.HandelIncomingJSONs
import com.gor.mqtt.sensors.interfaces.NotifyView
import com.gor.mqtt.sensors.interfaces.NotifyViewByData
import com.gor.mqtt.sensors.mqtt.MQTTInOutConnection
import com.gor.mqtt.sensors.sensors.GyroscopeSensor
import com.gor.mqtt.sensors.sensors.GyroscopeSensorDataJSON
import com.gor.mqtt.sensors.sensors.GyroscopeSensorJSON
import com.gor.mqtt.sensors.sensors.RequestFromServerToGyroscopeSensorJSON
import com.gor.mqtt.sensors.utils.JsonTools
import kotlinx.android.synthetic.main.gyroscope.*

class Gyroscope : Activity() {

    private lateinit var mqttInOutConnection: MQTTInOutConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gyroscope)
        mqttInOutConnection = MQTTInOutConnection.instance
    }

    var gyroscopeInstance: GyroscopeSensor? = null
    override fun onStart() {
        super.onStart()

        gyroscopeInstance = GyroscopeSensor.getInstance()

        // получает текстовое представление данных от сенсора
        gyroscopeInstance?.addDataListenerFromSensor(object : NotifyView {
            override fun updateView(value: String) {
                runOnUiThread {
                    gyroscope_sensor_text_field.text = value
                }
            }
        })

        // реализация обработки входящей команды от сервера
        gyroscopeInstance?.addIncomingDataServerHandler(object : HandelIncomingJSONs {
            override fun handleIncomingJSON(jsonTools: JsonTools) {
                // реализация обработки входящей команды от сервера
                val tmpData = jsonTools as RequestFromServerToGyroscopeSensorJSON

                val xTmp = tmpData.data.x.toInt() * 10
                val yTmp = tmpData.data.y.toInt() * 10
                val zTmp = tmpData.data.z.toInt() * 10

                runOnUiThread {
                    val_x_pb.progress = if (xTmp in 0..100) xTmp else 0
                    val_y_pb.progress = if (yTmp in 0..100) yTmp else 0
                    val_z_pb.progress = if (zTmp in 0..100) zTmp else 0
                }
            }
        })

        // получает исходное представление данных от сенсора (массив)
        gyroscopeInstance?.addRawDataListenerFromSensor(object : NotifyViewByData {
            override fun updateView(value: FloatArray) {
                val tmpString = JsonTools.getJSON(
                    GyroscopeSensorJSON(
                        "gyroscope",
                        GyroscopeSensorDataJSON(value[0], value[1], value[2])
                    )
                )
                MQTTInOutConnection.instance.sendMessage(tmpString)
                println("gyroscope->    $tmpString")
            }
        })

        mqttInOutConnection.setNotifyView(object : ConnectionStatus {
            override fun onConnected(value: Boolean) {
                if (value) connectionStatusGyroscope.text = "Connected"
            }

            override fun onDisconnected(value: String) {
                connectionStatusGyroscope.text = "Disconnected"
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        gyroscopeInstance?.clearAllHandlersOnDestroy()
    }
}