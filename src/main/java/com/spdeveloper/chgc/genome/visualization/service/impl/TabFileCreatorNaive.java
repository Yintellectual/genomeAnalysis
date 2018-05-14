package com.spdeveloper.chgc.genome.visualization.service.impl;

import static com.spdeveloper.chgc.genome.visualization.factory.TabFileTileFactory.toDNATabFileTile;
import static com.spdeveloper.chgc.genome.visualization.factory.TabFileTileFactory.toDNATile;
import static com.spdeveloper.chgc.genome.visualization.factory.TabFileTileFactory.toDNATileStringArray;
import static com.spdeveloper.chgc.genome.visualization.factory.TabFileTileFactory.toRNATabFileTile;
import static com.spdeveloper.chgc.genome.visualization.factory.TabFileTileFactory.toRNATile;
import static com.spdeveloper.chgc.genome.visualization.factory.TabFileTileFactory.toRNATileStringArray;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.service.TabFileCreator;

@Service
public class TabFileCreatorNaive implements TabFileCreator {

	@Override
	public void create(List<DNA> dnas, List<RNA> rnas, String fileName) throws IOException {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		dnas.stream().map(dna->toDNATabFileTile(dna)).map(t->toDNATile(t)).forEach(sb::append);
		rnas.stream().map(rna->toRNATabFileTile(rna)).map(t->toRNATile(t)).forEach(sb::append);
		writeTofile(fileName, sb.toString());
	}
	
	private void writeTofile(String fileName, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get("src", "main", "resources", "files", "tabFiles", fileName).toFile()));
		writer.write(content);
		writer.close();
	}

	@Override
	public String[] createStringArray(List<DNA> dnas, List<RNA> rnas, String fileName) throws IOException {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		List<String> dnaLines = dnas.stream().map(dna->toDNATabFileTile(dna)).map(t->toDNATileStringArray(t)).flatMap(lines->Arrays.stream(lines)).collect(Collectors.toList());
		List<String> rnaLines = rnas.stream().map(rna->toRNATabFileTile(rna)).map(t->toRNATileStringArray(t)).flatMap(lines->Arrays.stream(lines)).collect(Collectors.toList());
		dnaLines.addAll(rnaLines);
		dnaLines.add(0, ">"+fileName);
		return dnaLines.toArray(new String[0]);
	}
}
