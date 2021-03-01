package com.leego.assignment.apiserver

import com.leego.assignment.apiserver.controller.ApiController
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter


@RunWith(SpringRunner::class)
@AutoConfigureMockMvc
@SpringBootTest
class ApiServerApplicationTests {
	@Autowired
	private var mockMvc: MockMvc? = null

	@Autowired
	private val ctx: WebApplicationContext? = null

	@Before
	fun setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(ctx!!)
				.addFilters<DefaultMockMvcBuilder>(CharacterEncodingFilter(
						"UTF-8", true)).build()
	}

	@Test
	fun searchTest() {
		val param: MultiValueMap<String, String> = LinkedMultiValueMap()
		param["query"] = "카카오"
		param["size"] = "1"
		param["page"] = "1"
		mockMvc!!.perform(MockMvcRequestBuilders.get("/search")
				.params(param)
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk)
	}

	@Test
	fun rankingTest() {
		mockMvc!!.perform(MockMvcRequestBuilders.get("/ranking")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk)
	}

	@Test
	fun instanceRankTest() {
		mockMvc!!.perform(MockMvcRequestBuilders.get("/instanceRank")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk)
	}
}