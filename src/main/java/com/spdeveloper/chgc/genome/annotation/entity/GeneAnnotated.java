package com.spdeveloper.chgc.genome.annotation.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;

import com.spdeveloper.chgc.genome.util.xml.M7Parser.PrMatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.spdeveloper.chgc.genome.util.java.SpDeveloperStringUtil.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneAnnotated {
	private String name;
	private Integer start;
	private Integer stop;
	private Boolean positive;
	private String product;
	private String best_hit_organism;
	private String ko;
	private String cog;
	private String cog_class;

	public static ArrayList<ArrayList<Object>> listTo2Dimensional(List<GeneAnnotated> geneAnnotateds){
		return new ArrayList<>(geneAnnotateds.parallelStream().map(GeneAnnotated::toArrayList).collect(Collectors.toList()));
	}
	/*
	 * an object can only be type Integer or String
	 * */
	public ArrayList<Object> toArrayList() {
		ArrayList<Object> result = new ArrayList<Object>(9);
		
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

		result.add(4, mapNullToEmptyString(getProduct()));

		result.add(5, mapNullToEmptyString(getBest_hit_organism()));

		result.add(6, mapNullToEmptyString(getKo()));

		result.add(7, mapNullToEmptyString(getCog()));

		result.add(8, mapNullToEmptyString(getCog_class()));

		return result;
	}
	
	public GeneAnnotated(GenePrediction g, PrMatch blast, PrMatch cog) {
		this.name = g.getName();
		this.start = g.getStart();
		this.stop = g.getStop();
		this.positive = g.isPositive();
		String hit_def = null;
		try {
			hit_def = blast.getIteration_hits().getHit().getHit_def();
		} catch (NullPointerException e) {

		}
		if(hit_def==null||hit_def.isEmpty()) {
			
		}else {
			String prefix = "";
			String hit = "";
			if(hit_def.trim().startsWith("MULTISPECIES:")) {
				prefix = "MULTISPECIES: ";
				String[] hits = hit_def.trim().replaceAll(prefix, "").split("gi.*\\|");
				for(String h:hits) {
					if(!h.trim().isEmpty()&&h.contains("[")) {
						hit = h.trim();
						break;
					}else {
						
					}
				}
			}else {
				hit = hit_def.trim().replaceAll("gi.*\\|", "").trim();
			}
			Pattern pattern = Pattern.compile("(.*)\\[(.*)\\]");
			Matcher matcher = pattern.matcher(hit);
			if(matcher.find()) {
				this.product = prefix + matcher.group(1).trim();
				this.best_hit_organism = matcher.group(2).trim();
			}
		}
		//cog 
		String cog_hit_def = null;
		try {
			cog_hit_def = cog.getIteration_hits().getHit().getHit_def();
		} catch (NullPointerException e) {

		}
		if(cog_hit_def==null||cog_hit_def.isEmpty()) {
			
		}else {
			Pattern pattern = Pattern.compile("(.*)\\[(.*)\\]");
			Matcher matcher = pattern.matcher(cog_hit_def);
			if(matcher.find()) {
				this.cog = matcher.group(1).trim();
				this.cog_class = matcher.group(2).trim();
			}
		}
	}
	public int getLength() {
		return Math.abs(start - stop) + 1;
	}
	
	@Override
	public String toString() {
		int start;
		int stop;
		if(this.getPositive()) {
			start = this.getStart();
			stop = this.getStop();
		}else {
			start = this.getStop();
			stop = this.getStart();
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.getName());
		stringBuilder.append('\t');
		stringBuilder.append(start);
		stringBuilder.append('\t');
		stringBuilder.append(stop);
		stringBuilder.append('\t');
		stringBuilder.append(getLength());
		stringBuilder.append('\t');
		stringBuilder.append(getProduct());
		stringBuilder.append('\t');
		stringBuilder.append(getBest_hit_organism());
		stringBuilder.append('\t');
		stringBuilder.append(getKo());
		stringBuilder.append('\t');
		stringBuilder.append(getCog());
		stringBuilder.append('\t');
		stringBuilder.append(getCog_class());
		stringBuilder.append('\t');
		
		return stringBuilder.toString();
	}
}
