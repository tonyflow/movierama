package com.workable.movierama.web;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.workable.movierama.api.dto.Movie;
import com.workable.movierama.exceptions.TitleValidationException;
import com.workable.movierama.service.MovieRamaService;

/**
 * Controller containing the basic API calls for MovieRama. List operation are
 * allocated to two request mappings : search which handles single title queries
 * and latest which produces a list of movies being played in theaters.
 * 
 * @author niko.strongioglou
 */
@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping(value = "/movies")
public class MovieRamaController {

	Logger LOGGER = LoggerFactory.getLogger(MovieRamaController.class);

	@Autowired
	private MovieRamaService adminService;

	private final String ILLEGAL_CHARS = "<>?{}|\\@#$%^&*-_+";

	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Iterable<Movie> search(
			@RequestParam(value = "title", required = true) String title) throws TitleValidationException {

		validate(title);

		Movie m = adminService.search(title.toLowerCase());
		return Arrays.asList(m);
	}

	@RequestMapping(value = "/latest", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Iterable<Movie> latest() throws Exception {

		LOGGER.debug("Received request for latest movies in theaters");

		return adminService.latest();
	}

	private void validate(String title) throws TitleValidationException {

		if (StringUtils.isNotBlank(title)) {
			LOGGER.debug("Received request for title " + title);
		} else {
			throw new TitleValidationException();
		}

		char[] ca = title.toCharArray();

		for (char c : ca) {
			if (ILLEGAL_CHARS.indexOf(c) > 0) {
				throw new TitleValidationException("Movie title cannot contain the following special characters or be blank.");
			}
		}
	}

	@ExceptionHandler(RuntimeException.class)
	public String handleError(Exception e) {
		return e.getMessage();
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(TitleValidationException.class)
	public String validationHandler(Exception e) {
		return "Title contains " + ILLEGAL_CHARS + " or is blank";
	}

}
