package com.workable.movierama.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workable.movierama.api.dto.Movie;
import com.workable.movierama.service.MovieRamaAdminService;

@RestController
@RequestMapping(value = "/movies")
public class MovieRamaController {

	Logger LOGGER = LoggerFactory.getLogger(MovieRamaController.class);

	@Autowired
	private MovieRamaAdminService adminService;

	@CrossOrigin(origins = "http://localhost")
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Iterable<Movie> list(
			@RequestParam(value = "title", required = false) String title) {

		if (StringUtils.isNotBlank(title)) {
			LOGGER.info("Received request for title " + title);
		} else {
			LOGGER.info("Retrieving latest movies on theaters right now...");
		}

		return adminService.list(title);
	}

}
