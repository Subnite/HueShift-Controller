package com.example.hueshiftcontroller

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import java.net.InetAddress
import androidx.activity.viewModels


class MainActivity : AppCompatActivity() {
    private val discoveryVM: DiscoveryViewModel by viewModels()
    private val midiVM: MidiViewModel by viewModels()

    private lateinit var ipAddressView: TextView
    private lateinit var midiPortView: TextView
    private lateinit var buttonSelector: MaterialButtonToggleGroup
    private val buttonModeSelector = ButtonModeSelector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ipAddressView = findViewById(R.id.hueshiftIP)
        midiPortView = findViewById(R.id.hueshiftMidiPort)

        buttonSelector = findViewById(R.id.selectionToggleGroup)
        buttonModeSelector.setupSelections(this, buttonSelector)

        // Change the function used when the hueshift device was found or changed
        discoveryVM.discovery.onDeviceChanged = { address, midiPort -> updateHueshiftIP(address, midiPort) }

        val camButton: Button = findViewById(R.id.cameraData)
        camButton.isClickable = true
        camButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    // Call the function when the button is released
                    var msg = midiVM.midiHandler.composeMessage(buttonModeSelector.currentType, event.x, event.y)
                    midiVM.midiHandler.sendData(msg)
                    true
                }
                else -> false
            }
        }

        updateHueshiftIP(discoveryVM.discovery.hueShiftIP, discoveryVM.discovery.hueShiftPort)
    }

    private fun updateHueshiftIP(address: InetAddress?, midiPort: Int) {
        ipAddressView.text = address?.hostAddress
        midiPortView.text = midiPort.toString()

        midiVM.midiHandler.updateAddress(address, midiPort)
    }
}