package com.spdeveloper.chgc.genome.visualization.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;

public interface FASParser {
	String[] parseFile(String fileName) throws IOException, IllegalClassFormatException;
	String[] parseFile(InputStream sourceFile) throws IOException, IllegalClassFormatException;
	long count(String fileName) throws IOException, IllegalClassFormatException;
	long count(InputStream fileName) throws IOException, IllegalClassFormatException;
	long count(String[] fasta) throws IOException, IllegalClassFormatException;
	String parseFileForName(InputStream sourceFile)throws IOException, IllegalClassFormatException;
}
