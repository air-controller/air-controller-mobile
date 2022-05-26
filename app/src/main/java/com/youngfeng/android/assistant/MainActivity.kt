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
import android.view.MotionEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.youngfeng.android.assistant.about.AboutActivity
import com.youngfeng.android.assistant.databinding.ActivityMainBinding
import com.youngfeng.android.assistant.event.BatchUninstallEvent
import com.youngfeng.android.assistant.event.DeviceConnectEvent
import com.youngfeng.android.assistant.event.DeviceDisconnectEvent
import com.youngfeng.android.assistant.event.DeviceReportEvent
import com.youngfeng.android.assistant.home.HomeViewModel
import com.youngfeng.android.assistant.model.DesktopInfo
import com.youngfeng.android.assistant.model.Device
import com.youngfeng.android.assistant.model.PermissionGrantStatus
import com.youngfeng.android.assistant.model.RunStatus
import com.youngfeng.android.assistant.scan.ScanActivity
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
            .setNegativeButton(R.string.cancel) {
                dialog, _ ->
                dialog.dismiss()
            }.setMessage(R.string.tip_disconnect)
            .create()
    }
    private val mSupportDeveloperDialog by lazy {
        AlertDialog.Builder(this)
            .setPositiveButton(
                R.string.support
            ) { _, _ -> CommonUtil.openExternalBrowser(this, getString(R.string.url_project_desktop)) }
            .setNegativeButton(R.string.refuse) {
                dialog, _ ->
                dialog.dismiss()
            }.setMessage(R.string.tip_support_developer)
            .create()
    }
    private val mUninstalledPackages = mutableListOf<String>()
    private lateinit var mUninstallLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_PERMISSIONS = 1
        private const val RC_UNINSTALL = 2
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

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
                if (!mSupportDeveloperDialog.isShowing) mSupportDeveloperDialog.show()
            }

            this.btnIndicator.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    btnIndicator.alpha = 0.6f
                }

                if (event.action == MotionEvent.ACTION_UP) {
                    requestPermissions(false)
                    btnIndicator.alpha = 1f
                }

                if (event.action == MotionEvent.ACTION_CANCEL) {
                    btnIndicator.alpha = 1f
                }

                return@setOnTouchListener true
            }
        }

        registerNetworkListener()
        setUpDeviceInfo()
        updateRunStatus()

        observeRunStatusChange()

        requestPermissions(true)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        registerUninstallLauncher()
    }

    private fun registerUninstallLauncher() {
        mUninstallLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (mUninstalledPackages.isNotEmpty()) {
                val firstPackageName = mUninstalledPackages.first()
                mUninstalledPackages.removeFirst()

                batchUninstall(firstPackageName)
            }
        }
    }

    private fun registerNetworkListener() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)

                    runOnUiThread {
                        val isWifiConnected = connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
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
            val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)

                        runOnUiThread {
                            val isWifiConnected = connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
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

    private fun updateRunStatus() {
        mViewModel.isDeviceConnected.observe(this) { isConnected ->
            if (isConnected) {
                when (getPermissionGrantStatus()) {
                    PermissionGrantStatus.AllGranted -> mViewModel.updateRunStatus(RunStatus.Normal)
                    PermissionGrantStatus.PartOfGranted -> mViewModel.updateRunStatus(RunStatus.PartNormal)
                    else -> mViewModel.updateRunStatus(RunStatus.AllNotWorking)
                }
            } else {
                mViewModel.updateRunStatus(RunStatus.Disconnected)
            }
        }
    }

    private fun observeRunStatusChange() {
        mViewModel.currentRunStatus.observe(this) { status ->
            updateIndicatorUIWith(status)
            updateIndicatorHintWith(status)
        }
    }

    private fun updateIndicatorUIWith(status: RunStatus) {
        when (status) {
            RunStatus.Normal -> {
                mViewDataBinding?.btnIndicator?.setBackgroundResource(R.drawable.shape_circle_green)
                mViewDataBinding?.btnIndicator?.setText(R.string.enjoying)
                mViewDataBinding?.btnIndicator?.isEnabled = false
            }
            RunStatus.PartNormal -> {
                mViewDataBinding?.btnIndicator?.setBackgroundResource(R.drawable.shape_circle_yellow)
                mViewDataBinding?.btnIndicator?.setText(R.string.fix_immediately)
                mViewDataBinding?.btnIndicator?.isEnabled = true
            }
            RunStatus.AllNotWorking -> {
                mViewDataBinding?.btnIndicator?.setBackgroundResource(R.drawable.shape_circle_red)
                mViewDataBinding?.btnIndicator?.setText(R.string.fix_immediately)
                mViewDataBinding?.btnIndicator?.isEnabled = true
            }
            else -> {
                mViewDataBinding?.btnIndicator?.setBackgroundResource(R.drawable.shape_circle_gray)
                mViewDataBinding?.btnIndicator?.setText(R.string.readiness)
                mViewDataBinding?.btnIndicator?.isEnabled = false
            }
        }
    }

    private fun updateIndicatorHintWith(status: RunStatus) {
        when (status) {
            RunStatus.Normal -> {
                mViewDataBinding?.textIndicator?.setText(R.string.hint_connected_and_operation_normal)
            }
            RunStatus.PartNormal -> {
                mViewDataBinding?.textIndicator?.setText(R.string.hint_connected_and_part_of_normal)
            }
            RunStatus.AllNotWorking -> {
                mViewDataBinding?.textIndicator?.setText(R.string.hint_connected_and_all_not_working)
            }
            else -> {
                mViewDataBinding?.textIndicator?.setText(R.string.hint_disconnected)
            }
        }
    }

    // 这里指桌面端所需权限授予状态
    private fun getPermissionGrantStatus(): PermissionGrantStatus {
        var status = PermissionGrantStatus.AllNotGranted

        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
            EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            status = PermissionGrantStatus.AllGranted
        }

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
            !EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            status = PermissionGrantStatus.PartOfGranted
        }

        return status
    }

    // 请求必要权限，includeAppNeeded为false时，表示只请求桌面端所需手机权限，否则请求所有app所需权限
    private fun requestPermissions(includeAppNeeded: Boolean) {
        val perms = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

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
        updateRunStatus()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        updateRunStatus()
    }

    override fun onResume() {
        super.onResume()
        updateRunStatus()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}
