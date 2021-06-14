package com.gor.mqtt.sensors.mqtt

import android.content.Context


// метод возвращает MQTTConnectionParams. Настроен на соединение с ibmcloud если оставить поля login пустыми
//
fun getMQTTConnectionParams(
    topicIncoming: String,
    topicOutgoing: String,
    broker: String,
    pass: String,
    username: String,
    clientID: String,
    isSSLChecked: Boolean,
    context: Context
): MQTTConnectionParams {

    val tmpBroker = broker.trim()
    var host = if (isSSLChecked) "ssl://${tmpBroker}:8883" else "tcp://${tmpBroker}:1883"

    if (tmpBroker == "") {
        val ibmCloudBr = "aa3s6i.messaging.internetofthings.ibmcloud.com"
        host = if (isSSLChecked) "ssl://$ibmCloudBr:8883" else "tcp://${ibmCloudBr}:1883"
        val clientId = "d:aa3s6i:Android:12"
        val usernameIBM = "use-token-auth"
        val password = "secret123"
        //топик исходящих из приложения сообщений
        val topicIn= "iot-2/cmd/color/fmt/json"
        //топик входящих от сервера сообщений
        val topicOut = "iot-2/evt/accel/fmt/json"
        return MQTTConnectionParams(clientId, host, topicIn, topicOut, usernameIBM, password, context)
    }

    return MQTTConnectionParams(
        clientID.trim(),
        host,
        topicIncoming.trim(),
        topicOutgoing.trim(),
        username.trim(),
        pass.trim(),
        context
    )
}