package com.example.underwaterphoto

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.underwaterphoto.databinding.ActivityMainBinding
import com.example.underwaterphoto.interfacebuilder.InterfaceBuilder
import com.example.underwaterphoto.interfacebuilder.elements.HomeButton
import com.example.underwaterphoto.interfacebuilder.elements.IndicatorButton
import com.example.underwaterphoto.interfacebuilder.elements.PhotoButton


//typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var easyCamera: EasyCamera
    private lateinit var builder : InterfaceBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        if (allPermissionsGranted()) {
            easyCamera = EasyCamera(viewBinding,this)
        } else {
            requestPermissions()
        }

        builder = InterfaceBuilder(this,easyCamera)
        builder.getElements().forEach { element -> drawElement(element) }
        val buildButton = findViewById<ImageButton>(R.id.build_mode_button)
        val adderButton = findViewById<ImageButton>(R.id.add_element_button)

        adderButton.setOnClickListener { view ->
            showElementMenu(view)
        }

        var inDesignMode = false
        buildButton.setOnClickListener {
            if(!inDesignMode){
                inDesignMode = true
                buildButton.setImageResource(R.drawable.close_builder_mode)
                adderButton.isEnabled = true
                adderButton.visibility = View.VISIBLE

                builder.enableMode()
            }
            else {
                inDesignMode = false
                buildButton.setImageResource(R.drawable.builder_mode)
                adderButton.isEnabled = false
                adderButton.visibility = View.GONE
                builder.disableMode()
                builder.saveData()
            }

        }


    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    override fun onDestroy() {
        super.onDestroy()
        easyCamera.onDestroy()
    }

    private fun allPermissionsGranted() : Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                easyCamera = EasyCamera(viewBinding,this)
            }
        }

    private fun showElementMenu(summoner : View) {
        val popupMenu = PopupMenu(this, summoner)
        popupMenu.menuInflater.inflate(R.menu.add_element_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_round_button -> {
                    builder.addElement(PhotoButton.elementName)?.let { drawElement(it) }
                    true
                }
                R.id.create_left_button -> {
                    builder.addElement(HomeButton.elementName)?.let { drawElement(it) }
                    true
                }
                R.id.create_indicator -> {
                    builder.addElement(IndicatorButton.elementName)?.let { drawElement(it) }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun drawElement(element : View){
        val constraintLayout : ConstraintLayout = findViewById(R.id.constraintLayout)
        constraintLayout.addView(element)
    }

    companion object {
        private const val TAG = "UnwaterCamera"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}