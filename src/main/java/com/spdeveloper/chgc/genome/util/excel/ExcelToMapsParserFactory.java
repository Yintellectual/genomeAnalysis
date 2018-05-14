package com.spdeveloper.chgc.genome.util.excel;

import java.util.Map;

import com.spdeveloper.chgc.genome.util.excel.impl.ExcelToMapsParserNaive;

public abstract class ExcelToMapsParserFactory {
	public ExcelToMapsParser create(Map<Integer, String> fieldNames, Integer startIndex, Boolean validateColumns, Map<String, String> fieldNamesValidator) {
		return new ExcelToMapsParserNaive(fieldNames, startIndex, validateColumns, fieldNamesValidator);
	}
}
