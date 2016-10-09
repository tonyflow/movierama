package com.workable.movierama.web;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.workable.movierama.api.dto.Movie;
import com.workable.movierama.base.AbstractMovieRamaTest;

public class EhCacheControllerTests extends AbstractMovieRamaTest {

	@Autowired
	private CacheManager cacheManager;

	Cache moviesCache = cacheManager.getCache("moviesCache");

	MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@Before
	public void setup() throws Exception {
		super.setup();

		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();

		moviesCache.put("the godfather", new Movie(null,
				"the godfather",
				"the coolest movie",
				123l,
				1972,
				Arrays.asList("Marlon Brando", "Al Pacino")));
	}

	@Test
	public void testInspect() throws Exception {

	}

	@Test
	public void testClear() throws Exception {

	}
}
