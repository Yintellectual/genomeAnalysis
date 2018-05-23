package com.spdeveloper.chgc.genome.annotation.entity;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.Hit;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.Iteration_hits;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.PrMatch;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RNAAnnotatedFactoryMethodsTest {

	@Test
	public void RNAmmer() {
		String sample = "L.bulgaricus    RNAmmer-1.2     rRNA    1805473 1808378 3384.8  -       .       23s_rRNA";
	
		RnaAnnotated actual = RnaAnnotated.parseRNAmmer(sample);
		
		assertEquals("	1808378	1805473	2906	23s_rRNA", actual.toString());
	}
	@Test
	public void tRNAscan() {
		String sample = "ben181  	3	428181	428257	Arg	TCT	0	0	80.83";
		
		RnaAnnotated actual = RnaAnnotated.parseTRNAscan(sample);
		
		assertEquals("	428181	428257	77	tRNA-Arg-TCT", actual.toString());
	}
	@Test
	public void automaticallyIgnoreTableHeaders() {
		String sample = "Name    	tRNA #	Begin 	End   	Type	Codon	Begin	End	Score";
		
		RnaAnnotated actual = RnaAnnotated.parseTRNAscan(sample);
		
		assertNull(actual);
	}
}
