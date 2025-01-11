package com.example.underwaterphoto.interfacebuilder.elements

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog

import com.example.underwaterphoto.EasyCamera
import com.example.underwaterphoto.R
import com.example.underwaterphoto.interfacebuilder.InterfaceElement


open class FunctionalButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : InterfaceElement(context, attrs, defStyleAttr) {

    private var clickHandlerAction : HandlerActions? = null //press + release
    private var holdHandlerAction : HandlerActions? = null //press + timeout
    private var pressHandlerAction : HandlerActions? = null
    private var releaseHandlerAction : HandlerActions? = null

    private var lastClickTime = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private var doubleTapHandler: (() -> Unit)? = null

    private var isHeld : Boolean = false
    private var isPressed : Boolean = false

    var easyCamera: EasyCamera? = null

    override var mustBeDeleted: Boolean = false

    override fun convertToMap(): MutableMap<String, String> {

        val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams
        val marginLeft = layoutParams?.leftMargin ?: 0
        val marginTop = layoutParams?.topMargin ?: 0

        val paramsMap = mutableMapOf<String, String>()

        paramsMap.put("left_margin", marginLeft.toString())
        paramsMap.put("top_margin", marginTop.toString())

        paramsMap.put("click_action", clickHandlerAction?.name?: "")
        paramsMap.put("hold_action", holdHandlerAction?.name ?: "")
        paramsMap.put("press_action", pressHandlerAction?.name ?: "")
        paramsMap.put("release_action", releaseHandlerAction?.name ?: "")


        return paramsMap
    }

    override fun loadFromMap(map: MutableMap<String, String>) {

        clickHandlerAction = runCatching { HandlerActions.valueOf(map["click_action"] ?: "") }.getOrNull()
        holdHandlerAction = runCatching { HandlerActions.valueOf(map["hold_action"] ?: "") }.getOrNull()
        pressHandlerAction = runCatching { HandlerActions.valueOf(map["press_action"] ?: "") }.getOrNull()
        releaseHandlerAction = runCatching { HandlerActions.valueOf(map["release_action"] ?: "") }.getOrNull()

        val lp = layoutParams as ViewGroup.MarginLayoutParams
        lp.leftMargin = map["left_margin"]?.toInt() ?: 0
        lp.topMargin = map["top_margin"]?.toInt() ?: 0
        layoutParams = lp
    }

    override fun getElementName(): String {
        return elementName
    }

    override fun enableDesignMode() {
        setupDesignModeListeners()
    }

    override fun disableDesignMode() {
        setupListeners()
    }

    private fun doAction(action: HandlerActions){
        when(action){
            HandlerActions.START_VIDEO -> easyCamera?.startVideo()
            HandlerActions.STOP_VIDEO -> easyCamera?.stopVideo()
            HandlerActions.TAKE_PHOTO -> easyCamera?.takePhoto()
            HandlerActions.CLOSE_APP -> (context as? Activity)?.finishAffinity()
        }
    }

    private fun setupListeners(){
        clearListeners()

        setOnClickListener {
            if(!isHeld && clickHandlerAction != null) this.doAction(clickHandlerAction!!)
        }

        setOnLongClickListener {
            if(holdHandlerAction != null) this.doAction(holdHandlerAction!!)
            isHeld = true
            true
        }

        setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    if(!isPressed && pressHandlerAction != null) this.doAction(pressHandlerAction!!)
                    isPressed = true
                }

