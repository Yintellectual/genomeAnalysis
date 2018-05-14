package com.spdeveloper.chgc.genome.service;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.spdeveloper.chgc.genome.visualization.service.FASParser;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FASParserTest {

	@Autowired
	FASParser fastaCounter;

	@Test(expected = IllegalClassFormatException.class)
	public void numbersTriggerIllegalFormatException() throws IOException, IllegalClassFormatException {
		fastaCounter.count("test_numbers.fas");
	}

	@Test(expected = IllegalClassFormatException.class)
	public void charactersExceptTGCATriggerIllegalFormatException() throws IOException, IllegalClassFormatException {
		fastaCounter.count("test_IllegalCharacters.fas");
	}

	@Test
	public void caseInsensitive() throws IOException, IllegalClassFormatException {
		assertEquals(10, fastaCounter.count("test_lowercase10.fas"));
	}
	
	@Test(expected = IllegalClassFormatException.class)
	public void anyNonWordCharactersTriggerException() throws IOException, IllegalClassFormatException {
		fastaCounter.count("test_NonWordCharacters.fas");
	}

	@Test(expected = IllegalClassFormatException.class)
	public void firstCharacterIsGreaterThanOrException() throws IOException, IllegalClassFormatException {
		fastaCounter.count("test_MissingInitialCharacter.fas");
	}

	@Test(expected = IllegalClassFormatException.class)
	public void firstLineProvideNameOrException() throws IOException, IllegalClassFormatException {
		fastaCounter.count("test_MissingName.fas");
	}

	@Test
	public void countFromTheSecondLine() throws IOException, IllegalClassFormatException {
		assertEquals(10l, fastaCounter.count("test_10characters.fas"));
	}

	@Test
	public void sumEqualsSumsOfTGCA() throws IOException, IllegalClassFormatException {
		try (Stream<String> stream = Files.lines(Paths.get("src", "main","resources", "files", "fas","Genome.fas"))) {

			int[] content = stream.skip(1).flatMapToInt(s -> s.chars()).toArray();
			long countT = Arrays.stream(content).filter(c->c=='T'||c=='t').count();
			long countA = Arrays.stream(content).filter(c->c=='A'||c=='a').count();
			long countC = Arrays.stream(content).filter(c->c=='C'||c=='c').count();
			long countG = Arrays.stream(content).filter(c->c=='G'||c=='g').count();
			
			assertEquals(countT+countA+countC+countG, fastaCounter.count("Genome.fas"));
		}
	}
	
	@Test
	public void parseFileForNameAllowsMultipleWords() throws FileNotFoundException, IOException, IllegalClassFormatException {
		String name = fastaCounter.parseFileForName(new FileInputStream(Paths.get("src", "main","resources", "files", "fas","MultipleWordsNameTest.fas").toFile()));
		
		assertEquals("Hello Genome", name);
	}
	
}
