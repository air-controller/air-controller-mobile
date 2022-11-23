package com.youngfeng.android.assistant.scan

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cn.bingoogolapple.qrcode.core.QRCodeView
import cn.bingoogolapple.qrcode.zxing.ZXingView
import com.youngfeng.android.assistant.R
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class ScanActivity : AppCompatActivity(), QRCodeView.Delegate, EasyPermissions.PermissionCallbacks {
    private val mZXingView by lazy { findViewById<ZXingView>(R.id.zxingView) }
    private val mRationaleCameraDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(R.string.rationale_camera_perm)
            .setPositiveButton(R.string.rationale_ask_ok) { _, _ ->
                openAppDetailPage()
            }
            .setNegativeButton(R.string.rationale_ask_cancel) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .create()
    }
    private var isSpotStarted = false

    companion object {
        private const val TAG = "ScanActivity"
        private const val RC_CAMERA = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initZXingView()
        checkCameraPermission {
            startSpot()
        }
    }

    private fun initZXingView() {
        mZXingView.setDelegate(this)
    }

    private fun openAppDetailPage() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun startSpot() {
        if (!isSpotStarted) {
            mZXingView.startCamera()
            mZXingView.startSpotAndShowRect()
            isSpotStarted = true
        }
    }

    private fun stopSpot() {
        if (isSpotStarted) {
            mZXingView.stopCamera()
            isSpotStarted = false
        }
    }

    private fun checkCameraPermission(onGrant: () -> Unit) {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            onGrant.invoke()
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, RC_CAMERA, Manifest.permission.CAMERA)
                    .setRationale(R.string.rationale_camera_perm)
                    .setPositiveButtonText(R.string.rationale_ask_ok)
                    .setNegativeButtonText(R.string.rationale_ask_cancel)
                    .build()
            )
        }
    }

    override fun onScanQRCodeSuccess(result: String) {
        if (result.startsWith("http://") || result.startsWith("https://")) {
            openExternalBrowser(result)
        }

        startSpot()
    }

    private fun openExternalBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
    }

    override fun onScanQRCodeOpenCameraError() {
        Log.e(TAG, "onScanQRCodeOpenCameraError")
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        mZXingView.startSpot()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            mRationaleCameraDialog.show()
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            if (mRationaleCameraDialog.isShowing) mRationaleCameraDialog.dismiss()
            startSpot()
        }
    }

    override fun onStop() {
        super.onStop()
        stopSpot()
    }

    override fun onDestroy() {
        super.onDestroy()
        mZXingView.onDestroy()
    }
}
