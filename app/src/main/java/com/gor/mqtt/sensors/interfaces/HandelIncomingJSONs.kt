package com.gor.mqtt.sensors.interfaces

import com.gor.mqtt.sensors.utils.JsonTools

// интерфейс в котором реализуется обработка команды от сервера
interface HandelIncomingJSONs {
    fun handleIncomingJSON(jsonTools: JsonTools)
}