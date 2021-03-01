package com.leego.assignment.apiserver.responseModel

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SearchResponse(var meta: Any, var places: List<*>?)