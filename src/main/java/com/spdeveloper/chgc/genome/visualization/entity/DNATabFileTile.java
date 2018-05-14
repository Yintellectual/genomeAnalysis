package com.spdeveloper.chgc.genome.visualization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DNATabFileTile {
	
	private Integer start;
	private Integer stop;
	private String gene;
	private String product;
	
}
