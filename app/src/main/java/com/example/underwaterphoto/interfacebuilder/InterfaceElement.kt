package com.example.underwaterphoto.interfacebuilder

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

abstract class InterfaceElement(context: Context,
                                attrs: AttributeSet? = null,
                                defStyleAttr: Int = 0) : AppCompatButton(context, attrs, defStyleAttr) {

    abstract fun convertToMap() : MutableMap<String, String>
    abstract fun loadFromMap(map : MutableMap<String, String>)
    abstract fun getElementName() : String

    abstract fun enableDesignMode()
    abstract fun disableDesignMode()

    abstract var mustBeDeleted: Boolean
    protected set
}