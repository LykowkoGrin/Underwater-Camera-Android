package com.example.underwaterphoto.interfacebuilder.elements

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.example.underwaterphoto.R

import com.skydoves.colorpickerview.ColorPickerView

open class ColoredButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : FunctionalButton(context, attrs, defStyleAttr)
{



    override fun getElementName() : String {
        return elementName
    }

    override fun convertToMap(): MutableMap<String, String>{
        val paramsMap = super.convertToMap()

        val colorInt = backgroundTintList?.defaultColor
        val colorString = colorInt?.let { String.format("#%06X", 0xFFFFFF and it) }

        paramsMap["color"] = colorString ?: "#FFFFFF"

        return paramsMap
    }

    override fun loadFromMap(map: MutableMap<String, String>) {
        super.loadFromMap(map)

        val colorString = map["color"] ?: "#FFFFFF"
        val colorInt = Color.parseColor(colorString)
        backgroundTintList = ColorStateList.valueOf(colorInt)
    }

    override fun getDialogView(): View? {
        return LayoutInflater.from(context).inflate(R.layout.button_menu_with_color, null)
    }

    override fun onApplyButtonClick(dialog : View){
        super.onApplyButtonClick(dialog)

        val colorPickerView = dialog.findViewById<ColorPickerView>(R.id.color_picker_view)
        val hexColor: String = colorPickerView.colorEnvelope.hexCode
        val validHexColor = if (hexColor.length == 8) "#$hexColor" else "#FFFFFFFF"

        backgroundTintList = ColorStateList.valueOf(Color.parseColor(validHexColor))
    }

    override fun setupWidgets(dialogView: View) {
        super.setupWidgets(dialogView)
        val colorPicker = dialogView.findViewById<ColorPickerView>(R.id.color_picker_view)

        val color = (backgroundTintList?.defaultColor) ?: Color.WHITE

        colorPicker.setInitialColor(color)
    }

    //override fun


    companion object{

        const val elementName = "colored_button"

    }




}