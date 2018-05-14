package com.spdeveloper.chgc.genome.prediction.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;


@Service
public class GenePredictionParserNaive implements GenePredictionParser {

	@Override
	public List<GenePrediction> parse(List<String> glimmerPredictionFile, Function<String, GenePrediction> parser) {
		return glimmerPredictionFile.parallelStream()
		.map(parser)
		.filter(g->g!=null)
		.collect(Collectors.toList());
	}

	@Override
	public List<GenePrediction> parse(InputStream glimmerPredictionFile, Function<String, GenePrediction> parser) throws IOException{
		// TODO Auto-generated method stub
		return parse(new BufferedReader(new InputStreamReader(glimmerPredictionFile)).lines().collect(Collectors.toList()), parser);
	}

	@Override
	public List<GenePrediction> parse(MultipartFile glimmerPredictionFile, Function<String, GenePrediction> parser) throws IOException {
		// TODO Auto-generated method stub
		return parse(glimmerPredictionFile.getInputStream(), parser);
	}

	@Override
	public List<GenePrediction> parse(Path glimmerPredictionFile, Function<String, GenePrediction> parser) throws IOException {
		// TODO Auto-generated method stub
		return parse(Files.readAllLines(glimmerPredictionFile), parser);
	}

}
