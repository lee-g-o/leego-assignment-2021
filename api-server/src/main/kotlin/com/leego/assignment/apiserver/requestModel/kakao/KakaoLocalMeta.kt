package com.leego.assignment.apiserver.requestModel.kakao

import com.google.gson.internal.LinkedTreeMap

data class KakaoLocalMeta (var is_end: Boolean,
                           var pageable_count: Int,
                           var same_name: LinkedTreeMap<*,*>,
                           var total_count: Int)