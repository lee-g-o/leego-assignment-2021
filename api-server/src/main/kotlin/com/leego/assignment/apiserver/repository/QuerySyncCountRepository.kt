package com.leego.assignment.apiserver.repository

import com.leego.assignment.apiserver.model.QuerySyncCountModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuerySyncCountRepository: JpaRepository<QuerySyncCountModel, Any>