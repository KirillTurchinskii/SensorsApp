package com.gor.mqtt.sensors.interfaces

// интерфейсы для передачи информации от модели сенсора к view
interface NotifyView {
    fun updateView(value: String)
}

interface NotifyViewByData{
    fun updateView(value: FloatArray)
}