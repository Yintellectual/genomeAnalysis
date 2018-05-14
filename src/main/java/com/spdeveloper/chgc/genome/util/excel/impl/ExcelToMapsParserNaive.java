package com.spdeveloper.chgc.genome.util.excel.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParser;
import com.spdeveloper.chgc.genome.util.excel.WrongExcelFormatException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExcelToMapsParserNaive implements ExcelToMapsParser {

	private final Map<Integer, String> fieldNames;
	private final Integer startIndex;
	private final boolean validateColumns;
	private final Map<String, String> fieldNamesValidator;
	
	@Override
	public List<Map<String, String>> parse(File sourceFile, int sheetIndex) throws IOException {
		// TODO Auto-generated method stub

		FileInputStream file = new FileInputStream(sourceFile);
		return parse(file, sheetIndex);
	}

	@Override
	public List<Map<String, String>> parse(InputStream sourceFile, int sheetIndex) throws IOException {
		// TODO Auto-generated method stub

		Workbook workbook = new XSSFWorkbook(sourceFile);

		Sheet sheet = workbook.getSheetAt(sheetIndex);

		List<Map<String, String>> data = new LinkedList<>();

		if (validateColumns) {
			Row firstRow = sheet.getRow(0);
			for (int i : fieldNames.keySet()) {
				String columnInFile;
				String expectedColumn;
				try {
					columnInFile = firstRow.getCell(i).getStringCellValue().replaceAll("\\W", "_").trim().toUpperCase();
					expectedColumn = fieldNamesValidator.get(fieldNames.get(i)).replaceAll("\\W", "_").trim()
							.toUpperCase();
				} catch (Exception e) {
					throw new WrongExcelFormatException(e);
				}
				if (!expectedColumn.equals(columnInFile)) {
					throw new WrongExcelFormatException("Expected " + i + "th column \"" + expectedColumn + "\", but \""
							+ columnInFile);
				}
			}
		}

		for (int i = startIndex; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			Map<String, String> rowData = new LinkedHashMap<>(fieldNames.size());
			for (int j = 0; j <= row.getLastCellNum(); j++) {
				String key = fieldNames.get(j);
				Cell cell = row.getCell(j);
				if (key != null) {
					if (cell == null) {
						rowData.put(key, null);
					} else {
						String value = null;
						switch (cell.getCellTypeEnum()) {
						case STRING:
							value = cell.getStringCellValue();
							break;
						case NUMERIC:
							value = "" + cell.getNumericCellValue();
							break;
						case BOOLEAN:
							value = "" + cell.getBooleanCellValue();
							break;
						case FORMULA:
							value = "" + cell.getNumericCellValue();
							break;
						default:
							value = null;
							;
						}
						rowData.put(key, value);
					}
				}
			}
			data.add(rowData);
		}

		return data;
	}
}
