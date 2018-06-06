package com.spdeveloper.chgc.genome.annotation.entity;

import static com.spdeveloper.chgc.genome.util.java.SpDeveloperStringUtil.mapNullToEmptyString;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RnaAnnotated{
	private String name;
	private Integer start;
	private Integer stop;
	private Boolean positive;
	private String product;
	
	public static ArrayList<ArrayList<Object>>  listTo2Dimensional(List<RnaAnnotated> rnaAnnotateds){
		return new ArrayList<>(rnaAnnotateds.parallelStream().map(RnaAnnotated::toArrayList).collect(Collectors.toList()));
	}
	
	public ArrayList<Object>  toArrayList() {
		ArrayList<Object> result = new ArrayList<Object>(5);
		
		result.add(0, mapNullToEmptyString(getName()).trim());
		int start = 0;
		int stop = 0;
		if (getPositive()) {
			start = getStart();
			stop = getStop();
		} else {
			stop = getStart();
			start = getStop();
		}

		result.add(1, start);

		result.add(2, stop);

		result.add(3, getLength());

		result.add(4, mapNullToEmptyString(getProduct()).trim());
		
		return result;
	}
	
	public static RnaAnnotated parseRNAmmer(String string) {
		String regex = "(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		if(matcher.find()) {
			String name = "";
			Integer start = null;
			Integer stop = null;
			try {
				start = Integer.parseInt(matcher.group(4));
				stop = Integer.parseInt(matcher.group(5));
			}catch(NumberFormatException e) {
				
			}
			Boolean positive = Boolean.parseBoolean(matcher.group(7));
			String product = matcher.group(9);
			if(start == null||stop == null) {
				return null;
			}
			return new RnaAnnotated(name, start, stop, positive, product);
		}else {
			return null;
		}
	}
	public static RnaAnnotated parseTRNAscan(String string) {
		String regex = "(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		if(matcher.find()) {
			String name = "";
			Integer start = null;
			Integer stop = null;
			try {
				start = Integer.parseInt(matcher.group(3));
				stop = Integer.parseInt(matcher.group(4));
			}catch(NumberFormatException e) {
				
			}
			if(start == null||stop == null) {
				return null;
			}
			Boolean positive = start < stop;
			if(!positive) {
				int temp = start;
				start = stop;
				stop = temp;
			}
			String product = "tRNA-"+matcher.group(5)+"-"+matcher.group(6);
			return new RnaAnnotated(name, start, stop, positive, product);
		}else {
			return null;
		}
	}
	
	public Integer getLength() {
		return Math.abs(start-stop)+1;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(name);
		stringBuilder.append('\t');
		if(getPositive()) {
			stringBuilder.append(start);
			stringBuilder.append('\t');
			stringBuilder.append(stop);
			stringBuilder.append('\t');
		}else {
			stringBuilder.append(stop);
			stringBuilder.append('\t');
			stringBuilder.append(start);
			stringBuilder.append('\t');
		}
		stringBuilder.append(getLength());
		stringBuilder.append('\t');
		stringBuilder.append(product);
		return stringBuilder.toString();
	}
}
