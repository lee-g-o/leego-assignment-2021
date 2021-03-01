package com.leego.assignment.apiserver.service

import com.leego.assignment.apiserver.common.Utility
import com.leego.assignment.apiserver.responseModel.RankModel
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UniRestImpl (private val utility: Utility) {
    @Value("\${kakao.host}")
    private val kakaoHost = "https://dapi.kakao.com"

    @Value("\${naver.host}")
    private val naverHost = "https://openapi.naver.com"

    @Value("\${api.connect-timeout}")
    private val connectTimeout = 1000

    fun getUniRestToKakao(url: String,
                          headers: HashMap<String, String>): HttpResponse<JsonNode> {
        try {
            return Unirest.get(kakaoHost + url)
                    .headers(headers)
                    .connectTimeout(connectTimeout)
                    .asJson()
        } catch (e: Exception) {
            logger.debug(">>> $e")
            throw Exception()
        }
    }

    @SuppressWarnings("deprecation")
    fun getUniRestToNaver(url: String,
                          headers: HashMap<String, String>): HttpResponse<JsonNode> {
        try {
            Unirest.config().httpClient(utility.httpClient())
            return Unirest.get(naverHost + url)
                    .headers(headers)
                    .connectTimeout(connectTimeout)
                    .asJson()
        } catch (e: Exception) {
            logger.debug(">>> $e")
            throw Exception()
        }
    }

    fun getUniRestToLocalhost(url: String): HttpResponse<JsonNode> {
        return Unirest.get(url).asJson()
    }

    fun postUniRestToLocalhost(url: String, request: String): HttpResponse<JsonNode> {
        return try {
            Unirest.post(url).body(request).asJson()
        } catch (e: Exception) {
            throw Exception()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}