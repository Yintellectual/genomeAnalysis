package com.spdeveloper.chgc.genome.util.excel;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class TwoDimensionalDataToExcelSheet {
	public static void write(ArrayList<Object> headers, ArrayList<ArrayList<Object>> data, Sheet sheet) {
		Row headerRow = sheet.createRow(0);
		writeToRow(headers, headerRow);
		createRows(data, sheet, 1);
		
		// Resize all columns to fit the content size
        for(int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
        	sheet.autoSizeColumn(i);
        }
	}
	public static void write(String[] headers, ArrayList<ArrayList<Object>> data, Sheet sheet) {
		write(new ArrayList<Object>(Arrays.asList(headers)), data, sheet);
	}
	private static void writeToRow(ArrayList<Object> datas, Row row){
		for(int i=0; i<datas.size(); i++) {
			Object data = datas.get(i); 
			Class<? extends Object> dataType = data.getClass();
			if(dataType == Integer.class) {
				int value = ((Integer)data).intValue();
				row.createCell(i).setCellValue(value);
			}else if(dataType == String.class){
				row.createCell(i).setCellValue((String)data);	
			}else {
				throw new RuntimeException("Support ONLY Integer or String types");
			}
		}
	}
	private static void createRows(ArrayList<ArrayList<Object>> data, Sheet sheet, final int initialRowIndex) {
		for(int i=0;i<data.size();i++) {
			Row row = sheet.createRow(i+initialRowIndex);
			writeToRow(data.get(i), row);
		}
	}
}