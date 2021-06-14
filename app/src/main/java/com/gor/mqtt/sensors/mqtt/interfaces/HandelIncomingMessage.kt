package com.gor.mqtt.sensors.mqtt.interfaces

interface HandelIncomingMessage {
    // обработчик; вызывается при входящем сообщении от сервера
    fun incomingMessage(message: String)
}