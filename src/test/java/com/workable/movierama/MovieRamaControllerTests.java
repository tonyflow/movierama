package com.workable.movierama;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.workable.movierama.base.AbstractMovieRamaTest;
import com.workable.movierama.service.MovieRamaAdminService;
import com.workable.movierama.web.MovieRamaController;

@WebAppConfiguration
public class MovieRamaControllerTests extends AbstractMovieRamaTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@Autowired
	@InjectMocks
	private MovieRamaController controller;

	@Mock
	private MovieRamaAdminService mockMovieRamaAdminService;

	@Before
	public void setup() throws Exception {
		super.setup();
		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
	}

	@Test
	public void testNormal() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/list")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "godfather"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)));

	}

	@Test
	public void testEmptyList() throws Exception {

	}

	@Test
	public void testException() throws Exception {

	}

	@Test
	public void testValidationError() throws Exception {

	}
}
