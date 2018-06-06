package com.spdeveloper.chgc.genome.annotation.service;

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
import com.spdeveloper.chgc.genome.annotation.entity.RnaAnnotated;
import com.spdeveloper.chgc.genome.util.excel.TwoDimensionalDataToExcelSheet;

@Service
public class AnnotationExcelWriter {
	public static final String[] DNA_HEADERS;
	public static final String[] RNA_HEADERS;
	static {
		DNA_HEADERS = dnaHeaders();
		RNA_HEADERS = rnaHeaders();
	}

	public void write(Path destinyFile, List<GeneAnnotated> geneAnnotateds, List<RnaAnnotated> rnaAnnotateds)
			throws IOException {
		Workbook workbook = new XSSFWorkbook();
		CreationHelper createHelper = workbook.getCreationHelper();

		// Create a Font for styling header cells
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.BLACK.getIndex());

		// Create a CellStyle with the font
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);

		Sheet sheet0_dna = workbook.createSheet("DNA");
		if (geneAnnotateds != null) {
			TwoDimensionalDataToExcelSheet.write(DNA_HEADERS, GeneAnnotated.listTo2Dimensional(geneAnnotateds), sheet0_dna);
		}
		Sheet sheet1_rna = workbook.createSheet("RNA");
		if (rnaAnnotateds != null) {
			TwoDimensionalDataToExcelSheet.write(RNA_HEADERS, RnaAnnotated.listTo2Dimensional(rnaAnnotateds), sheet1_rna);
		}
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(destinyFile.toFile().getAbsolutePath());
		workbook.write(fileOut);
		fileOut.close();

		// Closing the workbook
		workbook.close();
	}

	private static String[] dnaHeaders() {
		String[] headers = new String[9];

		headers[0] = "Gene";

		headers[1] = "Start";

		headers[2] = "Stop";

		headers[3] = "Length (BP)";

		headers[4] = "Product";

		headers[5] = "Best Hit Organism";

		headers[6] = "KO";

		headers[7] = "COG";

		headers[8] = "COG Class";

		return headers;
	}

	private static String[] rnaHeaders() {
		String[] headers = new String[5];

		headers[0] = "RNA";

		headers[1] = "Start";

		headers[2] = "Stop";

		headers[3] = "Length (BP)";

		headers[4] = "Product";

		return headers;
	}
}
