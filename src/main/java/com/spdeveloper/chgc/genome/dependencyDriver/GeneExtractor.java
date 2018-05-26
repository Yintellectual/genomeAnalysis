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
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionResultCombiner;
import com.spdeveloper.chgc.genome.prediction.service.MissDependencyException;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.debug.ComparisonUtil;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;
import com.spdeveloper.chgc.genome.util.system.SystemUtil;

@Service
public class GeneExtractor extends AbstractDependencyDriver{
	private static final Path TEST_EXPECTED_FILE = Paths.get("src", "main", "resources", "files", "dependencyCheck", "gene.fas");
	private static final Path TEST_FASTA =Paths.get("src", "main", "resources", "files", "dependencyCheck", "short.fas");		
	private static final Path TEST_PREDICTIONS =Paths.get("src", "main", "resources", "files", "dependencyCheck", "Gene.xlsx");
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${cmdTemplate.extractor}")
	String cmdTemplate;

	@Autowired
	GenePredictionResultCombiner genePredictionResultCombiner;

	public GeneExtractor() {
		super(TEST_EXPECTED_FILE, TEST_FASTA, TEST_PREDICTIONS);
	}
	
	public File extract(File fastaFile, List<GenePrediction> predictions, Path tempDir)
			throws IOException, InterruptedException {
		Path predictionsFile = writeToFile(predictions, tempDir);
		return start(tempDir, fastaFile.toPath(), predictionsFile).toFile();
	}

	@Override
	public Path start(Path tempDir, Path...fastaAndPredictions) throws IOException, InterruptedException {
		Path fastaFile = fastaAndPredictions[0];
		Path predictions = fastaAndPredictions[1]; 
		
		IntegratedProgram extractor = new IntegratedProgram(cmdTemplate, null);
		Path extractorTempFile = Files.createTempFile(tempDir, "genomeAnalysis", "gene.fas");
		extractor.execute(null, extractorTempFile, fastaFile.toFile().getAbsolutePath(), predictions.toFile().getAbsolutePath(),
				extractorTempFile.toFile().getAbsolutePath());
		return extractorTempFile;
	}

	private Path writeToFile(List<GenePrediction> predictions, Path tempDir) throws IOException {
		Path path = Files.createTempFile(tempDir, "genomeAnalysis", null);
		WriteToFileUtil.writeToFile(predictions, path);
		return path;
	}

}
