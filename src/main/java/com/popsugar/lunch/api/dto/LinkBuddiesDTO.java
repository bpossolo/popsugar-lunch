package com.popsugar.lunch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkBuddiesDTO {
	
	@JsonProperty
	public Long userAKey;
	
	@JsonProperty
	public Long userBKey;
}
