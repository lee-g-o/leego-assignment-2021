package com.leego.assignment.apiserver.requestModel.naver

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NaverLocalMeta(var lastBuildDate: String,
                          var total: Int,
                          var start: Int,
                          var display: Int,
                          var items: List<Any>?)