package com.leego.assignment.apiserver.repository

import com.leego.assignment.apiserver.model.QueryCountModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QueryCountRepository: JpaRepository<QueryCountModel, Any> {
    fun countByQueryName(queryName: String): Long
    fun findByQueryName(queryName: String): QueryCountModel
    fun findTop10ByOrderByCountDesc(): List<QueryCountModel>
}