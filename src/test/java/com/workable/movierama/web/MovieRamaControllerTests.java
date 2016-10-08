package com.workable.movierama.web;

import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.workable.movierama.api.dto.Movie;
import com.workable.movierama.base.AbstractMovieRamaTest;
import com.workable.movierama.service.MovieRamaService;
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
	private MovieRamaService mockMovieRamaAdminService;

	@Before
	public void setup() throws Exception {
		super.setup();
		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
	}

	@Test
	public void testNormal() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.list(Mockito.eq("the godfather")))
				.toReturn(Arrays.asList(new Movie(null,
						"the godfather",
						"the coolest movie",
						123l,
						1972,
						Arrays.asList("Marlon Brando",
								"Al Pacino"))
						));

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/list")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "godfather"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)));

	}

	@Test
	public void testEmptyList() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.list(Mockito.eq("the godfather")))
				.toReturn(Collections.<Movie> emptyList());

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/list")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "no movie to show"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));

	}

	@Test
	public void testException() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.list(Mockito.eq("the godfather")))
				.toThrow(new Exception("Something bad happened here"));

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/list")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "no movie to show"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));

	}

	@Test
	public void testValidationError() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.list(Mockito.eq("the godfather")))
				.toReturn(Collections.<Movie> emptyList());

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/list")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "<>?{}|\\@#$%^&*-_+"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));

	}
}
