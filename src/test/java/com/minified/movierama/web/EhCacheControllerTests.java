package com.minified.movierama.web;

import java.util.Arrays;

import com.minified.movierama.api.dto.Movie;
import com.minified.movierama.base.AbstractMovieRamaTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Testing appropriate behavior of {@code EhCacheController}.
 * 
 * @author niko.strongioglou
 *
 */
public class EhCacheControllerTests extends AbstractMovieRamaTest {

	@Autowired
	private CacheManager cacheManager;

	MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webAppContext;

	@Before
	public void setup() throws Exception {
		super.setup();

		Cache moviesCache = cacheManager.getCache("moviesCache");

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

		MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/ehcache/inspect"))
				.andDo(MockMvcResultHandlers.print())
				.andReturn();

		String ehCacheContents = response.getResponse().getContentAsString();
		Assert.assertTrue(ehCacheContents.contains("title=the godfather"));
		Assert.assertTrue(ehCacheContents.contains("description=the coolest movie"));
		Assert.assertTrue(ehCacheContents.contains("numberOfReviews=123"));
		Assert.assertTrue(ehCacheContents.contains("productionYear=1972"));
		Assert.assertTrue(ehCacheContents.contains("actors=[Marlon Brando, Al Pacino]"));

	}

	@Test
	public void testClear() throws Exception {

		MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/ehcache/clear"))
				.andDo(MockMvcResultHandlers.print())
				.andReturn();

		String ehCacheContents = response.getResponse().getContentAsString();
		Assert.assertFalse(ehCacheContents.contains("name=the godfather"));
		Assert.assertFalse(ehCacheContents.contains("description=the coolest movie"));
		Assert.assertFalse(ehCacheContents.contains("numberOfReviews=123"));
		Assert.assertFalse(ehCacheContents.contains("1972"));
		Assert.assertFalse(ehCacheContents.contains("actors=[Marlon Brando, Al Pacino]"));

	}
}
