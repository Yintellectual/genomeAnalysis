package com.spdeveloper.chgc.genome.annotation.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.PrMatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
