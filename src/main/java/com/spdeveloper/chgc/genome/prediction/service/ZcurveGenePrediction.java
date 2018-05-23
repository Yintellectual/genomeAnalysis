package com.spdeveloper.chgc.genome.prediction.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;

@Service
public class ZcurveGenePrediction {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	GenePredictionParser genePredictionParser;
	
	@Value("${cmdTemplate.zcurveGenePrediction}")
	String cmdTemplate;
	
	
	
	@PostConstruct
	public void dependencyCheck() {
		String osName = System.getProperty("os.name");
		if(osName.toUpperCase().contains("WINDOWS")){
			return;
		}
		try {
			Path tempDir = Files.createTempDirectory("genomeAnalysis");
			List<GenePrediction> glimmerPrediction = getGenePredictions(Paths.get("src", "main", "resources", "files", "dependencyCheck", "short.fas").toFile(), tempDir);
			List<GenePrediction> expected = genePredictionParser.parse(Paths.get("src", "main", "resources", "files", "dependencyCheck", "zcurve.txt"),GenePredictionParser::fromZcurvePrediction);
			String comparisonReport = ComparisonUtil.diffs(expected.stream().map(g->g.toString()).toArray(String[]::new), glimmerPrediction.stream().map(g->g.toString()).toArray(String[]::new));
			if(comparisonReport.contains("identical")) {
				
			}else {
				throw new MissDependencyException(comparisonReport);
			}
			log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
			FileUtils.deleteDirectory(tempDir.toFile());
		}catch(Exception e){
			throw new MissDependencyException("Zcurve3.0 is not working.", e);
		}
	}
	
	
	public List<GenePrediction> getGenePredictions(File fasta, Path tempDir) throws IOException, InterruptedException{
		IntegratedProgram zcurve3 = new IntegratedProgram(cmdTemplate, null);
		Path zcurve3TempFile = Files.createTempFile(tempDir, "genomeAnalysis", "zcurve.txt");
		zcurve3.execute(null, zcurve3TempFile, fasta.getAbsolutePath(),  zcurve3TempFile.toFile().getAbsolutePath());
		
		return genePredictionParser.parse(zcurve3TempFile, GenePredictionParser::fromZcurvePrediction);
	} 	
}