package com.spdeveloper.chgc.genome.visualization.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;

public interface DNAParser {
	List<DNA> parse(String fileName, int sheet) throws IOException;
	List<DNA> parse(InputStream  sourceFile, int sheet) throws IOException;
}
