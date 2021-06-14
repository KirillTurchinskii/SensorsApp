package com.gor.mqtt.sensors.activities.sensors

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.gor.mqtt.sensors.R
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionStatus
import com.gor.mqtt.sensors.interfaces.HandelIncomingJSONs
import com.gor.mqtt.sensors.interfaces.NotifyView
import com.gor.mqtt.sensors.interfaces.NotifyViewByData
import com.gor.mqtt.sensors.mqtt.MQTTInOutConnection
import com.gor.mqtt.sensors.sensors.*
import com.gor.mqtt.sensors.utils.JsonTools
import kotlinx.android.synthetic.main.magnetic_fied.*


class MagneticField : Activity(), SensorEventListener {
    private lateinit var mqttInOutConnection: MQTTInOutConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.magnetic_fied)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager;
        mqttInOutConnection = MQTTInOutConnection.instance
    }

    private var currentDegree: Float = 0f
    private var mSensorManager: SensorManager? = null

    override fun onResume() {
        super.onResume()

        mSensorManager?.registerListener(
            this, mSensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private var magneticFieldInstance: MagneticFieldSensor? = null
    override fun onStart() {
        super.onStart()


        magneticFieldInstance = MagneticFieldSensor.getInstance()
        magneticFieldInstance?.addDataListenerFromSensor(object : NotifyView {
            override fun updateView(value: String) {
                runOnUiThread {
                    magnetic_field_textField.text = value
                }
            }
        })

        magneticFieldInstance?.addRawDataListenerFromSensor(object : NotifyViewByData {
            override fun updateView(value: FloatArray) {
                // анимация стрелки компаса
                runOnUiThread {
                    val degree: Float = value[0]
                   // println(degree)
                    try {
                        val rotationAnim = RotateAnimation(
                            currentDegree,
                            (-degree),
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f
                        )
                        rotationAnim.duration = 210
                        rotationAnim.fillAfter = true
                        currentDegree = -degree
                        magnetic_field_imageView_image_ID.startAnimation(rotationAnim)

                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }

                // отправка данных на сервер
                val tmpString = JsonTools.getJSON(
                    MagneticFieldSensorJSON(
                        "magnetic_field",
                        MagneticFieldSensorDataJSON(value[0], value[1], value[2])
                    )
                )
                MQTTInOutConnection.instance.sendMessage(tmpString)
                println("magnetic_field->    $tmpString")
            }
        })

        // тут обработка ответа от сервера
        magneticFieldInstance?.addIncomingDataServerHandler(object : HandelIncomingJSONs {
            override fun handleIncomingJSON(jsonTools: JsonTools) {
                val tmp = jsonTools as RequestFromServerToMagneticFieldSensorJSON

                /* flicker.colorFilter =
                     BlendModeColorFilter(tmpColor, BlendMode.SRC_IN);

                 flicker.setBackgroundColor(tmpColor)*/
            }
        })


        mqttInOutConnection.setNotifyView(object : ConnectionStatus {
            override fun onConnected(value: Boolean) {
                if (value) connectionStatusMagneticFiled.text = "Connected"
            }

            override fun onDisconnected(value: String) {
                connectionStatusMagneticFiled.text = "Disconnected"
            }
        })
    }

    override fun onDestroy() {
        mSensorManager?.unregisterListener(this)
        magneticFieldInstance?.clearAllHandlersOnDestroy()
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {}
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}