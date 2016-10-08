package com.workable.movierama.service;

import java.util.List;

import com.workable.movierama.api.dto.MovieDto;

public interface MovieRamaAdminService {

	List<MovieDto> list(String title);


}
