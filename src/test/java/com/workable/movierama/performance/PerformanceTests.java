package com.workable.movierama.performance;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.workable.movierama.base.AbstractMovieRamaTest;
import com.workable.movierama.service.MovieRamaAdminService;

public class PerformanceTests extends AbstractMovieRamaTest {

	@Autowired
	private MovieRamaAdminService adminService;

	private final String[] MOVIES = new String[] {};

	@Before
	public void setup() throws Exception {
		super.setup();
	}

	@Test
	public void testHammer() throws Exception {

	}

}
