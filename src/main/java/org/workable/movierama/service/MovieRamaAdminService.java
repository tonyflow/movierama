package org.workable.movierama.service;

import java.util.List;

import org.workable.movierama.api.dto.MovieDto;

public interface MovieRamaAdminService {

	List<MovieDto> list(String title);


}
