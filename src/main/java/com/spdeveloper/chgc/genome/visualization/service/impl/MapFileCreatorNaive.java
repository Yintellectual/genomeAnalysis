package com.spdeveloper.chgc.genome.visualization.service.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.MapFileRow;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.entity.Wrapper;
import com.spdeveloper.chgc.genome.visualization.factory.MapFileRowFactory;
import com.spdeveloper.chgc.genome.visualization.service.COGColorParser;
import com.spdeveloper.chgc.genome.visualization.service.DNAParser;
import com.spdeveloper.chgc.genome.visualization.service.FASParser;
import com.spdeveloper.chgc.genome.visualization.service.MapFileCreator;
import com.spdeveloper.chgc.genome.visualization.service.RNAParser;

@Service
public class MapFileCreatorNaive implements MapFileCreator {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	FASParser fasCounter;
	@Autowired
	DNAParser cdsParser;
	@Autowired
	RNAParser rnaParser;
	@Autowired
	COGColorParser cogColorParser;

	private void writeTofile(String fileName, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(Paths.get("src", "main", "resources", "files", "mapFiles", fileName).toFile()));
		writer.write(content);
		writer.close();
	}

	private String createContent(Long count, List<MapFileRow> data) {
		return createContent(createContentAsStringArray(count, data));
	}
	
	private String createContent(String[] lines) {
		StringBuilder sb = new StringBuilder();
		Arrays.stream(lines).forEach(line->{
			sb.append(line);
			sb.append("\n");
		});
		return sb.toString();
	}
	
	private String[] createContentAsStringArray(Long count, List<MapFileRow> data) {
		List<String> content = data.parallelStream().map(r->r.toString()).collect(Collectors.toList());
		content.add(0, count+"");
		return content.toArray(new String[0]);
	}

	@Override
	public void createMapFilesForDNA(List<DNA> dna, Long fasCount) throws IOException {
		List<MapFileRow> cogColors = dna.parallelStream().map(gene -> MapFileRowFactory.toMapFileRow(gene))
				.collect(Collectors.toList());
		List<MapFileRow> positiveCogColors = cogColors.parallelStream().filter(r -> r.getStart() < r.getStop())
				.collect(Collectors.toList());
		List<MapFileRow> negativeCogColors = cogColors.parallelStream().filter(r -> r.getStart() > r.getStop())
				.collect(Collectors.toList());
		writeTofile("DNA_positive.map", createContent(fasCount, positiveCogColors));
		writeTofile("DNA_negative.map", createContent(fasCount, negativeCogColors));
	}

	@Override
	public void createMapFilesForDNA(List<DNA> dna, Long fasCount, Wrapper positiveFile, Wrapper negativeFile) {
		List<MapFileRow> cogColors = dna.parallelStream().map(gene -> MapFileRowFactory.toMapFileRow(gene))
				.collect(Collectors.toList());
		List<MapFileRow> positiveCogColors = cogColors.parallelStream().filter(r -> r.getStart() < r.getStop())
				.collect(Collectors.toList());
		List<MapFileRow> negativeCogColors = cogColors.parallelStream().filter(r -> r.getStart() > r.getStop())
				.collect(Collectors.toList());
		positiveFile.setValue(createContentAsStringArray(fasCount, positiveCogColors));
		negativeFile.setValue(createContentAsStringArray(fasCount, negativeCogColors));
	}
	
	
	@Override
	public void createMapFilesForRNA(List<RNA> rna, Long fasCount) throws IOException {
		// TODO Auto-generated method stub
		List<MapFileRow> cogColors = rna.parallelStream().map(r -> MapFileRowFactory.toMapFileRow(r))
				.collect(Collectors.toList());
		List<MapFileRow> positive = cogColors.parallelStream().filter(r -> r.getStart() < r.getStop())
				.collect(Collectors.toList());
		List<MapFileRow> negative = cogColors.parallelStream().filter(r -> r.getStart() > r.getStop())
				.collect(Collectors.toList());
		writeTofile("RNA_positive.map", createContent(fasCount, positive));
		writeTofile("RNA_negative.map", createContent(fasCount, negative));
	}
	
	@Override
	public void createMapFilesForRNA(List<RNA> rna, Long fasCount, Wrapper positiveFile, Wrapper negativeFile) {
		// TODO Auto-generated method stub
		List<MapFileRow> cogColors = rna.parallelStream().map(r -> MapFileRowFactory.toMapFileRow(r))
				.collect(Collectors.toList());
		List<MapFileRow> positive = cogColors.parallelStream().filter(r -> r.getStart() < r.getStop())
				.collect(Collectors.toList());
		List<MapFileRow> negative = cogColors.parallelStream().filter(r -> r.getStart() > r.getStop())
				.collect(Collectors.toList());
		positiveFile.setValue(createContentAsStringArray(fasCount, positive));
		negativeFile.setValue(createContentAsStringArray(fasCount, negative));

	}

	@Override
	public void createMapFilesForFasta(String[] fasta, long fasCount, int window, int overlap) throws IOException {
		// TODO Auto-generated method stub
		List<MapFileRow> gcList = new ArrayList<>();
		List<MapFileRow> gcSkewList = new ArrayList<>();
		int[] data = Arrays.stream(fasta).flatMapToInt(line->line.chars()).toArray();
		int indentation = window-overlap;
		int size = data.length;
		for(int startIndex=0, j=1;startIndex+overlap<size;startIndex+=indentation, j++) {
			int countC;
			int countG;
			int length;
			if(startIndex+window>size) {
				length = size-startIndex;
				countC = count(data, startIndex, length, 'C');
				countG = count(data, startIndex, length, 'G');
			}else {
				length = window;
				countC = count(data, startIndex, length, 'C');
				countG = count(data, startIndex, length, 'G');
			}
			
			int gc = calculateGC(length, countC, countG);
			int gc_skew = calculateGCSkew(countC, countG);
			gcList.add(new MapFileRow("g"+j, gc+"", "+", startIndex, startIndex+window, "."));
			gcSkewList.add(new MapFileRow("g"+j, gc_skew+"", "+", startIndex, startIndex+window, "."));
		}
		writeTofile("gc.map", createContent(fasCount, gcList));
		writeTofile("gc_skew.map", createContent(fasCount, gcSkewList));
	}
	
	@Override
	public void createMapFilesForFasta(String[] fasta, long fasCount, int window, int overlap, Wrapper gcMap, Wrapper gc_skewMap) throws IOException {
		// TODO Auto-generated method stub
		List<MapFileRow> gcList = new ArrayList<>();
		List<MapFileRow> gcSkewList = new ArrayList<>();
		int[] data = Arrays.stream(fasta).flatMapToInt(line->line.chars()).toArray();
		int indentation = window-overlap;
		int size = data.length;
		for(int startIndex=0, j=1;startIndex+overlap<size;startIndex+=indentation, j++) {
			int countC;
			int countG;
			int length;
			if(startIndex+window>size) {
				length = size-startIndex;
				countC = count(data, startIndex, length, 'C');
				countG = count(data, startIndex, length, 'G');
			}else {
				length = window;
				countC = count(data, startIndex, length, 'C');
				countG = count(data, startIndex, length, 'G');
			}
			
			int gc = calculateGC(length, countC, countG);
			int gc_skew = calculateGCSkew(countC, countG);
			gcList.add(new MapFileRow("g"+j, gc+"", "+", startIndex, startIndex+window, "."));
			gcSkewList.add(new MapFileRow("g"+j, gc_skew+"", "+", startIndex, startIndex+window, "."));
		}
		gcMap.setValue(createContentAsStringArray(fasCount, gcList));
		gc_skewMap.setValue(createContentAsStringArray(fasCount, gcSkewList));
	}

	public static int calculateGC(int length, int countC, int countG) {
		return toPercentage((countC+countG)/(double)length);
	}

	public static int calculateGCSkew(int countC, int countG) {
		return toPercentage((countG-countC)/((double)(countG+countC)));
	}
	public static int toPercentage(double d) {
		return new Double(d*100).intValue();
	}
	public static int count(int[] data, int startIndex, int length, int character) {
		int endExclusive = startIndex+length;
		return (int) Arrays.stream(data, startIndex, endExclusive).filter(c->{
			return c==character;
		}).count();
	}


}
