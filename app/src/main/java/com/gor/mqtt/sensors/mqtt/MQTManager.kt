package com.gor.mqtt.sensors.mqtt

import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionStatus
import com.gor.mqtt.sensors.mqtt.interfaces.HandelIncomingMessage
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MQTManager(
    private val connectionParams: MQTTConnectionParams,
    @NonNull private val connectionStatus: ArrayList<ConnectionStatus>,
    private val handleIncomingMessage: HandelIncomingMessage
) {

    private var client: MqttAndroidClient? =
        MqttAndroidClient(
            connectionParams.context,
            connectionParams.host,
            connectionParams.clientId
        )

    private var uniqueID: String? = null
    private val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    init {

        client?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.w("mqtt", s)
            }

            override fun connectionLost(throwable: Throwable) {
                Log.w("mqtt", throwable.toString())

                timer = Timer()
                timerTask = object : TimerTask() {
                    override fun run() {
                        if (!client!!.isConnected) {
                            // уведомелние всех слушателей о потери соединения
                            connectionStatus.forEach { it.onDisconnected("Disconnected") }
                        }
                    }
                }
                // запускается не сразу, а по таймеру, так как может успеть переподключиться,
                // и будет отправлен неправильный сигнал о том, что нет соединения
                timer?.schedule(timerTask, 2000)
            }

            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Mqtt", "Arrived message ->  ${mqttMessage.toString()}")
                //call JsonTools getObjectFromJSON and parse string to object based on identifier like contains specific name
                Thread { handleIncomingMessage.incomingMessage(mqttMessage.toString()) }.start()
            }
            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
            }
        })
    }

    fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        if (this.connectionParams.username != "" && this.connectionParams.password != "") {
            mqttConnectOptions.userName = this.connectionParams.username
            mqttConnectOptions.password = this.connectionParams.password.toCharArray()
        }
        try {
            val params = this.connectionParams
            client?.connect(
                mqttConnectOptions,
                connectionParams.context,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        val disconnectedBufferOptions = DisconnectedBufferOptions()
                        disconnectedBufferOptions.isBufferEnabled = true
                        disconnectedBufferOptions.bufferSize = 100
                        disconnectedBufferOptions.isPersistBuffer = false
                        disconnectedBufferOptions.isDeleteOldestMessages = false
                        client?.setBufferOpts(disconnectedBufferOptions)
                        subscribe(params.topicIncoming)
                        // уведомление всех слушателей о соединении
                        connectionStatus.forEach { it.onConnected(true) }
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Failed to connect to: " + params.host + exception.toString())
                    }
                })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            client?.disconnect(connectionParams.context, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.w("mqtt", asyncActionToken.toString())
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w("mqtt", asyncActionToken.toString())
                }
            })
        } catch (ex: Exception) {
            System.err.println("Exception disconnect")
            ex.printStackTrace()
        }

        client = null
        uniqueID = null
    }

    // Subscribe to topic
    fun subscribe(topic: String) {
        try {
            client?.subscribe(
                // входящий топик
                connectionParams.topicIncoming,
                0,
                connectionParams.context,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "Subscription!")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscription fail!")
                    }
                })
        } catch (ex: MqttException) {
            System.err.println("Exception subscribing")
            ex.printStackTrace()
        }
    }

    // Unsubscribe the topic
    fun unsubscribe() {
        client?.apply {
            unregisterResources()
            close()
            unsubscribe(
                // исходящий топик
                connectionParams.topicIncoming,
                connectionParams.context,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {}

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Toast.makeText(
                            connectionParams.context,
                            "cant unsubscribe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        connectionStatus.forEach { it.onDisconnected("Unsubscribed from broker") }
    }

    @Synchronized
    fun publish(message: String) {
        try {
            client?.publish(
                // исходящий топик
                this.connectionParams.topicOutgoing,
                message.toByteArray(),
                0,
                false,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.w("Mqtt", "Publish Success!")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.w("Mqtt", "Publish Failed!")
                    }
                })
        } catch (ex: MqttException) {
            System.err.println("Exception publishing")
            ex.printStackTrace()
        }
    }

}

