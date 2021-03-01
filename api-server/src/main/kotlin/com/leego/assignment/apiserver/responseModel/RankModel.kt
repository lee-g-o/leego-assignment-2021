package com.leego.assignment.apiserver.responseModel

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RankModel(var rank: Int?,
                     var queryName: String?,
                     var count: Long?)