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

public class RottenTomatoesServiceTests extends AbstractMovieRamaTest {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RottenTomatoesService rottenTomatoesService;

	private MockRestServiceServer mockServer;

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
	 * Testing happy path.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetMovieNormal() throws Exception {

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

		// -- Actual Service Invocation
		Movie m = rottenTomatoesService.getMovie("the godfather");

		// -- Assertions
		Assert.assertEquals("The Godfather", m.getTitle());
		Assert.assertEquals("", m.getDescription());
		Assert.assertEquals(Long.valueOf(20), m.getNumberOfReviews());
		Assert.assertEquals(1972, m.getProductionYear());

		for (String actor : m.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf(ACTORS));
		}
	}

	/**
	 * In case Rotten Tomatoes Search API produces an {@code Exception},
	 * getMovie(title) must still return a null value so that the merge
	 * algorithm can continue its flow properly. Maybe another movie resource
	 * can produce a valid response.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetMovieException() throws Exception {

		// ROTTEN TOMATOES
		// --- Mock Search ----------

		mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/movies.json?"
				+ "apikey=" + rottenTomatoesApiKey + "&"
				+ "q=the%20godfather&"
				+ "page_limit=1"))
				.andRespond(MockRestResponseCreators.withBadRequest());

		// -- Actual Service Invocation
		Movie m = rottenTomatoesService.getMovie("the godfather");

		Assert.assertNull(m);

	}

	/**
	 * In case Rotten Tomatoes Reviews API produces an {@code Exception},
	 * getMovie(title) must still return a concrete movie value so that the
	 * merge algorithm can continue its flow properly. The effect of such an
	 * error will be a movies' map entry with 0 movie reviews.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetMovieReviewsException() throws Exception {

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
		mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/movies/12911/reviews.json?"
				+ "apikey=" + rottenTomatoesApiKey))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withBadRequest());

		// -- Actual Service Invocation
		Movie m = rottenTomatoesService.getMovie("the godfather");

		// -- Assertions
		Assert.assertEquals("The Godfather", m.getTitle());
		Assert.assertEquals("", m.getDescription());
		Assert.assertEquals(Long.valueOf(0), m.getNumberOfReviews());
		Assert.assertEquals(1972, m.getProductionYear());

		for (String actor : m.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf(ACTORS));
		}
	}

	/**
	 * Happy path.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLatestNormal() throws Exception {

		// ROTTEN TOMATOES mocks
		// -- Mock Now Playing -----------
		String rtmNowPlayingResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-now-playing.json"));
		mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?"
				+ "apikey=" + rottenTomatoesApiKey + "&"
				+ "page_limit=10"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(rtmNowPlayingResponse, MediaType.APPLICATION_JSON_UTF8));

		String rtmReviewsResponse = IOUtils.toString(this.getClass().getResourceAsStream("/mock-responses/rotten-tomatoes-reviews.json"));

		// -- Mock Reviews -----------
		for (String movieId : RTM_MOVIES) {
			mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/movies/" + movieId + "/reviews.json?"
					+ "apikey=" + rottenTomatoesApiKey))
					.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
					.andRespond(MockRestResponseCreators.withSuccess(rtmReviewsResponse, MediaType.APPLICATION_JSON_UTF8));

		}

		// -- Actual Service Invocation
		Map<String, Movie> result = rottenTomatoesService.listLatestMovies();

		System.out.println(testMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

		Movie miss = result.get("miss peregrine's home for peculiar children");
		Movie magnificent = result.get("the magnificent seven");
		// -- Assertions
		Assert.assertEquals("Miss Peregrine's Home for Peculiar Children", miss.getTitle());
		Assert.assertNotNull(miss.getDescription());
		Assert.assertEquals(Long.valueOf(20), miss.getNumberOfReviews());
		Assert.assertEquals(2016, miss.getProductionYear());

		for (String actor : miss.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf("Eva Green", "Asa Butterfield",
					"Chris O'Dowd", "Allison Janney",
					"Rupert Everett"));

		}

		Assert.assertEquals("The Magnificent Seven", magnificent.getTitle());
		Assert.assertNotNull(magnificent.getDescription());
		Assert.assertEquals(Long.valueOf(20), magnificent.getNumberOfReviews());
		Assert.assertEquals(2016, magnificent.getProductionYear());

		for (String actor : magnificent.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf("Denzel Washington", "Chris Pratt",
					"Ethan Hawke", "Vincent D'Onofrio",
					"Lee Byung-hun"));
		}
	}

	/**
	 * 
	 * In case Rotten Tomatoes Now Playing API produces an {@code Exception},
	 * listLatest() must still return an empty map value so that the merge
	 * algorithm can continue its flow properly. Maybe another movie resource
	 * can produce a valid response.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLatestException() throws Exception {

		// ROTTEN TOMATOES mocks
		// -- Mock Now Playing -----------
		mockServer.expect(MockRestRequestMatchers.requestTo("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?"
				+ "apikey=" + rottenTomatoesApiKey + "&"
				+ "page_limit=10"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withBadRequest());

		// -- Actual Service Invocation
		Map<String, Movie> movies = rottenTomatoesService.listLatestMovies();

		Assert.assertTrue(movies.isEmpty());

	}

}
