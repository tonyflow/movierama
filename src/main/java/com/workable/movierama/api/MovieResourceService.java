package com.workable.movierama.api;

import java.util.Map;

import com.workable.movierama.api.dto.Movie;

public interface MovieResourceService {

	String getResourceName();

	Map<String, Movie> listLatestMovies();

	Movie getMovie(String title);

}
