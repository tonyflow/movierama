package com.workable.movierama.service;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import com.workable.movierama.api.dto.MovieDto;
import com.workable.movierama.base.AbstractMovieRamaTest;
import com.workable.movierama.service.MovieRamaAdminService;

public class MovieRamaAdminServiceTests extends AbstractMovieRamaTest {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MovieRamaAdminService adminService;

	@Value("${application.the-movie-db-api-key}")
	private String theMovieDbApiKey;

	@Value("${application.rotter-tomatoes-api-key}")
	private String rottenTomatoesApiKey;

	private MockRestServiceServer mockServer;

	private final String[] MDB_MOVIES = new String[] { "333484" };

	private final String[] RTM_MOVIES = new String[] { "771359360", "771407527" };

	private final String[] ACTORS = new String[] { "Marlon Brando", "Al Pacino",
			"James Caan", "Richard S. Castellano",
			"Robert Duvall" };

	@Before
	public void setup() throws Exception {
		super.setup();
		mockServer = MockRestServiceServer.createServer(restTemplate);
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

		// ROTTEN TOMATOES
		// --- Mock Search ----------
		String rtmSearchResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-search.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/movies.json?"
				+ "apikey=" + rottenTomatoesApiKey + "&"
				+ "q=the%20godfather&"
				+ "page_limit=1"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(rtmSearchResponse, MediaType.APPLICATION_JSON_UTF8));

		// --- Mock Reviews ----------
		String rtmReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-reviews.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/movies/12911/reviews.json?"
				+ "apikey=" + rottenTomatoesApiKey))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(rtmReviewsResponse, MediaType.APPLICATION_JSON_UTF8));

		// MOVIE DB mocks
		// --- Mock Search ----------
		String mdbSearchResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-search.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/search/movie?"
				+ "page=1&"
				+ "query=the%20godfather&"
				+ "language=en-US&"
				+ "api_key=" + theMovieDbApiKey))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(mdbSearchResponse, MediaType.APPLICATION_JSON_UTF8));

		String mdbReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-reviews.json"));

		// --- Mock Reviews ----------
		mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/238/reviews?"
				+ "api_key=" + theMovieDbApiKey + "&"
				+ "language=en-US"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(mdbReviewsResponse, MediaType.APPLICATION_JSON_UTF8));

		// -- Mock Credits ----------
		String mdbCreditsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-credits.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/238/credits?"
				+ "api_key=" + theMovieDbApiKey))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(mdbCreditsResponse, MediaType.APPLICATION_JSON_UTF8));

		List<MovieDto> result = adminService.list("the godfather");

		System.out.println(testMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

		Assert.assertEquals("The Godfather", result.get(0).getTitle());
		Assert.assertEquals("The story spans the years from 1945 to 1955 and chronicles the fictional"
				+ " Italian-American Corleone crime family. When organized crime family patriarch "
				+ "Vito Corleone barely survives an attempt on his life, his youngest son, Michael,"
				+ " steps in to take care of the would-be killers, launching a campaign of bloody "
				+ "revenge.",
				result.get(0).getDescription());
		Assert.assertEquals(1972, result.get(0).getProductionYear());
		Assert.assertEquals(Long.valueOf(21), result.get(0).getNumberOfReviews());
		for (String actor : result.get(0).getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf(ACTORS));
		}

		Assert.assertTrue(isCached("the godfather"));
	}

	/**
	 * Testing results of the list service when no movie title is provided.
	 * Service should provide a list of movies playing now on theaters.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListLatest() throws Exception {

		// ROTTEN TOMATOES mocks
		// -- Mock Now Playing -----------
		String rtmNowPlayingResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-now-playing.json"));
		mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?"
				+ "apikey=" + rottenTomatoesApiKey + "&"
				+ "page_limit=20"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(rtmNowPlayingResponse, MediaType.APPLICATION_JSON_UTF8));

		String rtmReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-reviews.json"));

		for (String movieId : RTM_MOVIES) {
			mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/movies/" + movieId + "/reviews.json?"
					+ "apikey=" + rottenTomatoesApiKey))
					.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
					.andRespond(MockRestResponseCreators.withSuccess(rtmReviewsResponse, MediaType.APPLICATION_JSON_UTF8));

		}

		// MOVIE DB mocks
		// -- Mock Now Playing -----------
		String mdbNowPlayingResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-now-playing.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/now_playing?"
				+ "api_key=" + theMovieDbApiKey + "&"
				+ "language=en-US"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(mdbNowPlayingResponse, MediaType.APPLICATION_JSON_UTF8));

		String mdbReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-reviews.json"));

		// --Mock Review For Each Movie
		for (String movieId : MDB_MOVIES) {
			mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/" + movieId + "/reviews?"
					+ "api_key=" + theMovieDbApiKey + "&"
					+ "language=en-US"))
					.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
					.andRespond(MockRestResponseCreators.withSuccess(mdbReviewsResponse, MediaType.APPLICATION_JSON_UTF8));

		}

		// -- Mock Cast For Each Movie
		String mdbCreditsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-credits.json"));

		for (String movieId : MDB_MOVIES) {
			mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/" + movieId + "/credits?"
					+ "api_key=" + theMovieDbApiKey))
					.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
					.andRespond(MockRestResponseCreators.withSuccess(mdbCreditsResponse, MediaType.APPLICATION_JSON_UTF8));

		}

		List<MovieDto> result = adminService.list("");

		System.out.println(testMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

		String expected = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/assert-now-playing.json"));

		Assert.assertEquals(expected, testMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}
}
