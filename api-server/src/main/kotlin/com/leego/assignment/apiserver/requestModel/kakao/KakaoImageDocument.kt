package com.leego.assignment.apiserver.requestModel.kakao

data class KakaoImageDocument(var collection: String,
                              var thumbnail_url: String,
                              var image_url: String,
                              var width: Int,
                              var height: Int,
                              var display_sitename: String,
                              var doc_url: String,
                              var datetime: String)