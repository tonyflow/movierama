package com.minified.movierama.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.minified.movierama.api.RottenTomatoesService;
import com.minified.movierama.api.dto.Movie;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.minified.movierama.api.MovieDbService;
import com.minified.movierama.api.dto.CompositeId;
import com.minified.movierama.base.AbstractMovieRamaTest;

/**
 * Tests verifying the correct behavior of MovieRama's main management service
 * implementation: {@code MovieRamaServiceImpl}
 * 
 * @author niko.strongioglou
 *
 */
public class MovieRamaServiceTests extends AbstractMovieRamaTest {

	@MockBean
	private RottenTomatoesService mockRottenTomatoesService;

	@MockBean
	private MovieDbService mockMovieDbService;

	@Autowired
	@InjectMocks
	private MovieRamaService adminService;

	private final String[] ACTORS = new String[] { "Marlon Brando", "Al Pacino",
			"James Caan", "Richard S. Castellano",
			"Robert Duvall" };

	@Before
	public void setup() throws Exception {
		super.setup();
		MockitoAnnotations.initMocks(getClass());
	}

	/**
	 * Test which combines data from both APIs to produce a search() response.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSearchMovieNormal() throws Exception {

		// -- Mock RT Response
		Movie rtMovieStub = new Movie(new CompositeId(123l
				, null),
				"The Godfather",
				"this is a very small description",
				20l,
				1972,
				Arrays.asList("Marlon Brando", "Al Pacino",
						"James Caan", "Richard S. Castellano",
						"Robert Duvall"));

		Mockito.stub(mockRottenTomatoesService.getMovie(Mockito.eq("the godfather")))
				.toReturn(rtMovieStub);

		// -- Mock MDb Response
		Movie mdbMovieStub = new Movie(new CompositeId(null
				, 345l),
				"The Godfather",
				"this is obviously a larger description",
				1l,
				1972,
				null);

		Mockito.stub(mockMovieDbService.getMovie(Mockito.eq("the godfather")))
				.toReturn(mdbMovieStub);

		Movie result = adminService.search("the godfather");

		Assert.assertEquals("The Godfather", result.getTitle());
		Assert.assertEquals("this is obviously a larger description",
				result.getDescription());
		Assert.assertEquals(1972, result.getProductionYear());
		Assert.assertEquals(Long.valueOf(21), result.getNumberOfReviews());

		for (String actor : result.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf(ACTORS));
		}

		Assert.assertTrue(isCached("the godfather"));

	}

	/**
	 * Testing algorithms behavior on empty response from Movie Db API.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSearchMovieEmptyMDbResponse() throws Exception {

		// -- Mock RT Response
		Movie rtMovieStub = new Movie(new CompositeId(123l
				, null),
				"The Godfather",
				"this is a very small description",
				20l,
				1972,
				Arrays.asList("Marlon Brando", "Al Pacino",
						"James Caan", "Richard S. Castellano",
						"Robert Duvall"));

		Mockito.stub(mockRottenTomatoesService.getMovie(Mockito.eq("the godfather")))
				.toReturn(rtMovieStub);

		// -- Mock MDb Response
		Mockito.stub(mockMovieDbService.getMovie(Mockito.eq("the godfather")))
				.toReturn(null);

		Movie result = adminService.search("the godfather");

		Assert.assertEquals("The Godfather", result.getTitle());
		Assert.assertEquals("this is a very small description",
				result.getDescription());
		Assert.assertEquals(1972, result.getProductionYear());
		Assert.assertEquals(Long.valueOf(20), result.getNumberOfReviews());

		for (String actor : result.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf(ACTORS));
		}

	}

	/**
	 * Testing algorithms behavior on empty response from Rotten Tomatoes API.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSearchMovieEmptyRTResponse() throws Exception {

		// -- Mock RT Response

		Mockito.stub(mockRottenTomatoesService.getMovie(Mockito.eq("the godfather")))
				.toReturn(null);

		// -- Mock MDb Response
		Movie mdbMovieStub = new Movie(new CompositeId(null
				, 345l),
				"The Godfather",
				"this is obviously a larger description",
				1l,
				1972,
				Arrays.asList(ACTORS));

		Mockito.stub(mockMovieDbService.getMovie(Mockito.eq("the godfather")))
				.toReturn(mdbMovieStub);

		Movie result = adminService.search("the godfather");

		Assert.assertEquals("The Godfather", result.getTitle());
		Assert.assertEquals("this is obviously a larger description",
				result.getDescription());
		Assert.assertEquals(1972, result.getProductionYear());
		Assert.assertEquals(Long.valueOf(1), result.getNumberOfReviews());

		for (String actor : result.getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf(ACTORS));
		}

	}

	/**
	 * Returning an empty list when movie was not found on neither Movie Db nor
	 * Rotten Tomatoes. Remember that algorithm uses EXACT string matching and
	 * not approximate.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSearchMovieNotFound() throws Exception {

		// -- Mock RT Response
		Mockito.stub(mockRottenTomatoesService.getMovie(Mockito.eq("the godfather")))
				.toReturn(null);

		// -- Mock MDb Response
		Mockito.stub(mockMovieDbService.getMovie(Mockito.eq("the godfather")))
				.toReturn(null);

		Movie result = adminService.search("the godfather");

		Assert.assertNull(result);

	}

	/**
	 * Test which combines data from both APIs to produce a list latest movies
	 * in theaters response.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testListLatestNormal() throws Exception {

		// --Mock RT Response
		Map<String, Movie> rtLatestStub = new HashMap<String, Movie>();
		rtLatestStub.put("the magnificent seven", new Movie(new CompositeId(234l,
				null),
				"The Magnificent Seven",
				"smallig",
				2l,
				2016,
				Arrays.asList("Denzel Washington", "Chris Pratt",
						"Ethan Hawke", "Vincent D'Onofrio",
						"Lee Byung-hun")));

		rtLatestStub.put("miss pelegrine's home for peculiar children", new Movie(new CompositeId(1234l,
				null),
				"Miss Peregrine's Home for Peculiar Children",
				"miss pelegrine description",
				2l,
				2016,
				Arrays.asList("Eva Green", "Asa Butterfield",
						"Chris O'Dowd", "Allison Janney",
						"Rupert Everett")));

		Mockito.stub(mockRottenTomatoesService.listLatestMovies())
				.toReturn(rtLatestStub);

		// --Mock MDb Response
		Map<String, Movie> mdbLatestStub = new HashMap<String, Movie>();
		mdbLatestStub.put("the magnificent seven", new Movie(new CompositeId(null,
				123l),
				"The Magnificent Seven",
				"small",
				99l,
				2016,
				null));

		Mockito.stub(mockMovieDbService.listLatestMovies())
				.toReturn(mdbLatestStub);

		List<Movie> result = adminService.latest();

		Assert.assertEquals("Miss Peregrine's Home for Peculiar Children", result.get(0).getTitle());
		Assert.assertEquals("miss pelegrine description", result.get(0).getDescription());
		Assert.assertEquals(Long.valueOf(2), result.get(0).getNumberOfReviews());
		Assert.assertEquals(2016, result.get(0).getProductionYear());

		for (String actor : result.get(0).getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf("Eva Green", "Asa Butterfield",
					"Chris O'Dowd", "Allison Janney",
					"Rupert Everett"));

		}

		Assert.assertEquals("The Magnificent Seven", result.get(1).getTitle());
		Assert.assertEquals("smallig", result.get(1).getDescription());
		Assert.assertEquals(Long.valueOf(101), result.get(1).getNumberOfReviews());
		Assert.assertEquals(2016, result.get(1).getProductionYear());

		for (String actor : result.get(1).getActors()) {
			Assert.assertThat(actor, Matchers.isOneOf("Denzel Washington", "Chris Pratt",
					"Ethan Hawke", "Vincent D'Onofrio",
					"Lee Byung-hun"));
		}

	}

}
