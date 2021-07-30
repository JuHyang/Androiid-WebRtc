package com.practice.webrtc

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.practice.webrtc.databinding.ActivityMainBinding

/*
*
* https://amryousef.me/android-webrtc 참고
*
* */

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    lateinit var binding: ActivityMainBinding

    private lateinit var rtcClient : RTCClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED ) {
            requestCameraPermission()
        } else {
            onCameraPermissionGranted()
        }
    }

    private fun requestCameraPermission(dialogShown : Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) && !dialogShown) {
            showPermissionRationalDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera permission Required")
            .setMessage("This app need the camera to function")
            .setPositiveButton("Grant") { dialog, _  ->
                dialog.dismiss()
                requestCameraPermission(true)
            }
            .setNegativeButton("Deny") {dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
    }

    private fun onCameraPermissionGranted() {
        rtcClient = RTCClient(application, binding.localView)
        rtcClient.startLocalVideoCapture()
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show()
    }
}
