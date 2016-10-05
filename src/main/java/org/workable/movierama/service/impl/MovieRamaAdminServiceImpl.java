package org.workable.movierama.service.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.workable.movierama.api.dto.CompositeId;
import org.workable.movierama.api.dto.MovieDto;
import org.workable.movierama.service.MovieRamaAdminService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MovieRamaAdminServiceImpl implements MovieRamaAdminService {

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

	@Cacheable(value = "moviesCache")
	public List<MovieDto> list(String title) {

		String mdb = null;

		String rtm = null;

		Map<String, MovieDto> movies = new HashMap<>();

		if (StringUtils.isBlank(title)) {
			// get latest

			// retrieve movies from MOVIE DB
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
							new ArrayList<String>());

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
								new ArrayList<String>());

						movies.put(m.getTitle().toLowerCase(), m);
					}

					movies.get(rottenTitle.toLowerCase())
							.setActors(retrieveActors(movie.get("abridged_cast")));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			// get movie
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
								new ArrayList<String>());
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
									new ArrayList<String>());

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

		return movies.values().stream().collect(Collectors.toList());
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
