package com.minified.movierama.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.minified.movierama.api.MovieResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory will make a comparison between available {@code MovieResourceService}
 * implementations and configured ones, select common ones and produce a map
 * which {@code MovieRamaService} can use to merge the result of all APIs.
 * 
 * @author niko.strongioglou
 *
 */
@Component
public class MovieResourceFactory {

	Logger LOGGER = LoggerFactory.getLogger(MovieResourceFactory.class);

	@Autowired
	private Map<String, MovieResourceService> availableMovieResources;

	private Map<String, MovieResourceService> configuredMovieResources = new HashMap<String, MovieResourceService>();

	@Value("#{'${application.movie-resources}'.split(',')}")
	private List<String> resources;

	@PostConstruct
	private void init() {

		LOGGER.debug("Initializing MovieResourceFactory... \n"
				+ "Found " + resources.size() + " configured movie resources\n"
				+ "and there are " + availableMovieResources.keySet().size() + " available movie resources.");

		for (String available : availableMovieResources.keySet()) {
			for (String configured : resources) {
				if (available.startsWith(configured)) {
					configuredMovieResources.put(configured, availableMovieResources.get(available));
				}
			}
		}
	}

	public MovieResourceService getResourceService(String resourceName) {
		return configuredMovieResources.get(resourceName);
	}
}
