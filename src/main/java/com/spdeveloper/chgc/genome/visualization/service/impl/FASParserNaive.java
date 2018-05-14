package com.spdeveloper.chgc.genome.visualization.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.visualization.service.FASParser;

@Service
public class FASParserNaive implements FASParser {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public long count(String fileName) throws IOException, IllegalClassFormatException {
		String[] content = parseFile(fileName);
		return count(content);
	}

	@Override
	public long count(InputStream sourceFile) throws IOException, IllegalClassFormatException {
		String[] content = parseFile(sourceFile);
		return count(content);
	}

	private IllegalClassFormatException throwIllegalClassFormatException(String message)
			throws IllegalClassFormatException {
		throw new IllegalClassFormatException(message);
	}

	@Override
	public String[] parseFile(String fileName) throws IOException, IllegalClassFormatException {
		// TODO Auto-generated method stub
		Path path = Paths.get("src", "main", "resources", "files", "fas", fileName).toAbsolutePath();
		log.info("Looking for the .fas: " + path);
		try (Stream<String> stream = Files.lines(path)) {

			String[] lines = stream.parallel().map(s -> s.trim()).map(s -> s.toUpperCase()).toArray(String[]::new);
			if (lines.length < 1) {
				throw new IllegalClassFormatException("The .fas file " + fileName + " is empty.");
			} else {
				String firstLine = lines[0];
				if (firstLine.charAt(0) != '>') {
					throw new IllegalClassFormatException(
							"The .fas file " + fileName + " misses the initial character \">\"");
				} else if (firstLine.length() <= 1) {
					throw new IllegalClassFormatException("The .fas file " + fileName + " misses name \">\"");
				}
			}
			return Arrays.copyOfRange(lines, 1, lines.length);
		}
	}

	@Override
	public String[] parseFile(InputStream sourceFile) throws IOException, IllegalClassFormatException {
		String[] lines = validateFile(sourceFile);
		sourceFile.close();
		return Arrays.stream(lines, 1, lines.length).map(s -> s.toUpperCase()).toArray(String[]::new);
	}

	private String[] validateFile(InputStream sourceFile) throws IOException, IllegalClassFormatException {

		try (Stream<String> stream = new BufferedReader(new InputStreamReader(sourceFile)).lines()) {

			String[] lines = stream.parallel().map(s -> s.trim()).toArray(String[]::new);
			if (lines.length < 1) {
				throw new IllegalClassFormatException("The .fas file is empty.");
			} else {
				String firstLine = lines[0];
				if (firstLine.charAt(0) != '>') {
					throw new IllegalClassFormatException("The .fas file misses the initial character \">\"");
				} else if (firstLine.length() <= 1) {
					throw new IllegalClassFormatException("The .fas file misses name \">\"");
				}
			}
			return lines;
		}
	}

	@Override
	public long count(String[] content) throws IOException, IllegalClassFormatException {
		// TODO Auto-generated method stub

		long countIllegalCharacters = Arrays.stream(content).parallel().flatMapToInt(s -> s.chars())
				.filter(c -> (c != 'T' && c != 'A' && c != 'C' && c != 'G'&& c != 'N')).count();
		if (countIllegalCharacters != 0) {
			throw new IllegalClassFormatException(
					"The .fas file " + " contains " + countIllegalCharacters + " illegal characters.");
		} else {
			return Arrays.stream(content).parallel().flatMapToInt(s -> s.chars()).parallel().count();
		}
	}

	@Override
	public String parseFileForName(InputStream sourceFile) throws IOException, IllegalClassFormatException {
		String[] lines = validateFile(sourceFile);
		sourceFile.close();
		return lines[0].split(">")[1].trim();
	}
}
