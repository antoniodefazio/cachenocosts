package com.example.cachenoserver.dtos;

import java.io.Serializable;

public class ExactlyOnceDto implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private java.util.Date createdOn;

	private java.util.Date loadedOn;

	public ExactlyOnceDto() {
	}

	public java.util.Date getCreatedOn() {
		return createdOn;
	}

	public java.util.Date getLoadedOn() {
		return loadedOn;
	}

	public void setCreatedOn(java.util.Date createdOn) {
		this.createdOn = createdOn;
	}

	public void setLoadedOn(java.util.Date loadedOn) {
		this.loadedOn = loadedOn;
	}

	@Override
	public String toString() {
		return "ExactlyOnceDto [createdOn=" + createdOn + ", loadedOn=" + loadedOn + "]";
	}

}
