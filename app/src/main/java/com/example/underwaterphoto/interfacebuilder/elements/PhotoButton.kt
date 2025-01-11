package com.example.underwaterphoto.interfacebuilder.elements

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.underwaterphoto.EasyCamera

import com.example.underwaterphoto.R

class PhotoButton @JvmOverloads constructor(context: Context,
                  attrs: AttributeSet? = null,
                  defStyleAttr: Int = 0) : ColoredButton(context, attrs, defStyleAttr) {

    override fun getElementName() : String {
        return PhotoButton.elementName
    }

    companion object{

        fun createButton(context: Context, easyCamera: EasyCamera): PhotoButton {
            val photoButton: PhotoButton = LayoutInflater.from(context).inflate(R.layout.round_button, null) as PhotoButton
            photoButton.easyCamera = easyCamera
            photoButton.id = View.generateViewId()

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID

                topMargin = 0
                marginStart = 0
            }

            photoButton.layoutParams = layoutParams

            return photoButton
        }
        const val elementName = "photo_button"

    }
}