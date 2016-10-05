package org.workable.movierama.base;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.workable.movierama.MovieRamaApplication;
import org.workable.movierama.support.EhCacheUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { MovieRamaApplication.class })
public class AbstractMovieRamaTest {

	@Autowired
	public ObjectMapper testMapper;

	@Value("${application.the-movie-db-api-key}")
	public String theMovieDbApiKey;

	@Value("${application.rotter-tomatoes-api-key}")
	public String rottenTomatoesApiKey;

	@Before
	public void setup() throws Exception {

		EhCacheUtils.inspectCache();

		EhCacheUtils.clearCache();
	}

	@After
	public void tearDown() {
		System.out.println("================================");
		System.out.println("================================");
		System.out.println("================================");
		EhCacheUtils.inspectCache();

	}

}