                MotionEvent.ACTION_UP -> {
                    if(isPressed) {
                        if(releaseHandlerAction != null) this.doAction(releaseHandlerAction!!)
                    }
                    isPressed = false
                }

            }
            false
        }
    }

    private fun setupDesignModeListeners() {
        clearListeners()

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX
                    lastY = event.rawY
                    isDragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY
                    if (dx * dx + dy * dy > 100) {
                        isDragging = true
                    }
                    if (isDragging) {
                        val parent = parent as? ViewGroup
                        parent?.let {
                            val lp = layoutParams as ViewGroup.MarginLayoutParams
                            lp.leftMargin += dx.toInt()
                            lp.topMargin += dy.toInt()
                            layoutParams = lp
                        }
                        lastX = event.rawX
                        lastY = event.rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        performClick()
                    }
                    isDragging = false
                }
            }
            true
        }

        setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                doubleTapHandler?.invoke()
            }
            lastClickTime = clickTime
        }

        setOnDoubleTapListener{
            showButtonSettings()
        }
    }

    private fun showButtonSettings(){
        val dialogView = getDialogView()
        setupWidgets(dialogView)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val applyButton : Button = dialogView.findViewById(R.id.apply_button)
        val cancelButton : Button = dialogView.findViewById(R.id.cancel_button)
        val deleteButton : ImageButton = dialogView.findViewById(R.id.trash_button)

        applyButton.setOnClickListener {
            onApplyButtonClick(dialogView)
            dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        deleteButton.setOnClickListener {
            val parentView = parent as? ViewGroup
            parentView?.removeView(this)

            mustBeDeleted = true

            dialog.dismiss()
        }

        dialog.show()
    }

    protected open fun getDialogView() : View {
        return LayoutInflater.from(context).inflate(R.layout.button_menu, null)
    }
    protected open fun setupWidgets(dialogView : View){
        val spinner1: Spinner = dialogView.findViewById(R.id.click_action)
        val spinner2: Spinner = dialogView.findViewById(R.id.hold_action)
        val spinner3: Spinner = dialogView.findViewById(R.id.press_action)
        val spinner4: Spinner = dialogView.findViewById(R.id.release_action)

        val adapter = ArrayAdapter.createFromResource(
            context,
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner1.adapter = adapter
        spinner2.adapter = adapter
        spinner3.adapter = adapter
        spinner4.adapter = adapter

        spinner1.setSelection((clickHandlerAction?.ordinal ?: -1) + 1)
        spinner2.setSelection((holdHandlerAction?.ordinal ?: -1) + 1)
        spinner3.setSelection((pressHandlerAction?.ordinal ?: -1) + 1)
        spinner4.setSelection((releaseHandlerAction?.ordinal ?: -1) + 1)
    }
    protected open fun onApplyButtonClick(dialog: View){
        val spinner1: Spinner = dialog.findViewById(R.id.click_action)
        val spinner2: Spinner = dialog.findViewById(R.id.hold_action)
        val spinner3: Spinner = dialog.findViewById(R.id.press_action)
        val spinner4: Spinner = dialog.findViewById(R.id.release_action)

        val actionList = HandlerActions.entries.toList()
        var selectedPosition = spinner1.selectedItemPosition
        clickHandlerAction = if(selectedPosition > 0) actionList[selectedPosition - 1] else null
        selectedPosition = spinner2.selectedItemPosition
        holdHandlerAction = if(selectedPosition > 0) actionList[selectedPosition - 1] else null
        selectedPosition = spinner3.selectedItemPosition
        pressHandlerAction = if(selectedPosition > 0) actionList[selectedPosition - 1] else null
        selectedPosition = spinner4.selectedItemPosition
        releaseHandlerAction = if(selectedPosition > 0) actionList[selectedPosition - 1] else null
    }

    
    private fun clearListeners(){
        setOnClickListener(null)
        setOnTouchListener(null)
        setOnLongClickListener(null)
        setOnDoubleTapListener(null)
    }

    init{
        setupListeners()
    }

    fun setOnDoubleTapListener(handler: (() -> Unit)?) {
        doubleTapHandler = handler
    }

    companion object{

        enum class HandlerActions{
            START_VIDEO,
            STOP_VIDEO,
            TAKE_PHOTO,
            CLOSE_APP
        }
        const val elementName = "functional_button"
        private const val DOUBLE_CLICK_THRESHOLD = 300 // Миллисекунды
    }

}