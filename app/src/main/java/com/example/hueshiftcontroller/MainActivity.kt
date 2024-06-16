package com.example.hueshiftcontroller

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import java.net.InetAddress


class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var buttonSelector: MaterialButtonToggleGroup
    private val discoveryHandler = DiscoveryHandler()
    private val buttonModeSelector = ButtonModeSelector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.hueshiftIP)

        buttonSelector = findViewById(R.id.selectionToggleGroup)
        buttonModeSelector.setupSelections(this, buttonSelector)

        // Start the UDP discovery sender and answer receiver
        discoveryHandler.onDeviceChanged = { address -> updateHueshiftIP(address) }
        discoveryHandler.startDiscovery()
    }

    private fun updateHueshiftIP(address: InetAddress) {
        textView.text = address.hostAddress
    }
}