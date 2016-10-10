package com.workable.movierama.base;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workable.movierama.MovieRamaApplication;
import com.workable.movierama.support.EhCacheUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { MovieRamaApplication.class },
		webEnvironment = WebEnvironment.MOCK)
public abstract class AbstractMovieRamaTest {

	@Autowired
	public ObjectMapper testMapper;

	@Value("${application.the-movie-db-api-key}")
	public String theMovieDbApiKey;

	@Value("${application.rotter-tomatoes-api-key}")
	public String rottenTomatoesApiKey;

	@Before
	public void setup() throws Exception {
		EhCacheUtils.clearCache();
	}

	@After
	public void tearDown() {
		EhCacheUtils.clearCache();
	}

	/**
	 * Was our movie actually put in cache.
	 * 
	 * @return
	 */
	protected boolean isCached(String title) {
		CacheManager manager = CacheManager.getInstance();
		Ehcache mc = manager.getEhcache("moviesCache");

		return mc.get(title) != null;
	}

}
