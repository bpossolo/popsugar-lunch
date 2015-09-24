package com.popsugar.lunch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePairDTO {
	
	@JsonProperty
	public Long user1Key;
	
	@JsonProperty
	public Long user2Key;
}
