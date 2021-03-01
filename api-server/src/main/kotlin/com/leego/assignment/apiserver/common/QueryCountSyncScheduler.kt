package com.leego.assignment.apiserver.common

import com.leego.assignment.apiserver.common.Utility.Companion.GSON
import com.leego.assignment.apiserver.responseModel.RankModel
import com.leego.assignment.apiserver.service.QueryCountService
import com.leego.assignment.apiserver.service.UniRestImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class QueryCountSyncScheduler(val uniRestImpl: UniRestImpl,
                              val discoveryClient: DiscoveryClient,
                              val queryCountService: QueryCountService) {

    @Value("\${spring.application.name}")
    private val appName: String? = null

    @Scheduled(fixedDelay = 3000)
    fun queryCountSync() {
        try {
            val instances = discoveryClient.getInstances(appName)
            if (instances.count() <= 1) {
                val d = queryCountService.selfRank()
            }
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

            val rankModelList = ArrayList<RankModel>()
            if (rankModelMap.isNotEmpty()) {
                rankModelMap.forEach { (k, v) -> rankModelList += RankModel(null, k, v) }
                rankModelList.sortByDescending { it.count }
                rankModelList.forEachIndexed { i, v -> v.rank = i + 1 }
                rankModelList.take(10)
                for ((index, _) in instances.withIndex()) {
                    val serviceUri = String.format("%s/%s",
                            instances[index].uri.toString(), "syncQueryCount")
                    val jsonArray = GSON.toJson(rankModelList)
                    uniRestImpl.postUniRestToLocalhost(serviceUri, jsonArray)
                }
            }
        } catch (e: Exception) {
            logger.debug(">>> $e")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}