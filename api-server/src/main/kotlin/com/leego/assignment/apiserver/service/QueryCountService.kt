package com.leego.assignment.apiserver.service

import com.leego.assignment.apiserver.model.QueryCountModel
import com.leego.assignment.apiserver.repository.QueryCountRepository
import com.leego.assignment.apiserver.responseModel.ErrorMessage
import com.leego.assignment.apiserver.responseModel.RankModel
import com.leego.assignment.apiserver.common.Utility.Companion.GSON
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.stereotype.Service

@Service
class QueryCountService(
        private val queryCountRepository: QueryCountRepository,
        private val uniRestImpl: UniRestImpl,
        private val discoveryClient: DiscoveryClient) {

    @Value("\${spring.application.name}")
    private val appName: String? = null

    fun insertQuery(query: String) {
        val model = QueryCountModel(null, query, 1)
        queryCountRepository.save(model)
    }

    fun selectQueryCount(query: String): Long {
        return queryCountRepository.countByQueryName(query)
    }

    fun countingQuery(query: String) {
        val model = queryCountRepository.findByQueryName(query)
        model.count++
        queryCountRepository.save(model)
    }

    fun getInstanceRanking(): List<QueryCountModel> {
        return queryCountRepository.findAll()
    }

    fun selfRank(): Any {
        val result = queryCountRepository.findTop10ByOrderByCountDesc()
        return if (result.isEmpty()) {
            ErrorMessage("API 사용량이 없습니다.")
        } else {
            var count = 1
            val response = ArrayList<RankModel>()
            result.forEach {
                if (it.queryName.isNotEmpty()) {
                    response += RankModel(count++, it.queryName, it.count)
                }
            }
            response
        }
    }

    fun getQueryRanking(): Any {
        val instances = discoveryClient.getInstances(appName)
        return if (instances.count() <= 1) {
            selfRank()
        } else {
            var count = 1
            val rankModelMap = hashMapOf<String, Long>()
            val rankModelList = ArrayList<RankModel>()
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
                rankModelList.forEach { it.rank = count++ }
                rankModelList.take(10)
            }
        }
    }
}