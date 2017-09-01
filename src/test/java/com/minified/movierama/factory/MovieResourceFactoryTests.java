package com.minified.movierama.factory;

import com.minified.movierama.api.MovieDbService;
import com.minified.movierama.api.MovieResourceService;
import com.minified.movierama.api.RottenTomatoesService;
import com.minified.movierama.base.AbstractMovieRamaTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a test suite for verifying the appropriate initialization of our
 * {@code MovieResourceFactory}.
 * 
 * @author niko.strongioglou
 *
 */
public class MovieResourceFactoryTests extends AbstractMovieRamaTest {

	@Autowired
	private MovieResourceFactory movieResourceFactory;

	@Before
	public void setup() throws Exception {
		super.setup();
	}

	@Test
	public void testFactoryInitialization() throws Exception {

		MovieResourceService rtTesourceService = movieResourceFactory.getResourceService("rottenTomatoes");
		Assert.assertTrue(rtTesourceService instanceof RottenTomatoesService);

		MovieResourceService mdResourceService = movieResourceFactory.getResourceService("movieDb");
		Assert.assertTrue(mdResourceService instanceof MovieDbService);

	}
}
