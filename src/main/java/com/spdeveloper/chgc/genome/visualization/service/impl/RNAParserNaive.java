package com.spdeveloper.chgc.genome.visualization.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParser;
import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParserFactory;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.service.RNAParser;

@Service
public class RNAParserNaive implements RNAParser{
	private final ObjectMapper mapper = new ObjectMapper();
	private ExcelToMapsParserFactory excelToMapsParserFactory = new ExcelToMapsParserFactory() {};  
	private ExcelToMapsParser excelToMapsParser = excelToMapsParserFactory.create(new HashMap<Integer, String>(){{
		put(0, "rna");
		put(1, "start");
		put(2, "stop");
		put(3, "length");
		put(4, "product");
	}}, 1, true, new HashMap<String, String>(){{
		put("rna", "RNA");
		put("start", "Start");
		put("stop", "Stop");
		put("length", "Length (BP)");
		put("product", "Product");
	}});
	@Override
	public List<RNA> parse(String fileName, int sheet) throws IOException {
		// TODO Auto-generated method stub
		return excelToMapsParser.parse(Paths.get("src", "main", "resources", "files", "cds", fileName).toFile(), sheet)
				.stream()
				.map(m->mapper.convertValue(m, RNA.class))
				.collect(Collectors.toList());
	}
	
	@Override
	public List<RNA> parse(InputStream sourceFile, int sheet) throws IOException {
		// TODO Auto-generated method stub
		List<RNA> result = excelToMapsParser.parse(sourceFile, sheet)
				.stream()
				.map(m->mapper.convertValue(m, RNA.class))
				.collect(Collectors.toList());
		return result;
	}

}
