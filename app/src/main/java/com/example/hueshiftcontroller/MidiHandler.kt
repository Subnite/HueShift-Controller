package com.example.hueshiftcontroller

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MidiViewModel : ViewModel() {
    val midiHandler = MidiHandler()

    init {

    }

    override fun onCleared() {
        super.onCleared()

    }
}

class MidiHandler {
    private var ip: InetAddress? = null
    private var port: Int? = null

    private var sendSocket: DatagramSocket? = null

    fun updateAddress(address: InetAddress?, port: Int?) {
        ip = address
        this.port = port
    }

    fun sendData(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connected = sendSocket?.isConnected
                if (sendSocket == null || connected == false) {
                    sendSocket = DatagramSocket(0) // can be random port
                    sendSocket?.broadcast = true
                }

                if (port == null || ip == null){
                    throw IOException("Port or IP was null")
                }

                val discoveryMessageBytes = message.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(
                    discoveryMessageBytes,
                    discoveryMessageBytes.size,
                    InetAddress.getByName(
                        ip?.hostAddress
                    ),
                    port!!
                )

                // sending it to all devices on the local network
                sendSocket?.send(packet)
                Log.d("HueShift.Midi", "broadcasting midi message: $message")
            } catch (e: Exception) {
                Log.e("HueShift.Midi", "Error sending discovery UDP packet: ", e)
            }
        }
    }

    private fun getMidiMessagePrefix(currentType: ToggleType): String {
        when (currentType) {
            ToggleType.SET -> return "s-"
            ToggleType.OCTAVE -> return "o-"
            ToggleType.FREEZE -> return "f-"
        }
    }

    private fun getVoiceIndex(x: Float, y: Float, amtColumns: Int, amtRows: Int): Int {
        val deviceWidth = 1080f // random atm
        val deviceHeight = 2700f // random atm

        return 0 // just testing
    }

    fun composeMessage(currentType: ToggleType, x: Float, y: Float): String {
        var msg = getMidiMessagePrefix(currentType)
        val voiceIndex = getVoiceIndex(x, y, 2, 1).toString()

        val maxDigits = 9
        var zeros = ""
        for (i in 1..maxDigits-voiceIndex.length) {
            zeros += '0'
        }

        msg += zeros.plus(voiceIndex)
        msg += ';'

        return msg
    }
}