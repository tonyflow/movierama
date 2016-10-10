package com.workable.movierama.factory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.workable.movierama.api.MovieDbService;
import com.workable.movierama.api.MovieResourceService;
import com.workable.movierama.api.RottenTomatoesService;
import com.workable.movierama.base.AbstractMovieRamaTest;

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
