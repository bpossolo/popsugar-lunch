package com.popsugar.lunch.model;

public enum Location {
	
	SanFrancisco,
	LosAngeles,
	NewYork;

	@Override
	public String toString(){
		switch( this ){
		case SanFrancisco : return "San Francisco";
		case LosAngeles : return "Los Angeles";
		case NewYork : return "New York";
		default : throw new RuntimeException("someone forgot to augment toString() with the new location value");
		}
	}
}
