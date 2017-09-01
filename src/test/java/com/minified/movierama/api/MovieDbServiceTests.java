package com.minified.movierama.api;

import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import com.minified.movierama.api.dto.Movie;
import com.minified.movierama.base.AbstractMovieRamaTest;

/**
 * Both tests in this class contain happy paths for Movie Db service. Exception
 * cases for {@code MovieResourceService} can be found in
 * {@code RottenTomatoesServiceTests}.
 * 
 * @author niko.strongioglou
 *
 */
public class MovieDbServiceTests extends AbstractMovieRamaTest {

	@Autowired
	private MovieDbService movieDbService;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	private final String[] MDB_MOVIES = new String[] { "333484" };

	@Before
	public void setup() throws Exception {
		super.setup();
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}

	/**
	 * Testing happy path.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetMovieNormal() throws Exception {

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

		// -- Mock Credits ----------
		String mdbCreditsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-credits.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/238/credits?"
				+ "api_key=" + theMovieDbApiKey))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(mdbCreditsResponse, MediaType.APPLICATION_JSON_UTF8));

		// --- Mock Reviews ----------
		String mdbReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-reviews.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/238/reviews?"
				+ "api_key=" + theMovieDbApiKey + "&"
				+ "language=en-US"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(mdbReviewsResponse, MediaType.APPLICATION_JSON_UTF8));

		// -- Actual Service Invocation
		Movie m = movieDbService.getMovie("the godfather");

		// -- Assertions
		Assert.assertEquals("The Godfather", m.getTitle());
		Assert.assertEquals("The story spans the years from 1945 to 1955 and"
				+ " chronicles the fictional Italian-American Corleone crime family."
				+ " When organized crime family patriarch Vito Corleone barely"
				+ " survives an attempt on his life, his youngest son, Michael,"
				+ " steps in to take care of the would-be killers, launching a"
				+ " campaign of bloody revenge.", m.getDescription());
		Assert.assertEquals(Long.valueOf(1), m.getNumberOfReviews());
		Assert.assertEquals(1972, m.getProductionYear());
		for (String actor : m.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf("Marlon Brando", "Al Pacino",
					"James Caan", "Richard S. Castellano",
					"Robert Duvall"));
		}
	}

	/**
	 * Testing happy path.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListLatestNormal() throws Exception {

		// MOVIE DB mocks
		// -- Mock Now Playing -----------
		String mdbNowPlayingResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-now-playing.json"));

		mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/now_playing?"
				+ "api_key=" + theMovieDbApiKey + "&"
				+ "language=en-US"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(mdbNowPlayingResponse, MediaType.APPLICATION_JSON_UTF8));

		String mdbReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-reviews.json"));

		// -- Mock Cast For Each Movie
		String mdbCreditsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/movie-db-credits.json"));

		for (String movieId : MDB_MOVIES) {
			mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/" + movieId + "/credits?"
					+ "api_key=" + theMovieDbApiKey))
					.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
					.andRespond(MockRestResponseCreators.withSuccess(mdbCreditsResponse, MediaType.APPLICATION_JSON_UTF8));

		}

		// --Mock Review For Each Movie
		for (String movieId : MDB_MOVIES) {
			mockServer.expect(MockRestRequestMatchers.requestTo("https://api.themoviedb.org/3/movie/" + movieId + "/reviews?"
					+ "api_key=" + theMovieDbApiKey + "&"
					+ "language=en-US"))
					.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
					.andRespond(MockRestResponseCreators.withSuccess(mdbReviewsResponse, MediaType.APPLICATION_JSON_UTF8));

		}

		// -- Actual Service Invocation
		Map<String, Movie> result = movieDbService.listLatestMovies();

		System.out.println(testMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

		// -- Assertions
		Movie magnificent = result.get("the magnificent seven");
		Assert.assertEquals("The Magnificent Seven", magnificent.getTitle());
		Assert.assertNotNull(magnificent.getDescription());
		Assert.assertEquals(Long.valueOf(1), magnificent.getNumberOfReviews());
		Assert.assertEquals(2016, magnificent.getProductionYear());

		// the cast is't actually magnificent seven but kind of an overkill to
		// use different cast responses for testing purposes
		for (String actor : magnificent.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf("Marlon Brando", "Al Pacino",
					"James Caan", "Richard S. Castellano",
					"Robert Duvall"));
		}
	}
}
