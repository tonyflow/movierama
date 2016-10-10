package com.workable.movierama.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.workable.movierama.api.MovieDbService;
import com.workable.movierama.api.RottenTomatoesService;
import com.workable.movierama.api.dto.Movie;
import com.workable.movierama.service.MovieRamaService;

@Component
public class MovieRamaServiceImpl implements MovieRamaService {

	private static final String NOW_PLAYING = "now playing";

	Logger LOGGER = LoggerFactory.getLogger(MovieRamaServiceImpl.class);

	@Autowired
	private RottenTomatoesService rottenTomatoesService;

	@Autowired
	private MovieDbService movieDbService;

	@Autowired
	private CacheManager cacheManager;

	private Cache moviesCache;

	@PostConstruct
	private void init() {
		moviesCache = cacheManager.getCache("moviesCache");
	}

	@Cacheable(value = "moviesCache", sync = true)
	public Movie search(String title) {

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
			return null;
		}

		return merge(rtMap, mdbMap).get(title);

	}

	public List<Movie> latest() {

		if (moviesCache.get(NOW_PLAYING) != null) {
			return retrieveCachedMovies();
		} else {
			Map<String, Movie> movies = new HashMap<>();
			Map<String, Movie> rtLatest = rottenTomatoesService.listLatestMovies();
			Map<String, Movie> mdbLatest = movieDbService.listLatestMovies();

			movies = merge(rtLatest, mdbLatest);

			movies.forEach((t, m) -> {
				moviesCache.put(t, m);
			});

			moviesCache.put(NOW_PLAYING, new ArrayList<String>(movies.keySet()));

			return movies.values().stream().collect(Collectors.toList());
		}

	}

	private Map<String, Movie> merge(Map<String, Movie> rtLatest, Map<String, Movie> mdbLatest) {

		if (CollectionUtils.isEmpty(rtLatest)) {

			return mdbLatest;

		} else if (CollectionUtils.isEmpty(mdbLatest)) {

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

					if (rtLatest.get(title).getNumberOfReviews() != null) {
						rtLatest.get(title)
								.addReviews(mdbLatest.get(title).getNumberOfReviews());
					} else {
						rtLatest.get(title).setNumberOfReviews(mdbLatest.get(title).getNumberOfReviews());
					}

					// update composite id
					rtLatest.get(title).getCompositeId().setMovieDbId(movie.getCompositeId().getMovieDbId());
				} else {

					rtLatest.put(title, movie);

				}

			});
		}

		return rtLatest;
	}

	@SuppressWarnings("unchecked")
	private List<Movie> retrieveCachedMovies() {

		LOGGER.debug("Retrieving latst movies from cache");

		Iterable<String> titles = (Iterable<String>) moviesCache.get(NOW_PLAYING).get();
		ArrayList<Movie> latest = new ArrayList<Movie>();
		for (String t : titles) {
			Movie m = (Movie) moviesCache.get(t).get();
			latest.add(m);
		}
		return latest;
	}

}
