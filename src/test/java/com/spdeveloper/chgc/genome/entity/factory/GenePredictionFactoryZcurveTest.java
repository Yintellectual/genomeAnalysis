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
public class GenePredictionFactoryZcurveTest {

	@Test
	public void returnNullForEmptyLine() {
		String sample = " ";

		GenePrediction genePrediction = GenePredictionParser.fromGlimmerPrediction(sample);

		assertNull(genePrediction);
	}

	@Test
	public void returnNullForTheFirstLine() {
		String sample = "        >genome";

		GenePrediction genePrediction = GenePredictionParser.fromGlimmerPrediction(sample);

		assertNull(genePrediction);
	}

	@Test
	public void returnMeaningfulObjectForASample() {
		String sample = "    1           16          402            +          387      0.45091";

		GenePrediction genePrediction = GenePredictionParser.fromZcurvePrediction(sample);

		assertEquals(new GenePrediction("1", 16, 402, true), genePrediction);
	}
	
	@Test
	public void reverseStartAndStopForNegativeLines() {
		String sample = "\t\t15        13004        13882            -          879      0.56855";

		GenePrediction genePrediction = GenePredictionParser.fromZcurvePrediction(sample);

		assertEquals(new GenePrediction("15", 13004, 13882, false), genePrediction);
	}
}
