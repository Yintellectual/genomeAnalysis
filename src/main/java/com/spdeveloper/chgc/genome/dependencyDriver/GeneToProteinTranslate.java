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

import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.MissDependencyException;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;
import com.spdeveloper.chgc.genome.util.system.SystemUtil;

@Service
public class GeneToProteinTranslate extends AbstractDependencyDriver{
	private static final Path TEST_EXPECTED_FILE =Paths.get("src", "main", "resources", "files", "dependencyCheck", "pr.fas");
	private static final Path TEST_INPUT_FILE =Paths.get("src", "main", "resources", "files", "dependencyCheck", "gene.fas");			

	@Value("${cmdTemplate.translate}")
	String cmdTemplate;

	public GeneToProteinTranslate() {
		super(TEST_EXPECTED_FILE, TEST_INPUT_FILE);
	}

	public File translate(File geneFas, Path tempDir) throws IOException, InterruptedException {
		return edit(start(tempDir, geneFas.toPath()), tempDir);
	}
	
	public Path start(Path tempDir, Path...justGeneFas) throws IOException, InterruptedException {
		Path geneFas = justGeneFas[0];
		
		IntegratedProgram translate = new IntegratedProgram(cmdTemplate, null);
		Path translateTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "rawPr.fas");
		translate.execute(null, translateTempFile, geneFas.toFile().getAbsolutePath(), translateTempFile.toFile().getAbsolutePath());
		return translateTempFile;
	}
	
	public File edit(Path rawPrFas, Path tempDir) throws IOException {
		List<String> translatedData = Files.readAllLines(rawPrFas);
		Path resultingFile = Files.createTempFile(tempDir, "genomeAnalysis", "Pr.fas");
		WriteToFileUtil.writeToFile(
		   		translatedData.stream().map(s->{
		    			if(s.startsWith(">")) {
		    				return s.replaceAll("\\s+.*", "");
		    			}else {
		    				return s.replaceAll("^(V)", "M").replaceAll("^(L)", "M").replaceAll("(\\*)$", "");
		    			}
		    		}).collect(Collectors.toList())
		    		, resultingFile);
		return resultingFile.toFile();
	}
}
