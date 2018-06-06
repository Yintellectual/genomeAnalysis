package com.spdeveloper.chgc.genome.util.excel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.annotation.entity.GeneAnnotated;



@Service
public class GeneAnnotatedToExcel {

	private Row createHeaderRow(Sheet sheet) {
		Row row = sheet.createRow(0);

		row.createCell(0).setCellValue("Gene");

		row.createCell(1).setCellValue("Start");

		row.createCell(2).setCellValue("Stop");

		row.createCell(3).setCellValue("Length (BP)");

		row.createCell(4).setCellValue("Product");

		row.createCell(5).setCellValue("Best Hit Organism");

		row.createCell(6).setCellValue("KO");

		row.createCell(7).setCellValue("COG");

		row.createCell(8).setCellValue("COG Class");

		return row;
	}
	
	private String mapNullToEmptyString(String str) {
		if(str==null) {
			return "";
		}else {
			return str.trim();
		}
	} 
	
	private void geneAnnotatedToRow(GeneAnnotated geneAnnotated, Row row) {
		row.createCell(0).setCellValue(mapNullToEmptyString(geneAnnotated.getName()));
		int start = 0;
		int stop = 0;
		if (geneAnnotated.getPositive()) {
			start = geneAnnotated.getStart();
			stop = geneAnnotated.getStop();
		} else {
			stop = geneAnnotated.getStart();
			start = geneAnnotated.getStop();
		}

		row.createCell(1).setCellValue(start);

		row.createCell(2).setCellValue(stop);

		row.createCell(3).setCellValue(geneAnnotated.getLength());

		row.createCell(4).setCellValue(mapNullToEmptyString(geneAnnotated.getProduct()));

		row.createCell(5).setCellValue(mapNullToEmptyString(geneAnnotated.getBest_hit_organism()));

		row.createCell(6).setCellValue(mapNullToEmptyString(geneAnnotated.getKo()));

		row.createCell(7).setCellValue(mapNullToEmptyString(geneAnnotated.getCog()));

		row.createCell(8).setCellValue(mapNullToEmptyString(geneAnnotated.getCog_class()));

	}

	public void write(Path destinyFile, List<GeneAnnotated> data) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		CreationHelper createHelper = workbook.getCreationHelper();
		Sheet sheet0_dna = workbook.createSheet("DNA");

		// Create a Font for styling header cells
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.BLACK.getIndex());
		// Create a CellStyle with the font
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		createHeaderRow(sheet0_dna);
		for(int i=0;i<data.size();i++) {
			Row row = sheet0_dna.createRow(1+i);
			geneAnnotatedToRow(data.get(i), row);
		}
		
		// Resize all columns to fit the content size
        for(int i = 0; i < sheet0_dna.getRow(0).getLastCellNum(); i++) {
        	sheet0_dna.autoSizeColumn(i);
        }
        
        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(destinyFile.toFile().getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
	}
}
