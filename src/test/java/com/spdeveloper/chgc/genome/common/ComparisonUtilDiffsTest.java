package com.spdeveloper.chgc.genome.common;

import static com.spdeveloper.chgc.genome.util.debug.ComparisonUtil.findDifferentLines;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ComparisonUtilDiffsTest {
	@Test
	public void differentLinesShouldBeDetected() {
		String[] expected = new String[] { "orf00063	-	+	56994	57539	transport protein " };
		String[] actual = new String[] { "	orf00063	-	+	56994	57539	trandsportorf  protein " };

		int[] differentLines = findDifferentLines(expected, actual);

		assertEquals(0, differentLines[0]);
	}

}
