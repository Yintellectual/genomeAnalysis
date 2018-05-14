package com.spdeveloper.chgc.genome.visualization.service.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParser;
import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParserFactory;
import com.spdeveloper.chgc.genome.visualization.service.COGColorParser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class COGColorParserNaive implements COGColorParser {
	private final ObjectMapper mapper = new ObjectMapper(); 
	private ExcelToMapsParserFactory excelToMapsParserFactory = new ExcelToMapsParserFactory() {};  
	private ExcelToMapsParser excelToMapsParser = excelToMapsParserFactory.create(new HashMap<Integer, String>(){{
		put(0, "value");
		put(1, "key");
	}},  0, false, null);	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Couple{
		private String key;
		private String value;
	}

	@Override
	public Map<String, String> parse(String fileName, int sheet) throws IOException {
		// TODO Auto-generated method stub
		List<Map<String, String>> data = excelToMapsParser.parse(Paths.get("src", "main", "resources", "files", "cog_color", fileName).toFile(), sheet);
		List<Couple> asCouples = data.parallelStream().map(m->mapper.convertValue(m, Couple.class)).collect(Collectors.toList());
		Map<String, String> result = new HashMap<>();
		for(Couple couple:asCouples) {
			result.put(couple.getKey(), couple.getValue());
		}
		return result;
	}
	
}
