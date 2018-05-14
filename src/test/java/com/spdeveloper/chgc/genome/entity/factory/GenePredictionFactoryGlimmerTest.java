package com.spdeveloper.chgc.genome.entity.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GenePredictionFactoryGlimmerTest {

	@Test
	public void returnNullForEmptyLine() {
		String sample = " ";

		GenePrediction genePrediction = GenePredictionParser.fromGlimmerPrediction(sample);

		assertNull(genePrediction);
	}

	@Test
	public void returnNullForTheFirstLine() {
		String sample = ">genome";

		GenePrediction genePrediction = GenePredictionParser.fromGlimmerPrediction(sample);

		assertNull(genePrediction);
	}

	@Test
	public void returnMeaningfulObjectForASample() {
		String sample = "orf00002      395     1255  +2    12.15";

		GenePrediction genePrediction = GenePredictionParser.fromGlimmerPrediction(sample);

		assertEquals(new GenePrediction("orf00002", 395, 1255, true), genePrediction);
	}
}
