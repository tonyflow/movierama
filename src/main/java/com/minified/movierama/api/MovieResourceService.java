package com.minified.movierama.api;

import java.util.Map;

import com.minified.movierama.api.dto.Movie;

/**
 * This is the basic interface any movie resource should implement in order to
 * take part in the aggregation and merge algorithm of
 * {@code MovieResourceService}.
 * 
 * @author niko.strongioglou
 *
 */
public interface MovieResourceService {

	String getResourceName();

	Map<String, Movie> listLatestMovies();

	Movie getMovie(String title);

}
