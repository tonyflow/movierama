package com.minified.movierama.api.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Movie implements Serializable {

	private static final long serialVersionUID = -6620202295601783684L;

	/**
	 * Internal primary key
	 */
	@JsonIgnore
	private Long id;

	/**
	 * Composite id creted by both Rotten Tomatoes and MovieDb ids
	 */
	@JsonIgnore
	private CompositeId compositeId;

	private String title;
	private String description;
	private Long numberOfReviews;
	private Integer productionYear;
	private List<String> actors;

	public Movie() {
	}

	/**
	 * Used when retrieving movies for the first time from mDBT or Rotten
	 * Tomatoes.
	 * 
	 * @param name
	 * @param description
	 * @param numberOfReviews
	 * @param actors
	 */
	public Movie(CompositeId compositeId, String title, String description,
			Long numberOfReviews, int productionYear, List<String> actors) {
		this.compositeId = compositeId;
		this.title = title;
		this.description = description;
		this.numberOfReviews = numberOfReviews;
		this.productionYear = productionYear;
		this.actors = actors;
	}

	/**
	 * Used when movie is retrieved from cache and there is a specifc internal
	 * id for it.
	 * 
	 * @param id
	 * @param name
	 * @param description
	 * @param numberOfReviews
	 * @param actors
	 */
	public Movie(Long id, CompositeId compositeId, String name,
			String description, Long numberOfReviews, int productionYear,
			List<String> actors) {

		this(compositeId, name, description, numberOfReviews, productionYear,
				actors);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CompositeId getCompositeId() {
		return compositeId;
	}

	public void setCompositeId(CompositeId compositeId) {
		this.compositeId = compositeId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getNumberOfReviews() {
		return numberOfReviews;
	}

	public void setNumberOfReviews(Long numberOfReviews) {
		this.numberOfReviews = numberOfReviews;
	}

	public void addReviews(Long reviews) {
		this.numberOfReviews += reviews;
	}

	public int getProductionYear() {
		return productionYear;
	}

	public void setProductionYear(int productionYear) {
		this.productionYear = productionYear;
	}

	public List<String> getActors() {
		return actors;
	}

	public void setActors(List<String> actors) {
		this.actors = actors;
	}

	public void addActor(String actor) {
		this.actors.add(actor);
	}

	@Override
	public String toString() {
		return "Movie [id=" + id + ", compositeId=" + compositeId + ", title=" + title + ", description=" + description + ", numberOfReviews="
				+ numberOfReviews + ", productionYear=" + productionYear + ", actors=" + actors + "]";
	}

}
