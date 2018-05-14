package com.spdeveloper.chgc.genome.common;

import static com.spdeveloper.chgc.genome.util.debug.ComparisonUtil.trim;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ComparisonUtilTrimTest {

	@Test
	public void removeAllEmptyLinesInBothEnds() {
		String[] data = new String[] { "", "3", "", "3", "4", "" };

		assertEquals(3, trim(data).length);
	}

	@Test
	public void trimEachLine() {
		String[] data = new String[] { "", "3 ", "  4 ", "		5 ", "" };

		String[] trimed = trim(data);

		assertEquals(3 + "", trimed[0]);
		assertEquals(4 + "", trimed[1]);
		assertEquals(5 + "", trimed[2]);
	}

	@Test
	public void trimNewLineCharacters() {
		String[] data = new String[] { "					\r\n", "3 ", "  4 ", "		5 ", "" };

		String[] trimed = trim(data);

		assertEquals(3 + "", trimed[0]);
		assertEquals(4 + "", trimed[1]);
		assertEquals(5 + "", trimed[2]);
	}

	@Test
	public void removeNulls() {
		String[] data = new String[] { "					\r\n", "3\tnull\t4", "  4 ", "		5 ", "" };

		String[] trimed = trim(data);

		assertEquals("3\t\t4", trimed[0]);
		assertEquals(4 + "", trimed[1]);
		assertEquals(5 + "", trimed[2]);
	}

	@Test
	public void removeLastNullTogetherWithTab() {
		String[] data = new String[] { "					\r\n", "3\t\t4\tnull", "  4 ", "		5 ", "" };

		String[] trimed = trim(data);

		assertEquals("3\t\t4", trimed[0]);
		assertEquals(4 + "", trimed[1]);
		assertEquals(5 + "", trimed[2]);
	}
	
	@Test
	public void makeIntegerNumbersInteger() {
		String[] data = new String[] { "					\r\n", "3.0\t\t4.0", "  4.0 ", "		5.0 ", "" };

		String[] trimed = trim(data);

		assertEquals("3\t\t4", trimed[0]);
		assertEquals(4 + "", trimed[1]);
		assertEquals(5 + "", trimed[2]);
	}

}
