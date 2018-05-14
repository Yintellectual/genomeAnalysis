package com.spdeveloper.chgc.genome.annotation.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.spdeveloper.chgc.genome.prediction.service.GenePredictionResultCombiner;
import com.spdeveloper.chgc.genome.prediction.service.MissDependencyException;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;

public class BlastAllProteinAnnotation {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${cmdTemplate.blastAll}")
	String cmdTemplate;

	@Autowired
	GenePredictionResultCombiner genePredictionResultCombiner;

	@PostConstruct
	public void dependencyCheck() throws IOException, InterruptedException {
		try {
			File prFas = Paths.get("src", "main", "resources", "files", "dependencyCheck", "pr.fas").toFile();
			Path tempDir = Files.createTempDirectory("genomeAnalysis");
			File actual = blastAll(prFas, tempDir);
			Path expected = Paths.get("src", "main", "resources", "files", "dependencyCheck", "Pr.nr");
			String comparisonReport = ComparisonUtil.diffs(Files.readAllLines(expected).toArray(new String[0]), Files.readAllLines(actual.toPath()).toArray(new String[0]));
			if(comparisonReport.contains("identical")) {
				
			}else {
				throw new MissDependencyException(comparisonReport);
			}
			log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
			FileUtils.deleteDirectory(tempDir.toFile());
		} catch (Exception e) {
			throw new MissDependencyException("blastAll is not working.", e);
		}
	}
	
	public File blastAll(File prFas, Path tempDir) throws IOException, InterruptedException {
		IntegratedProgram blastAll = new IntegratedProgram(cmdTemplate, null);
		Path blastAllTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "pr.nr");
		return blastAll.execute(null, blastAllTempFile, prFas.getAbsolutePath(), blastAllTempFile.toFile().getAbsolutePath());
	}
}
