package org.workable.movierama.api;

import java.io.Serializable;
import java.util.List;

public class Movie implements Serializable {

	private static final long serialVersionUID = -2358202167637530949L;

	String id;
	String title;
	String synopsis;
	Long reviews;
	int releaseYear;

	List<String> actors;

	public Movie(String id, String title, String synopsis, Long reviews, int releaseYear, List<String> actors) {
		super();
		this.id = id;
		this.title = title;
		this.synopsis = synopsis;
		this.reviews = reviews;
		this.releaseYear = releaseYear;
		this.actors = actors;
	}

}
