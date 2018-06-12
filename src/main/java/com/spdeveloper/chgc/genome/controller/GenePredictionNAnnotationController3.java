package com.spdeveloper.chgc.genome.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.spdeveloper.chgc.genome.annotation.entity.GeneAnnotated;
import com.spdeveloper.chgc.genome.annotation.entity.RnaAnnotated;
import com.spdeveloper.chgc.genome.annotation.entity.RnaAnnotated.RNAType;
import com.spdeveloper.chgc.genome.annotation.service.AnnotationExcelWriter;
import com.spdeveloper.chgc.genome.annotation.service.FastaToAnnotationService;
import com.spdeveloper.chgc.genome.dependencyDriver.BlastAllProteinAnnotation;
import com.spdeveloper.chgc.genome.dependencyDriver.GeneExtractor;
import com.spdeveloper.chgc.genome.dependencyDriver.GeneToProteinTranslate;
import com.spdeveloper.chgc.genome.dependencyDriver.RNAmmer;
import com.spdeveloper.chgc.genome.dependencyDriver.RpsBlastProteinAnnotation;
import com.spdeveloper.chgc.genome.dependencyDriver.TRNAScan;
import com.spdeveloper.chgc.genome.dependencyDriver.lagency.GlimmerGenePrediction;
import com.spdeveloper.chgc.genome.dependencyDriver.lagency.ZcurveGenePrediction;
import com.spdeveloper.chgc.genome.prediction.entity.GenePrediction;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionParser;
import com.spdeveloper.chgc.genome.prediction.service.GenePredictionResultCombiner;
import com.spdeveloper.chgc.genome.util.cmd.ExecuteCommandAndReadResultingFile;
import com.spdeveloper.chgc.genome.util.cmd.IntegratedProgram;
import com.spdeveloper.chgc.genome.util.file.WriteToFileUtil;
import com.spdeveloper.chgc.genome.util.xml.M7Parser;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.BlastOutput;
import com.spdeveloper.chgc.genome.util.xml.M7Parser.PrMatch;
import com.spdeveloper.chgc.genome.util.zip.ZipDirectory;
import com.spdeveloper.chgc.genome.visualization.entity.Wrapper;

import reactor.core.publisher.Flux;

@Controller
public class GenePredictionNAnnotationController3 {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	FastaToAnnotationService fastaToAnnotationService;
	
	
	@PostMapping("/genomeAnnotation")
	public ResponseEntity<Resource> handleFileUpload(@RequestParam("fas") MultipartFile fas) throws IOException, InterruptedException {
		
		//save fas on disk
		InputStream initialStream = fas.getInputStream();
		byte[] buffer = new byte[initialStream.available()];
		initialStream.read(buffer);
				
		Path fastaFile = Files.createTempFile(Paths.get("."), "genomeAnalysis", "fasta.fas");
		OutputStream outStream = new FileOutputStream(fastaFile.toFile());
		outStream.write(buffer);
		outStream.close();
		
		//get annotation file
		Path result = fastaToAnnotationService.start(fastaFile);
		
	    
	    //ready the download
	    HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		responseHeaders.set("charset", "utf-8");
		responseHeaders.set("Content-disposition", "attachment; filename=" + result.getFileName());
		Resource resource = new InputStreamResource(new FileInputStream(result.toFile()));
		ResponseEntity<Resource> response = new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
		
		return response;
	}
}
