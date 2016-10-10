package com.workable.movierama.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workable.movierama.api.dto.CompositeId;
import com.workable.movierama.api.dto.Movie;

/**
 * Implements all {@code MovieResourceService} interface's methods for
 * extracting movie data via the MovieDb API. listLatestMovies, getMovie and
 * retrieveMDReviewsAndBuild methods are all fault tolerant to any unexpected
 * exceptions produced by the Movie Db API (e.g. server unavailability). The
 * data retrieval flow trusts that at least one of the configured data sources
 * or the cache will produce results.
 * 
 * @author niko.strongioglou
 *
 */
@Component
public class MovieDbService implements MovieResourceService {

	Logger LOGGER = LoggerFactory.getLogger(MovieDbService.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Value("${application.the-movie-db-api-key}")
	private String theMovieDbApiKey;

	private final Integer MDB_PAGING = 1;

	private final int MAX_CAST = 5;

	private final String LANGUAGE = "en-US";

	public static String MOVIE_DB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie?"
			+ "page={page}&"
			+ "query={title}&"
			+ "language={language}&"
			+ "api_key={api_key}";

	public static String MOVIE_DB_LATEST_URL = "https://api.themoviedb.org/3/movie/now_playing?"
			+ "api_key={api_key}&"
			+ "language={language}";

	public static String MOVIE_DB_REVIEWS_URL = "https://api.themoviedb.org/3/movie/{movie_id}/reviews?"
			+ "api_key={api_key}&"
			+ "language={language}";

	public static String MOVIE_DB_CREDITS_URL = "https://api.themoviedb.org/3/movie/{movie_id}/credits?"
			+ "api_key={api_key}";

	@Override
	public String getResourceName() {

		return "The Movie Db";
	}

	@Override
	public Map<String, Movie> listLatestMovies() {

		LOGGER.debug("Retrieving latest movies from Movie Db");

		Map<String, Movie> movies = new HashMap<String, Movie>();

		try {
			String mdb = restTemplate.getForObject(MOVIE_DB_LATEST_URL,
					String.class,
					theMovieDbApiKey,
					LANGUAGE);

			JsonNode mdbResults = mapper.readTree(mdb).get("results");

			for (JsonNode movie : mdbResults) {

				retrieveMDReviewsAndBuild(movies, movie);

			}

		} catch (Exception e) {
			LOGGER.error("Error while building Movie Db response"
					+ "for the latest movies in theaters", e);
		}

		return movies;
	}

	@Override
	public Movie getMovie(String title) {

		LOGGER.debug("Retrieving movie data from Movie Db for title " + title);

		try {

			Map<String, Movie> movies = new HashMap<String, Movie>();

			String mdb = restTemplate.getForObject(MOVIE_DB_SEARCH_URL,
					String.class,
					String.valueOf(MDB_PAGING),
					title,
					LANGUAGE,
					theMovieDbApiKey);

			JsonNode mdbResults = mapper.readTree(mdb).get("results");

			for (JsonNode movie : mdbResults) {

				if (movie.get("original_title").asText().equalsIgnoreCase(title)) {

					retrieveMDReviewsAndBuild(movies, movie);

					return movies.get(title.toLowerCase());
				}

			}
		} catch (Exception e) {
			LOGGER.error("Error while building Movie Db response for movie " + title, e);
		}

		return null;
	}

	private void retrieveMDReviewsAndBuild(Map<String, Movie> movies, JsonNode movie) throws JsonProcessingException, IOException {

		LOGGER.debug("Retrieving movie reviews from Movie Db for title " + movie.get("original_title").asText());

		try {
			Movie m = new Movie(
					new CompositeId(null,
							movie.get("id").asLong()),
					movie.get("original_title").asText(),
					movie.get("overview").asText(),
					0l,
					LocalDate.parse(movie.get("release_date").asText()).getYear(),
					retrieveActors(movie.get("id").asText()));

			movies.put(movie.get("title").asText().toLowerCase(), m);

			String mdbReviews = restTemplate.getForObject(MOVIE_DB_REVIEWS_URL,
					String.class,
					movie.get("id").asText(),
					theMovieDbApiKey,
					LANGUAGE);

			Long dbReviews = mapper.readTree(mdbReviews).get("total_results").asLong();

			movies.get(movie.get("title").asText().toLowerCase()).addReviews(dbReviews);

		} catch (Exception e) {
			LOGGER.error("Error while retrieving reviews from Movie Db for movie " + movie.get("id")
					+ " will continue with the rest of movies", e);
		}

	}

	/**
	 * Used to retrieved actors from the Movie Db API. The Movie Db API has a
	 * rate limit and thus was not preferred to initially fetch movie cast. Only
	 * cast not having been retrieved by the rotten tomatoes API will be queried
	 * by the Movie Db API.
	 * 
	 * @param movieId
	 * @return
	 */
	public List<String> retrieveActors(String movieId) {

		LOGGER.debug("Retrieving actor data from Movie Db. Movie id : " + movieId);

		ArrayList<String> al = new ArrayList<String>();

		try {

			String mdbActorsResults = restTemplate.getForObject(MOVIE_DB_CREDITS_URL, String.class,
					movieId,
					theMovieDbApiKey);

			JsonNode actors = mapper.readTree(mdbActorsResults).get("cast");
			int max = actors.size() < MAX_CAST ? actors.size() : 5;
			for (int i = 0; i < max; i++) {
				al.add(actors.get(i).get("name").asText());
			}

		} catch (IOException e) {
			LOGGER.error("Error while trying to retrive cast from Movie Db");
		}

		return al;
	}

}
