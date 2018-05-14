package com.spdeveloper.chgc.genome.service;


import static com.spdeveloper.chgc.genome.visualization.factory.MapFileRowFactory.color;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;
@RunWith(SpringRunner.class)
@SpringBootTest
public class MapFileRowFactoryColorTest {
	
	
	@Test
	public void defaultValueForEmptyInput() {
		DNA dna = new DNA();
		dna.setCog_class("");
		
		assertEquals("-", color(dna));
	}
	@Test
	public void defaultValueForNullInput() {
		DNA dna = new DNA();
		dna.setCog_class(null);
		
		assertEquals("-", color(dna));
	}
	
	@Test
	public void ignoreNoneWordCharacters() {
		String sample = "Cell   motility and secretion ";
		DNA dna = new DNA();
		dna.setCog_class(sample);
		
		assertEquals("N", color(dna));
	}
	
	@Test
	public void ignoreEverythingAfterTheFirstSpliter() {
		String sample = "Cell motility and secretion / Intracellular trafficking and secretion";
		DNA dna = new DNA();
		dna.setCog_class(sample);
		
		assertEquals("N", color(dna));
	}
	
}
