package org.workable.movierama.api;

public abstract class UrlConstants {

	public static String MOVIE_DB_SEARCH_URL = "https://api.themoviedb.org/3/search/movie?"
			+ "page={page}&"
			+ "query={title}&"
			+ "language={language}&"
			+ "api_key={api_key}";

	public static String MOVIE_DB_LATEST_URL = "https://api.themoviedb.org/3/movie/now_playing?"
			+ "api_key={api_key}&"
			+ "language={language}";

	public static String MOVIE_DB_REVIEWS_URL = "https://api.themoviedb.org/3/movie/{movie_id}/reviews?"
			+ "api_key={api_key}&"
			+ "language={language}";

	public static String MOVIE_DB_CREDITS_URL = "https://api.themoviedb.org/3/movie/{movie_id}/credits?"
			+ "api_key={api_key}";

	public static String ROTTEN_TOMATOES_SEARCH_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?"
			+ "apikey={api_key}&"
			+ "q={title}&"
			+ "page_limit={page}";

	public static String ROTTEN_TOMATOES_LATEST_URL = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?"
			+ "apikey={api_key}&"
			+ "page_limit={page}";

	public static String ROTTEN_TOMATOES_REVIEWS_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies/{movie_id}/reviews.json?"
			+ "apikey={api_key}";

}
