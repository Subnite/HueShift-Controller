package com.example.hueshiftcontroller

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val discoveryHandler = DiscoveryHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.hueshiftIP)

        // Start the UDP discovery sender and answer receiver
        discoveryHandler.onDeviceChanged = { address -> updateHueshiftIP(address) }
        discoveryHandler.startDiscovery()
    }

    private fun updateHueshiftIP(address: InetAddress) {
        textView.text = address.hostAddress
    }
}