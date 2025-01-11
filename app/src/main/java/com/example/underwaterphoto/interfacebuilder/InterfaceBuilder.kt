package com.example.underwaterphoto.interfacebuilder

import com.example.underwaterphoto.EasyCamera

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.underwaterphoto.interfacebuilder.elements.HomeButton
import com.example.underwaterphoto.interfacebuilder.elements.PhotoButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets


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
        val gson = Gson()

        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                val fileInputStream = FileInputStream(file)
                val json = fileInputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

                val type = object : TypeToken<MutableMap<String, MutableMap<String, String>>>() {}.type

                val resultMap: MutableMap<String, MutableMap<String, String>>? = gson.fromJson(json, type)
                return resultMap
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun createElementByName(name : String) : InterfaceElement? {
        val element : InterfaceElement? = when(name){
            PhotoButton.elementName -> PhotoButton.createButton(context,easyCamera)
            HomeButton.elementName -> HomeButton.createButton(context,easyCamera)
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

    fun saveData(){
        val gson = Gson()
        val resultMap = mutableMapOf<String, MutableMap<String, String>>()

        elements.forEachIndexed { index, element ->
            val elementName = element.getElementName()
            val elementMap = element.convertToMap()

            val numberedName = "${elementName}#${index + 1}"
            resultMap[numberedName] = elementMap
        }

        val json = gson.toJson(resultMap)

        try {
            val file = File(context.filesDir, FILE_NAME)
            FileOutputStream(file).use { fos ->
                fos.write(json.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object{

        private const val FILE_NAME = "UnderwaterElements.json"

    }

}