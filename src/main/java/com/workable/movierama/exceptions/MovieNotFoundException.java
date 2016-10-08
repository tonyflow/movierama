package com.workable.movierama.exceptions;

public class MovieNotFoundException extends Exception {

	private static final long serialVersionUID = -4083175583972932032L;

	public MovieNotFoundException() {
		super();
	}

	public MovieNotFoundException(String message) {
		super(message);
	}

}
