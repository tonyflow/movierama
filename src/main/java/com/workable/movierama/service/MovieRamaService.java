package com.workable.movierama.service;

import java.util.List;

import com.workable.movierama.api.dto.Movie;

/**
 * Basic MovieRama interface.
 * 
 * @author niko.strongioglou
 */
public interface MovieRamaService {

	Movie search(String title);

	List<Movie> latest();

}
