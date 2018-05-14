package com.spdeveloper.chgc.genome.prediction.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;

public interface GenePredictionParser {
	List<GenePrediction> parse(List<String> predictionFile,Function<String, GenePrediction> parser);
	List<GenePrediction> parse(InputStream predictionFile, Function<String, GenePrediction> parser) throws IOException;
	List<GenePrediction> parse(MultipartFile predictionFile, Function<String, GenePrediction> parser) throws IOException;
	List<GenePrediction> parse(Path predictionFile, Function<String, GenePrediction> parser) throws IOException;
	
	
	public static GenePrediction fromGlimmerPrediction(String prediction) {
		Pattern pattern = Pattern.compile("(orf\\d*)\\s+(\\d*)\\s+(\\d*)\\s+([-+]\\d*)\\s+(\\d*\\.\\d*)");
		Matcher matcher = pattern.matcher(prediction);
		
		if(matcher.matches()) {
			String name = matcher.group(1);
			int start = Integer.parseInt(matcher.group(2));
			int stop = Integer.parseInt(matcher.group(3));
			int direction = Integer.parseInt(matcher.group(4));
			boolean positive = direction>=0;
			if(!positive) {
				int temp = start;
				start = stop;
				stop = temp;
			}
			return new GenePrediction(name, start, stop, positive);
		}else {
			return null;
		}
	}
	
	
	public static GenePrediction fromZcurvePrediction(String prediction) {
		Pattern pattern = Pattern.compile("(\\d*)\\s+(\\d*)\\s+(\\d*)\\s+([+-])\\s+(\\d*)\\s+(\\d\\.\\d*)");
		Matcher matcher = pattern.matcher(prediction.trim());
		if(matcher.matches()) {
			String name = matcher.group(1);
			int start = Integer.parseInt(matcher.group(2));
			int stop = Integer.parseInt(matcher.group(3));
			boolean positive = "+".equals(matcher.group(4));
			return new GenePrediction(name, start, stop, positive);
		}else {
			return null;
		}
	}
	
}
