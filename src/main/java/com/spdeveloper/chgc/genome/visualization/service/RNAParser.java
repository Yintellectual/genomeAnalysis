package com.spdeveloper.chgc.genome.visualization.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.spdeveloper.chgc.genome.visualization.entity.RNA;

public interface RNAParser {
	List<RNA> parse(String fileName, int sheet) throws IOException;
	List<RNA> parse(InputStream sourceFile, int sheet) throws IOException;
}
