package com.spdeveloper.chgc.genome.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.spdeveloper.chgc.genome.annotation.service.AnnotationExcelWriter;
import com.spdeveloper.chgc.genome.dependencyDriver.BlastAllProteinAnnotation;
import com.spdeveloper.chgc.genome.dependencyDriver.GeneExtractor;
import com.spdeveloper.chgc.genome.dependencyDriver.GeneToProteinTranslate;
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
	AnnotationExcelWriter AnnotationExcelWriter;
	
	
	
	@PostMapping("/genomeAnnotation")
	public ResponseEntity<Resource> handleFileUpload(@RequestParam("fas") MultipartFile fas) throws IOException, InterruptedException {
		
		//save .fas file under /files/fas/
		InputStream initialStream = fas.getInputStream();
		byte[] buffer = new byte[initialStream.available()];
		initialStream.read(buffer);
				
		File fastaFile = new File("src/main/resources/files/fas/temp.fas");
		OutputStream outStream = new FileOutputStream(fastaFile);
		outStream.write(buffer);
		outStream.close();

		Path tempDir = Files.createTempDirectory("genomeAnalysis");
		List<GenePrediction> genePrediction = genePredictionResultCombiner.combine(fastaFile, tempDir);
		
		Path geneFasFile = geneExtractor.extract(fastaFile, genePrediction, tempDir).toPath();
	    
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
	    Path tRNAScanResult = tRNAScan.start(tempDir, fastaFile.toPath());
	    List<RnaAnnotated> rnaAnnotateds = Files.readAllLines(tRNAScanResult).stream().map(RnaAnnotated::parseTRNAscan).collect(Collectors.toList());
	    
	    //write both geneAnnotations and rnaAnnotations to a xlsx file, with geneAnnotations be the first sheet and rnaAnnotations be the second. 
	    Path annotationFile = Paths.get("src", "main", "resources", "files", "fas", "Annotation.xlsx");
	    //WriteToFileUtil.writeToFile(geneAnnotateds, annotationFile);
	    AnnotationExcelWriter.write(annotationFile, geneAnnotateds, rnaAnnotateds);
	    
	    
	    
	    
	    //zip resulting files: fasta(the input file), gene.fas, pr.fas, and Annotation.xlsx
	    
	    //start download
	    HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		responseHeaders.set("charset", "utf-8");
		responseHeaders.set("Content-disposition", "attachment; filename=" + "Annotation.xlsx");
		Resource resource = new InputStreamResource(new FileInputStream(annotationFile.toFile()));
		//Resource resource = new InputStreamResource(new FileInputStream(translatedFile.toFile()));
		ResponseEntity<Resource> result = new ResponseEntity<>(resource, responseHeaders, HttpStatus.OK);
		
		
		log.info("Delete tempDir: " + tempDir.toFile().getAbsolutePath());
		//log.info("Deletion Disabled, tempDir="+tempDir.toFile().getAbsolutePath());
		FileUtils.deleteDirectory(tempDir.toFile());
		return result;
	}
}
