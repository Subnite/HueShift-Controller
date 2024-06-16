package com.example.hueshiftcontroller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.material.button.MaterialButtonToggleGroup

class ButtonModeSelector {
    var currentIndex = 0
        private set

    fun setupSelections(context: Context, buttonSelector: MaterialButtonToggleGroup) {
        buttonSelector.check(R.id.toggleSel)

        buttonSelector.addOnButtonCheckedListener{ _, checkedId, isChecked ->
            if (isChecked) {
                var message = "No configured button selected"
                when (checkedId) {
                    R.id.toggleSel -> {
                        currentIndex = 0
                        message = "Selection mode activated!"
                    }
                    R.id.toggleFrz -> {
                        currentIndex = 1
                        message = "Freeze mode activated!"
                    }
                    R.id.toggleOct -> {
                        currentIndex = 2
                        message = "Octave / Frequency mode activated!"
                    }
                }

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.d("HueShift.Toggles", "cycled to $currentIndex")
            }
//            else {
//                currentIndex = -1
//            }
        }
    }
}