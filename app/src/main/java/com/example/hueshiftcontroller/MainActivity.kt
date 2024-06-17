package com.example.hueshiftcontroller

import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
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
    private lateinit var cameraButtons: CameraButtonSelector
    private val buttonModeSelector = ButtonModeSelector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // VIEW COMPONENTS SETUP
        ipAddressView = findViewById(R.id.hueshiftIP)
        midiPortView = findViewById(R.id.hueshiftMidiPort)
        buttonSelector = findViewById(R.id.selectionToggleGroup)


        // MODE SELECTOR SETUP
        buttonModeSelector.setupSelections(this, buttonSelector)


        // DISCOVERY SETUP
        // Change the function used when the hueshift device was found or changed
        discoveryVM.discovery.onDeviceChanged = { address, midiPort -> updateHueshiftIP(address, midiPort) }


        // CAMERA BUTTONS SETUP
        cameraButtons = CameraButtonSelector(findViewById<LinearLayout>(R.id.ll_vert), this)
        // assign on clicked before making the grid!!!
        cameraButtons.onButtonClicked = { column, row ->
            val msg = midiVM.midiHandler.composeMessage(buttonModeSelector.currentType, column, row)
            midiVM.midiHandler.sendData(msg)
        }
        cameraButtons.createButtonGrid(5,2)


        // Update IP to show most recent data at the start
        updateHueshiftIP(discoveryVM.discovery.hueShiftIP, discoveryVM.discovery.hueShiftPort)
    }

    private fun updateHueshiftIP(address: InetAddress?, midiPort: Int) {
        ipAddressView.text = address?.hostAddress
        midiPortView.text = midiPort.toString()

        midiVM.midiHandler.updateAddress(address, midiPort)
    }
}