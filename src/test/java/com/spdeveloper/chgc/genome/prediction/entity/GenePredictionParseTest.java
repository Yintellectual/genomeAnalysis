package com.spdeveloper.chgc.genome.prediction.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GenePredictionParseTest {

	@Test
	public void ableToRecoverFromToString() {
		GenePrediction sample = new GenePrediction("orf00005", 2224, 5556, true);
		
		GenePrediction recovered = GenePrediction.parseGenePrediction(sample.toString());
		
		assertNotNull(recovered);
		assertEquals(recovered.getName(), "orf00005");
		assertEquals(recovered.getStart(), 2224);
		assertEquals(recovered.getStop(), 5556);
		assertEquals(recovered.isPositive(), true);
	}
}
