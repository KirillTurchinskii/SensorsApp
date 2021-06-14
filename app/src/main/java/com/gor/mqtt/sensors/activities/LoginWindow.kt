package com.gor.mqtt.sensors.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.gor.mqtt.sensors.R
import com.gor.mqtt.sensors.mqtt.interfaces.ConnectionStatus
import com.gor.mqtt.sensors.mqtt.MQTTInOutConnection
import com.gor.mqtt.sensors.mqtt.getMQTTConnectionParams
import kotlinx.android.synthetic.main.login_layout.*
import java.util.*

import kotlin.concurrent.schedule

class LoginWindow : Activity() {

    private var mqttInOutConnection: MQTTInOutConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        mqttInOutConnection = MQTTInOutConnection.instance
    }

    var isConnectionDone: Boolean = false
    override fun onStart() {
        super.onStart()
        if (mqttInOutConnection != null) {
            // добавляем listener для получения результата соединения
            mqttInOutConnection!!.setNotifyView(object : ConnectionStatus {
                override fun onConnected(value: Boolean) {
                    if (value && !isConnectionDone) {
                        openSensorMenuAct()
                        isConnectionDone = true
                    }
                    connectionStatusLoginWindows.text = "Connected"
                }

                override fun onDisconnected(value: String) {
                    Toast.makeText(
                        this@LoginWindow,
                        "Disconnected", Toast.LENGTH_LONG
                    ).show()
                    connectionStatusLoginWindows.text = value
                    isConnectionDone = false
                }
            })
        }
    }

    // переход к окну сенсоров, с задержкой в секунду для комфортного восприятия UI
    private fun openSensorMenuAct() {
        Timer().schedule(1000) {
            val myIntent = Intent(this@LoginWindow, SensorMenu::class.java)
            this@LoginWindow.startActivityForResult(myIntent, 48)
        }
    }

    fun connectBtn(view: View) {
        Thread {
            // подключение
            mqttInOutConnection?.connect(
                // получение параметров подключения
                getMQTTConnectionParams(
                    topicIncoming.text.toString(),
                    topicOutgoing.text.toString(),
                    broker.text.toString(),
                    pass.text.toString(),
                    username.text.toString(),
                    clientID.text.toString(),
                    use_ssl_cb.isChecked,
                    this,
                )
            )
        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //отключение от сервера при возвращении на окно входа
        mqttInOutConnection?.disconnect()
    }
}