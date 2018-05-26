package com.spdeveloper.chgc.genome.dependencyDriver;

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
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;
import com.spdeveloper.chgc.genome.prediction.service.MissDependencyException;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.system.SystemUtil;

@Service
public class GlimmerGenePrediction {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	GenePredictionParser genePredictionParser;
	
	@Value("${cmdTemplate.glimmerGenePrediction}")
	String cmdTemplate;
	
	
	
	@PostConstruct
	public void dependencyCheck() {
		if(SystemUtil.isWindows()) {
			return;
		}
		try {
			Path tempDir = Files.createTempDirectory("genomeAnalysis");
			List<GenePrediction> glimmerPrediction = getGenePredictions(Paths.get("src", "main", "resources", "files", "dependencyCheck", "short.fas").toFile(), tempDir);
			List<GenePrediction> expected = genePredictionParser.parse(Paths.get("src", "main", "resources", "files", "dependencyCheck", "tag.predict"),GenePredictionParser::fromGlimmerPrediction);
			String comparisonReport = ComparisonUtil.diffs(expected.stream().map(g->g.toString()).toArray(String[]::new), glimmerPrediction.stream().map(g->g.toString()).toArray(String[]::new));
			if(comparisonReport.contains("identical")) {
				
			}else {
				throw new MissDependencyException(comparisonReport);
			}
			log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
			FileUtils.deleteDirectory(tempDir.toFile());
		}catch(Exception e){
			throw new MissDependencyException("Glimmer is not working.", e);
		}
	}
	
	
	public List<GenePrediction> getGenePredictions(File fasta, Path tempDir) throws IOException, InterruptedException{
		IntegratedProgram glimmer = new IntegratedProgram(cmdTemplate, null);
		Path glimmerTempFile = Files.createTempFile(tempDir, "genomeAnalysis", ".predict");
		glimmer.execute(null, glimmerTempFile, fasta.getAbsolutePath(),  FilenameUtils.removeExtension(glimmerTempFile.toFile().getAbsolutePath()));
		
		return genePredictionParser.parse(glimmerTempFile, GenePredictionParser::fromGlimmerPrediction);
	} 	
}
