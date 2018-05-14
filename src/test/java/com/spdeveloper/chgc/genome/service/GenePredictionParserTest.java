package com.spdeveloper.chgc.genome.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GenePredictionParserTest {

	@Autowired
	GenePredictionParser glimmerPredictionParser;
	private List<GenePrediction> glimmerSample = new ArrayList<>();
	private List<GenePrediction> zcurveSample = new ArrayList<>();
	@Before
	public void before() throws IOException {
		glimmerSample = glimmerPredictionParser.parse(Paths.get("src", "main", "resources", "files", "test", "tag.predict"), s->GenePredictionParser.fromGlimmerPrediction(s));
		zcurveSample = glimmerPredictionParser.parse(Paths.get("src", "main", "resources", "files", "test", "zcurve.txt"), s->GenePredictionParser.fromZcurvePrediction(s));
	}
	
	@Test 
	public void noNoneValuesForGlimmerSample() throws IOException {
		glimmerSample.forEach(gene->{
			assertNotNull(gene);
		});
	}
	@Test 
	public void noNoneValuesForZcurveSample() throws IOException {
		zcurveSample.forEach(gene->{
			assertNotNull(gene);
		});
	}
	
	@Test
	public void numberOfPredictionsRemainUnchangedGlimmer() {
		assertEquals(3116, glimmerSample.size());
	}
	@Test
	public void numberOfPredictionsRemainUnchangedZcurve() {
		assertEquals(3017, zcurveSample.size());
	}
}
