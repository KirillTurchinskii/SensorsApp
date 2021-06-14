package com.gor.mqtt.sensors.activities.sensors

import android.app.Activity
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
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
import kotlinx.android.synthetic.main.luxometer.*

class Luxometer : Activity() {
    private lateinit var mqttInOutConnection: MQTTInOutConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.luxometer)
        mqttInOutConnection = MQTTInOutConnection.instance
    }

    var luxometerInstance: LightSensor? = null
    override fun onStart() {
        super.onStart()

        luxometerInstance = LightSensor.getInstance()
        luxometerInstance?.addDataListenerFromSensor(object : NotifyView {
            override fun updateView(value: String) {
                runOnUiThread {
                    luxTextField.text = value
                }
            }
        })

        luxometerInstance?.addIncomingDataServerHandler(object : HandelIncomingJSONs {
            override fun handleIncomingJSON(jsonTools: JsonTools) {
                val tmpObj = jsonTools as RequestFromServerToLightSensor
                //   val tmpColor = Color.parseColor(tmpObj.data.color)
                val tmpColor = Color.rgb(tmpObj.data.r, tmpObj.data.g, tmpObj.data.b)
                // lightIndicatorBar.setBackgroundColor(tmpColor)
                runOnUiThread {
                    lightIndicatorBar.progressDrawable.colorFilter =
                        BlendModeColorFilter(tmpColor, BlendMode.SRC_IN);
                }
            }
        })

        var lastMax = 1
        lightIndicatorBar.scaleY = 10f
        luxometerInstance?.addRawDataListenerFromSensor(object : NotifyViewByData {
            override fun updateView(value: FloatArray) {

                // адаптивная шкала люксометра
                val tmpVal = value[0].toInt()
                if (tmpVal > lastMax) lastMax = tmpVal
                val tmpRes = tmpVal * 100 / lastMax
                lightIndicatorBar.progress = tmpRes
                //

                // отправка данных на сервер
                val tmpString = JsonTools.getJSON(
                    LightSensorJSON(
                        "light",
                        LightSensorDataJSON(value[0])
                    )
                )
                MQTTInOutConnection.instance.sendMessage(tmpString)
                println("light->    $tmpString")
            }
        })

        mqttInOutConnection.setNotifyView(object : ConnectionStatus {
            override fun onConnected(value: Boolean) {
                if (value) connectionStatusLuxometer.text = "Connected"
            }

            override fun onDisconnected(value: String) {
                connectionStatusLuxometer.text = "Disconnected"
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        luxometerInstance?.clearAllHandlersOnDestroy()
    }
}