package com.gor.mqtt.sensors.interfaces

import android.content.Context
import com.gor.mqtt.sensors.utils.JsonTools




interface SensorActions {
    // изменение частоты опроса сенсора
    fun updateDataReceivingDelay(value: Long)
    // остановка и запуск таймеров
    fun stopTimer()
    fun startTimer()
    //вызывается в момент, когда сервер получается сообщение, перенаправляет его в JsonTools,
    // тот в зависимости от содержания (например magnetic_field) создаёт POJO бъект с полями
    // и перенаправляет его в модель конкретного сенсора
    // сам метод проходит по всем слушателям HandelIncomingJSONs  (лист handleIncomingJSONsList)
    // и вызывает реализации метода handleIncomingJSONs
    fun passActionFromServer(json: JsonTools)
    // добавление listener тестовой информаии от модели
    fun addDataListenerFromSensor(notifyView: NotifyView)
    // добавление listener сырых данных (float []) от модели
    fun addRawDataListenerFromSensor(notifyView: NotifyViewByData)
    // добаление слушателей-обработчиков handleIncomingJSONs
    fun addIncomingDataServerHandler(handleIncomingJSONs: HandelIncomingJSONs)
    // так как сенсоры снглтоны, то всех Handlers должны быть убраны при отключении от сенсора
    fun clearAllHandlersOnDestroy()
    // регистраиця и отмена слушателей broadcast
    fun registerReceiver(context: Context)
    fun unregisterReceiver(context: Context)
}