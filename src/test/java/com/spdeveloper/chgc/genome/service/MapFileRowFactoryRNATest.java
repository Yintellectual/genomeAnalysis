package com.spdeveloper.chgc.genome.service;


import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.visualization.entity.MapFileRow;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.factory.MapFileRowFactory;
import com.spdeveloper.chgc.genome.visualization.service.RNAParser;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapFileRowFactoryRNATest {
	
	public List<RNA> rnas;
	
	@Autowired
	RNAParser rnaParser;
	
	@Before
	public void before() throws IOException {
		rnas = rnaParser.parse("L99-Gene.xlsx", 1);
	}
	
	@Test
	public void lengthIncreasedBy1000() {
		for(RNA rna:rnas) {
			int start = MapFileRowFactory.toMapFileRow(rna).getStart().intValue();
			int stop = MapFileRowFactory.toMapFileRow(rna).getStop().intValue();
			if(start>500&&stop>500) {
				assertEquals(rna.getLength().intValue()+1000, Math.abs(start-stop)+1);	
			}
		}
	}
	
	@Test
	public void bothStartAndStopChangedBy500() {
		for(RNA rna:rnas) {
			int start = MapFileRowFactory.toMapFileRow(rna).getStart().intValue();
			int stop = MapFileRowFactory.toMapFileRow(rna).getStop().intValue();
			if(start>500&&stop>500) {
				assertEquals(500, Math.abs(start-rna.getStart().intValue()));	
				assertEquals(500, Math.abs(stop-rna.getStop().intValue()));
			}
		}
	}
	
	@Test
	public void startSmallerThan500SetToZero(){
		RNA test1 = new RNA("test1", 10.0, 600.0, 591.0, "tRNA");
		RNA test2 = new RNA("test1", 10.0, 300.0, 291.0, "tRNA");
		RNA test3 = new RNA("test1", 600.0, 10.0, 591.0, "tRNA");
		assertEquals(0, MapFileRowFactory.toMapFileRow(test1).getStart().intValue());
		assertEquals(1100, MapFileRowFactory.toMapFileRow(test1).getStop().intValue());
		assertEquals(0, MapFileRowFactory.toMapFileRow(test2).getStart().intValue());
		assertEquals(800, MapFileRowFactory.toMapFileRow(test2).getStop().intValue());
		assertEquals(1100, MapFileRowFactory.toMapFileRow(test3).getStart().intValue());
		assertEquals(0, MapFileRowFactory.toMapFileRow(test3).getStop().intValue());
	}
	
	
	@Test
	public void all_tRNA_Is_Z() {
		for(RNA rna:rnas) {
			MapFileRow row = MapFileRowFactory.toMapFileRow(rna);
			if(rna.getProduct().trim().toUpperCase().contains("TRNA")) {
				assertEquals("Z", row.getColor());
			}
		}
	}
	@Test
	public void all_Z_is_tRNA() {
		for(RNA rna:rnas) {
			MapFileRow row = MapFileRowFactory.toMapFileRow(rna);
			if("Z".equals(row.getColor())) {
				assertThat(rna.getProduct().trim().toUpperCase(), containsString("TRNA"));
			}
		}
	}
	@Test
	public void all_Ribosomal_is_G() {
		for(RNA rna:rnas) {
			MapFileRow row = MapFileRowFactory.toMapFileRow(rna);
			if(rna.getProduct().trim().toUpperCase().contains("RIBOSOMAL")) {
				assertEquals("G", row.getColor());
			}
		}
	}
	@Test
	public void all_G_is_Ribosomal() {
		for(RNA rna:rnas) {
			MapFileRow row = MapFileRowFactory.toMapFileRow(rna);
			if("G".equals(row.getColor())) {
				assertThat(rna.getProduct().trim().toUpperCase(), containsString("RIBOSOMAL"));
			}
		}
	}
	
}
