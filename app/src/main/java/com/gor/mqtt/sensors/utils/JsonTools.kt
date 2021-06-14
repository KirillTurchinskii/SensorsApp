package com.gor.mqtt.sensors.utils

import com.google.gson.Gson
import com.gor.mqtt.sensors.sensors.*

abstract class JsonTools {

    companion object {

        // создание json для передачи на сервер
        @Synchronized
        fun getJSON(value: JsonTools): String {
            val gson = Gson()
            return gson.toJson(value)
        }


        //обработка входящего json
        @Synchronized
        fun getObjectFromJSON(json: String): JsonTools? {
            if (json.contains("light")) {
                val tmp = Gson().fromJson(json, RequestFromServerToLightSensor::class.java)
                LightSensor.getInstance().passActionFromServer(tmp)
            }

            if (json.contains("accelerometer")) {
                val tmp = Gson().fromJson(json, RequestFromServerToAccelerometerSensor::class.java)
                AccelerometerSensor.getInstance().passActionFromServer(tmp)
                         }
            if (json.contains("gyroscope")) {
                val tmp = Gson().fromJson(json, RequestFromServerToGyroscopeSensorJSON::class.java)
                GyroscopeSensor.getInstance().passActionFromServer(tmp)
            }
            if (json.contains("magnetic_field")) {
                val tmp = Gson().fromJson(json, RequestFromServerToMagneticFieldSensorJSON::class.java)
                MagneticFieldSensor.getInstance().passActionFromServer(tmp)
            }

            return null
        }
    }
}

