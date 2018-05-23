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
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;

@Service
public class GeneExtractor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${cmdTemplate.extractor}")
	String cmdTemplate;

	@Autowired
	GenePredictionResultCombiner genePredictionResultCombiner;

	@PostConstruct
	public void dependencyCheck() throws IOException, InterruptedException {
		String osName = System.getProperty("os.name");
		if(osName.toUpperCase().contains("WINDOWS")){
			return;
		}
		try {
			File fasta = Paths.get("src", "main", "resources", "files", "dependencyCheck", "short.fas").toFile();
			File predictions = Paths.get("src", "main", "resources", "files", "dependencyCheck", "Gene.xlsx").toFile();
			Path tempDir = Files.createTempDirectory("genomeAnalysis");
			Path actual = extract(fasta, predictions, tempDir).toPath();
			Path expected = Paths.get("src", "main", "resources", "files", "dependencyCheck", "gene.fas");
			String comparisonReport = ComparisonUtil.diffs(Files.readAllLines(expected).toArray(new String[0]), Files.readAllLines(actual).toArray(new String[0]));
			if(comparisonReport.contains("identical")) {
				
			}else {
				throw new MissDependencyException(comparisonReport);
			}
			log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
			FileUtils.deleteDirectory(tempDir.toFile());
		} catch (Exception e) {
			throw new MissDependencyException("Glimmer is not working.", e);
		}
	}

	public File extract(File fastaFile, List<GenePrediction> predictions, Path tempDir)
			throws IOException, InterruptedException {
		Path predictionsFile = writeToFile(predictions, tempDir);
		return extract(fastaFile, predictionsFile.toFile(), tempDir);
	}

	public File extract(File fastaFile, File predictions, Path tempDir) throws IOException, InterruptedException {
		IntegratedProgram extractor = new IntegratedProgram(cmdTemplate, null);
		Path extractorTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "gene.fas");
		extractor.execute(null, extractorTempFile, fastaFile.getAbsolutePath(), predictions.getAbsolutePath(),
				extractorTempFile.toFile().getAbsolutePath());
		return extractorTempFile.toFile();
	}

	private Path writeToFile(List<GenePrediction> predictions, Path tempDir) throws IOException {
		Path path = Files.createTempFile(tempDir, "genomeAnalysis", null);
		WriteToFileUtil.writeToFile(predictions, path);
		return path;
	}
}
