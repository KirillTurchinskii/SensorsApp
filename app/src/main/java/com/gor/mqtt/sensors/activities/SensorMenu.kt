package com.gor.mqtt.sensors.activities

import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.gor.mqtt.sensors.R
import com.gor.mqtt.sensors.activities.sensors.Accelerometer
import com.gor.mqtt.sensors.activities.sensors.Gyroscope
import com.gor.mqtt.sensors.activities.sensors.Luxometer
import com.gor.mqtt.sensors.activities.sensors.MagneticField
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionStatus
import com.gor.mqtt.sensors.interfaces.SensorActions
import com.gor.mqtt.sensors.mqtt.MQTTInOutConnection
import com.gor.mqtt.sensors.sensors.AccelerometerSensor
import com.gor.mqtt.sensors.sensors.GyroscopeSensor
import com.gor.mqtt.sensors.sensors.LightSensor
import com.gor.mqtt.sensors.sensors.MagneticFieldSensor
import com.gor.mqtt.sensors.services.SensorsService
import kotlinx.android.synthetic.main.sensors_menu.*
import kotlin.math.roundToInt


class SensorMenu : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensors_menu)

        // запуск сервиса, который будет получать и транслировать данные со всех сенсоров устройства
        if (!isServiceStarted) {
            val tmpPendingIntent = createPendingResult(777, Intent(), 0)
            val mIntentQt = Intent(this, SensorsService::class.java)
            // pendingIntent для получения ответа статуса от сервиса
            mIntentQt.putExtra("pendingIntent", tmpPendingIntent)
            this.startService(mIntentQt)
            isServiceStarted = true
        } else onActivityResult(1, 44, null)
    }

    companion object {
        val sensors: ArrayList<SensorActions> = ArrayList()
        var isServiceStarted = false
    }

    override fun onStart() {
        super.onStart()

        if (sensors.isEmpty()) {
            sensors.add(LightSensor.getInstance(this))
            sensors.add(AccelerometerSensor.getInstance(this))
            sensors.add(GyroscopeSensor.getInstance(this))
            sensors.add(MagneticFieldSensor.getInstance(this))
        }

        // регситарция receiver для реализованнх сенсоров, запус приёма данных на уровне модели сенсора,
        // для получения самих данных в Activity каждого сенсора имеются listener
        sensors.forEach {
            it.registerReceiver(this)
            it.startTimer()
        }

        val mqttInOutConnection = MQTTInOutConnection.instance
        connectionStatusSensorsMenu.text = "Connected"
        mqttInOutConnection.setNotifyView(object : ConnectionStatus {
            override fun onConnected(value: Boolean) {
                if (value) connectionStatusSensorsMenu.text = "Connected"
            }

            override fun onDisconnected(value: String) {
                connectionStatusSensorsMenu.text = value
            }
        })
    }

    // когда acitvity уничтожается опязательно нужно отписаться от всех broadcast receiver,
    // которые были зарегистрированы на урове acitvity
    override fun onDestroy() {
        try {
            sensors.forEach {
                it.unregisterReceiver(this)
                it.stopTimer() // остановка таймеров (нужа была в реализации, когда датчики отправляют данные и из основного меню)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        // остановка сервиса
        stopService(Intent(this, SensorsService::class.java))
        super.onDestroy()
    }


    /// на ответ от сервиса обрабатываем статус доступных сенсоров, меняем их фон/окрас
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == 44) {

            if (!SensorsService.isAccelerometerAvailable)
                setImageDrawable(imageViewAccelerometer, R.drawable.ic_coordinates)

            if (!SensorsService.isGyroscopeAvailable)
                setImageDrawable(imageViewGyroscope, R.drawable.ic_gyroscope)

            if (!SensorsService.isMagneticFieldAvailable)
                setImageDrawable(imageViewMagneticField, R.drawable.ic_magnetic_field)

            if (!SensorsService.isLuxometerAvailable)
                setImageDrawable(imageViewLight, R.drawable.ic_light)
        }
    }

    private val alphaFactor = 1.5f
    private val tmpColorFilter = BlendModeColorFilter(
        adjustAlpha(R.color.lite_gray, alphaFactor),
        BlendMode.SRC_ATOP
    )

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun setImageDrawable(view: ImageView, id: Int) {
        val drawable = ContextCompat.getDrawable(this, id)
        drawable?.colorFilter = tmpColorFilter
        view.setImageDrawable(drawable)
    }
    ///

    fun openLuxometer(view: View) {
        if (SensorsService.isLuxometerAvailable) {
            val myIntent = Intent(this, Luxometer::class.java)
            this.startActivity(myIntent)
        }
    }

    fun openMagneticField(view: View) {
        if (SensorsService.isMagneticFieldAvailable) {
            val myIntent = Intent(this, MagneticField::class.java)
            this.startActivity(myIntent)
        }
    }

    fun openGyroscope(view: View) {
        if (SensorsService.isGyroscopeAvailable) {
            val myIntent = Intent(this, Gyroscope::class.java)
            this.startActivity(myIntent)
        }
    }

    fun openAccelerometer(view: View) {
        if (SensorsService.isAccelerometerAvailable) {
            val myIntent = Intent(this, Accelerometer::class.java)
            this.startActivity(myIntent)
        }
    }
}