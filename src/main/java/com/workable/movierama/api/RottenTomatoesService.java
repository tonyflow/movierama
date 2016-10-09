package com.workable.movierama.api;

import static com.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_LATEST_URL;
import static com.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_REVIEWS_URL;
import static com.workable.movierama.api.UrlConstants.ROTTEN_TOMATOES_SEARCH_URL;

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
 * data via the Rotten tomatoes API
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

	@Override
	public String getResourceName() {
		return "Rotten Tomatoes";
	}

	@Override
	public Map<String, Movie> listLatestMovies() {

		Map<String, Movie> movies = new HashMap<String, Movie>();

		String rtm = restTemplate.getForObject(ROTTEN_TOMATOES_LATEST_URL,
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
			LOGGER.error("Error while");
		}

		return movies;

	}

	@Override
	public Movie getMovie(String title) {

		Map<String, Movie> movies = new HashMap<String, Movie>();

		String rtm = restTemplate.getForObject(ROTTEN_TOMATOES_SEARCH_URL,
				String.class,
				rottenTomatoesApiKey,
				title,
				String.valueOf(SEARCH_PAGING));

		try {

			JsonNode rtmResults = mapper.readTree(rtm).get("movies");

			for (JsonNode movie : rtmResults) {
				if (movie.get("title").asText().equalsIgnoreCase(title)) {
					retrieveRTReviewsAndBuild(movies, movie);

					return movies.get(title.toLowerCase());
				}
			}

		} catch (Exception e) {
			LOGGER.error("Error while building Rotten tomatoes response for movie " + title, e);
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
	private void retrieveRTReviewsAndBuild(Map<String, Movie> movies, JsonNode movie) throws Exception {
		String rtmReviews = restTemplate.getForObject(ROTTEN_TOMATOES_REVIEWS_URL,
				String.class,
				movie.get("id").asText(),
				rottenTomatoesApiKey);

		Long rottenReviews = mapper.readTree(rtmReviews).get("total").asLong();

		Movie m = new Movie(
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

}
