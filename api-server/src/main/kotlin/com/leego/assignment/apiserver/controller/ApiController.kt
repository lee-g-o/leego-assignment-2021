package com.leego.assignment.apiserver.controller

import com.leego.assignment.apiserver.model.QueryCountModel
import com.leego.assignment.apiserver.responseModel.ErrorMessage
import com.leego.assignment.apiserver.service.QueryCountService
import com.leego.assignment.apiserver.service.UniRestService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*


@RestController
class ApiController (private val uniRestService: UniRestService,
                     private val queryCountService: QueryCountService) {
    @ResponseBody
    @RequestMapping(value = ["/search"], method = [RequestMethod.GET], produces = ["application/json"])
    fun search(@RequestParam("query") query: String?,
               @RequestParam("size") size: Int?,
               @RequestParam("page") page: Int?): Any {
        return when {
            (query.isNullOrEmpty()) -> ErrorMessage("검색 내용을 입력하세요.")
            ((size != null) && (size > 15 || size < 1)) -> ErrorMessage("size 값이 적절하지 않습니다. (1 ~ 15)")
            ((page != null) && (page > 45 || page < 1)) -> ErrorMessage("page 값이 적절하지 않습니다. (1 ~ 45)")
            else -> {
                val docSize = size ?: 15
                val docPage = page ?: 1

                val queryCount = queryCountService.selectQueryCount(query)
                if (queryCount == 0L) {
                    queryCountService.insertQuery(query)
                } else {
                    queryCountService.countingQuery(query)
                }
                 uniRestService.getLocalApi(query, docSize, docPage)
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = ["/ranking"], method = [RequestMethod.GET], produces = ["application/json"])
    fun ranking(): Any {
        return queryCountService.getQueryRanking()
    }

    @ResponseBody
    @RequestMapping(value = ["/instanceRank"], method = [RequestMethod.GET], produces = ["application/json"])
    fun instanceRank(): List<QueryCountModel> {
        return queryCountService.getInstanceRanking()
    }
}