package com.workable.movierama.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.workable.movierama.api.MovieResourceService;

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

	@Autowired
	private Map<String, MovieResourceService> availableMovieResources;

	private Map<String, MovieResourceService> configuredMovieResources = new HashMap<String, MovieResourceService>();

	@Value("#{'${application.movie-resources}'.split(',')}")
	private List<String> resources;

	@PostConstruct
	private void init() {

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
