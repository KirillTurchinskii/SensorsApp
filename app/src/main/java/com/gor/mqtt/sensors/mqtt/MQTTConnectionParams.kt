package com.gor.mqtt.sensors.mqtt

import android.content.Context


data class MQTTConnectionParams(
    var clientId: String,
    var host: String,
    var topicIncoming: String,
    var topicOutgoing: String,
    val username: String,
    val password: String,
    val context: Context
)
