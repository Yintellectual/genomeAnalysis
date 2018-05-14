package com.spdeveloper.chgc.genome.util.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ExcelToMapsParser {
	List<Map<String, String>> parse(File sourceFile, int sheet) throws IOException;

	List<Map<String, String>> parse(InputStream sourceFile, int sheetIndex) throws IOException;
}
