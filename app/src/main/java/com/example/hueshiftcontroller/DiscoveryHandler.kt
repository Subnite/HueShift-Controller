package com.example.hueshiftcontroller

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException


class DiscoveryViewModel : ViewModel() {
    val discovery = DiscoveryHandler()

    init {
        discovery.startDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
        discovery.stopDiscovery()
    }
}

class DiscoveryHandler {
    private val Tag = "HueShift.DiscoveryHandler"

    private val discoveryUDPPort = 8179 // sends to this port
    private val discoveryReceiveUDPPort = 8180 // receives on this port
    private val discoveryPingMessage = "HS_PING"
    private val gotDiscoveredMessage = "HS_65535" // the last part is the port, can start with a 0 for shorter port num
    var subnetAmount = 1

    private var discoverySendJob: Job? = null
    private var discoveryReceiveJob: Job? = null
    private var sendSocket: DatagramSocket? = null
    private var receiveSocket: DatagramSocket? = null

    var hueShiftIP: InetAddress? = null
        private set
    var hueShiftPort: Int = 0
        private set

    // this will be called every time a device responded to the broadcast discovery call, returns ip and midi port
    var onDeviceChanged: ((InetAddress, Int) -> Unit)? = null

    fun startDiscovery() {
        UDPDiscoveryReceiver()
        sendUDPDiscovery()
    }

    fun stopDiscovery() { // usually done automatically when you find a device
        discoverySendJob?.cancel()
        discoveryReceiveJob?.cancel()
    }

    private fun sendUDPDiscovery() {
        discoverySendJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val connected = sendSocket?.isConnected
                if (sendSocket == null || connected == false) {
                    sendSocket = DatagramSocket(discoveryUDPPort)
                    sendSocket?.broadcast = true
                }

                val subnetAdjustedIP = getLocalBroadcastAddress(subnetAmount)
                val discoveryMessageBytes = discoveryPingMessage.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(
                    discoveryMessageBytes,
                    discoveryMessageBytes.size,
                    InetAddress.getByName(
                        subnetAdjustedIP
                    ),
                    discoveryUDPPort
                )
                // sending it to all devices on the local network

                while (true) {
                    sendSocket?.send(packet)
                    Log.d(Tag, "broadcasting discovery message to $subnetAdjustedIP")
                    delay(5000)
                }
            } catch (e: Exception) {
                Log.e(Tag, "Error sending discovery UDP packet", e)
            }
        }

        discoverySendJob?.invokeOnCompletion { throwable ->
            if (throwable is CancellationException) {
                Log.i(Tag, "Discovery sender job completed")
                // Perform any cleanup here if necessary
                sendSocket?.close()
            }
        }
    }

    private fun UDPDiscoveryReceiver() {
        discoveryReceiveJob?.cancel()
        discoveryReceiveJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val connected = receiveSocket?.isConnected
                if (receiveSocket == null || connected == false){
                    receiveSocket = DatagramSocket(discoveryReceiveUDPPort)
                }

                val buffer = ByteArray(gotDiscoveredMessage.toByteArray(Charsets.UTF_8).size) // just the size of the expected message, idk if length would work as well
                val packet = DatagramPacket(buffer, buffer.size)

                var foundIP = false
                while (hueShiftIP == null && !foundIP) {
                    Log.i(Tag, "Reading from " + discoveryReceiveUDPPort)
                    receiveSocket?.receive(packet)
                    val receivedData = String(packet.data, 0, packet.length)

                    Log.i(Tag, "Received message: " + receivedData)
                    if (receivedData.startsWith(gotDiscoveredMessage.substringBefore('_').plus('_'))) {
                        hueShiftPort = receivedData.substringAfter('_').toInt()
                        hueShiftIP = packet.address
                        if (hueShiftIP != null) {
                            Log.i(Tag, "Received response succesfully from ${hueShiftIP?.hostAddress}, midi port: $hueShiftPort")
                            foundIP = true
                            onDeviceChanged?.invoke(packet.address, hueShiftPort)
                        }
                    }
                }

                discoverySendJob?.cancel()
                discoveryReceiveJob?.cancel()
            } catch (e: Exception) {
                Log.e(Tag, "Error receiving discovery response", e)
            }
        }

        discoveryReceiveJob?.invokeOnCompletion { throwable ->
            if (throwable is CancellationException) {
                Log.i(Tag, "Discovery receiver job completed")
                // Perform any cleanup here if necessary
                receiveSocket?.close()
            }
        }
    }

    // subnet Amount is how many 255's to add to the IP. Returns local addr if not found.
    fun getLocalBroadcastAddress(subnetAmount: Int) : String {
        var localIP = getLocalIpAddress()
        for (i in 1..subnetAmount) {
            localIP = localIP?.substringBeforeLast('.')
        }
        for (i in 1..subnetAmount) {
            localIP = localIP?.plus(".255")
        }

        Log.d(Tag, "local IP: $localIP")
        if (localIP != null) {
            return localIP
        }
        return "127.0.0.1"
    }

    // got from internet https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
    private fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }
}