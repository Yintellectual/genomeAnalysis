package com.spdeveloper.chgc.genome.util.debug;

import static com.spdeveloper.chgc.genome.util.debug.ComparisonUtil.readLines;
import static com.spdeveloper.chgc.genome.util.debug.ComparisonUtil.readMapFromExcelSheet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ComparisonReportCreator {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private StringBuilder sb = new StringBuilder();
	public ComparisonReportCreator appendLine(String str) {
		sb.append(str);
		log.info(str);
		sb.append("\n");
		return this;
	}

	public ComparisonReportCreator compareAndAppendLine(String[] expected, String[] actual) {
		appendLine(compare(expected, actual));
		return this;
	}

	public ComparisonReportCreator compareAndAppendLine(File expected, Integer expectedSheet, File actual)
			throws IOException {
		if (expectedSheet != null) {
			appendLine("Compare EXPECTED :" + expected.getName() +" "+ expectedSheet + "th sheet " + ""
					+ " against ACTUAL: " + actual.getName() + ":");
			appendLine(compareExcelMapSheets(expected, expectedSheet, actual));
			appendLine("");
		}
		return this;
	}
	public ComparisonReportCreator compareAndAppendLine(InputStream expected, Integer expectedSheet, String[] actual)
			throws IOException {
		if (expectedSheet != null) {
			appendLine("Compare EXPECTED :" + expectedSheet + "th sheet " + ""
					+ " against ACTUAL:");
			appendLine(compareExcelMapSheets(expected, expectedSheet, actual));
			appendLine("");
		}
		expected.close();
		return this;
	}

	public String flush() throws IOException {
		String result = sb.toString();
		sb = new StringBuilder();
		return result;
	}

	private String compareIfNotNull(String[] expected, String[] actual) {
		if (expected == null) {
			return "expected is null";
		}else if(actual == null) {
			return "actual is null";
		} else {
			return ComparisonUtil.diffs(expected, actual);
		}
	}

	private void writeTofile(String fileName, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(Paths.get("src", "main", "resources", "files", "testReport", fileName).toFile()));
		writer.write(content);
		writer.close();
	}

	public String compare(String[] expected, String[] actual) {
		return compareIfNotNull(expected, actual);
	}

	public String compareExcelMapSheets(File expected, Integer expectedSheet, File actual) throws IOException {
		return compare(readMapFromExcelSheet(expected, expectedSheet), readLines(actual));
	}
	
	public String compareExcelMapSheets(InputStream expected, Integer expectedSheet, String[] actual) throws IOException {
		return compare(readMapFromExcelSheet(expected, expectedSheet), actual);
	}
	
}
