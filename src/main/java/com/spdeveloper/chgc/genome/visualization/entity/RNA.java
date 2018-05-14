package com.spdeveloper.chgc.genome.visualization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RNA {
	private String rna;
	private Double start;
	private Double stop;
	private Double length;
	private String product;
}
