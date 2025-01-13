package com.example.underwaterphoto.interfacebuilder

import com.example.underwaterphoto.EasyCamera

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.underwaterphoto.interfacebuilder.elements.HomeButton
import com.example.underwaterphoto.interfacebuilder.elements.IndicatorButton
import com.example.underwaterphoto.interfacebuilder.elements.PhotoButton
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

import kotlinx.serialization.decodeFromString


class InterfaceBuilder(private val context: Context, private val easyCamera: EasyCamera){

    private val elements = ArrayList<InterfaceElement>()

    init{
        try{
            this.loadData()
        }
        catch (e : Exception){
            elements.clear()
            saveData()
        }
    }

    private var inDesignMode = false

    private fun loadData() {
        val mapData : MutableMap<String, MutableMap<String, String>> = loadJsonData() ?: return

        mapData.forEach { (elementNameIndex, elementMap) ->
            val (name, number) = elementNameIndex.split("#")
            val element = createElementByName(name) ?: return
            element.loadFromMap(elementMap)
            elements.add(element)
        }
        mapData.toString()

    }

    private fun loadJsonData(): MutableMap<String, MutableMap<String, String>>? {

        val file = File(context.filesDir, FILE_NAME)

        if (file.exists()) {
            val json = file.readText(StandardCharsets.UTF_8)

            val resultMap: MutableMap<String, MutableMap<String, String>> = Json.decodeFromString(json)

            return resultMap
        } else {
            Log.d("FileCheck", "File does not exist.")
        }


        Log.d("Result", "Returning null as result.")
        return null
    }


    private fun createElementByName(name : String) : InterfaceElement? {
        val element : InterfaceElement? = when(name){
            PhotoButton.elementName -> PhotoButton.createButton(context,easyCamera)
            HomeButton.elementName -> HomeButton.createButton(context,easyCamera)
            IndicatorButton.elementName -> IndicatorButton.createButton(context,easyCamera)
            else -> null
        }

        return element

    }

    fun enableMode(){
        inDesignMode = true
        for(i in 0 until elements.size){
            elements[i].enableDesignMode()
        }
    }

    fun disableMode(){
        inDesignMode = false
        elements.forEach { element -> element.disableDesignMode() }
        elements.removeAll { it.mustBeDeleted }
    }

    fun addElement(elementName: String) : View? {
        val element = createElementByName(elementName) ?: return null

        if(inDesignMode) element.enableDesignMode()
        elements.add(element)

        return element
    }

    fun getElements() : ArrayList<View>{
        val viewsList = ArrayList<View>()

        elements.forEach { element -> viewsList.add(element) }

        return viewsList
    }

    fun saveData() {
        val resultMap = mutableMapOf<String, MutableMap<String, String>>()

        elements.forEachIndexed { index, element ->
            val elementName = element.getElementName()
            val elementMap = element.convertToMap()

            val numberedName = "${elementName}#${index + 1}"
            resultMap[numberedName] = elementMap
        }

        try {
            val mapSerializer = MapSerializer(
                String.serializer(),
                MapSerializer(String.serializer(), String.serializer())
            )
            val json = Json.encodeToString(mapSerializer, resultMap)

            val file = File(context.filesDir, FILE_NAME)

            file.writeText(json)
        } catch (e: IOException) {
            Log.e("SaveError", "Failed to save file: ${e.message}")
        }
    }

    companion object{

        private const val FILE_NAME = "UnderwaterElements.json"

    }

}