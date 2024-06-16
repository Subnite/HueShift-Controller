package com.example.hueshiftcontroller

// import android.net.wifi.WifiManager
import android.util.Log
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


class DiscoveryHandler {
    private val Tag = "HueShift.DiscoveryHandler"

    private val discoveryUDPPort = 8179 // sends to this port
    private val discoveryReceiveUDPPort = 8180 // receives on this port
    private val discoveryPingMessage = "HS_PING"
    private val gotDiscoveredMessage = "HS_FIND"

    private var discoverySendJob: Job? = null
    private var discoveryReceiveJob: Job? = null
    private var sendSocket: DatagramSocket? = null
    private var receiveSocket: DatagramSocket? = null

    private var hueShiftIP: InetAddress? = null

    // this will be called every time a device responded to the broadcast discovery call
    var onDeviceChanged: ((InetAddress) -> Unit)? = null

    fun startDiscovery() {
        UDPDiscoveryReceiver()
        sendUDPDiscovery()
    }

    private fun sendUDPDiscovery() {
        discoverySendJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                sendSocket = DatagramSocket(discoveryUDPPort)
                sendSocket?.broadcast = true

                val discoveryMessageBytes = discoveryPingMessage.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(
                    discoveryMessageBytes,
                    discoveryMessageBytes.size,
                    InetAddress.getByName(
                        getLocalBroadcastAddress()
                    ),
                    discoveryUDPPort
                )
                // sending it to all devices on the local network

                while (true) {
                    sendSocket?.send(packet)
                    Log.d(Tag, "broadcasting discovery message")
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
        discoveryReceiveJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                receiveSocket = DatagramSocket(discoveryReceiveUDPPort)
                // socket.soTimeout = 10000 // 10 seconds timeout

                val buffer = ByteArray(gotDiscoveredMessage.toByteArray(Charsets.UTF_8).size) // just the size of the expected message, idk if length would work as well
                val packet = DatagramPacket(buffer, buffer.size)

                var foundIP = false
                while (hueShiftIP == null && !foundIP) {
                    Log.i(Tag, "Reading from " + discoveryReceiveUDPPort)
                    receiveSocket?.receive(packet)
                    val receivedData = String(packet.data, 0, packet.length)

                    Log.i(Tag, "Received message: " + receivedData)
                    if (receivedData == gotDiscoveredMessage) {
                        hueShiftIP = packet.address
                        if (hueShiftIP != null) {
                            Log.i(Tag, "Received response succesfully from ${hueShiftIP?.hostAddress}")
                            foundIP = true
                            onDeviceChanged?.invoke(packet.address)
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

    fun getLocalBroadcastAddress() : String {
        var localIP = getLocalIpAddress()
        localIP = localIP?.substringBeforeLast('.').plus(".255")

        Log.d(Tag, "local IP: $localIP")
        return localIP
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