package com.leego.assignment.apiserver.service

import com.leego.assignment.apiserver.requestModel.naver.NaverLocalItem
import com.leego.assignment.apiserver.responseModel.ErrorMessage
import com.leego.assignment.apiserver.responseModel.Meta
import com.leego.assignment.apiserver.responseModel.SearchResponse
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class UniRestService (private val uniRestImpl: UniRestImpl,
                      private val responseProccessor: ResponseProccessor) {
    @Value("\${kakao.local-search-uri}")
    private val kakaoLocalSearchUri = "/v2/local/search/keyword.json"

    @Value("\${kakao.image-search-uri}")
    private val kakaoImageSearchUri = "/v2/search/image"

    @Value("\${kakao.api-key}")
    private val kakaoApiKey = "KakaoAK 7938ce17f332bdb030b2359f8a64467f"

    @Value("\${naver.local-search-uri}")
    private val naverLocalSearchUri = "/v1/search/local.json"

    @Value("\${naver.image-search-uri}")
    private val naverImageSearchUri = "/v1/search/image"

    @Value("\${naver.client-id}")
    private val naverClientId = "kK5EgiGa1CFHEwRyeULu"

    @Value("\${naver.client-secret}")
    private val naverClientSecret = "RDQHY87cBL"

    @Value("\${api.image.result-size}")
    private val imageResultSize = 3

    @HystrixCommand(fallbackMethod = "getLocalApiForNaver")
    fun getLocalApi(query: String,
                    size: Int,
                    page: Int): SearchResponse {
        val headers = HashMap<String, String>()
        headers["authorization"] = kakaoApiKey
        val result: HttpResponse<JsonNode>
        try {
            result = uniRestImpl.getUniRestToKakao(kakaoLocalSearchUri +
                    "?query=$query" + "&size=$size" + "&page=$page", headers)
        } catch (e: Exception) {
            return getLocalApiForNaver(query, size, page, e)
        }
        val localMeta = responseProccessor.preProcLocalMeta(result)
        when (localMeta.total_count) {
            0 -> {
                throw Exception()
            }
            else -> {
                val localDocs = responseProccessor.preProcLocalDocs(result)
                localDocs.forEach {
                    val imageResponse = getImageApi(it.place_name)
                    it.imageUrls = responseProccessor.preProcImageDocs(imageResponse)
                }
                val totalPage = ceil(localMeta.total_count.toDouble() / size).toInt()
                val meta = Meta(totalPages = totalPage,
                        localMeta.total_count,
                        currentPage = if (page > totalPage) totalPage else page,
                        pageSize = size, null)
                return SearchResponse(meta, localDocs)
            }
        }
    }

    @HystrixCommand(fallbackMethod = "localServiceNotAvailable")
    fun getLocalApiForNaver(query: String,
                            size: Int,
                            page: Int,
                            e: Throwable?): SearchResponse {
        logger.debug(">>> $e")
        val headers = HashMap<String, String>()
        headers["X-Naver-Client-Id"] = naverClientId
        headers["X-Naver-Client-Secret"] = naverClientSecret
        val result = uniRestImpl.getUniRestToNaver(naverLocalSearchUri +
                "?query=$query" + "&display=$size", headers)
        val localMeta = responseProccessor.preProcLocalMetaForNaver(result)
        when (localMeta.total) {
            0 -> {
                return SearchResponse(ErrorMessage("검색 결과가 없습니다."), null)
            }
            else -> {
                localMeta.items!!.forEach {
                    val item = it as NaverLocalItem
                    val imageResponse = getImageApi(item.title)
                    it.imageUrls = responseProccessor.preProcImageDocs(imageResponse)
                }
                val meta = Meta(null, null, null,
                        if (size >= 5) 5 else size,
                        "네이버 지역 API는 Pagination을 지원하지 않습니다.")
                return SearchResponse(meta, localMeta.items!!)
            }
        }
    }

    @HystrixCommand(fallbackMethod = "getImageApiForNaver")
    fun getImageApi(imageQuery: String): HttpResponse<JsonNode>? {
        val headers = HashMap<String, String>()
        headers["authorization"] = kakaoApiKey
        val result: HttpResponse<JsonNode>
        try {
            result = uniRestImpl.getUniRestToKakao(kakaoImageSearchUri +
                    "?query=$imageQuery" + "&size=$imageResultSize", headers)
            if (result.status != 200) throw Exception()
        } catch (e: Exception) {
            return getImageApiForNaver(imageQuery, e)
        }
        return result
    }

    fun getImageApiForNaver(imageQuery: String,
                            e: Throwable?): HttpResponse<JsonNode>? {
        logger.debug(">>> $e")
        val headers = HashMap<String, String>()
        headers["X-Naver-Client-Id"] = naverClientId
        headers["X-Naver-Client-Secret"] = naverClientSecret
        val result: HttpResponse<JsonNode>
        try {
            result = uniRestImpl.getUniRestToNaver(naverImageSearchUri +
                    "?query=$imageQuery" + "&display=$imageResultSize", headers)
            if (result.status != 200) throw Exception()
        } catch (e: Exception) {
            return null
        }
        return result
    }

    fun localServiceNotAvailable(query: String,
                                 size: Int,
                                 page: Int): SearchResponse {
        return SearchResponse(ErrorMessage("OpenAPI 서버가 원활하지 않습니다."), null)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}