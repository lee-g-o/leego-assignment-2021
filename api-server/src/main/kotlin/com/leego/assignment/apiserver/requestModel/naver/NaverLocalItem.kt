package com.leego.assignment.apiserver.requestModel.naver

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NaverLocalItem(var title: String,
                          var link: String?,
                          var category: String?,
                          var description: String?,
                          var telephone: String?,
                          var address: String?,
                          var roadAddress: String?,
                          var mapx: String?,
                          var mapy: String?,
                          var imageUrls: ArrayList<String>?)