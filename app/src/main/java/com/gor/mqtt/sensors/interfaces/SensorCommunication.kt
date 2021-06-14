package com.gor.mqtt.sensors.interfaces


interface SensorCommunication {
    // интерфейс для получения исходных данных от общей модели сенсора (класс Sensor.kt) в частную модель
    // Sensor.kt класс, который и получает данные от сервиса, хранит в себе теймер для передачи инфомарции
    fun notifyDataChange(data: FloatArray)
}