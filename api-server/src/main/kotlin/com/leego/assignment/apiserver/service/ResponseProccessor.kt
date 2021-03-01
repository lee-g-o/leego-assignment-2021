package com.leego.assignment.apiserver.service

import com.leego.assignment.apiserver.requestModel.kakao.KakaoImageDocument
import com.leego.assignment.apiserver.requestModel.kakao.KakaoLocalDocument
import com.leego.assignment.apiserver.requestModel.kakao.KakaoLocalMeta
import com.leego.assignment.apiserver.requestModel.naver.NaverImageItem
import com.leego.assignment.apiserver.requestModel.naver.NaverLocalItem
import com.leego.assignment.apiserver.requestModel.naver.NaverLocalMeta
import com.leego.assignment.apiserver.common.Utility
import com.leego.assignment.apiserver.common.Utility.Companion.GSON
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import org.springframework.stereotype.Component

@Component
class ResponseProccessor (private val utility: Utility) {
    private fun responseToMap(response: HttpResponse<JsonNode>): MutableMap<String, Any>
            = response.body.`object`.toMap()

    fun preProcLocalMetaForNaver(response: HttpResponse<JsonNode>): NaverLocalMeta {
        val items = ArrayList<NaverLocalItem>()
        val result = responseToMap(response)
        val localMeta = GSON.fromJson(GSON.toJson(result), NaverLocalMeta::class.java)
        localMeta.items?.forEach {
            val jsonObject = GSON.toJsonTree(it).asJsonObject
            val item = GSON.fromJson(jsonObject, NaverLocalItem::class.java)
            item.title = utility.removeTag(item.title)
            items += item
        }
        localMeta.items = items
        return localMeta
    }

    fun preProcLocalMeta(response: HttpResponse<JsonNode>): KakaoLocalMeta {
        val jsonObject = GSON.toJsonTree(responseToMap(response)["meta"]).asJsonObject
        return GSON.fromJson(jsonObject, KakaoLocalMeta::class.java)
    }

    fun preProcLocalDocs(response: HttpResponse<JsonNode>): ArrayList<KakaoLocalDocument> {
        val localDocs = ArrayList<KakaoLocalDocument>()
        val result = responseToMap(response)
        val documents = result["documents"] as ArrayList<*>
        documents.forEach {
            val jsonObject = GSON.toJsonTree(it).asJsonObject
            localDocs += GSON.fromJson(jsonObject, KakaoLocalDocument::class.java)
        }
        return localDocs
    }

    fun preProcImageDocs(response: HttpResponse<JsonNode>?): ArrayList<String> {
        if (response == null) return ArrayList()
        val result = responseToMap(response)
        return when {
            result["documents"] != null -> imageByKakao(result)
            result["items"] != null -> imageByNaver(result)
            else -> ArrayList()
        }
    }

    fun imageByNaver(result: MutableMap<String, Any>): ArrayList<String> {
        val imageUrls = ArrayList<String>()
        val imageDocs = ArrayList<NaverImageItem>()
        val documents = result["items"] as ArrayList<*>
        documents.forEach {
            val jsonObject = GSON.toJsonTree(it).asJsonObject
            imageDocs += GSON.fromJson(jsonObject, NaverImageItem::class.java)
        }
        imageDocs.forEach {
            imageUrls += it.link
        }
        return imageUrls
    }

    fun imageByKakao(result: MutableMap<String, Any>): ArrayList<String> {
        val imageUrls = ArrayList<String>()
        val imageDocs = ArrayList<KakaoImageDocument>()
        val documents = result["documents"] as ArrayList<*>
        documents.forEach {
            val jsonObject = GSON.toJsonTree(it).asJsonObject
            imageDocs += GSON.fromJson(jsonObject, KakaoImageDocument::class.java)
        }
        imageDocs.forEach {
            imageUrls += it.image_url
        }
        return imageUrls
    }
}