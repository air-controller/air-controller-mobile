package com.youngfeng.android.assistant.connection.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputType
import android.text.format.Formatter
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.skydoves.powermenu.CustomPowerMenu
import com.skydoves.powermenu.OnMenuItemClickListener
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.connection.viewmodel.ConnectionViewModel
import com.youngfeng.android.assistant.databinding.ActivityConnectionBinding
import com.youngfeng.android.assistant.manager.AccessControlManager
import com.youngfeng.android.assistant.popmenu.CheckMenuAdapter
import com.youngfeng.android.assistant.popmenu.CheckMenuItem

class ConnectionActivity : AppCompatActivity() {
    private var mViewDataBinding: ActivityConnectionBinding? = null
    private val mViewModel by viewModels<ConnectionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_connection)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mViewModel.init()

        mViewDataBinding?.apply {
            this.lifecycleOwner = this@ConnectionActivity
            this.viewModel = mViewModel

            selectAccessState.setOnClickListener {
                showAccessStateMenu(it)
            }

            selectPwd.setOnClickListener {
                showPwdStateMenu(it)
            }

            imageFinish.setOnClickListener {
                finish()
            }

            imageCommit.setOnClickListener {
                submitPasswd()
            }
        }

        initObservers()
        initWifiManager()
    }

    private fun submitPasswd() {
        AccessControlManager.getInstance().password = mViewModel.passwd.value
        finish()
    }

    private fun showAccessStateMenu(anchor: View) {
        val density = this.resources.displayMetrics.density
        val menu =
            CustomPowerMenu.Builder<CheckMenuItem, CheckMenuAdapter>(this, CheckMenuAdapter())
                .addItem(
                    CheckMenuItem(
                        text = getString(R.string.allow),
                        isChecked = mViewModel.allowAccess.value == true
                    )
                ).addItem(
                    CheckMenuItem(
                        text = getString(R.string.disallow),
                        isChecked = mViewModel.allowAccess.value == false
                    )
                ).setMenuRadius(15 * density).setWidth((density * 187).toInt()).build()

        menu.onMenuItemClickListener = OnMenuItemClickListener { position, item ->
            if (item?.isChecked == false) {
                if (position == 0) {
                    mViewModel.setAllowAccess(true)
                } else {
                    mViewModel.setAllowAccess(false)
                }
                menu?.dismiss()
            }
        }
        menu.showAsAnchorCenter(anchor)
    }

    private fun showPwdStateMenu(anchor: View) {
        val density = this.resources.displayMetrics.density
        val menu =
            CustomPowerMenu.Builder<CheckMenuItem, CheckMenuAdapter>(this, CheckMenuAdapter())
                .addItem(
                    CheckMenuItem(
                        text = getString(R.string.no_pwd),
                        isChecked = mViewModel.enablePwd.value == false
                    )
                ).addItem(
                    CheckMenuItem(
                        text = getString(R.string.password),
                        isChecked = mViewModel.enablePwd.value == true
                    )
                ).setMenuRadius(15 * density).setWidth((density * 187).toInt())
                .setLifecycleOwner(this).setFocusable(true).build()
        menu.onMenuItemClickListener = OnMenuItemClickListener { position, item ->
            if (item?.isChecked == false) {
                if (position == 0) {
                    mViewModel.setEnablePwd(false)
                } else {
                    mViewModel.setEnablePwd(true)
                }
                menu.dismiss()
            }
        }
        menu.showAsAnchorCenter(anchor)
    }

    private fun initWifiManager() {
        val wifiManager =
            this@ConnectionActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        mViewModel.setIpAddress(Formatter.formatIpAddress(wifiInfo.ipAddress))
    }

    private fun initObservers() {
        mViewModel.allowAccess.observe(
            this
        ) { allowAccess ->
            mViewDataBinding?.selectAccessState?.findViewById<TextView>(R.id.text_label)
                ?.setText(if (allowAccess) R.string.allow else R.string.disallow)
        }

        mViewModel.enablePwd.observe(this) { enablePwd ->
            mViewDataBinding?.selectPwd?.findViewById<TextView>(R.id.text_label)
                ?.setText(if (enablePwd) R.string.password else R.string.no_pwd)
        }

        mViewModel.showPwd.observe(this) { showPwd ->
            mViewDataBinding?.editPwd?.inputType = if (showPwd) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            mViewDataBinding?.imagePwd?.imageTintList = ColorStateList.valueOf(if (showPwd) Color.parseColor("#0f84ff") else Color.parseColor("#727272"))
        }

        mViewModel.passwd.observe(this) { passwd ->
            mViewDataBinding?.imageCommit?.isEnabled = (passwd?.length ?: 0) >= 6
            mViewDataBinding?.imageCommit?.imageTintList = ColorStateList.valueOf(if ((passwd?.length ?: 0) >= 6) Color.parseColor("#333333") else Color.GRAY)
        }
    }
}
