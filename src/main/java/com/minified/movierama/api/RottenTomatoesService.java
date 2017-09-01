package com.minified.movierama.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.minified.movierama.api.dto.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minified.movierama.api.dto.CompositeId;

/**
 * Implements all {@code MovieResourceService} interface's methods for
 * extracting movie data via the Rotten tomatoes API. listLatestMovies, getMovie
 * and retrieveMDReviewsAndBuild methods are all fault tolerant to any
 * unexpected exceptions produced by the Rotten Tomatoes API (e.g. server
 * unavailability). The data retrieval flow trusts that at least one of the
 * configured data sources or the cache will produce results.
 * 
 * @author niko.strongioglou
 *
 */
@Component
public class RottenTomatoesService implements MovieResourceService {

	Logger LOGGER = LoggerFactory.getLogger(RottenTomatoesService.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Value("${application.rotter-tomatoes-api-key}")
	private String rottenTomatoesApiKey;

	private final String SEARCH_PAGING = "1";

	private final String NOW_PLAYING_PAGING = "10";

	public static String ROTTEN_TOMATOES_SEARCH_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?"
			+ "apikey={api_key}&"
			+ "q={title}&"
			+ "page_limit={page}";

	public static String ROTTEN_TOMATOES_LATEST_URL = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?"
			+ "apikey={api_key}&"
			+ "page_limit={page}";

	public static String ROTTEN_TOMATOES_REVIEWS_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies/{movie_id}/reviews.json?"
			+ "apikey={api_key}";

	@Override
	public String getResourceName() {
		return "Rotten Tomatoes";
	}

	@Override
	public Map<String, Movie> listLatestMovies() {

		LOGGER.info("Retrieving latest movies from Rotten Tomatoes");

		Map<String, Movie> movies = new HashMap<String, Movie>();

		try {
			String rtm = restTemplate.getForObject(ROTTEN_TOMATOES_LATEST_URL,
					String.class,
					rottenTomatoesApiKey,
					NOW_PLAYING_PAGING);

			JsonNode rmtResults = mapper.readTree(rtm).get("movies");

			for (JsonNode movie : rmtResults) {

				retrieveRTReviewsAndBuild(movies, movie);

			}
		} catch (Exception e) {
			LOGGER.error("Error while building Rotten Tomatoes response"
					+ "for the latest movies in theaters", e);
		}

		return movies;

	}

	@Override
	public Movie getMovie(String title) {

		LOGGER.debug("Retrieving movie data from Rotten Tomatoes for title " + title);

		try {

			Map<String, Movie> movies = new HashMap<String, Movie>();

			String rtm = restTemplate.getForObject(ROTTEN_TOMATOES_SEARCH_URL,
					String.class,
					rottenTomatoesApiKey,
					title,
					String.valueOf(SEARCH_PAGING));

			JsonNode rtmResults = mapper.readTree(rtm).get("movies");

			for (JsonNode movie : rtmResults) {
				if (movie.get("title").asText().equalsIgnoreCase(title)) {
					retrieveRTReviewsAndBuild(movies, movie);

					return movies.get(title.toLowerCase());
				}
			}

		} catch (Exception e) {
			LOGGER.error("Error while building Rotten Tomatoes response for movie " + title, e);
		}

		return null;
	}

	/**
	 * Will retrieve review from rotten tomatoes and produce the initial movie
	 * dto residing in movies map.
	 * 
	 * @param movies
	 * @param movie
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	private void retrieveRTReviewsAndBuild(Map<String, Movie> movies, JsonNode movie) {

		LOGGER.debug("Retrieving movie reviews from Rotten Tomatoes for title " + movie.get("title").asText());

		try {
			Movie m = new Movie(
					new CompositeId(movie.get("id").asLong(),
							null),
					movie.get("title").asText(),
					movie.get("synopsis").asText(),
					0l,
					LocalDate.parse(movie.get("release_dates").get("theater").asText()).getYear(),
					retrieveActors(movie.get("abridged_cast")));

			movies.put(movie.get("title").asText().toLowerCase(), m);

			String rtmReviews = restTemplate.getForObject(ROTTEN_TOMATOES_REVIEWS_URL,
					String.class,
					movie.get("id").asText(),
					rottenTomatoesApiKey);

			Long rottenReviews = mapper.readTree(rtmReviews).get("total").asLong();

			movies.get(movie.get("title").asText().toLowerCase()).addReviews(rottenReviews);

		} catch (Exception e) {
			LOGGER.error("Error while retrieving reviews from Rotten Tomatoes for movie " + movie.get("id")
					+ " .Control flow will continue with the rest of movies", e);
		}
	}

	/**
	 * Used to parse actors from the "abridged_cast" element of rotten tomatoes'
	 * search response API call.
	 * 
	 * @param actors
	 * @return
	 */
	private List<String> retrieveActors(JsonNode actors) {

		LOGGER.debug("Retrieving actor data from Rotten Tomatoes Search API's response");

		ArrayList<String> al = new ArrayList<String>();

		for (JsonNode a : actors) {
			al.add(a.get("name").asText());
		}

		return al;
	}

}
