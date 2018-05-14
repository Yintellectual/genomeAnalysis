package com.spdeveloper.chgc.genome.visualization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapFileRow {
	private String id;
	private String color;
	private String symbol;
	private Integer start;
	private Integer stop;
	private String product;
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getId());
		sb.append("\t");
		sb.append(getColor());
		sb.append("\t");
		sb.append(getSymbol());
		sb.append("\t");
		sb.append(getStart());
		sb.append("\t");
		sb.append(getStop());
		sb.append("\t");
		sb.append(getProduct());
		sb.append("\t");
		return sb.toString();
	}
}
