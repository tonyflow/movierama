package com.workable.movierama.base;

import java.lang.reflect.Method;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workable.movierama.MovieRamaApplication;
import com.workable.movierama.support.EhCacheUtils;

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
		EhCacheUtils.inspectCache();

	}

	/**
	 * Was our movie actually put in cache.
	 * 
	 * @return
	 */
	protected boolean isCached(String title) {
		CacheManager manager = CacheManager.getInstance();
		Ehcache mc = manager.getEhcache("moviesCache");

		for (Object key : mc.getKeys()) {
			Element e = mc.getQuiet(key);

			Object o = e.getObjectValue();

			Class<? extends Object> clazz = o.getClass();

			try {
				Method m = clazz.getDeclaredMethod("get");
				Object movieDto = m.invoke(o, 0);
				clazz = o.getClass();

				return movieDto != null && clazz.getDeclaredField("title").equals(title);
			} catch (Exception e1) {
			}
		}

		return true;
	}

}
