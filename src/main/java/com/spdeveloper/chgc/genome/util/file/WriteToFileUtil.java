package com.spdeveloper.chgc.genome.util.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteToFileUtil {
	private static final Logger log = LoggerFactory.getLogger(WriteToFileUtil.class);
	public static void writeToFile(List<? extends Object> content, Path file) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		content.forEach(g->{
			stringBuilder.append(g.toString());
			stringBuilder.append("\n");
		});
		BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()));
		writer.write(stringBuilder.toString());
	    writer.close();
	}
}
