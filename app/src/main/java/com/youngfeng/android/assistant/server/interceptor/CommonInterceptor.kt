package com.youngfeng.android.assistant.server.interceptor

import com.yanzhenjie.andserver.annotation.Interceptor
import com.yanzhenjie.andserver.framework.HandlerInterceptor
import com.yanzhenjie.andserver.framework.handler.RequestHandler
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse

@Interceptor
class CommonInterceptor : HandlerInterceptor {
    override fun onIntercept(
        request: HttpRequest,
        response: HttpResponse,
        handler: RequestHandler
    ): Boolean {
        response.addHeader("Access-Control-Allow-Private-Network", "true")
        return false
    }
}
