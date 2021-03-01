package com.leego.assignment.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Bean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@EnableZuulProxy
@EnableEurekaClient
@SpringBootApplication
class GatewayApplication

fun main(args: Array<String>) {
	runApplication<GatewayApplication>(*args)
}

@Bean
fun corsFilter(): CorsFilter {
	val source = UrlBasedCorsConfigurationSource()
	val config = CorsConfiguration()
	config.allowCredentials = false
	config.addAllowedOrigin("*")
	config.addAllowedHeader("*")
	config.addAllowedMethod("*")
	source.registerCorsConfiguration("/**", config)
	return CorsFilter(source)
}