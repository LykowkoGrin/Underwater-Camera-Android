package com.example.underwaterphoto.interfacebuilder.elements

import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.underwaterphoto.EasyCamera
import com.example.underwaterphoto.R


class IndicatorButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FunctionalButton(context, attrs,defStyleAttr) {

    override var mustBeDeleted: Boolean = false
        get() = super.mustBeDeleted
        set(value) {
            field = value
            easyCamera?.removeVideoStatusListener(videoStatusListener)
        }

    private fun disableIndicator(){
        val backgroundDrawable = background as LayerDrawable
        val innerCircle = backgroundDrawable.getDrawable(1)

        innerCircle.alpha = 0
    }
    private fun enableIndicator(){
        val backgroundDrawable = background as LayerDrawable
        val innerCircle = backgroundDrawable.getDrawable(1)

        innerCircle.alpha = 255
    }

    override fun setupWidgets(dialogView: View) {
    }


    override fun getDialogView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.indicator_menu, null)
    }

    init{
        disableIndicator()
    }

    val videoStatusListener: (isVideoRecording: Boolean) -> Unit = { isVideoRecording ->

        if (isVideoRecording) enableIndicator()
        else disableIndicator()

    }

    override fun getElementName(): String {
        return elementName
    }

    override var easyCamera: EasyCamera? = null
        set(value){
            field = value
            easyCamera?.removeVideoStatusListener(videoStatusListener)
            easyCamera?.addVideoStatusListener(videoStatusListener)
        }

    companion object{

        fun createButton(context: Context, easyCamera: EasyCamera): IndicatorButton {
            val indicatorButton: IndicatorButton = LayoutInflater.from(context).inflate(R.layout.indicator_button, null) as IndicatorButton
            indicatorButton.easyCamera = easyCamera
            indicatorButton.id = View.generateViewId()

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID

                topMargin = 0
                marginStart = 0
            }

            indicatorButton.layoutParams = layoutParams

            return indicatorButton
        }
        const val elementName = "indicator_button"

    }

}