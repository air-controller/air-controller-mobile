package com.youngfeng.android.assistant.web.controller

import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.model.MobileInfo
import com.youngfeng.android.assistant.util.CommonUtil
import com.youngfeng.android.assistant.web.entity.HttpResponseEntity

@RestController
@RequestMapping("/common")
class CommonController {
    private val mContext by lazy { MobileAssistantApplication.getInstance() }

    @PostMapping("/mobileInfo")
    @ResponseBody
    fun getMobileInfo(): HttpResponseEntity<MobileInfo> {
        val batteryLevel = CommonUtil.getBatteryLevel(mContext)
        val storageSize = CommonUtil.getExternalStorageSize()

        val mobileInfo = MobileInfo(batteryLevel, storageSize)

        return HttpResponseEntity.success(mobileInfo)
    }
}
