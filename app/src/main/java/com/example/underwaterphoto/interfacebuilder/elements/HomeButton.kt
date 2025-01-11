package com.example.underwaterphoto.interfacebuilder.elements

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.underwaterphoto.EasyCamera

import com.example.underwaterphoto.R

class HomeButton @JvmOverloads constructor(context: Context,
                  attrs: AttributeSet? = null,
                  defStyleAttr: Int = 0) : ColoredButton(context, attrs, defStyleAttr) {

    override fun getElementName() : String {
        return HomeButton.elementName
    }

    companion object{

        fun createButton(context: Context, easyCamera: EasyCamera): HomeButton {
            val homeButton: HomeButton = LayoutInflater.from(context).inflate(R.layout.home_button, null) as HomeButton

            homeButton.easyCamera = easyCamera
            homeButton.id = View.generateViewId()

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID

                topMargin = 0
                marginStart = 0
            }

            homeButton.layoutParams = layoutParams

            return homeButton
        }


        const val elementName = "home_button"


    }
}