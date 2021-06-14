package com.gor.mqtt.sensors.mqtt.interfaces

interface ConnectionStatus {
    fun onConnected(value: Boolean)
    fun onDisconnected(value: String)
}