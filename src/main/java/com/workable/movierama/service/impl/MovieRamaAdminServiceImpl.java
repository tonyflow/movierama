package com.workable.movierama.service.impl;

import static com.workable.movierama.api.UrlConstants.MOVIE_DB_CREDITS_URL;
import static com.workable.movierama.api.UrlConstants.MOVIE_DB_LATEST_URL;
import static com.workable.movierama.api.UrlConstants.MOVIE_DB_REVIEWS_URL;
import static com.workable.movierama.api.UrlConstants.MOVIE_DB_SEARCH_URL;
import static com.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_LATEST_URL;
import static com.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_REVIEWS_URL;
import static com.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_SEARCH_URL;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workable.movierama.api.dto.CompositeId;
import com.workable.movierama.api.dto.MovieDto;
import com.workable.movierama.service.MovieRamaAdminService;
import com.workable.movierama.web.MovieRamaController;

@Component
public class MovieRamaAdminServiceImpl implements MovieRamaAdminService {

	Logger LOGGER = LoggerFactory.getLogger(MovieRamaController.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Value("${application.the-movie-db-api-key}")
	private String theMovieDbApiKey;

	@Value("${application.rotter-tomatoes-api-key}")
	private String rottenTomatoesApiKey;

	private final Integer PAGING = 1;

	private final String LANGUAGE = "en-US";

	@Cacheable(value = "moviesCache", sync = true)
	public List<MovieDto> list(String title) {

		Map<String, MovieDto> movies = new HashMap<>();

		if (StringUtils.isBlank(title)) {
			getLatest(movies);
		} else {
			getMovie(title, movies);
		}

		return movies.values().stream().collect(Collectors.toList());
	}

	private void getLatest(Map<String, MovieDto> movies) {
		String mdb;
		String rtm;
		// get latest

		// retrieve movies from ROTTEN TOMATOES
		rtm = restTemplate.getForObject(ROTTEN_TOMATOES_LATEST_URL,
				String.class,
				rottenTomatoesApiKey,
				String.valueOf(20)); // uses different paging from movie db

		JsonNode rmtResults;
		try {
			rmtResults = mapper.readTree(rtm).get("movies");

			for (JsonNode movie : rmtResults) {

				retrieveRTReviewsAndBuild(movies, movie);

			}
		} catch (Exception e) {

		}

		// retrieve movies from MOVIE DB
		LOGGER.info("Retrieving	now playing data from Movie DB");

		mdb = restTemplate.getForObject(MOVIE_DB_LATEST_URL,
				String.class,
				theMovieDbApiKey,
				LANGUAGE);

		JsonNode mdbResults;
		try {
			mdbResults = mapper.readTree(mdb).get("results");

			for (JsonNode movie : mdbResults) {
				merge(movies, movie);

			}
		} catch (Exception e) {

		}

	}

	private void getMovie(String title, Map<String, MovieDto> movies) {
		String mdb;
		String rtm;
		// get movie

		// retrieve movies from ROTTEN TOMATOES
		rtm = restTemplate.getForObject(ROTTEN_TOMATOES_SEARCH_URL,
				String.class,
				rottenTomatoesApiKey,
				title,
				String.valueOf(PAGING));

		try {

			JsonNode rtmResults = mapper.readTree(rtm).get("movies");

			for (JsonNode movie : rtmResults) {
				if (movie.get("title").asText().equalsIgnoreCase(title)) {
					retrieveRTReviewsAndBuild(movies, movie);
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// retrieve movies from MOVIE DB

		LOGGER.info("Retrieving data from Movie DB");

		mdb = restTemplate.getForObject(MOVIE_DB_SEARCH_URL,
				String.class,
				String.valueOf(PAGING),
				title,
				LANGUAGE,
				theMovieDbApiKey);

		try {
			JsonNode mdbResults = mapper.readTree(mdb).get("results");

			for (JsonNode movie : mdbResults) {

				if (movie.get("title").asText().equalsIgnoreCase(title)) {
					merge(movies, movie);

					break;
				}

			}
		} catch (Exception e) {

		}

	}

	/**
	 * Will add reviews found for the movie from both APIs and evaluate the
	 * final description the MovieRama page will present. Original title,release
	 * date actors were evaluated on rotten tomatoes query
	 * 
	 * @param movies
	 * @param movie
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	private void merge(Map<String, MovieDto> movies, JsonNode movie) throws IOException, JsonProcessingException {
		String mdbReviews = restTemplate.getForObject(MOVIE_DB_REVIEWS_URL,
				String.class,
				movie.get("id").asText(),
				theMovieDbApiKey,
				LANGUAGE);

		Long dbr = mapper.readTree(mdbReviews).get("total_results").asLong();

		String mdbTitle = movie.get("original_title").asText();
		String mdbDescription = movie.get("overview").asText();

		if (movies.containsKey(movie.get("original_title").asText().toLowerCase())) {

			String description = movies.get(mdbTitle.toLowerCase()).getDescription();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Rotten tomatoes description is : " + description +
						" \n Movie DB description is : " + mdbDescription);
			}

			if (StringUtils.isBlank(description) ||
					description.length() < mdbDescription.length()) {
				movies.get(mdbTitle.toLowerCase()).setDescription(mdbDescription);
			}
			movies.get(mdbTitle.toLowerCase()).addReviews(dbr);
		} else {
			MovieDto m = new MovieDto(new CompositeId(null,
					movie.get("id").asLong()),
					movie.get("original_title").asText(),
					movie.get("overview").asText(),
					dbr,
					LocalDate.parse(movie.get("release_date").asText()).getYear(),
					retrieveActors(movie.get("id").asText()));

			movies.put(m.getTitle().toLowerCase(), m);
		}
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
	private void retrieveRTReviewsAndBuild(Map<String, MovieDto> movies, JsonNode movie) throws IOException, JsonProcessingException {
		String rtmReviews = restTemplate.getForObject(ROTTEN_TOMATOES_REVIEWS_URL,
				String.class,
				movie.get("id").asText(),
				rottenTomatoesApiKey);

		Long rottenReviews = mapper.readTree(rtmReviews).get("total").asLong();

		MovieDto m = new MovieDto(
				new CompositeId(movie.get("id").asLong(), // rotten
															// tomatoes
															// id
						null), // movie db
								// id
				movie.get("title").asText(),
				movie.get("synopsis").asText(),
				rottenReviews, // numberOfReviews
				LocalDate.parse(movie.get("release_dates").get("theater").asText()).getYear(),
				retrieveActors(movie.get("abridged_cast")));
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
	private List<String> retrieveActors(String movieId) {

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

	/**
	 * Used to parse actors from the "abridged_cast" element of rotten tomatoes'
	 * search response API call.
	 * 
	 * @param actors
	 * @return
	 */
	private List<String> retrieveActors(JsonNode actors) {

		ArrayList<String> al = new ArrayList<String>();

		for (JsonNode a : actors) {
			al.add(a.get("name").asText());
		}

		return al;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

}
