package com.spdeveloper.chgc.genome.prediction.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenePrediction {
	private String name;
	private int start;
	private int stop;
	private boolean positive;
	
	public int getLength() {
		return Math.abs(start - stop) + 1;
	}
	public int getInit() {
		if(isPositive()) {
			return start;
		}else {
			return stop;
		}
	}
	
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getName());
		stringBuilder.append("\t");
		if(isPositive()) {
			stringBuilder.append(getStart());
			stringBuilder.append("\t");
			stringBuilder.append(getStop());
			stringBuilder.append("\t");
		}else {
			stringBuilder.append(getStop());
			stringBuilder.append("\t");
			stringBuilder.append(getStart());
			stringBuilder.append("\t");
		}
		return stringBuilder.toString();
	}
	
	public static GenePrediction parseGenePrediction(String s) {
		String regex = "([\\w\\d]+)\\s+(\\d+)\\s+(\\d+)\\s*";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(s);
    	if(matcher.matches()) {
    		String name = matcher.group(1);
    		int start = Integer.parseInt(matcher.group(2));
    		int stop = Integer.parseInt(matcher.group(3));
    		if(start<stop) {
    			return new GenePrediction(name, start, stop, true);
    		}else {
    			return new GenePrediction(name, stop, start, false);
    		}
    	}else {
    		return null;
    	}
	}
}
