package com.spdeveloper.chgc.genome.visualization.service;

import java.io.IOException;
import java.util.List;

import com.spdeveloper.chgc.genome.visualization.entity.DNA;
import com.spdeveloper.chgc.genome.visualization.entity.RNA;
import com.spdeveloper.chgc.genome.visualization.entity.Wrapper;

public interface MapFileCreator{
	void createMapFilesForDNA(List<DNA> dna, Long fasCount)throws IOException;
	void createMapFilesForDNA(List<DNA> dna, Long fasCount, Wrapper positiveFile, Wrapper negativeFile);
	
	
	void createMapFilesForRNA(List<RNA> rna, Long fasCount)throws IOException;
	void createMapFilesForRNA(List<RNA> rna, Long fasCount, Wrapper positiveFile, Wrapper negativeFile);
	
	void createMapFilesForFasta(String[] fasta, long fasCount, int window, int overlap)throws IOException;
	void createMapFilesForFasta(String[] fasta, long fasCount, int window, int overlap, Wrapper gcMap, Wrapper gc_skewMap) throws IOException;
}
