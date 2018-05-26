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

public abstract class AbstractDependencyDriver {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private Path[] testInputFiles;
	private Path testExpectedFile;
	
	public AbstractDependencyDriver() {
		// TODO Auto-generated constructor stub
	}
	public AbstractDependencyDriver(Path testExpectedFile, Path...testInputFiles) {
		this.testInputFiles = testInputFiles;
		this.testExpectedFile = testExpectedFile;
	}
	
	@Value("${dependencyCheck}")
	protected Boolean doDependencyCheck;
	
	public boolean doDependencyCheck() {
		// TODO Auto-generated method stub
		return doDependencyCheck;
	}
	
	@PostConstruct
	public void dependencyCheck() throws IOException, InterruptedException {
		if(SystemUtil.isWindows()) {
			return;
		}
		log.info("*********************************************************");
		if(doDependencyCheck()) {
			log.info("* Dependency Check For "+this.getClass().getName()+" starts now: *");
			try {
				Path tempDir = Files.createTempDirectory("genomeAnalysis");
				Path actual = start(tempDir, testInputFiles);
				
				String comparisonReport = ComparisonUtil.diffs(Files.readAllLines(testExpectedFile).toArray(new String[0]), Files.readAllLines(actual).toArray(new String[0]));
				if(comparisonReport.contains("identical")) {
					
				}else {
					throw new MissDependencyException(comparisonReport);
				}
				
				log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
				FileUtils.deleteDirectory(tempDir.toFile());
			} catch (Exception e) {
				throw new MissDependencyException(this.getClass().getName()+" is not working.", e);
			}
			
			log.info("*"+this.getClass().getName()+" works fine!*");
		}else{
			log.info("*Skip checking "+this.getClass().getName()+"*");
		}
		log.info("*********************************************************");
	}
	
	
	
	public abstract Path start(Path tempDir, Path...inputFiles) throws IOException, InterruptedException ;
	
}

