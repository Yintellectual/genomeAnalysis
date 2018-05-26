package com.spdeveloper.chgc.genome.dependencyDriver;

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
import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.prediction.service.GenePredictionResultCombiner;
import com.spdeveloper.chgc.genome.prediction.service.MissDependencyException;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;
import com.spdeveloper.chgc.genome.util.system.SystemUtil;

@Service
public class BlastAllProteinAnnotation extends AbstractDependencyDriver{
	private static final Path TEST_EXPECTED_FILE =Paths.get("src", "main", "resources", "files", "dependencyCheck", "Pr.nr");
	private static final Path TEST_INPUT_FILE =Paths.get("src", "main", "resources", "files", "dependencyCheck", "pr.fas");			

	@Value("${cmdTemplate.blastAll}")
	String cmdTemplate;

	
	public BlastAllProteinAnnotation() {
		super(TEST_EXPECTED_FILE, TEST_INPUT_FILE);
	}
	
	@Override
	public Path start(Path tempDir, Path... justPrFas ) throws IOException, InterruptedException {
		Path prFas = justPrFas[0];
		
		IntegratedProgram blastAll = new IntegratedProgram(cmdTemplate, null);
		Path blastAllTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "pr.nr");
		blastAll.execute(null, blastAllTempFile, prFas.toFile().getAbsolutePath(), blastAllTempFile.toFile().getAbsolutePath());
		return blastAllTempFile;
	}


}
