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
import com.spdeveloper.chgc.genome.prediction.service.GeneToProteinTranslate;
import com.spdeveloper.chgc.genome.prediction.service.MissDependencyException;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;
import com.spdeveloper.chgc.genome.util.system.SystemUtil;

@Service
public class TRNAScan extends AbstractDependencyDriver{
	private static final Path TEST_INPUT_FILE = Paths.get("src", "main", "resources", "files", "dependencyCheck", "short.fas");
	private static final Path TEST_EXPECTED_FILE = Paths.get("src", "main", "resources", "files", "dependencyCheck", "test.trna");
	
	@Value("${cmdTemplate.tRNAscan}")
	String cmdTemplate;

	public TRNAScan() {
		this(TEST_EXPECTED_FILE, TEST_INPUT_FILE);
	}
	public TRNAScan(Path testExpectedFile, Path...testInputFiles){
		super(testExpectedFile, testInputFiles);
	}
	
	@Override
	public Path start(Path tempDir, Path... fas) throws IOException, InterruptedException {
		Path inputFas = fas[0];
		
		IntegratedProgram tRNAscan = new IntegratedProgram(cmdTemplate, null);
		Path tRNAScanTempFile = Files.createTempFile(tempDir, "genomeAnalysis", ".trna");
		tRNAscan.execute(null, tRNAScanTempFile, inputFas.toFile().getAbsolutePath(), tRNAScanTempFile.toFile().getAbsolutePath());
		return tRNAScanTempFile;
	}
}
