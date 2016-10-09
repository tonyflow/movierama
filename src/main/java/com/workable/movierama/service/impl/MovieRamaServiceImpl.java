package com.workable.movierama.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.workable.movierama.api.MovieDbService;
import com.workable.movierama.api.RottenTomatoesService;
import com.workable.movierama.api.dto.Movie;
import com.workable.movierama.service.MovieRamaService;
import com.workable.movierama.web.MovieRamaController;

@Component
public class MovieRamaServiceImpl implements MovieRamaService {

	Logger LOGGER = LoggerFactory.getLogger(MovieRamaController.class);

	@Autowired
	private RottenTomatoesService rottenTomatoesService;

	@Autowired
	private MovieDbService movieDbService;

	@Cacheable(value = "moviesCache", sync = true)
	public List<Movie> list(String title) {

		if (StringUtils.isBlank(title)) {
			return getLatest();
		} else {
			return getMovie(title);
		}

	}

	private List<Movie> getLatest() {

		Map<String, Movie> movies = new HashMap<>();

		Map<String, Movie> rtLatest = rottenTomatoesService.listLatestMovies();

		Map<String, Movie> mdbLatest = movieDbService.listLatestMovies();

		movies = merge(rtLatest, mdbLatest);

		return movies.values().stream().collect(Collectors.toList());

	}

	private List<Movie> getMovie(String title) {

		Movie rtMovieResponse = rottenTomatoesService.getMovie(title);
		Movie mdbMovieResponse = movieDbService.getMovie(title);

		Map<String, Movie> rtMap = new HashMap<String, Movie>();
		Map<String, Movie> mdbMap = new HashMap<String, Movie>();

		if (rtMovieResponse != null && mdbMovieResponse != null) {

			rtMap.put(title.toLowerCase(), rtMovieResponse);
			mdbMap.put(title.toLowerCase(), mdbMovieResponse);

		} else if (rtMovieResponse != null) {
			rtMap.put(title.toLowerCase(), rtMovieResponse);
		} else if (mdbMovieResponse != null) {
			mdbMap.put(title.toLowerCase(), mdbMovieResponse);
		} else {
			return Collections.<Movie> emptyList();
		}

		return merge(rtMap, mdbMap).values().stream().collect(Collectors.toList());

	}

	private Map<String, Movie> merge(Map<String, Movie> rtLatest, Map<String, Movie> mdbLatest) {

		if (CollectionUtils.isEmpty(rtLatest)) {

			// retrieve actors and return mdbLatest
			mdbLatest.forEach((title, movie) -> {
				List<String> actors = movieDbService.retrieveActors(movie.getCompositeId().getMovieDbId().toString());
				movie.setActors(actors);
			});

			return mdbLatest;

		} else if (mdbLatest.isEmpty()) {

			return rtLatest;

		} else {
			mdbLatest.forEach((title, movie) -> {

				if (rtLatest.containsKey(title)) {

					String mdbDescription = movie.getDescription();
					String rtDescription = rtLatest.get(title).getDescription();

					if (StringUtils.isBlank(rtDescription) ||
							rtDescription.length() < mdbDescription.length()) {
						rtLatest.get(title).setDescription(mdbDescription);
					}

					rtLatest.get(title)
							.addReviews(mdbLatest.get(title).getNumberOfReviews());

				} else {

					List<String> actors = movieDbService.retrieveActors(movie.getCompositeId().getMovieDbId().toString());

					movie.setActors(actors);

					rtLatest.put(title, movie);

				}

			});
		}

		return rtLatest;
	}

}