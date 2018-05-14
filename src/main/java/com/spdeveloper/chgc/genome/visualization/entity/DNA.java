package com.spdeveloper.chgc.genome.visualization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DNA {
	private String gene;
	private Double start;
	private Double stop;
	private Double length; 
	private String product;
	private String best_hit_organism;
	private String ko;
	private String cog;
	private String cog_class;
}
