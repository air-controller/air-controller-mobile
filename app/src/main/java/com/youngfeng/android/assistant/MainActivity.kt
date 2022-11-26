package com.youngfeng.android.assistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.youngfeng.android.assistant.about.AboutActivity
import com.youngfeng.android.assistant.connection.view.ConnectionActivity
import com.youngfeng.android.assistant.databinding.ActivityMainBinding
import com.youngfeng.android.assistant.event.BatchUninstallEvent
import com.youngfeng.android.assistant.event.DeviceConnectEvent
import com.youngfeng.android.assistant.event.DeviceDisconnectEvent
import com.youngfeng.android.assistant.event.DeviceReportEvent
import com.youngfeng.android.assistant.event.Permission
import com.youngfeng.android.assistant.event.RequestDrawOverlayEvent
import com.youngfeng.android.assistant.event.RequestPermissionsEvent
import com.youngfeng.android.assistant.home.HomeViewModel
import com.youngfeng.android.assistant.model.DesktopInfo
import com.youngfeng.android.assistant.model.Device
import com.youngfeng.android.assistant.scan.ScanActivity
import com.youngfeng.android.assistant.service.NetworkService
import com.youngfeng.android.assistant.support.DeveloperSupportActivity
import com.youngfeng.android.assistant.util.CommonUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import timber.log.Timber

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private var mViewDataBinding: ActivityMainBinding? = null
    private val mViewModel by viewModels<HomeViewModel>()
    private val mDisconnectConfirmDialog by lazy {
        AlertDialog.Builder(this)
            .setPositiveButton(
                R.string.sure
            ) { _, _ -> mViewModel.disconnect() }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }.setMessage(R.string.tip_disconnect)
            .create()
    }
    private val mRequestDrawOverlayDialog by lazy {
        AlertDialog.Builder(this)
            .setPositiveButton(
                R.string.authorize_now
            ) { _, _ ->
                startDrawOverlayRequestActivity()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }.setMessage(R.string.rationale_draw_overlay_window)
            .create()
    }
    private val mUninstalledPackages = mutableListOf<String>()
    private lateinit var mUninstallLauncher: ActivityResultLauncher<Intent>
    private val mPermissionManager by lazy {
        com.youngfeng.android.assistant.manager.PermissionManager.with(this)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_PERMISSIONS = 1
        private const val RC_UNINSTALL = 2
        private const val RC_PERM_GET_ACCOUNTS = 3
        private const val RC_PERM_READ_CONTACTS = 4
        private const val RC_PERM_WRITE_CONTACTS = 5
        private const val RC_DIALOG_PERMISSION = 6
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initializeUI()

        registerNetworkListener()
        setUpDeviceInfo()

        updatePermissionsStatus()
        requestPermissions(true)
        requestFloatPermission()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        registerUninstallLauncher()
        startNetworkService()
    }

    private fun startNetworkService() {
        val intent = Intent().setClass(this, NetworkService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun initializeUI() {
        mViewDataBinding?.apply {
            this.lifecycleOwner = this@MainActivity
            this.textSupportDeveloper.paint.flags = Paint.UNDERLINE_TEXT_FLAG
            this.viewModel = mViewModel

            this.btnOpenWifiSettings.setOnClickListener {
                try {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Timber.e("Open wifi setting failure, reason: ${e.message}")
                }
            }

            this.btnDisconnect.setOnClickListener {
                if (!mDisconnectConfirmDialog.isShowing) {
                    mDisconnectConfirmDialog.show()
                }
            }

            this.textSupportDeveloper.setOnClickListener {
                val intent = Intent(this@MainActivity, DeveloperSupportActivity::class.java)
                startActivity(intent)
            }

            this.textAuthorizeNow.apply {
                paintFlags = this.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }

            this.textAuthorizeNow.setOnClickListener {
                CommonUtil.openAppDetailSettings(this@MainActivity)
            }
        }
    }

    private fun registerUninstallLauncher() {
        mUninstallLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (mUninstalledPackages.isNotEmpty()) {
                    val firstPackageName = mUninstalledPackages.first()
                    mUninstalledPackages.removeFirst()

                    batchUninstall(firstPackageName)
                }
            }
    }

    private fun registerNetworkListener() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object :
                    ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)

                        runOnUiThread {
                            val isWifiConnected = connectivityManager.getNetworkCapabilities(network)
                                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            mViewModel.setWifiConnectStatus(isWifiConnected == true)
                        }
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)

                        runOnUiThread {
                            mViewModel.setWifiConnectStatus(false)
                        }
                    }
                })
        } else {
            val request =
                NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            connectivityManager.registerNetworkCallback(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)

                        runOnUiThread {
                            val isWifiConnected =
                                connectivityManager.getNetworkCapabilities(network)
                                    ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            mViewModel.setWifiConnectStatus(isWifiConnected == true)
                        }
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)

                        runOnUiThread {
                            mViewModel.setWifiConnectStatus(false)
                        }
                    }
                }
            )
        }
    }

    private fun setUpDeviceInfo() {
        mViewModel.setDeviceName(Build.MODEL)

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid
        mViewModel.setWlanName(ssid?.replace(oldValue = "\"", newValue = "") ?: "Unknown SSID")
    }

    private fun updatePermissionsStatus() {
        val permissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.REQUEST_INSTALL_PACKAGES
        )

        EasyPermissions.hasPermissions(this, *permissions.toTypedArray()).apply {
            mViewModel.updateAllPermissionsGranted(this)
        }
    }

    // 请求必要权限，includeAppNeeded为false时，表示只请求桌面端所需手机权限，否则请求所有app所需权限
    private fun requestPermissions(includeAppNeeded: Boolean) {
        val perms = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            perms.add(
                Manifest.permission.REQUEST_INSTALL_PACKAGES
            )
        }

        if (includeAppNeeded) {
            perms.add(Manifest.permission.CAMERA)
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
            perms.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        EasyPermissions.requestPermissions(
            PermissionRequest.Builder(this, RC_PERMISSIONS, *(perms.toTypedArray()))
                .setRationale(R.string.rationale_permissions)
                .setPositiveButtonText(R.string.rationale_ask_ok)
                .setNegativeButtonText(R.string.rationale_ask_cancel)
                .build()
        )
    }

    private fun requestFloatPermission() {
        if (!CommonUtil.checkFloatPermission(this)) {
            mRequestDrawOverlayDialog.show()
        }
    }

    private fun startDrawOverlayRequestActivity() {
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        } else if (sdkInt >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceConnected(event: DeviceConnectEvent) {
        mViewModel.setDeviceConnected(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceDisconnected(event: DeviceDisconnectEvent) {
        mViewModel.setDeviceConnected(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceReport(event: DeviceReportEvent) {
        var os = "MacOS"

        when (event.device.platform) {
            Device.PLATFORM_LINUX -> os = "Linux"
            Device.PLATFORM_WINDOWS -> os = "Windows"
            else -> "MacOS"
        }
        val desktopInfo = DesktopInfo(event.device.name, event.device.ip, os)
        mViewModel.setDesktopInfo(desktopInfo)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBatchUninstall(event: BatchUninstallEvent) {
        mUninstalledPackages.addAll(event.packages)

        if (mUninstalledPackages.isNotEmpty()) {
            val packageName = mUninstalledPackages.first()
            mUninstalledPackages.removeFirst()

            batchUninstall(packageName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRequestPermissions(event: RequestPermissionsEvent) {
        val permissions = event.permissions.map {
            when (it) {
                Permission.GetAccounts -> Manifest.permission.GET_ACCOUNTS
                Permission.ReadContacts -> Manifest.permission.READ_CONTACTS
                Permission.WriteContacts -> Manifest.permission.WRITE_CONTACTS
                Permission.RequestInstallPackages -> Manifest.permission.REQUEST_INSTALL_PACKAGES
                Permission.WriteExternalStorage -> Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
        }.toTypedArray()

        mPermissionManager.requestMultiplePermissions(RC_PERMISSIONS, *permissions)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRequestDrawOverlay(event: RequestDrawOverlayEvent) {
        requestFloatPermission()
    }

    private fun batchUninstall(packageName: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_DELETE
        intent.data = Uri.parse("package:$packageName")
        mUninstallLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_scan) {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
            return true
        }

        if (item.itemId == R.id.menu_about) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            return true
        }

        if (item.itemId == R.id.menu_connect) {
            val intent = Intent(this, ConnectionActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        updatePermissionsStatus()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        updatePermissionsStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionsStatus()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}
