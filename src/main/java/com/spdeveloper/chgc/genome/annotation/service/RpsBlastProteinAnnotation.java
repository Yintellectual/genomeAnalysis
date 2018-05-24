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
import org.springframework.stereotype.Service;

import com.spdeveloper.chgc.genome.prediction.service.GenePredictionResultCombiner;
import com.spdeveloper.chgc.genome.prediction.service.GeneToProteinTranslate;
import com.spdeveloper.chgc.genome.prediction.service.MissDependencyException;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;
import com.spdeveloper.chgc.genome.util.system.SystemUtil;

@Service
public class RpsBlastProteinAnnotation {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${cmdTemplate.rpsBlast}")
	String cmdTemplate;

	@Autowired
	GeneToProteinTranslate geneToProteinTranslate;
	
	@PostConstruct
	public void dependencyCheck() throws IOException, InterruptedException {
		if(SystemUtil.isWindows()) {
			return;
		}
		try {
			File prFas = Paths.get("src", "main", "resources", "files", "dependencyCheck", "pr.fas").toFile();
			Path tempDir = Files.createTempDirectory("genomeAnalysis");
			
			File actual = rpsBlast(prFas, tempDir);
			
			Path expected = Paths.get("src", "main", "resources", "files", "dependencyCheck", "Pr.cog");
			String comparisonReport = ComparisonUtil.diffs(Files.readAllLines(expected).toArray(new String[0]), Files.readAllLines(actual.toPath()).toArray(new String[0]));
			if(comparisonReport.contains("identical")) {
				
			}else {
				throw new MissDependencyException(comparisonReport);
			}
			log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
			FileUtils.deleteDirectory(tempDir.toFile());
		} catch (Exception e) {
			throw new MissDependencyException("RpsBlast is not working.", e);
		}
	}
	
	public File rpsBlast(File prFas, Path tempDir) throws IOException, InterruptedException {
		IntegratedProgram rpsBlast = new IntegratedProgram(cmdTemplate, null);
		Path rpsBlastTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "pr.cog");
		rpsBlast.execute(null, rpsBlastTempFile, prFas.getAbsolutePath(), rpsBlastTempFile.toFile().getAbsolutePath());
		log.info(rpsBlastTempFile.toFile().getAbsolutePath());
		return rpsBlastTempFile.toFile();
	}
}
