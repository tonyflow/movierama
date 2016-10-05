package org.workable.movierama.service.impl;

import static org.workable.movierama.api.UrlConstants.MOVIE_DB_CREDITS_URL;
import static org.workable.movierama.api.UrlConstants.MOVIE_DB_LATEST_URL;
import static org.workable.movierama.api.UrlConstants.MOVIE_DB_REVIEWS_URL;
import static org.workable.movierama.api.UrlConstants.MOVIE_DB_SEARCH_URL;
import static org.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_LATEST_URL;
import static org.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_REVIEWS_URL;
import static org.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_SEARCH_URL;

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
import org.workable.movierama.api.dto.CompositeId;
import org.workable.movierama.api.dto.MovieDto;
import org.workable.movierama.service.MovieRamaAdminService;
import org.workable.movierama.web.MovieRamaController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

				String mdbReviews = restTemplate.getForObject(MOVIE_DB_REVIEWS_URL,
						String.class,
						movie.get("id").asText(),
						theMovieDbApiKey,
						LANGUAGE);

				MovieDto m = new MovieDto(
						new CompositeId(null, // rotten tomatoes id
								movie.get("id").asLong()), // movie db id
						movie.get("original_title").asText(),
						movie.get("overview").asText(),
						mapper.readTree(mdbReviews).get("total_results").asLong(), // numberOfReviews
						LocalDate.parse(movie.get("release_date").asText()).getYear(),
						retrieveActors(movie.get("id").asText()));

				movies.put(movie.get("original_title").asText().toLowerCase(), m);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// retrieve movies from ROTTEN TOMATOES
		rtm = restTemplate.getForObject(ROTTEN_TOMATOES_LATEST_URL,
				String.class,
				rottenTomatoesApiKey,
				String.valueOf(20)); // uses different paging from movie db

		JsonNode rmtResults;
		try {
			rmtResults = mapper.readTree(rtm).get("movies");

			for (JsonNode movie : rmtResults) {

				String rtmReviews = restTemplate.getForObject(ROTTEN_TOMATOES_REVIEWS_URL,
						String.class,
						movie.get("id").asText(),
						rottenTomatoesApiKey);

				Long rottenReviews = mapper.readTree(rtmReviews).get("total").asLong();

				String rottenTitle = movie.get("title").asText();
				String rottenDescription = movie.get("synopsis").asText();

				if (movies.containsKey(movie.get("title").asText().toLowerCase())) {

					// original title,release date were evaluated on
					// movie db query

					String description = movies.get(rottenTitle.toLowerCase()).getDescription();

					// check description length
					movies.get(rottenTitle.toLowerCase()).setDescription(StringUtils.isBlank(description) ||
							description.length() < rottenDescription.length() ?
							rottenDescription : description);
					// add more reviews
					movies.get(rottenTitle.toLowerCase()).addReviews(rottenReviews);
				} else {
					MovieDto m = new MovieDto(new CompositeId(movie.get("id").asLong(),
							null),
							movie.get("title").asText(),
							movie.get("synopsis").asText(),
							rottenReviews,
							LocalDate.parse(movie.get("release_dates").get("theater").asText()).getYear(),
							retrieveActors(movie.get("abridged_cast")));

					movies.put(m.getTitle().toLowerCase(), m);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getMovie(String title, Map<String, MovieDto> movies) {
		String mdb;
		String rtm;
		// get movie

		LOGGER.info("Retrieving data from Movie DB");

		mdb = restTemplate.getForObject(MOVIE_DB_SEARCH_URL,
				String.class,
				String.valueOf(PAGING),
				title,
				LANGUAGE,
				theMovieDbApiKey);

		try {

			// retrieve Movie Db id
			JsonNode mdbResults = mapper.readTree(mdb).get("results");

			for (JsonNode movie : mdbResults) {
				if (movie.get("original_title").asText().equalsIgnoreCase(title)) {
					MovieDto m = new MovieDto(
							new CompositeId(null, // rotten tomatoes id
									movie.get("id").asLong()), // movie db
																// id
							movie.get("original_title").asText(),
							movie.get("overview").asText(),
							null, // numberOfReviews
							LocalDate.parse(movie.get("release_date").asText()).getYear(),
							retrieveActors(movie.get("id").asText()));
					movies.put(title.toLowerCase(), m);
					break;
				}
			}

			if (!movies.isEmpty()) {
				// Retrieve Movie Db reviews
				String mdbReviews = restTemplate.getForObject(MOVIE_DB_REVIEWS_URL,
						String.class,
						movies.get(title.toLowerCase()).getCompositeId().getMovieDbId(),
						theMovieDbApiKey,
						LANGUAGE);
				movies.get(title.toLowerCase())
						.setNumberOfReviews(mapper.readTree(mdbReviews).get("total_results").asLong());

			} else {
				// TODO Log Info. No movie was found with the movie db API.
			}

		} catch (IOException e) {
			// TODO Logging
			e.printStackTrace();
		}

		// retrieve movies from ROTTEN TOMATOES
		rtm = restTemplate.getForObject(ROTTEN_TOMATOES_SEARCH_URL,
				String.class,
				rottenTomatoesApiKey,
				title,
				String.valueOf(PAGING));

		try {
			JsonNode rmtResults = mapper.readTree(rtm).get("movies");

			for (JsonNode movie : rmtResults) {

				if (movie.get("title").asText().equalsIgnoreCase(title)) {

					String rottenTitle = movie.get("title").asText();
					String rottenDescription = movie.get("synopsis").asText();

					String rtmReviews = restTemplate.getForObject(ROTTEN_TOMATOES_REVIEWS_URL,
							String.class,
							movie.get("id").asText(),
							rottenTomatoesApiKey);

					Long rottenReviews = mapper.readTree(rtmReviews).get("total").asLong();

					if (movies.containsKey(rottenTitle.toLowerCase())) {

						String description = movies.get(rottenTitle.toLowerCase()).getDescription();

						movies.get(rottenTitle.toLowerCase()).setDescription(StringUtils.isBlank(description) ||
								description.length() < rottenDescription.length() ?
								rottenDescription : description);
						movies.get(rottenTitle.toLowerCase()).addReviews(rottenReviews);

					} else {
						MovieDto m = new MovieDto(new CompositeId(movie.get("id").asLong(),
								null),
								movie.get("title").asText(),
								movie.get("synopsis").asText(),
								rottenReviews,
								LocalDate.parse(movie.get("release_dates").get("theater").asText()).getYear(),
								null);

						movies.put(m.getTitle().toLowerCase(), m);
					}

					movies.get(rottenTitle.toLowerCase())
							.setActors(retrieveActors(movie.get("abridged_cast")));

					break;
				}
			}

		} catch (IOException e) {
			// TODO Logging
			e.printStackTrace();
		}
	}

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
