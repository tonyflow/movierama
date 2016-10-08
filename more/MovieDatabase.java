package org.workable.movierama.api;

import java.io.Serializable;
import java.util.Map;

public interface MovieDatabase extends Serializable {

	public String getKey();

	public Map<String, Movie> listLatestMovies();

	public Movie getMovie(String title);

}
