package com.gor.mqtt.sensors.activities.sensors

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import com.gor.mqtt.sensors.R
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionStatus
import com.gor.mqtt.sensors.interfaces.HandelIncomingJSONs
import com.gor.mqtt.sensors.interfaces.NotifyView
import com.gor.mqtt.sensors.interfaces.NotifyViewByData
import com.gor.mqtt.sensors.mqtt.MQTTInOutConnection
import com.gor.mqtt.sensors.sensors.*
import com.gor.mqtt.sensors.utils.JsonTools
import kotlinx.android.synthetic.main.accelerometer.*


//в пакете activities.sensors  находятся классы Activity для сенсоров
class Accelerometer : Activity() {

    private lateinit var mqttInOutConnection: MQTTInOutConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accelerometer)
        mqttInOutConnection = MQTTInOutConnection.instance
    }

    var accelerometerInstance: AccelerometerSensor? = null
    override fun onStart() {
        super.onStart()

        // на старте получаем instance объекта сенсора, после чего подключаем обработчики для разных listeners
        accelerometerInstance = AccelerometerSensor.getInstance()

        // получает текстовое представление данных от сенсора
        accelerometerInstance?.addDataListenerFromSensor(object : NotifyView {
            override fun updateView(value: String) {
                runOnUiThread {
                    image_view_accelerometer_text_field.text = value
                }
            }
        })

        // реализация обработки входящей команды от сервера
        accelerometerInstance?.addIncomingDataServerHandler(object : HandelIncomingJSONs {
            override fun handleIncomingJSON(jsonTools: JsonTools) {
                val tmpObj = jsonTools as RequestFromServerToAccelerometerSensor
                val tmpColor = Color.rgb(tmpObj.data.r, tmpObj.data.g, tmpObj.data.b)
                runOnUiThread { accelerometer_layout_ID.setBackgroundColor(tmpColor) }
            }
        })

        // получает исходное представление данных от сенсора (массив)
        accelerometerInstance?.addRawDataListenerFromSensor(object : NotifyViewByData {
            override fun updateView(value: FloatArray) {
                val tmpString = JsonTools.getJSON(
                    AccelerometerSensorJSON(
                        "accelerometer",
                        AccelerometerSensorDataJSON(value[0], value[1], value[2])
                    )
                )
                MQTTInOutConnection.instance.sendMessage(tmpString)
              //  println("accelerometer->    $tmpString")
            }
        })

        mqttInOutConnection.setNotifyView(object : ConnectionStatus {
            override fun onConnected(value: Boolean) {
                if (value) connectionStatusAccelerometer.text = "Connected"
            }

            override fun onDisconnected(value: String) {
                connectionStatusAccelerometer.text = "Disconnected"
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometerInstance?.clearAllHandlersOnDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}