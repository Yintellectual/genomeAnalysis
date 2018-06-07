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
public class GenePredictionNAnnotationController2 {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	GenePredictionParser genePredictionParser;
	@Autowired
	GenePredictionResultCombiner genePredictionResultCombiner;
	@Autowired
	GeneExtractor geneExtractor;
	@Autowired
	GeneToProteinTranslate geneToProteinTranslate;
	@Autowired
	BlastAllProteinAnnotation blastAllProteinAnnotation;
	@Autowired
	RpsBlastProteinAnnotation rpsBlastProteinAnnotation;
	@Autowired
	TRNAScan tRNAScan;
	@Autowired
	RNAmmer rnammer;
	@Autowired
	AnnotationExcelWriter AnnotationExcelWriter;
	
	
	
	@PostMapping("/genomeAnnotation")
	public ResponseEntity<Resource> handleFileUpload(@RequestParam("fas") MultipartFile fas) throws IOException, InterruptedException {
		
		String fastaName = "";
		
		List<RnaAnnotated> rnaAnnotateds = new ArrayList<>();
		
		//save all the files under a temporary directory which is deleted later
		Path tempDir = Files.createTempDirectory("genomeAnalysis");
		
		InputStream initialStream = fas.getInputStream();
		byte[] buffer = new byte[initialStream.available()];
		initialStream.read(buffer);
				
		Path fastaFile = Files.createTempFile(tempDir, "genomeAnalysis", "fasta.fas");
		OutputStream outStream = new FileOutputStream(fastaFile.toFile());
		outStream.write(buffer);
		outStream.close();

		List<GenePrediction> genePrediction = genePredictionResultCombiner.combine(fastaFile.toFile(), tempDir);
		fastaName = Files.readAllLines(fastaFile).get(0).split(">")[1]; 
				
		Path geneFasFile = geneExtractor.extract(fastaFile.toFile(), genePrediction, tempDir).toPath();
	    
	    Path translatedFile = geneToProteinTranslate.translate(geneFasFile.toFile(), tempDir).toPath();
	    
	    Path blastResult = blastAllProteinAnnotation.start(tempDir, translatedFile);
	    
	    BlastOutput blastOutput = M7Parser.parse(blastResult.toFile());
	    Flux<PrMatch> nrPrMatches = Flux.fromIterable(blastOutput.getBlastOutput_iterations().getPrMatchs());
	    
	    Path cogResult = rpsBlastProteinAnnotation.start(tempDir, translatedFile);
	    
	    BlastOutput cogOutput = M7Parser.parse(cogResult.toFile());
	    Flux<PrMatch> cogPrMathes= Flux.fromIterable(cogOutput.getBlastOutput_iterations().getPrMatchs());
	    
	    Flux<GenePrediction> genePredictionFlux = Flux.fromIterable(genePrediction);
	    List<GeneAnnotated> geneAnnotateds = Flux.zip(genePredictionFlux, nrPrMatches, cogPrMathes).map(t3->{
	    	return new GeneAnnotated(t3.getT1(), t3.getT2(), t3.getT3());
	    }).collectList().block();
	    
	    //generate rnaAnnotations using both tRNAScanner and RNAmmer
	    Path tRNAScanResult = tRNAScan.start(tempDir, fastaFile);
	    List<RnaAnnotated> tRNAAnnotateds = Files.readAllLines(tRNAScanResult).stream().map(RnaAnnotated::parseTRNAscan).filter(e->e!=null).collect(Collectors.toList());
	    RnaAnnotated.generateNameByIndexNumber(tRNAAnnotateds, RNAType.tRNA);
	    rnaAnnotateds.addAll(tRNAAnnotateds);
	    
	    Path rnammerResult = rnammer.start(tempDir, fastaFile);
	    List<RnaAnnotated> rRNAAnnotateds = Files.readAllLines(rnammerResult).stream().map(RnaAnnotated::parseRNAmmer).filter(e->e!=null).collect(Collectors.toList());
	    RnaAnnotated.generateNameByIndexNumber(rRNAAnnotateds, RNAType.rRNA);
	    rnaAnnotateds.addAll(rRNAAnnotateds);
	    
	    
	    //write both geneAnnotations and rnaAnnotations to a xlsx file, with geneAnnotations be the first sheet and rnaAnnotations be the second. 
	    Path annotationFile = Files.createTempFile(tempDir, "genomeAnalysis", "Annotation.xlsx");
	    //WriteToFileUtil.writeToFile(geneAnnotateds, annotationFile);
	    AnnotationExcelWriter.write(annotationFile, geneAnnotateds, rnaAnnotateds);
	    
	    //zip resulting files: fasta(the input file), gene.fas, pr.fas, and Annotation.xlsx
	    ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
	    HashMap<String, Path> resultingFiles = new HashMap<String, Path>(){{
	    	put("gene.fas", geneFasFile);
	    	put("pr.fas", translatedFile);
	    	put("Annotation.xlsx", annotationFile);
	    }};
	    resultingFiles.put(fastaName+".fasta", fastaFile);
	    ZipDirectory.doZipFiles(resultingFiles, zipBuffer);
	    
	    //ready the downloadable
	    HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		responseHeaders.set("charset", "utf-8");
		responseHeaders.set("Content-disposition", "attachment; filename=" + fastaName+".zip");
		Resource resource = new InputStreamResource(new ByteArrayInputStream(zipBuffer.toByteArray()));
		ResponseEntity<Resource> result = new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
		
		//delete all temporary files with the folder
		log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
		//log.info("Deletion Disabled, tempDir="+tempDir.toFile().getAbsolutePath());
		FileUtils.deleteDirectory(tempDir.toFile());
		
		return result;
	}
}
