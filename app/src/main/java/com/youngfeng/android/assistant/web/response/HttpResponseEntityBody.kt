package com.youngfeng.android.assistant.web.response

import com.yanzhenjie.andserver.framework.body.StringBody
import com.yanzhenjie.andserver.util.MediaType

class HttpResponseEntityBody(body: String) : StringBody(body, MediaType.APPLICATION_JSON_UTF8)
