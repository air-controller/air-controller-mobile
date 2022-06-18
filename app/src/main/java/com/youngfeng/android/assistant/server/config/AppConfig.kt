package com.youngfeng.android.assistant.server.config

import android.content.Context
import android.os.Environment
import com.yanzhenjie.andserver.annotation.Config
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.website.FileBrowser

@Config
class AppConfig : WebConfig {

    override fun onConfig(context: Context, delegate: WebConfig.Delegate?) {
        // 这里添加SD卡作为访问目录，方便直接访问手机应用上的资源
        delegate?.addWebsite(FileBrowser(Environment.getExternalStorageDirectory().absolutePath))
    }
}
