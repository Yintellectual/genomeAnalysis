package com.spdeveloper.chgc.genome.prediction.service;

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
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;
import com.spdeveloper.chgc.genome.util.system.SystemUtil;

@Service
public class GeneToProteinTranslate {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${cmdTemplate.translate}")
	String cmdTemplate;

	@PostConstruct
	public void dependencyCheck() throws IOException, InterruptedException {
		if(SystemUtil.isWindows()) {
			return ;
		}
		try {
			File geneFas = Paths.get("src", "main", "resources", "files", "dependencyCheck", "gene.fas").toFile();
			Path tempDir = Files.createTempDirectory("genomeAnalysis");
			File actual = translateOnly(geneFas, tempDir);
			Path expected = Paths.get("src", "main", "resources", "files", "dependencyCheck", "pr.fas");
			String comparisonReport = ComparisonUtil.diffs(Files.readAllLines(expected).toArray(new String[0]), Files.readAllLines(actual.toPath()).toArray(new String[0]));
			if(comparisonReport.contains("identical")) {
				
			}else {
				throw new MissDependencyException(comparisonReport);
			}
			log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
			FileUtils.deleteDirectory(tempDir.toFile());
		} catch (Exception e) {
			throw new MissDependencyException("translateScript is not working.", e);
		}
	}

	public File translate(File geneFas, Path tempDir) throws IOException, InterruptedException {
		return edit(translateOnly(geneFas, tempDir).toPath(), tempDir);
	}
	
	public File translateOnly(File geneFas, Path tempDir) throws IOException, InterruptedException {
		IntegratedProgram translate = new IntegratedProgram(cmdTemplate, null);
		Path translateTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "rawPr.fas");
		translate.execute(null, translateTempFile, geneFas.getAbsolutePath(), translateTempFile.toFile().getAbsolutePath());
		return translateTempFile.toFile();
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
