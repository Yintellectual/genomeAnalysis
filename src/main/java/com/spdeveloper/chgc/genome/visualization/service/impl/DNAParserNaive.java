package com.spdeveloper.chgc.genome.visualization.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParser;
import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParserFactory;
import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.service.DNAParser;

@Service
public class DNAParserNaive implements DNAParser{
	private final ObjectMapper mapper = new ObjectMapper();
	private ExcelToMapsParserFactory excelToMapsParserFactory = new ExcelToMapsParserFactory() {};  
	private ExcelToMapsParser excelToMapsParser = excelToMapsParserFactory.create(new HashMap<Integer, String>(){{
		put(0, "gene");
		put(1, "start");
		put(2, "stop");
		put(3, "length");
		put(4, "product");
		put(5, "best_hit_organism");
		put(6, "ko");
		put(7, "cog");
		put(8, "cog_class");
	}},  1, true, new HashMap<String, String>(){{
		put("gene", "Gene");
		put("start", "Start");
		put("stop", "Stop");
		put("length", "Length (BP)");
		put("product", "Product");
		put("best_hit_organism", "Best Hit Organism");
		put("ko", "KO");
		put("cog", "COG");
		put("cog_class", "COG Class");
	}});
	@Override
	public List<DNA> parse(String fileName, int sheet) throws IOException {
		// TODO Auto-generated method stub
		List<Map<String, String>> data = excelToMapsParser.parse(Paths.get("src", "main", "resources", "files", "cds", fileName).toFile(), sheet);
		return data.parallelStream().map(m->mapper.convertValue(m, DNA.class)).collect(Collectors.toList());
	}
	@Override
	public List<DNA> parse(InputStream  sourceFile, int sheet) throws IOException {
		// TODO Auto-generated method stub
		List<Map<String, String>> data = excelToMapsParser.parse(sourceFile, sheet);
		sourceFile.close();
		return data.parallelStream().map(m->mapper.convertValue(m, DNA.class)).collect(Collectors.toList());
	}
}
