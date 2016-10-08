package com.workable.movierama.service;

import java.util.List;

import com.workable.movierama.api.dto.Movie;

public interface MovieRamaAdminService {

	List<Movie> list(String title);


}
