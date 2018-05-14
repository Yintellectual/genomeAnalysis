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
		File fasta = Paths.get("src", "main", "resources", "files", "dependencyCheck", "short.fas").toFile();
		Path tempDir = Files.createTempDirectory("genomeAnalysis");
		List<GenePrediction> predictions = genePredictionResultCombiner.combine(fasta, tempDir);
		extract(fasta, predictions, tempDir);
		log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
		FileUtils.deleteDirectory(tempDir.toFile());

	}
	
	public File extract(File fastaFile, List<GenePrediction> predictions, Path tempDir) throws IOException, InterruptedException {
		Path predictionsFile = writeToFile(predictions);
		return extract(fastaFile, predictionsFile.toFile(), tempDir);
	}
	public File extract(File fastaFile, File predictions, Path tempDir) throws IOException, InterruptedException {
		IntegratedProgram extractor = new IntegratedProgram(cmdTemplate, null);
		Path extractorTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "gene.fas");
		extractor.execute(null, extractorTempFile, fastaFile.getAbsolutePath(),predictions.getAbsolutePath(), extractorTempFile.toFile().getAbsolutePath());
		return extractorTempFile.toFile();
	}
	private Path writeToFile(List<GenePrediction> predictions) throws IOException {
		Path path = Paths.get("root","gene.xlsx");
		WriteToFileUtil.writeToFile(predictions, path);
		return path;
	}
}
