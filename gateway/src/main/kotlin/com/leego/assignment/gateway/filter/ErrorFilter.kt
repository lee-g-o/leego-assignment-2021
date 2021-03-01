package com.leego.assignment.gateway.filter

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.exception.ZuulException
import org.springframework.stereotype.Component


@Component
class ErrorFilter : ZuulFilter() {
    override fun filterType() = "error"
    override fun filterOrder() = -1
    override fun shouldFilter() = true
    override fun run(): Any? {
        val ctx = RequestContext.getCurrentContext()
        val throwable= ctx["throwable"]
        if (throwable is ZuulException) {
            if (throwable.errorCause != "route:RibbonRoutingFilter") return null
            ctx.remove("throwable")
            val serviceId:String? = ctx["serviceId"] as String
            ctx.response.contentType = "application/json"
            ctx.response.characterEncoding = "UTF-8"
            ctx.responseBody = "{\n\"message\" : \"마이크로서비스($serviceId)를 찾을 수 없습니다. 잠시 후 다시 시도하십시오.\"\n}"
            ctx.responseStatusCode = 503
        }
        return null
    }
}