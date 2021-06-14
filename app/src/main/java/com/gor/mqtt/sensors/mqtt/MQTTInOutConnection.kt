package com.gor.mqtt.sensors.mqtt

import androidx.annotation.NonNull
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionStatus
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionCommands
import com.gor.mqtt.sensors.mqtt.interfaces.HandelIncomingMessage
import com.gor.mqtt.sensors.utils.JsonTools

class MQTTInOutConnection private constructor() : HandelIncomingMessage,
    ConnectionCommands {

    private var mqttManager: MQTManager? = null

    @NonNull
    var connectionStatus: ArrayList<ConnectionStatus> = ArrayList()

    companion object {
        private var mqttInOutConnection: MQTTInOutConnection? = null

        val instance: MQTTInOutConnection
            get() {
                if (mqttInOutConnection == null) mqttInOutConnection = MQTTInOutConnection()
                return mqttInOutConnection!!
            }
    }

    fun setNotifyView(@NonNull connectionStatus: ConnectionStatus) {
        this.connectionStatus.add(connectionStatus)
    }

    override fun connect(mqttConnectionParams: MQTTConnectionParams) {
        mqttManager = MQTManager(mqttConnectionParams, this.connectionStatus, this)
        mqttManager?.connect()
    }

    override fun disconnect() {
        mqttManager?.unsubscribe()
        mqttManager?.disconnect()
    }

    // обработка сообщений от сервера: передача на JsonTools,
    // который в свою очередь вызовет необходимые методы
    override fun incomingMessage(message: String) {
        Thread {
            JsonTools.getObjectFromJSON(message)
        }.start()
    }

    // отправка сообщений на сервер
    fun sendMessage(message: String) {
        Thread { mqttManager?.publish(message) }.start()
    }
}
