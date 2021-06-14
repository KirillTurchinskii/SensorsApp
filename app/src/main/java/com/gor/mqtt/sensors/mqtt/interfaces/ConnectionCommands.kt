package com.gor.mqtt.sensors.mqtt.interfaces

import com.gor.mqtt.sensors.mqtt.MQTTConnectionParams


interface ConnectionCommands {
    fun connect(  mqttConnectionParams: MQTTConnectionParams)
    fun disconnect()
}