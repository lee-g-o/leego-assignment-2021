package com.leego.assignment.apiserver.requestModel.kakao

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class KakaoLocalDocument(var address_name: String,
                              var category_group_code: String,
                              var category_group_name: String,
                              var category_name: String,
                              var id: String,
                              var phone: String,
                              var place_name: String,
                              var place_url: String,
                              var road_address_name: String,
                              var x: String,
                              var y: String,
                              var distance: String,
                              var imageUrls: ArrayList<String>?)