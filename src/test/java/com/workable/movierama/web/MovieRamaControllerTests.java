package com.workable.movierama.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.workable.movierama.api.dto.Movie;
import com.workable.movierama.base.AbstractMovieRamaTest;
import com.workable.movierama.service.MovieRamaService;

@WebAppConfiguration
public class MovieRamaControllerTests extends AbstractMovieRamaTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@Autowired
	@InjectMocks
	private MovieRamaController controller;

	@MockBean
	private MovieRamaService mockMovieRamaAdminService;

	@Before
	public void setup() throws Exception {
		super.setup();
		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
	}

	@Test
	public void testNormal() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.search(Mockito.eq("the godfather")))
				.toReturn(new Movie(null,
						"the godfather",
						"the coolest movie",
						123l,
						1972,
						Arrays.asList("Marlon Brando",
								"Al Pacino"))
				);

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/search")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "the godfather"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(1)))
				.andExpect(jsonPath("$[0].title", Matchers.equalTo("the godfather")))
				.andExpect(jsonPath("$[0].description", Matchers.equalTo("the coolest movie")))
				.andExpect(jsonPath("$[0].numberOfReviews", Matchers.equalTo(123)))
				.andExpect(jsonPath("$[0].productionYear", Matchers.equalTo(1972)))
				.andExpect(jsonPath("$[0].actors[0]", Matchers.equalTo("Marlon Brando")))
				.andExpect(jsonPath("$[0].actors[1]", Matchers.equalTo("Al Pacino")));

	}

	@Test
	public void testEmptyList() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.search(Mockito.eq("no movie to show")))
				.toReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/search")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "no movie to show"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.nullValue()));

	}

	@Test
	public void testException() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.search(Mockito.eq("bad movie")))
				.toThrow(new RuntimeException("Something bad happened here"));

		MvcResult httpResponse = mockMvc.perform(MockMvcRequestBuilders.get("/movies/search")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "bad movie"))
				.andDo(MockMvcResultHandlers.print())
				.andReturn();

		Assert.assertEquals("Something bad happened here", httpResponse
				.getResponse().getContentAsString());

	}

	@Test
	public void testValidationError() throws Exception {

		MvcResult httpResponse = mockMvc.perform(MockMvcRequestBuilders.get("/movies/search")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.param("title", "the godfather^"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andReturn();

		Assert.assertEquals("Title contains <>?{}|\\@#$%^&*-_+ or is blank", httpResponse
				.getResponse().getContentAsString());

	}

	@Test
	public void testGetLatest() throws Exception {

		Mockito.stub(mockMovieRamaAdminService.latest())
				.toReturn(Arrays.asList(new Movie(null,
						"the godfather",
						"the coolest movie",
						123l,
						1972,
						Arrays.asList("Marlon Brando",
								"Al Pacino")))
				);

		mockMvc.perform(MockMvcRequestBuilders.get("/movies/latest"))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(1)))
				.andExpect(jsonPath("$[0].title", Matchers.equalTo("the godfather")))
				.andExpect(jsonPath("$[0].description", Matchers.equalTo("the coolest movie")))
				.andExpect(jsonPath("$[0].numberOfReviews", Matchers.equalTo(123)))
				.andExpect(jsonPath("$[0].productionYear", Matchers.equalTo(1972)))
				.andExpect(jsonPath("$[0].actors[0]", Matchers.equalTo("Marlon Brando")))
				.andExpect(jsonPath("$[0].actors[1]", Matchers.equalTo("Al Pacino")));
	}
}
