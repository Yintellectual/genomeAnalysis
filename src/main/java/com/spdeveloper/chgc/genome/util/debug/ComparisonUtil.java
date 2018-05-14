package com.spdeveloper.chgc.genome.util.debug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParser;
import com.spdeveloper.chgc.genome.util.excel.ExcelToMapsParserFactory;



public class ComparisonUtil {
	public static String diffs(String[] expected, String[] actual) {
		String[] trimedExpected = trim(expected);
		String[] trimedActual = trim(actual);
		
		if(!sameNumberOfLines(trimedExpected, trimedActual)) {
			return String.format(DiffsResult.Different_Number_Of_Lines.toString(), trimedExpected.length, trimedActual.length);
		}else{
			int[] differentLines = findDifferentLines(trimedExpected, trimedActual);
			if(differentLines==null||differentLines.length==0) {
				return DiffsResult.Identical.toString();
			}else {
				int firstDifferentLine = differentLines[0];
				return String.format(DiffsResult.First_Different_Line.toString(), firstDifferentLine, trimedExpected[firstDifferentLine], trimedActual[firstDifferentLine]);
			}
		}
	}
	public static enum DiffsResult {
		Different_Number_Of_Lines{
			@Override
			public String toString() {
				return "Number of lines are different, expected=%d, while actual=%d";
			}
		}, First_Different_Line{
			@Override
			public String toString() {
				return "The first different line is the %dth line, expected=%s, while actual=%s";
			}
		}, Identical{
			@Override
			public String toString() {
				return "The two files are identical";
			}
		};
	}
	public static String[] trim(String[] data) {
		return Arrays.stream(data).parallel()
				.map(line->line.replaceAll("[\r\n]", ""))
				.filter(line->line!=null)
				.map(line->line.trim())
				.map(ComparisonUtil::removeNulls)
				.map(ComparisonUtil::recoverIntegers)
				.map(line->line.trim())
				.filter(line->!line.isEmpty()).toArray(String[]::new);
	} 
	
	public static String removeNulls(String data) {
		
		String result = data.replaceAll("(^|\t)(null)", "\t");
		
		return result;

	}
	public static String recoverIntegers(String data) {
		Pattern pattern = Pattern.compile("((^|\t)-?[\\d]+)(\\.0)");
		Matcher matcher = pattern.matcher(data);
		
		return matcher.replaceAll("$1");
		
	}
	public static boolean sameNumberOfLines(String[] expected, String[] actual) {
		return expected.length == actual.length;
	}
	public static int[] findDifferentLines(String[] expected, String[] actual) {
		return IntStream.range(0, expected.length).parallel().filter(i->{
			return !expected[i].equals(actual[i]);
		}).toArray();
	}
	public static String mapValuesToLine(Map<String, String> m) {
		int size = m.size();
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<size;i++) {
			sb.append(m.get(i+""));
			sb.append("\t");
		}
		return sb.toString();
	}
	
	public static String[] readMapFromExcelSheet(File file, Integer sheet_index) throws IOException {
		ExcelToMapsParser mapFileParser = new ExcelToMapsParserFactory() {
		}.create(new HashMap<Integer, String>() {
			{
				put(0, "0");
				put(1, "1");
				put(2, "2");
				put(3, "3");
				put(4, "4");
				put(5, "5");
			}
		}, 0, false, null);

		return mapFileParser
				.parse(file, sheet_index)
				.parallelStream().map(m -> {
					return ComparisonUtil.mapValuesToLine(m);
				}).toArray(String[]::new);
	}
	public static String[] readMapFromExcelSheet(InputStream sourceFile, Integer sheet_index) throws IOException {
		ExcelToMapsParser mapFileParser = new ExcelToMapsParserFactory() {
		}.create(new HashMap<Integer, String>() {
			{
				put(0, "0");
				put(1, "1");
				put(2, "2");
				put(3, "3");
				put(4, "4");
				put(5, "5");
			}
		}, 0, false, null);

		return mapFileParser
				.parse(sourceFile, sheet_index)
				.parallelStream().map(m -> {
					return ComparisonUtil.mapValuesToLine(m);
				}).toArray(String[]::new);
	}

	
	public static String[] readLines(File file) throws IOException {
		return Files.readAllLines(file.toPath()).toArray(new String[] {});
	}
	
}
