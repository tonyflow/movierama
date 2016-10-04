package org.workable.movierama.exceptions;

public class MovieNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -1450024838116040065L;

	public MovieNotFoundException() {
		super();
	}

	public MovieNotFoundException(String message) {
		super(message);
	}

}
