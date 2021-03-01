package com.leego.assignment.apiserver.service

import com.leego.assignment.apiserver.model.QueryCountModel
import com.leego.assignment.apiserver.repository.QueryCountRepository
import com.leego.assignment.apiserver.responseModel.ErrorMessage
import com.leego.assignment.apiserver.responseModel.RankModel
import com.leego.assignment.apiserver.common.Utility.Companion.GSON
import com.leego.assignment.apiserver.model.QuerySyncCountModel
import com.leego.assignment.apiserver.repository.QuerySyncCountRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.Throws

@Service
@Transactional
class QueryCountService(
        private val queryCountRepository: QueryCountRepository,
        private val querySyncCountRepository: QuerySyncCountRepository,
        private val uniRestImpl: UniRestImpl,
        private val discoveryClient: DiscoveryClient) {

    @Value("\${spring.application.name}")
    private val appName: String? = null

    @Throws
    fun syncQueryCount(modelList: String) {
        querySyncCountRepository.deleteAll()
        val linkedTreeMap = GSON.fromJson(modelList, ArrayList::class.java)
        linkedTreeMap.forEach {
            val jsonObject = GSON.toJsonTree(it).asJsonObject
            val rankModel = GSON.fromJson(jsonObject, RankModel::class.java)
            querySyncCountRepository.save(
                    QuerySyncCountModel(null,  rankModel.rank, rankModel.queryName!!, rankModel.count!!))
        }
    }

    @Throws
    fun selectQuerySyncCountRank(): Any {
        val list = querySyncCountRepository.findAll()
        return when (list.size) {
            0 -> ErrorMessage("API 사용량이 없습니다.")
            else -> list
        }
    }

    @Throws
    fun insertQuery(query: String) {
        val model = QueryCountModel(null, query, 1)
        queryCountRepository.save(model)
    }

    @Throws
    fun selectQueryCount(query: String): Long {
        return queryCountRepository.countByQueryName(query)
    }

    @Throws
    fun countingQuery(query: String) {
        val model = queryCountRepository.findByQueryName(query)
        model.count++
        queryCountRepository.save(model)
    }

    @Throws
    fun getInstanceRanking(): List<QueryCountModel> {
        return queryCountRepository.findAll()
    }

    @Throws
    fun selfRank(): Any {
        val result = queryCountRepository.findTop10ByOrderByCountDesc()
        return if (result.isEmpty()) {
            ErrorMessage("API 사용량이 없습니다.")
        } else {
            val response = ArrayList<RankModel>()
            result.forEachIndexed { i, v ->
                if (v.queryName.isNotEmpty()) {
                    response += RankModel(i + 1, v.queryName, v.count)
                }
            }
            response
        }
    }

    @Throws
    fun getQueryRanking(): Any {
        val instances = discoveryClient.getInstances(appName)
        return if (instances.count() <= 1) {
            selfRank()
        } else {
            val rankModelList = ArrayList<RankModel>()
            val rankModelMap = hashMapOf<String, Long>()
            for ((index, _) in instances.withIndex()) {
                val serviceUri = String.format("%s/%s",
                        instances[index].uri.toString(), "instanceRank")
                val instanceResult = uniRestImpl.getUniRestToLocalhost(serviceUri)
                if (!instanceResult.body.array.isEmpty) {
                    instanceResult.body.array.toList().forEach {
                        val model = GSON.fromJson(it.toString(), RankModel::class.java)
                        val queryName = model.queryName!!
                        if (queryName.isNotEmpty()) {
                            if (rankModelMap.containsKey(queryName)) {
                                rankModelMap[queryName] = rankModelMap[queryName]?.plus(model.count!!)!!
                            } else {
                                rankModelMap[queryName] = model.count!!
                            }
                        }
                    }
                }
            }
            if (rankModelMap.isEmpty()) {
                ErrorMessage("API 사용량이 없습니다.")
            } else {
                rankModelMap.forEach { (k, v) -> rankModelList += RankModel(null, k, v) }
                rankModelList.sortByDescending { it.count }
                rankModelList.forEachIndexed { i, v ->  v.rank = i + 1 }
                rankModelList.take(10)
            }
        }
    }
}