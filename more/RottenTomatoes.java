package org.workable.movierama.service;

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
import org.workable.movierama.api.Movie;
import org.workable.movierama.api.MovieDatabase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RottenTomatoes implements MovieDatabase {

	private static final long serialVersionUID = -2748104583757644969L;

	Logger LOGGER = LoggerFactory.getLogger(RottenTomatoes.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Value("${application.rotter-tomatoes-api-key}")
	private String apiKey;

	// private final Integer PAGING = 1;

	private static String SEARCH_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?"
			+ "apikey={api_key}&"
			+ "q={title}&"
			+ "page_limit={page}";

	private static String LATEST_URL = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?"
			+ "apikey={api_key}&"
			+ "page_limit={page}";

	private static String REVIEWS_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies/{movie_id}/reviews.json?"
			+ "apikey={api_key}";

	@Override
	public String getKey() {
		return "Rotten Tomatoes";
	}

	@Override
	public Map<String, Movie> listLatestMovies() {

		String latest = restTemplate.getForObject(LATEST_URL, String.class, apiKey, String.valueOf(20));
		Map<String, Movie> movies = new HashMap<String, Movie>();

		JsonNode results;
		try {
			results = mapper.readTree(latest).get("movies");

			for (JsonNode m : results) {
				movies.put(m.get("title").asText().toLowerCase(),
						new Movie(m.get("id").asText(),
								m.get("title").asText(),
								m.get("synopsis").asText(),
								getReviews(m.get("id").asText()),
								LocalDate.parse(m.get("release_dates").get("theater").asText()).getYear(),
								parseActors(m.get("abridged_cast"))));

			}
		} catch (Exception e) {

		}

		return movies;
	}

	@Override
	public Movie getMovie(String title) {

		// retrieve movies from ROTTEN TOMATOES
		String result = restTemplate.getForObject(SEARCH_URL,
				String.class,
				apiKey,
				title,
				String.valueOf(1));

		try {

			JsonNode all = mapper.readTree(result).get("movies");

			for (JsonNode m : all) {
				if (m.get("title").asText().equalsIgnoreCase(title)) {
					return new Movie(m.get("id").asText(),
							m.get("title").asText(),
							m.get("synopsis").asText(),
							getReviews(m.get("id").asText()),
							LocalDate.parse(m.get("release_dates").get("theater").asText()).getYear(),
							parseActors(m.get("abridged_cast")));
				}
			}

		} catch (IOException e) {
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
	private Long getReviews(String movieId) throws IOException, JsonProcessingException {

		String result = restTemplate.getForObject(REVIEWS_URL, String.class, movieId, apiKey);

		return mapper.readTree(result).get("total").asLong();

	}

	private List<String> parseActors(JsonNode actors) {

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
