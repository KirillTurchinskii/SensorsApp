package com.gor.mqtt.sensors.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import java.lang.NumberFormatException
import java.lang.ref.WeakReference
import java.math.RoundingMode
import java.util.*


class SensorsService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    companion object {
        var isAccelerometerAvailable = false;
        var isLuxometerAvailable = false;
        var isGyroscopeAvailable = false;
        var isMagneticFieldAvailable = false;
    }

    private val serviceMessage = Messenger(IncomingHandler(this))
    override fun onBind(intent: Intent?): IBinder? {
        return serviceMessage.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        sensorManager = applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val deviceSensors: List<Sensor> = sensorManager!!.getSensorList(Sensor.TYPE_ALL)
        deviceSensors.forEach {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
          //  println(Calendar.getInstance().time)

            if (it.stringType.contains("sensor.accelerometer")) isAccelerometerAvailable = true
            if (it.stringType.contains("sensor.light")) isLuxometerAvailable = true
            if (it.stringType.contains("sensor.gyroscope")) isGyroscopeAvailable = true
            if (it.stringType.contains("sensor.magnetic_field")) isMagneticFieldAvailable = true
        }

        val pi: PendingIntent? = intent?.getParcelableExtra("pendingIntent")
        pi?.send(44)

        return START_REDELIVER_INTENT
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val tmpVal = FloatArray(event?.values!!.size)

        event.values.forEachIndexed { index, floatVal ->
            tmpVal[index] = getRoundedVal(floatVal)
        }

        val tmpIntent = Intent()
        tmpIntent.action = event.sensor?.stringType
        tmpIntent.putExtra("value", tmpVal)
        applicationContext.sendBroadcast(tmpIntent)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        print(sensor?.resolution)
    }


    private fun getRoundedVal(value: Float): Float{

        try {
            val rounded = value.toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
            return  rounded.toFloat()
        }catch (e: NumberFormatException){
            e.printStackTrace()
            //println("VALUE IS: $value")
        }
        return 0f
    }


}


class IncomingHandler(sensorsService: SensorsService) : Handler() {
    private val apiService: WeakReference<SensorsService> = WeakReference(sensorsService)

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            // можно добавить в функционал команды для контроля сервиса
        }
    }
}
