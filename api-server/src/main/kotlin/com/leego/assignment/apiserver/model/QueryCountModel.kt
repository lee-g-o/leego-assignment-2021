package com.leego.assignment.apiserver.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
@Table(name = "QUERY_COUNT")
data class QueryCountModel(
        @Id @GeneratedValue
        @Column(name = "ID")
        @JsonIgnore
        var id: Long?,
        @Column(name = "QUERY_NAME")
        var queryName: String,
        @Column(name = "COUNT")
        var count: Long)