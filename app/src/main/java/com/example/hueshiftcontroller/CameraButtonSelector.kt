package com.example.hueshiftcontroller

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.button.MaterialButton

class CameraButtonSelector : AppCompatActivity() {
    private lateinit var ll_vert: LinearLayout
    private val dbgTag = "HueShift.CameraButtons"

    var onClickListener = View.OnClickListener() {
        Toast.makeText(applicationContext, ""+it.tag, Toast.LENGTH_SHORT).show()

        // var msg = midiVM.midiHandler.composeMessage(buttonModeSelector.currentType, event.x, event.y)
        // midiVM.midiHandler.sendData(msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ll_vert = findViewById<LinearLayout>(R.id.ll_vert)
        Log.d(dbgTag, "CameraButtonSelector class initialized")

        createButtonGrid(2, 3, ll_vert.width, ll_vert.height)
    }

    fun createButtonGrid(colums: Int, rows: Int, maxWidth: Int, maxHeight: Int) {
        for (i in 1..rows) {
            val llRow = createSingleRow()
            // val width = maxWidth / colums
            ll_vert.addView(llRow)
            for(j in 1..colums) {
                val btnNew = createSingleMaterialButton(i, j)
                llRow.addView(btnNew)
                btnNew.setOnClickListener(onClickListener)
            }
        }
        Log.d(dbgTag, "Camera button grid created")
    }

    private fun createSingleMaterialButton(row: Int = 0, column: Int = 0): MaterialButton {
        val btn = MaterialButton(this)

        btn.text = " "
        btn.tag = ""+row+"_"+column

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        params.marginStart = 0
        params.marginEnd = 0
        params.rightMargin = 0
        params.leftMargin = 0

        btn.layoutParams = params

        btn.setStrokeColorResource(R.color.purple_200)
        btn.strokeWidth = 10

        // btn.setPadding(0,0,0,0)
        btn.insetBottom = 0 // real
        btn.insetTop = 0 // real

        btn.cornerRadius = 0

        btn.setBackgroundColor(Color(.2f, .1f, .4f).toArgb())

        return btn
    }

    private fun createSingleRow(): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER

        return row
    }
}