package org.workable.movierama.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.workable.movierama.api.dto.MovieDto;
import org.workable.movierama.service.MovieRamaAdminService;

@RestController
@RequestMapping(value = "/movies")
public class MovieRamaController {

	@Autowired
	private MovieRamaAdminService adminService;

	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<MovieDto> list(
			@RequestParam(value = "title", required = false) String title) {
		return adminService.list(title);
	}

	
}
