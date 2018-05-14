package com.spdeveloper.chgc.genome.prediction.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;

@Service
public class GlimmerGenePrediction {
	@Autowired
	GenePredictionParser genePredictionParser;
	
	
	@PostConstruct
	public void dependencyCheck() {
		try {
			Path tempDir = Files.createTempDirectory("genomeAnalysis");
			List<GenePrediction> glimmerPrediction = getGenePredictions(Paths.get("src", "main", "recources", "files", "dependencyCheck", "short.fas").toFile(), tempDir);
			List<GenePrediction> expected = genePredictionParser.parse(Paths.get("src", "main", "recources", "files", "dependencyCheck", "tag.predict"),GenePredictionParser::fromGlimmerPrediction);
			String comparisonReport = ComparisonUtil.diffs(expected.stream().map(g->g.toString()).toArray(String[]::new), glimmerPrediction.stream().map(g->g.toString()).toArray(String[]::new));
			if(comparisonReport.contains("identical")) {
				
			}else {
				throw new MissDependencyException(comparisonReport);
			}
		}catch(Exception e){
			throw new MissDependencyException("Glimmer is not working.", e);
		}
	}
	
	
	public List<GenePrediction> getGenePredictions(File fasta, Path tempDir) throws IOException, InterruptedException{
		String cmdTemplate = " csh /fs/szgenefinding/Glimmer3/scripts/g3-iterated.csh %s %s";
		IntegratedProgram glimmer = new IntegratedProgram(cmdTemplate, null);
		Path glimmerTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "glimmer");
		glimmer.execute(null, glimmerTempFile, fasta.getAbsolutePath(),  FilenameUtils.removeExtension(glimmerTempFile.toFile().getAbsolutePath()));
		
		return genePredictionParser.parse(glimmerTempFile, GenePredictionParser::fromGlimmerPrediction);
	} 	
}
