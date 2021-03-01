package com.leego.assignment.apiserver.responseModel

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Meta (var totalPages: Int?,
                 var totalElements: Int?,
                 var currentPage: Int?,
                 var pageSize: Int?,
                 var message: String?)