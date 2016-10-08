package com.workable.movierama.api;

import static com.workable.movierama.api.UrlConstants.MOVIE_DB_CREDITS_URL;
import static com.workable.movierama.api.UrlConstants.MOVIE_DB_LATEST_URL;
import static com.workable.movierama.api.UrlConstants.MOVIE_DB_REVIEWS_URL;
import static com.workable.movierama.api.UrlConstants.MOVIE_DB_SEARCH_URL;

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
 * Implements all MovieResourceService interface's method for extracting movie
 * data via the MovieDB API.
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

	private final String LANGUAGE = "en-US";

	@Override
	public String getResourceName() {

		return "The Movie Db";
	}

	@Override
	public Map<String, Movie> listLatestMovies() {

		Map<String, Movie> movies = new HashMap<String, Movie>();

		String mdb = restTemplate.getForObject(MOVIE_DB_LATEST_URL,
				String.class,
				theMovieDbApiKey,
				LANGUAGE);

		try {
			JsonNode mdbResults = mapper.readTree(mdb).get("results");

			for (JsonNode movie : mdbResults) {

				retrieveMDReviewsAndBuild(movies, movie);

			}

		} catch (Exception e) {

		}

		return movies;
	}

	@Override
	public Movie getMovie(String title) {

		Map<String, Movie> movies = new HashMap<String, Movie>();

		String mdb = restTemplate.getForObject(MOVIE_DB_SEARCH_URL,
				String.class,
				String.valueOf(MDB_PAGING),
				title,
				LANGUAGE,
				theMovieDbApiKey);

		try {
			JsonNode mdbResults = mapper.readTree(mdb).get("results");

			for (JsonNode movie : mdbResults) {

				if (movie.get("original_title").asText().equalsIgnoreCase(title)) {

					retrieveMDReviewsAndBuild(movies, movie);

					return movies.get(title.toLowerCase());
				}

			}
		} catch (Exception e) {

		}

		return null;
	}

	private void retrieveMDReviewsAndBuild(Map<String, Movie> movies, JsonNode movie) throws JsonProcessingException, IOException {

		String mdbReviews = restTemplate.getForObject(MOVIE_DB_REVIEWS_URL,
				String.class,
				movie.get("id").asText(),
				theMovieDbApiKey,
				LANGUAGE);

		Long dbReviews = mapper.readTree(mdbReviews).get("total_results").asLong();

		Movie m = new Movie(
				new CompositeId(null,
						movie.get("id").asLong()),
				movie.get("original_title").asText(),
				movie.get("overview").asText(),
				dbReviews,
				LocalDate.parse(movie.get("release_date").asText()).getYear(),
				null);
		movies.put(movie.get("title").asText().toLowerCase(), m);
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

		LOGGER.info("Retrieving actor data for movie with id " + movieId);

		ArrayList<String> al = new ArrayList<String>();

		String mdbActorsResults = restTemplate.getForObject(MOVIE_DB_CREDITS_URL, String.class,
				movieId,
				theMovieDbApiKey);

		try {
			JsonNode actors = mapper.readTree(mdbActorsResults).get("cast");
			int max = actors.size() < 5 ? actors.size() : 5;
			for (int i = 0; i < max; i++) {
				al.add(actors.get(i).get("name").asText());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return al;
	}

}
