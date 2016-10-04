package org.workable.movierama.api.dto;

import java.io.Serializable;

public class CompositeId implements Serializable {

	private static final long serialVersionUID = 4096668182948314121L;

	private Long rottenTomatoesId;
	private Long movieDbId;

	public CompositeId() {

	}

	public CompositeId(Long rottenTomatoesId, Long movieDbId) {
		super();
		this.rottenTomatoesId = rottenTomatoesId;
		this.movieDbId = movieDbId;
	}

	public Long getRottenTomatoesId() {
		return rottenTomatoesId;
	}

	public void setRottenTomatoesId(Long rottenTomatoesId) {
		this.rottenTomatoesId = rottenTomatoesId;
	}

	public Long getMovieDbId() {
		return movieDbId;
	}

	public void setMovieDbId(Long movieDbId) {
		this.movieDbId = movieDbId;
	}

}
