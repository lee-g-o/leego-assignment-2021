package com.leego.assignment.apiserver.common

import com.google.gson.Gson
import org.apache.http.client.HttpClient
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.springframework.stereotype.Component
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext

@Component
class Utility {
    fun httpClient(): HttpClient {
        val sslContext: SSLContext = SSLContextBuilder()
                .loadTrustMaterial(null, object : TrustSelfSignedStrategy() {
                    override fun isTrusted(chain: Array<X509Certificate>, authType: String): Boolean {
                        return true
                    }
                }).build()
        return HttpClients.custom().setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier()).build()
    }

    fun removeTag(html: String): String {
        val regx = "<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>".toRegex()
        return html.replace(regx, "")
    }

    companion object {
        val GSON = Gson()
    }
}