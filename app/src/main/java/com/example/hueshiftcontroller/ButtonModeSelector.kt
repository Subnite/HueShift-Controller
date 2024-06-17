package com.example.hueshiftcontroller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.material.button.MaterialButtonToggleGroup

enum class ToggleType {
    SET,
    FREEZE,
    OCTAVE
}

class ButtonModeSelector {
    var currentType = ToggleType.SET
        private set

    fun setupSelections(context: Context, buttonSelector: MaterialButtonToggleGroup) {
        buttonSelector.check(R.id.toggleSel)

        buttonSelector.addOnButtonCheckedListener{ _, checkedId, isChecked ->
            if (isChecked) {
                var message = "No configured button selected"
                when (checkedId) {
                    R.id.toggleSel -> {
                        currentType = ToggleType.SET
                        message = "Selection mode activated!"
                    }
                    R.id.toggleFrz -> {
                        currentType = ToggleType.FREEZE
                        message = "Freeze mode activated!"
                    }
                    R.id.toggleOct -> {
                        currentType = ToggleType.OCTAVE
                        message = "Octave / Frequency mode activated!"
                    }
                }

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.d("HueShift.Toggles", "cycled to $currentType")
            }
//            else {
//                currentIndex = -1
//            }
        }
    }
}