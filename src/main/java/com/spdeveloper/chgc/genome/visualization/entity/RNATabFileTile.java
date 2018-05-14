package com.spdeveloper.chgc.genome.visualization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RNATabFileTile {
	private Integer start;
	private Integer stop;
	private String rnaType;
	private String rna;
	private String product;
}
