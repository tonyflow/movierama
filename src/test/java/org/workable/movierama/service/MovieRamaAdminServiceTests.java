package org.workable.movierama.service;

import static org.workable.movierama.api.UrlConstants.MOVIE_DB_LATEST_URL;
import static org.workable.movierama.api.UrlConstants.MOVIE_DB_REVIEWS_URL;
import static org.workable.movierama.api.UrlConstants.MOVIE_DB_SEARCH_URL;
import static org.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_LATEST_URL;
import static org.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_REVIEWS_URL;
import static org.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_SEARCH_URL;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.workable.movierama.api.dto.MovieDto;
import org.workable.movierama.base.AbstractMovieRamaTest;

public class MovieRamaAdminServiceTests extends AbstractMovieRamaTest {

	@Mock
	private RestTemplate mockRestTemplate;

	@Autowired
	@InjectMocks
	private MovieRamaAdminService adminService;

	@Before
	public void setup() throws Exception {

		super.setup();

		MockitoAnnotations.initMocks(this);

		// MOVIE DB MOCKINGS
		String mdbSearchResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-search.json"));

		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(MOVIE_DB_SEARCH_URL),
				Mockito.eq(String.class),// string class
				Mockito.eq("1"),
				Mockito.eq("the godfather"),
				Mockito.eq("en-US"),
				Mockito.eq(theMovieDbApiKey)))
				.thenReturn(mdbSearchResponse);

		String mdbReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-reviews.json"));

		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(MOVIE_DB_REVIEWS_URL),
				Mockito.eq(String.class),// string class
				Mockito.eq("238"),
				Mockito.eq(theMovieDbApiKey),
				Mockito.eq("en-US")))
				.thenReturn(mdbReviewsResponse);

		// ---for the now playing version
		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(MOVIE_DB_REVIEWS_URL),
				Mockito.eq(String.class),// string class
				Mockito.any(),
				Mockito.eq(theMovieDbApiKey),
				Mockito.any()))
				.thenReturn(mdbReviewsResponse);

		String mdbNowPlayingResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-now-playing.json"));

		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(MOVIE_DB_LATEST_URL),
				Mockito.eq(String.class),// string class
				Mockito.eq(theMovieDbApiKey),
				Mockito.eq("en-US")))
				.thenReturn(mdbNowPlayingResponse);

		// ROTTEN TOMATOES MOCKINGS
		String rtmSearchResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-search.json"));

		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(ROTTEN_TOMATOES_SEARCH_URL),
				Mockito.eq(String.class), // string class
				Mockito.eq(rottenTomatoesApiKey),
				Mockito.eq("the godfather"),
				Mockito.eq("1")))
				.thenReturn(rtmSearchResponse);

		String rtmReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-reviews.json"));

		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(ROTTEN_TOMATOES_REVIEWS_URL),
				Mockito.eq(String.class), // string class
				Mockito.eq("12911"),
				Mockito.eq(rottenTomatoesApiKey)))
				.thenReturn(rtmReviewsResponse);

		// ---for the now playing version
		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(ROTTEN_TOMATOES_REVIEWS_URL),
				Mockito.eq(String.class), // string class
				Mockito.any(), // movie id
				Mockito.eq(rottenTomatoesApiKey)))
				.thenReturn(rtmReviewsResponse);

		String rtmNowPlayingResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-now-playing.json"));

		Mockito.when(mockRestTemplate.getForObject(Mockito.eq(ROTTEN_TOMATOES_LATEST_URL),
				Mockito.eq(String.class), // string class
				Mockito.eq(rottenTomatoesApiKey),
				Mockito.any()))// page limit
				.thenReturn(rtmNowPlayingResponse);

	}

	/**
	 * Testing functionality when a movie title is provided. Http response
	 * should provide a list of one page of size including titles whose name
	 * matches the most the query term.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListMovie() throws Exception {

		List<MovieDto> result = adminService.list("the godfather");

		System.out.println(testMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	/**
	 * Testing results of the list service when no movie title is provided.
	 * Service should provide a list of movies playing now on theaters.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListLatest() throws Exception {

		List<MovieDto> result = adminService.list("");

		System.out.println(testMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}
}
