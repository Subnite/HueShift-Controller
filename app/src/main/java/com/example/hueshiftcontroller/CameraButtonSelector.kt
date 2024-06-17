package com.example.hueshiftcontroller

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.button.MaterialButton

class CameraButtonSelector(llGrid: LinearLayout, context: Context) {
    private var ll_vert: LinearLayout
    private var ctx: Context
    private val dbgTag = "HueShift.CameraButtons"

    var onButtonClicked: ((column: Int, row: Int) -> Unit)? = null

    init {
        ll_vert = llGrid
        ctx = context
        // createButtonGrid(5, 2)
        Log.d(dbgTag, "CameraButtonSelector class initialized")
    }

    // starts from 1, returns row, column
    private fun getGridInt(gridTag: String): Pair<Int, Int> {
        val row = gridTag.substringBefore('_').toInt()
        val col = gridTag.substringAfter('_').toInt()

        return Pair(row, col)
    }

    private var onClickListener = View.OnClickListener() {
//        Toast.makeText(ctx, ""+it.tag, Toast.LENGTH_SHORT).show()
        val coord = getGridInt(it.tag.toString())
        onButtonClicked?.invoke(coord.second, coord.first)
        Log.d(dbgTag, "Clicked! $coord")
    }

    fun createButtonGrid(colums: Int, rows: Int) {
        val maxWidth = ll_vert.width
        val maxHeight = ll_vert.height
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
        val btn = MaterialButton(ctx)

        btn.text = " "
        btn.tag = ""+row+"_"+column
        btn.isClickable = true

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
        val row = LinearLayout(ctx)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        row.isClickable = true

        return row
    }
}